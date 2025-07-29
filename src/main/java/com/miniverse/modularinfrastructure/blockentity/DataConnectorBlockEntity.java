package com.miniverse.modularinfrastructure.blockentity;

import com.google.common.collect.ImmutableList;
import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.block.DataConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

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
    
    @Override
    protected double getConnectorLength() {
        // Based on model dimensions - both tiers are 11 pixels tall
        // Connection point should be at the top of the connector
        return 0.6875;  // 11/16 = 0.6875 blocks from edge
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
    
    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        // TODO: When implementing data transfer (AE2, RS integration), request appropriate handlers
        // For now, data connectors don't request any handlers
        return ImmutableList.of();
    }
}