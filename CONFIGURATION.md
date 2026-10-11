# Roleplay Status configuration

Open **U → Configure**. Fzzy Config 0.7.7 provides eight named sections, each with a label and an opaque RGB color picker. The server controls the configuration. The default editing permission is operator level 2; ordinary players can view it but cannot submit server changes.

Accepted in-game changes apply live, update the status screen and icons, and sync through Fzzy Config. The status choice and No Sleep toggle still use Status's networking.

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

Labels accept 1–64 characters; control characters, formatting controls, and Minecraft formatting codes are rejected by the editor. Runtime labels are also trimmed and sanitized. Colors use red, green, and blue components from 0–255, without transparency. Fixed IDs keep unsupported or legacy availability values from becoming arbitrary status entries. Unknown/blank states fall back to Out of Character.

## Upgrade from Andromeda Status

Remove the old addon JAR. Install Roleplay Status and Fzzy Config with its dependencies. If the new config does not exist, Roleplay Status imports known labels and hex colors from `config/andromeda_status/statuses.json`; it leaves that original file untouched. The old map schema is only supported for migration. New edits use the Fzzy section schema above.

Legacy `andromeda:*` player state IDs normalize to `roleplay_status:*`; Recording and Streaming keep the IDs expected by Status. If migration fails, consult the server log and the preserved original file.
