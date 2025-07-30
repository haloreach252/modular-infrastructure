package com.miniverse.modularinfrastructure.blockentity;

import com.google.common.collect.ImmutableList;
import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.block.UtilityConnectorBlock;
import com.miniverse.modularinfrastructure.common.wires.WireConfig;
import com.miniverse.modularinfrastructure.api.wires.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wires.redstone.IRedstoneConnector;
import com.miniverse.modularinfrastructure.api.wires.redstone.RedstoneNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

/**
 * Block entity for utility connectors (redstone, structural)
 */
public class UtilityConnectorBlockEntity extends ConnectorBlockEntity implements IRedstoneConnector {
    private final UtilityConnectorBlock.UtilityType type;
    private int redstoneSignal = 0; // For redstone connectors
    
    public UtilityConnectorBlockEntity(BlockPos pos, BlockState state, UtilityConnectorBlock.UtilityType type) {
        super(ModBlockEntities.UTILITY_CONNECTOR.get(), pos, state);
        this.type = type;
        this.maxConnections = type == UtilityConnectorBlock.UtilityType.STRUCTURAL ? 16 : 8;
    }
    
    public UtilityConnectorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, UtilityConnectorBlock.UtilityType.STRUCTURAL);
    }
    
    @Override
    protected double getConnectorLength() {
        // Using values from WireConfig for easy adjustment
        return switch (type) {
            case REDSTONE -> WireConfig.ConnectorOffsets.REDSTONE_CONNECTOR_LENGTH;
            case STRUCTURAL -> WireConfig.ConnectorOffsets.STRUCTURAL_CONNECTOR_LENGTH;
        };
    }
    
    public int getRedstoneSignal() {
        return redstoneSignal;
    }
    
    public void setRedstoneSignal(int signal) {
        if (type == UtilityConnectorBlock.UtilityType.REDSTONE) {
            this.redstoneSignal = Math.max(0, Math.min(15, signal));
            setChanged();
            
            // Signal will be propagated through wire network via updateInput
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("type", type.name());
        if (type == UtilityConnectorBlock.UtilityType.REDSTONE) {
            tag.putInt("redstoneSignal", redstoneSignal);
        }
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("redstoneSignal")) {
            redstoneSignal = tag.getInt("redstoneSignal");
        }
    }
    
    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        // Request appropriate handlers based on connector type
        return switch (type) {
            case REDSTONE -> ImmutableList.of(
                com.miniverse.modularinfrastructure.api.wires.redstone.RedstoneNetworkHandler.ID
            );
            case STRUCTURAL -> ImmutableList.of(); // Structural wires don't need handlers
        };
    }
    
    // IRedstoneConnector implementation
    @Override
    public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler) {
        if (type != UtilityConnectorBlock.UtilityType.REDSTONE) {
            return;
        }
        
        // Update our redstone output based on the network
        byte[] values = handler.getValuesExcluding(cp);
        int maxSignal = 0;
        for (byte val : values) {
            maxSignal = Math.max(maxSignal, val & 0xFF);
        }
        
        if (this.redstoneSignal != maxSignal) {
            this.redstoneSignal = maxSignal;
            setChanged();
            
            // Notify neighbors of redstone change
            if (level != null && !level.isClientSide) {
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
        }
    }
    
    @Override
    public void updateInput(byte[] signals, ConnectionPoint cp) {
        if (type != UtilityConnectorBlock.UtilityType.REDSTONE) {
            return;
        }
        
        // Get redstone input from neighboring blocks
        if (level != null) {
            int power = level.getBestNeighborSignal(worldPosition);
            if (power > 0) {
                // Set all channels to this power level (IE uses channel 0 by default)
                signals[0] = (byte) Math.max(signals[0], power);
            }
        }
    }
}