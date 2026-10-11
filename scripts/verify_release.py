"""Verify the exact installable release before publishing; never select a sources JAR."""
import json
import os
from pathlib import Path
from zipfile import ZipFile

properties = dict(line.split("=", 1) for line in Path("gradle.properties").read_text().splitlines() if "=" in line and not line.startswith("#"))
version = os.environ.get("RELEASE_VERSION", properties["mod_version"])
file = Path(os.environ.get("RELEASE_FILE", f"build/libs/roleplay-status-{version}.jar"))
assert file.is_file(), f"Missing release artifact: {file}"
with ZipFile(file) as jar:
    metadata = json.loads(jar.read("fabric.mod.json"))
    assert metadata["id"] == "roleplay_status"
    assert metadata["name"] == "Roleplay Status"
    assert metadata["version"] == version
    assert metadata["depends"]["minecraft"] == "1.21.1"
    assert "fzzy_config" in metadata["depends"]
    assert metadata["depends"]["fabric-language-kotlin"] == ">=1.13.7+kotlin.2.2.21"
    assert "net/bms/roleplay_status/RoleplayStatus$Companion.class" in jar.namelist()
    assert metadata["icon"] in jar.namelist()
    mixins = json.loads(jar.read("roleplay_status.mixins.json"))
    assert "StatusScreenMixin" in mixins["client"]
    assert not any(name.startswith("de/maxhenkel/status/") for name in jar.namelist()), "Status must not be bundled"
    assert not any(name.startswith("me/fzzyhmstrs/") for name in jar.namelist()), "Fzzy Config must not be bundled"
assert Path("CHANGELOG.md").is_file()
print(f"Verified {file}")
