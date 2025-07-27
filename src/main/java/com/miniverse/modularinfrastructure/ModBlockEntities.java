package com.miniverse.modularinfrastructure;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.miniverse.modularinfrastructure.blockentity.PostBlockEntity;
import com.miniverse.modularinfrastructure.blockentity.PowerConnectorBlockEntity;
import com.miniverse.modularinfrastructure.blockentity.DataConnectorBlockEntity;
import com.miniverse.modularinfrastructure.blockentity.UtilityConnectorBlockEntity;

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
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerConnectorBlockEntity>> POWER_CONNECTOR = 
        ModularInfrastructure.BLOCK_ENTITIES.register("power_connector", 
            () -> BlockEntityType.Builder.of(PowerConnectorBlockEntity::new, 
                ModBlocks.POWER_CONNECTOR_LV.get(),
                ModBlocks.POWER_CONNECTOR_MV.get(),
                ModBlocks.POWER_CONNECTOR_HV.get()
            ).build(null));
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DataConnectorBlockEntity>> DATA_CONNECTOR = 
        ModularInfrastructure.BLOCK_ENTITIES.register("data_connector", 
            () -> BlockEntityType.Builder.of(DataConnectorBlockEntity::new, 
                ModBlocks.DATA_CONNECTOR_BASIC.get(),
                ModBlocks.DATA_CONNECTOR_ADVANCED.get()
            ).build(null));
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UtilityConnectorBlockEntity>> UTILITY_CONNECTOR = 
        ModularInfrastructure.BLOCK_ENTITIES.register("utility_connector", 
            () -> BlockEntityType.Builder.of(UtilityConnectorBlockEntity::new, 
                ModBlocks.REDSTONE_CONNECTOR.get(),
                ModBlocks.STRUCTURAL_CONNECTOR.get()
            ).build(null));
    
    public static void init() {
        // Static initialization
    }
}