package com.miniverse.modularinfrastructure.blockentity;

import com.google.common.collect.ImmutableList;
import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.block.DataConnectorBlock;
import com.miniverse.modularinfrastructure.common.wires.WireConfig;
import com.miniverse.modularinfrastructure.integration.ae2.IAE2DataConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

/**
 * Block entity for data connectors
 * Handles data channel management for mod integration (AE2, RS, etc.)
 * 
 * When AE2 is present, this block entity delegates to an AE2 component for grid functionality
 */
public class DataConnectorBlockEntity extends ConnectorBlockEntity implements com.miniverse.modularinfrastructure.integration.ae2.IOptionalAE2Host {
    private final DataConnectorBlock.DataTier tier;
    private int usedChannels = 0;
    
    // Optional AE2 integration component
    private IAE2DataConnector ae2Component = null;
    
    public DataConnectorBlockEntity(BlockPos pos, BlockState state, DataConnectorBlock.DataTier tier) {
        super(ModBlockEntities.DATA_CONNECTOR.get(), pos, state);
        this.tier = tier;
        // Don't set maxConnections here, it will be dynamically calculated based on channel mode
        
        // Initialize AE2 component if AE2 is loaded
        if (com.miniverse.modularinfrastructure.integration.ae2.ModAE2Integration.isAE2Loaded()) {
            ae2Component = com.miniverse.modularinfrastructure.integration.ae2.ModAE2Integration.createAE2Component(this);
        }
    }
    
    public DataConnectorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, DataConnectorBlock.DataTier.BASIC);
    }
    
    @Override
    protected double getConnectorLength() {
        // Using values from WireConfig for easy adjustment
        return switch (tier) {
            case BASIC -> WireConfig.ConnectorOffsets.DATA_BASIC_CONNECTOR_LENGTH;
            case ADVANCED -> WireConfig.ConnectorOffsets.DATA_ADVANCED_CONNECTOR_LENGTH;
        };
    }
    
    public int getAvailableChannels() {
        return getMaxChannels() - usedChannels;
    }
    
    /**
     * Get the maximum channels this connector can handle, respecting AE2's channel mode if present
     */
    public int getMaxChannels() {
        if (ae2Component != null) {
            // Use AE2's channel mode configuration
            return ae2Component.getEffectiveChannelCapacity(tier.getChannels());
        }
        // Without AE2, use the base tier channels
        return tier.getChannels();
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
    public void onLoad() {
        super.onLoad();
        if (ae2Component != null && !level.isClientSide) {
            ae2Component.onLoad();
        }
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (ae2Component != null) {
            ae2Component.onRemoved();
        }
    }
    
    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (ae2Component != null) {
            ae2Component.onClearRemoved();
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("usedChannels", usedChannels);
        tag.putString("tier", tier.name());
        
        if (ae2Component != null) {
            ae2Component.saveAdditional(tag, registries);
        }
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        usedChannels = tag.getInt("usedChannels");
        
        if (ae2Component != null) {
            ae2Component.loadAdditional(tag, registries);
        }
    }
    
    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        // Request AE2 handler if AE2 is loaded
        if (com.miniverse.modularinfrastructure.integration.ae2.ModAE2Integration.isAE2Loaded()) {
            return ImmutableList.of(com.miniverse.modularinfrastructure.integration.ae2.ModAE2Integration.AE2_NETWORK_BRIDGE_HANDLER);
        }
        return ImmutableList.of();
    }
    
    /**
     * Get the AE2 component if available
     */
    public IAE2DataConnector getAE2Component() {
        return ae2Component;
    }
    
    /**
     * Get the data tier of this connector
     */
    public DataConnectorBlock.DataTier getDataTier() {
        return tier;
    }
    
    /**
     * Override to provide dynamic max connections based on AE2 channel mode
     */
    protected int getMaxConnectionsInternal() {
        return getMaxChannels();
    }
}