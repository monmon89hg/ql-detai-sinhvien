#!/usr/bin/env python3
"""Tổng hợp Surefire, loại properties, log, tham số và nội dung lỗi khỏi artifact."""

import argparse
import json
import os
from pathlib import Path
import xml.etree.ElementTree as ET


def summarize(reports: Path, output: Path) -> int:
    output.mkdir(parents=True, exist_ok=True)
    totals = {"PASS": 0, "FAIL": 0, "SKIP": 0}
    classes = {}
    sanitized = ET.Element("testsuites")
    for file in sorted(reports.glob("TEST-*.xml")):
        for case in ET.parse(file).getroot().iter("testcase"):
            name = case.get("classname", "unknown")
            counts = classes.setdefault(name, {"PASS": 0, "FAIL": 0, "SKIP": 0})
            state = ("SKIP" if case.find("skipped") is not None else
                     "FAIL" if case.find("failure") is not None
                     or case.find("error") is not None else "PASS")
            counts[state] += 1
            totals[state] += 1
    for name, counts in sorted(classes.items()):
        suite = ET.SubElement(sanitized, "testsuite", name=name,
                              tests=str(sum(counts.values())),
                              failures=str(counts["FAIL"]), skipped=str(counts["SKIP"]))
        for state, count in counts.items():
            for index in range(count):
                case = ET.SubElement(suite, "testcase", classname=name,
                                     name=f"{state.lower()}_{index + 1}")
                if state == "FAIL":
                    ET.SubElement(case, "failure", message="Test thất bại; xem log của job")
                elif state == "SKIP":
                    ET.SubElement(case, "skipped")
    status = ("NOT RUN" if not sum(totals.values()) else
              "FAIL" if totals["FAIL"] or totals["SKIP"] else "PASS")
    result = {"status": status, **totals, "classes": classes}
    (output / "summary.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    ET.ElementTree(sanitized).write(output / "junit.xml", encoding="utf-8", xml_declaration=True)
    markdown = (f"Kết quả test: **{status}**\n\n"
                "| PASS | FAIL (gồm ERROR) | SKIP |\n| --- | --- | --- |\n"
                f"| {totals['PASS']} | {totals['FAIL']} | {totals['SKIP']} |\n\n"
                "Không có báo cáo là NOT RUN. SKIP không được tính là PASS.\n"
                "Artifact đã loại properties, stdout/stderr, tên tham số và nội dung lỗi.\n")
    (output / "summary.md").write_text(markdown, encoding="utf-8")
    if os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(os.environ["GITHUB_STEP_SUMMARY"], "a", encoding="utf-8") as stream:
            stream.write(markdown)
    print(f"{status}: PASS={totals['PASS']} FAIL={totals['FAIL']} SKIP={totals['SKIP']}")
    return 0 if status == "PASS" else 1


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--reports", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    raise SystemExit(summarize(args.reports, args.output))
