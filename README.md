# Farmhand

Minecraft **26.1.2**, NeoForge **26.1.2.108**, Java **25**.

- **Growpost** — place on farmland or a solid surface, then right-click for its configuration GUI. Name your field, set a 1–16 block scan radius and 5–100% notification threshold, and individually toggle first-harvest, threshold and fully-grown alerts. Compact labeled chat messages go only to the owner, once per milestone until readiness falls below it. Outputs readiness as redstone/comparator strength 0–15. Optional trample protection and Growth Lamp interlock are saved per post. Non-owners can view but cannot edit.
- **Growth Lamp** — animated emissive jade core while on, dim core while off. Right-click to toggle manual power. A covering Growpost's interlock pauses growth and extinguishes the lamp when its field is fully grown, then resumes after harvest unless manually switched off.
- **Chicken Coop** — visibly attracts eggs/feathers into nine real slots. Right-click to open its inventory and toggle egg hatching per coop. Eggs hatch after five seconds by default, without a random-chance roll. Collection continues with hatching off. Clear space beside or above the coop is required.
- **Pasture Feeder** — nine slots for mixed breeding foods; right-click to manage inventory and toggle feeding. Feeds compatible adult pairs, not animals on breeding cooldown. Real inventory items are displayed inside the trough, just as the coop displays its stored eggs/feathers.

### Animal attraction and redstone
- Breed-ready adult animals walk toward a nearby active feeder holding their matching food. Babies,
  animals on cooldown, leashed animals and animals already in love are not attracted.
- Feeding now requires both compatible adults within three blocks of the trough, with enough food for the pair.
  Navigation does not teleport animals through fences; keep an accessible route. Ordinary panic, mating and
  higher-priority player food-following goals take precedence.
- **Power either the feeder or coop to pause it; remove power to resume.** Feeder attraction and feeding stop;
  coop collection and incubation stop. Redstone preserves inventory, remaining incubation time and GUI switches.
  The coop's Hatch OFF button still only disables hatching, not collection, when redstone is absent.
- GUI status shows the redstone pause. Inventory access/hopper transfers remain available while powered.
- Build checks are automated; live animal attraction and redstone circuitry still need in-world testing.
- **Planter's Satchel** — redesigned Seed Pouch with nine persistent seed slots. Use in the air or sneak-use to open storage; use on farmland to plant. Supports wheat, beetroot, carrots, potatoes, melon and pumpkin. Planting immediately wakes a covering Growpost, so an interlock-paused Growth Lamp relights right away instead of waiting for the next scan.

XP Siphon and the handheld Harvest Compass have been removed; old compass items do not migrate to Growposts. Satchel and feeder item IDs are retained. Legacy coop/feeder contents migrate to real inventories. Back up existing worlds before upgrading.
Install the mod on **both client and server**. GUI progress/state and inventory transfers are server-authoritative.
Editable source models are in `art/blockbench/`.

Items live in a dedicated **Farmhand** creative tab (Growth Lamp, Coop, Feeder, Growpost, Planter's Satchel).

Growpost scans ordinary farmland crops every second. An empty or partially unloaded field does not report fully grown. Overlapping posts can each notify their own owner. Ripe crops already remain ripe in vanilla; trample protection protects their farmland, not against harvesting, explosions or every kind of mob interaction. Tests cover milestone deduplication, saved settings, lamp-power logic and inventory render synchronization; GUI appearance and live gameplay loops still require in-game verification.

## Build
```bash
./gradlew build            # compile + inventory/persistence tests + model checks + jar
./gradlew runClient        # launch the dev client
./gradlew buildAndCollect  # copy jar into builds/26.1.2-neoforge/
```
