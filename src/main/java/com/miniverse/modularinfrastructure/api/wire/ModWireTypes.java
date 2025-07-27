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
    
    // Power wires
    public static final WireType COPPER = new PowerWireType("copper", WireType.POWER_LV, 0xB87333, 1.05, 16, 0.0625, 256);
    public static final WireType ELECTRUM = new PowerWireType("electrum", WireType.POWER_MV, 0xFFD700, 1.025, 32, 0.0625, 1024);
    public static final WireType STEEL = new PowerWireType("steel", WireType.POWER_HV, 0x7A7A7A, 1.0, 48, 0.0625, 4096);
    
    // Data wires
    public static final WireType DATA_CABLE = new DataWireType("data_cable", WireType.DATA_BASIC, 0x4169E1, 1.025, 24, 0.0625, 8);
    public static final WireType DENSE_CABLE = new DataWireType("dense_cable", WireType.DATA_ADVANCED, 0x191970, 1.025, 24, 0.0875, 32);
    
    // Utility wires
    public static final WireType REDSTONE_WIRE = new UtilityWireType("redstone_wire", WireType.REDSTONE, 0xCC0000, 1.025, 32, 0.0625);
    public static final WireType ROPE = new UtilityWireType("rope", WireType.STRUCTURAL, 0x8B7355, 1.05, 64, 0.125);
    
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
            // TODO: Return actual wire coil items when implemented
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
            // TODO: Return actual wire coil items when implemented
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
            // TODO: Return actual wire coil items when implemented
            return ItemStack.EMPTY;
        }
        
        @Override
        public int getTransferCapacity() {
            return 0; // Utility wires don't transfer power/data
        }
    }
}