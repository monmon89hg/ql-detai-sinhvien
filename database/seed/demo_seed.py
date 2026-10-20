#!/usr/bin/env python3
"""Explicit LOCAL-ONLY seed, read-only validation, and isolated MySQL 8.4 QA.

Never connects to Railway. No DROP/DELETE/TRUNCATE, FK disabling, or app startup hook.
Test schemas are unique and retained for inspection, never re-used or dropped.
"""
import argparse
from collections import defaultdict
from datetime import datetime
from decimal import Decimal
import getpass
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import uuid

import pymysql
from fixture_tools import BASE, FIXTURES, MANIFEST, REPORTS, REPO, digest, pdf_bytes, provision, verify

TABLE_KEYS = {
    "bo_mon": "ma_bo_mon", "nguoi_dung": "ma_nguoi_dung", "vai_tro": "ma_vai_tro",
    "nguoi_dung_vai_tro": "ma_nguoi_dung", "thong_bao": "ma_thong_bao", "dot_dang_ky": "ma_dot",
    "de_tai": "ma_de_tai", "de_tai_giang_vien": "ma_de_tai", "nhom_sinh_vien": "ma_nhom",
    "thanh_vien_nhom": "ma_nhom", "dang_ky_de_tai": "ma_nhom", "nop_bao_cao": "ma_nhom",
    "hoi_dong": "ma_hoi_dong", "hoi_dong_de_tai": "ma_nhom", "thanh_vien_hoi_dong": "ma_hoi_dong",
    "phieu_cham_diem": "ma_nhom", "ket_qua": "ma_nhom",
}


def statements(sql):
    """Split these SQL fixtures, respecting quoted strings and SQL comments."""
    parts, current, quote, i = [], [], None, 0
    while i < len(sql):
        ch = sql[i]
        if quote:
            current.append(ch)
            if ch == "\\" and i + 1 < len(sql):
                i += 1
                current.append(sql[i])
            elif ch == quote:
                if i + 1 < len(sql) and sql[i + 1] == quote:
                    i += 1
                    current.append(sql[i])
                else:
                    quote = None
        elif ch in "'\"`":
            quote = ch
            current.append(ch)
        elif sql[i:i + 2] == "--" and (i + 2 == len(sql) or sql[i + 2].isspace()):
            end = sql.find("\n", i)
            i = len(sql) if end < 0 else end
            current.append("\n")
            continue
        elif sql[i:i + 2] == "/*":
            end = sql.find("*/", i + 2)
            if end < 0:
                raise ValueError("Unclosed SQL comment")
            i = end + 2
            current.append(" ")
            continue
        elif ch == ";":
            if "".join(current).strip():
                parts.append("".join(current).strip())
            current = []
        else:
            current.append(ch)
        i += 1
    if quote:
        raise ValueError("Unclosed SQL string")
    if "".join(current).strip():
        parts.append("".join(current).strip())
    return parts


def identifier(value):
    return "`" + value.replace("`", "``") + "`"


def query(connection, sql, params=None):
    with connection.cursor() as cursor:
        cursor.execute(sql, params)
        return cursor.fetchall() if cursor.description else []


def local_only(host):
    if host not in {"localhost", "127.0.0.1", "::1"}:
        raise ValueError("LOCAL ONLY: remote hosts (including Railway) are forbidden by this helper")


def connect(args, password, database=None):
    local_only(args.host)
    return pymysql.connect(host=args.host, port=args.port, user=args.user, password=password,
                           database=database, charset="utf8mb4", autocommit=True,
                           connect_timeout=10, read_timeout=60, write_timeout=60,
                           cursorclass=pymysql.cursors.DictCursor)


def assert_target(connection, expected):
    row = query(connection, "SELECT DATABASE() AS db, VERSION() AS version, @@foreign_key_checks AS fks, @@sql_mode AS mode")[0]
    if not expected or row["db"] != expected:
        raise ValueError("Selected schema does not match the explicit expected schema")
    if not row["version"].startswith("8.4.") or row["fks"] != 1 or "STRICT" not in row["mode"]:
        raise ValueError("Requires real MySQL 8.4, strict SQL mode and FK checks enabled")
    query(connection, "SET time_zone = '+07:00'")
    return row


