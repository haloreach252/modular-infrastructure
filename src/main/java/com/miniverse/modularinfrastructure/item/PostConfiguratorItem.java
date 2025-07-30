package com.miniverse.modularinfrastructure.item;

import com.miniverse.modularinfrastructure.block.PostBlock;
import com.miniverse.modularinfrastructure.blockentity.PostBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PostConfiguratorItem extends Item {
    public enum Mode {
        COPY("Copy", ChatFormatting.GREEN),
        PASTE("Paste", ChatFormatting.YELLOW),
        BATCH("Batch", ChatFormatting.AQUA);
        
        private final String name;
        private final ChatFormatting color;
        
        Mode(String name, ChatFormatting color) {
            this.name = name;
            this.color = color;
        }
        
        public Component getDisplay() {
            return Component.literal(name).withStyle(color);
        }
        
        public Mode next() {
            Mode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
    
    public PostConfiguratorItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        
        if (!(state.getBlock() instanceof PostBlock)) {
            return InteractionResult.PASS;
        }
        
        Mode mode = getMode(stack);
        
        // Check for shift-click to paste
        if (player != null && player.isShiftKeyDown()) {
            // Paste post configuration
            CompoundTag data = getStoredData(stack);
            if (data != null && data.contains("Width")) {
                int width = data.getInt("Width");
                String material = data.getString("Material");
                
                if (!level.isClientSide) {
                    level.setBlock(pos, state.setValue(PostBlock.WIDTH, width), 3);
                    
                    if (level.getBlockEntity(pos) instanceof PostBlockEntity postEntity) {
                        postEntity.setWidth(width * 2);
                        try {
                            PostBlockEntity.PostMaterial mat = PostBlockEntity.PostMaterial.valueOf(material);
                            postEntity.setMaterial(mat);
                        } catch (IllegalArgumentException e) {
                            // Material not found, keep existing
                        }
                        postEntity.setChanged();
                    }
                    
                    player.displayClientMessage(
                        Component.literal("Pasted ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(mode.getDisplay())
                            .append(" configuration"),
                        true
                    );
                }
                
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.literal("No configuration stored!")
                        .withStyle(ChatFormatting.RED),
                    true
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            // Normal click - copy configuration
            if (level.getBlockEntity(pos) instanceof PostBlockEntity postEntity) {
                CompoundTag data = new CompoundTag();
                data.putInt("Width", state.getValue(PostBlock.WIDTH));
                data.putString("Material", postEntity.getMaterial().name());
                
                setStoredData(stack, data);
                
                if (!level.isClientSide && player != null) {
                    player.displayClientMessage(
                        Component.literal("Copied ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(mode.getDisplay())
                            .append(" configuration (")
                            .append(Component.literal((state.getValue(PostBlock.WIDTH) * 2) + "px " + postEntity.getMaterial().getTextureName()))
                            .append(")"),
                        true
                    );
                }
                
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        
        return InteractionResult.PASS;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (player.isShiftKeyDown()) {
            // Shift+Right-click to cycle mode
            Mode currentMode = getMode(stack);
            Mode newMode = currentMode.next();
            setMode(stack, newMode);
            
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.literal("Mode: ").withStyle(ChatFormatting.WHITE)
                        .append(newMode.getDisplay()),
                    true
                );
            }
            
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        
        return InteractionResultHolder.pass(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Mode mode = getMode(stack);
        tooltip.add(Component.literal("Mode: ").withStyle(ChatFormatting.GRAY)
            .append(mode.getDisplay()));
        
        CompoundTag data = getStoredData(stack);
        if (data != null && data.contains("Width")) {
            tooltip.add(Component.literal("Stored: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal((data.getInt("Width") * 2) + "px " + data.getString("Material"))
                    .withStyle(ChatFormatting.WHITE)));
        }
        
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Right-click post to copy")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("Shift+Right-click post to paste")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("Shift+Right-click air to change mode")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
    
    private Mode getMode(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains("Mode")) {
            String modeName = customData.copyTag().getString("Mode");
            try {
                return Mode.valueOf(modeName);
            } catch (IllegalArgumentException e) {
                // Invalid mode, return default
            }
        }
        return Mode.COPY;
    }
    
    private void setMode(ItemStack stack, Mode mode) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString("Mode", mode.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    
    private CompoundTag getStoredData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains("PostData")) {
            return customData.copyTag().getCompound("PostData");
        }
        return null;
    }
    
    private void setStoredData(ItemStack stack, CompoundTag data) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.put("PostData", data);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}