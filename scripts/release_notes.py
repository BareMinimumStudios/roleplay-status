"""Print only the changelog section for the configured release."""
from pathlib import Path
properties = dict(line.split("=", 1) for line in Path("gradle.properties").read_text().splitlines() if "=" in line)
version = properties["mod_version"]
text = Path("CHANGELOG.md").read_text(encoding="utf-8")
heading = f"## {version}"
assert heading in text, f"Missing release notes for {version}"
print(text.split(heading, 1)[1].split("\n## ", 1)[0].strip())
