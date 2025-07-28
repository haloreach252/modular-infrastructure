package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Manages wire networks and handles world events
 * 
 * Inspired by Immersive Engineering's wire network management
 * Original wire system concepts from Immersive Engineering by BluSunrize
 */
@EventBusSubscriber(modid = "modularinfrastructure")
public class WireNetworkManager {
    
    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide()) {
            GlobalWireNetwork network = GlobalWireNetwork.getNetwork(event.getLevel());
            network.update(event.getLevel());
        }
    }
    
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            LevelChunk chunk = (LevelChunk) event.getChunk();
            GlobalWireNetwork network = GlobalWireNetwork.getNetwork(level);
            
            // Find all connectors in the chunk and register them
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be instanceof IImmersiveConnectable connector) {
                    // The connector will register itself via onLoad()
                    // We don't need to do it here to avoid double registration
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            LevelChunk chunk = (LevelChunk) event.getChunk();
            GlobalWireNetwork network = GlobalWireNetwork.getNetwork(level);
            
            // Unload all connectors in the chunk
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be instanceof IImmersiveConnectable connector) {
                    network.onConnectorUnload(connector);
                }
            }
        }
    }
    
    /**
     * Add a connection between two connectors
     */
    public static boolean addConnection(Level level, BlockPos posA, BlockPos posB, WireType wireType) {
        if (level.isClientSide()) {
            return false;
        }
        
        BlockEntity beA = level.getBlockEntity(posA);
        BlockEntity beB = level.getBlockEntity(posB);
        
        if (!(beA instanceof IImmersiveConnectable connA) || !(beB instanceof IImmersiveConnectable connB)) {
            return false;
        }
        
        // For now, assume single connection point per connector
        ConnectionPoint cpA = new ConnectionPoint(posA, 0);
        ConnectionPoint cpB = new ConnectionPoint(posB, 0);
        
        // Check if connection is allowed
        if (!connA.canConnectCable(wireType, cpB) || !connB.canConnectCable(wireType, cpA)) {
            return false;
        }
        
        // Check distance
        double distance = Math.sqrt(posA.distSqr(posB));
        if (distance > wireType.getMaxLength()) {
            return false;
        }
        
        // Create the connection
        GlobalWireNetwork network = GlobalWireNetwork.getNetwork(level);
        Vec3 offsetA = connA.getConnectionOffset(cpA, cpB, wireType);
        Vec3 offsetB = connB.getConnectionOffset(cpB, cpA, wireType);
        
        Connection connection = new Connection(wireType, cpA, cpB, offsetA, offsetB);
        network.addConnection(connection);
        
        return true;
    }
    
    /**
     * Remove a connection between two connectors
     */
    public static void removeConnection(Level level, BlockPos posA, BlockPos posB) {
        if (level.isClientSide()) {
            return;
        }
        
        GlobalWireNetwork network = GlobalWireNetwork.getNetwork(level);
        ConnectionPoint cpA = new ConnectionPoint(posA, 0);
        
        network.removeAllConnectionsAt(cpA, connection -> {
            ConnectionPoint other = connection.getOtherEnd(cpA);
            if (other.position().equals(posB)) {
                // This is the connection we want to remove
            }
        });
    }
    
    /**
     * Remove all connections from a connector
     */
    public static void removeAllConnections(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        
        GlobalWireNetwork network = GlobalWireNetwork.getNetwork(level);
        BlockEntity be = level.getBlockEntity(pos);
        
        if (be instanceof IImmersiveConnectable connector) {
            network.removeConnector(connector);
        }
    }
}