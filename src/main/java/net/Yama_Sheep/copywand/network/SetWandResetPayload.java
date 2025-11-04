package net.Yama_Sheep.copywand.network;

import net.Yama_Sheep.copywand.CopyWand;
import net.Yama_Sheep.copywand.CopyWandItem;
import net.Yama_Sheep.copywand.ModDataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetWandResetPayload(boolean reset) implements CustomPacketPayload {

    public static final Type<SetWandResetPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CopyWand.MODID, "reset"));

    public static final StreamCodec<FriendlyByteBuf,SetWandResetPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetWandResetPayload::reset,
                    SetWandResetPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static class ClientPacketHandler{
        public static void handle(final SetWandResetPayload packet , final IPayloadContext context) {
            ItemStack stack = context.player().getMainHandItem();
            if (stack.getItem() instanceof CopyWandItem) {
                stack.set(ModDataComponents.BLOCKPOS_START, null);
                stack.set(ModDataComponents.BLOCKPOS_END, null);
                stack.set(ModDataComponents.IS_CAN_COPY, 0);
            }
        }
    }
}
