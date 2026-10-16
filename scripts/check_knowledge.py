#!/usr/bin/env python3
"""Check the structure and local links of the project knowledge index.

This is deliberately an offline structural check. It does not assess whether
claims are true, sources are current, or permissions have been obtained.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from datetime import date
from pathlib import Path
from typing import Iterable, Mapping, Optional, Union
from urllib.parse import unquote, urlsplit


KNOWLEDGE_DIR = Path("docs/knowledge")
REGISTRY = KNOWLEDGE_DIR / "sources.json"
INDEX = KNOWLEDGE_DIR / "index.md"
SHA256_RE = re.compile(r"^[0-9a-fA-F]{64}$")


def _inside(path: Path, parent: Path) -> bool:
    try:
        path.relative_to(parent)
        return True
    except ValueError:
        return False


def _without_fenced_code(text: str) -> str:
    """Blank fenced code content, including backtick and tilde fences."""
    result: list[str] = []
    fence_char: Optional[str] = None
    fence_len = 0
    for line in text.splitlines(keepends=True):
        match = re.match(r"^\s{0,3}(`{3,}|~{3,})", line)
        if fence_char is None:
            if match:
                fence_char = match.group(1)[0]
                fence_len = len(match.group(1))
                result.append("\n" if line.endswith("\n") else "")
            else:
                result.append(line)
        else:
            result.append("\n" if line.endswith("\n") else "")
            closing = re.match(r"^\s{0,3}(`{3,}|~{3,})[ \t]*$", line.rstrip("\r\n"))
            if closing and closing.group(1)[0] == fence_char and len(closing.group(1)) >= fence_len:
                fence_char = None
                fence_len = 0
    return "".join(result)


def _inline_destinations(text: str) -> Iterable[str]:
    """Yield destinations from ordinary inline links and images.

    This small scanner handles nested parentheses and angle-bracketed
    destinations, which covers the repository's documentation without trying
    to implement all of Markdown.
    """
    text = _without_fenced_code(text)
    pos = 0
    while pos < len(text):
        opening = text.find("](", pos)
        if opening < 0:
            return
        # Ignore a close bracket that is escaped. The text before it is the
        # link label; the opening bracket itself may have an image marker.
        backslashes = 0
        probe = opening - 1
        while probe >= 0 and text[probe] == "\\":
            backslashes += 1
            probe -= 1
        if backslashes % 2:
            pos = opening + 2
            continue

        start = opening + 2
        while start < len(text) and text[start].isspace():
            start += 1
        if start >= len(text):
            return
        if text[start] == "<":
            end = start + 1
            escaped = False
            while end < len(text):
                char = text[end]
                if char == ">" and not escaped:
                    break
                if char == "\\" and not escaped:
                    escaped = True
                else:
                    escaped = False
                end += 1
            destination = text[start + 1 : end]
            if end < len(text):
                yield destination
                pos = end + 1
                # Skip the rest of this link's parenthesized title.
                depth = 1
                while pos < len(text) and depth:
                    if text[pos] == "\\":
                        pos += 2
                        continue
                    if text[pos] == "(":
                        depth += 1
                    elif text[pos] == ")":
                        depth -= 1
                    pos += 1
            else:
                pos = start + 1
            continue

        end = start
        depth = 0
        escaped = False
        while end < len(text):
            char = text[end]
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == "(":
                depth += 1
            elif char == ")":
                if depth == 0:
                    break
                depth -= 1
            elif char.isspace() and depth == 0:
                break
            end += 1
        destination = text[start:end]
        if destination:
            yield destination
        # Advance through the full link, respecting parentheses in titles.
        pos = end
        nesting = 1 + depth
        while pos < len(text) and nesting:
            if text[pos] == "\\":
                pos += 2
                continue
            if text[pos] == "(":
                nesting += 1
            elif text[pos] == ")":
                nesting -= 1
            pos += 1


def _local_target(
    destination: str, markdown_file: Path
) -> tuple[Optional[Path], Optional[str]]:
    destination = destination.strip()
    if not destination or destination.startswith("#"):
        return None, None
    # Angle-bracketed destinations are unwrapped by the scanner. Decode URL
    # escapes before path resolution so encoded traversal cannot bypass checks.
    decoded = unquote(destination)
    try:
        parsed = urlsplit(decoded)
    except ValueError as exc:
        return None, f"malformed link URL {destination!r}: {exc}"
    if parsed.scheme.lower() in {"http", "https", "mailto"}:
        return None, None
    if parsed.netloc:
        return None, f"unsupported network-path link: {destination}"
    path_text = parsed.path
    if not path_text:
        return markdown_file.resolve(), None
    candidate = Path(path_text)
    if candidate.is_absolute():
        return None, f"absolute local link path is not allowed: {destination}"
    candidate = markdown_file.parent / candidate
    return candidate.resolve(), None


def _read_markdown(path: Path, errors: list[str]) -> Optional[str]:
    try:
        return path.read_text(encoding="utf-8")
    except (OSError, UnicodeError) as exc:
        errors.append(f"{path}: cannot read UTF-8 Markdown: {exc}")
        return None


def _validate_local_links(path: Path, root: Path, errors: list[str]) -> list[Path]:
    text = _read_markdown(path, errors)
    if text is None:
        return []
    resolved_root = root.resolve()
    targets: list[Path] = []
    for destination in _inline_destinations(text):
        target, target_error = _local_target(destination, path)
        if target_error:
            errors.append(f"{path}: {target_error}")
            continue
        if target is None:
            continue
        if not _inside(target, resolved_root):
            errors.append(f"{path}: link escapes repository: {destination}")
        elif not target.exists():
            errors.append(f"{path}: broken local link: {destination}")
        else:
            targets.append(target)
    return targets


def _nonempty_string(record: dict, key: str, where: str, errors: list[str]) -> Optional[str]:
    value = record.get(key)
    if not isinstance(value, str) or not value.strip():
        errors.append(f"{where}: {key} must be a nonempty string")
        return None
    return value.strip()


def _valid_date(value: Optional[str], key: str, where: str, errors: list[str]) -> Optional[date]:
    if value is None or not re.fullmatch(r"\d{4}-\d{2}-\d{2}", value):
        errors.append(f"{where}: {key} must be a valid YYYY-MM-DD date")
        return None
    try:
        return date.fromisoformat(value)
    except ValueError:
        errors.append(f"{where}: {key} must be a valid YYYY-MM-DD date")
        return None


def _load_registry(path: Path, errors: list[str]) -> list[dict]:
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        errors.append(f"{path}: source registry is missing")
        return []
    except (OSError, UnicodeError, json.JSONDecodeError) as exc:
        errors.append(f"{path}: cannot read valid JSON: {exc}")
        return []
    if not isinstance(data, dict):
        errors.append(f"{path}: registry must be a JSON object")
        return []
    version = data.get("version")
    if not isinstance(version, int) or isinstance(version, bool) or version != 1:
        errors.append(f"{path}: version must be 1")
    records = data.get("sources")
    if not isinstance(records, list):
        errors.append(f"{path}: sources must be an array")
        return []
    normalized: list[dict] = []
    for index, record in enumerate(records):
        if isinstance(record, dict):
            normalized.append(record)
        else:
            errors.append(f"{path}: sources[{index}] must be an object")
    return normalized


def _validate_records(records: list[dict], root: Path, errors: list[str]) -> list[Path]:
    seen_ids: set[str] = set()
    seen_notes: set[str] = set()
    notes: list[Path] = []
    knowledge_root = (root / KNOWLEDGE_DIR).resolve()
    for index, record in enumerate(records):
        where = f"{REGISTRY} sources[{index}]"
        source_id = _nonempty_string(record, "id", where, errors)
        _nonempty_string(record, "title", where, errors)
        kind = _nonempty_string(record, "kind", where, errors)
        recorded_text = _nonempty_string(record, "recorded", where, errors)
        reviewed_text = _nonempty_string(record, "reviewed", where, errors)
        note_text = _nonempty_string(record, "note", where, errors)
        _nonempty_string(record, "scope", where, errors)
        _nonempty_string(record, "limitations", where, errors)

        if source_id:
            if source_id in seen_ids:
                errors.append(f"{where}: duplicate source id {source_id!r}")
            seen_ids.add(source_id)
        recorded = _valid_date(recorded_text, "recorded", where, errors)
        reviewed = _valid_date(reviewed_text, "reviewed", where, errors)
        if recorded and reviewed and reviewed < recorded:
            errors.append(f"{where}: reviewed must be on or after recorded")
        if kind not in {"web", "attachment"}:
            errors.append(f"{where}: kind must be 'web' or 'attachment'")

        if note_text:
            note_path = Path(note_text)
            if note_path.is_absolute() or ".." in note_path.parts or note_path.suffix.lower() != ".md":
                errors.append(f"{where}: note must be a safe relative Markdown path under docs/knowledge")
            else:
                # Registry note paths are relative to docs/knowledge.
                resolved = (root / KNOWLEDGE_DIR / note_path).resolve()
                if not _inside(resolved, knowledge_root) or not _inside(resolved, root.resolve()):
                    errors.append(f"{where}: note must resolve inside docs/knowledge")
                else:
                    normalized = resolved.as_posix()
                    if normalized in seen_notes:
                        errors.append(f"{where}: duplicate note path {note_text!r}")
                    seen_notes.add(normalized)
                    if not resolved.is_file():
                        errors.append(f"{where}: note file does not exist: {note_text}")
                    else:
                        notes.append(resolved)

        if kind == "web":
            url = _nonempty_string(record, "url", where, errors)
            if url:
                try:
                    parsed = urlsplit(url)
                except ValueError as exc:
                    errors.append(f"{where}: url is malformed: {exc}")
                    continue
                if (
                    parsed.scheme.lower() != "https"
                    or not parsed.netloc
                    or parsed.username is not None
                    or parsed.password is not None
                ):
                    errors.append(f"{where}: web url must be an absolute HTTPS URL")
        elif kind == "attachment":
            file_name = _nonempty_string(record, "file_name", where, errors)
            digest = _nonempty_string(record, "sha256", where, errors)
            if file_name and (Path(file_name).name != file_name or file_name in {".", ".."}):
                errors.append(f"{where}: file_name must be a basename")
            if digest and not SHA256_RE.fullmatch(digest):
                errors.append(f"{where}: sha256 must contain 64 hexadecimal characters")
    return notes


def check(root: Union[Path, str], raw_sources: Optional[Mapping[str, Union[Path, str]]] = None) -> list[str]:
    """Return structural errors for a repository root, without changing files."""
    root = Path(root).resolve()
    errors: list[str] = []
    index_path = root / INDEX
    knowledge_path = root / KNOWLEDGE_DIR
    resolved_root = root.resolve()
    resolved_knowledge = knowledge_path.resolve()
    knowledge_root_is_safe = _inside(resolved_knowledge, resolved_root)
    if not knowledge_root_is_safe:
        errors.append(f"{knowledge_path}: knowledge directory resolves outside repository")

    index_is_safe = False
    if not knowledge_root_is_safe:
        index_targets: list[Path] = []
    elif not index_path.is_file():
        errors.append(f"{INDEX}: knowledge index is missing")
        index_targets: list[Path] = []
    elif not _inside(index_path.resolve(), resolved_knowledge) or not _inside(index_path.resolve(), resolved_root):
        errors.append(f"{index_path}: knowledge page resolves outside docs/knowledge")
        index_targets = []
    else:
        index_is_safe = True
        index_targets = _validate_local_links(index_path, root, errors)

    registry_path = root / REGISTRY
    if not knowledge_root_is_safe:
        records = []
    elif not _inside(registry_path.resolve(), resolved_knowledge) or not _inside(registry_path.resolve(), resolved_root):
        errors.append(f"{registry_path}: source registry resolves outside docs/knowledge")
        records = []
    else:
        records = _load_registry(registry_path, errors)
    record_notes = _validate_records(records, root, errors)

    markdown_pages = sorted(knowledge_path.rglob("*.md")) if knowledge_root_is_safe and knowledge_path.is_dir() else []
    resolved_index = index_path.resolve()
    safe_markdown_pages: list[Path] = []
    for page in markdown_pages:
        if page == index_path and not index_is_safe:
            continue
        if not _inside(page.resolve(), resolved_knowledge) or not _inside(page.resolve(), resolved_root):
            errors.append(f"{page}: knowledge page resolves outside docs/knowledge")
        else:
            safe_markdown_pages.append(page)
    maintained = [page.resolve() for page in safe_markdown_pages if page.resolve() != resolved_index]
    counts: dict[Path, int] = {}
    for target in index_targets:
        if target.suffix.lower() == ".md":
            counts[target] = counts.get(target, 0) + 1
    for page in maintained:
        count = counts.get(page, 0)
        relative = page.relative_to(root.resolve()).as_posix() if _inside(page, root.resolve()) else str(page)
        if count == 0:
            errors.append(f"{INDEX}: knowledge page is not linked: {relative}")
        elif count > 1:
            errors.append(f"{INDEX}: knowledge page must be linked exactly once (found {count}): {relative}")
    for note in record_notes:
        if counts.get(note, 0) != 1:
            relative = note.relative_to(root.resolve()).as_posix()
            errors.append(f"{REGISTRY}: source note must be linked exactly once by {INDEX}: {relative}")

    # Link checking is limited to maintained knowledge pages as requested.
    for page in safe_markdown_pages:
        if page.resolve() != resolved_index:
            _validate_local_links(page, root, errors)

    for source_id, supplied_path in (raw_sources or {}).items():
        matches = [record for record in records if record.get("id") == source_id]
        if len(matches) != 1:
            errors.append(f"--raw-source {source_id}: source id must match exactly one registry record")
            continue
        record = matches[0]
        if record.get("kind") != "attachment":
            errors.append(f"--raw-source {source_id}: registry record is not an attachment")
            continue
        expected = record.get("sha256")
        if not isinstance(expected, str) or not SHA256_RE.fullmatch(expected):
            errors.append(f"--raw-source {source_id}: attachment has no valid registered SHA-256")
            continue
        try:
            digest = hashlib.sha256(Path(supplied_path).read_bytes()).hexdigest()
        except OSError as exc:
            errors.append(f"--raw-source {source_id}: cannot read supplied file: {exc}")
            continue
        if digest.lower() != expected.lower():
            errors.append(f"--raw-source {source_id}: SHA-256 does not match registry")
    return errors


def _parse_raw_source(value: str) -> tuple[str, Path]:
    source_id, separator, path = value.partition("=")
    if not separator or not source_id.strip() or not path:
        raise argparse.ArgumentTypeError("must use ID=PATH")
    return source_id.strip(), Path(path)


def main(argv: Optional[list[str]] = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parent.parent,
                        help="repository root (defaults to this script's parent repository)")
    parser.add_argument("--raw-source", action="append", type=_parse_raw_source, default=[], metavar="ID=PATH",
                        help="optionally verify a supplied attachment snapshot against its registered SHA-256")
    args = parser.parse_args(argv)
    raw_sources: dict[str, Path] = {}
    for source_id, path in args.raw_source:
        if source_id in raw_sources:
            parser.error(f"--raw-source supplied more than once for {source_id!r}")
        raw_sources[source_id] = path
    errors = check(args.root, raw_sources)
    if errors:
        for error in errors:
            print(error, file=sys.stderr)
        return 1
    print("Knowledge structure and local links are valid.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
