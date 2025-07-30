package com.miniverse.modularinfrastructure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class IndustrialWrenchItem extends Item {
    
    public IndustrialWrenchItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        
        if (player != null && !level.isClientSide()) {
            // Block-specific wrench behavior will be handled by individual blocks
            // through their use() methods checking for wrench in hand
        }
        
        return InteractionResult.PASS;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide() && player.isShiftKeyDown()) {
            // Right-click air with shift - could be used for mode cycling in future
            player.displayClientMessage(Component.literal("Wrench mode: Standard"), true);
        }
        
        return InteractionResultHolder.pass(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.modularinfrastructure.industrial_wrench.tooltip"));
        tooltip.add(Component.translatable("item.modularinfrastructure.industrial_wrench.tooltip.usage"));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }
}