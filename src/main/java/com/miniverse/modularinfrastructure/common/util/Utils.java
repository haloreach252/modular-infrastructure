package com.miniverse.modularinfrastructure.common.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class Utils {
    
    /**
     * Convert a string to camel case
     */
    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }
        
        return result.toString();
    }
    
    /**
     * Unlock an advancement for a player
     */
    public static void unlockIEAdvancement(Player player, String name) {
        if (player instanceof ServerPlayer serverPlayer) {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(
                com.miniverse.modularinfrastructure.ModularInfrastructure.MOD_ID, name
            );
            AdvancementHolder advancement = serverPlayer.getServer().getAdvancements().get(advancementId);
            if (advancement != null) {
                AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        serverPlayer.getAdvancements().award(advancement, criterion);
                    }
                }
            }
        }
    }
    
    /**
     * Check if a Vec3 position is within a block's bounds (with tolerance)
     * The third BlockPos parameter seems to be used as an offset or reference point
     */
    public static boolean isVecInBlock(Vec3 vec, BlockPos blockPos, BlockPos referencePos, double tolerance) {
        double minX = blockPos.getX() - tolerance;
        double minY = blockPos.getY() - tolerance;
        double minZ = blockPos.getZ() - tolerance;
        double maxX = blockPos.getX() + 1 + tolerance;
        double maxY = blockPos.getY() + 1 + tolerance;
        double maxZ = blockPos.getZ() + 1 + tolerance;
        
        return vec.x >= minX && vec.x <= maxX &&
               vec.y >= minY && vec.y <= maxY &&
               vec.z >= minZ && vec.z <= maxZ;
    }
}