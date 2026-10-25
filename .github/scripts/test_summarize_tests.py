"""Kiểm tra phân loại kết quả và loại dữ liệu nhạy cảm trước khi upload artifact."""

import contextlib
import io
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
from summarize_tests import summarize


class TestSummarizeTests(unittest.TestCase):
    def run_summary(self, source):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        base = Path(directory.name)
        reports = base / "reports"
        reports.mkdir()
        if source is not None:
            (reports / "TEST-example.xml").write_text(source, encoding="utf-8")
        output = base / "output"
        with patch.dict(os.environ, {"GITHUB_STEP_SUMMARY": ""}), contextlib.redirect_stdout(io.StringIO()):
            code = summarize(reports, output)
        return code, json.loads((output / "summary.json").read_text()), output

    def test_failure_error_skip_and_sensitive_fields_are_handled(self):
        marker = "SENSITIVE_SENTINEL"
        code, result, output = self.run_summary(f"""
            <testsuite><properties><property name="password" value="{marker}"/></properties>
              <testcase classname="ExampleTests" name="ok"/>
              <testcase classname="ExampleTests" name="{marker}"><failure message="{marker}"/></testcase>
              <testcase classname="ExampleTests" name="error"><error>{marker}</error></testcase>
              <testcase classname="ExampleTests" name="skip"><skipped message="{marker}"/></testcase>
              <system-out>{marker}</system-out><system-err>{marker}</system-err>
            </testsuite>""")
        self.assertEqual((code, result["PASS"], result["FAIL"], result["SKIP"]), (1, 1, 2, 1))
        for file in output.iterdir():
            self.assertNotIn(marker, file.read_text(encoding="utf-8"))

    def test_missing_reports_are_not_a_pass(self):
        code, result, _ = self.run_summary(None)
        self.assertEqual((code, result["status"], result["PASS"]), (1, "NOT RUN", 0))

    def test_success_requires_executed_tests_without_skips(self):
        code, result, _ = self.run_summary(
            '<testsuite><testcase classname="ExampleTests" name="ok"/></testsuite>')
        self.assertEqual((code, result["status"], result["PASS"]), (0, "PASS", 1))


if __name__ == "__main__":
    unittest.main()
