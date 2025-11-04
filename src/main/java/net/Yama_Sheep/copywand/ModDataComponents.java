package net.Yama_Sheep.copywand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class  ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,"copywand");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> IS_CAN_COPY=
            DATA_COMPONENT_TYPES.registerComponentType("iscancopy" ,builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> IS_CAN_PASTE=
            DATA_COMPONENT_TYPES.registerComponentType("iscanpaste" ,builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> BLOCKPOS_PASTE=
            DATA_COMPONENT_TYPES.registerComponentType("blockpospaste" ,builder -> builder.persistent(BlockPos.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> WAND_MODE=
            DATA_COMPONENT_TYPES.registerComponentType("wandmode" ,builder -> builder.persistent(ExtraCodecs.NON_EMPTY_STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> BLOCKPOS_START=
            DATA_COMPONENT_TYPES.registerComponentType("blockposstart" ,builder -> builder.persistent(BlockPos.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> BLOCKPOS_END=
            DATA_COMPONENT_TYPES.registerComponentType("blockposend" ,builder -> builder.persistent(BlockPos.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<BlockPos>>> COPYBLOCKPOSES_INFO =
            DATA_COMPONENT_TYPES.registerComponentType("copyblockposesinfo", builder ->
                    builder.persistent(BlockPos.CODEC.listOf())
            );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<BlockState>>> COPYBLOCKSTATES_INFO =
            DATA_COMPONENT_TYPES.registerComponentType("copyblockstatesinfo", builder ->
                    builder.persistent(BlockState.CODEC.listOf())
            );


    public static void register(IEventBus eventBus){ DATA_COMPONENT_TYPES.register(eventBus); }
}

