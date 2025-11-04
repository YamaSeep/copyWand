package net.Yama_Sheep.copywand;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

public class ModItems {
    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("copywand");

    public static final DeferredItem<Item> COPYWANDITEM =
            ITEMS.registerItem("copywanditem", CopyWandItem::new, new Item.Properties().durability(2048).stacksTo(1));

    public static void register(IEventBus eventBus){ ITEMS.register(eventBus); }
}
