import json
import os
from pathlib import Path
import sys
import tempfile
import unittest

from scripts.check_community_content import (
    CheckError,
    compare_reports,
    extract_marker_report,
    extract_node_report,
    load_manifest,
    require_completion,
    run_process,
    semantic_equal,
    validate_report,
)


class CommunityContentCheckerTests(unittest.TestCase):
    expected = {"valid": ["good.json"], "invalid": ["bad.json"]}
    report = {"valid": [{"name": "good.json", "package": {"n": 1, "items": ["a", "b"]}}],
              "invalid": ["bad.json"]}

    def test_rejects_changed_invalid_membership_and_duplicates(self):
        for invalid in ([], ["bad.json", "bad.json"], ["different.json"]):
            with self.subTest(invalid=invalid):
                with self.assertRaises(CheckError):
                    validate_report({**self.report, "invalid": invalid}, self.expected, "fixture")
        malformed = {"valid": [{"name": "good.json", "package": {}}], "invalid": [4]}
        with self.assertRaises(CheckError):
            validate_report(malformed, self.expected, "fixture")

    def test_rejects_altered_normalized_value(self):
        altered = {"valid": [{"name": "good.json", "package": {"n": 2, "items": ["a", "b"]}}],
                   "invalid": ["bad.json"]}
        with self.assertRaisesRegex(CheckError, "normalized package differs"):
            compare_reports(self.report, altered, self.expected, "fixture")

    def test_json_semantics_keep_array_order_and_booleans_distinct(self):
        self.assertTrue(semantic_equal({"n": 1}, {"n": 1.0}))
        self.assertFalse(semantic_equal({"n": True}, {"n": 1}))
        self.assertFalse(semantic_equal({"items": [1, 2]}, {"items": [2, 1]}))
        self.assertTrue(semantic_equal({"a": 1, "b": [True]}, {"b": [True], "a": 1.0}))

    def test_report_readers_reject_empty_truncated_missing_and_duplicate_reports(self):
        for output in ("", "{\"valid\":", "noise", "{}\n{}\n"):
            with self.subTest(output=output), self.assertRaises(CheckError):
                extract_node_report(output)
        with self.assertRaises(CheckError):
            validate_report(extract_node_report("{}"), self.expected, "fixture")
        for output in ("", "BTC_REPORT:{\"valid\":", "BTC_REPORT:{}\nBTC_REPORT:{}\n"):
            with self.subTest(output=output), self.assertRaises(CheckError):
                extract_marker_report(output, "BTC_REPORT:", "fixture")

    def test_requires_one_positive_completion_sentinel(self):
        for output in ("", "Godot contract checks passed (0 assertions).",
                       "Godot contract checks passed (1 assertions).\nGodot contract checks passed (1 assertions)."):
            with self.subTest(output=output), self.assertRaises(CheckError):
                require_completion(output)
        require_completion("Godot contract checks passed (2 assertions).")

    def test_manifest_must_cover_each_fixture_file(self):
        with tempfile.TemporaryDirectory() as temporary:
            community = Path(temporary) / "content/community/v1"
            invalid_dir = community / "fixtures/invalid"
            invalid_dir.mkdir(parents=True)
            (community / "package.json").write_text("{}")
            (community / "acceptance.json").write_text(json.dumps({
                "valid": ["one.json"], "invalid": ["bad.json"]
            }))
            (community / "one.json").write_text("{}")
            (invalid_dir / "bad.json").write_text("{}")
            self.assertEqual(load_manifest(Path(temporary), "v1"),
                             {"valid": ["one.json"], "invalid": ["bad.json"]})
            (community / "omitted.json").write_text("{}")
            with self.assertRaises(CheckError):
                load_manifest(Path(temporary), "v1")

    def test_zero_exit_with_script_or_parse_error_is_rejected(self):
        for diagnostic in ("SCRIPT ERROR: bad script", "Parse Error: bad syntax", "ERROR: bad import"):
            code = f"print({diagnostic!r})"
            with self.subTest(diagnostic=diagnostic):
                with self.assertRaisesRegex(CheckError, "engine errors"):
                    run_process([sys.executable, "-c", code], cwd=Path.cwd(), env=os.environ.copy(), timeout=5)


if __name__ == "__main__":
    unittest.main()
