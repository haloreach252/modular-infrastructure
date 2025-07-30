package com.miniverse.modularinfrastructure.client.render;

import com.miniverse.modularinfrastructure.block.PostBlock;
import com.miniverse.modularinfrastructure.item.PostBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PostPreviewRenderer {
    
    public static void renderPreview(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        
        if (player == null || level == null) return;
        
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof PostBlockItem)) {
            stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof PostBlockItem)) {
                return;
            }
        }
        
        // Get the block the player is looking at
        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos targetPos = blockHit.getBlockPos();
            
            // Create a fake place context to determine where the block would be placed
            // Determine which hand holds the post item
            var hand = stack == player.getMainHandItem() ? net.minecraft.world.InteractionHand.MAIN_HAND : net.minecraft.world.InteractionHand.OFF_HAND;
            BlockPlaceContext placeContext = new BlockPlaceContext(player, hand, stack, blockHit);
            BlockPos placePos = placeContext.getClickedPos();
            
            if (placePos != null && level.getBlockState(placePos).canBeReplaced()) {
                PostBlock postBlock = (PostBlock) ((BlockItem) stack.getItem()).getBlock();
                int width = PostBlockItem.getStoredWidth(stack);
                BlockState previewState = postBlock.defaultBlockState().setValue(PostBlock.WIDTH, width);
                
                // Get the buffer source
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                renderGhostBlock(poseStack, bufferSource, placePos, previewState, level, partialTick);
                bufferSource.endBatch();
            }
        }
    }
    
    private static void renderGhostBlock(PoseStack poseStack, MultiBufferSource bufferSource, 
                                        BlockPos pos, BlockState state, Level level, float partialTick) {
        poseStack.pushPose();
        
        // Translate to block position relative to camera
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
        
        // Get render type with transparency
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());
        
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        
        // Render with transparency - using reduced light for ghost effect
        dispatcher.renderSingleBlock(state, poseStack, bufferSource, 0x00F000F0, OverlayTexture.NO_OVERLAY);
        
        poseStack.popPose();
    }
}