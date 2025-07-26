package com.miniverse.modularinfrastructure.client;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.item.PostBlockItem;
import com.miniverse.modularinfrastructure.client.render.PostPreviewRenderer;
import com.miniverse.modularinfrastructure.network.packets.UpdatePostItemWidthPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ModularInfrastructure.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player == null || !player.isShiftKeyDown()) return;
        
        ItemStack stack = player.getMainHandItem();
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!(stack.getItem() instanceof PostBlockItem)) {
            stack = player.getOffhandItem();
            hand = InteractionHand.OFF_HAND;
            if (!(stack.getItem() instanceof PostBlockItem)) {
                return;
            }
        }
        
        double scrollDelta = event.getScrollDeltaY();
        
        if (scrollDelta != 0) {
            int currentWidth = PostBlockItem.getStoredWidth(stack);
            int newWidth = currentWidth;
            
            if (scrollDelta > 0) {
                // Scroll up - increase size
                newWidth = Math.min(8, currentWidth + 1);
            } else {
                // Scroll down - decrease size
                newWidth = Math.max(2, currentWidth - 1);
            }
            
            if (newWidth != currentWidth) {
                PostBlockItem.setStoredWidth(stack, newWidth);
                
                // Send packet to server to sync the change
                InteractionHand finalHand = hand;
                PacketDistributor.sendToServer(new UpdatePostItemWidthPacket(finalHand, newWidth));
                
                // Debug logging
                ModularInfrastructure.LOGGER.debug("Setting post width - Stack: {}, New Width: {}", stack, newWidth);
                
                // Send feedback to player
                player.displayClientMessage(
                    Component.literal("Post size: " + (newWidth * 2) + "px"), 
                    true
                );
                
                // Cancel the scroll event so it doesn't change hotbar selection
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            PostPreviewRenderer.renderPreview(event.getPoseStack(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }
}