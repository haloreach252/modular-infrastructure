package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.world.item.ItemStack;

/**
 * Wire type definitions for Modular Infrastructure
 * Based on concepts from Immersive Engineering's wire system
 * 
 * Original wire system concepts from Immersive Engineering by BluSunrize
 * Used under "Blu's License of Common Sense"
 */
public abstract class WireType {
    // Wire categories for compatibility grouping
    public static final String POWER_LV = "POWER_LV";
    public static final String POWER_MV = "POWER_MV";
    public static final String POWER_HV = "POWER_HV";
    public static final String DATA_BASIC = "DATA_BASIC";
    public static final String DATA_ADVANCED = "DATA_ADVANCED";
    public static final String REDSTONE = "REDSTONE";
    public static final String STRUCTURAL = "STRUCTURAL";

    private final String uniqueName;

    protected WireType(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getUniqueName() {
        return uniqueName;
    }
    
    /**
     * Get the unique ID for this wire type (used for network sync)
     */
    public String getId() {
        return "modularinfrastructure:" + uniqueName;
    }

    /**
     * Get the color of this wire type for rendering
     */
    public abstract int getColor();

    /**
     * Get the slack/sag amount for this wire type
     * Higher values = more sag
     */
    public abstract double getSlack();

    /**
     * Maximum connection distance in blocks
     */
    public abstract int getMaxLength();

    /**
     * Wire diameter for rendering
     */
    public abstract double getRenderDiameter();

    /**
     * Get the category this wire belongs to
     * Wires in different categories cannot share the same connector
     */
    public abstract String getCategory();

    /**
     * Get the wire coil item for this wire type
     */
    public abstract ItemStack getWireCoil();

    /**
     * Transfer capacity for this wire type (power, data channels, etc.)
     */
    public abstract int getTransferCapacity();
}