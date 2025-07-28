package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableDouble;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Utility methods for wire interactions and raytracing
 * 
 * Based on Immersive Engineering's WireUtils
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public class WireUtils {
    
    /**
     * Get the connection offset for a connector
     * This matches IE's implementation
     */
    public static Vec3 getConnectionOffset(GlobalWireNetwork globalNet, ConnectionPoint here, ConnectionPoint other, WireType type) {
        IImmersiveConnectable connector = globalNet.getLocalNet(here).getConnector(here);
        if (connector != null) {
            return connector.getConnectionOffset(here, other, type);
        }
        return new Vec3(0.5, 0.5, 0.5);
    }
    
    /**
     * Get a wire connection that an entity moved through
     */
    public static Connection getConnectionMovedThrough(Level world, LivingEntity e) {
        Vec3 start = e.getEyePosition(0);
        Vec3 end = e.getEyePosition(1);
        return raytraceWires(world, start, end, null);
    }

    /**
     * Raytrace through the world to find wire connections
     */
    public static Connection raytraceWires(Level world, Vec3 start, Vec3 end, @Nullable Connection ignored) {
        GlobalWireNetwork global = GlobalWireNetwork.getNetwork(world);
        WireCollisionData collisionData = global.getCollisionData();
        AtomicReference<Connection> ret = new AtomicReference<>();
        MutableDouble minDistSq = new MutableDouble(Double.POSITIVE_INFINITY);
        
        rayTrace(start, end, world, (pos) -> {
            Collection<WireCollisionData.CollisionInfo> infoAtPos = collisionData.getCollisionInfo(pos);
            for (WireCollisionData.CollisionInfo wireInfo : infoAtPos) {
                Connection c = wireInfo.connection();
                if (!c.equals(ignored)) {
                    Vec3 startRelative = start.add(-pos.getX(), -pos.getY(), -pos.getZ());
                    Vec3 across = wireInfo.intersectB().subtract(wireInfo.intersectA());
                    double t = getCoeffForMinDistance(startRelative, wireInfo.intersectA(), across);
                    t = Mth.clamp(t, 0, 1);
                    Vec3 closest = wireInfo.intersectA().add(t * across.x, t * across.y, t * across.z);
                    double distSq = closest.distanceToSqr(startRelative);
                    if (distSq < minDistSq.doubleValue()) {
                        ret.set(c);
                        minDistSq.setValue(distSq);
                    }
                }
            }
        });
        return ret.get();
    }

    /**
     * Check if a block prevents wire connections from passing through
     */
    public static boolean preventsConnection(Level worldIn, BlockPos pos, BlockState state, Vec3 a, Vec3 b) {
        VoxelShape shape = state.getCollisionShape(worldIn, pos);
        shape = Shapes.joinUnoptimized(shape, Shapes.block(), BooleanOp.AND);
        for (AABB aabb : shape.toAabbs()) {
            aabb = aabb.inflate(1e-5);
            if (aabb.contains(a) || aabb.contains(b) || aabb.clip(a, b).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find blocks that obstruct a wire connection
     */
    public static Set<BlockPos> findObstructingBlocks(Level world, Connection conn, Set<BlockPos> ignore) {
        Set<BlockPos> obstructions = new HashSet<>();
        raytraceAlongCatenary(conn, (p) -> {
            if (!ignore.contains(p.block())) {
                BlockState state = world.getBlockState(p.block());
                if (preventsConnection(world, p.block(), state, p.entersAt(), p.leavesAt())) {
                    obstructions.add(p.block());
                }
            }
        }, (p) -> {
        });
        return obstructions;
    }

    /**
     * Raytrace along a catenary wire connection
     */
    public static void raytraceAlongCatenary(Connection conn, Consumer<BlockIntersection> in,
                                             Consumer<BlockIntersection> close) {
        final BlockPos offset = conn.getEndA().position();
        CatenaryTracer ct = new CatenaryTracer(conn.getCatenaryData(), offset);
        ct.calculateIntegerIntersections();
        ct.forEachSegment(segment -> (segment.inBlock ? in : close).accept(new BlockIntersection(
                segment.mainPos, segment.relativeSegmentStart, segment.relativeSegmentEnd
        )));
    }

    /**
     * Get the wire connection a player is looking at
     */
    public static Connection getTargetConnection(Level world, Player player, Connection ignored, double maxDistance) {
        Vec3 look = player.getLookAngle();
        Vec3 start = player.getEyePosition(1);
        Vec3 end = start.add(look.scale(maxDistance));
        return raytraceWires(world, start, end, ignored);
    }

    /**
     * Iterate over render points of a connection
     */
    public static void forEachRenderPoint(Connection conn, RenderPointConsumer out) {
        BlockPos origin = conn.getEndA().position();
        for (int i = 0; i <= Connection.RENDER_POINTS_PER_WIRE; ++i) {
            Vec3 relativePos = conn.getCatenaryData().getRenderPoint(i);
            BlockPos containingBlock = origin.offset(BlockPos.containing(relativePos));
            SectionPos section = SectionPos.of(containingBlock);
            out.accept(i, relativePos, section);
        }
    }

    private static double getCoeffForMinDistance(Vec3 point, Vec3 lineStart, Vec3 lineDirection) {
        Vec3 toPoint = point.subtract(lineStart);
        return toPoint.dot(lineDirection) / lineDirection.lengthSqr();
    }

    private static void rayTrace(Vec3 start, Vec3 end, Level world, Consumer<BlockPos> consumer) {
        // Simple implementation of block raytracing
        Vec3 current = start;
        Vec3 direction = end.subtract(start).normalize();
        double totalDistance = start.distanceTo(end);
        double step = 0.1;
        
        Set<BlockPos> visited = new HashSet<>();
        
        for (double distance = 0; distance <= totalDistance; distance += step) {
            Vec3 pos = start.add(direction.scale(distance));
            BlockPos blockPos = BlockPos.containing(pos);
            
            if (visited.add(blockPos)) {
                consumer.accept(blockPos);
            }
        }
    }

    public record BlockIntersection(BlockPos block, Vec3 entersAt, Vec3 leavesAt) {
    }

    @FunctionalInterface
    public interface RenderPointConsumer {
        void accept(int id, Vec3 relative, SectionPos section);
    }
}