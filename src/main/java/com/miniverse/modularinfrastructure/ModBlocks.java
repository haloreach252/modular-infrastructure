package com.miniverse.modularinfrastructure;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import com.miniverse.modularinfrastructure.block.PostBlock;
import com.miniverse.modularinfrastructure.item.PostBlockItem;

import java.util.function.Supplier;

public class ModBlocks {
    // Wood Posts
    public static final DeferredBlock<PostBlock> OAK_POST = registerPostBlock("oak_post", 
        () -> new PostBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .noOcclusion()));
    
    public static final DeferredBlock<PostBlock> BIRCH_POST = registerPostBlock("birch_post",
        () -> new PostBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.SAND)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .noOcclusion()));
    
    public static final DeferredBlock<PostBlock> SPRUCE_POST = registerPostBlock("spruce_post",
        () -> new PostBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.PODZOL)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .noOcclusion()));
    
    // Metal Posts
    public static final DeferredBlock<PostBlock> IRON_POST = registerPostBlock("iron_post",
        () -> new PostBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(3.5F)
            .sound(SoundType.METAL)
            .noOcclusion()));
    
    // Concrete Posts
    public static final DeferredBlock<PostBlock> CONCRETE_POST = registerPostBlock("concrete_post",
        () -> new PostBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(4.0F)
            .sound(SoundType.STONE)
            .noOcclusion()));
    
    private static <T extends Block> DeferredBlock<T> registerPostBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = ModularInfrastructure.BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    
    private static <T extends Block> DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<T> block) {
        return ModularInfrastructure.ITEMS.register(name, () -> new PostBlockItem(block.get(), new Item.Properties()));
    }
    
    public static void init() {
        // Static initialization
    }
    
    public static void addCreativeTabItems(CreativeModeTab.Output output) {
        output.accept(OAK_POST.get());
        output.accept(BIRCH_POST.get());
        output.accept(SPRUCE_POST.get());
        output.accept(IRON_POST.get());
        output.accept(CONCRETE_POST.get());
    }
}