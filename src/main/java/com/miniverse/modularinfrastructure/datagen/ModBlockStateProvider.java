package com.miniverse.modularinfrastructure.datagen;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.ModBlocks;
import com.miniverse.modularinfrastructure.block.PostBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

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
}