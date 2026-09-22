# SF_SlimeHUD 2.0.2

A modernized SlimeHUD fork for **Slimefun Legacy** and Minecraft **1.21.11+** on Paper-family servers.

## What changed in 2.0.2

- Keeps SF_SlimeHUD's native `PlayerWAILA` as the single source of truth for HUD lifecycle.
- Adds an optional **What Is That? (WIT)** bridge through WIT's public block-handler API.
- When WIT is installed and active for a player, WIT owns the visible BossBar/ActionBar while SF_SlimeHUD supplies Slimefun machine information.
- If WIT is absent, hidden, disabled for the player/world, or cannot be linked, SF_SlimeHUD automatically falls back to its native HUD.
- Exposes safe per-player range and vanilla-block overrides for JustEnoughGuide instead of requiring JEG to replace or pause SlimeHUD.
- `/slimehud status` reports whether WIT currently owns the display.
- `/slimehud reload` refreshes the optional WIT bridge.
- Keeps the 2.0.x standalone architecture: no InfinityLib runtime dependency.

## Existing 2.0 features

- Java 21 bytecode with compatibility builds on Java 21 and Java 25.
- Folia-aware player polling through Paper's entity scheduler.
- Automatic detection of placed Slimefun/Slimefun Legacy blocks through Slimefun block data.
- Automatic vanilla block names without maintaining a hard-coded material list.
- Dropped-item targeting for vanilla stacks and registered Slimefun items.
- WIT-inspired vanilla details for redstone, crops, beehives, furnaces, brewing stands, containers, beacons, spawners, note blocks, farmland, tool type, and more.
- Slimefun machine progress, stored energy, cargo channel/network, and energy network information.
- Per-player persistent BossBar or ActionBar display choice.
- PlaceholderAPI support.

## What Is That? integration

WIT is **optional**. You can run:

- SF_SlimeHUD by itself for its native Slimefun + vanilla HUD.
- WIT by itself for WIT's normal block/entity information.
- Both together. WIT renders the HUD and SF_SlimeHUD contributes Slimefun block details, avoiding duplicate BossBars/ActionBars.

The bridge is enabled by default:

```yaml
integrations:
  what-is-that:
    enabled: true
```

Disable it if you intentionally want the two plugins to render independently.

## JustEnoughGuide integration

JEG 2.1.65+ should use SF_SlimeHUD's native HUD adapter. JEG may supply per-player display range, vanilla-block visibility, and BossBar/ActionBar preference without creating a second HUD task or pausing `PlayerWAILA`.

Older JEG builds that replace/pause SlimeHUD should be updated.

## Player commands

- `/slimehud toggle`
- `/slimehud display bossbar`
- `/slimehud display actionbar`
- `/slimehud status`

Shortcuts: `/slimehud bossbar` and `/slimehud actionbar`.

Admin: `/slimehud reload`

## Output

The Maven build produces exactly:

`SF_SlimeHUD2.0.2.jar`

## Compatibility target

- Minecraft 1.21.11+
- Paper
- Purpur
- Leaf
- Folia
- Slimefun Legacy
- Optional What Is That? integration
- Optional SF_JustEnoughGuide 2.1.65+ native adapter

The plugin intentionally uses public Bukkit/Paper and Slimefun APIs for generic block and dropped-item discovery. Newly registered Slimefun addon blocks/items and newly introduced vanilla materials therefore do not need to be added to a SlimeHUD item list.

## Compatibility validation

`SF_SlimeHUD2.0.2.jar` is runtime-smoked with the real **Slimefun Legacy 4.1.59** release. The smoke test requires the server to reach `Done`, Slimefun to enable, SlimeHUD to enable, no SlimeHUD runtime error during the smoke window, and a clean shutdown.

Paper 26.3 remains a **candidate/advisory runtime target** until the full Slimefun Legacy stack graduates 26.3 from candidate validation. The addon is also compiled directly against the current Paper 26.3 API line so API breakage is caught separately from full-stack runtime readiness.
