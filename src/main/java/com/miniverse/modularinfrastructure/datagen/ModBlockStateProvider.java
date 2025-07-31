package com.miniverse.modularinfrastructure.datagen;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.ModBlocks;
import com.miniverse.modularinfrastructure.block.*;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

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
        
        // Fencing
        chainLinkFenceBlock(ModBlocks.CHAIN_LINK_FENCE);
        
        // Decorative blocks
        concreteBarrierBlock(ModBlocks.CONCRETE_BARRIER);
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
    
    private void chainLinkFenceBlock(DeferredBlock<ChainLinkFenceBlock> blockSupplier) {
        ChainLinkFenceBlock block = blockSupplier.get();
        String blockName = blockSupplier.getId().getPath();
        
        // Create blockstate variants for each fence type
        getVariantBuilder(block).forAllStates(state -> {
            ChainLinkFenceBlock.FenceType type = state.getValue(ChainLinkFenceBlock.TYPE);
            boolean hasPost = state.getValue(ChainLinkFenceBlock.HAS_POST);
            boolean north = state.getValue(ChainLinkFenceBlock.NORTH);
            boolean east = state.getValue(ChainLinkFenceBlock.EAST);
            boolean south = state.getValue(ChainLinkFenceBlock.SOUTH);
            boolean west = state.getValue(ChainLinkFenceBlock.WEST);
            
            // Determine model name based on type and has_post
            String modelName;
            if (type == ChainLinkFenceBlock.FenceType.SINGLE && !hasPost) {
                // Single without post uses the straight_no_post model
                modelName = blockName + "_straight_no_post";
            } else if (!hasPost && type != ChainLinkFenceBlock.FenceType.SINGLE) {
                // Other types without post use _no_post suffix
                modelName = blockName + "_" + type.getSerializedName() + "_no_post";
            } else {
                // With post, use normal model name
                modelName = blockName + "_" + type.getSerializedName();
            }
            
            ModelFile model = models().getExistingFile(modLoc("block/" + modelName));
            
            // Calculate rotation based on fence type and connections
            int yRot = 0;
            
            switch (type) {
                case STRAIGHT:
                    // Straight fence needs rotation based on direction
                    if (north && south) {
                        yRot = 90; // North-South orientation (rotate 90 degrees)
                    } else if (east && west) {
                        yRot = 0; // East-West orientation (default model orientation)
                    }
                    break;
                    
                case CORNER:
                    // Corner fence needs rotation based on which sides connect
                    if (north && east) {
                        yRot = 0; // Default: North-East corner
                    } else if (east && south) {
                        yRot = 90; // East-South corner
                    } else if (south && west) {
                        yRot = 180; // South-West corner
                    } else if (west && north) {
                        yRot = 270; // West-North corner
                    }
                    break;
                    
                case T_SHAPE:
                    // T-shape needs rotation based on which side doesn't connect
                    if (!north) {
                        yRot = 180; // T pointing south
                    } else if (!east) {
                        yRot = 270; // T pointing west
                    } else if (!south) {
                        yRot = 0; // T pointing north (default)
                    } else if (!west) {
                        yRot = 90; // T pointing east
                    }
                    break;
                    
                case SINGLE:
                    // Single fence - post should be at the end opposite to the connection
                    // Default model: fence extends west, post is on east
                    if (north) {
                        yRot = 90; // Connection to north, post at south
                    } else if (east) {
                        yRot = 180; // Connection to east, post at west
                    } else if (south) {
                        yRot = 270; // Connection to south, post at north
                    } else if (west) {
                        yRot = 0; // Connection to west, post at east (default)
                    } else {
                        // No connections - default to east-west orientation
                        yRot = 0;
                    }
                    break;
                    
                case EDGE:
                    // Edge fence - similar to single but for special corner cases
                    // Edge model has fence extending west from center
                    if (north) {
                        yRot = 90; // Connection to north
                    } else if (east) {
                        yRot = 180; // Connection to east
                    } else if (south) {
                        yRot = 270; // Connection to south
                    } else if (west) {
                        yRot = 0; // Connection to west (default)
                    } else {
                        yRot = 0; // Default
                    }
                    break;
                    
                case CROSS:
                    // Cross doesn't need rotation - it's symmetrical
                    yRot = 0;
                    break;
            }
            
            return ConfiguredModel.builder()
                .modelFile(model)
                .rotationY(yRot)
                .build();
        });
        
        // Generate item model - use the single variant for inventory
        simpleBlockItem(block, models().getExistingFile(modLoc("block/" + blockName + "_straight_no_post")));
    }
    
    private void concreteBarrierBlock(DeferredBlock<ConcreteBarrierBlock> blockSupplier) {
        ConcreteBarrierBlock block = blockSupplier.get();
        String blockName = blockSupplier.getId().getPath();
        
        // Create blockstate variants for each barrier type
        getVariantBuilder(block).forAllStates(state -> {
            ConcreteBarrierBlock.BarrierType type = state.getValue(ConcreteBarrierBlock.TYPE);
            boolean isBase = state.getValue(ConcreteBarrierBlock.IS_BASE);
            boolean north = state.getValue(ConcreteBarrierBlock.NORTH);
            boolean east = state.getValue(ConcreteBarrierBlock.EAST);
            boolean south = state.getValue(ConcreteBarrierBlock.SOUTH);
            boolean west = state.getValue(ConcreteBarrierBlock.WEST);
            
            // Determine model name based on type and is_base
            String modelName = blockName;
            if (isBase) {
                modelName += "_base";
                if (type != ConcreteBarrierBlock.BarrierType.STRAIGHT) {
                    // Map enum names to model file names
                    String typeName = type.getSerializedName();
                    if (typeName.equals("t_junction")) {
                        typeName = "t";
                    }
                    modelName += "_" + typeName;
                }
            } else {
                // Map enum names to model file names
                String typeName = type.getSerializedName();
                if (typeName.equals("t_junction")) {
                    typeName = "t";
                }
                modelName += "_" + typeName;
            }
            
            ModelFile model = models().getExistingFile(modLoc("block/" + modelName));
            
            // Calculate rotation based on barrier type and connections
            int yRot = 0;
            
            switch (type) {
                case STRAIGHT:
                    // Straight barrier needs rotation based on direction
                    if (north || south) {
                        yRot = 90; // North-South orientation (rotate 90 degrees)
                    } else {
                        yRot = 0; // East-West orientation (default model orientation)
                    }
                    break;
                    
                case CORNER:
                    // Corner barrier needs rotation based on which sides connect
                    if (north && east) {
                        yRot = 0; // Default: North-East corner
                    } else if (east && south) {
                        yRot = 90; // East-South corner
                    } else if (south && west) {
                        yRot = 180; // South-West corner
                    } else if (west && north) {
                        yRot = 270; // West-North corner
                    }
                    break;
                    
                case T_JUNCTION:
                    // T-junction needs rotation based on which side doesn't connect
                    if (!north) {
                        yRot = 180; // T pointing south
                    } else if (!east) {
                        yRot = 270; // T pointing west
                    } else if (!south) {
                        yRot = 0; // T pointing north (default)
                    } else if (!west) {
                        yRot = 90; // T pointing east
                    }
                    break;
                    
                case CROSS:
                    // Cross doesn't need rotation - it's symmetrical
                    yRot = 0;
                    break;
            }
            
            return ConfiguredModel.builder()
                .modelFile(model)
                .rotationY(yRot)
                .build();
        });
        
        // Generate item model - use the base variant for inventory
        simpleBlockItem(block, models().getExistingFile(modLoc("block/" + blockName + "_base")));
    }
}