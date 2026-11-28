"""Draw the project's pixel emblem, typeset banner, and screenshot comparison."""
import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
BRAND = ROOT / "docs" / "branding"
MEDIA = ROOT / "docs" / "media"
CHARCOAL = "#1A1A1A"
IVORY = "#F2F2F2"
TEAL = "#68CDD1"


def emblem():
    image = Image.new("RGB", (32, 32), CHARCOAL)
    draw = ImageDraw.Draw(image)
    draw.polygon([(1, 3), (3, 1), (28, 1), (30, 3), (30, 28), (28, 30),
                  (3, 30), (1, 28)], fill="#243637")
    draw.polygon([(5, 6), (26, 6), (26, 21), (16, 21), (9, 27), (9, 21),
                  (5, 21)], fill=IVORY)
    draw.rectangle((8, 10, 11, 13), fill="#24545A")
    draw.rectangle((14, 10, 23, 12), fill=CHARCOAL)
    draw.rectangle((14, 15, 21, 17), fill="#24545A")
    draw.rectangle((23, 23, 26, 26), fill=TEAL)
    return image


def svg_for(image):
    lines = ['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" shape-rendering="crispEdges">']
    for y in range(32):
        start = 0
        while start < 32:
            color = image.getpixel((start, y))
            end = start + 1
            while end < 32 and image.getpixel((end, y)) == color:
                end += 1
            hex_color = "#{:02x}{:02x}{:02x}".format(*color)
            lines.append(f'  <rect x="{start}" y="{y}" width="{end-start}" height="1" fill="{hex_color}"/>')
            start = end
    return "\n".join(lines + ["</svg>", ""])


def banner(mark, regular, bold):
    image = Image.new("RGB", (1600, 480), CHARCOAL)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, 1599, 7), fill=TEAL)
    draw.rectangle((0, 471, 1599, 479), fill="#24545A")
    for i, size in enumerate((8, 12, 16, 20, 24)):
        x = 1390 + i * 29
        y = 345 - i * 21
        draw.rectangle((x, y, x + size, y + size), fill="#294142")
    image.paste(mark.resize((288, 288), Image.Resampling.NEAREST), (108, 96))
    draw.rectangle((446, 106, 449, 374), fill="#3B6263")
    draw.text((497, 151), "BARE MINIMUM STUDIOS", font=regular(27), fill=TEAL)
    draw.text((491, 194), "Roleplay Status", font=bold(111), fill=IVORY)
    return image


def comparison(regular, bold):
    rows = [
        ("Standard indicators", "Colorblind mode off · status labels off", "tab-standard-original.png"),
        ("Out of Character", "Colorblind mode + labels · outline square", "tab-colorblind-ooc-original.png"),
        ("Open to RP", "Colorblind mode + labels · plus shape", "tab-colorblind-open-rp-original.png"),
    ]
    captures = [Image.open(MEDIA / filename).convert("RGB") for _, _, filename in rows]
    width = max(capture.width for capture in captures) + 84
    height = 115 + sum(capture.height + 108 for capture in captures)
    image = Image.new("RGB", (width, height), CHARCOAL)
    draw = ImageDraw.Draw(image)
    draw.text((28, 23), "Tab indicators", font=bold(36), fill=IVORY)
    draw.text((30, 73), "Minecraft screenshots", font=regular(20), fill=TEAL)
    top = 111
    for (title, subtitle, _), capture in zip(rows, captures):
        bottom = top + capture.height + 90
        draw.rounded_rectangle((18, top, width - 18, bottom), radius=8, fill="#242B2C")
        draw.text((40, top + 9), title, font=bold(25), fill=IVORY)
        draw.text((40, top + 42), subtitle, font=regular(19), fill="#BCD0D0")
        image.paste(capture, ((width - capture.width) // 2, top + 70))
        top = bottom + 18
    return image


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--font-dir", type=Path, default=Path("C:/Windows/Fonts"))
    args = parser.parse_args()
    def regular(size):
        return ImageFont.truetype(str(args.font_dir / "segoeui.ttf"), size)
    def bold(size):
        return ImageFont.truetype(str(args.font_dir / "segoeuib.ttf"), size)
    mark = emblem()
    (BRAND / "roleplay-status.svg").write_text(svg_for(mark), encoding="utf-8")
    mark.resize((512, 512), Image.Resampling.NEAREST).save(BRAND / "roleplay-status.png")
    mark.resize((128, 128), Image.Resampling.NEAREST).save(
        ROOT / "src/main/resources/assets/roleplay_status/icon.png"
    )
    banner(mark, regular, bold).save(BRAND / "roleplay-status-banner.png")
    comparison(regular, bold).save(MEDIA / "tab-accessibility-comparison.png")


if __name__ == "__main__":
    main()
