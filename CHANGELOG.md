# Changelog

## 2.0.1+1.21.1

- Address andromeda-mc #68: make Tab status labels independently optional and off by default, preserving colorblind shapes. Optional suffixes use gray text to distinguish them from player names.

- Inset indicators to 6x6 with vertical padding while preserving their Tab slot spacing, including colorblind shapes.

- Add client-only colorblind mode with eight unique shapes, readable Tab status labels, a status-screen toggle, and a local Fzzy Accessibility config.

- Align 8-pixel-high Tab indicators with player heads and center them in a 10-pixel slot.
- Restore right-half-square activity indicators for Recording and Streaming, with new orange and blue defaults; preserve saved colors.

- Apply indicators after all player-name replacement hooks, including Figura, instead of allowing later hooks to discard them.
- Enable default indicators for online players without requiring a status selection; synchronize all online players on join.
- Show the join reminder automatically, with the bound status key highlighted green.

- Draw Tab indicators as color blocks with reserved space so resource-pack fonts cannot hide them.
- Preserve TAB formatting and custom fonts; resolve changed UUIDs by unambiguous account names.
- Retry the client handshake and make throttled server snapshots repeatable.
- Include bounded account names in synchronization; update both server and client to 2.0.1.

## 2.0.0+1.21.1

- Fix incorrectly encoded selected-status checkmarks, pagination arrows, ellipses, and config text; verify UI symbols in both release JARs.

- Become a standalone mod; remove Status as a runtime and build dependency.
- Add native Fabric and NeoForge builds sharing the Kotlin implementation.
- Implement server-owned player statuses, bounded/versioned packets, request validation, rate limiting, join synchronization, and disconnect cleanup.
- Save status and No Sleep preferences by UUID in each world's `data/roleplay_status.dat`.
- Open the status screen directly with its own rebindable U key and show original colored Tab-list badges.
- Implement No Sleep using the world's sleeping percentage and immediately refresh sleep calculations after changes.
- Preserve the existing Fzzy JSON schema, admin editor, color picker, live config sync, and non-destructive Andromeda config migration.
- Add separate loader builds, tests, artifact verification, local build helpers, and Modrinth-only uploads with loader-specific dependencies.
- Keep Gradle 9.8.0 and Java 21; use NeoForge 21.1.266, ModDevGradle 2.0.148, and the compatible NeoForge Fzzy Config build.
- Remove Status and older addon JARs when upgrading. Old Status player selections start fresh; existing labels/colors are retained.

## 1.0.7+1.21.1

- Rewrite the mod initialization, status catalog, client runtime, screen, and configuration in Kotlin.
- Preserve existing JSON settings, operator permissions, live synchronization, and Andromeda migration.
- Require Fabric Language Kotlin 1.13.7+kotlin.2.2.21 or newer.
- Use GitHub's native build badge linked to the main-branch workflow.

## 1.0.6+1.21.1

- Rename Andromeda Status to Roleplay Status across metadata, namespaces, UI, artifacts, and branding.
- Add a flat speech-bubble logo and coordinated project-page assets.
- Replace the original Status screen before initialization; add responsive layout, selection markers, full-label tooltips, Configure, and Done.
- Use Fzzy Config 0.7.7+1.21 for server-owned editable labels, RGB color picking, permissions, JSON storage, and live synchronization.
- Preserve the No Sleep toggle and migrate legacy labels, colors, and roleplay state IDs.
- Sanitize display labels, avoid unnecessary texture rebuilds, and update an open screen when configuration or player state changes.
- Upgrade Gradle to 9.8.0, Fabric Loader to 0.19.5, Fabric API to 0.116.17+1.21.1, and pin Java 21-compatible Loom 1.17.21.
- Add Mixin MCP 1.5.0, automated builds, and manual MC Publish releases to Modrinth only.
