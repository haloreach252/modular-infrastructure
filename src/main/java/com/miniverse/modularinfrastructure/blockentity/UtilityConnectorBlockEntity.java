package com.miniverse.modularinfrastructure.blockentity;

import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.block.UtilityConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for utility connectors (redstone, structural)
 */
public class UtilityConnectorBlockEntity extends ConnectorBlockEntity {
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
        // Based on model dimensions - distance from block edge
        // Models: Redstone is 6 pixels tall, Structural is 8 pixels tall
        return switch (type) {
            case REDSTONE -> 0.375;      // 6/16 = 0.375 blocks from edge
            case STRUCTURAL -> 0.5;      // 8/16 = 0.5 blocks from edge (center)
        };
    }
    
    public int getRedstoneSignal() {
        return redstoneSignal;
    }
    
    public void setRedstoneSignal(int signal) {
        if (type == UtilityConnectorBlock.UtilityType.REDSTONE) {
            this.redstoneSignal = Math.max(0, Math.min(15, signal));
            setChanged();
            
            // TODO: Propagate signal through wire network
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
}