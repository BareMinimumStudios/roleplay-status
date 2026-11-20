# Roleplay Status configuration

Open **U â†’ Configure**. Fzzy Config 0.7.7 provides eight named sections, each with a label and an opaque RGB color picker. The server controls the configuration. The default editing permission is operator level 2; ordinary players can view it but cannot submit server changes.

Accepted in-game changes apply live, update the status screen and icons, and sync through Fzzy Config. Player choices and No Sleep use the standalone server-validated Roleplay Status protocol. They persist per world in `data/roleplay_status.dat`, independently of the label/color config.

## JSON file

The server file is `config/roleplay_status/statuses.json`. Fzzy Config owns this file and its serialization. Stop the server before editing it manually; restart to load edits. Missing or invalid fields are handled by Fzzy's validation.

An entry uses this structure (merge it into the generated file, keeping other sections):

```json
{
  "inCharacter": {
    "label": "In Character",
    "color": { "r": 46, "g": 236, "b": 35 }
  }
}
```

| JSON section | Fixed status ID |
| --- | --- |
| `inCharacter` | `roleplay_status:ic` |
| `outOfCharacter` | `roleplay_status:ooc` |
| `openToRoleplay` | `roleplay_status:open_rp` |
| `busyAfk` | `roleplay_status:busy_afk` |
| `event` | `roleplay_status:event` |
| `newPlayer` | `roleplay_status:new` |
| `recording` | `recording` |
| `streaming` | `streaming` |

Labels accept 1â€“64 characters; control characters, formatting controls, and Minecraft formatting codes are rejected by the editor. Runtime labels are also trimmed and sanitized. Colors use red, green, and blue components from 0â€“255, without transparency. Fixed IDs keep unsupported or legacy availability values from becoming arbitrary status entries. Unknown saved states fall back to Out of Character. Unknown client requests are rejected.

## Upgrade from Andromeda Status

Remove the old addon JAR. Install Roleplay Status and Fzzy Config with its dependencies. If the new config does not exist, Roleplay Status imports known labels and hex colors from `config/andromeda_status/statuses.json`; it leaves that original file untouched. The old map schema is only supported for migration. New edits use the Fzzy section schema above.

Legacy `andromeda:*` player state IDs normalize to `roleplay_status:*`; Recording and Streaming retain their existing IDs for configuration compatibility. If migration fails, consult the server log and the preserved original file.


## Upgrade from the 1.x addon

Keep `config/roleplay_status/statuses.json`; its eight sections and schema version remain compatible on both loaders. Remove Status and the old addon JAR. Install the loader-specific 2.x JAR, Fzzy Config, and the matching Kotlin runtime.

The old dependency's stored player selections are not migrated. Each player initially uses Out of Character with No Sleep off. Standalone selections are saved by UUID to the world data file and survive reconnects and restarts.

## No Sleep

When enabled, that player is excluded from the active and sleeping populations used to calculate Minecraft's required sleeping percentage. At least one eligible player must sleep; opting everyone out does not automatically skip night. Changes update the current dimension's sleep calculation immediately.


## Local accessibility

**Configure → Accessibility** contains two independent client-only settings:

- **Colorblind mode:** distinct shapes for each status, also available on the U screen.
- **Show status labels:** optional gray status suffixes in Tab, **off by default**. Works with colorblind mode on or off.

These preferences are saved in `config/roleplay_status/accessibility.json`, require no operator permission, and never change server settings. Existing colorblind preferences are preserved; an absent `showStatusLabels` entry defaults to false.


## Nearby dice rolls

Use `/roll` (defaults to d20), `/roll d20`, `/roll 2d6`, or `/roll 1d20+3 Stealth check`.
The reason is optional. Supported dice: d2, d4, d6, d8, d10, d12, d20 and d100;
up to 20 dice per roll, with a modifier from -1000 to +1000.

The server generates the results and shares them in chat with players within **25 blocks**
(including the roller), measured in all directions in the same dimension.
Operators can change the range (1–256 blocks), enable/disable rolling, and adjust the
one-second cooldown through **Configure → Dice rolls** in Fzzy Config.
Settings are saved in `config/roleplay_status/dice.json`.

**Configure → Accessibility → Dice animations** controls the optional local HUD animation.
It is enabled by default and can be disabled without hiding chat results. Dice use
their corresponding polyhedral shapes; d2 is a coin, and d100 uses two percentile d10s
(00 + 0 means 100). The overlay previews up to four dice at a time; chat lists every
individual result and the modifier. For multiple d100s it cycles through percentile pairs.
Animations do not affect outcomes. Fabric and NeoForge on Minecraft 1.21.1 are supported.
