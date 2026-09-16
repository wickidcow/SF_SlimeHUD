# SF_SlimeHUD 2.0.1

A modernized SlimeHUD fork for **Slimefun Legacy** and Minecraft **1.21.11+** on Paper, Purpur, Leaf, and Folia.

## What changed in 2.0.1

- Removed the old InfinityLib bootstrap/runtime dependency.
- Java 21 bytecode with compatibility builds on Java 21 and Java 25.
- Folia-aware player polling through Paper's entity scheduler.
- Automatic detection of every placed Slimefun/Slimefun Legacy block through Slimefun's registered block data.
- Automatic vanilla block names without maintaining a hard-coded material list.
- Dropped-item targeting for both vanilla stacks and registered Slimefun items.
- WIT-inspired vanilla details for redstone, crops, beehives, furnaces, brewing stands, containers, beacons, spawners, note blocks, farmland, tool type, and more.
- Preserves SlimeHUD machine progress, stored energy, cargo channel, cargo network, and energy network information.
- Per-player persistent BossBar or ActionBar display choice.
- PlaceholderAPI support.

## Player commands

- `/slimehud toggle`
- `/slimehud display bossbar`
- `/slimehud display actionbar`
- `/slimehud status`

Shortcuts: `/slimehud bossbar` and `/slimehud actionbar`.

Admin: `/slimehud reload`

## Output

The Maven build produces exactly:

`SF_SlimeHUD2.0.1.jar`

## Compatibility target

- Minecraft 1.21.11+
- Paper
- Purpur
- Leaf
- Folia
- Slimefun Legacy

The plugin intentionally uses public Bukkit/Paper and Slimefun APIs for generic block and dropped-item discovery. Newly registered Slimefun addon blocks/items and newly introduced vanilla materials therefore do not need to be added to a SlimeHUD item list.

## Compatibility testing

GitHub Actions validates compatibility in two layers:

1. **API compile matrix** — Paper 1.21.11 on Java 21 and Java 25, plus Paper 26.1.2, 26.2, and the current 26.3 pre-release API.
2. **Runtime smoke matrix** — boots the exact `SF_SlimeHUD2.0.1.jar` with **Slimefun Legacy 4.1.50** on Paper, Purpur, Leaf, and Folia across the supported Minecraft line.

Stable/current production targets are blocking checks. Historical experimental builds and the 26.3 pre-release runtime are advisory so upstream availability or alpha churn is visible without misrepresenting it as a production regression.
