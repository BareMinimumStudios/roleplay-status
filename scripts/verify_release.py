"""Verify one or both standalone loader artifacts before publishing."""
import json
import os
import tomllib
from pathlib import Path
from zipfile import ZipFile

properties = dict(line.split("=", 1) for line in Path("gradle.properties").read_text().splitlines()
                  if "=" in line and not line.startswith("#"))
version = os.environ.get("RELEASE_VERSION", properties["mod_version"])
requested = os.environ.get("RELEASE_LOADER")
assert requested in (None, "fabric", "neoforge"), "Unknown release loader"
loaders = [requested] if requested else ["fabric", "neoforge"]
for loader in loaders:
    file = Path(os.environ.get("RELEASE_FILE", f"{loader}/build/libs/roleplay-status-{loader}-{version}.jar"))
    assert file.is_file(), f"Missing installable artifact: {file}"
    assert not file.name.endswith("-sources.jar")
    with ZipFile(file) as jar:
        names = set(jar.namelist())
        if loader == "fabric":
            metadata = json.loads(jar.read("fabric.mod.json"))
            assert metadata["id"] == "roleplay_status"
            assert metadata["name"] == "Roleplay Status"
            assert metadata["version"] == version
            assert metadata["depends"]["minecraft"] == "1.21.1"
            assert "status" not in metadata["depends"]
            assert "fabric-language-kotlin" in metadata["depends"]
            assert "fzzy_config" in metadata["depends"]
            assert "META-INF/neoforge.mods.toml" not in names
            assert "net/bms/roleplay_status/fabric/FabricEntrypoint.class" in names
            assert not any(n.startswith("net/bms/roleplay_status/neoforge/") for n in names)
        else:
            metadata = tomllib.loads(jar.read("META-INF/neoforge.mods.toml").decode())
            mod = metadata["mods"][0]
            assert mod["modId"] == "roleplay_status" and mod["version"] == version
            deps = {d["modId"]: d for d in metadata["dependencies"]["roleplay_status"]}
            assert deps["fzzy_config"]["type"] == "required"
            assert deps["kotlinforforge"]["type"] == "required"
            assert deps["minecraft"]["versionRange"] == "[1.21.1]"
            assert "status" not in deps or deps["status"]["type"] == "incompatible"
            assert "fabric.mod.json" not in names
            assert "net/bms/roleplay_status/neoforge/NeoForgeEntrypoint.class" in names
            assert not any(n.startswith("net/bms/roleplay_status/fabric/") for n in names)
        for feature in ("dice/DiceCommands", "dice/DiceExpression", "dice/DicePayload", "dice/DieGeometry", "client/DiceOverlay", "config/DiceConfig"):
            assert f"net/bms/roleplay_status/{feature}.class" in names, f"Missing dice feature: {feature}"
        screen = jar.read("net/bms/roleplay_status/client/RoleplayStatusScreen.class")
        for symbol in ("\u2713", "\u2026", "\u2039", "\u203a"):
            assert symbol.encode("utf-8") in screen, f"Missing UI symbol: {symbol!a}"
            broken = symbol.encode("utf-8").decode("cp1252").encode("utf-8")
            assert broken not in screen, f"Corrupted UI symbol: {symbol!a}"
        config = jar.read("net/bms/roleplay_status/config/RoleplayStatusConfig$StatusEntry.class")
        assert "\u00c2\u00a7".encode("utf-8") not in config, "Corrupted config label validator"
        indicator_font = json.loads(jar.read("assets/roleplay_status/font/indicator.json"))
        assert indicator_font["providers"][0]["advances"]["\ue000"] == 10
        assert indicator_font["providers"][0]["advances"]["\ue001"] == 10
        assert "net/bms/roleplay_status/client/TabIndicator.class" in names
        assert "assets/roleplay_status/icon.png" in names
        assert "net/bms/roleplay_status/state/StatusData.class" in names
        mixins = json.loads(jar.read("roleplay_status.mixins.json"))
        assert mixins["mixins"] == ["SleepStatusMixin"]
        assert mixins["client"] == ["PlayerTabOverlayMixin", "ChatComponentAccessor"]
        assert not any(n.startswith(("de/maxhenkel/", "me/fzzyhmstrs/")) for n in names)
        assert not any(b"de/maxhenkel/status" in jar.read(n) for n in names if n.endswith(".class")), "Status references remain"
    print(f"Verified standalone {loader}: {file}")
