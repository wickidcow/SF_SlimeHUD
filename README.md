# SF_SlimeHUD 2.0.3

A modernized SlimeHUD fork for **Slimefun Legacy** and Minecraft **1.21.11+** on Paper-family servers.

## What changed in 2.0.3

- Adds a compact preferred-tool symbol beside **vanilla block names**.
- Tool symbols are shown only for vanilla blocks handled by the vanilla provider.
- Normal Slimefun blocks/items never receive a vanilla tool symbol.
- Vanilla **Spawner** blocks are treated specially and report **Pickaxe of Containment** rather than a normal pickaxe.
- The held-tool check recognizes the actual Slimefun `PICKAXE_OF_CONTAINMENT` item for spawners.
- Symbols are configurable in `config.yml`, so servers with custom fonts/resource packs can replace the defaults.
- When What Is That? owns the visible HUD, SF_SlimeHUD can now provide the same vanilla block information through the WIT bridge while still respecting WIT's block enabled/blacklist rules.

Default vanilla markers:

- Pickaxe: `⛏`
- Axe: `🪓`
- Shovel: `🪏`
- Hoe: `⚒`
- Pickaxe of Containment: `⛏`

Example native display:

`Stone [⛏] | Tool: Pickaxe`

`Spawner [⛏] | Spawner: Zombie | Tool: Pickaxe of Containment`

The symbols are controlled by:

```yaml
vanilla:
  show-tool-symbol: true
  tool-symbols:
    pickaxe: "⛏"
    axe: "🪓"
    shovel: "🪏"
    hoe: "⚒"
    containment: "⛏"
```

Set any symbol to an empty string to hide only that tool marker. Custom resource-pack font glyphs can also be used here.

## What Is That? integration

WIT remains optional. With both plugins installed, WIT owns the visible BossBar/ActionBar while SF_SlimeHUD supplies Slimefun data. By default, SF_SlimeHUD also supplies vanilla block details so tool markers remain consistent:

```yaml
integrations:
  what-is-that:
    enabled: true
    use-slimehud-vanilla: true
```

WIT still controls whether block display is enabled and whether a vanilla block is allowed by WIT's block whitelist/blacklist.

## Existing 2.0 features

- Java 21 bytecode with Java 21 and Java 25 compatibility builds.
- Paper, Purpur, Leaf, and Folia support targets.
- Native Slimefun machine progress, energy, cargo, and network information.
- Dropped-item targeting for vanilla and registered Slimefun items.
- Vanilla block details for redstone, crops, furnaces, brewing stands, containers, beacons, spawners, farmland, note blocks, and more.
- Persistent BossBar or ActionBar selection.
- Optional What Is That? bridge.
- Native JustEnoughGuide 2.1.65+ adapter support.
- PlaceholderAPI support.
- No InfinityLib runtime dependency.

## Player commands

- `/slimehud toggle`
- `/slimehud display bossbar`
- `/slimehud display actionbar`
- `/slimehud status`

Admin: `/slimehud reload`

## Output

The Maven build produces exactly:

`SF_SlimeHUD2.0.3.jar`

## Compatibility target

- Minecraft 1.21.11+
- Paper
- Purpur
- Leaf
- Folia
- Slimefun Legacy
- Optional What Is That? integration
- Optional SF_JustEnoughGuide 2.1.65+ native adapter
