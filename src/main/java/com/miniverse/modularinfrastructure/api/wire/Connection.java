package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a wire connection between two connection points
 * 
 * Inspired by Immersive Engineering's Connection system
 * Original wire system concepts from Immersive Engineering by BluSunrize
 */
public class Connection {
    public static final int RENDER_POINTS_PER_WIRE = 16;
    
    private final WireType wireType;
    private final ConnectionPoint endA;
    private final ConnectionPoint endB;
    private boolean internal; // Internal connections within the same block
    
    // Catenary data for wire rendering
    private Vec3 endAOffset;
    private Vec3 endBOffset;
    private double length;
    private CatenaryData catenaryData;
    
    // Collision data generation tracking
    private boolean blockDataGenerated = false;
    
    public Connection(WireType wireType, ConnectionPoint endA, ConnectionPoint endB, Vec3 endAOffset, Vec3 endBOffset) {
        this.wireType = wireType;
        
        // Ensure consistent ordering (endA > endB)
        if (endA.compareTo(endB) < 0) {
            this.endA = endB;
            this.endB = endA;
            this.endAOffset = endBOffset;
            this.endBOffset = endAOffset;
        } else {
            this.endA = endA;
            this.endB = endB;
            this.endAOffset = endAOffset;
            this.endBOffset = endBOffset;
        }
        
        this.internal = endA.position().equals(endB.position());
        this.length = Math.sqrt(endA.position().distSqr(endB.position()));
    }
    
    // Constructor for internal connections
    public Connection(BlockPos pos, int idA, int idB) {
        this(ModWireTypes.INTERNAL_CONNECTION, 
             new ConnectionPoint(pos, idA), 
             new ConnectionPoint(pos, idB),
             Vec3.ZERO, Vec3.ZERO);
    }
    
    // Constructor for normal connections (defaults offsets to zero)
    public Connection(WireType wireType, ConnectionPoint endA, ConnectionPoint endB) {
        this(wireType, endA, endB, Vec3.ZERO, Vec3.ZERO);
    }
    
    // Constructor used by GlobalWireNetwork
    public Connection(WireType wireType, ConnectionPoint endA, ConnectionPoint endB, GlobalWireNetwork globalNet) {
        this(wireType, endA, endB, 
             WireUtils.getConnectionOffset(globalNet, endA, endB, wireType),
             WireUtils.getConnectionOffset(globalNet, endB, endA, wireType));
    }
    
    public Connection(CompoundTag nbt) {
        this(getWireTypeFromName(nbt.getString("type")),
             new ConnectionPoint(nbt.getCompound("endA")),
             new ConnectionPoint(nbt.getCompound("endB")),
             loadVec3(nbt.getCompound("endAOffset")),
             loadVec3(nbt.getCompound("endBOffset")));
    }
    
    private static WireType getWireTypeFromName(String name) {
        WireType type = WireTypeRegistry.get(name);
        if (type == null) {
            // Fallback to copper if type not found
            return ModWireTypes.COPPER_LV;
        }
        return type;
    }
    
    private static Vec3 loadVec3(CompoundTag tag) {
        return new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
    }
    
