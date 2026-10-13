"""Check that the offline Studio's production files are actually packaged."""
import hashlib
import sys
import zipfile
from pathlib import Path

if len(sys.argv) != 2:
    raise SystemExit("Usage: python3 scripts/check_studio_apk.py <apk>")

bundle = Path(__file__).resolve().parents[1] / "community-studio/build"
files = [file for file in bundle.rglob("*") if file.is_file()]
if not (bundle / "200.html").is_file() or not any("_app/" in file.relative_to(bundle).as_posix() for file in files):
    raise SystemExit("Build Community Studio before checking its APK.")

with zipfile.ZipFile(sys.argv[1]) as apk:
    for file in files:
        entry = "assets/community-studio/" + file.relative_to(bundle).as_posix()
        try:
            packaged = apk.read(entry)
        except KeyError:
            raise SystemExit(f"Missing offline Studio asset: {entry}")
        if hashlib.sha256(packaged).digest() != hashlib.sha256(file.read_bytes()).digest():
            raise SystemExit(f"Stale offline Studio asset: {entry}")
print(f"Offline Studio APK check passed: {len(files)} production files match.")
