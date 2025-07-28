package com.miniverse.modularinfrastructure.api.wire;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for wire types
 * Allows looking up wire types by their unique name
 */
public class WireTypeRegistry {
    private static final Map<String, WireType> REGISTRY = new HashMap<>();
    
    static {
        // Register all built-in wire types
        register(ModWireTypes.INTERNAL_CONNECTION);
        register(ModWireTypes.COPPER_LV);
        register(ModWireTypes.ELECTRUM);
        register(ModWireTypes.STEEL);
        register(ModWireTypes.DATA_CABLE);
        register(ModWireTypes.DENSE_CABLE);
        register(ModWireTypes.REDSTONE_WIRE);
        register(ModWireTypes.ROPE);
    }
    
    /**
     * Register a wire type
     */
    public static void register(WireType type) {
        REGISTRY.put(type.getUniqueName(), type);
    }
    
    /**
     * Get a wire type by its unique name
     */
    public static WireType get(String name) {
        return REGISTRY.get(name);
    }
    
    /**
     * Check if a wire type is registered
     */
    public static boolean contains(String name) {
        return REGISTRY.containsKey(name);
    }
}