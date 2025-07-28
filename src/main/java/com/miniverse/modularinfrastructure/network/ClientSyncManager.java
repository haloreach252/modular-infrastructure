package com.miniverse.modularinfrastructure.network;

import com.miniverse.modularinfrastructure.api.wire.Connection;
import com.miniverse.modularinfrastructure.api.wire.IWireSyncManager;

/**
 * Client-side sync manager that doesn't send network packets
 * 
 * Based on Immersive Engineering's client-side wire handling
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public class ClientSyncManager implements IWireSyncManager {
    
    @Override
    public void onConnectionAdded(Connection c) {
        // No-op on client - connections are received from server
    }
    
    @Override
    public void onConnectionRemoved(Connection c) {
        // No-op on client - connections are received from server
    }
    
    @Override
    public void onConnectionEndpointsChanged(Connection c) {
        // No-op on client - connections are received from server
    }
}