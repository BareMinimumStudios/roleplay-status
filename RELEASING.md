# Release Roleplay Status

1. Update `mod_version` in `gradle.properties` using the complete version, for example `1.0.7+1.21.1` and write the release notes in `CHANGELOG.md`.
2. Run `gradlew.bat clean build` (or `./gradlew clean build`). Review the runtime checks in `docs/VALIDATION.md`.
3. Commit and push the reviewed changes. Ensure the GitHub Build workflow succeeds.
4. Add the repository Actions secret `MODRINTH_TOKEN` with permission to publish to project **v5aMEMjW**. Never commit a token.
5. Run **Publish → Run workflow** from the intended branch. The workflow builds, tests, verifies the exact release JAR, and publishes it through MC Publish.

Only Modrinth is configured. The workflow does not create a GitHub release or publish to CurseForge. To add CurseForge later, supply its real project ID and token and review the dependency mappings first.

`MODRINTH.md` and `CURSEFORGE.md` contain ready-to-use description copy. Upload `docs/branding/roleplay-status.png` as the project icon. The banner is `docs/branding/roleplay-status-banner.png`. Project description/logo updates are separate from the version-upload workflow.

## Modrinth 401: no permission to upload this version

The project ID `v5aMEMjW` is valid. A version upload requires a Modrinth personal access token with **Create versions** (`VERSION_CREATE`) scope. The token owner must also own this project or be an accepted team/organization member with permission to upload versions. A token that publishes another mod may not have permission for this project.

Update the GitHub repository Actions secret `MODRINTH_TOKEN` with the correct token. Check organization-secret access if the secret is shared from an organization. An expired/revoked token must be replaced. Do not paste tokens into issues, workflow logs, or chat. GitHub's `GITHUB_TOKEN` cannot authenticate to Modrinth.

The workflow checks that the secret exists before building. A nonempty secret alone does not prove its scope or project permissions; those must be checked in Modrinth. Then manually rerun Publish from the branch containing this workflow. The uploaded version number is exactly `mod_version`, with no additional Minecraft or loader suffix.
