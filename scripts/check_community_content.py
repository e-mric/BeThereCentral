#!/usr/bin/env python3
"""Run the independent community-content contracts and compare their reports."""

from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile

ERROR_LINE = re.compile(r"(?:SCRIPT ERROR|Parse Error|ERROR:)", re.IGNORECASE)
GODOT_REPORT = "BTC_CONTRACT_REPORT:"
NODE_REPORT = "BTC_ACCEPTANCE_REPORT:"
COMPLETION = re.compile(r"^Godot contract checks passed \((\d+) assertions\)\.$", re.MULTILINE)


class CheckError(RuntimeError):
    pass


def semantic_equal(left: object, right: object) -> bool:
    """JSON equality where booleans differ from numbers, 1 equals 1.0, and arrays stay ordered."""
    if isinstance(left, bool) or isinstance(right, bool):
        return type(left) is type(right) and left == right
    if isinstance(left, (int, float)) and isinstance(right, (int, float)):
        return left == right
    if type(left) is not type(right):
        return False
    if isinstance(left, list):
        return len(left) == len(right) and all(semantic_equal(a, b) for a, b in zip(left, right))
    if isinstance(left, dict):
        return left.keys() == right.keys() and all(semantic_equal(left[k], right[k]) for k in left)
    return left == right


def validate_report(report: object, expected: dict[str, list[str]], label: str) -> dict:
    if not isinstance(report, dict) or set(report) != {"valid", "invalid"}:
        raise CheckError(f"{label} acceptance report is missing or malformed")
    for key in ("valid", "invalid"):
        rows = report[key]
        names = [row.get("name") if isinstance(row, dict) else row for row in rows] if isinstance(rows, list) else None
        if names is None or any(not isinstance(name, str) for name in names):
            raise CheckError(f"{label} {key} report has malformed fixture names")
        if len(names) != len(set(names)) or names != expected[key]:
            raise CheckError(f"{label} {key} membership/order differs from the pinned manifest")
    for row in report["valid"]:
        if (not isinstance(row, dict) or set(row) != {"name", "package"}
                or not isinstance(row["package"], dict) or not row["package"]):
            raise CheckError(f"{label} valid report has no normalized package value")
    return report


def load_manifest(root: Path, version: str) -> dict[str, list[str]]:
    directory = root / "content/community" / version
    try:
        manifest = json.loads((directory / "acceptance.json").read_text())
    except (OSError, json.JSONDecodeError) as exc:
        raise CheckError(f"{version} acceptance manifest is unreadable: {exc}") from exc
    if not isinstance(manifest, dict) or set(manifest) != {"valid", "invalid"}:
        raise CheckError(f"{version} acceptance manifest must contain valid and invalid lists")
    result = {}
    for key, fixture_dir in (("valid", directory), ("invalid", directory / "fixtures/invalid")):
        names = manifest[key]
        files = sorted(path.name for path in fixture_dir.glob("*.json")
                       if path.name not in {"acceptance.json", "package.json"})
        if (not isinstance(names, list) or not names or any(not isinstance(name, str) for name in names)
                or any(Path(name).name != name or "/" in name or "\\" in name for name in names)
                or len(names) != len(set(names)) or set(names) != set(files)):
            raise CheckError(f"{version} {key} manifest must list every fixture exactly once")
        result[key] = names
    return result


def require_completion(stdout: str) -> None:
    counts = COMPLETION.findall(stdout)
    if len(counts) != 1 or int(counts[0]) <= 0:
        raise CheckError("Godot contract completion sentinel is missing, duplicated, or has no assertions")


def extract_node_report(stdout: str) -> dict:
    try:
        report = json.loads(stdout)
    except json.JSONDecodeError as exc:
        raise CheckError(f"Node acceptance runner emitted empty, truncated, or non-JSON output: {exc}") from exc
    return report


def extract_marker_report(stdout: str, marker: str, label: str) -> dict:
    lines = [line for line in stdout.splitlines() if line.startswith(marker)]
    if len(lines) != 1:
        raise CheckError(f"{label} must emit exactly one complete {marker} report marker")
    try:
        return json.loads(lines[0][len(marker):])
    except json.JSONDecodeError as exc:
        raise CheckError(f"{label} report marker is truncated or invalid JSON: {exc}") from exc


def compare_reports(node: dict, godot: dict, expected: dict[str, list[str]], label: str) -> None:
    node = validate_report(node, expected, f"{label} Node")
    godot = validate_report(godot, expected, f"{label} Godot")
    if node["invalid"] != godot["invalid"]:
        raise CheckError(f"{label} Node and Godot invalid fixture reports differ")
    node_packages = {row["name"]: row["package"] for row in node["valid"]}
    godot_packages = {row["name"]: row["package"] for row in godot["valid"]}
    for name in expected["valid"]:
        if not semantic_equal(node_packages[name], godot_packages[name]):
            raise CheckError(f"{label} normalized package differs between Node and Godot: {name}")


