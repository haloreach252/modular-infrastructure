package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

/**
 * Represents a specific connection point on a connector
 * A connector may have multiple connection points (e.g., multi-terminal blocks)
 * 
 * Inspired by Immersive Engineering's ConnectionPoint
 * Original wire system concepts from Immersive Engineering by BluSunrize
 */
public record ConnectionPoint(BlockPos position, int index) implements Comparable<ConnectionPoint> {
    
    public ConnectionPoint(CompoundTag nbt) {
        this(NbtUtils.readBlockPos(nbt, "position").orElseThrow(), nbt.getInt("index"));
    }
    
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("position", NbtUtils.writeBlockPos(position));
        tag.putInt("index", index);
        return tag;
    }
    
    @Override
    public int compareTo(ConnectionPoint other) {
        int blockCmp = position.compareTo(other.position);
        if (blockCmp != 0) {
            return blockCmp;
        }
        return Integer.compare(index, other.index);
    }
}