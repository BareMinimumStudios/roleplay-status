"""Validate Keep a Changelog release notes and export the configured release."""

import argparse
from datetime import date
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
VERSION = r"\d+\.\d+\.\d+\+\d+\.\d+\.\d+"
RELEASE = re.compile(rf"(?m)^## \[({VERSION})\] - (\d{{4}}-\d{{2}}-\d{{2}}) *$")
SECTION = re.compile(r"(?m)^## ")
CATEGORIES = {"Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"}


def extract_release(text: str, version: str) -> str:
    headings = list(SECTION.finditer(text))
    if not headings or text[headings[0].start():].splitlines()[0] != "## [Unreleased]":
        raise ValueError("CHANGELOG.md must begin its release sections with ## [Unreleased].")

    releases = {}
    previous_date = None
    for index, heading in enumerate(headings[1:], start=1):
        heading_text = text[heading.start():].splitlines()[0]
        match = RELEASE.fullmatch(heading_text)
        if not match:
            raise ValueError(f"Invalid release heading: {heading_text}")
        key, released = match.groups()
        release_date = date.fromisoformat(released)
        if key in releases:
            raise ValueError(f"Duplicate release section: {key}")
        if previous_date is not None and release_date > previous_date:
            raise ValueError(f"Release dates must be newest first: {key}")
        previous_date = release_date

        start = heading.start() + len(heading_text)
        end = headings[index + 1].start() if index + 1 < len(headings) else len(text)
        body = text[start:end]
        body = re.split(r"(?m)^\[[^\]\n]+\]: ", body, maxsplit=1)[0].strip()
        categories = re.findall(r"(?m)^### (.+)$", body)
        if not categories or set(categories) - CATEGORIES or len(categories) != len(set(categories)):
            raise ValueError(f"Invalid or missing change categories in {key}.")
        for section in re.split(r"(?m)^### .+$", body)[1:]:
            if not re.search(r"(?m)^- \S", section):
                raise ValueError(f"Empty change category in {key}.")
        releases[key] = body
    if version not in releases:
        raise ValueError(f"Missing release notes for {version}.")
    if next(iter(releases)) != version:
        raise ValueError(f"The newest release section must match mod_version ({version}).")
    return releases[version]


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    output = parser.add_mutually_exclusive_group()
    output.add_argument("--check", action="store_true", help="Validate without printing notes.")
    output.add_argument("--output", type=Path, help="Write the notes to a UTF-8 Markdown file.")
    output.add_argument("--version", action="store_true", help="Print the validated release version.")
    args = parser.parse_args()

    try:
        properties = dict(line.split("=", 1) for line in (ROOT / "gradle.properties").read_text(
            encoding="utf-8"
        ).splitlines() if "=" in line and not line.lstrip().startswith("#"))
        version = properties["mod_version"].strip()
        minecraft = properties["minecraft_version"].strip()
        if not re.fullmatch(VERSION, version) or version.split("+")[1] != minecraft:
            raise ValueError("mod_version must use X.Y.Z+<minecraft_version>.")
        notes = extract_release((ROOT / "CHANGELOG.md").read_text(encoding="utf-8"), version) + "\n"
        if args.output:
            args.output.parent.mkdir(parents=True, exist_ok=True)
            args.output.write_text(notes, encoding="utf-8", newline="\n")
        elif args.version:
            print(version)
        elif not args.check:
            sys.stdout.reconfigure(encoding="utf-8")
            print(notes, end="")
    except (OSError, KeyError, ValueError) as exc:
        parser.exit(1, f"Release notes: {exc}\n")


if __name__ == "__main__":
    main()
