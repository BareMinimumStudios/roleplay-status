# Release Roleplay Status

1. Update `mod_version` in `gradle.properties` and write the release notes in `CHANGELOG.md`.
2. Run `gradlew.bat clean build` (or `./gradlew clean build`). Review the runtime checks in `docs/VALIDATION.md`.
3. Commit and push the reviewed changes. Ensure the GitHub Build workflow succeeds.
4. Add the repository Actions secret `MODRINTH_TOKEN` with permission to publish to project **v5aMEMjW**. Never commit a token.
5. Run **Publish → Run workflow** from the intended branch. The workflow builds, tests, verifies the exact release JAR, and publishes it through MC Publish.

Only Modrinth is configured. The workflow does not create a GitHub release or publish to CurseForge. To add CurseForge later, supply its real project ID and token and review the dependency mappings first.

`MODRINTH.md` and `CURSEFORGE.md` contain ready-to-use description copy. Upload `docs/branding/roleplay-status.png` as the project icon. The banner is `docs/branding/roleplay-status-banner.png`. Project description/logo updates are separate from the version-upload workflow.