def validation_rows(connection, root):
    manifest = verify(root)
    results = []
    for sql in statements((BASE / "validate_demo_data.sql").read_text(encoding="utf-8")):
        results.extend(query(connection, sql))
    # Inspect EVERY real FK, including composites and optional columns, without writes.
    keys = defaultdict(list)
    for row in query(connection, """SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME,
        REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA=DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL
        ORDER BY TABLE_NAME, CONSTRAINT_NAME, ORDINAL_POSITION"""):
        keys[(row["TABLE_NAME"], row["CONSTRAINT_NAME"], row["REFERENCED_TABLE_NAME"])].append(row)
    for (table, constraint, parent), columns in keys.items():
        join = " AND ".join(f"c.{identifier(r['COLUMN_NAME'])}=p.{identifier(r['REFERENCED_COLUMN_NAME'])}" for r in columns)
        present = " AND ".join(f"c.{identifier(r['COLUMN_NAME'])} IS NOT NULL" for r in columns)
        sql = f"SELECT COUNT(*) AS n FROM {identifier(table)} c WHERE {present} AND NOT EXISTS (SELECT 1 FROM {identifier(parent)} p WHERE {join})"
        count = query(connection, sql)[0]["n"]
        results.append(dict(check_name=f"orphans.{constraint}", violations=count, status="FAIL" if count else "PASS"))
    expected = []
    fields = ["ma_nhom", "phien_ban", "ma_nguoi_nop", "ten_file_goc", "object_key", "mime_type", "kich_thuoc_byte", "sha256", "nop_luc"]
    actual = query(connection, "SELECT " + ",".join(fields) + " FROM nop_bao_cao WHERE LEFT(ma_nhom,5)='DEMO_' ORDER BY ma_nhom,phien_ban")
    for report in manifest["reports"]:
        expected.append({k: datetime.fromisoformat(report[k]) if k == "nop_luc" else report[k] for k in fields})
    valid = actual == sorted(expected, key=lambda r: (r["ma_nhom"], r["phien_ban"]))
    results.append(dict(check_name="pdf.database_metadata_matches_real_files", violations=int(not valid), status="PASS" if valid else "FAIL"))
    return results


def failures(results):
    return [r for r in results if r.get("status") == "FAIL"]


def run_seed(connection, expected_schema, root, sql=None):
    assert_target(connection, expected_schema)
    verify(root)  # No DB inserts until every actual private local file is verified.
    lock_name = "qldt-demo:" + hashlib.sha256(expected_schema.encode()).hexdigest()[:48]
    if query(connection, "SELECT GET_LOCK(%s,10) AS acquired", (lock_name,))[0]["acquired"] != 1:
        raise RuntimeError("Another seed operation holds this schema's advisory lock")
    try:
        query(connection, "SET @demo_expected_schema=%s, @demo_local_fixture_only=1, @demo_fixture_manifest_sha=%s",
              (expected_schema, digest(MANIFEST.read_bytes())))
        source = sql if sql is not None else (BASE / "demo_data.sql").read_text(encoding="utf-8")
        for statement in statements(source):
            if statement.upper() == "COMMIT":
                continue  # The helper validates all inserts inside the transaction BEFORE commit.
            query(connection, statement)
        results = validation_rows(connection, root)
        if failures(results):
            names = [r.get("check_name", r.get("table_name")) for r in failures(results)]
            raise ValueError("Pre-commit validation failed; all new business rows rolled back: " + ", ".join(names))
        connection.commit()
        return results
    except BaseException:
        connection.rollback()
        raise
    finally:
        query(connection, "SELECT RELEASE_LOCK(%s)", (lock_name,))


