# Andromeda Status

By **pokesmells** / **Bare Minimum Studios**.

Fabric 1.21.1 addon for Max Henkel's **Status** mod.

This project does **not** fork, bundle, copy, or redistribute Status. Status remains a required external dependency. The build pulls the published Fabric 1.21.1 Status artifact from Modrinth Maven.

## Required mods

- Minecraft 1.21.1
- Fabric Loader 0.15.11+
- Fabric API
- Status 1.21.1-1.0.8
- Andromeda Status

Install Status and this addon on the server and on modded clients.

## Statuses

The original availability choices and neutral/no-status choice are replaced by:

- In Character
- Out of Character
- Open to RP
- Busy / AFK
- Event
- New
- Recording
- Streaming

`Out of Character` is the fallback/default. Blank, neutral, unsupported, and legacy availability states are normalized to OOC/none.

The original **No Sleep** toggle is preserved.

## Server configuration

On first server startup the addon writes:

`config/andromeda_status/statuses.json`

Example defaults:

```json
{
  "statuses": {
    "andromeda:ic": {
      "label": "In Character",
      "color": "#2EEC23"
    },
    "andromeda:ooc": {
      "label": "Out of Character",
      "color": "#FF3347"
    },
    "andromeda:open_rp": {
      "label": "Open to RP",
      "color": "#FFC83D"
    },
    "andromeda:busy_afk": {
      "label": "Busy / AFK",
      "color": "#8A8A8A"
    },
    "andromeda:event": {
      "label": "Event",
      "color": "#A855F7"
    },
    "andromeda:new": {
      "label": "New",
      "color": "#42D9FF"
    },
    "recording": {
      "label": "Recording",
      "color": "#FF0014"
    },
    "streaming": {
      "label": "Streaming",
      "color": "#8821FF"
    }
  }
}
```

The server sends these labels/colors to clients with the addon when they join, keeping the display consistent for everyone using the server modpack.

The IDs are deliberately fixed so old/malicious/unknown Status strings cannot create arbitrary entries. Labels and colors are configurable.

## How it works

- Status already serializes its base status as a string, so the addon uses custom string IDs through Status's existing networking.
- A small mixin normalizes Status `PlayerState` objects so old Availability values are disabled and unsupported/blank base statuses become OOC.
- The server explicitly stores OOC as a player's initial state so players who join later also receive that default.
- The addon replaces the original U-key Status screen with an Andromeda screen without editing the Status jar.
- Client-side dynamic icons are generated from the server-provided hex colors.
- Recording and Streaming remain valid Status base states.
- No Sleep remains functional.

## Build

```bash
./gradlew build
```

The built addon jar will be under `build/libs/`. The Status jar is a dependency and is not included in the addon jar.


## 1.0.1

- Fixed a native OpenGL crash on client startup by deferring dynamic status icon creation until the normal client tick loop, after the render context is initialized.


## 1.0.2

- Fixed the status screen render order so Minecraft's background blur runs only in the background pass.
- The panel is now drawn after the world blur, while status color swatches and the "Set your Status" title are drawn as foreground elements after the widgets, keeping them crisp.

## 1.0.3

- Switched Andromeda Status to the Bare Minimum License (BML) v1.0.
- Prevented the original neutral Status icon from briefly appearing while dynamic addon icons are still being prepared.
- Added bounds validation to the server-to-client status-definition payload.
- Status labels now strip control characters before being written/synced.
- Login initialization now preserves an already-present state instead of blindly overwriting it with the default.


## 1.0.4

- Renamed the project from Andromeda Status Addon to Andromeda Status.
- Updated the mod ID, package namespace, config folder, translation namespace, mixin config, and archive name to match.
- Updated authors to pokesmells and Bare Minimum Studios.
- Updated the mod icon.
