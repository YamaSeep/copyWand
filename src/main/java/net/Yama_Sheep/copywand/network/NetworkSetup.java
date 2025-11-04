package net.Yama_Sheep.copywand.network;

import net.Yama_Sheep.copywand.CopyWand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CopyWand.MODID)
public class NetworkSetup {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar=event.registrar("1");
        registrar.playToServer(
                SetWandModePayload.TYPE,
                SetWandModePayload.STREAM_CODEC,
                new MainThreadPayloadHandler<>(
                        SetWandModePayload.ClientPacketHandler::handle)
        );
        registrar.playToServer(
                SetWandResetPayload.TYPE,
                SetWandResetPayload.STREAM_CODEC,
                new MainThreadPayloadHandler<>(
                        SetWandResetPayload.ClientPacketHandler::handle)
        );
    }
}
