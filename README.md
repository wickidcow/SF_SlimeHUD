# SF_SlimeHUD 2.0.1

A modernized SlimeHUD fork for **Slimefun Legacy** and Minecraft **1.21.11+** on Paper, Purpur, Leaf, and Folia.

## What changed in 2.0.1

- Removed the old InfinityLib bootstrap/runtime dependency.
- Java 21 bytecode with CI builds on Java 21 and Java 25.
- Folia-aware player polling through Paper's entity scheduler.
- Automatic detection of every placed Slimefun/Slimefun Legacy item through Slimefun's registered block data.
- Automatic vanilla block names without maintaining a hard-coded material list.
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

The plugin intentionally uses public Bukkit/Paper and Slimefun APIs for generic block discovery so newly registered Slimefun addon blocks and newly introduced vanilla materials do not require a SlimeHUD item list update.
