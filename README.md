# SF_SlimeHUD 2.0.1

A modern WAILA-style HUD for **Slimefun Legacy** and vanilla Minecraft blocks/entities.

This fork keeps the useful Slimefun-specific HUD information from SlimeHUD, modernizes it for current Paper-family servers, and adds general block/entity information inspired by the feature set of WIT / WhatIsThat.

## Server targets

- Minecraft 1.21.11+
- Paper
- Purpur
- Leaf
- Folia
- Slimefun Legacy 4.1.50+
- Java 21 bytecode; CI builds on Java 21 and Java 25

The plugin declares Folia support and uses player/entity scheduling rather than a single global repeating Bukkit task for HUD updates. The CI artifact is also forced to the exact release filename `SF_SlimeHUD2.0.1.jar`.

## Display modes

Every player can choose their own display mode:

- BossBar
- ActionBar

Commands:

```text
/slimehud toggle
/slimehud display bossbar
/slimehud display actionbar
/slimehud status
```

Aliases: `/sfhud`, `/sfh`

Player preferences are stored on the player using PersistentDataContainer, avoiding a shared player-data YAML write on every toggle.

## Slimefun Legacy information

All registered Slimefun items are recognized generically by their Slimefun ID/name, including core items, built-in Slimefun Legacy addons, and external addon items.

Specialized information is shown where the Slimefun API exposes it:

- machine operation progress
- generator output
- stored energy / capacity using the modern long-capacity energy API
- energy network size
- cargo network size
- cargo channel

The HUD lookup is cache-only, so simply looking around does not intentionally trigger database reads for unloaded Slimefun block records.

## Vanilla information

Vanilla support is Material-based rather than a hardcoded block list, so newly added vanilla materials automatically receive basic identification.

Optional details include:

- container slot/item fill
- redstone power
- crop/age progress
- levelled block state
- powered/lit/open state
- beehive population
- spawner mob type
- preferred-tool hint
- hardness / unbreakable state
- entity health
- villager profession and level
- dropped item count

All detail groups are individually configurable in `config.yml`.

## PlaceholderAPI

When PlaceholderAPI is installed:

```text
%slimehud_enabled%
%slimehud_toggle%
%slimehud_display%
%slimehud_hud%
%slimehud_hud_block%
%slimehud_hud_block_info%
```

## Build

```bash
mvn clean package
```

The release JAR is always named:

```text
SF_SlimeHUD2.0.1.jar
```

## Credits

- Original SlimeHUD by SchnTgaiSpock and contributors
- WIT / WhatIsThat by darksoulq for modern HUD feature inspiration and Folia-oriented design ideas
- Slimefun Legacy project and contributors

This repository retains its existing license.
