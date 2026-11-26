# Changelog

## 0.2.0 — 2026-11-26

- **Minecraft 26.2** — SnkMC moved to 26.2, the mod follows. This version requires Minecraft 26.2 and Fabric API 0.159.0 or newer; stay on 0.1.3 for 26.1.2. No feature change.
- **Works alongside MinePiece Essentials** — both mods can now stay installed together: this one no longer wakes up on MinePiece (it used to recognise MinePiece island names), so each mod only shows up on its own server.

## 0.1.3 — 2026-10-22

- **AH price colors** — new "AH price colors..." screen in `K`: pick any color for good deals, fair prices and overpriced listings (palette or hex), with a live preview; one-click "swap yellow / green" for those who want good deals in green. Also editable in `config/snkmc-essentials/config.json` (`ahColorCheap`, `ahColorFair`, `ahColorExpensive`).

## 0.1.2 — 2026-10-18

- **Rarity filter** — bar on the right of any server menu (auction house included): click a rarity to dim every other one. Rarity emblems on items can be switched on in `K`.

## 0.1.1 — 2026-10-18

- New icon.

## 0.1.0 — 2026-10-18

First release, for Minecraft 26.1.2. Derived from MinePiece Essentials 1.9.0.

- **Parchment reader** — quest parchments in your inventory shown as a HUD (objective, progress, expiry).
- **Weapons panel** — level, XP and ascension marker for your upgradable blades.
- **Job HUD** — level, XP and `%` of the current job while harvesting.
- **Auction house prices** — price per unit and colour band vs the average price.
- HUD editor (`K`), in-game help (`H`), update notifier.
- Only active on SnkMC (`play.snkmc.net`). Sends nothing to the server.
