# Farmhand

Minecraft **26.1.2**, NeoForge **26.1.2.108**, Java **25**.

- **Growpost** — place on farmland or a solid surface, then right-click for its configuration GUI. Name your field, set a 1–16 block scan radius and 5–100% notification threshold, and individually toggle first-harvest, threshold and fully-grown alerts. Compact labeled chat messages go only to the owner, once per milestone until readiness falls below it. Outputs readiness as redstone/comparator strength 0–15. Optional trample protection and Growth Lamp interlock are saved per post. Non-owners can view but cannot edit.
- **Growth Lamp** — animated emissive jade core while on, dim core while off. Right-click to toggle manual power. A covering Growpost's interlock pauses growth and extinguishes the lamp when its field is fully grown, then resumes after harvest unless manually switched off.
- **Chicken Coop** — visibly attracts eggs/feathers into nine real slots. Right-click to open its inventory and toggle egg hatching per coop. Eggs hatch after five seconds by default, without a random-chance roll. Collection continues with hatching off. Clear space beside or above the coop is required.
- **Pasture Feeder** — nine slots for mixed breeding foods; right-click to manage inventory and toggle feeding. Feeds compatible adult pairs, not animals on breeding cooldown. Real inventory items are displayed inside the trough, just as the coop displays its stored eggs/feathers.
- **Planter's Satchel** — redesigned Seed Pouch with nine persistent seed slots. Use in the air or sneak-use to open storage; use on farmland to plant. Supports wheat, beetroot, carrots, potatoes, melon and pumpkin.

XP Siphon and the handheld Harvest Compass have been removed; old compass items do not migrate to Growposts. Satchel and feeder item IDs are retained. Legacy coop/feeder contents migrate to real inventories. Back up existing worlds before upgrading.
Install the mod on **both client and server**. GUI progress/state and inventory transfers are server-authoritative.
Editable source models are in `art/blockbench/`.

Farmhand has no separate creative tab: machines are in Functional Blocks, and the satchel is in Tools & Utilities.

Growpost scans ordinary farmland crops every second. An empty or partially unloaded field does not report fully grown. Overlapping posts can each notify their own owner. Ripe crops already remain ripe in vanilla; trample protection protects their farmland, not against harvesting, explosions or every kind of mob interaction. Tests cover milestone deduplication, saved settings, lamp-power logic and inventory render synchronization; GUI appearance and live gameplay loops still require in-game verification.

## Build
```bash
./gradlew build            # compile + inventory/persistence tests + model checks + jar
./gradlew runClient        # launch the dev client
./gradlew buildAndCollect  # copy jar into builds/26.1.2-neoforge/
```
