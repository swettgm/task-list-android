#!/usr/bin/env python3
"""Offline integrity checks, independent of the Android SDK. Not a substitute for a build."""
import re
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main"
ANDROID = "{http://schemas.android.com/apk/res/android}"

subprocess.run(["java", str(ROOT / "tools/SourceCheck.java"), str(MAIN / "java"),
                str(ROOT / "app/src/test/java")], check=True)

resources = set()
definitions = {}
xml_files = sorted(MAIN.rglob("*.xml"))
for file in xml_files:
    tree = ET.parse(file)
    if file.parent.name.split("-")[0] == "values":
        for node in tree.getroot():
            if node.get("name"):
                definition = (file.parent.name, node.tag, node.get("name"))
                assert definition not in definitions, f"Duplicate resource {definition}: {file} and {definitions.get(definition)}"
                definitions[definition] = file
                resources.add((node.tag, node.get("name")))
    elif file.name != "AndroidManifest.xml":
        resources.add((file.parent.name.split("-")[0], file.stem))
    for node in tree.iter():
        for value in node.attrib.values():
            if value.startswith("@+id/"):
                resources.add(("id", value.split("/", 1)[1]))

for file in xml_files:
    for kind, name in re.findall(r"@(?!(?:android:|\+))([a-z]+)/([a-zA-Z_0-9]+)", file.read_text()):
        assert (kind, name) in resources, f"Missing XML resource {kind}/{name}: {file}"
for file in (MAIN / "java").rglob("*.java"):
    for kind, name in re.findall(r"(?<!android\.)\bR\.([a-z]+)\.([a-zA-Z_0-9]+)", file.read_text()):
        assert (kind, name) in resources, f"Missing Java resource {kind}/{name}: {file}"
manifest = ET.parse(MAIN / "AndroidManifest.xml").getroot()
namespace_match = re.search(r"namespace\s+['\"]([^'\"]+)['\"]", (ROOT / "app/build.gradle").read_text())
assert namespace_match, "Android namespace missing from app/build.gradle"
namespace = namespace_match.group(1)
for node in manifest.findall("./application/*"):
    if node.tag not in ("activity", "receiver"):
        continue
    name = node.get(ANDROID + "name")
    assert name, f"Manifest component missing a class name: {node.tag}"
    path = MAIN / "java" / ((namespace + name if name.startswith(".") else name).replace(".", "/") + ".java")
    assert path.is_file(), f"Manifest class missing: {name}"
print(f"XML and resource references passed: {len(xml_files)} XML files, {len(resources)} resource definitions.")

print("Offline source integrity passed. Android build, Robolectric, lint, and device checks have not run here.")
