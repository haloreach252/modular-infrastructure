package com.miniverse.modularinfrastructure.integration.ae2;

import com.miniverse.modularinfrastructure.api.wires.Connection;
import com.miniverse.modularinfrastructure.api.wires.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.LocalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.IImmersiveConnectable;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler;
import com.miniverse.modularinfrastructure.blockentity.DataConnectorBlockEntity;
import com.miniverse.modularinfrastructure.common.wires.ModWireTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;

import java.util.*;

/**
 * Local network handler that creates virtual connections between AE2 grid nodes
 * connected by our data cables
 */
public class AE2NetworkBridgeHandler extends LocalNetworkHandler {
    
    // Track active bridge connections
    private final Map<ConnectionPair, IGridConnection> bridgeConnections = new HashMap<>();
    
    public AE2NetworkBridgeHandler(LocalWireNetwork localNet, GlobalWireNetwork globalNet) {
        super(localNet, globalNet);
    }
    
    @Override
    public LocalNetworkHandler merge(LocalNetworkHandler other) {
        if (other instanceof AE2NetworkBridgeHandler otherHandler) {
            // Merge bridge connections
            bridgeConnections.putAll(otherHandler.bridgeConnections);
        }
        return this;
    }
    
    @Override
    public void onConnectorLoaded(ConnectionPoint p, IImmersiveConnectable iic) {
        // Check for existing connections when a connector loads
        if (iic instanceof DataConnectorBlockEntity dataConnector && dataConnector.getAE2Component() != null) {
            for (Connection conn : localNet.getConnections(p)) {
                createBridgeIfPossible(conn);
            }
        }
    }
    
    @Override
    public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic) {
        // Nothing to do on unload - connections remain
    }
    
    @Override
    public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic) {
        // Remove any bridges connected to this position
        Iterator<Map.Entry<ConnectionPair, IGridConnection>> it = bridgeConnections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ConnectionPair, IGridConnection> entry = it.next();
            ConnectionPair pair = entry.getKey();
            if (pair.posA.equals(p) || pair.posB.equals(p)) {
                entry.getValue().destroy();
                it.remove();
            }
        }
    }
    
    @Override
    public void onConnectionAdded(Connection connection) {
        // Only handle data cable connections
        if (connection.type != ModWireTypes.DATA_CABLE && connection.type != ModWireTypes.DENSE_CABLE) {
            return;
        }
        
        // Try to create AE2 bridge if both ends are AE2 connectors
        createBridgeIfPossible(connection);
    }
    
    @Override
    public void onConnectionRemoved(Connection connection) {
        // Remove any bridge connections associated with this wire connection
        ConnectionPair pair = new ConnectionPair(connection.getEndA().position(), connection.getEndB().position());
        IGridConnection bridge = bridgeConnections.remove(pair);
        
        if (bridge != null) {
            bridge.destroy();
        }
    }
    
    private void createBridgeIfPossible(Connection connection) {
        if (globalNet == null) {
            return;
        }
        
        BlockPos posA = connection.getEndA().position();
        BlockPos posB = connection.getEndB().position();
        
        // Use globalNet to get connectable at positions
        IImmersiveConnectable connA = globalNet.getLocalNet(connection.getEndA()).getConnector(posA);
        IImmersiveConnectable connB = globalNet.getLocalNet(connection.getEndB()).getConnector(posB);
        
        // Check if both ends are data connectors with AE2 components
        if (!(connA instanceof DataConnectorBlockEntity dataA) || !(connB instanceof DataConnectorBlockEntity dataB)) {
            return;
        }
        
        IAE2DataConnector ae2A = dataA.getAE2Component();
        IAE2DataConnector ae2B = dataB.getAE2Component();
        
        if (!(ae2A instanceof AE2DataConnectorComponent compA) || !(ae2B instanceof AE2DataConnectorComponent compB)) {
            return;
        }
        
        // Get grid nodes from both AE2 components
        IGridNode nodeA = compA.getGridNode();
        IGridNode nodeB = compB.getGridNode();
        
        if (nodeA == null || nodeB == null) {
            // Nodes not ready yet, they'll connect when ready
            return;
        }
        
        // Check if nodes are already connected
        for (IGridConnection existingConn : nodeA.getConnections()) {
            if (existingConn.getOtherSide(nodeA) == nodeB) {
                // Already connected
                return;
            }
        }
        
        try {
            // Create a virtual connection between the nodes
            IGridConnection gridConnection = GridHelper.createConnection(nodeA, nodeB);
            
            // Store the connection for cleanup later
            ConnectionPair pair = new ConnectionPair(connection.getEndA().position(), connection.getEndB().position());
            bridgeConnections.put(pair, gridConnection);
            
        } catch (Exception e) {
            // Connection might fail if nodes are in same grid or other reasons
            // This is expected in some cases
        }
    }
    
    
    /**
     * Helper class to track connection pairs
     */
    private static class ConnectionPair {
        private final BlockPos posA;
        private final BlockPos posB;
        
        public ConnectionPair(BlockPos posA, BlockPos posB) {
            // Always store in consistent order for proper equals/hashCode
            if (posA.compareTo(posB) < 0) {
                this.posA = posA;
                this.posB = posB;
            } else {
                this.posA = posB;
                this.posB = posA;
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConnectionPair that = (ConnectionPair) o;
            return Objects.equals(posA, that.posA) && Objects.equals(posB, that.posB);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(posA, posB);
        }
    }
}