def snapshot(connection, non_demo=False):
    data = {}
    for table, key in TABLE_KEYS.items():
        where = (" WHERE LEFT(" + identifier(key) + ",5)<>'DEMO_'") if non_demo and table != "vai_tro" else ""
        rows = query(connection, "SELECT * FROM " + identifier(table) + where)
        # Only fingerprints leave the process; password hashes never go into test output.
        normalized = sorted(json.dumps(r, sort_keys=True, default=str, ensure_ascii=False) for r in rows)
        data[table] = digest("\n".join(normalized).encode())
    return data


def create_test_schema(args, password, suffix):
    name = "qldt_demo_qa_" + suffix + "_" + uuid.uuid4().hex[:10]
    with connect(args, password) as admin:
        query(admin, f"CREATE DATABASE {identifier(name)} CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci")
    connection = connect(args, password, name)
    migrations = sorted((REPO / "src/main/resources/db/migration").glob("V[1-5]__*.sql"))
    if len(migrations) != 5:
        raise ValueError("Exactly the approved V1-V5 files are required")
    for file in migrations:
        for sql in statements(file.read_text(encoding="utf-8")):
            query(connection, sql)
    return connection, name


def execute_tests(args, password, root):
    report = {"mysql": None, "schemas_retained": [], "tests": [], "counts": [],
              "migration_method": "Execute the unchanged V1-V5 SQL files in order; not a Flyway engine/startup test"}

    def record(name, status="PASS", detail=None):
        item = dict(name=name, status=status)
        if detail:
            item["detail"] = detail
        report["tests"].append(item)
        print(f"{status} {name}" + (f": {detail}" if detail else ""), flush=True)

    def expect(condition, name):
        if not condition:
            record(name, "FAIL")
            raise AssertionError(name)
        record(name)

    connection = None
    try:
        connection, name = create_test_schema(args, password, "fresh")
        report["schemas_retained"].append(name)
        report["mysql"] = assert_target(connection, name)["version"]
        expect(len(query(connection, "SHOW TABLES")) == 17, "fresh.V1_V5_create_17_business_tables")
        # Valid existing non-demo rows, plus all five immutable V5 role rows.
        query(connection, "INSERT INTO bo_mon(ma_bo_mon,ten_bo_mon) VALUES('KEEP_BO','Existing fictional department')")
        fixture_hash = re.search(r"'([$]2[aby][$][^']+)'", (BASE / "demo_data.sql").read_text()).group(1)
        query(connection, """INSERT INTO nguoi_dung(ma_nguoi_dung,ho_ten,email,mat_khau_hash,loai_nguoi_dung,trang_thai)
            VALUES('KEEP_ADMIN','Existing fictional administrator','keep.admin@example.test',%s,'CAN_BO','KHOA')""", (fixture_hash,))
        query(connection, "INSERT INTO nguoi_dung_vai_tro(ma_nguoi_dung,ma_vai_tro) VALUES('KEEP_ADMIN','ADMIN')")
        query(connection, """INSERT INTO thong_bao(ma_thong_bao,tieu_de,noi_dung,ma_nguoi_dang)
            VALUES('KEEP_TB','Existing fictional draft','Preserve exactly','KEEP_ADMIN')""")
        non_demo_before = snapshot(connection, non_demo=True)
        # A missing SQL opt-in must reject even if someone runs the file directly.
        before = snapshot(connection)
        query(connection, "SET @demo_expected_schema=NULL, @demo_local_fixture_only=NULL, @demo_fixture_manifest_sha=NULL")
        rejected = False
        try:
            for sql in statements((BASE / "demo_data.sql").read_text(encoding="utf-8")):
                query(connection, sql)
        except pymysql.MySQLError as exc:
            rejected = exc.args[0] == 3819
            connection.rollback()
        expect(rejected and before == snapshot(connection), "safety.direct_SQL_without_schema_opt_in_rejected")
        query(connection, "SET @demo_expected_schema='WRONG_SCHEMA'")
        rejected = False
        try:
            for sql in statements((BASE / "demo_data.sql").read_text(encoding="utf-8")):
                query(connection, sql)
        except pymysql.MySQLError as exc:
            rejected = exc.args[0] == 3819
            connection.rollback()
        expect(rejected and before == snapshot(connection), "safety.wrong_expected_schema_rejected")
        try:
            local_only("railway.invalid.test")
        except ValueError:
            record("safety.remote_hosts_blocked_before_connection")
        else:
            raise AssertionError("Remote host accepted")
        results = run_seed(connection, name, root)
        report["counts"] = [r for r in results if "table_name" in r]
        report["validation"] = {
            "total_checks": len([r for r in results if "status" in r]),
            "foreign_key_checks": len([r for r in results if r.get("check_name", "").startswith("orphans.")]),
            "failures": len(failures(results)),
        }
        record("seed.fresh_insertion_and_precommit_business_validation")
        expect(len(report["counts"]) == 17 and not failures(results), "coverage.all_17_expected_counts_and_invariants")
        before = snapshot(connection)
        run_seed(connection, name, root)
        expect(before == snapshot(connection), "seed.second_run_byte_identical_all_17_tables")
        expect(non_demo_before == snapshot(connection, non_demo=True), "seed.non_demo_data_and_V5_roles_unchanged")
        query(connection, "UPDATE nhom_sinh_vien SET ten_nhom='Owner edited demo name', version=version+1 WHERE ma_nhom='DEMO_N01'")
        edited = snapshot(connection)
        run_seed(connection, name, root)
        expect(edited == snapshot(connection), "seed.rerun_preserves_owner_demo_edits")
        pristine = snapshot(connection)

        def db_negative(label, sql, code):
            connection.begin()
            rejected = False
            try:
                query(connection, sql)
            except pymysql.MySQLError as exc:
                rejected = exc.args[0] == code
            finally:
                connection.rollback()
            expect(rejected and snapshot(connection) == pristine, label)

        db_negative("negative.foreign_key_1452", "INSERT INTO nguoi_dung_vai_tro(ma_nguoi_dung,ma_vai_tro) VALUES('MISSING_DEMO_USER','SINH_VIEN')", 1452)
        db_negative("negative.duplicate_membership_PK_1062", "INSERT INTO thanh_vien_nhom(ma_nhom,ma_sinh_vien,yeu_cau_luc) VALUES('DEMO_N06','DEMO_SV18','2026-10-20 10:00:00')", 1062)
        db_negative("negative.two_active_groups_generated_UNIQUE_1062", """INSERT INTO thanh_vien_nhom(ma_nhom,ma_sinh_vien,trang_thai,yeu_cau_luc,tham_gia_luc)
            VALUES('DEMO_N01','DEMO_SV18','THAM_GIA','2026-10-20 10:00:00','2026-10-20 10:00:00')""", 1062)
        db_negative("negative.second_group_leader_generated_UNIQUE_1062", """INSERT INTO thanh_vien_nhom(ma_nhom,ma_sinh_vien,trang_thai,la_nhom_truong,yeu_cau_luc,tham_gia_luc)
            VALUES('DEMO_N06','DEMO_SV38','THAM_GIA',1,'2026-10-20 10:00:00','2026-10-20 10:00:00')""", 1062)
        db_negative("negative.second_committee_chair_generated_UNIQUE_1062", "INSERT INTO thanh_vien_hoi_dong(ma_hoi_dong,ma_giang_vien,chuc_danh) VALUES('DEMO_HD05','DEMO_GV03','CHU_TICH')", 1062)
        for score in ["-0.01", "10.01"]:
            db_negative("negative.grade_" + score + "_CHECK_3819", "UPDATE phieu_cham_diem SET diem=" + score + " WHERE ma_nhom='DEMO_N10' AND ma_giang_vien='DEMO_GV04'", 3819)
        db_negative("negative.result_internal_formula_CHECK_3819", "UPDATE ket_qua SET diem_cuoi=9.99 WHERE ma_nhom='DEMO_N09'", 3819)

        def validator_negative(label, sql, expected_check):
            connection.begin()
            try:
                query(connection, sql)
                bad = failures(validation_rows(connection, root))
                detected = expected_check in [r.get("check_name") for r in bad]
            finally:
                connection.rollback()
            expect(detected and snapshot(connection) == pristine, label)

        validator_negative("negative.cross_table_sum_detected_by_validator", "UPDATE ket_qua SET tong_diem=32.00,diem_cuoi=8.00 WHERE ma_nhom='DEMO_N09'", "results.actual_sum_count_average_and_time")
        validator_negative("negative.missing_required_form_detected_by_identity", "UPDATE phieu_cham_diem SET trang_thai='NHAP',nop_luc=NULL WHERE ma_nhom='DEMO_N09' AND ma_giang_vien='DEMO_GV06'", "results.all_required_forms_submitted")
        validator_negative("negative.same_count_wrong_assessor_identity_rejected", "UPDATE phieu_cham_diem SET ma_giang_vien='DEMO_GV05' WHERE ma_nhom='DEMO_N09' AND ma_giang_vien='DEMO_GV06'", "results.all_required_forms_submitted")
        validator_negative("negative.wrong_assignment_type_detected_by_validator", "UPDATE phieu_cham_diem SET loai_phieu='PHAN_BIEN' WHERE ma_nhom='DEMO_N11'", "grades.exact_assessor_identity_and_type")
        validator_negative("negative.supervisor_in_committee_detected_by_validator", "INSERT INTO thanh_vien_hoi_dong(ma_hoi_dong,ma_giang_vien,chuc_danh) VALUES('DEMO_HD03','DEMO_GV01','UY_VIEN')", "committees.no_supervisor_or_reviewer_conflicts")
        validator_negative("negative.six_members_detected_by_validator_only", """INSERT INTO thanh_vien_nhom(ma_nhom,ma_sinh_vien,trang_thai,la_thanh_vien_chot,yeu_cau_luc,tham_gia_luc)
            VALUES('DEMO_N07','DEMO_SV38','THAM_GIA',1,'2026-10-19 09:00:00','2026-10-19 09:00:00')""", "groups.active_leader_and_capacity")
        record("pdf.six_real_files_size_sha256_and_database_metadata")
        regenerated = [pdf_bytes(*row) for row in REPORTS]
        expect(all(raw == (FIXTURES / f"{row[0]}-v{row[1]}.pdf").read_bytes()
                   for raw, row in zip(regenerated, REPORTS)), "pdf.deterministic_regeneration_matches_committed_bytes")
        with tempfile.TemporaryDirectory(prefix="qldt-fixture-negative-") as temp:
            private = provision(Path(temp))
            first = verify(private)["reports"][0]
            target = private / first["object_key"]
            target.write_bytes(target.read_bytes() + b"tampered")
            try:
                verify(private)
            except ValueError:
                record("negative.tampered_PDF_rejected")
            else:
                raise AssertionError("Tampered PDF accepted")
            try:
                provision(private)
            except ValueError:
                record("negative.existing_different_object_not_overwritten")
            else:
                raise AssertionError("Different object overwritten")

        collision, collision_name = create_test_schema(args, password, "rollback")
        report["schemas_retained"].append(collision_name)
        try:
            query(collision, """INSERT INTO nguoi_dung(ma_nguoi_dung,ho_ten,email,mat_khau_hash,loai_nguoi_dung,trang_thai)
                VALUES('KEEP_COLLISION','Existing fictional account','demo.sv18@example.test',%s,'SV','KHOA')""", (fixture_hash,))
            prior = snapshot(collision)
            rejected = False
            try:
                run_seed(collision, collision_name, root)
            except pymysql.MySQLError as exc:
                rejected = exc.args[0] == 1062
            expect(rejected and prior == snapshot(collision), "safety.mid_seed_email_collision_rolls_back_all_inserts")
        finally:
            collision.close()

        # Verify the exact published v1 without embedding another legacy SQL copy in the repo.
        try:
            legacy_sql = subprocess.run(["git", "show", "4897835a58e8b48fa656f4c8bcca14cf8b68c124:database/seed/demo_data.sql"],
                                        cwd=REPO, capture_output=True, text=True, encoding="utf-8", check=True).stdout
        except (OSError, subprocess.CalledProcessError):
            record("legacy.v1_conflicts_reported_without_overwriting", "SKIP", "Original v1 commit not available in this export/checkout")
        else:
            legacy, legacy_name = create_test_schema(args, password, "legacy")
            report["schemas_retained"].append(legacy_name)
            try:
                for sql in statements(legacy_sql):
                    query(legacy, sql)
                original = snapshot(legacy)
                rejected = False
                try:
                    run_seed(legacy, legacy_name, root)
                except ValueError as exc:
                    rejected = "groups.creator_is_initial_leader" in str(exc) and "topics.supervisors_before_approval" in str(exc)
                expect(rejected and original == snapshot(legacy), "legacy.v1_conflicts_reported_without_overwriting")
            finally:
                legacy.close()

        record("service.capacity_6th_member_rejected", "SKIP", "No membership Service/Controller exists; SQL validator test above is not service enforcement")
        record("http.unauthorized_report_access", "SKIP", "No authentication/report Controller/private application storage")
        record("http.authenticated_A_to_I_workflows", "SKIP", "Only application bootstrap Java class exists; all demo accounts intentionally locked")
        record("service.concurrent_membership_upload_grade_publication", "SKIP", "Transaction services not implemented")
        record("integration.actual_Flyway_engine_and_Spring_startup", "SKIP", "This runner executes V1-V5 SQL directly; Java runtime here is 17, project requires 21")
        record("integration.SMTP_result_notification", "SKIP", "No mail service; no messages sent")
        expect(not failures(validation_rows(connection, root)), "final.all_database_invariants_still_valid_after_negative_rollbacks")
    except BaseException as exc:
        # Do not expose DB values, credentials or hashes in the report.
        record("suite", "FAIL", type(exc).__name__ + ": " + str(exc)[:400])
        if args.report:
            args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2, default=str) + "\n", encoding="utf-8")
        raise
    finally:
        if connection:
            connection.close()
    if args.report:
        args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2, default=str) + "\n", encoding="utf-8")
    return report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=["seed", "validate", "test"])
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=3306)
    parser.add_argument("--user", required=True)
    parser.add_argument("--database", help="Explicit existing schema for seed/validate; test always creates unique schemas")
    parser.add_argument("--fixture-root", type=Path, required=True, help="Already provisioned private LOCAL QA root outside repository")
    parser.add_argument("--report", type=Path, help="Optional test evidence JSON (contains no secrets)")
    args = parser.parse_args()
    local_only(args.host)
    if args.action == "test" and args.database:
        parser.error("test never uses an existing database; omit --database")
    if args.action != "test" and not args.database:
        parser.error("seed/validate require --database to explicitly select the schema")
    root = args.fixture_root.expanduser().resolve()
    if root.is_relative_to(REPO):
        parser.error("fixture root must be outside the repository/webroot")
    verify(root)
    password = os.environ["DEMO_DB_PASSWORD"] if "DEMO_DB_PASSWORD" in os.environ else getpass.getpass("Local MySQL password: ")
    if args.action == "test":
        result = execute_tests(args, password, root)
        counts = defaultdict(int)
        for test in result["tests"]:
            counts[test["status"]] += 1
        print(json.dumps(dict(counts), sort_keys=True))
        print("Retained isolated schemas: " + ", ".join(result["schemas_retained"]))
    else:
        with connect(args, password, args.database) as connection:
            assert_target(connection, args.database)
            results = run_seed(connection, args.database, root) if args.action == "seed" else validation_rows(connection, root)
            for row in results:
                if "table_name" in row or row.get("status") == "FAIL":
                    print(json.dumps(row, default=str, ensure_ascii=False))
            print(f"{len([r for r in results if 'status' in r])} checks; {len(failures(results))} FAIL")
            if failures(results):
                raise SystemExit(1)


if __name__ == "__main__":
    try:
        main()
    except (ValueError, AssertionError, pymysql.MySQLError, OSError) as exc:
        print(f"FAIL {type(exc).__name__}: {exc}", file=sys.stderr)
        raise SystemExit(1)
