package me.ez.farmhand;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server/common configuration for Farmhand. */
public class Config {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;

    public static final ModConfigSpec.BooleanValue SENTINEL_ENABLED;
    public static final ModConfigSpec.IntValue SENTINEL_RADIUS;

    public static final ModConfigSpec.BooleanValue LAMP_ENABLED;
    public static final ModConfigSpec.IntValue LAMP_RADIUS;
    public static final ModConfigSpec.IntValue LAMP_CHANCE;
    public static final ModConfigSpec.IntValue LAMP_INTERVAL;

    public static final ModConfigSpec.BooleanValue COOP_ENABLED;
    public static final ModConfigSpec.IntValue COOP_RADIUS;
    public static final ModConfigSpec.BooleanValue COOP_INCUBATE;
    public static final ModConfigSpec.IntValue COOP_INCUBATE_CHANCE;

    public static final ModConfigSpec.BooleanValue FEEDER_ENABLED;
    public static final ModConfigSpec.IntValue FEEDER_RADIUS;

    public static final ModConfigSpec.BooleanValue POUCH_ENABLED;
    public static final ModConfigSpec.IntValue POUCH_WIDTH;
    public static final ModConfigSpec.IntValue POUCH_DEPTH;

    public static final ModConfigSpec.BooleanValue SIPHON_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Master switch for Farmhand").translation("farmhand.configuration.general").push("general");
        ENABLED = builder.comment("Enable Farmhand").translation("farmhand.configuration.general.enabled")
                .define("enabled", true);
        builder.pop();

        builder.comment("Result Sentinel crop scanner").translation("farmhand.configuration.sentinel").push("sentinel");
        SENTINEL_ENABLED = builder.comment("Enable the Result Sentinel").translation("farmhand.configuration.sentinel.enabled")
                .define("enabled", true);
        SENTINEL_RADIUS = builder.comment("Scan radius in blocks").translation("farmhand.configuration.sentinel.radius")
                .defineInRange("radius", 12, 1, 48);
        builder.pop();

        builder.comment("Growth Lamp").translation("farmhand.configuration.growth_lamp").push("growth_lamp");
        LAMP_ENABLED = builder.comment("Enable the Growth Lamp").translation("farmhand.configuration.growth_lamp.enabled")
                .define("enabled", true);
        LAMP_RADIUS = builder.comment("Radius in blocks the lamp can grow").translation("farmhand.configuration.growth_lamp.radius")
                .defineInRange("radius", 4, 1, 12);
        LAMP_CHANCE = builder.comment("Percent chance per sampled crop to grow each cycle")
                .translation("farmhand.configuration.growth_lamp.chance").defineInRange("chance", 35, 1, 100);
        LAMP_INTERVAL = builder.comment("Ticks between growth cycles").translation("farmhand.configuration.growth_lamp.interval")
                .defineInRange("interval", 40, 5, 400);
        builder.pop();

        builder.comment("Chicken Coop").translation("farmhand.configuration.chicken_coop").push("chicken_coop");
        COOP_ENABLED = builder.comment("Enable the Chicken Coop").translation("farmhand.configuration.chicken_coop.enabled")
                .define("enabled", true);
        COOP_RADIUS = builder.comment("Radius in blocks to collect eggs and feathers")
                .translation("farmhand.configuration.chicken_coop.radius").defineInRange("radius", 6, 1, 16);
        COOP_INCUBATE = builder.comment("Chance to hatch a stored egg into a chick")
                .translation("farmhand.configuration.chicken_coop.incubate").define("incubate", true);
        COOP_INCUBATE_CHANCE = builder.comment("Percent chance per cycle to hatch one egg")
                .translation("farmhand.configuration.chicken_coop.incubateChance").defineInRange("incubateChance", 8, 1, 100);
        builder.pop();

        builder.comment("Animal Feeder").translation("farmhand.configuration.animal_feeder").push("animal_feeder");
        FEEDER_ENABLED = builder.comment("Enable the Animal Feeder").translation("farmhand.configuration.animal_feeder.enabled")
                .define("enabled", true);
        FEEDER_RADIUS = builder.comment("Radius in blocks to feed animals").translation("farmhand.configuration.animal_feeder.radius")
                .defineInRange("radius", 8, 1, 24);
        builder.pop();

        builder.comment("Seed Pouch").translation("farmhand.configuration.seed_pouch").push("seed_pouch");
        POUCH_ENABLED = builder.comment("Enable the Seed Pouch").translation("farmhand.configuration.seed_pouch.enabled")
                .define("enabled", true);
        POUCH_WIDTH = builder.comment("Half-width of the planting plane").translation("farmhand.configuration.seed_pouch.width")
                .defineInRange("width", 1, 0, 4);
        POUCH_DEPTH = builder.comment("Half-depth of the planting plane").translation("farmhand.configuration.seed_pouch.depth")
                .defineInRange("depth", 1, 0, 4);
        builder.pop();

        builder.comment("Furnace XP Siphon").translation("farmhand.configuration.xp_siphon").push("xp_siphon");
        SIPHON_ENABLED = builder.comment("Enable the XP Siphon").translation("farmhand.configuration.xp_siphon.enabled")
                .define("enabled", true);
        builder.pop();

        SPEC = builder.build();
    }
}
