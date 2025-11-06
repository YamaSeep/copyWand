package net.Yama_Sheep.copywand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CopyWandItem extends Item {

    private static final int color = TextColor.fromRgb(0xB43434).getValue();

    public CopyWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {

        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (player == null) return InteractionResult.SUCCESS;

        if (stack.getOrDefault(ModDataComponents.WAND_MODE, "copy").equals("copy"))
            copyBlocksMode(player, level, stack, clickedPos);
        else pasteBlocksMode(player, level, stack, context);

        return InteractionResult.SUCCESS;
    }

    private static void copyBlocksMode(Player player, Level level, ItemStack stack, BlockPos clickedPos) {
        if (player == null) return;

        if (stack.getOrDefault(ModDataComponents.IS_CAN_COPY, 0) == 0) {
            stack.set(ModDataComponents.IS_CAN_COPY, 1);
            stack.set(ModDataComponents.BLOCKPOS_START, clickedPos);

            BlockPos pos = (BlockPos) stack.getOrDefault(ModDataComponents.BLOCKPOS_START, 0);
            sendMessage(Component.translatable("message.copywand.start_pos", pos.getX(), pos.getY(), pos.getZ()), player);

        } else {
            stack.set(ModDataComponents.BLOCKPOS_END, clickedPos);

            if (stack.getOrDefault(ModDataComponents.BLOCKPOS_END, "0")
                    .equals(stack.getOrDefault(ModDataComponents.BLOCKPOS_START, "1"))) {
                stack.set(ModDataComponents.IS_CAN_COPY, 0);
                stack.set(ModDataComponents.BLOCKPOS_START, null);
                stack.set(ModDataComponents.BLOCKPOS_END, null);
                if (level.isClientSide)
                    player.displayClientMessage(Component.translatable("message.copywand.copy_reset"), false);

            } else {
                BlockPos pos = (BlockPos) stack.getOrDefault(ModDataComponents.BLOCKPOS_END, 0);
                sendMessage(Component.translatable("message.copywand.end_pos", pos.getX(), pos.getY(), pos.getZ()), player);

                if (!copyBlocks(player, level, stack))
                    sendMessage(Component.translatable("message.copywand.copy_limit"), player);
                else {
                    CopySavedData savedData = CopySavedData.get((ServerLevel) level);
                    CopyDataManager.CopyData copy = savedData.getCopy(player.getUUID());

                    if (copy != null)
                        sendMessage(Component.translatable("message.copywand.copy_done", copy.states().size()), player);
                }
            }
        }
    }

    private static void pasteBlocksMode(Player player, Level level, ItemStack stack, UseOnContext context) {
        CopySavedData savedData = CopySavedData.get((ServerLevel) level);
        CopyDataManager.CopyData copy = savedData.getCopy(player.getUUID());
        List<BlockPos> pos = null;
        List<BlockState> state = null;
        if (copy != null) {
            pos = copy.poses();
            state = copy.states();
        }
        if (pos == null || state == null) return;

        if (pos.size() != state.size()) {
            player.displayClientMessage(Component.translatable("message.copywand.data_mismatch").withColor(color), true);
            return;
        }

        if (player.gameMode() == GameType.SURVIVAL && Config.CONSUME_ITEMS.get()) {
            //enough:0 notEnough:1 already:2
            switch (isEnoughBlocks(pos, state, player, stack,level)){
                case 0:player.displayClientMessage(Component.translatable("message.copywand.enough_blocks"), false);break;
                case 1:return;
                case 2:player.displayClientMessage(Component.translatable("message.copywand.no_place_blocks").withColor(color), false);
            }
        }

        if (!doubleClickPasteSetting(stack,player,context))return;

        List<Block> overrideBlocks = new ArrayList<>(List.of(
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS,
                Blocks.SHORT_DRY_GRASS, Blocks.TALL_DRY_GRASS, Blocks.SNOW
        ));

        BlockPos dxyz = CopyWandClient.getDxyz(player, stack);
        if (dxyz == null) return;

        for (int i = 0; i < pos.size(); i++) {
            BlockState placeState = state.get(i);
            Item item = placeState.getBlock().asItem();
            if (pos.get(i) != null) {
                BlockPos pastePos = pos.get(i).offset(dxyz);

                boolean isCanPlace = overrideBlocks.stream()
                        .anyMatch(b -> level.getBlockState(pastePos).is(b))
                        || (level.getBlockState(pastePos).hasProperty(BlockStateProperties.LEVEL) && Config.REPLACE_LIQUID.get())
                        || (level.getBlockState(pastePos).is(BlockTags.FLOWERS) && Config.REPLACE_FLOWERS.get())
                        || level.getBlockState(pastePos).is(Blocks.AIR)
                        || Config.OVERRIDE_BLOCKS.get();

                if (!isCanPlace) continue;

                level.setBlock(pastePos, placeState, 3);
            }
            if (player.gameMode() == GameType.SURVIVAL && Config.CONSUME_ITEMS.get()) {
                l:
                for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                    ItemStack stackInSlot = player.getInventory().getItem(slot);

                    if (stackInSlot.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock && Config.USE_SHULKERBOX.get()) {
                        ItemContainerContents contents = stackInSlot.get(DataComponents.CONTAINER);
                        if (contents != null) {
                            int j = 0;
                            for (var shulkerSlots : contents.nonEmptyItems()) {
                                if (shurinkItem(shulkerSlots,stackInSlot, placeState, item, player, j, true)) break l;
                                else j++;
                            }
                        }
                    }
                    if (shurinkItem(stackInSlot,null, placeState, item, player, slot, false)) break;
                }
            }
        }
    }

    private static boolean shurinkItem(ItemStack stackInSlot,ItemStack shulkeritem, BlockState placeState, Item item, Player player, int slot, boolean isShulker) {
        boolean isFluid = isSameLiquid(stackInSlot.getItem(), placeState.getBlock());
        if (!stackInSlot.isEmpty()) {
            if (stackInSlot.getItem() == item || isFluid) {
                int decrementNum = 1;

                if (placeState.hasProperty(BlockStateProperties.SLAB_TYPE))
                    if (placeState.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)
                        decrementNum++;
                if (placeState.hasProperty(BlockStateProperties.LAYERS))
                    decrementNum += placeState.getValue(BlockStateProperties.LAYERS) - 1;

                if (item == Items.POWDER_SNOW_BUCKET || isFluid) {
                    if (isShulker) editShulker(shulkeritem,Items.POWDER_SNOW_BUCKET,placeState);
                    else player.getInventory().setItem(slot, new ItemStack(Items.BUCKET, 1));
                    return true;
                }
                if (placeState.hasProperty(BlockStateProperties.WATERLOGGED) && placeState.getValue(BlockStateProperties.WATERLOGGED)) {
                    for (int j = 0; j < Inventory.INVENTORY_SIZE; j++) {
                        if (player.getInventory().getItem(j).getItem() == Items.WATER_BUCKET) {
                            player.getInventory().setItem(j, new ItemStack(Items.BUCKET, 1));
                        }
                    }
                    if (isShulker) editShulker(shulkeritem,Items.WATER_BUCKET,Blocks.AIR.defaultBlockState());

                }

                stackInSlot.shrink(decrementNum);
                return true;
            }
        }
        return false;
    }

    private static void editShulker(ItemStack shulker,Item editItem,BlockState placeblock) {
        ItemContainerContents contents = shulker.get(DataComponents.CONTAINER);

        if (contents==null)return;

        int size = contents.getSlots();
        NonNullList<ItemStack> slots = NonNullList.withSize(size, ItemStack.EMPTY);
        contents.copyInto(slots);
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i);
            if ((!stack.isEmpty() && stack.is(editItem))||isSameLiquid(stack.getItem(),placeblock.getBlock())) {
                slots.set(i, new ItemStack(Items.BUCKET, 1));
            }
        }
        shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
    }

    private static int isEnoughBlocks(List<BlockPos> pos,List<BlockState> states, Player player,ItemStack itemstack,Level level) {

        boolean isEnough = true;
        Map<Block, List<BlockState>> map=getBlockNums(pos,states,player,itemstack,level);

        if (map.isEmpty())return 2;

        player.displayClientMessage(Component.literal("---------------------"), false);

        int hasWaterLoggedBlocks = 0;
        int hasWaterLoggedBlocksInInventory = 0;
        int waterblokNum=0;

        Map<Item, Integer> shulkerItemCounts = new HashMap<>();
        for (var i : player.getInventory()) {
            if (!i.isEmpty()) {
                if (i.getItem() instanceof BlockItem blockItem &&
                        blockItem.getBlock() instanceof ShulkerBoxBlock && Config.USE_SHULKERBOX.get()) {
                    ItemContainerContents shulker = i.get(DataComponents.CONTAINER);
                    if (shulker != null) {
                        for (ItemStack item : shulker.nonEmptyItems()) {
                            shulkerItemCounts.merge(item.getItem(), item.getCount(), Integer::sum);
                            if (item.getItem() == Items.WATER_BUCKET) {
                                hasWaterLoggedBlocksInInventory++;
                            }
                        }
                    }
                }
                if (i.getItem() == Items.WATER_BUCKET) {
                    hasWaterLoggedBlocksInInventory++;
                }
            }
        }

        for (Map.Entry<Block, List<BlockState>> entry : map.entrySet()) {

            Block block = entry.getKey();

            int countInInventory = 0;

            int countRequestBlocks = entry.getValue().size();

            for (BlockState state : entry.getValue()) {
                if (state.hasProperty(BlockStateProperties.SLAB_TYPE)) {
                    if (state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)
                        countRequestBlocks++;
                }
                if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED))
                    hasWaterLoggedBlocks++;
                if (state.hasProperty(BlockStateProperties.LAYERS)) {
                    countRequestBlocks += state.getValue(BlockStateProperties.LAYERS) - 1;
                }
            }
            for (var stack : player.getInventory()) {
                if (!stack.isEmpty()) {
                    boolean b=isSameLiquid(stack.getItem(), entry.getKey());
                    if (stack.getItem() == block.asItem() || b) {
                        countInInventory += stack.getCount();
                    }
                }
            }
            if (!shulkerItemCounts.isEmpty()){
                for (var e : shulkerItemCounts.entrySet()) {
                    if (e.getKey() == block.asItem() || isSameLiquid(e.getKey(), block)) {
                        countInInventory += e.getValue();
                    }
                }
            }
            String s = block.getName().getString();

            if (block.defaultBlockState().hasProperty(BlockStateProperties.LEVEL)) {
                s += " Bucket";
            }
            if (block!=Blocks.WATER){
                if (countInInventory > 0) {
                    if (countInInventory < countRequestBlocks) {
                        sendMessage(Component.translatable("message.copywand.not_enough", s, countInInventory, countRequestBlocks).withColor(color), player);
                        isEnough=false;
                    }
                } else {
                    sendMessage(Component.translatable("message.copywand.not_found", s).withColor(color), player);
                    isEnough=false;
                }
            }else waterblokNum++;

        }
        boolean isWaterLoggedEnough = true;
        if (hasWaterLoggedBlocks != 0||waterblokNum!=0) {
            if (hasWaterLoggedBlocksInInventory > 0) {
                if (hasWaterLoggedBlocksInInventory < hasWaterLoggedBlocks+waterblokNum) {
                    sendMessage(Component.translatable("message.copywand.not_enough", "Water bucket", hasWaterLoggedBlocksInInventory, hasWaterLoggedBlocks+waterblokNum).withColor(color), player);
                    isWaterLoggedEnough=false;
                }
            } else {
                sendMessage(Component.translatable("message.copywand.not_found", "Water bucket").withColor(color), player);
                isWaterLoggedEnough=false;
            }
        }
        return (isWaterLoggedEnough && isEnough)?0:1;
    }

    private static Map<Block, List<BlockState>> getBlockNums(List<BlockPos> pos, List<BlockState> states,Player player,ItemStack stack,Level level) {
        Map<Block, List<BlockState>> stateMap = new HashMap<>();

        Map<BlockPos, BlockState> blockStates = new HashMap<>();

        if (pos == null) return null;
        if (states == null) return null;
        for (int i = 0; i < pos.size(); i++) {
            blockStates.put(pos.get(i), states.get(i));
        }
        for (Map.Entry<BlockPos, BlockState> entry : blockStates.entrySet()) {
            BlockState state = entry.getValue();
            Block block = state.getBlock();

            BlockPos dxyz = CopyWandClient.getDxyz(player, stack);
            if (dxyz == null) continue;

            BlockPos pastePos = entry.getKey().offset(dxyz);
            BlockState targetState = level.getBlockState(pastePos);
            boolean canReplace = false;
            if (Config.OVERRIDE_BLOCKS.get()) {
                canReplace = true; // すべて上書き
            } else if (targetState.isAir()) {
                canReplace = true; // 空気なら常に上書き
            } else if (targetState.is(BlockTags.FLOWERS) && Config.REPLACE_FLOWERS.get()) {
                canReplace = true; // 花ブロックは設定がtrueなら上書き
            } else if (targetState.hasProperty(BlockStateProperties.LEVEL) && Config.REPLACE_LIQUID.get()) {
                canReplace = true; // 液体ブロックは設定がtrueなら上書き
            }
            if (!canReplace) continue;
            stateMap.computeIfAbsent(block, k -> new ArrayList<>()).add(state);
        }
        return stateMap;
    }

    private static boolean copyBlocks(Player player, Level level, ItemStack stack) {
        CopySavedData savedData = CopySavedData.get((ServerLevel) level);

        List<BlockPos> poses = new ArrayList<>();
        List<BlockState> states = new ArrayList<>();

        BlockPos start = stack.getOrDefault(ModDataComponents.BLOCKPOS_START, new BlockPos(0, 0, 0));
        BlockPos end = stack.getOrDefault(ModDataComponents.BLOCKPOS_END, new BlockPos(0, 0, 0));

        int minX = Math.min(start.getX(), end.getX());
        int minY = Math.min(start.getY(), end.getY());
        int minZ = Math.min(start.getZ(), end.getZ());
        int maxX = Math.max(start.getX(), end.getX());
        int maxY = Math.max(start.getY(), end.getY());
        int maxZ = Math.max(start.getZ(), end.getZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (!state.is(Blocks.AIR)) {
                        if (state.is(Blocks.GRASS_BLOCK)) {
                            state = Blocks.GRASS_BLOCK.defaultBlockState();
                        } else if (level.getBlockEntity(pos) != null) {
                            if (!Config.INCLUDE_BLOCK_ENTITIES.get()) continue;
                            else if (state.is(BlockTags.SHULKER_BOXES)) continue;
                        } else if (state.is(BlockTags.FLOWERS)) {
                            if (!Config.FLOWERS_COPY.get()) continue;
                        } else if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.SHORT_DRY_GRASS) || state.is(Blocks.TALL_DRY_GRASS)) {
                            if (!Config.GRASS_COPY.get()) continue;
                        } else if (state.hasProperty(BlockStateProperties.LAYERS)) {
                            if (!Config.SNOW_COPY.get()) continue;
                        } else if (state.hasProperty(BlockStateProperties.LEVEL)) {
                            if (!Config.LIQUID_COPY.get()) continue;
                            if (state.getValue(BlockStateProperties.LEVEL) != 0) continue;
                        } else if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                            if (!Config.LIQUID_COPY.get()) state = state.setValue(BlockStateProperties.WATERLOGGED, false);
                        } else if (state.is(Blocks.POWDER_SNOW)) {
                            if (!Config.PAWDERSNOW_COPY.get()) continue;
                        } else if (Config.CONSUME_ITEMS.get()) {
                            if (state.hasProperty(BlockStateProperties.LEVEL_CAULDRON))
                                state = Blocks.CAULDRON.defaultBlockState();
                            else if (state.hasProperty(BlockStateProperties.LEVEL_COMPOSTER))
                                state = Blocks.COMPOSTER.defaultBlockState();
                            else if (state.hasProperty(BlockStateProperties.LEVEL_HONEY))
                                state = Blocks.HONEY_BLOCK.defaultBlockState();
                            else if (state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH))
                                state = Blocks.DIRT.defaultBlockState();
                            else if (state.is(BlockTags.CROPS)) continue;
                        }

                        poses.add(pos);
                        states.add(state);
                    }
                }
            }
        }
        if (states.size() <= Config.COPY_LIMIT.get()) {
            savedData.saveCopy(player.getUUID(), new CopyDataManager.CopyData(poses, states));
            return true;
        }
        return false;
    }

    private static boolean isSameLiquid(Item item, Block block) {
        if (item instanceof BucketItem bucketItem) {
            Fluid itemfluid = bucketItem.content;
            if (itemfluid != Fluids.EMPTY) {
                Fluid fluid = block.defaultBlockState().getFluidState().getType();
                return fluid == itemfluid;
            }
        }
        return false;
    }

    private static void sendMessage(
            MutableComponent component, Player player) {
        player.displayClientMessage(component, false);
    }
    private static boolean doubleClickPasteSetting(ItemStack stack,Player player,UseOnContext context){
        if (Config.DOUBLE_CLICK_PASTE.get()) {
            if (stack.getOrDefault(ModDataComponents.IS_CAN_PASTE, 0) == 0) {
                stack.set(ModDataComponents.IS_CAN_PASTE, 1);
                stack.set(ModDataComponents.BLOCKPOS_PASTE, context.getClickedPos());
                if (Config.DOUBLE_CLICK_PASTE_CHAT.get()) {
                    sendMessage(Component.translatable("message.copywand.double_click.wait",
                            context.getClickedPos().getX(),
                            context.getClickedPos().getY(),
                            context.getClickedPos().getZ()).withStyle(style -> style.withBold(true)), player);
                    sendMessage(Component.translatable("message.copywand.double_click.cancel_hint").withStyle(style -> style.withBold(true)), player);
                }else sendMessage(Component.literal("Ready to paste"),player);
                return false;
            } else {
                stack.set(ModDataComponents.IS_CAN_PASTE, 0);
                if (!stack.getOrDefault(ModDataComponents.BLOCKPOS_PASTE, null).equals(context.getClickedPos())) {
                    if (Config.DOUBLE_CLICK_PASTE_CHAT.get())
                        sendMessage(Component.translatable("message.copywand.double_click.cancelled").withStyle(style -> style.withBold(true)).withColor(color), player);
                    else sendMessage(Component.literal("Paste cancelled"),player);
                    return false;
                }
            }
        } else stack.set(ModDataComponents.IS_CAN_PASTE, 0);
        return true;
    }
}
