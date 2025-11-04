package net.Yama_Sheep.copywand;

import net.Yama_Sheep.copywand.network.SetWandModePayload;
import net.Yama_Sheep.copywand.network.SetWandResetPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientConfigScreen extends Screen {

    protected ClientConfigScreen() {
        super(Component.literal("Copy Wand Config"));
    }

    @Override
    protected void init() {
        Player player=Minecraft.getInstance().player;
        if (player==null){
            this.onClose();
            return;
        }

        ItemStack stack=player.getMainHandItem();
        Button copy= this.addRenderableWidget(setButton("Copy",this.width/2  - 40,this.height/2 - 40,80,20,
                b -> modeSwitch(stack,"copy")));
        copy.active= !stack.getOrDefault(ModDataComponents.WAND_MODE, "copy").equals("copy");

        Button paste= this.addRenderableWidget(setButton("Paste",this.width/2  - 40,this.height/2 - 20,80,20,
                b -> modeSwitch(stack,"paste")));
        paste.active= !stack.getOrDefault(ModDataComponents.WAND_MODE, "copy").equals("paste");

        this.addRenderableWidget(setButton("reset area",this.width/2  - 60,this.height-30,120,20,
                b -> resetArea(stack)));

        this.addRenderableWidget(setButton("Close",this.width  - 90,this.height + 10,80,20,
                b -> this.onClose())
        );
    }
    private void modeSwitch(ItemStack stack, String mode){
        stack.set(ModDataComponents.WAND_MODE, mode);
        PacketDistributor.sendToServer(new SetWandModePayload(mode));
        this.onClose();
    }
    private void resetArea(ItemStack stack){
        stack.set(ModDataComponents.BLOCKPOS_START, null);
        stack.set(ModDataComponents.BLOCKPOS_END, null);
        PacketDistributor.sendToServer(new SetWandResetPayload(false));
        this.onClose();
    }

    private static Button setButton(String name, int x, int y, int w, int h, Button.OnPress o){
        return Button.builder(Component.literal(name), o).pos(x,y).size(w,h).build();
    }
    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
