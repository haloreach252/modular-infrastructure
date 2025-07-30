package com.miniverse.modularinfrastructure.integration.ae2;

import com.miniverse.modularinfrastructure.api.wires.Connection;
import com.miniverse.modularinfrastructure.api.wires.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wires.WireType;
import com.miniverse.modularinfrastructure.blockentity.DataConnectorBlockEntity;
import com.miniverse.modularinfrastructure.common.wires.ModWireTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import appeng.api.networking.IGridNode;

/**
 * Specialized renderer for data cables that shows channel usage
 */
public class AE2DataCableRenderer {
    
    /**
     * Get the color for a data cable based on its channel usage
     * Returns standard colors for different states:
     * - No channels/offline: Dark gray
     * - Channels in use: Light blue for basic, cyan for dense
     * - Near capacity: Yellow
     * - At capacity: Red
     */
    public static int getDataCableColor(Connection connection, Level level) {
        WireType wireType = connection.type;
        
        // Only modify color for data cables
        if (wireType != ModWireTypes.DATA_CABLE && wireType != ModWireTypes.DENSE_CABLE) {
            return connection.type.getColour(connection);
        }
        
        // Check if AE2 is loaded
        if (!ModAE2Integration.isAE2Loaded()) {
            return connection.type.getColour(connection);
        }
        
        // Get channel usage from endpoints
        int maxChannels = getMaxChannelsForWireType(wireType);
        int usedChannels = getUsedChannels(connection, level);
        
        // Determine color based on usage
        if (usedChannels == 0) {
            // No channels - dark gray
            return 0x404040;
        } else if (usedChannels >= maxChannels) {
            // At capacity - red
            return 0xFF0000;
        } else if (usedChannels >= maxChannels * 0.75) {
            // Near capacity (75%+) - yellow
            return 0xFFFF00;
        } else {
            // Normal operation - use default color
            return connection.type.getColour(connection);
        }
    }
    
    /**
     * Get the number of channels being used by this connection
     */
    private static int getUsedChannels(Connection connection, Level level) {
        // Check both endpoints for AE2 data connectors
        int channelsA = getChannelsAtPoint(connection.getEndA(), level);
        int channelsB = getChannelsAtPoint(connection.getEndB(), level);
        
        // Use the maximum of both ends (channels flow through the cable)
        return Math.max(channelsA, channelsB);
    }
    
    /**
     * Get channel count at a specific connection point
     */
    private static int getChannelsAtPoint(ConnectionPoint point, Level level) {
        BlockPos pos = point.position();
        BlockEntity be = level.getBlockEntity(pos);
        
        if (be instanceof DataConnectorBlockEntity dataConnector) {
            IAE2DataConnector ae2Component = dataConnector.getAE2Component();
            if (ae2Component instanceof AE2DataConnectorComponent component) {
                IGridNode node = component.getGridNode();
                
                if (node != null && node.isActive()) {
                    return node.getUsedChannels();
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Check if we should render channel indicators (small lights) on the cable
     */
    public static boolean shouldRenderChannelIndicators(Connection connection) {
        WireType wireType = connection.type;
        return (wireType == ModWireTypes.DATA_CABLE || wireType == ModWireTypes.DENSE_CABLE) && 
               ModAE2Integration.isAE2Loaded();
    }
    
    /**
     * Get the maximum channels for a wire type
     */
    private static int getMaxChannelsForWireType(WireType wireType) {
        if (wireType == ModWireTypes.DENSE_CABLE) {
            return 32;
        } else if (wireType == ModWireTypes.DATA_CABLE) {
            return 8;
        }
        return 0;
    }
    
    /**
     * Get positions for channel indicator lights along the cable
     * Returns positions as interpolation values (0.0 to 1.0) along the cable
     */
    public static float[] getChannelIndicatorPositions(Connection connection, Level level) {
        int usedChannels = getUsedChannels(connection, level);
        if (usedChannels == 0) {
            return new float[0];
        }
        
        // Show indicators at regular intervals
        // Basic cable: up to 8 indicators
        // Dense cable: show fewer indicators but brighter
        WireType wireType = connection.type;
        int maxIndicators = wireType == ModWireTypes.DENSE_CABLE ? 4 : 8;
        int indicators = Math.min(usedChannels, maxIndicators);
        
        float[] positions = new float[indicators];
        for (int i = 0; i < indicators; i++) {
            // Distribute evenly along the cable
            positions[i] = (i + 1) / (float)(indicators + 1);
        }
        
        return positions;
    }
}