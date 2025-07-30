package com.miniverse.modularinfrastructure.api;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Information about targeting for wire connections
 */
public record TargetingInfo(Direction side, float hitX, float hitY, float hitZ) {
    
    public Vec3 getHitVec() {
        return new Vec3(hitX, hitY, hitZ);
    }
}