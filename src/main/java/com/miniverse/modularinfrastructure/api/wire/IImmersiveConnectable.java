package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

/**
 * Interface for blocks that can participate in the wire network
 * 
 * Inspired by Immersive Engineering's IImmersiveConnectable
 * Original wire system concepts from Immersive Engineering by BluSunrize
 */
public interface IImmersiveConnectable {
    
    /**
     * Get the block position of this connector
     */
    BlockPos getPosition();
    
    /**
     * Get all connection points on this connector
     * Most connectors will have just one (index 0)
     */
    Collection<ConnectionPoint> getConnectionPoints();
    
    /**
     * Get the offset for wire connections at the given connection point
     * This determines where the wire visually connects to the block
     */
    Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type);
    
    /**
     * Check if this connector can accept a connection of the given wire type
     */
    boolean canConnectCable(WireType type, ConnectionPoint target);
    
    /**
     * Called when a cable is connected to this connector
     */
    void connectCable(Connection connection, ConnectionPoint point);
    
    /**
     * Called when a cable is removed from this connector
     */
    void removeCable(Connection connection, ConnectionPoint point);
    
    /**
     * Get all internal connections within this block
     * Used for multi-terminal blocks
     */
    Collection<Connection> getInternalConnections();
    
    /**
     * Check if this is a proxy connector (unloaded chunk placeholder)
     */
    default boolean isProxy() {
        return false;
    }
}