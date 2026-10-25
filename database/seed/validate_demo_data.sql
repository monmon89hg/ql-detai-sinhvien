-- READ ONLY. MySQL 8.4; run in the explicitly selected schema.
-- Expected counts describe the pristine v2 fixture snapshot, not live demo mutations.
SELECT DATABASE() AS selected_schema, VERSION() AS mysql_version;

WITH counts AS (
    SELECT 'bo_mon' AS table_name, 4 AS expected_rows, COUNT(*) AS actual_rows FROM bo_mon WHERE LEFT(ma_bo_mon,5)='DEMO_'
    UNION ALL SELECT 'nguoi_dung',47,COUNT(*) FROM nguoi_dung WHERE LEFT(ma_nguoi_dung,5)='DEMO_'
    UNION ALL SELECT 'vai_tro',5,COUNT(*) FROM vai_tro
    UNION ALL SELECT 'nguoi_dung_vai_tro',48,COUNT(*) FROM nguoi_dung_vai_tro WHERE LEFT(ma_nguoi_dung,5)='DEMO_'
    UNION ALL SELECT 'thong_bao',4,COUNT(*) FROM thong_bao WHERE LEFT(ma_thong_bao,5)='DEMO_'
    UNION ALL SELECT 'dot_dang_ky',7,COUNT(*) FROM dot_dang_ky WHERE LEFT(ma_dot,5)='DEMO_'
    UNION ALL SELECT 'de_tai',17,COUNT(*) FROM de_tai WHERE LEFT(ma_de_tai,5)='DEMO_'
    UNION ALL SELECT 'de_tai_giang_vien',27,COUNT(*) FROM de_tai_giang_vien WHERE LEFT(ma_de_tai,5)='DEMO_'
    UNION ALL SELECT 'nhom_sinh_vien',11,COUNT(*) FROM nhom_sinh_vien WHERE LEFT(ma_nhom,5)='DEMO_'
    UNION ALL SELECT 'thanh_vien_nhom',38,COUNT(*) FROM thanh_vien_nhom WHERE LEFT(ma_nhom,5)='DEMO_'
    UNION ALL SELECT 'dang_ky_de_tai',9,COUNT(*) FROM dang_ky_de_tai WHERE LEFT(ma_nhom,5)='DEMO_'
    UNION ALL SELECT 'nop_bao_cao',6,COUNT(*) FROM nop_bao_cao WHERE LEFT(ma_nhom,5)='DEMO_'
    UNION ALL SELECT 'hoi_dong',5,COUNT(*) FROM hoi_dong WHERE LEFT(ma_hoi_dong,5)='DEMO_'
    UNION ALL SELECT 'hoi_dong_de_tai',4,COUNT(*) FROM hoi_dong_de_tai WHERE LEFT(ma_nhom,5)='DEMO_'
    UNION ALL SELECT 'thanh_vien_hoi_dong',14,COUNT(*) FROM thanh_vien_hoi_dong WHERE LEFT(ma_hoi_dong,5)='DEMO_'
    UNION ALL SELECT 'phieu_cham_diem',14,COUNT(*) FROM phieu_cham_diem WHERE LEFT(ma_nhom,5)='DEMO_'
    UNION ALL SELECT 'ket_qua',2,COUNT(*) FROM ket_qua WHERE LEFT(ma_nhom,5)='DEMO_'
)
SELECT table_name, expected_rows, actual_rows, IF(expected_rows=actual_rows,'PASS','FAIL') AS status
FROM counts ORDER BY table_name;

