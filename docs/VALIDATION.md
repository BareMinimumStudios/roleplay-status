# Roleplay Status 1.1.0 validation

## Automated and startup checks

- Clean Java 21 / Gradle 9.8.0 build, JUnit status normalization and label validation tests.
- Release artifact verification: correct identity/version, icon, Minecraft version, Fzzy dependency, replacement mixin, and no bundled Status/Fzzy classes.
- Dedicated-server initialization registered Fzzy Config successfully; generated JSON contains all eight sections and RGB components. The server stopped at its EULA gate, so a multiplayer session was not tested.
- Development client started through renderer/resource initialization after fixing the overloaded mixin selector. The initial runtime test exposed that error despite a passing compile.
- Isolated server initialization verified legacy label migration, exact RGB preservation, malformed-field fallback, and original-file preservation.
- Mixin MCP IDE build passed after refreshing the project files.
- Logo/banner inspected; source SVG and packaged PNG use the same mark and colors.

## Remaining in-game checks before publishing

Visual UI interaction was stopped at the user's request. These checks are not claimed as completed:

1. Join a server with the matching addon, Status 1.21.1-1.0.8, Fabric API, and Fzzy Config dependencies.
2. Press U; confirm only the Roleplay Status screen appears. Select every status and confirm another player sees its indicator. Toggle No Sleep and confirm Status behavior.
3. Open Configure as an operator; edit a label/color with the picker, apply, and confirm another client and an already-open status screen update. Reconnect/restart and confirm persistence.
4. Open Configure without permission level 2; confirm server edits are rejected.
5. Test the smallest supported GUI size, larger GUI scales, keyboard navigation, long labels, tooltips, Configure return, Done, Escape, and the rebound Status key.
6. Test migration using a copy of a legacy config; confirm labels and all three RGB channels, original-file preservation, and normalization of legacy state IDs.
7. Switch between servers with different settings; confirm labels/icons do not leak between sessions.
8. Launch an installed remapped release JAR in a normal Fabric instance, alongside the documented dependencies, before uploading it.

Publishing is manual and Modrinth-only. No marketplace release or project description has been uploaded by this work.
