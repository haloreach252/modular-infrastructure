package com.miniverse.modularinfrastructure.api;

import net.minecraft.resources.ResourceLocation;

/**
 * General API utilities
 */
public class IEApi {
    public static ResourceLocation ieLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(Lib.MODID, path);
    }
}