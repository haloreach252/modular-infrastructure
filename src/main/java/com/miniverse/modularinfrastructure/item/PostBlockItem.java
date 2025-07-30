package com.miniverse.modularinfrastructure.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class PostBlockItem extends BlockItem {
    public PostBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        
        int width = getStoredWidth(stack);
        tooltip.add(Component.literal("Size: " + (width * 2) + "px")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift + Scroll to adjust size")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
    
    public static int getStoredWidth(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains("PostWidth")) {
            int width = customData.copyTag().getInt("PostWidth");
            return Math.max(2, Math.min(8, width));
        }
        return 4; // Default 8px
    }
    
    public static void setStoredWidth(ItemStack stack, int width) {
        width = Math.max(2, Math.min(8, width));
        // Get existing custom data or create new
        CustomData existingData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existingData.copyTag();
        tag.putInt("PostWidth", width);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}