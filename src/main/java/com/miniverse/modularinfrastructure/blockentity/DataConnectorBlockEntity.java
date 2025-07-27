package com.miniverse.modularinfrastructure.blockentity;

import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.block.DataConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for data connectors
 * Handles data channel management for mod integration (AE2, RS, etc.)
 */
public class DataConnectorBlockEntity extends ConnectorBlockEntity {
    private final DataConnectorBlock.DataTier tier;
    private int usedChannels = 0;
    
    public DataConnectorBlockEntity(BlockPos pos, BlockState state, DataConnectorBlock.DataTier tier) {
        super(ModBlockEntities.DATA_CONNECTOR.get(), pos, state);
        this.tier = tier;
        this.maxConnections = tier.getChannels();
    }
    
    public DataConnectorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, DataConnectorBlock.DataTier.BASIC);
    }
    
    public int getAvailableChannels() {
        return tier.getChannels() - usedChannels;
    }
    
    public boolean requestChannels(int amount) {
        if (getAvailableChannels() >= amount) {
            usedChannels += amount;
            setChanged();
            return true;
        }
        return false;
    }
    
    public void releaseChannels(int amount) {
        usedChannels = Math.max(0, usedChannels - amount);
        setChanged();
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("usedChannels", usedChannels);
        tag.putString("tier", tier.name());
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        usedChannels = tag.getInt("usedChannels");
    }
}