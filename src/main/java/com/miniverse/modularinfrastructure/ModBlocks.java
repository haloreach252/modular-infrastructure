package com.miniverse.modularinfrastructure;

import com.miniverse.modularinfrastructure.block.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

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
    
    // Power Connectors
    public static final DeferredBlock<PowerConnectorBlock> POWER_CONNECTOR_LV = registerConnectorBlock("power_connector_lv",
        () -> new PowerConnectorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(2.0F)
            .sound(SoundType.METAL)
            .noOcclusion(), PowerConnectorBlock.PowerTier.LV));
    
    public static final DeferredBlock<PowerConnectorBlock> POWER_CONNECTOR_MV = registerConnectorBlock("power_connector_mv",
        () -> new PowerConnectorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(2.5F)
            .sound(SoundType.METAL)
            .noOcclusion(), PowerConnectorBlock.PowerTier.MV));
    
    public static final DeferredBlock<PowerConnectorBlock> POWER_CONNECTOR_HV = registerConnectorBlock("power_connector_hv",
        () -> new PowerConnectorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(3.0F)
            .sound(SoundType.METAL)
            .noOcclusion(), PowerConnectorBlock.PowerTier.HV));
    
    // Data Connectors
    public static final DeferredBlock<DataConnectorBlock> DATA_CONNECTOR_BASIC = registerConnectorBlock("data_connector_basic",
        () -> new DataConnectorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .strength(2.0F)
            .sound(SoundType.METAL)
            .noOcclusion(), DataConnectorBlock.DataTier.BASIC));
    
    public static final DeferredBlock<DataConnectorBlock> DATA_CONNECTOR_ADVANCED = registerConnectorBlock("data_connector_advanced",
        () -> new DataConnectorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(2.5F)
            .sound(SoundType.METAL)
            .noOcclusion(), DataConnectorBlock.DataTier.ADVANCED));
    
    // Utility Connectors
    public static final DeferredBlock<UtilityConnectorBlock> REDSTONE_CONNECTOR = registerConnectorBlock("redstone_connector",
        () -> new UtilityConnectorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_RED)
            .strength(2.0F)
            .sound(SoundType.METAL)
            .noOcclusion(), UtilityConnectorBlock.UtilityType.REDSTONE));
    
    public static final DeferredBlock<UtilityConnectorBlock> STRUCTURAL_CONNECTOR = registerConnectorBlock("structural_connector",
        () -> new UtilityConnectorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BROWN)
            .strength(2.0F)
            .sound(SoundType.METAL)
            .noOcclusion(), UtilityConnectorBlock.UtilityType.STRUCTURAL));
    
    // Circuit Breaker
    public static final DeferredBlock<CircuitBreakerBlock> CIRCUIT_BREAKER = registerConnectorBlock("circuit_breaker",
        () -> new CircuitBreakerBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .strength(2.5F)
            .sound(SoundType.METAL)
            .noOcclusion()));
    
    // Fencing
    public static final DeferredBlock<ChainLinkFenceBlock> CHAIN_LINK_FENCE = registerBlock("chain_link_fence",
        () -> new ChainLinkFenceBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(3.0F)
            .sound(SoundType.METAL)
            .noOcclusion()));

    public static final DeferredBlock<ConcreteBarrierBlock> CONCRETE_BARRIER = registerBlock("concrete_barrier",
            () -> new ConcreteBarrierBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));
    
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = ModularInfrastructure.BLOCKS.register(name, block);
        registerSimpleBlockItem(name, toReturn);
        return toReturn;
    }
    
    private static <T extends Block> DeferredBlock<T> registerPostBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = ModularInfrastructure.BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    
    private static <T extends Block> DeferredBlock<T> registerConnectorBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = ModularInfrastructure.BLOCKS.register(name, block);
        registerConnectorBlockItem(name, toReturn);
        return toReturn;
    }
    
    private static <T extends Block> DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<T> block) {
        return ModularInfrastructure.ITEMS.register(name, () -> new PostBlockItem(block.get(), new Item.Properties()));
    }
    
    private static <T extends Block> DeferredItem<BlockItem> registerSimpleBlockItem(String name, DeferredBlock<T> block) {
        return ModularInfrastructure.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    
    private static <T extends Block> DeferredItem<BlockItem> registerConnectorBlockItem(String name, DeferredBlock<T> block) {
        return ModularInfrastructure.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    
    public static void init() {
        // Static initialization
    }
    
    public static void addCreativeTabItems(CreativeModeTab.Output output) {
        // Posts
        output.accept(OAK_POST.get());
        output.accept(BIRCH_POST.get());
        output.accept(SPRUCE_POST.get());
        output.accept(IRON_POST.get());
        output.accept(CONCRETE_POST.get());
        
        // Power Connectors
        output.accept(POWER_CONNECTOR_LV.get());
        output.accept(POWER_CONNECTOR_MV.get());
        output.accept(POWER_CONNECTOR_HV.get());
        
        // Data Connectors
        output.accept(DATA_CONNECTOR_BASIC.get());
        output.accept(DATA_CONNECTOR_ADVANCED.get());
        
        // Utility Connectors
        output.accept(REDSTONE_CONNECTOR.get());
        output.accept(STRUCTURAL_CONNECTOR.get());
        
        // Circuit Breaker
        output.accept(CIRCUIT_BREAKER.get());
        
        // Fencing
        output.accept(CHAIN_LINK_FENCE.get());

        // Barriers
        output.accept(CONCRETE_BARRIER.get());
    }
}