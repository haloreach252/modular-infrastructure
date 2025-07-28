package com.miniverse.modularinfrastructure.api.wire;

/**
 * Interface for managing wire network synchronization between client and server
 * 
 * Based on Immersive Engineering's IWireSyncManager
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public interface IWireSyncManager {
    /**
     * Called when a connection is added to the network
     */
    void onConnectionAdded(Connection c);

    /**
     * Called when a connection is removed from the network
     */
    void onConnectionRemoved(Connection c);

    /**
     * Called when a connection's endpoints have changed
     */
    void onConnectionEndpointsChanged(Connection c);
}