    private static CompoundTag saveVec3(Vec3 vec) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", vec.x);
        tag.putDouble("y", vec.y);
        tag.putDouble("z", vec.z);
        return tag;
    }
    
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("endA", endA.toNBT());
        nbt.put("endB", endB.toNBT());
        nbt.putString("type", wireType.getUniqueName());
        nbt.putBoolean("internal", internal);
        nbt.put("endAOffset", saveVec3(endAOffset));
        nbt.put("endBOffset", saveVec3(endBOffset));
        return nbt;
    }
    
    public ConnectionPoint getOtherEnd(ConnectionPoint known) {
        if (known.equals(endA)) {
            return endB;
        } else {
            return endA;
        }
    }
    
    public boolean isEnd(ConnectionPoint point) {
        return point.equals(endA) || point.equals(endB);
    }
    
    public boolean isPositiveEnd(ConnectionPoint point) {
        return point.equals(endA);
    }
    
    // Getters
    public WireType getWireType() { return wireType; }
    public ConnectionPoint getEndA() { return endA; }
    public ConnectionPoint getEndB() { return endB; }
    public boolean isInternal() { return internal; }
    public double getLength() { return length; }
    public Vec3 getEndAOffset() { return endAOffset; }
    public Vec3 getEndBOffset() { return endBOffset; }
    
    // Setters
    public void setInternal(boolean internal) {
        this.internal = internal;
    }
    
    public boolean isBlockDataGenerated() {
        return blockDataGenerated;
    }
    
    public void setBlockDataGenerated(boolean blockDataGenerated) {
        this.blockDataGenerated = blockDataGenerated;
    }
    
    public LocalWireNetwork getContainingNet(GlobalWireNetwork globalNet) {
        return globalNet.getLocalNet(endA);
    }
    
    // Catenary calculations for wire rendering
    public CatenaryData getCatenaryData() {
        if (catenaryData == null) {
            // Match IE's implementation exactly - catenary starts from endAOffset
            catenaryData = calculateCatenaryData(
                endAOffset,
                Vec3.atLowerCornerOf(endB.position().subtract(endA.position())).add(endBOffset),
                wireType.getSlack()
            );
        }
        return catenaryData;
    }
    
    public void resetCatenaryData(Vec3 newOffsetA, Vec3 newOffsetB) {
        this.catenaryData = null;
        this.endAOffset = newOffsetA;
        this.endBOffset = newOffsetB;
        this.length = Math.sqrt(endA.position().distSqr(endB.position()));
    }
    
    // Get a point along the wire for rendering (0.0 to 1.0)
    public Vec3 getPoint(double pos, ConnectionPoint from) {
        pos = transformPosition(pos, from);
        Vec3 basic = getCatenaryData().getPoint(pos);
        Vec3 add = Vec3.ZERO;
        if (endB.equals(from)) {
            add = Vec3.atLowerCornerOf(endA.position().subtract(endB.position()));
        }
        return basic.add(add);
    }
    
    private double transformPosition(double pos, ConnectionPoint from) {
        if (endB.equals(from)) {
            return 1 - pos;
        } else {
            return pos;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Connection that = (Connection) o;
        
        if (internal != that.internal) return false;
        if (!wireType.equals(that.wireType)) return false;
        if (!endA.equals(that.endA)) return false;
        return endB.equals(that.endB);
    }
    
    @Override
    public int hashCode() {
        int result = wireType.hashCode();
        result = 31 * result + endA.hashCode();
        result = 31 * result + endB.hashCode();
        result = 31 * result + (internal ? 1 : 0);
        return result;
    }
    
    // Catenary curve calculation for realistic wire sag
    private static CatenaryData calculateCatenaryData(Vec3 start, Vec3 end, double slack) {
        Vec3 delta = end.subtract(start);
        double horLength = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        
        // Special case for vertical wires
        if (Math.abs(delta.x) < 0.05 && Math.abs(delta.z) < 0.05) {
            return new CatenaryData(true, 0, 0, 1, delta, 0, start);
        }
        
        double wireLength = delta.length() * slack;
        
        // Calculate catenary parameter
        double l = calculateCatenaryParameter(horLength, wireLength, delta.y);
        
        double scale = horLength / (2 * l);
        double offsetX = (horLength - scale * Math.log((wireLength + delta.y) / (wireLength - delta.y))) * 0.5;
        double offsetY = (delta.y - wireLength * Math.cosh(l) / Math.sinh(l)) * 0.5;
        
        return new CatenaryData(false, offsetX, offsetY, scale, delta, horLength, start);
    }
    
    private static double calculateCatenaryParameter(double horLength, double wireLength, double deltaY) {
        double goal = Math.sqrt(wireLength * wireLength - deltaY * deltaY) / horLength;
        double lower = 0;
        double upper = 1;
        
        // Find bounds
        while (Math.sinh(upper) / upper < goal) {
            lower = upper;
            upper *= 2;
        }
        
        // Binary search for the parameter
        for (int i = 0; i < 20; i++) {
            double middle = (lower + upper) / 2;
            double value = Math.sinh(middle) / middle;
            if (value < goal) {
                lower = middle;
            } else {
                upper = middle;
            }
        }
        
        return (lower + upper) / 2;
    }
    
    // Data class for catenary curve calculations
    public static record CatenaryData(
        boolean isVertical,
        double offsetX,
        double offsetY,
        double scale,
        Vec3 delta,
        double horLength,
        Vec3 start
    ) {
        public Vec3 getPoint(double pos) {
            if (pos == 1) {
                return start.add(delta);
            }
            
            double x = delta.x * pos;
            double y;
            if (isVertical) {
                y = delta.y * pos;
            } else {
                y = scale * Math.cosh((horLength * pos - offsetX) / scale) + offsetY;
            }
            double z = delta.z * pos;
            
            return start.add(x, y, z);
        }
        
        public Vec3 getRenderPoint(int index) {
            return getPoint(index / (double) RENDER_POINTS_PER_WIRE);
        }
    }
}