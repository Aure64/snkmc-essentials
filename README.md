# SnkMC Essentials

A client-side Fabric mod for the SnkMC server. It reads what the game already shows you (tooltips, server menus, action bar) and turns it into HUDs and tooltips. No macros, no auto-farming, nothing is clicked or sent for you. The mod only activates on SnkMC.

- **Parchment reader** — scans quest parchments in your inventory and shows their objectives, progress and expiry in a HUD.
- **Weapons panel** — a HUD listing your upgradable blades with their level, XP to the next level, and a marker when an ascension is available.
- **Job HUD** — level, XP and `%` of your current job while you harvest.
- **Auction house prices** — price per unit on stacks, and a colour band comparing the listing to the average price (yellow = cheap, green = fair, red = expensive).
- **In-game help** — a guide popup shown on first join and reopenable with `H`.
- **Update notifier** — tells you in chat when a newer version is available on GitHub.

All HUDs are draggable. Press `K` to enter edit mode, drag with the mouse, scroll to resize, press `Esc` to save. The interface follows your Minecraft language (French or English).

## Requirements

- Minecraft 26.1.2
- Fabric Loader ≥ 0.19.5
- Fabric API
- Java 25

## Install

Download the jar from the [GitHub releases](https://github.com/Aure64/snkmc-essentials/releases/latest) and drop it into `.minecraft/mods/`, along with Fabric API. Keep only one version of the mod in the folder.

## Build

Requires JDK 25 (`JAVA_HOME=/path/to/jdk-25`).

```bash
./gradlew build
```

The jar lands in `build/libs/snkmc-essentials-<version>.jar`.

## Keybinds

| Default | Action |
|--|--|
| `K` | Open HUD edit screen |
| `H` | Open the in-game help guide |

## Configuration

Config files live at `.minecraft/config/snkmc-essentials/`:

- `config.json` — feature switches
- `layouts.json` — HUD positions and sizes
- `telemetry.json` — your random anonymous ID (see below)

## What the mod sends

**To the SnkMC server** — nothing. Every HUD is filled from what the server already sends to your client.

**Anonymous statistics (off by default)**

The mod can send a few anonymous statistics so the author knows how many players use it. It is switched off in this version.

- Sent, when enabled: a random ID generated on first launch, mod / Minecraft / Fabric Loader / Java versions, OS, client language, and the names of the features used.
- Never sent: your username, Minecraft UUID, IP address, chat, coordinates, inventory or progression.
- Toggle: `K` → "Anonymous stats". Hosted by PostHog, EU region.

**Update check**

On launch the mod asks GitHub whether a newer release exists and tells you in chat if so.

## Credits

Derived from [MinePiece Essentials](https://github.com/Aure64/minepiece-essentials), by the same author.

## License

All rights reserved.
