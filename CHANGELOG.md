# Roleplay Status 1.0.6+1.21.1

## 1.0.7+1.21.1

- Rewrite the mod initialization, status catalog, client runtime, screen, and configuration in Kotlin.
- Preserve existing JSON settings, operator permissions, live synchronization, and Andromeda migration.
- Require Fabric Language Kotlin 1.13.7+kotlin.2.2.21 or newer.
- Use GitHub's native build badge linked to the main-branch workflow.

- Rename Andromeda Status to Roleplay Status across metadata, namespaces, UI, artifacts, and branding.
- Add a flat speech-bubble logo and coordinated project-page assets.
- Replace the original Status screen before initialization; add responsive layout, selection markers, full-label tooltips, Configure, and Done.
- Use Fzzy Config 0.7.7+1.21 for server-owned editable labels, RGB color picking, permissions, JSON storage, and live synchronization.
- Preserve the No Sleep toggle and migrate legacy labels, colors, and roleplay state IDs.
- Sanitize display labels, avoid unnecessary texture rebuilds, and update an open screen when configuration or player state changes.
- Upgrade Gradle to 9.8.0, Fabric Loader to 0.19.5, Fabric API to 0.116.17+1.21.1, and pin Java 21-compatible Loom 1.17.21.
- Add Mixin MCP 1.5.0, automated builds, and manual MC Publish releases to Modrinth only.
