package net.Yama_Sheep.copywand;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CopyWand.MODID, value = Dist.CLIENT)

public class KeyBind {
    private static KeyMapping copyWandMode;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        copyWandMode = new KeyMapping(
                "key.minetweaks.copywandmode",      // 翻訳キー
                InputConstants.Type.KEYSYM,   // 入力タイプ（キーボード）
                GLFW.GLFW_KEY_Y,         // デフォルトのキー（O）
                "key.categories.copywand"   // カテゴリ名
        );
        event.register(copyWandMode);
    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (copyWandMode != null) {
            Minecraft mc = Minecraft.getInstance();
            Player player=mc.player;
            if (player==null)return;
            if(player.getMainHandItem().is(ModItems.COPYWANDITEM)){
                if (copyWandMode.consumeClick()){
                    mc.setScreen(new ClientConfigScreen());
                }
            }

        }
    }

}
