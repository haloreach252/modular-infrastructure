package com.miniverse.modularinfrastructure.integration.ae2;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * Interface for AE2 integration components that can be attached to data connectors
 * This allows the data connector to remain functional without AE2
 */
public interface IAE2DataConnector {
    
    /**
     * Called when the block entity is loaded
     */
    void onLoad();
    
    /**
     * Called when the block entity is removed
     */
    void onRemoved();
    
    /**
     * Called when the block entity is cleared from removal
     */
    void onClearRemoved();
    
    /**
     * Save AE2-specific data
     */
    void saveAdditional(CompoundTag tag, HolderLookup.Provider registries);
    
    /**
     * Load AE2-specific data
     */
    void loadAdditional(CompoundTag tag, HolderLookup.Provider registries);
}