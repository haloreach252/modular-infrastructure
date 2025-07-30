package com.miniverse.modularinfrastructure.datagen;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.ModBlocks;
import com.miniverse.modularinfrastructure.block.PostBlock;
import com.miniverse.modularinfrastructure.block.ConnectorBlock;
import com.miniverse.modularinfrastructure.block.PowerConnectorBlock;
import com.miniverse.modularinfrastructure.block.DataConnectorBlock;
import com.miniverse.modularinfrastructure.block.UtilityConnectorBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.minecraft.core.Direction;

public class ModBlockStateProvider extends BlockStateProvider {
    
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ModularInfrastructure.MODID, exFileHelper);
    }
    
    @Override
    protected void registerStatesAndModels() {
        // Register all post blocks
        postBlockWithWidthVariants(ModBlocks.OAK_POST, "oak");
        postBlockWithWidthVariants(ModBlocks.BIRCH_POST, "birch");
        postBlockWithWidthVariants(ModBlocks.SPRUCE_POST, "spruce");
        postBlockWithWidthVariants(ModBlocks.IRON_POST, "iron");
        postBlockWithWidthVariants(ModBlocks.CONCRETE_POST, "concrete");
        
        // Register connector blocks
        // Power Connectors
        connectorBlock(ModBlocks.POWER_CONNECTOR_LV, "power_connector_lv");
        connectorBlock(ModBlocks.POWER_CONNECTOR_MV, "power_connector_mv");
        connectorBlock(ModBlocks.POWER_CONNECTOR_HV, "power_connector_hv");
        
        // Data Connectors
        connectorBlock(ModBlocks.DATA_CONNECTOR_BASIC, "data_connector_basic");
        connectorBlock(ModBlocks.DATA_CONNECTOR_ADVANCED, "data_connector_advanced");
        
        // Utility Connectors
        connectorBlock(ModBlocks.REDSTONE_CONNECTOR, "redstone_connector");
        connectorBlock(ModBlocks.STRUCTURAL_CONNECTOR, "structural_connector");
    }
    
    private void postBlockWithWidthVariants(DeferredBlock<PostBlock> blockSupplier, String materialName) {
        PostBlock block = blockSupplier.get();
        String blockName = blockSupplier.getId().getPath();
        
        // Create blockstate with width variants
        getVariantBuilder(block).forAllStates(state -> {
            int width = state.getValue(PostBlock.WIDTH);
            
            // Generate material-specific model that inherits from base width model
            ModelFile model = models().getBuilder(blockName + "_width_" + width)
                .parent(models().getExistingFile(modLoc("block/post_width_" + width)))
                .texture("side", modLoc("block/" + materialName + "_post_side"))
                .texture("top", modLoc("block/" + materialName + "_post_top"))
                .texture("bottom", modLoc("block/" + materialName + "_post_top"));
            
            return ConfiguredModel.builder()
                .modelFile(model)
                .build();
        });
        
        // Generate simple item model pointing to default width (4 = 8px)
        simpleBlockItem(block, models().getExistingFile(modLoc("block/" + blockName + "_width_4")));
    }
    
    private void connectorBlock(DeferredBlock<? extends ConnectorBlock> blockSupplier, String modelName) {
        ConnectorBlock block = blockSupplier.get();
        
        // Create blockstate with directional variants
        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(ConnectorBlock.FACING);
            
            // Use the model file you've created - datagen won't create the model itself
            ModelFile model = models().getExistingFile(modLoc("block/" + modelName));
            
            // Calculate rotations based on facing direction
            // The model's default orientation is facing UP (base at bottom)
            // We need to rotate it to face other directions
            int xRot = 0;
            int yRot = 0;
            
            switch (facing) {
                case UP -> { xRot = 0; yRot = 0; }     // Default orientation
                case DOWN -> { xRot = 180; yRot = 0; } // Flip upside down
                case NORTH -> { xRot = 90; yRot = 0; }  // Rotate forward
                case SOUTH -> { xRot = 270; yRot = 0; } // Rotate backward  
                case EAST -> { xRot = 90; yRot = 90; }  // Rotate right then forward
                case WEST -> { xRot = 90; yRot = 270; } // Rotate left then forward
            }
            
            return ConfiguredModel.builder()
                .modelFile(model)
                .rotationX(xRot)
                .rotationY(yRot)
                .build();
        });
        
        // Generate item model - connectors use their block model directly
        simpleBlockItem(block, models().getExistingFile(modLoc("block/" + modelName)));
    }
}