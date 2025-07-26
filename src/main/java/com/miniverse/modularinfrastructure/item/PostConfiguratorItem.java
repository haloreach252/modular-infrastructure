package com.miniverse.modularinfrastructure.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class PostConfiguratorItem extends Item {
    public PostConfiguratorItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        // TODO: Implement copy/paste functionality
        return InteractionResult.PASS;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.modularinfrastructure.post_configurator.tooltip"));
        // TODO: Show current mode and copied configuration
    }
}