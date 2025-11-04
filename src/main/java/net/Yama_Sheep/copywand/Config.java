package net.Yama_Sheep.copywand;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {

    public static final ModConfigSpec.BooleanValue FLOWERS_COPY;
    public static final ModConfigSpec.BooleanValue GRASS_COPY;
    public static final ModConfigSpec.BooleanValue SNOW_COPY;
    public static final ModConfigSpec.BooleanValue LIQUID_COPY;
    public static final ModConfigSpec.BooleanValue PAWDERSNOW_COPY;
    public static final ModConfigSpec.BooleanValue INCLUDE_BLOCK_ENTITIES;
    public static final ModConfigSpec.BooleanValue CONSUME_ITEMS;
    public static final ModConfigSpec.BooleanValue REPLACE_FLOWERS;
    public static final ModConfigSpec.BooleanValue REPLACE_LIQUID;
    public static final ModConfigSpec.BooleanValue OVERRIDE_BLOCKS;
    public static final ModConfigSpec.BooleanValue DOUBLE_CLICK_PASTE;
    public static final ModConfigSpec.BooleanValue DOUBLE_CLICK_PASTE_CHAT;
    public static final ModConfigSpec.IntValue COPY_LIMIT;
    public static final ModConfigSpec.BooleanValue USE_SHULKERBOX;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();



    // ───── Copy カテゴリ ─────
    static {
        BUILDER.push("Copy");

        COPY_LIMIT = BUILDER
                .comment("Maximum number of blocks that can be copied.")
                .defineInRange("copyBlockLimit", 1000, 2, 2304);

        FLOWERS_COPY = BUILDER
                .comment("Whether to copy flowers. Default: false")
                .define("flowersCopy", false);

        GRASS_COPY = BUILDER
                .comment("Whether to copy grass. Default: false")
                .define("grassCopy", false);

        SNOW_COPY = BUILDER
                .comment("Whether to copy snow. Default: false")
                .define("snowCopy", false);

        LIQUID_COPY = BUILDER
                .comment("Whether to copy liquid. Default: false")

                .define("includeLiquid", false);
        PAWDERSNOW_COPY = BUILDER
                .comment("Whether to copy powder snow. Default: false")
                .define("includepowdersnow", false);

        INCLUDE_BLOCK_ENTITIES = BUILDER
                .comment("Include block entities when copying (chests, signs, etc.). Default: true")
                .define("includeBlockEntities", true);

        BUILDER.pop();
    }

    // ───── Paste カテゴリ ─────
    static {
        BUILDER.push("Paste");

        DOUBLE_CLICK_PASTE = BUILDER
                .comment("Allow pasting by clicking the same block twice. Default: false")
                .define("doubleClickPaste", true);


        REPLACE_FLOWERS = BUILDER
                .comment("Replace flowers when pasting. Default: true")
                .define("replaceFlowers", true);

        REPLACE_LIQUID= BUILDER
                .comment("Replace liquid when pasting. Default: true")
                .define("replaceLiquid", true);

        CONSUME_ITEMS = BUILDER
                .comment("Consume items from the player's inventory when pasting blocks (Survival mode only). Default: true")
                .define("consumeItemsOnPaste", true);

        OVERRIDE_BLOCKS = BUILDER
                .comment("Replace \"ALL\" blocks when pasting. Default: false")
                .define("overrideBlocks", false);

        BUILDER.pop();


    }
    static {

        BUILDER.push("Other");

        USE_SHULKERBOX = BUILDER
                .comment("Enable use of items stored inside shulker boxes in inventory. Default: true")
                .define("useshulkerbox", true);

        DOUBLE_CLICK_PASTE_CHAT = BUILDER
                .comment("Show double click paste tips. Default: true")
                .define("doubleclickchat", true);

        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
