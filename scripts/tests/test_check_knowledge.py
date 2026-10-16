"""Observable fixture tests for the offline knowledge checker."""

from __future__ import annotations

import hashlib
import json
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS))
import check_knowledge  # noqa: E402


class KnowledgeFixture(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        (self.root / "docs/knowledge/sources").mkdir(parents=True)
        self.note = self.root / "docs/knowledge/sources/attachment.md"
        self.note.write_text("# Attachment notes\n\nA local [reference](../guide.md).\n", encoding="utf-8")
        self.guide = self.root / "docs/knowledge/guide.md"
        self.guide.write_text("# Guide\n", encoding="utf-8")
        self.index = self.root / "docs/knowledge/index.md"
        self.index.write_text(
            "# Knowledge index\n\n[Guide](guide.md)\n[Attachment notes](sources/attachment.md)\n",
            encoding="utf-8",
        )
        payload = b"private fixture bytes\n"
        self.raw = self.root / "private-source.bin"
        self.raw.write_bytes(payload)
        self.attachment_digest = hashlib.sha256(payload).hexdigest()
        self.records = [
            {
                "id": "attachment-example",
                "title": "Example attachment",
                "kind": "attachment",
                "recorded": "2026-10-16",
                "reviewed": "2026-10-16",
                "note": "sources/attachment.md",
                "scope": "A bounded source note.",
                "limitations": "Does not establish product facts.",
                "file_name": "reference.pdf",
                "sha256": self.attachment_digest,
            },
            {
                "id": "official-guide",
                "title": "Official guide",
                "kind": "web",
                "recorded": "2026-10-16",
                "reviewed": "2026-10-16",
                "note": "guide.md",
                "scope": "A public guide.",
                "limitations": "External content is not fetched.",
                "url": "https://example.test/guide",
            },
        ]
        self.write_registry()

    def write_registry(self) -> None:
        (self.root / "docs/knowledge/sources.json").write_text(
            json.dumps({"version": 1, "sources": self.records}, indent=2) + "\n", encoding="utf-8"
        )

    def errors(self) -> list[str]:
        return check_knowledge.check(self.root)


class CheckKnowledgeTests(KnowledgeFixture):
    def test_clean_clone_passes_without_private_raw_attachment(self) -> None:
        self.raw.unlink()
        before = self.index.read_bytes()
        self.assertEqual([], self.errors())
        self.assertEqual(before, self.index.read_bytes())

    def test_local_inline_links_are_checked_and_fences_ignored(self) -> None:
        (self.root / "docs/knowledge/guide.md").write_text(
            "[bad](missing.md)\n```md\n[ignored](also-missing.md)\n```\n", encoding="utf-8"
        )
        self.assertTrue(any("broken local link: missing.md" in error for error in self.errors()))
        self.assertFalse(any("also-missing" in error for error in self.errors()))

    def test_percent_encoded_and_angle_bracket_paths_resolve(self) -> None:
        (self.root / "docs/knowledge/space name.md").write_text("# Space\n", encoding="utf-8")
        self.index.write_text(
            "[Guide](guide.md)\n[Notes](sources/attachment.md)\n[Space](<space%20name.md>)\n",
            encoding="utf-8",
        )
        self.assertEqual([], self.errors())

    def test_repository_escaping_link_is_rejected(self) -> None:
        outside = self.root.parent / (self.root.name + "-outside.md")
        outside.write_text("outside", encoding="utf-8")
        self.addCleanup(outside.unlink)
        self.guide.write_text("[escape](../../../" + outside.name + ")\n", encoding="utf-8")
        self.assertTrue(any("escapes repository" in error for error in self.errors()))

    def test_absolute_local_link_path_is_rejected(self) -> None:
        self.guide.write_text("[absolute](/tmp/outside.md)\n", encoding="utf-8")
        self.assertTrue(any("absolute local link path is not allowed" in error for error in self.errors()))

    def test_malformed_link_url_returns_a_diagnostic(self) -> None:
        self.guide.write_text("[malformed](https://[broken)\n", encoding="utf-8")
        errors = self.errors()
        self.assertTrue(any("malformed link URL" in error for error in errors))

    def test_malformed_and_credentialed_web_urls_are_rejected(self) -> None:
        self.records[1]["url"] = "https://[broken"
        self.write_registry()
        self.assertTrue(any("url is malformed" in error for error in self.errors()))
        self.records[1]["url"] = "https://user:secret@example.test/guide"
        self.write_registry()
        self.assertTrue(any("absolute HTTPS URL" in error for error in self.errors()))

    def test_symlink_escape_is_rejected(self) -> None:
        outside = self.root.parent / (self.root.name + "-outside.md")
        outside.write_text("outside", encoding="utf-8")
        self.addCleanup(outside.unlink)
        link = self.root / "docs/knowledge/outside.md"
        link.symlink_to(outside)
        self.guide.write_text("[escape](outside.md)\n", encoding="utf-8")
        self.assertTrue(any("escapes repository" in error for error in self.errors()))

    def test_registry_symlink_escape_is_rejected_without_reading_target(self) -> None:
        registry = self.root / "docs/knowledge/sources.json"
        registry.unlink()
        outside = self.root.parent / (self.root.name + "-outside-registry.json")
        outside.write_text("this is deliberately not JSON", encoding="utf-8")
        self.addCleanup(outside.unlink)
        registry.symlink_to(outside)

        errors = self.errors()
        self.assertTrue(any("source registry resolves outside docs/knowledge" in error for error in errors))
        self.assertFalse(any("cannot read valid JSON" in error for error in errors))

    def test_knowledge_directory_symlink_escape_is_rejected_without_reading_files(self) -> None:
        knowledge = self.root / "docs/knowledge"
        shutil.rmtree(knowledge)
        outside = self.root.parent / (self.root.name + "-outside-knowledge")
        outside.mkdir()
        self.addCleanup(shutil.rmtree, outside)
        (outside / "index.md").write_text("# Outside\n", encoding="utf-8")
        (outside / "sources.json").write_text("this is deliberately not JSON", encoding="utf-8")
        knowledge.symlink_to(outside, target_is_directory=True)

        errors = self.errors()
        self.assertTrue(any("knowledge directory resolves outside repository" in error for error in errors))
        self.assertFalse(any("cannot read valid JSON" in error for error in errors))

    def test_missing_and_duplicate_index_entries_are_reported(self) -> None:
        self.index.write_text("[Guide](guide.md)\n", encoding="utf-8")
        errors = self.errors()
        self.assertTrue(any("knowledge page is not linked" in error and "attachment.md" in error for error in errors))
        self.assertTrue(any("knowledge page is not linked" in error and "sources/attachment.md" in error for error in errors))
        self.index.write_text(
            "[Guide](guide.md)\n[Guide again](guide.md)\n[Notes](sources/attachment.md)\n",
            encoding="utf-8",
        )
        self.assertTrue(any("linked exactly once (found 2)" in error for error in self.errors()))

    def test_missing_registry_note_and_invalid_metadata_are_reported(self) -> None:
        self.records[0]["note"] = "sources/absent.md"
        self.records[1]["recorded"] = "2026-10-16"
        self.records[1]["reviewed"] = "2026-10-15"
        self.records[1]["url"] = "http://example.test/guide"
        self.records[1]["scope"] = ""
        self.write_registry()
        errors = self.errors()
        self.assertTrue(any("note file does not exist" in error for error in errors))
        self.assertTrue(any("reviewed must be on or after recorded" in error for error in errors))
        self.assertTrue(any("absolute HTTPS URL" in error for error in errors))
        self.assertTrue(any("scope must be a nonempty string" in error for error in errors))
        self.records[1]["recorded"] = "2026-13-01"
        self.write_registry()
        self.assertTrue(any("valid YYYY-MM-DD" in error for error in self.errors()))

    def test_duplicate_source_ids_and_notes_are_rejected(self) -> None:
        self.records[1]["id"] = self.records[0]["id"]
        self.records[1]["note"] = self.records[0]["note"]
        self.write_registry()
        errors = self.errors()
        self.assertTrue(any("duplicate source id" in error for error in errors))
        self.assertTrue(any("duplicate note path" in error for error in errors))

    def test_user_supplied_invalid_marker_does_not_bypass_required_fields(self) -> None:
        self.records.append({"_invalid": True})
        self.write_registry()
        errors = self.errors()
        self.assertTrue(any("id must be a nonempty string" in error for error in errors))
        self.assertTrue(any("kind must be 'web' or 'attachment'" in error for error in errors))

    def test_note_traversal_and_missing_index_are_rejected(self) -> None:
        self.records[0]["note"] = "../outside.md"
        self.write_registry()
        self.index.unlink()
        errors = self.errors()
        self.assertTrue(any("safe relative Markdown path" in error for error in errors))
        self.assertTrue(any("knowledge index is missing" in error for error in errors))

    def test_optional_attachment_digest_check_passes_and_detects_mismatch(self) -> None:
        self.assertEqual([], check_knowledge.check(self.root, {"attachment-example": self.raw}))
        self.raw.write_bytes(b"changed bytes")
        self.assertTrue(any("SHA-256 does not match" in error for error in
                            check_knowledge.check(self.root, {"attachment-example": self.raw})))

    def test_checker_does_not_modify_repository_files(self) -> None:
        tracked = [self.index, self.note, self.guide, self.root / "docs/knowledge/sources.json"]
        before = {path: path.read_bytes() for path in tracked}
        self.assertEqual([], self.errors())
        self.assertEqual(before, {path: path.read_bytes() for path in tracked})

    def test_cli_success_and_failure_exit_status(self) -> None:
        script = SCRIPTS / "check_knowledge.py"
        good = subprocess.run([sys.executable, str(script), "--root", str(self.root)], capture_output=True, text=True)
        self.assertEqual(0, good.returncode, good.stderr)
        self.guide.write_text("[broken](missing.md)\n", encoding="utf-8")
        bad = subprocess.run([sys.executable, str(script), "--root", str(self.root)], capture_output=True, text=True)
        self.assertEqual(1, bad.returncode)
        self.assertIn("broken local link", bad.stderr)


if __name__ == "__main__":
    unittest.main()
