# Farmhand

Minecraft **26.1.2**, NeoForge **26.1.2.108**, Java **25**.

- **Harvest Compass** — redesigned Result Sentinel, with a quiet held-item harvest HUD. Use to refresh; passive scans every five seconds. No chat/action-bar spam or sounds.
- **Growth Lamp** — detailed copper/jade crop lamp with animated lenses and growth particles.
- **Chicken Coop** — visibly attracts eggs/feathers into nine real slots. Right-click to open its inventory and toggle egg hatching per coop. Eggs hatch after five seconds by default, without a random-chance roll. Collection continues with hatching off. Clear space beside or above the coop is required.
- **Pasture Feeder** — nine slots for mixed breeding foods; right-click to manage inventory and toggle feeding. Feeds compatible adult pairs, not animals on breeding cooldown.
- **Planter's Satchel** — redesigned Seed Pouch with nine persistent seed slots. Use in the air or sneak-use to open storage; use on farmland to plant. Supports wheat, beetroot, carrots, potatoes, melon and pumpkin.

XP Siphon has been removed. Existing item IDs for the renamed compass, satchel and feeder are retained. Legacy coop/feeder contents migrate to real inventories.
Install the mod on **both client and server**. GUI progress/state and inventory transfers are server-authoritative.
Editable source models are in `art/blockbench/`.

## Build
```bash
./gradlew build            # compile + inventory/persistence tests + model checks + jar
./gradlew runClient        # launch the dev client
./gradlew buildAndCollect  # copy jar into builds/26.1.2-neoforge/
```
