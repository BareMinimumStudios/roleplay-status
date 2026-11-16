<!-- Prepared page copy; CurseForge publishing is not configured. -->

# Roleplay Status

**Standalone roleplay statuses for Fabric and NeoForge on Minecraft 1.21.1.**

Made by **pokesmells / Bare Minimum Studios**. Press **U** to choose In Character, Out of Character, Open to RP, Busy / AFK, Event, New, Recording, or Streaming. Colored indicators appear in the Tab player list.

Use the clean status screen to choose a status, toggle **No Sleep**, or configure labels and colors. No Sleep excludes you from the players required to skip night. Player choices are validated by the server and persist per world.

## Make it fit your server

Fzzy Config provides an in-game label editor, RGB color picker, presets, and hex entry. Server edits require operator permission level 2. Accepted changes synchronize live. JSON is also available at `config/roleplay_status/statuses.json`.

## Requirements

Install the correct loader build on the server and clients. Use Minecraft 1.21.1 and Java 21.

- **Fabric:** Fabric Loader 0.19.5+, Fabric API, Fabric Language Kotlin 1.13.7+kotlin.2.2.21+, and Fzzy Config 0.7.7+1.21.
- **NeoForge:** NeoForge 21.1.266+, Kotlin for Forge 5.11.0+, and Fzzy Config 0.7.7+fix1+1.21+neoforge.

**Status is no longer required. Remove it and the old addon JAR when installing version 2.** Existing Roleplay Status labels/colors are preserved. Legacy Andromeda labels/colors migrate when no new config exists, with the original preserved. Old Status player selections start fresh; standalone selections persist afterward.

## Credits

Roleplay Status began as an addon for Status by Max Henkel. Version 2 uses its own implementation and does not include Status code or assets. Fzzy Config supplies the editor and configuration synchronization.

Code and artwork: Bare Minimum License v1.0. Source and issue tracker: [GitHub](https://github.com/BareMinimumStudios/roleplay-status).
