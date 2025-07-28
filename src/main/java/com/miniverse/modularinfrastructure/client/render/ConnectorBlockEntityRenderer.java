package com.miniverse.modularinfrastructure.client.render;

import com.miniverse.modularinfrastructure.api.wire.*;
import com.miniverse.modularinfrastructure.blockentity.ConnectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;

/**
 * Renders wires connected to connector blocks
 */
public class ConnectorBlockEntityRenderer implements BlockEntityRenderer<ConnectorBlockEntity> {
    
    public ConnectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    @Override
    public void render(ConnectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        
        // Get the wire network
        GlobalWireNetwork network = GlobalWireNetwork.getNetwork(level);
        
        // Get the connection point for this connector
        ConnectionPoint cp = new ConnectionPoint(blockEntity.getBlockPos(), 0);
        LocalWireNetwork localNet = network.getNullableLocalNet(cp);
        
        if (localNet == null) {
            return;
        }
        
        // Render all connections from this connector
        for (Connection connection : localNet.getConnections(cp)) {
            // Only render from the "positive" end to avoid double rendering
            if (connection.isPositiveEnd(cp)) {
                WireRenderer.renderConnection(connection, poseStack, bufferSource);
            }
        }
    }
    
    @Override
    public int getViewDistance() {
        // Wires should be visible from far away
        return 256;
    }
    
    @Override
    public boolean shouldRenderOffScreen(ConnectorBlockEntity blockEntity) {
        // Render wires even if the connector is off-screen
        return true;
    }
}