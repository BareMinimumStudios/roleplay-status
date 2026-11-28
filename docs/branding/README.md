# Roleplay Status branding

The speech-bubble emblem is drawn on a 32 × 32 grid and enlarged with nearest-neighbor scaling. The banner pairs it with a typeset wordmark in charcoal, off-white, and the dice HUD’s teal.

- `roleplay-status.png` is the 512 × 512 storefront logo.
- `roleplay-status-banner.png` is the 1600 × 480 header for the README and storefront descriptions.
- `roleplay-status.svg` is the vector export of the emblem.
- `src/main/resources/assets/roleplay_status/icon.png` is the 128 × 128 icon bundled in both loader builds.

Run `python scripts/draw_branding.py` with Pillow and Segoe UI fonts to regenerate the assets. Use `--font-dir` to specify the directory containing `segoeui.ttf` and `segoeuib.ttf`. The script draws the branding from primitive shapes and text. It also lays out the three unedited Minecraft screenshots for the accessibility comparison.
