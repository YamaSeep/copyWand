package net.Yama_Sheep.copywand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CopyWand.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CopyWand.MODID, value = Dist.CLIENT)
public class CopyWandClient {
    public CopyWandClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof CopyWandItem)) return;

        BlockPos start = stack.getOrDefault(ModDataComponents.BLOCKPOS_START,null);
        BlockPos end = stack.getOrDefault(ModDataComponents.BLOCKPOS_END,null);

        if(stack.getOrDefault(ModDataComponents.WAND_MODE,"copy").equals("paste")){
            BlockPos pos = mc.player.blockPosition();
            BlockPos startTMP=stack.get(ModDataComponents.BLOCKPOS_START);
            BlockPos endTMP=stack.get(ModDataComponents.BLOCKPOS_END);

            if (startTMP==null||endTMP==null)return;

            BlockPos dxyz= getDxyz(mc.player,stack);
            if (dxyz==null)return;
            int dx = dxyz.getX();
            int dy = dxyz.getY();
            int dz = dxyz.getZ();

            start=new BlockPos(startTMP.getX()+dx,startTMP.getY()+dy,startTMP.getZ()+dz);
            end=new BlockPos(endTMP.getX()+dx,endTMP.getY()+dy,endTMP.getZ()+dz);

        }

        PoseStack poseStack = event.getPoseStack();
            Camera camera = mc.gameRenderer.getMainCamera();
            Vec3 camPos = camera.getPosition();

            MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
            VertexConsumer builder = buffer.getBuffer(RenderType.lines());

            poseStack.pushPose();

            // 始点は緑、終点は赤で表示
            if (start != null)
                drawBox(builder, poseStack, new AABB(start).move(-camPos.x, -camPos.y, -camPos.z), 0f, 1f, 0f, 1f);
            if (end != null)
                drawBox(builder, poseStack, new AABB(end).move(-camPos.x, -camPos.y, -camPos.z), 1f, 0f, 0f, 1f);
            if (start != null && end != null) {
                int minX = Math.min(start.getX(), end.getX());
                int minY = Math.min(start.getY(), end.getY());
                int minZ = Math.min(start.getZ(), end.getZ());
                int maxX = Math.max(start.getX(), end.getX()) + 1;
                int maxY = Math.max(start.getY(), end.getY()) + 1;
                int maxZ = Math.max(start.getZ(), end.getZ()) + 1;

                AABB selectionBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
                drawBox(builder, poseStack, selectionBox.move(-camPos.x, -camPos.y, -camPos.z), 0f, 0f, 1f, 1f);
            }
            poseStack.popPose();
            buffer.endBatch(RenderType.lines());

    }
    private static void drawBox(VertexConsumer builder, PoseStack poseStack, AABB box,
                                float r, float g, float b, float a) {
        PoseStack.Pose pose = poseStack.last();

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        // 12本の辺を描く
        drawLine(builder, pose, x1, y1, z1, x2, y1, z1, r, g, b, a);
        drawLine(builder, pose, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(builder, pose, x2, y1, z2, x1, y1, z2, r, g, b, a);
        drawLine(builder, pose, x1, y1, z2, x1, y1, z1, r, g, b, a);

        drawLine(builder, pose, x1, y2, z1, x2, y2, z1, r, g, b, a);
        drawLine(builder, pose, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(builder, pose, x2, y2, z2, x1, y2, z2, r, g, b, a);
        drawLine(builder, pose, x1, y2, z2, x1, y2, z1, r, g, b, a);

        drawLine(builder, pose, x1, y1, z1, x1, y2, z1, r, g, b, a);
        drawLine(builder, pose, x2, y1, z1, x2, y2, z1, r, g, b, a);
        drawLine(builder, pose, x2, y1, z2, x2, y2, z2, r, g, b, a);
        drawLine(builder, pose, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }
    private static void drawLine(VertexConsumer builder, PoseStack.Pose pose,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float r, float g, float b, float a) {
        builder.addVertex(pose.pose(), x1, y1, z1)
                .setColor(r, g, b, a)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(pose.pose(), x2, y2, z2)
                .setColor(r, g, b, a)
                .setNormal(0f, 1f, 0f);
    }
    public static BlockPos getDxyz(Player player, ItemStack stack){
        BlockPos pos = player.blockPosition();
        BlockPos startTMP=stack.get(ModDataComponents.BLOCKPOS_START);
        BlockPos endTMP=stack.get(ModDataComponents.BLOCKPOS_END);

        if (startTMP==null||endTMP==null)return null;
        int vecX =startTMP.getX()-endTMP.getX();
        int vecZ =startTMP.getZ()-endTMP.getZ();

        int dx = pos.getX() - startTMP.getX()-Integer.signum(vecX);
        int dy = pos.getY() - startTMP.getY();
        int dz = pos.getZ() - startTMP.getZ()-Integer.signum(vecZ);
        return new BlockPos(dx,dy,dz);
    }
}
