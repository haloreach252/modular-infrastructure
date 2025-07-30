package com.miniverse.modularinfrastructure.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Utilities for safely accessing chunks
 */
public class SafeChunkUtils {
    
    /**
     * Check if a chunk is safely loaded
     */
    public static boolean isChunkSafe(LevelAccessor world, BlockPos pos) {
        return world.hasChunkAt(pos);
    }
    
    /**
     * Check if a chunk is safely loaded
     */
    public static boolean isChunkSafe(LevelAccessor world, ChunkPos pos) {
        return world.hasChunk(pos.x, pos.z);
    }
    
    /**
     * Safely get a block entity
     */
    public static BlockEntity getSafeBE(Level world, BlockPos pos) {
        if (isChunkSafe(world, pos)) {
            return world.getBlockEntity(pos);
        }
        return null;
    }
}