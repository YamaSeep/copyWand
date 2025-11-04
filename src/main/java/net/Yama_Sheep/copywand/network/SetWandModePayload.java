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

public record SetWandModePayload(String mode) implements CustomPacketPayload {

    public static final Type<SetWandModePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CopyWand.MODID, "mode"));

    public static final StreamCodec<FriendlyByteBuf,SetWandModePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SetWandModePayload::mode,
                    SetWandModePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static class ClientPacketHandler{
        public static void handle(final SetWandModePayload packet , final IPayloadContext context) {
            ItemStack stack = context.player().getMainHandItem();
            if (stack.getItem() instanceof CopyWandItem) {
                stack.set(ModDataComponents.WAND_MODE, packet.mode());
            }
        }
    }
}
