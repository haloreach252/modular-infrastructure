package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Traces intersections of a catenary curve with block boundaries
 * 
 * Based on Immersive Engineering's catenary tracing system
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public class CatenaryTracer {
    private final Connection.CatenaryData catenary;
    private final BlockPos offset;
    private final List<Segment> segments = new ArrayList<>();

    public CatenaryTracer(Connection.CatenaryData catenary, BlockPos offset) {
        this.catenary = catenary;
        this.offset = offset;
    }

    public void calculateIntegerIntersections() {
        segments.clear();
        
        // Special case for vertical wires
        if (catenary.isVertical()) {
            calculateVerticalSegments();
            return;
        }
        
        // Calculate segments for catenary curve
        double step = 1.0 / Connection.RENDER_POINTS_PER_WIRE;
        Vec3 previousPoint = catenary.getPoint(0);
        BlockPos previousBlock = BlockPos.containing(previousPoint);
        Vec3 segmentStart = previousPoint;
        
        for (double t = step; t <= 1.0 + step / 2; t += step) {
            double clampedT = Math.min(t, 1.0);
            Vec3 currentPoint = catenary.getPoint(clampedT);
            BlockPos currentBlock = BlockPos.containing(currentPoint);
            
            if (!currentBlock.equals(previousBlock) || clampedT >= 1.0) {
                // We've crossed a block boundary or reached the end
                segments.add(new Segment(
                    previousBlock,
                    segmentStart,
                    previousPoint,
                    true // For now, assume all segments are "in block"
                ));
                
                if (clampedT < 1.0) {
                    // Add a boundary segment if not at the end
                    Vec3 boundary = findBoundaryPoint(previousPoint, currentPoint, previousBlock, currentBlock);
                    segments.add(new Segment(
                        previousBlock,
                        previousPoint,
                        boundary,
                        false
                    ));
                    segmentStart = boundary;
                }
                
                previousBlock = currentBlock;
            }
            
            previousPoint = currentPoint;
        }
    }

    private void calculateVerticalSegments() {
        Vec3 start = catenary.start();
        Vec3 end = catenary.start().add(catenary.delta());
        
        int y1 = Mth.floor(start.y);
        int y2 = Mth.floor(end.y);
        int dir = y2 > y1 ? 1 : -1;
        
        Vec3 current = start;
        for (int y = y1; y != y2; y += dir) {
            double nextY = y + (dir > 0 ? 1 : 0);
            double t = (nextY - start.y) / catenary.delta().y;
            Vec3 next = start.add(catenary.delta().scale(t));
            
            segments.add(new Segment(
                new BlockPos(Mth.floor(current.x), y, Mth.floor(current.z)),
                current.subtract(Vec3.atLowerCornerOf(offset)),
                next.subtract(Vec3.atLowerCornerOf(offset)),
                true
            ));
            
            current = next;
        }
        
        // Add final segment
        segments.add(new Segment(
            new BlockPos(Mth.floor(end.x), y2, Mth.floor(end.z)),
            current.subtract(Vec3.atLowerCornerOf(offset)),
            end.subtract(Vec3.atLowerCornerOf(offset)),
            true
        ));
    }

    private Vec3 findBoundaryPoint(Vec3 from, Vec3 to, BlockPos fromBlock, BlockPos toBlock) {
        // Simple linear interpolation to find where the line crosses the block boundary
        Vec3 direction = to.subtract(from);
        double tMin = 0;
        double tMax = 1;
        
        // Check each axis
        for (int axis = 0; axis < 3; axis++) {
            double fromCoord = getComponent(from, axis);
            double toCoord = getComponent(to, axis);
            int fromBlockCoord = getComponent(fromBlock, axis);
            int toBlockCoord = getComponent(toBlock, axis);
            
            if (fromBlockCoord != toBlockCoord) {
                double boundary = fromBlockCoord + (toBlockCoord > fromBlockCoord ? 1 : 0);
                double t = (boundary - fromCoord) / (toCoord - fromCoord);
                
                if (t > tMin && t < tMax) {
                    tMin = t;
                }
            }
        }
        
        return from.add(direction.scale(tMin));
    }

    private double getComponent(Vec3 vec, int axis) {
        return switch (axis) {
            case 0 -> vec.x;
            case 1 -> vec.y;
            case 2 -> vec.z;
            default -> throw new IllegalArgumentException();
        };
    }

    private int getComponent(BlockPos pos, int axis) {
        return switch (axis) {
            case 0 -> pos.getX();
            case 1 -> pos.getY();
            case 2 -> pos.getZ();
            default -> throw new IllegalArgumentException();
        };
    }

    public void forEachSegment(Consumer<Segment> consumer) {
        for (Segment segment : segments) {
            consumer.accept(segment);
        }
    }

    public static class Segment {
        public final BlockPos mainPos;
        public final Vec3 relativeSegmentStart;
        public final Vec3 relativeSegmentEnd;
        public final boolean inBlock;

        public Segment(BlockPos mainPos, Vec3 relativeSegmentStart, Vec3 relativeSegmentEnd, boolean inBlock) {
            this.mainPos = mainPos;
            this.relativeSegmentStart = relativeSegmentStart;
            this.relativeSegmentEnd = relativeSegmentEnd;
            this.inBlock = inBlock;
        }
    }
}