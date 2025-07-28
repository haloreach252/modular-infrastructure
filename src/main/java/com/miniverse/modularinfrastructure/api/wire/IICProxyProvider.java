package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Provider for creating proxy connectors in unloaded chunks
 * 
 * Based on Immersive Engineering's proxy system
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public interface IICProxyProvider {
    /**
     * Create a proxy connector for an unloaded position
     * @param level The world level
     * @param pos The position where the connector should be
     * @return A proxy connector that can handle connections until the real one loads
     */
    IImmersiveConnectable create(Level level, BlockPos pos);
}