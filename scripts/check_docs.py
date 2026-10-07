"""Require documentation and changelog alongside source/build changes in a PR."""
import subprocess
import sys

if len(sys.argv) != 2:
    raise SystemExit("Usage: python3 scripts/check_docs.py <base-commit>")
changed = subprocess.check_output(["git", "diff", "--name-only", sys.argv[1], "HEAD"], text=True).splitlines()
source_changed = any(p.endswith((".kt", ".kts", ".swift", ".py", ".xml", ".yml", ".properties", ".js", ".mjs", ".html", ".css", ".json")) and not p.startswith(".agents/") for p in changed)
if source_changed:
    docs = any(p.endswith(".md") and p != "CHANGELOG.md" and not p.startswith(".agents/") for p in changed)
    if not docs or "CHANGELOG.md" not in changed:
        raise SystemExit("Source/build changes require an affected Markdown document and CHANGELOG.md in the same PR.")
print("Documentation change check passed.")
