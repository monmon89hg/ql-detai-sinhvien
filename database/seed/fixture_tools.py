#!/usr/bin/env python3
"""Generate/verify fictional PDFs, or provision a PRIVATE LOCAL fixture store.

This is a QA fixture adapter, not the application's unimplemented storage API.
No network calls, database credentials, public download URLs, or account changes.
"""
import argparse
import hashlib
import io
import json
import os
from pathlib import Path

BASE = Path(__file__).resolve().parent
REPO = BASE.parents[1]
FIXTURES = BASE / "fixtures"
MANIFEST = FIXTURES / "manifest.json"

REPORTS = [
    ("DEMO_N02", 1, "DEMO_SV04", "2026-10-19 14:00:00", "Digital library", "Initial report"),
    ("DEMO_N08", 1, "DEMO_SV26", "2026-09-05 09:00:00", "Thesis archive", "Initial design and test plan"),
    ("DEMO_N08", 2, "DEMO_SV26", "2026-09-09 09:00:00", "Thesis archive", "Revised schema and verified test evidence"),
    ("DEMO_N09", 1, "DEMO_SV29", "2026-09-08 09:00:00", "Campus knowledge search", "Final assessment copy"),
    ("DEMO_N10", 1, "DEMO_SV32", "2026-09-09 10:00:00", "Learning resource index", "Copy awaiting all mandatory grades"),
    ("DEMO_N11", 1, "DEMO_SV35", "2026-09-09 11:00:00", "Laboratory booking", "Independent assessment copy"),
]


def pdf_bytes(group, version, uploader, submitted, title, change):
    import reportlab
    from reportlab.pdfbase import pdfmetrics
    from reportlab.pdfbase.ttfonts import TTFont
    from reportlab.pdfgen import canvas
    # Embed the fonts shipped with the pinned ReportLab package: no viewer fallback.
    fonts = Path(reportlab.__file__).parent / "fonts"
    if "DemoSans" not in pdfmetrics.getRegisteredFontNames():
        pdfmetrics.registerFont(TTFont("DemoSans", str(fonts / "Vera.ttf")))
        pdfmetrics.registerFont(TTFont("DemoSansBold", str(fonts / "VeraBd.ttf")))
    out = io.BytesIO()
    doc = canvas.Canvas(out, pagesize=(595, 842), invariant=True, pageCompression=0)
    doc.setTitle(f"DEMONSTRATION ONLY - {group} - version {version}")
    doc.setAuthor("Group 12 - fictional QA fixtures")
    doc.setFillColorRGB(0.10, 0.19, 0.27)
    doc.rect(0, 720, 595, 122, fill=1, stroke=0)
    doc.setFillColorRGB(1, 1, 1)
    doc.setFont("DemoSansBold", 19)
    doc.drawString(48, 787, "DEMONSTRATION DATA ONLY")
    doc.setFont("DemoSans", 12)
    doc.drawString(48, 759, "Student topic management - Group 12")
    doc.setFillColorRGB(0.12, 0.15, 0.19)
    lines = [f"Report: {title}", f"Group: {group}", f"Version: {version}",
             f"Submitted by: {uploader}", f"Submission time (Vietnam): {submitted}",
             "", "Purpose", "Fictional report for database and file integrity tests.",
             "No real student identities, research data, or credentials.", "", "Version notes", change,
             "", "Demonstration scope", "1. Model business keys and relationships.",
             "2. Verify PDF metadata, version history, size, and SHA-256.",
             "3. Keep downloads private until authorization is implemented.", "",
             "This file is not an official academic submission."]
    y = 682
    for line in lines:
        doc.setFont("DemoSansBold" if line in ("Purpose", "Version notes", "Demonstration scope") else "DemoSans", 11)
        doc.drawString(48, y, line)
        y -= 23
    doc.setFont("DemoSans", 9)
    doc.drawString(48, 46, "Fictional QA fixture | CSDL 2.0 | Page 1 of 1")
    doc.showPage()
    doc.save()
    return out.getvalue()


def digest(data):
    return hashlib.sha256(data).hexdigest()


def load_manifest():
    data = json.loads(MANIFEST.read_text(encoding="utf-8"))
    if data["format_version"] != 1 or len(data["reports"]) != len(REPORTS):
        raise ValueError("Unexpected fixture manifest")
    return data


