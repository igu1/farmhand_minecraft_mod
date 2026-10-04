# Farmhand — visual and inventory overhaul

- Fixed overlapping model faces; added detailed editable Blockbench models.
- Added real nine-slot coop and feeder inventories, animated GUIs and persistent per-machine toggles.
- Eggs and feathers now move toward the coop before collection.
- Replaced random incubation chance with a deterministic five-second cycle, safe alternate chick spawn positions and meaningful status text. Removed decorative progress bars.
- Feeder uses mixed foods and respects adult breeding cooldowns; feeds pairs atomically.
- Redesigned Seed Pouch as Planter's Satchel, with persistent seed storage and farmland planting.
- Redesigned Result Sentinel as Harvest Compass, with a quiet held-item HUD.
- Removed pointing tooltips and XP Siphon.
- Retained renamed item/block IDs and migrated original machine contents.
- Inventory/component persistence and geometry/UV regression tests included. Live gameplay verification is still pending.
