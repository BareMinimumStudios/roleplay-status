# Roleplay Status 2.0.0+1.21.1 validation

## Automated checks

- Gradle 9.8.0 / Java 21 builds both standalone loader artifacts. Shared Kotlin sources compile against Fabric's official mappings and NeoForge separately.
- Twelve regression tests per loader cover catalog normalization, UTF-16 label limits, client status validation, per-player NBT serialization, invalid saved UUIDs, legacy/unknown saved states, compressed world-data reload, bounded client packets, and packet/handshake round trips.
- The disk reload regression exposed vanilla Fabric's requirement for a non-null data fixer type. The saved-data factory was corrected; the same disk test passes on both loaders.
- Artifact verification checks loader metadata, exact version and identity, loader-exclusive entrypoints, required config/Kotlin dependencies, common state storage and mixins, the icon, and absence of bundled Status/Fzzy classes or any Status bytecode references.
- Current-release notes are extracted independently for publishing. Both JARs are verified before either Modrinth upload. Each upload has its own loader and dependency list; there is no Status dependency.

## Background runtime checks

- Fabric dedicated-server initialization loaded 2.0.0 without Status and registered Fzzy Config. The isolated run stopped at the EULA gate; the EULA was not accepted.
- Fabric legacy config migration preserved labels and exact RGB channels (#123456 and #ABCDEF), used defaults for malformed fields, retained the original file, and generated exactly eight status sections plus schema version.
- NeoForge loaded the standalone mod with Kotlin for Forge and Fzzy Config and reached a running world. Existing 1.x-format labels/colors loaded successfully. This used NeoForge's development launch, which bypasses the EULA gate by default; no EULA file was accepted or edited. The smoke-test process was stopped after startup, so its Gradle run task reports the intentional process termination.
- No Minecraft client window or desktop input was used. Mixin MCP cannot inspect this project through the currently open IDE project; command-line Gradle checks are the build authority for this rewrite.

## Remaining in-game release checks

These checks have not been claimed as complete. Test Fabric and NeoForge independently with matching server/client loader builds:

1. Install the remapped Fabric release or NeoForge release with the documented dependencies and without Status/older addons.
2. Open U, select all eight states, and confirm the Tab badges on a second client. Test rebinding U, keyboard navigation, tooltips, narrow windows, large GUI scales, Configure return, Done, and Escape.
3. As an operator, edit labels/colors through Configure and confirm live updates on another client and an already-open screen. Confirm a non-operator cannot change server settings.
4. Reconnect and restart the world; confirm selected status and No Sleep persist. Check join snapshots, leaving-player cleanup, respawn, dimension changes, and switching between worlds/servers.
5. With at least two players, verify No Sleep excludes the opted-out player, changing the flag refreshes sleep requirements immediately, spectators remain excluded, and Minecraft's sleeping percentage is respected. Opting everyone out must not skip night without an eligible sleeper.
6. Confirm stale/disconnected sessions cannot submit changes and rapid client requests remain bounded. Confirm protocol mismatches cannot silently alter statuses.

Modrinth uploads and public project-description edits were not performed. CurseForge publishing remains disabled. The GitHub workflow itself must run after these local changes are pushed.
