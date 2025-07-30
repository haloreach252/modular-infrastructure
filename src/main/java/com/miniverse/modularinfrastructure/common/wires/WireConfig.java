package com.miniverse.modularinfrastructure.common.wires;

/**
 * Centralized configuration for all wire properties
 * Edit this file to quickly adjust wire visuals and behavior
 */
public class WireConfig {
    
    // General Wire Settings
    public static class General {
        public static final boolean ENABLE_WIRE_DAMAGE = true;
        public static final boolean BLOCKS_BREAK_WIRES = true;
    }
    
    // Power Wire Configurations
    public static class PowerWires {
        // Copper/LV Wire
        public static final int COPPER_COLOR = 0xd4804a;
        public static final double COPPER_THICKNESS = 0.03125;  // Thin wire
        public static final int COPPER_MAX_LENGTH = 64;
        public static final double COPPER_SAG = 1.005;  // 0.5% sag
        public static final int COPPER_TRANSFER_RATE = 2048;
        public static final float COPPER_DAMAGE_RADIUS = 0.05f;
        
        // Electrum/MV Wire  
        public static final int ELECTRUM_COLOR = 0xeda045;
        public static final double ELECTRUM_THICKNESS = 0.03125;  // Thin wire
        public static final int ELECTRUM_MAX_LENGTH = 128;
        public static final double ELECTRUM_SAG = 1.007;  // 0.7% sag
        public static final int ELECTRUM_TRANSFER_RATE = 8192;
        public static final float ELECTRUM_DAMAGE_RADIUS = 0.1f;
        
        // Steel/HV Wire
        public static final int STEEL_COLOR = 0x6e6e6e;
        public static final double STEEL_THICKNESS = 0.0625;  // Thick wire
        public static final int STEEL_MAX_LENGTH = 256;
        public static final double STEEL_SAG = 1.010;  // 1% sag - heavier wire
        public static final int STEEL_TRANSFER_RATE = 32768;
        public static final float STEEL_DAMAGE_RADIUS = 0.3f;
    }
    
    // Data Wire Configurations
    public static class DataWires {
        // Basic Data Cable
        public static final int DATA_COLOR = 0x24c6e8;
        public static final double DATA_THICKNESS = 0.03125;  // Thin wire
        public static final int DATA_MAX_LENGTH = 32;
        public static final double DATA_SAG = 1.003;  // 0.3% sag - light cable
        
        // Dense Data Cable
        public static final int DENSE_COLOR = 0x1a9dc4;
        public static final double DENSE_THICKNESS = 0.0625;  // Thick wire
        public static final int DENSE_MAX_LENGTH = 64;
        public static final double DENSE_SAG = 1.008;  // 0.8% sag - heavier cable
    }
    
    // Utility Wire Configurations
    public static class UtilityWires {
        // Redstone Wire
        public static final int REDSTONE_COLOR = 0xff0000;
        public static final double REDSTONE_THICKNESS = 0.03125;  // Thin wire
        public static final int REDSTONE_MAX_LENGTH = 32;
        public static final double REDSTONE_SAG = 1.004;  // 0.4% sag
        
        // Structural Rope
        public static final int ROPE_COLOR = 0x967969;
        public static final double ROPE_THICKNESS = 0.0625;  // Thick wire
        public static final int ROPE_MAX_LENGTH = 32;
        public static final double ROPE_SAG = 1.015;  // 1.5% sag - rope is heavy and flexible
    }
    
    // Connector Offset Configurations
    public static class ConnectorOffsets {
        // Higher values = lower on the block

        // Power Connectors
        public static final double LV_CONNECTOR_LENGTH = 0.65;
        public static final double MV_CONNECTOR_LENGTH = 0.55;
        public static final double HV_CONNECTOR_LENGTH = 0.3;
        
        // Data Connectors
        public static final double DATA_BASIC_CONNECTOR_LENGTH = 0.7;
        public static final double DATA_ADVANCED_CONNECTOR_LENGTH = 0.7;
        
        // Utility Connectors
        public static final double REDSTONE_CONNECTOR_LENGTH = 0.8;
        public static final double STRUCTURAL_CONNECTOR_LENGTH = 0.75;

        // Misc
        public static final double CIRCUIT_BREAKER_LENGTH = 0.85;
    }
    
    // Future: Per-connection sag configuration
    // This will be used when we implement player-configurable sag
    public static class SagPresets {
        public static final double TIGHT = 1.002;    // 0.2% - Very tight wire
        public static final double NORMAL = 1.005;   // 0.5% - Standard sag
        public static final double LOOSE = 1.010;    // 1.0% - Noticeable sag
        public static final double SAGGY = 1.020;    // 2.0% - Very saggy wire
        public static final double EXTREME = 1.030;   // 3.0% - Extreme sag
    }
}