def run_process(command: list[str], *, cwd: Path, env: dict[str, str], timeout: int,
                log_file: Path | None = None) -> subprocess.CompletedProcess[str]:
    if log_file:
        command = [*command, "--log-file", str(log_file)]
    try:
        result = subprocess.run(command, cwd=cwd, env=env, text=True, capture_output=True,
                                timeout=timeout, check=False)
    except subprocess.TimeoutExpired as exc:
        raise CheckError(f"Timed out after {timeout}s: {' '.join(command)}") from exc
    logged = log_file.read_text(errors="replace") if log_file and log_file.exists() else ""
    combined = result.stdout + "\n" + result.stderr + "\n" + logged
    diagnostics = [line for line in combined.splitlines() if ERROR_LINE.search(line)]
    if result.returncode != 0:
        raise CheckError(f"Command exited {result.returncode}: {' '.join(command)}\n{combined[-5000:]}")
    if diagnostics:
        raise CheckError(f"Command emitted engine errors: {' '.join(command)}\n" + "\n".join(diagnostics))
    return result


def check_assets(root: Path, env: dict[str, str], timeout: int) -> None:
    tracked = subprocess.run(["git", "ls-files", "game/assets"], cwd=root, env=env, text=True,
                             capture_output=True, check=True).stdout.splitlines()
    missing = [name for name in tracked if not (root / name).is_file()]
    if missing:
        raise CheckError(f"Tracked game assets are missing before synchronization: {missing}")
    run_process(["node", "game/tools/sync_catalog_assets.mjs"], cwd=root, env=env, timeout=timeout)
    diff = subprocess.run(["git", "diff", "--exit-code", "HEAD", "--", "game/assets"], cwd=root,
                          env=env, text=True, capture_output=True, check=False)
    untracked = subprocess.run(["git", "ls-files", "--others", "--exclude-standard", "--", "game/assets"],
                               cwd=root, env=env, text=True, capture_output=True, check=True).stdout.splitlines()
    if diff.returncode or untracked:
        raise CheckError("Trusted asset sync changed committed outputs or created untracked files.\n"
                         + diff.stdout[-3000:] + diff.stderr[-3000:] + "\n".join(untracked))


def execute(root: Path, godot: str, timeout: int) -> None:
    godot_path = shutil.which(godot) if os.sep not in godot else godot
    if not godot_path or not Path(godot_path).is_file():
        raise CheckError(f"Godot executable not found: {godot}")
    version = subprocess.run([godot_path, "--version"], text=True, capture_output=True, timeout=timeout)
    if version.returncode or not version.stdout.strip().startswith("4.7.2.stable.official."):
        raise CheckError(f"Expected official Godot 4.7.2, received {version.stdout.strip()!r}")

    with tempfile.TemporaryDirectory(prefix="btc-community-contract-") as temporary:
        scratch = Path(temporary)
        env = os.environ.copy()
        for key in ("XDG_DATA_HOME", "XDG_CONFIG_HOME", "XDG_CACHE_HOME"):
            env[key] = str(scratch / key.lower())
            Path(env[key]).mkdir()
        contract_dir = scratch / "contract-test-data"
        contract_dir.mkdir()
        env["BTC_CONTRACT_TEST_DIR"] = str(contract_dir)
        check_assets(root, env, timeout)
        expected = {version: load_manifest(root, version) for version in ("v1", "v2")}

        reports: dict[str, dict] = {}
        for version_name in ("v1", "v2"):
            suite = root / f"content/community/{version_name}/test/package.test.js"
            runner = root / f"content/community/{version_name}/test/acceptance-runner.{'mjs' if version_name == 'v1' else 'js'}"
            run_process(["node", "--test", str(suite)], cwd=root, env=env, timeout=timeout)
            output = run_process(["node", str(runner)], cwd=root, env=env, timeout=timeout).stdout
            if version_name == "v1":
                report = extract_marker_report(output, NODE_REPORT, "v1 Node runner")
            else:
                report = extract_node_report(output)
            reports[version_name] = validate_report(report, expected[version_name], f"{version_name} Node")

        godot_command = [godot_path, "--headless", "--path", "game"]
        run_process([*godot_command, "--editor", "--import"], cwd=root, env=env, timeout=timeout,
                    log_file=scratch / "godot-import.log")
        contract = run_process([*godot_command, "--script", "res://tests/run_contract.gd"], cwd=root,
                               env=env, timeout=timeout, log_file=scratch / "godot-contract.log")
        require_completion(contract.stdout)
        godot_report = extract_marker_report(contract.stdout, GODOT_REPORT, "Godot contract")
        godot_reports = validate_report({"valid": godot_report.get("valid"), "invalid": godot_report.get("invalid")},
                                        expected["v1"], "Godot v1")
        if "v2" not in godot_report:
            raise CheckError("Godot v2 acceptance report is missing")
        godot_reports_v2 = validate_report(godot_report["v2"], expected["v2"], "Godot v2")
        compare_reports(reports["v1"], godot_reports, expected["v1"], "v1")
        compare_reports(reports["v2"], godot_reports_v2, expected["v2"], "v2")
        print(f"Community content contracts passed: v1 ({len(expected['v1']['valid'])} valid, {len(expected['v1']['invalid'])} invalid), "
              f"v2 ({len(expected['v2']['valid'])} valid, {len(expected['v2']['invalid'])} invalid), Godot 4.7.2.")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--godot", default=os.environ.get("GODOT_BIN", "godot"), help="Godot 4.7.2 executable")
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1], help="repository checkout")
    parser.add_argument("--timeout", type=int, default=180, help="per-command timeout in seconds")
    args = parser.parse_args()
    try:
        execute(args.root.resolve(), args.godot, args.timeout)
    except (CheckError, OSError, subprocess.SubprocessError) as exc:
        print(f"Community content check failed: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