def safe_path(root, key):
    relative = Path(key)
    if relative.is_absolute() or ".." in relative.parts or "\\" in key:
        raise ValueError("Unsafe object key")
    target = root / relative
    for parent in [target, *target.parents]:
        if parent == root:
            break
        if parent.is_symlink():
            raise ValueError("Symlinks are forbidden in the fixture store")
    if not target.resolve().is_relative_to(root):
        raise ValueError("Object key escapes fixture root")
    return target


def verify(root=None):
    from pypdf import PdfReader
    data = load_manifest()
    seen_keys, seen_versions = set(), set()
    for item in data["reports"]:
        key = item["object_key"]
        identity = (item["ma_nhom"], item["phien_ban"])
        if key in seen_keys or identity in seen_versions:
            raise ValueError("Duplicate key/version in manifest")
        seen_keys.add(key)
        seen_versions.add(identity)
        path = safe_path(root, key) if root is not None else FIXTURES / item["file"]
        if not path.resolve().is_relative_to(root if root is not None else FIXTURES):
            raise ValueError("Fixture path escapes root")
        raw = path.read_bytes()
        if not (0 < len(raw) <= 20 * 1024 * 1024) or len(raw) != item["kich_thuoc_byte"]:
            raise ValueError(f"PDF size mismatch: {identity}")
        if digest(raw) != item["sha256"] or not raw.startswith(b"%PDF-"):
            raise ValueError(f"PDF checksum/signature mismatch: {identity}")
        reader = PdfReader(io.BytesIO(raw), strict=True)
        if reader.is_encrypted or len(reader.pages) != 1:
            raise ValueError("Unexpected PDF structure")
        text = reader.pages[0].extract_text()
        if "DEMONSTRATION DATA ONLY" not in text or item["ma_nhom"] not in text:
            raise ValueError("PDF missing demo identity")
        if item["mime_type"] != "application/pdf":
            raise ValueError("Unexpected MIME type")
    return data


def generate():
    import importlib.metadata
    FIXTURES.mkdir(exist_ok=True)
    reports = []
    for group, version, uploader, submitted, title, change in REPORTS:
        raw = pdf_bytes(group, version, uploader, submitted, title, change)
        filename = f"{group}-v{version}.pdf"
        (FIXTURES / filename).write_bytes(raw)
        reports.append(dict(ma_nhom=group, phien_ban=version, ma_nguoi_nop=uploader,
                            nop_luc=submitted, ten_file_goc=filename, file=filename,
                            object_key=f"demo-fixtures/v1/{group}/v{version}.pdf",
                            mime_type="application/pdf", kich_thuoc_byte=len(raw), sha256=digest(raw)))
    data = dict(format_version=1, storage_contract="private-local-qa-only; application storage not implemented",
                generator=f"reportlab {importlib.metadata.version('reportlab')}; invariant=True", reports=reports)
    MANIFEST.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    verify()


def provision(root):
    root = root.expanduser().resolve()
    if root.is_relative_to(REPO):
        raise ValueError("Private fixture root must be outside the git repository/webroot")
    data = verify()
    root.mkdir(parents=True, exist_ok=True, mode=0o700)
    for item in data["reports"]:
        path = safe_path(root, item["object_key"])
        path.parent.mkdir(parents=True, exist_ok=True, mode=0o700)
        raw = (FIXTURES / item["file"]).read_bytes()
        try:
            fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
        except FileExistsError:
            if digest(path.read_bytes()) != item["sha256"]:
                raise ValueError(f"Refusing to overwrite a different object: {item['object_key']}")
        else:
            with os.fdopen(fd, "wb") as stream:
                stream.write(raw)
    verify(root)
    return root


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=["generate", "check", "provision"])
    parser.add_argument("--fixture-root", type=Path)
    args = parser.parse_args()
    if args.action == "generate":
        generate()
    elif args.action == "provision":
        if args.fixture_root is None:
            parser.error("provision requires --fixture-root")
        provision(args.fixture_root)
    else:
        verify(args.fixture_root.resolve() if args.fixture_root else None)
    print(f"PASS fixtures.{args.action}: {len(REPORTS)} authentic PDF files")


if __name__ == "__main__":
    main()
