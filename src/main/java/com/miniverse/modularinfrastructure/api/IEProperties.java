package com.miniverse.modularinfrastructure.api;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Common block state properties used by the mod
 */
public class IEProperties {
    public static final DirectionProperty FACING_ALL = DirectionProperty.create("facing", Direction.values());
}