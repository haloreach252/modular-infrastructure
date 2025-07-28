package com.miniverse.modularinfrastructure.api.wire;

import com.miniverse.modularinfrastructure.ModItems;
import net.minecraft.world.item.ItemStack;

/**
 * Wire type implementations for Modular Infrastructure
 * 
 * Wire rendering and physics concepts adapted from Immersive Engineering by BluSunrize
 * Used under "Blu's License of Common Sense"
 */
public class ModWireTypes {
    
    // Internal connection type (for multi-terminal blocks)
    public static final WireType INTERNAL_CONNECTION = new InternalWireType();
    
    // Power wires - IE-style diameters
    public static final WireType COPPER_LV = new PowerWireType("copper", WireType.POWER_LV, 0xB87333, 1.05, 16, 0.03125, 256); // 0.5 pixels
    public static final WireType ELECTRUM = new PowerWireType("electrum", WireType.POWER_MV, 0xFFD700, 1.025, 32, 0.03125, 1024); // 0.5 pixels
    public static final WireType STEEL = new PowerWireType("steel", WireType.POWER_HV, 0x7A7A7A, 1.0, 48, 0.0625, 4096); // 1 pixel
    
    // Data wires - slightly thicker than power for visibility
    public static final WireType DATA_CABLE = new DataWireType("data_cable", WireType.DATA_BASIC, 0x4169E1, 1.025, 24, 0.0625, 8); // 1 pixel
    public static final WireType DENSE_CABLE = new DataWireType("dense_cable", WireType.DATA_ADVANCED, 0x191970, 1.025, 24, 0.09375, 32); // 1.5 pixels
    
    // Utility wires
    public static final WireType REDSTONE_WIRE = new UtilityWireType("redstone_wire", WireType.REDSTONE, 0xCC0000, 1.025, 32, 0.03125); // 0.5 pixels
    public static final WireType ROPE = new UtilityWireType("rope", WireType.STRUCTURAL, 0x8B7355, 1.05, 64, 0.0625); // 1 pixel
    
    // Registry for wire types - used for network sync
    private static final java.util.Map<String, WireType> WIRE_TYPE_REGISTRY = new java.util.HashMap<>();
    
    static {
        // Register all wire types
        WIRE_TYPE_REGISTRY.put(INTERNAL_CONNECTION.getId(), INTERNAL_CONNECTION);
        WIRE_TYPE_REGISTRY.put(COPPER_LV.getId(), COPPER_LV);
        WIRE_TYPE_REGISTRY.put(ELECTRUM.getId(), ELECTRUM);
        WIRE_TYPE_REGISTRY.put(STEEL.getId(), STEEL);
        WIRE_TYPE_REGISTRY.put(DATA_CABLE.getId(), DATA_CABLE);
        WIRE_TYPE_REGISTRY.put(DENSE_CABLE.getId(), DENSE_CABLE);
        WIRE_TYPE_REGISTRY.put(REDSTONE_WIRE.getId(), REDSTONE_WIRE);
        WIRE_TYPE_REGISTRY.put(ROPE.getId(), ROPE);
    }
    
    /**
     * Get a wire type by its ID
     * @param id The wire type ID
     * @return The wire type, or null if not found
     */
    public static WireType getWireType(String id) {
        return WIRE_TYPE_REGISTRY.get(id);
    }
    
    static class PowerWireType extends WireType {
        private final int color;
        private final double slack;
        private final int maxLength;
        private final double diameter;
        private final int transferCapacity;
        
        PowerWireType(String name, String category, int color, double slack, int maxLength, double diameter, int transferCapacity) {
            super(name);
            this.color = color;
            this.slack = slack;
            this.maxLength = maxLength;
            this.diameter = diameter;
            this.transferCapacity = transferCapacity;
        }
        
        @Override
        public int getColor() {
            return color;
        }
        
        @Override
        public double getSlack() {
            return slack;
        }
        
        @Override
        public int getMaxLength() {
            return maxLength;
        }
        
        @Override
        public double getRenderDiameter() {
            return diameter;
        }
        
        @Override
        public String getCategory() {
            return getUniqueName().contains("copper") ? POWER_LV :
                   getUniqueName().contains("electrum") ? POWER_MV : POWER_HV;
        }
        
        @Override
        public ItemStack getWireCoil() {
            // Import is avoided to prevent circular dependency
            // These will be set properly when items are registered
            return ItemStack.EMPTY;
        }
        
        @Override
        public int getTransferCapacity() {
            return transferCapacity;
        }
    }
    
    static class DataWireType extends WireType {
        private final int color;
        private final double slack;
        private final int maxLength;
        private final double diameter;
        private final int channels;
        
        DataWireType(String name, String category, int color, double slack, int maxLength, double diameter, int channels) {
            super(name);
            this.color = color;
            this.slack = slack;
            this.maxLength = maxLength;
            this.diameter = diameter;
            this.channels = channels;
        }
        
        @Override
        public int getColor() {
            return color;
        }
        
        @Override
        public double getSlack() {
            return slack;
        }
        
        @Override
        public int getMaxLength() {
            return maxLength;
        }
        
        @Override
        public double getRenderDiameter() {
            return diameter;
        }
        
        @Override
        public String getCategory() {
            return channels > 8 ? DATA_ADVANCED : DATA_BASIC;
        }
        
        @Override
        public ItemStack getWireCoil() {
            // Import is avoided to prevent circular dependency
            // These will be set properly when items are registered
            return ItemStack.EMPTY;
        }
        
        @Override
        public int getTransferCapacity() {
            return channels;
        }
    }
    
    static class UtilityWireType extends WireType {
        private final int color;
        private final double slack;
        private final int maxLength;
        private final double diameter;
        
        UtilityWireType(String name, String category, int color, double slack, int maxLength, double diameter) {
            super(name);
            this.color = color;
            this.slack = slack;
            this.maxLength = maxLength;
            this.diameter = diameter;
        }
        
        @Override
        public int getColor() {
            return color;
        }
        
        @Override
        public double getSlack() {
            return slack;
        }
        
        @Override
        public int getMaxLength() {
            return maxLength;
        }
        
        @Override
        public double getRenderDiameter() {
            return diameter;
        }
        
        @Override
        public String getCategory() {
            return getUniqueName().contains("redstone") ? REDSTONE : STRUCTURAL;
        }
        
        @Override
        public ItemStack getWireCoil() {
            // Import is avoided to prevent circular dependency
            // These will be set properly when items are registered
            return ItemStack.EMPTY;
        }
        
        @Override
        public int getTransferCapacity() {
            return 0; // Utility wires don't transfer power/data
        }
    }
    
    static class InternalWireType extends WireType {
        InternalWireType() {
            super("internal");
        }
        
        @Override
        public int getColor() {
            return 0x000000; // Black, but won't be rendered
        }
        
        @Override
        public double getSlack() {
            return 1.0; // No slack for internal connections
        }
        
        @Override
        public int getMaxLength() {
            return 0; // Internal only
        }
        
        @Override
        public double getRenderDiameter() {
            return 0; // Not rendered
        }
        
        @Override
        public String getCategory() {
            return "INTERNAL";
        }
        
        @Override
        public ItemStack getWireCoil() {
            return ItemStack.EMPTY; // No item representation
        }
        
        @Override
        public int getTransferCapacity() {
            return Integer.MAX_VALUE; // Unlimited for internal connections
        }
    }
}