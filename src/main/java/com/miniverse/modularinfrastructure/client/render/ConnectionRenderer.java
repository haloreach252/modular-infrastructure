// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.client.render;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.utils.ResettableLazy;
import com.miniverse.modularinfrastructure.api.wires.Connection;
import com.miniverse.modularinfrastructure.api.wires.Connection.CatenaryData;
import com.miniverse.modularinfrastructure.api.wires.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.WireCollisionData.ConnectionSegments;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent.SectionRenderingContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@EventBusSubscriber(value = Dist.CLIENT, modid = ModularInfrastructure.MOD_ID, bus = Bus.GAME)
public class ConnectionRenderer implements ResourceManagerReloadListener
{
	private static final LoadingCache<SectionKey, List<RenderedSegment>> SEGMENT_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(120, TimeUnit.SECONDS)
			.build(CacheLoader.from(ConnectionRenderer::renderSectionForCache));
	private static final ResettableLazy<TextureAtlasSprite> WIRE_TEXTURE = new ResettableLazy<>(
			() -> Minecraft.getInstance().getModelManager()
					.getAtlas(InventoryMenu.BLOCK_ATLAS)
					.getSprite(ResourceLocation.fromNamespaceAndPath(ModularInfrastructure.MOD_ID, "block/wire"))
	);

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager pResourceManager)
	{
		WIRE_TEXTURE.reset();
		resetCache();
	}

	public static void resetCache()
	{
		SEGMENT_CACHE.invalidateAll();
	}

	@SubscribeEvent
	public static void onSectionRender(AddSectionGeometryEvent ev)
	{
		final BlockPos origin = ev.getSectionOrigin();
		final SectionPos section = SectionPos.of(origin);
		final GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(ev.getLevel());
		final List<ConnectionSegments> connectionParts = globalNet.getCollisionData().getWiresIn(section);
		if(connectionParts!=null&&!connectionParts.isEmpty())
			ev.addRenderer(context -> renderConnectionsInSection(origin, context, connectionParts));
	}

	public static void renderConnectionsInSection(
			BlockPos sectionOrigin, SectionRenderingContext context, List<ConnectionSegments> segments
	)
	{
		final VertexConsumer builder = context.getOrCreateChunkBuffer(RenderType.solid());
		final PoseStack transform = context.getPoseStack();
		for(ConnectionSegments connection : segments)
		{
			transform.pushPose();
			ConnectionPoint connectionOrigin = connection.connection().getEndA();
			transform.translate(
					connectionOrigin.getX()-sectionOrigin.getX(),
					connectionOrigin.getY()-sectionOrigin.getY(),
					connectionOrigin.getZ()-sectionOrigin.getZ()
			);
			renderSegments(builder, connection, context.getRegion(), transform);
			transform.popPose();
		}
	}

	public static void renderSegments(
			VertexConsumer out,
			ConnectionSegments toRender,
			BlockAndTintGetter level,
			PoseStack transform
	)
	{
		Connection connection = toRender.connection();
		// Check if we should use AE2 channel-based coloring
		int color;
		if (level instanceof net.minecraft.world.level.Level fullLevel && 
		    com.miniverse.modularinfrastructure.integration.ae2.ModAE2Integration.isAE2Loaded() &&
		    (connection.type == com.miniverse.modularinfrastructure.common.wires.ModWireTypes.DATA_CABLE || 
		     connection.type == com.miniverse.modularinfrastructure.common.wires.ModWireTypes.DENSE_CABLE)) {
			color = com.miniverse.modularinfrastructure.integration.ae2.AE2DataCableRenderer.getDataCableColor(connection, fullLevel);
		} else {
			color = connection.type.getColour(connection);
		}
		double radius = connection.type.getRenderDiameter()/2;
		int lastLight = 0;
		List<RenderedSegment> renderedSection = SEGMENT_CACHE.getUnchecked(new SectionKey(
				radius, color, connection.getCatenaryData(), toRender.firstPointToRender(), toRender.lastPointToRender()
		));
		for(int i = 0; i < renderedSection.size(); ++i)
		{
			RenderedSegment segment = renderedSection.get(i);
			if(i==0)
				lastLight = getLight(connection, segment.offsetStart, level);
			int nextLight = getLight(connection, segment.offsetEnd, level);
			segment.render(lastLight, nextLight, out, transform);
			lastLight = nextLight;
		}
	}

	public static void renderConnection(
			VertexConsumer out,
			CatenaryData catenaryData, double radius, int color,
			int light
	)
	{
		final List<RenderedSegment> section = SEGMENT_CACHE.getUnchecked(new SectionKey(
				radius, color, catenaryData, 0, Connection.RENDER_POINTS_PER_WIRE
		));
		final PoseStack transform = new PoseStack();
		for(RenderedSegment renderedSegment : section)
			renderedSegment.render(light, light, out, transform);
	}

	private static List<RenderedSegment> renderSectionForCache(SectionKey key)
	{
		CatenaryData catenaryData = key.catenaryShape();
		List<RenderedSegment> segments = new ArrayList<>(key.lastIndex-key.firstIndex);
		for(int startIndex = key.firstIndex; startIndex < key.lastIndex; ++startIndex)
		{
			List<Vertex> vertices = new ArrayList<>(4*4);
			Vec3 start = key.catenaryShape().getRenderPoint(startIndex);
			Vec3 end = key.catenaryShape().getRenderPoint(startIndex+1);
			Vec3 horNormal;
			if(key.catenaryShape().isVertical())
				horNormal = new Vec3(1, 0, 0);
			else
				horNormal = new Vec3(-catenaryData.delta().z, 0, catenaryData.delta().x).normalize();
			Vec3 verticalNormal = start.subtract(end).cross(horNormal).normalize();
			Vec3 horRadius = horNormal.scale(key.radius());
			Vec3 verticalRadius = verticalNormal.scale(-key.radius());

			renderBidirectionalQuad(vertices, start, end, horRadius, key.color(), verticalNormal);
			renderBidirectionalQuad(vertices, start, end, verticalRadius, key.color(), horNormal);
			segments.add(new RenderedSegment(
					vertices, BlockPos.containing(start), BlockPos.containing(end)
			));
		}
		return segments;
	}

	private static int getLight(Connection connection, Vec3i point, BlockAndTintGetter level)
	{
		return LevelRenderer.getLightColor(level, connection.getEndA().position().offset(point));
	}

	//TODO move somewhere else
	private static int getByte(int value, int lowestBit)
	{
		return (value >> lowestBit)&255;
	}

	private static void renderBidirectionalQuad(
			List<Vertex> out, Vec3 start, Vec3 end, Vec3 radius, int color, Vec3 positiveNormal
	)
	{
		TextureAtlasSprite texture = WIRE_TEXTURE.get();
		float u0 = texture.getU0();
		float u1 = texture.getU1();
		float v0 = texture.getV0();
		float v1 = texture.getV1();
		Vec3[] vertices = {start.add(radius), end.add(radius), end.subtract(radius), start.subtract(radius),};
		for(int i = 0; i < vertices.length; i++)
			out.add(vertex(vertices[i], u0 + (i==1||i==2 ? u1-u0 : 0), v0 + (i==2||i==3 ? v1-v0 : 0), color, positiveNormal, i==0||i==3));
		for(int i = vertices.length-1; i >= 0; i--)
			out.add(vertex(vertices[i], u0 + (i==1||i==2 ? u1-u0 : 0), v0 + (i==2||i==3 ? v1-v0 : 0), color, positiveNormal.scale(-1), i==0||i==3));
	}

	private static Vertex vertex(Vec3 point, float u, float v, int color, Vec3 normal, boolean lightForStart)
	{
		return new Vertex(
				(float)point.x, (float)point.y, (float)point.z,
				u, v,
				getByte(color, 16)/255f, getByte(color, 8)/255f, getByte(color, 0)/255f,
				(float)normal.x, (float)normal.y, (float)normal.z,
				lightForStart
		);
	}

	private record SectionKey(double radius, int color, CatenaryData catenaryShape, int firstIndex, int lastIndex)
	{
	}

	private record Vertex(
			float posX, float posY, float posZ,
			float texU, float texV,
			float red, float green, float blue,
			float normalX, float normalY, float normalZ,
			boolean lightForStart
	)
	{
	}

	private record RenderedSegment(List<Vertex> vertices, Vec3i offsetStart, Vec3i offsetEnd)
	{
		public void render(int lightStart, int lightEnd, VertexConsumer out, PoseStack transform)
		{
			for(Vertex v : vertices)
				out.addVertex(transform.last(), v.posX, v.posY, v.posZ)
						.setColor(v.red, v.green, v.blue, 1)
						.setUv(v.texU, v.texV)
						.setOverlay(OverlayTexture.NO_OVERLAY)
						.setLight(v.lightForStart?lightStart: lightEnd)
						.setNormal(transform.last(), v.normalX, v.normalY, v.normalZ);
		}
	}
}