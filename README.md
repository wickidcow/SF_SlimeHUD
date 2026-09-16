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

## Compatibility validation

`SF_SlimeHUD2.0.1.jar` is built once and runtime-smoked with the real **Slimefun Legacy 4.1.50** release. The smoke test requires the server to reach `Done`, Slimefun to enable, SlimeHUD to enable, no SlimeHUD runtime error during the smoke window, and a clean shutdown.

| Minecraft | Paper | Purpur | Leaf | Folia |
| --- | --- | --- | --- | --- |
| 1.21.11 | Runtime PASS | Runtime PASS | Runtime PASS | Runtime PASS |
| 26.1.2 | Runtime PASS | Runtime PASS | Runtime PASS | Runtime PASS |
| 26.2 | Runtime PASS | Runtime PASS | Runtime PASS | Runtime PASS |
| 26.3 pre-release | API compile PASS; runtime blocked by Slimefun Legacy 4.1.50 version gate | Not claimed | Not claimed | Not claimed |

The 26.3 runtime result is intentionally classified separately. Paper 26.3 loads the server and discovers both plugins, but Slimefun Legacy 4.1.50 currently rejects Minecraft 26.3 before SlimeHUD can be runtime-validated. This is a Slimefun Legacy support-version gate, not an SF_SlimeHUD Paper-API compile failure. Once Slimefun Legacy allows 26.3, the advisory runtime smoke becomes the next compatibility gate automatically.
