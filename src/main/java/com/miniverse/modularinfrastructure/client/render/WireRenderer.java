package com.miniverse.modularinfrastructure.client.render;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.wire.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent.SectionRenderingContext;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * Renders wires in the world using section-based rendering
 * 
 * Based heavily on Immersive Engineering's ConnectionRenderer
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = ModularInfrastructure.MODID, value = Dist.CLIENT, bus = Bus.GAME)
public class WireRenderer {
    private static final ResourceLocation WIRE_TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(ModularInfrastructure.MODID, "block/wire");
    private static final float WIRE_PIXEL_WIDTH = 4f / 16f; // 4 pixels wide
    
    private static TextureAtlasSprite getWireTexture() {
        return Minecraft.getInstance().getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS)
                .getSprite(WIRE_TEXTURE_LOCATION);
    }
    
    @SubscribeEvent
    public static void onSectionRender(AddSectionGeometryEvent event) {
        final BlockPos origin = event.getSectionOrigin();
        final SectionPos section = SectionPos.of(origin);
        
        // Add debug logging for every section being rendered
        ModularInfrastructure.LOGGER.debug("AddSectionGeometryEvent fired for section {}", section);
        
        final GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(event.getLevel());
        final List<WireCollisionData.ConnectionSegments> connectionParts = globalNet.getCollisionData().getWiresIn(section);
        
        if (connectionParts == null) {
            ModularInfrastructure.LOGGER.debug("getWiresIn returned null for section {}", section);
        } else if (connectionParts.isEmpty()) {
            ModularInfrastructure.LOGGER.debug("getWiresIn returned empty list for section {}", section);
        } else {
            ModularInfrastructure.LOGGER.info("Rendering {} wire segments in section {}", connectionParts.size(), section);
            event.addRenderer(context -> renderConnectionsInSection(origin, context, connectionParts));
        }
    }
    
    public static void renderConnectionsInSection(BlockPos sectionOrigin, AddSectionGeometryEvent.SectionRenderingContext context, 
                                                  List<WireCollisionData.ConnectionSegments> segments) {
        ModularInfrastructure.LOGGER.info("renderConnectionsInSection called with {} segments", segments.size());
        final VertexConsumer builder = context.getOrCreateChunkBuffer(RenderType.solid());
        final PoseStack transform = context.getPoseStack();
        
        for (WireCollisionData.ConnectionSegments connectionSegment : segments) {
            transform.pushPose();
            ConnectionPoint connectionOrigin = connectionSegment.connection().getEndA();
            BlockPos originPos = connectionOrigin.position();
            
            // Match IE's approach - translate to block position only
            // The catenary data already includes the connection offset
            transform.translate(
                originPos.getX() - sectionOrigin.getX(),
                originPos.getY() - sectionOrigin.getY(),
                originPos.getZ() - sectionOrigin.getZ()
            );
            ModularInfrastructure.LOGGER.info("Rendering connection from {} to {}", 
                connectionSegment.connection().getEndA().position(), 
                connectionSegment.connection().getEndB().position());
            renderSegments(builder, connectionSegment, context.getRegion(), transform);
            transform.popPose();
        }
    }
    
    /**
     * Render wire segments within a section
     */
    public static void renderSegments(VertexConsumer out, WireCollisionData.ConnectionSegments toRender,
                                      BlockAndTintGetter level, PoseStack transform) {
        Connection connection = toRender.connection();
        WireType wireType = connection.getWireType();
        int color = wireType.getColor();
        double radius = wireType.getRenderDiameter() / 2;
        
        // Extract RGB components
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        
        // Calculate lighting for the connection segments
        int light = 0xF000F0; // Full bright for now
        
        // Render segments from firstPointToRender to lastPointToRender
        ModularInfrastructure.LOGGER.info("Rendering segments from {} to {} (wire type: {}, color: {:#08x})", 
            toRender.firstPointToRender(), toRender.lastPointToRender(), wireType.getUniqueName(), color);
        for (int i = toRender.firstPointToRender(); i < toRender.lastPointToRender(); i++) {
            Vec3 start = connection.getCatenaryData().getRenderPoint(i);
            Vec3 end = connection.getCatenaryData().getRenderPoint(i + 1);
            ModularInfrastructure.LOGGER.debug("Segment {}: {} -> {}", i, start, end);
            
            renderWireSegment(transform, out, start, end, (float)radius, r, g, b, light);
        }
    }
    
    /**
     * Render a single wire connection (for non-section based rendering)
     */
    public static void renderConnection(Connection connection, PoseStack poseStack, MultiBufferSource bufferSource) {
        WireType wireType = connection.getWireType();
        float diameter = (float) wireType.getRenderDiameter();
        int color = wireType.getColor();
        
        // Extract RGB components
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        
        // For non-section based rendering, we would need a different approach
        // This method is not currently used since we're using section-based rendering
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        
        // Get catenary data
        Connection.CatenaryData catenary = connection.getCatenaryData();
        
        // Render wire segments
        for (int i = 0; i < Connection.RENDER_POINTS_PER_WIRE - 1; i++) {
            Vec3 start = catenary.getRenderPoint(i);
            Vec3 end = catenary.getRenderPoint(i + 1);
            
            renderWireSegment(poseStack, vertexConsumer, start, end, diameter, r, g, b, 0xF000F0);
        }
    }
    
    /**
     * Render a single segment of wire between two points using IE's approach
     */
    private static void renderWireSegment(PoseStack poseStack, VertexConsumer vertexConsumer, 
                                          Vec3 start, Vec3 end, float radius, 
                                          float r, float g, float b, int light) {
        Matrix4f matrix = poseStack.last().pose();
        
        // Get texture from block atlas
        TextureAtlasSprite texture = getWireTexture();
        float u1 = texture.getU0();
        float u2 = texture.getU1();
        float v1 = texture.getV0();
        float v2 = texture.getV1();
        
        // Calculate normals like IE does
        Vec3 delta = end.subtract(start);
        Vec3 horNormal;
        if (Math.abs(delta.x) < 0.05 && Math.abs(delta.z) < 0.05) {
            // Vertical wire
            horNormal = new Vec3(1, 0, 0);
        } else {
            // Horizontal component perpendicular to wire direction
            horNormal = new Vec3(-delta.z, 0, delta.x).normalize();
        }
        Vec3 verticalNormal = start.subtract(end).cross(horNormal).normalize();
        Vec3 horRadius = horNormal.scale(radius);
        Vec3 verticalRadius = verticalNormal.scale(-radius);
        
        // Render horizontal quad (both sides)
        renderBidirectionalQuad(vertexConsumer, matrix, start, end, horRadius, 
                               u1, u2, v1, v2, r, g, b, light, verticalNormal);
        
        // Render vertical quad (both sides)
        renderBidirectionalQuad(vertexConsumer, matrix, start, end, verticalRadius, 
                               u1, u2, v1, v2, r, g, b, light, horNormal);
    }
    
    /**
     * Render a quad facing both directions (like IE does)
     */
    private static void renderBidirectionalQuad(VertexConsumer vertexConsumer, Matrix4f matrix,
                                               Vec3 start, Vec3 end, Vec3 radius,
                                               float u1, float u2, float v1, float v2,
                                               float r, float g, float b, int light,
                                               Vec3 normal) {
        // Calculate vertices
        Vec3 v1Pos = start.add(radius);
        Vec3 v2Pos = end.add(radius);
        Vec3 v3Pos = end.subtract(radius);
        Vec3 v4Pos = start.subtract(radius);
        
        // Render front face
        float nx = (float)normal.x;
        float ny = (float)normal.y;
        float nz = (float)normal.z;
        
        vertexConsumer.addVertex(matrix, (float)v1Pos.x, (float)v1Pos.y, (float)v1Pos.z)
                      .setColor(r, g, b, 1f)
                      .setUv(u1, v1)
                      .setOverlay(OverlayTexture.NO_OVERLAY)
                      .setLight(light)
                      .setNormal(nx, ny, nz);
        
        vertexConsumer.addVertex(matrix, (float)v2Pos.x, (float)v2Pos.y, (float)v2Pos.z)
                      .setColor(r, g, b, 1f)
                      .setUv(u2, v1)
                      .setOverlay(OverlayTexture.NO_OVERLAY)
                      .setLight(light)
                      .setNormal(nx, ny, nz);
        
        vertexConsumer.addVertex(matrix, (float)v3Pos.x, (float)v3Pos.y, (float)v3Pos.z)
                      .setColor(r, g, b, 1f)
                      .setUv(u2, v2)
                      .setOverlay(OverlayTexture.NO_OVERLAY)
                      .setLight(light)
                      .setNormal(nx, ny, nz);
        
        vertexConsumer.addVertex(matrix, (float)v4Pos.x, (float)v4Pos.y, (float)v4Pos.z)
                      .setColor(r, g, b, 1f)
                      .setUv(u1, v2)
                      .setOverlay(OverlayTexture.NO_OVERLAY)
                      .setLight(light)
                      .setNormal(nx, ny, nz);
        
        // Render back face (reverse order)
        vertexConsumer.addVertex(matrix, (float)v4Pos.x, (float)v4Pos.y, (float)v4Pos.z)
                      .setColor(r, g, b, 1f)
                      .setUv(u1, v2)
                      .setOverlay(OverlayTexture.NO_OVERLAY)
                      .setLight(light)
                      .setNormal(-nx, -ny, -nz);
        
        vertexConsumer.addVertex(matrix, (float)v3Pos.x, (float)v3Pos.y, (float)v3Pos.z)
                      .setColor(r, g, b, 1f)
                      .setUv(u2, v2)
                      .setOverlay(OverlayTexture.NO_OVERLAY)
                      .setLight(light)
                      .setNormal(-nx, -ny, -nz);
        
        vertexConsumer.addVertex(matrix, (float)v2Pos.x, (float)v2Pos.y, (float)v2Pos.z)
                      .setColor(r, g, b, 1f)
                      .setUv(u2, v1)
                      .setOverlay(OverlayTexture.NO_OVERLAY)
                      .setLight(light)
                      .setNormal(-nx, -ny, -nz);
        
        vertexConsumer.addVertex(matrix, (float)v1Pos.x, (float)v1Pos.y, (float)v1Pos.z)
                      .setColor(r, g, b, 1f)
                      .setUv(u1, v1)
                      .setOverlay(OverlayTexture.NO_OVERLAY)
                      .setLight(light)
                      .setNormal(-nx, -ny, -nz);
    }
}