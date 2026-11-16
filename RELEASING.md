# Release Roleplay Status

1. Set the complete `mod_version`, currently `2.0.1+1.21.1`, in `gradle.properties`. Add the matching `## 2.0.1+1.21.1` section to `CHANGELOG.md`.
2. With Java 21 and Python 3.11+, run `gradlew.bat clean build` and `python scripts/verify_release.py`. Linux/macOS: use `./gradlew`. Both loader builds must pass.
3. Complete the in-game checks in `docs/VALIDATION.md`. Commit and push the reviewed changes and wait for both Build jobs to pass.
4. Keep the repository Actions secret `MODRINTH_TOKEN` configured with Create versions access for project **v5aMEMjW**.
5. Run **Publish â†’ Run workflow** from the intended branch.

Publish builds/tests both loaders and verifies both exact JARs before either upload. It extracts only the current changelog section, then uses MC Publish to create two Modrinth versions:

| Upload | Artifact | Required dependencies |
| --- | --- | --- |
| Fabric | `fabric/build/libs/roleplay-status-fabric-2.0.1+1.21.1.jar` | Fabric API, Fabric Language Kotlin, Fzzy Config |
| NeoForge | `neoforge/build/libs/roleplay-status-neoforge-2.0.1+1.21.1.jar` | Kotlin for Forge, Fzzy Config |

Both use the exact version `2.0.1+1.21.1`. Loader labels appear in the release titles and loader metadata. The Status dependency is absent from both uploads. Sources/development JARs are never uploaded. The same Modrinth project and secret work for both loaders.

Uploads are sequential and independent: Modrinth does not offer an atomic two-loader upload. If one succeeds and the other fails, inspect the project, then choose only the failed loader when retrying to avoid duplicating the successful upload. A failed preflight build prevents all uploads.

Only Modrinth is configured. CurseForge and GitHub release publishing remain disabled. `MODRINTH.md` and `CURSEFORGE.md` contain updated standalone page copy; project description and icon updates are separate from version uploads. Upload `docs/branding/roleplay-status.png` as the project icon.

## Authentication errors

For HTTP 401, replace `MODRINTH_TOKEN` with a valid Modrinth token with **Create versions** (`VERSION_CREATE`) scope. Its owner must own the project or have upload permission on its team/organization. A nonempty secret does not prove access. Never commit or paste tokens into logs, issues, or chat.
