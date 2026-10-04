# Farmhand

**Let the farm run itself.**

**Development build — version 1.0.0+26.1.2. Gameplay verification is still in progress.**

Five detailed farm machines and tools, with real inventories, labeled harvest alerts and portable seed storage.

## Features

### Growpost
Place a labeled field monitor on farmland or a solid surface. Right-click its configuration GUI to set the field label, radius and readiness threshold, and toggle owner-only chat milestones. Redstone/comparator output scales from 0 to 15 with readiness. Optional trample protection and Growth Lamp interlock are saved per post. Alerts fire once per milestone, not repeatedly while crops remain ripe. Replaces the handheld Harvest Compass.

### Growth Lamp
A glowing block that bone-meals nearby crops over time. Radius, chance and speed are configurable.

### Chicken Coop
Pulls nearby dropped eggs and feathers visibly toward its nine-slot inventory. Right-click to manage stored items and toggle egg hatching for this coop. Eggs hatch after five seconds by default, without a random-chance roll. Collection remains active when hatching is off. Leave clear space beside or above the coop.

### Pasture Feeder
Right-click to open its nine-slot mixed-food inventory and feeding toggle. Feeds compatible adult pairs and respects breeding cooldowns.

### Planter's Satchel
Nine persistent slots for crop seeds. Use in the air or sneak-use to open storage; use on farmland to plant an area. Falls back to seeds in your player inventory when its own storage is empty.

## Configuration

Machine radius, chance and interval settings are in `config/farmhand-common.toml`. The coop and feeder have per-machine GUI toggles. Growpost settings are configured in its own GUI; only the owner can edit them.

## Keybinds

No extra keybinds required. Right-click machines to open their inventories.

## Requirements

- **Minecraft 26.1.2**
- **NeoForge 26.1.2.108 or newer**
- **Java 25** (bundled with Minecraft 26.1)
- Install on both client and server: custom blocks, items and synchronized inventories require it.

## Installation

1. Install **NeoForge** for Minecraft 26.1.2.
2. Drop the `.jar` into your `mods/` folder.
3. Launch the game. That is it — no dependencies and no configuration required to start.

## Compatibility

- Works in single-player and on servers with Farmhand installed.
- Should be compatible with most other mods. Please report conflicts in the issue tracker.

## Links

- **Source code:** https://github.com/igu1/farmhand_minecraft_mod
- **Issue tracker:** https://github.com/igu1/farmhand_minecraft_mod/issues

## License

All rights reserved.

---

*Made by Love, Cheese By Ez.*
