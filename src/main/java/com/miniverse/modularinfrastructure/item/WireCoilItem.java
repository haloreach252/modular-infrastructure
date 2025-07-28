package com.miniverse.modularinfrastructure.item;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.wire.*;
import com.miniverse.modularinfrastructure.blockentity.ConnectorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Wire coil item for placing wires between connectors
 * 
 * Inspired by Immersive Engineering's wire coil system
 * Original wire system concepts from Immersive Engineering by BluSunrize
 */
public class WireCoilItem extends Item {
    private final WireType wireType;
    private final int maxLength;
    
    public WireCoilItem(Properties properties, WireType wireType) {
        super(properties.stacksTo(1));
        this.wireType = wireType;
        this.maxLength = wireType.getMaxLength();
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        
        if (player == null) {
            return InteractionResult.PASS;
        }
        
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ConnectorBlockEntity connector)) {
            return InteractionResult.PASS;
        }
        
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            
            if (tag.contains("linkingPos")) {
                // Second click - complete the connection
                BlockPos firstPos = NbtUtils.readBlockPos(tag, "linkingPos").orElse(null);
                
                if (firstPos == null || firstPos.equals(pos)) {
                    // Invalid connection
                    tag.remove("linkingPos");
                    player.displayClientMessage(Component.translatable("message.modularinfrastructure.wire_connection_cancelled"), true);
                    return InteractionResult.SUCCESS;
                }
                
                // Try to create the connection
                double distance = Math.sqrt(pos.distSqr(firstPos));
                if (distance > maxLength) {
                    player.displayClientMessage(Component.translatable("message.modularinfrastructure.wire_too_long", maxLength), true);
                    return InteractionResult.SUCCESS;
                }
                
                // Get the connectors to calculate offsets
                BlockEntity be1 = level.getBlockEntity(firstPos);
                BlockEntity be2 = level.getBlockEntity(pos);
                
                if (!(be1 instanceof IImmersiveConnectable conn1) || !(be2 instanceof IImmersiveConnectable conn2)) {
                    player.displayClientMessage(Component.translatable("message.modularinfrastructure.invalid_connector"), true);
                    return InteractionResult.SUCCESS;
                }
                
                // Create connection points
                ConnectionPoint cp1 = new ConnectionPoint(firstPos, 0);
                ConnectionPoint cp2 = new ConnectionPoint(pos, 0);
                
                // Get connection offsets from the connectors
                Vec3 offset1 = conn1.getConnectionOffset(cp1, cp2, wireType);
                Vec3 offset2 = conn2.getConnectionOffset(cp2, cp1, wireType);
                
                ModularInfrastructure.LOGGER.info("Connection offsets - Pos1: {}, Offset1: {}, Pos2: {}, Offset2: {}", 
                    firstPos, offset1, pos, offset2);
                
                // Create the connection with offsets
                Connection connection = new Connection(wireType, cp1, cp2, offset1, offset2);
                
                // Try to add the connection
                GlobalWireNetwork network = GlobalWireNetwork.getNetwork(level);
                ModularInfrastructure.LOGGER.info("Adding wire connection from {} to {} with type {}", firstPos, pos, wireType.getUniqueName());
                network.addConnection(connection);
                
                if (true) { // Connection always succeeds with new system
                    // Connection successful
                    ModularInfrastructure.LOGGER.info("Wire connection successful!");
                    player.displayClientMessage(Component.translatable("message.modularinfrastructure.wire_connected"), true);
                    level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    
                    // Consume the wire coil if not in creative mode
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                } else {
                    player.displayClientMessage(Component.translatable("message.modularinfrastructure.wire_connection_failed"), true);
                }
                
                // Clear the linking position
                stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(new CompoundTag()));
                
            } else {
                // First click - store the position
                tag = new CompoundTag();
                tag.put("linkingPos", NbtUtils.writeBlockPos(pos));
                stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                player.displayClientMessage(Component.translatable("message.modularinfrastructure.wire_linking", pos.toShortString()), true);
                level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.6F);
            }
        }
        
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.modularinfrastructure.wire_coil.type", wireType.getUniqueName())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.modularinfrastructure.wire_coil.max_length", maxLength)
                .withStyle(ChatFormatting.GRAY));
        
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains("linkingPos")) {
            CompoundTag tag = customData.copyTag();
            BlockPos linkingPos = NbtUtils.readBlockPos(tag, "linkingPos").orElse(null);
            if (linkingPos != null) {
                tooltip.add(Component.translatable("tooltip.modularinfrastructure.wire_coil.linking", linkingPos.toShortString())
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }
    
    // Note: isFoil removed as it's not properly overridable in 1.21.1
    // The glowing effect would need to be implemented differently
    
    public WireType getWireType() {
        return wireType;
    }
}