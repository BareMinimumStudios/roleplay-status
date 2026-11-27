# Release Roleplay Status

1. Set the complete mod version in `gradle.properties`. Move published changes from `## [Unreleased]` into a dated heading such as `## [2.0.4+1.21.1] - 2026-11-27`, keeping the newest release first and grouping entries under change categories such as Added, Changed, or Fixed.
2. With Java 21 and Python 3.11+, run `scripts/build.ps1` on Windows or `bash scripts/build.sh` on Linux/macOS. Both helpers validate the changelog before building and verifying the JARs.
3. Complete the in-game checks in `docs/VALIDATION.md`. Commit and push the reviewed changes and wait for both Build jobs to pass.
4. Keep the repository Actions secret `MODRINTH_TOKEN` configured with Create versions access for project **v5aMEMjW**.
5. Run **Publish â†’ Run workflow** from the intended branch.

Publish builds/tests both loaders and verifies both exact JARs before either upload. It validates the changelog and exports only the matching release section as UTF-8, then uses MC Publish to create two Modrinth versions:

| Upload | Artifact | Required dependencies |
| --- | --- | --- |
| Fabric | `fabric/build/libs/roleplay-status-fabric-2.0.4+1.21.1.jar` | Fabric API, Fabric Language Kotlin, Fzzy Config |
| NeoForge | `neoforge/build/libs/roleplay-status-neoforge-2.0.4+1.21.1.jar` | Kotlin for Forge, Fzzy Config |

Both use the exact version `2.0.4+1.21.1`. Loader labels appear in the release titles and loader metadata. The Status dependency is absent from both uploads. Sources/development JARs are never uploaded. The same Modrinth project and secret work for both loaders.

Uploads are sequential and independent: Modrinth does not offer an atomic two-loader upload. If one succeeds and the other fails, inspect the project, then choose only the failed loader when retrying to avoid duplicating the successful upload. A failed preflight build prevents all uploads.

Only Modrinth is configured. CurseForge and GitHub release publishing remain disabled. `MODRINTH.md` and `CURSEFORGE.md` contain updated standalone page copy; project description and icon updates are separate from version uploads. Upload `docs/branding/roleplay-status.png` as the project icon.

## Create the CurseForge listing

Use the [CurseForge author dashboard](https://authors.curseforge.com/#/projects/create/choose-game) with the Bare Minimum Studios publishing account.

| Field | Value |
| --- | --- |
| Game and project type | Minecraft, Mods |
| Project name | Roleplay Status |
| Summary | Share roleplay statuses in Tab and roll dice with nearby players. Includes accessible indicators and a customizable dice HUD. |
| Description | Paste [CURSEFORGE.md](CURSEFORGE.md) in Markdown mode. It matches the Modrinth feature copy and uses the same preview images. |
| Logo | [Roleplay Status icon](docs/branding/roleplay-status.png) |
| License | Bare Minimum License v1.1 ([text](LICENSE)) |
| Environment | Client and server |
| Source | https://github.com/BareMinimumStudios/roleplay-status |
| Issues | https://github.com/BareMinimumStudios/roleplay-status/issues |

When submitting the project for review, upload the verified JARs separately. Tag each with Minecraft **1.21.1** and its matching loader, and use the display names `2.0.4+1.21.1-fabric` and `2.0.4+1.21.1-neoforge`. Use the matching [changelog section](CHANGELOG.md#2041211---2026-11-27) and the loader-specific dependencies listed above.

Export the current notes for a manual upload with `python scripts/release_notes.py --output build/release-notes.md`.

## Authentication errors

For HTTP 401, replace `MODRINTH_TOKEN` with a valid Modrinth token with **Create versions** (`VERSION_CREATE`) scope. Its owner must own the project or have upload permission on its team/organization. A nonempty secret does not prove access. Never commit or paste tokens into logs, issues, or chat.