WITH
members AS (
    SELECT n.ma_nhom, n.trang_thai AS group_state, n.chot_luc, n.ket_thuc_luc,
        SUM(v.trang_thai='THAM_GIA') AS active_count,
        SUM(v.trang_thai='THAM_GIA' AND v.la_nhom_truong=1) AS active_leaders,
        SUM(v.la_thanh_vien_chot=1) AS snapshot_count,
        SUM(v.la_thanh_vien_chot=1 AND v.la_nhom_truong=1) AS snapshot_leaders
    FROM nhom_sinh_vien n LEFT JOIN thanh_vien_nhom v ON v.ma_nhom=n.ma_nhom
    WHERE LEFT(n.ma_nhom,5)='DEMO_' GROUP BY n.ma_nhom,n.trang_thai,n.chot_luc,n.ket_thuc_luc
),
required_assessors AS (
    SELECT d.ma_nhom, a.ma_giang_vien, a.loai_phan_cong AS loai_phieu
    FROM dang_ky_de_tai d JOIN de_tai t ON t.ma_de_tai=d.ma_de_tai
    JOIN dot_dang_ky r ON r.ma_dot=t.ma_dot JOIN de_tai_giang_vien a ON a.ma_de_tai=t.ma_de_tai
    WHERE LEFT(d.ma_nhom,5)='DEMO_' AND (
        (r.loai_de_tai IN ('MON_HOC','NCKH') AND a.loai_phan_cong='CHAM_DOC_LAP') OR
        (r.loai_de_tai IN ('TLCN','KLTN') AND a.loai_phan_cong='PHAN_BIEN'))
    UNION ALL
    SELECT h.ma_nhom,v.ma_giang_vien,'HOI_DONG'
    FROM hoi_dong_de_tai h JOIN thanh_vien_hoi_dong v ON v.ma_hoi_dong=h.ma_hoi_dong
    WHERE LEFT(h.ma_nhom,5)='DEMO_'
),
checks AS (
    SELECT 'accounts.locked_test_identities' AS check_name, COUNT(*) AS violations FROM nguoi_dung
    WHERE LEFT(ma_nguoi_dung,5)='DEMO_' AND (trang_thai<>'KHOA' OR buoc_doi_mat_khau<>1
        OR lan_dang_nhap_cuoi IS NOT NULL OR email NOT LIKE '%.test'
        OR BINARY email<>BINARY LOWER(TRIM(email)) OR mat_khau_hash NOT REGEXP '^[$]2[aby][$][0-9]{2}[$][./A-Za-z0-9]{53}$')
    UNION ALL SELECT 'roles.compatible_profiles',COUNT(*) FROM nguoi_dung_vai_tro v JOIN nguoi_dung u USING(ma_nguoi_dung)
    WHERE LEFT(u.ma_nguoi_dung,5)='DEMO_' AND (
        (u.loai_nguoi_dung='SV' AND v.ma_vai_tro<>'SINH_VIEN') OR
        (u.loai_nguoi_dung='GV' AND v.ma_vai_tro NOT IN ('GIANG_VIEN','TRUONG_BO_MON')) OR
        (u.loai_nguoi_dung='CAN_BO' AND v.ma_vai_tro NOT IN ('ADMIN','TRUONG_KHOA')))
    UNION ALL SELECT 'roles.authorized_grantor',COUNT(*) FROM nguoi_dung_vai_tro v
    WHERE LEFT(v.ma_nguoi_dung,5)='DEMO_' AND NOT (
        (v.ma_nguoi_dung='DEMO_CB01' AND v.ma_vai_tro='ADMIN' AND v.ma_nguoi_cap IS NULL) OR
        EXISTS(SELECT 1 FROM nguoi_dung_vai_tro a WHERE a.ma_nguoi_dung=v.ma_nguoi_cap AND a.ma_vai_tro='ADMIN'))
    UNION ALL SELECT 'groups.active_leader_and_capacity',COUNT(*) FROM members
    WHERE group_state NOT IN ('HOAN_THANH','GIAI_TAN') AND (active_count NOT BETWEEN 1 AND 5 OR active_leaders<>1)
    UNION ALL SELECT 'groups.completed_history_released',COUNT(*) FROM members
    WHERE group_state='HOAN_THANH' AND (active_count<>0 OR snapshot_count NOT BETWEEN 3 AND 5
        OR snapshot_leaders<>1 OR ket_thuc_luc IS NULL)
    UNION ALL SELECT 'groups.finalized_membership',COUNT(*) FROM members
    WHERE group_state IN ('DA_CHOT','DANG_THUC_HIEN') AND (snapshot_count NOT BETWEEN 3 AND 5
        OR active_count<>snapshot_count OR snapshot_leaders<>1 OR chot_luc IS NULL)
    UNION ALL SELECT 'groups.creator_is_initial_leader',COUNT(*) FROM nhom_sinh_vien n
    WHERE LEFT(n.ma_nhom,5)='DEMO_' AND NOT EXISTS (
        SELECT 1 FROM thanh_vien_nhom v JOIN nguoi_dung u ON u.ma_nguoi_dung=v.ma_sinh_vien
        WHERE v.ma_nhom=n.ma_nhom AND v.ma_sinh_vien=n.ma_nguoi_tao AND v.la_nhom_truong=1
        AND u.loai_nguoi_dung='SV' AND v.tham_gia_luc=n.created_at AND v.created_at=n.created_at)
    UNION ALL SELECT 'members.one_active_group_globally',COUNT(*) FROM (
        SELECT ma_sinh_vien FROM thanh_vien_nhom WHERE trang_thai='THAM_GIA'
        GROUP BY ma_sinh_vien HAVING COUNT(*)>1) duplicate_students
    UNION ALL SELECT 'members.type_role_and_timestamps',COUNT(*) FROM thanh_vien_nhom v
    JOIN nguoi_dung u ON u.ma_nguoi_dung=v.ma_sinh_vien JOIN nhom_sinh_vien n USING(ma_nhom)
    WHERE LEFT(v.ma_nhom,5)='DEMO_' AND (u.loai_nguoi_dung<>'SV'
        OR NOT EXISTS(SELECT 1 FROM nguoi_dung_vai_tro r WHERE r.ma_nguoi_dung=v.ma_sinh_vien AND r.ma_vai_tro='SINH_VIEN')
        OR v.yeu_cau_luc<n.created_at OR v.created_at<v.yeu_cau_luc OR v.tham_gia_luc<v.yeu_cau_luc
        OR (v.la_thanh_vien_chot=1 AND (v.tham_gia_luc>n.chot_luc OR v.trang_thai NOT IN ('THAM_GIA','KET_THUC')))
        OR (v.trang_thai IN ('CHO_DUYET','TU_CHOI') AND (v.la_nhom_truong=1 OR v.la_thanh_vien_chot=1))
        OR (n.trang_thai='HOAN_THANH' AND v.la_thanh_vien_chot=1 AND NOT (v.ket_thuc_luc<=>n.ket_thuc_luc)))
    UNION ALL SELECT 'rounds.deadline_order_and_owner',COUNT(*) FROM dot_dang_ky r
    WHERE LEFT(r.ma_dot,5)='DEMO_' AND (r.han_nop_bao_cao>r.han_nop_diem_pb
        OR (r.ngay_bao_cao_hd IS NOT NULL AND (r.ngay_bao_cao_hd<=DATE(r.han_nop_bao_cao)
            OR r.ngay_bao_cao_hd<=DATE(r.han_nop_diem_pb))) OR r.created_at>r.bat_dau_gv
        OR NOT EXISTS(SELECT 1 FROM nguoi_dung_vai_tro v WHERE v.ma_nguoi_dung=r.ma_nguoi_tao AND v.ma_vai_tro='TRUONG_KHOA'))
    UNION ALL SELECT 'rounds.late_october_window',IF(EXISTS(
        SELECT 1 FROM dot_dang_ky WHERE ma_dot='DEMO_DOT_LATE26' AND trang_thai='CONG_BO'
        AND bat_dau_sv<='2026-10-20 00:00:00' AND ket_thuc_sv>='2026-10-31 23:59:59'),0,1)
    UNION ALL SELECT 'topics.supervisors_before_approval',COUNT(*) FROM de_tai t
    WHERE LEFT(t.ma_de_tai,5)='DEMO_' AND t.trang_thai IN ('DA_DUYET','CONG_BO','NGUNG_NHAN') AND (
        (SELECT COUNT(*) FROM de_tai_giang_vien a WHERE a.ma_de_tai=t.ma_de_tai AND a.loai_phan_cong='HUONG_DAN') NOT BETWEEN 1 AND 2
        OR EXISTS(SELECT 1 FROM de_tai_giang_vien a WHERE a.ma_de_tai=t.ma_de_tai AND a.loai_phan_cong='HUONG_DAN' AND a.phan_cong_luc>t.duyet_luc)
        OR t.ma_nguoi_duyet IS NULL OR t.duyet_luc IS NULL)
    UNION ALL SELECT 'topics.review_timestamps_and_authority',COUNT(*) FROM de_tai t JOIN dot_dang_ky r USING(ma_dot)
    WHERE LEFT(t.ma_de_tai,5)='DEMO_' AND (t.created_at<r.bat_dau_gv OR t.created_at>r.ket_thuc_gv
        OR t.duyet_luc<t.created_at OR t.cong_bo_luc<t.duyet_luc OR (t.ma_nguoi_duyet IS NOT NULL AND NOT EXISTS(
            SELECT 1 FROM nguoi_dung_vai_tro v WHERE v.ma_nguoi_dung=t.ma_nguoi_duyet AND v.ma_vai_tro='TRUONG_KHOA')))
    UNION ALL SELECT 'lecturers.type_role_and_assignment_owner',COUNT(*) FROM de_tai_giang_vien a JOIN nguoi_dung u ON u.ma_nguoi_dung=a.ma_giang_vien
    WHERE LEFT(a.ma_de_tai,5)='DEMO_' AND (u.loai_nguoi_dung<>'GV' OR u.ma_bo_mon IS NULL
        OR NOT EXISTS(SELECT 1 FROM nguoi_dung_vai_tro v WHERE v.ma_nguoi_dung=a.ma_giang_vien AND v.ma_vai_tro='GIANG_VIEN')
        OR NOT EXISTS(SELECT 1 FROM nguoi_dung_vai_tro v WHERE v.ma_nguoi_dung=a.ma_nguoi_phan_cong AND v.ma_vai_tro='TRUONG_KHOA'))
    UNION ALL SELECT 'registrations.same_round_and_registration_window',COUNT(*) FROM dang_ky_de_tai d
    JOIN nhom_sinh_vien n USING(ma_nhom) JOIN de_tai t USING(ma_de_tai) JOIN dot_dang_ky r ON r.ma_dot=n.ma_dot
    WHERE LEFT(d.ma_nhom,5)='DEMO_' AND (n.ma_dot<>t.ma_dot OR t.trang_thai<>'CONG_BO'
        OR t.cong_bo_luc>d.dang_ky_luc OR n.created_at>d.dang_ky_luc
        OR d.dang_ky_luc NOT BETWEEN r.bat_dau_sv AND r.ket_thuc_sv)
    UNION ALL SELECT 'registrations.leader_and_group_size',COUNT(*) FROM dang_ky_de_tai d JOIN members m USING(ma_nhom)
    WHERE LEFT(d.ma_nhom,5)='DEMO_' AND (
        IF(d.trang_thai='HOAN_THANH',m.snapshot_count,m.active_count) NOT BETWEEN 3 AND 5
        OR NOT EXISTS(SELECT 1 FROM thanh_vien_nhom v WHERE v.ma_nhom=d.ma_nhom
            AND v.ma_sinh_vien=d.ma_nguoi_dang_ky AND v.la_nhom_truong=1
            AND v.tham_gia_luc<=d.dang_ky_luc AND (v.ket_thuc_luc IS NULL OR v.ket_thuc_luc>=d.dang_ky_luc)))
    UNION ALL SELECT 'registrations.decision_and_lifecycle',COUNT(*) FROM dang_ky_de_tai d JOIN nhom_sinh_vien n USING(ma_nhom)
    WHERE LEFT(d.ma_nhom,5)='DEMO_' AND (
        (d.trang_thai='CHO_DUYET' AND (d.ma_nguoi_duyet IS NOT NULL OR d.duyet_luc IS NOT NULL OR n.chot_luc IS NOT NULL))
        OR (d.trang_thai<>'CHO_DUYET' AND (d.duyet_luc IS NULL OR d.duyet_luc<d.dang_ky_luc OR NOT EXISTS(
            SELECT 1 FROM nguoi_dung_vai_tro v WHERE v.ma_nguoi_dung=d.ma_nguoi_duyet AND v.ma_vai_tro='TRUONG_KHOA')))
        OR (d.trang_thai IN ('DA_DUYET','DANG_THUC_HIEN','DANG_CHAM','HOAN_THANH') AND NOT (n.chot_luc<=>d.duyet_luc))
        OR (d.trang_thai='DA_DUYET' AND n.trang_thai<>'DA_CHOT')
        OR (d.trang_thai IN ('DANG_THUC_HIEN','DANG_CHAM') AND n.trang_thai<>'DANG_THUC_HIEN')
        OR (d.trang_thai='HOAN_THANH' AND (n.trang_thai<>'HOAN_THANH' OR NOT (n.ket_thuc_luc<=>d.ket_thuc_luc))))
    UNION ALL SELECT 'reports.realistic_metadata_and_submission',COUNT(*) FROM nop_bao_cao b JOIN dang_ky_de_tai d USING(ma_nhom)
    JOIN de_tai t USING(ma_de_tai) JOIN dot_dang_ky r USING(ma_dot)
    WHERE LEFT(b.ma_nhom,5)='DEMO_' AND (b.mime_type<>'application/pdf' OR b.sha256 NOT REGEXP '^[0-9a-f]{64}$'
        OR b.object_key<>CONCAT('demo-fixtures/v1/',b.ma_nhom,'/v',b.phien_ban,'.pdf')
        OR b.kich_thuoc_byte NOT BETWEEN 1 AND 20971520 OR b.nop_luc<d.duyet_luc OR r.han_nop_bao_cao IS NULL
        OR b.nop_luc>=r.han_nop_bao_cao OR b.nop_luc>=d.bat_dau_cham_luc
        OR NOT EXISTS(SELECT 1 FROM thanh_vien_nhom v WHERE v.ma_nhom=b.ma_nhom AND v.ma_sinh_vien=b.ma_nguoi_nop
            AND v.la_nhom_truong=1 AND v.la_thanh_vien_chot=1 AND v.tham_gia_luc<=b.nop_luc
            AND (v.ket_thuc_luc IS NULL OR v.ket_thuc_luc>=b.nop_luc)))
    UNION ALL SELECT 'reports.contiguous_versions',COUNT(*) FROM (
        SELECT ma_nhom FROM nop_bao_cao WHERE LEFT(ma_nhom,5)='DEMO_'
        GROUP BY ma_nhom HAVING MIN(phien_ban)<>1 OR MAX(phien_ban)<>COUNT(*)) version_gaps
    UNION ALL SELECT 'assessment.report_deadline_and_required_assignments',COUNT(*) FROM dang_ky_de_tai d
    JOIN de_tai t USING(ma_de_tai) JOIN dot_dang_ky r USING(ma_dot)
    WHERE LEFT(d.ma_nhom,5)='DEMO_' AND d.trang_thai IN ('DANG_CHAM','HOAN_THANH') AND (
        d.bat_dau_cham_luc IS NULL OR r.han_nop_bao_cao IS NULL OR d.bat_dau_cham_luc<r.han_nop_bao_cao
        OR NOT EXISTS(SELECT 1 FROM nop_bao_cao b WHERE b.ma_nhom=d.ma_nhom AND b.trang_thai='HOP_LE' AND b.nop_luc<d.bat_dau_cham_luc)
        OR (SELECT COUNT(*) FROM de_tai_giang_vien a WHERE a.ma_de_tai=t.ma_de_tai AND a.loai_phan_cong=
            IF(r.loai_de_tai IN ('MON_HOC','NCKH'),'CHAM_DOC_LAP','PHAN_BIEN'))<>1
        OR (r.loai_de_tai='KLTN' AND NOT EXISTS(SELECT 1 FROM hoi_dong_de_tai h WHERE h.ma_nhom=d.ma_nhom)))
    UNION ALL SELECT 'committees.klt_only_authorized_creator',COUNT(*) FROM hoi_dong h JOIN dot_dang_ky r USING(ma_dot)
    WHERE LEFT(h.ma_hoi_dong,5)='DEMO_' AND (r.loai_de_tai<>'KLTN' OR NOT EXISTS(
        SELECT 1 FROM nguoi_dung_vai_tro v WHERE v.ma_nguoi_dung=h.ma_nguoi_tao AND v.ma_vai_tro='TRUONG_KHOA'))
    UNION ALL SELECT 'committees.ready_composition',COUNT(*) FROM hoi_dong h
    WHERE LEFT(h.ma_hoi_dong,5)='DEMO_' AND h.trang_thai<>'NHAP' AND (
        (SELECT COUNT(*) FROM thanh_vien_hoi_dong v WHERE v.ma_hoi_dong=h.ma_hoi_dong) NOT BETWEEN 3 AND 5
        OR (SELECT COUNT(*) FROM thanh_vien_hoi_dong v WHERE v.ma_hoi_dong=h.ma_hoi_dong AND v.chuc_danh='CHU_TICH')<>1
        OR (SELECT COUNT(*) FROM thanh_vien_hoi_dong v WHERE v.ma_hoi_dong=h.ma_hoi_dong AND v.chuc_danh='THU_KY')<>1)
    UNION ALL SELECT 'committees.members_lecturer_role',COUNT(*) FROM thanh_vien_hoi_dong v JOIN nguoi_dung u ON u.ma_nguoi_dung=v.ma_giang_vien
    WHERE LEFT(v.ma_hoi_dong,5)='DEMO_' AND (u.loai_nguoi_dung<>'GV' OR NOT EXISTS(
        SELECT 1 FROM nguoi_dung_vai_tro a WHERE a.ma_nguoi_dung=v.ma_giang_vien AND a.ma_vai_tro='GIANG_VIEN'))
    UNION ALL SELECT 'committees.assignment_round_and_timing',COUNT(*) FROM hoi_dong_de_tai h JOIN hoi_dong c USING(ma_hoi_dong)
    JOIN dang_ky_de_tai d USING(ma_nhom) JOIN de_tai t USING(ma_de_tai) JOIN nhom_sinh_vien n USING(ma_nhom)
    WHERE LEFT(h.ma_nhom,5)='DEMO_' AND (c.ma_dot<>t.ma_dot OR c.ma_dot<>n.ma_dot
        OR d.trang_thai IN ('CHO_DUYET','TU_CHOI') OR h.phan_cong_luc<d.duyet_luc OR h.phan_cong_luc>d.bat_dau_cham_luc
        OR NOT EXISTS(SELECT 1 FROM nguoi_dung_vai_tro v WHERE v.ma_nguoi_dung=h.ma_nguoi_phan_cong AND v.ma_vai_tro='TRUONG_KHOA'))
    UNION ALL SELECT 'committees.no_supervisor_or_reviewer_conflicts',COUNT(*) FROM hoi_dong_de_tai h
    JOIN dang_ky_de_tai d USING(ma_nhom) JOIN thanh_vien_hoi_dong v USING(ma_hoi_dong)
    JOIN de_tai_giang_vien a ON a.ma_de_tai=d.ma_de_tai AND a.ma_giang_vien=v.ma_giang_vien
    WHERE LEFT(h.ma_nhom,5)='DEMO_' AND a.loai_phan_cong IN ('HUONG_DAN','PHAN_BIEN')
    UNION ALL SELECT 'grades.exact_assessor_identity_and_type',COUNT(*) FROM phieu_cham_diem p
    JOIN dang_ky_de_tai d USING(ma_nhom)
    WHERE LEFT(p.ma_nhom,5)='DEMO_' AND (NOT EXISTS(SELECT 1 FROM required_assessors a
        WHERE a.ma_nhom=p.ma_nhom AND a.ma_giang_vien=p.ma_giang_vien AND a.loai_phieu=p.loai_phieu)
        OR (p.loai_phieu<>'HOI_DONG' AND p.ma_de_tai_phan_cong<>d.ma_de_tai)
        OR EXISTS(SELECT 1 FROM de_tai_giang_vien a WHERE a.ma_de_tai=d.ma_de_tai AND a.ma_giang_vien=p.ma_giang_vien AND a.loai_phan_cong='HUONG_DAN'))
    UNION ALL SELECT 'grades.submission_window_and_frozen_state',COUNT(*) FROM phieu_cham_diem p
    JOIN dang_ky_de_tai d USING(ma_nhom) JOIN de_tai t USING(ma_de_tai) JOIN dot_dang_ky r USING(ma_dot)
    WHERE LEFT(p.ma_nhom,5)='DEMO_' AND (d.trang_thai NOT IN ('DANG_CHAM','HOAN_THANH')
        OR p.created_at<d.bat_dau_cham_luc OR p.nop_luc<p.created_at
        OR (p.loai_phieu='PHAN_BIEN' AND p.nop_luc>=r.han_nop_diem_pb)
        OR (p.loai_phieu='HOI_DONG' AND DATE(p.created_at)<r.ngay_bao_cao_hd))
    UNION ALL SELECT 'results.all_required_forms_submitted',COUNT(*) FROM ket_qua k
    WHERE LEFT(k.ma_nhom,5)='DEMO_' AND (EXISTS(SELECT 1 FROM required_assessors a
        WHERE a.ma_nhom=k.ma_nhom AND NOT EXISTS(SELECT 1 FROM phieu_cham_diem p WHERE p.ma_nhom=a.ma_nhom
            AND p.ma_giang_vien=a.ma_giang_vien AND p.loai_phieu=a.loai_phieu AND p.trang_thai='DA_NOP'))
        OR k.so_phieu<>(SELECT COUNT(*) FROM required_assessors a WHERE a.ma_nhom=k.ma_nhom))
    UNION ALL SELECT 'results.actual_sum_count_average_and_time',COUNT(*) FROM ket_qua k
    WHERE LEFT(k.ma_nhom,5)='DEMO_' AND (k.so_phieu<>(SELECT COUNT(*) FROM phieu_cham_diem p WHERE p.ma_nhom=k.ma_nhom AND p.trang_thai='DA_NOP')
        OR NOT (k.tong_diem<=>(SELECT SUM(p.diem) FROM phieu_cham_diem p WHERE p.ma_nhom=k.ma_nhom AND p.trang_thai='DA_NOP'))
        OR k.diem_cuoi<>ROUND(k.tong_diem/k.so_phieu,2)
        OR k.tong_hop_luc<(SELECT MAX(p.nop_luc) FROM phieu_cham_diem p WHERE p.ma_nhom=k.ma_nhom))
    UNION ALL SELECT 'results.chairperson_aggregates_kltn',COUNT(*) FROM ket_qua k JOIN hoi_dong_de_tai h USING(ma_nhom)
    WHERE LEFT(k.ma_nhom,5)='DEMO_' AND NOT EXISTS(SELECT 1 FROM thanh_vien_hoi_dong v
        WHERE v.ma_hoi_dong=h.ma_hoi_dong AND v.ma_giang_vien=k.ma_nguoi_tong_hop AND v.chuc_danh='CHU_TICH')
    UNION ALL SELECT 'results.publication_transaction_consistent',COUNT(*) FROM ket_qua k
    JOIN dang_ky_de_tai d USING(ma_nhom) JOIN nhom_sinh_vien n USING(ma_nhom)
    WHERE LEFT(k.ma_nhom,5)='DEMO_' AND (
        (k.trang_thai='CONG_BO' AND (d.trang_thai<>'HOAN_THANH' OR n.trang_thai<>'HOAN_THANH'
            OR NOT (d.ket_thuc_luc<=>k.cong_bo_luc) OR NOT (n.ket_thuc_luc<=>k.cong_bo_luc) OR k.cong_bo_luc<k.tong_hop_luc
            OR NOT EXISTS(SELECT 1 FROM nguoi_dung_vai_tro v WHERE v.ma_nguoi_dung=k.ma_nguoi_cong_bo AND v.ma_vai_tro='TRUONG_KHOA')))
        OR (k.trang_thai='DA_TONG_HOP' AND (d.trang_thai<>'DANG_CHAM' OR n.trang_thai<>'DANG_THUC_HIEN')))
    UNION ALL SELECT 'results.completed_registration_has_published_result',COUNT(*) FROM dang_ky_de_tai d
    WHERE LEFT(d.ma_nhom,5)='DEMO_' AND d.trang_thai='HOAN_THANH' AND NOT EXISTS(
        SELECT 1 FROM ket_qua k WHERE k.ma_nhom=d.ma_nhom AND k.trang_thai='CONG_BO')
    UNION ALL SELECT 'announcements.authority_and_dates',COUNT(*) FROM thong_bao b
    WHERE LEFT(b.ma_thong_bao,5)='DEMO_' AND (b.cong_bo_luc<b.created_at OR NOT EXISTS(
        SELECT 1 FROM nguoi_dung_vai_tro v WHERE v.ma_nguoi_dung=b.ma_nguoi_dang AND v.ma_vai_tro IN ('ADMIN','TRUONG_KHOA')))
)
SELECT check_name, violations, IF(violations=0,'PASS','FAIL') AS status FROM checks ORDER BY check_name;

-- Informational: expiring windows are visible without changing the server clock.
SELECT ma_dot, trang_thai, bat_dau_sv, ket_thuc_sv,
    'Asia/Ho_Chi_Minh' AS business_timezone,
    'Fixed 2026 snapshot; use live server time in application services' AS note
FROM dot_dang_ky WHERE LEFT(ma_dot,5)='DEMO_' ORDER BY ma_dot;
