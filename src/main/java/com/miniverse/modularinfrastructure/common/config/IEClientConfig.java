package com.miniverse.modularinfrastructure.common.config;

import com.miniverse.modularinfrastructure.common.wires.IEWireTypes;
import java.util.EnumMap;
import java.util.Map;

public class IEClientConfig {
    public static final IEClientConfig INSTANCE = new IEClientConfig();
    
    // Maximum number of wires to render at once
    public int maxWireRenderCount = 256;
    
    // Wire render quality (segments per wire)
    public int wireRenderQuality = 16;
    
    // Wire colors map (RGB values packed as integers)
    public static final Map<IEWireTypes.IEWireType, Integer> wireColors = new EnumMap<>(IEWireTypes.IEWireType.class);
    
    static {
        // Initialize wire colors map with default colors matching Immersive Engineering
        wireColors.put(IEWireTypes.IEWireType.COPPER, 0xb36c3f);
        wireColors.put(IEWireTypes.IEWireType.ELECTRUM, 0xded38a);
        wireColors.put(IEWireTypes.IEWireType.STEEL, 0x7a7a7a);
        wireColors.put(IEWireTypes.IEWireType.STRUCTURE_ROPE, 0x967e6d);
        wireColors.put(IEWireTypes.IEWireType.STRUCTURE_STEEL, 0x6f6f6f);
        wireColors.put(IEWireTypes.IEWireType.REDSTONE, 0xff2f2f);
        wireColors.put(IEWireTypes.IEWireType.COPPER_INSULATED, 0x4a4a4a);
        wireColors.put(IEWireTypes.IEWireType.ELECTRUM_INSULATED, 0x4a4a4a);
    }
}