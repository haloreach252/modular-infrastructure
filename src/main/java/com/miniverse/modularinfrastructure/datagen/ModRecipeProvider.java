package com.miniverse.modularinfrastructure.datagen;

import com.miniverse.modularinfrastructure.ModBlocks;
import com.miniverse.modularinfrastructure.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }
    
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Wood Posts - 6 posts from 3 logs
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.OAK_POST.get(), 6)
            .pattern("L")
            .pattern("L")
            .pattern("L")
            .define('L', Blocks.OAK_LOG)
            .unlockedBy("has_log", has(Blocks.OAK_LOG))
            .save(recipeOutput);
            
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.BIRCH_POST.get(), 6)
            .pattern("L")
            .pattern("L")
            .pattern("L")
            .define('L', Blocks.BIRCH_LOG)
            .unlockedBy("has_log", has(Blocks.BIRCH_LOG))
            .save(recipeOutput);
            
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SPRUCE_POST.get(), 6)
            .pattern("L")
            .pattern("L")
            .pattern("L")
            .define('L', Blocks.SPRUCE_LOG)
            .unlockedBy("has_log", has(Blocks.SPRUCE_LOG))
            .save(recipeOutput);
            
        // Iron Post - 4 posts from iron bars
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.IRON_POST.get(), 4)
            .pattern("I")
            .pattern("I")
            .pattern("I")
            .define('I', Items.IRON_BARS)
            .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
            .save(recipeOutput);
            
        // Concrete Post - 4 posts from concrete
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CONCRETE_POST.get(), 4)
            .pattern("C")
            .pattern("C")
            .pattern("C")
            .define('C', Blocks.GRAY_CONCRETE)
            .unlockedBy("has_concrete", has(Blocks.GRAY_CONCRETE))
            .save(recipeOutput);
            
        // Tools
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.POST_CONFIGURATOR.get())
            .pattern(" IR")
            .pattern(" SI")
            .pattern("S  ")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('S', Tags.Items.RODS_WOODEN)
            .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
            .save(recipeOutput);
            
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WIRE_CONNECTOR.get())
            .pattern(" I ")
            .pattern(" SI")
            .pattern("S  ")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('S', Tags.Items.RODS_WOODEN)
            .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
            .save(recipeOutput);
            
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WIRE_CUTTERS.get())
            .pattern(" I ")
            .pattern("ISI")
            .pattern("S S")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('S', Tags.Items.RODS_WOODEN)
            .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
            .save(recipeOutput);
    }
}