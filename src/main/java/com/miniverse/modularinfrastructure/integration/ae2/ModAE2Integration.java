package com.miniverse.modularinfrastructure.integration.ae2;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.ILocalHandlerConstructor;
import com.miniverse.modularinfrastructure.blockentity.DataConnectorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

/**
 * Handles integration with Applied Energistics 2
 * Only loads if AE2 is present
 */
public class ModAE2Integration {
    
    private static final String AE2_MODID = "ae2";
    public static final ResourceLocation AE2_NETWORK_BRIDGE_HANDLER = ResourceLocation.fromNamespaceAndPath(ModularInfrastructure.MOD_ID, "ae2_network_bridge");
    
    // Local handler constructor for the network bridge
    private static final ILocalHandlerConstructor AE2_BRIDGE_CONSTRUCTOR = (localNet, globalNet) -> new AE2NetworkBridgeHandler(localNet, globalNet);
    
    /**
     * Register content (blocks, items, etc) - called during mod construction
     * This must happen early, before RegisterEvent
     */
    public static void registerContent() {
        // No separate AE2 blocks/items to register
        // The existing data connectors will be AE2-aware when AE2 is present
    }
    
    /**
     * Initialize handlers and network connections - called during common setup
     */
    public static void initHandlers() {
        if (!isAE2Loaded()) {
            ModularInfrastructure.LOGGER.info("AE2 not found, skipping AE2 integration handlers");
            return;
        }
        
        ModularInfrastructure.LOGGER.info("Initializing AE2 integration handlers");
        
        // Register the network bridge handler
        com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler.register(
            AE2_NETWORK_BRIDGE_HANDLER, AE2_BRIDGE_CONSTRUCTOR
        );
        
        ModularInfrastructure.LOGGER.info("AE2 integration handlers initialized successfully");
    }
    
    /**
     * Check if AE2 is loaded
     */
    public static boolean isAE2Loaded() {
        return ModList.get().isLoaded(AE2_MODID);
    }
    
    /**
     * Create an AE2 component for a data connector
     */
    public static IAE2DataConnector createAE2Component(DataConnectorBlockEntity host) {
        if (!isAE2Loaded()) {
            return null;
        }
        return new AE2DataConnectorComponent(host);
    }
}