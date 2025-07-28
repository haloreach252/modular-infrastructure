package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of proxy provider that creates proxy connectors for unloaded chunks
 * 
 * Based on Immersive Engineering's proxy connector system
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public class DefaultProxyProvider implements IICProxyProvider {
    
    @Override
    public IImmersiveConnectable create(Level level, BlockPos pos) {
        return new ProxyConnector(pos);
    }
    
    /**
     * Simple proxy connector that handles connections while the real connector is unloaded
     */
    public static class ProxyConnector implements IImmersiveConnectable {
        private final BlockPos pos;
        
        public ProxyConnector(BlockPos pos) {
            this.pos = pos;
        }
        
        @Override
        public BlockPos getPosition() {
            return pos;
        }
        
        @Override
        public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) {
            // Default offset for proxy connectors
            return Vec3.ZERO;
        }
        
        @Override
        public Collection<ConnectionPoint> getConnectionPoints() {
            // Proxy connectors support a single connection point by default
            return List.of(new ConnectionPoint(pos, 0));
        }
        
        @Override
        public boolean canConnectCable(WireType type, ConnectionPoint target) {
            return true;
        }
        
        @Override
        public void connectCable(Connection connection, ConnectionPoint point) {
            // No-op for proxy
        }
        
        @Override
        public boolean isProxy() {
            return true;
        }
        
        @Override
        public void removeCable(Connection connection, ConnectionPoint point) {
            // No-op for proxy
        }
        
        @Override
        public Collection<Connection> getInternalConnections() {
            return Collections.emptyList();
        }
    }
}