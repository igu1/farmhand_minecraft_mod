# Farmhand — visual and inventory overhaul

- Removed the separate creative tab; items use vanilla Functional Blocks and Tools & Utilities.
- Growth Lamp right-click power, visible interlock shutoff, and active-only emissive jade texture.
- Coop and feeder display synchronized real inventory contents instead of decorative eggs/feed.
- Planter's Satchel planting now wakes a covering Growpost, so an interlock-paused Growth Lamp relights immediately after replanting.

- Fixed overlapping model faces; added detailed editable Blockbench models.
- Added real nine-slot coop and feeder inventories, animated GUIs and persistent per-machine toggles.
- Eggs and feathers now move toward the coop before collection.
- Replaced random incubation chance with a deterministic five-second cycle, safe alternate chick spawn positions and meaningful status text. Removed decorative progress bars.
- Feeder uses mixed foods and respects adult breeding cooldowns; feeds pairs atomically.
- Redesigned Seed Pouch as Planter's Satchel, with persistent seed storage and farmland planting.
- Replaced the handheld scanner with Growpost: per-post configuration GUI, field labels, owner-only milestone chat alerts, readiness redstone output, trample protection and Growth Lamp interlock. Back up worlds: old compass items do not migrate.
- Removed pointing tooltips and XP Siphon.
- Retained renamed item/block IDs and migrated original machine contents.
- Inventory/component persistence and geometry/UV regression tests included. Live gameplay verification is still pending.
