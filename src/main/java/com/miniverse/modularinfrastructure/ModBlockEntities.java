package com.miniverse.modularinfrastructure;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.miniverse.modularinfrastructure.blockentity.PostBlockEntity;

public class ModBlockEntities {
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PostBlockEntity>> POST_BLOCK_ENTITY = 
        ModularInfrastructure.BLOCK_ENTITIES.register("post_block_entity", 
            () -> BlockEntityType.Builder.of(PostBlockEntity::new, 
                ModBlocks.OAK_POST.get(),
                ModBlocks.BIRCH_POST.get(),
                ModBlocks.SPRUCE_POST.get(),
                ModBlocks.IRON_POST.get(),
                ModBlocks.CONCRETE_POST.get()
            ).build(null));
    
    public static void init() {
        // Static initialization
    }
}