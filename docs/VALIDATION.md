# Roleplay Status 2.0.1+1.21.1 validation

- Both loader builds pass, with 26 regression tests each (52 total).
- Tab badges now use a drawn rectangle and a private space-only font reserving ten pixels, independent of the server pack's text glyphs. Tests verify original TAB formatting and custom font styles remain intact and indicators are not duplicated.
- Status packets now carry bounded account names. Tests cover changed Tab UUIDs, exact UUID precedence, unique case-insensitive account-name fallback, disconnect cleanup, and rejection of ambiguous/fake layout identities. Formatted nicknames are not guessed.
- Handshake retries run once per second until acknowledged; server replay is throttled and snapshots are repeatable. Protocol 3 requires both client and server to update to 2.0.1.
- Packaged release verification passes for both loaders; the Fabric rectangle-rendering invocation target is remapped to intermediary names correctly.
- The live server's TAB configuration and the rendered result have not been tested here. Server-side installation of Roleplay Status is required. Anonymous TAB Layout entries with neither the real UUID nor account name cannot be reliably attributed to a player.


- Additional fix: the installed Figura mod also replaces getNameForDisplay results. Roleplay now wraps the completed name call inside render, so later RETURN handlers cannot discard the indicator marker. A replacement-name regression test covers this ordering.
- Indicators are automatic: synchronized snapshots include all online players and missing selections use Out of Character. The green bound-key reminder is displayed once per connection without any opt-in or handshake dependency. Reminder wording, key color, session reset, and default statuses are tested.

- Visual polish: full indicators are now 6x6 with one-pixel vertical padding, centered in a 10-pixel slot. Recording and Streaming use right-half-square indicators with orange/blue new defaults. Existing saved colors are preserved.
- A recent empty chat entry immediately before the join reminder is trimmed from visible history. Tests verify ordinary chat and old separators are preserved; no global Chat Patches settings are changed.

- Client-only Accessibility config is registered with Fzzy RegisterType.CLIENT. Colorblind mode applies immediately through the U-screen toggle or Configure and is not synchronized to the server.
- All eight six-pixel silhouettes are distinct without color. Tests cover shape uniqueness, bounds, normal-mode compatibility, text labels, and preservation of custom player names.

- Issue #68: status text suffixes are independently optional and off by default. Tests exercise all colorblind/label combinations, preserve custom name styles, and verify enabled labels use gray text. Existing client configs receive the default-off missing field without changing colorblind preferences.

## Previous release validation
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
