package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.world.level.Level;

/**
 * Helper methods for connector block entities
 * 
 * Based on Immersive Engineering's ConnectorBlockEntityHelper
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public class ConnectorBlockEntityHelper {
    
    /**
     * Called when a connector is loaded (chunk load or placement)
     */
    public static void onChunkLoad(IImmersiveConnectable iic, Level world) {
        GlobalWireNetwork.getNetwork(world).onConnectorLoad(iic, world);
    }
    
    /**
     * Called when a connector is removed
     */
    public static void remove(Level world, IImmersiveConnectable iic) {
        GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(world);
        if (!world.isClientSide) {
            globalNet.removeAllConnectionsAt(iic, conn -> {});
        }
        globalNet.onConnectorUnload(iic);
    }
}