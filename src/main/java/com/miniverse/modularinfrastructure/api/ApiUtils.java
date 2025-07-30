package com.miniverse.modularinfrastructure.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * General API utilities
 */
public class ApiUtils {
    
    /**
     * Apply knockback to an entity without a damage source
     * Based on Immersive Engineering's implementation
     */
    public static void knockbackNoSource(LivingEntity entity, double strength, double xRatio, double zRatio) {
        entity.hasImpulse = true;
        Vec3 motionOld = entity.getDeltaMovement();
        Vec3 toAdd = (new Vec3(xRatio, 0.0D, zRatio)).normalize().scale(strength);
        entity.setDeltaMovement(
                motionOld.x/2.0D-toAdd.x,
                entity.onGround() ? Math.min(0.4D, motionOld.y/2.0D+strength) : motionOld.y,
                motionOld.z/2.0D-toAdd.z);
    }
    
    /**
     * Add a task to be run on the next server tick
     */
    public static void addFutureServerTask(Level world, Runnable task, boolean forceFuture) {
        if (!world.isClientSide()) {
            // In a real implementation, this would queue the task
            // For now, just run it immediately
            task.run();
        }
    }
    
    /**
     * Get a specific dimension (x=0, y=1, z=2) from a Vec3
     */
    public static double getDim(Vec3 vec, int dim) {
        return switch (dim) {
            case 0 -> vec.x;
            case 1 -> vec.y;
            case 2 -> vec.z;
            default -> throw new IllegalArgumentException("Invalid dimension: " + dim);
        };
    }
}