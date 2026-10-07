"""Catch an Android Activity compiled without Compose compiler lowering."""

from pathlib import Path
import subprocess


root = Path(__file__).resolve().parents[1]
activity = (
    root
    / "androidApp/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"
    / "com/betherecentral/android/MainActivity.class"
)
if not activity.is_file():
    raise SystemExit(f"Missing compiled Activity: {activity}. Build :androidApp:assembleDebug first.")

result = subprocess.run(
    ["javap", "-c", "-p", str(activity)],
    check=True,
    capture_output=True,
    text=True,
)
calls = [line.strip() for line in result.stdout.splitlines() if "ComponentActivityKt.setContent$default:" in line]
if len(calls) != 1 or "Lkotlin/jvm/functions/Function2;" not in calls[0] or "Lkotlin/jvm/functions/Function0;" in calls[0]:
    raise SystemExit(
        "Android Activity has the wrong Compose setContent call signature. "
        "Ensure androidApp applies org.jetbrains.kotlin.plugin.compose.\n"
        + "\n".join(calls or ["No setContent$default call found."])
    )
print("Android Compose Activity signature verified (Function2).")
