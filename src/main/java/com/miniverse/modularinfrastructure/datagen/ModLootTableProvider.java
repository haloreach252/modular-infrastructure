package com.miniverse.modularinfrastructure.datagen;

import com.miniverse.modularinfrastructure.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)
        ), registries);
    }
    
    private static class ModBlockLootTables extends BlockLootSubProvider {
        protected ModBlockLootTables(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }
        
        @Override
        protected void generate() {
            // Posts - drop themselves with NBT data preserved
            dropSelf(ModBlocks.OAK_POST.get());
            dropSelf(ModBlocks.BIRCH_POST.get());
            dropSelf(ModBlocks.SPRUCE_POST.get());
            dropSelf(ModBlocks.IRON_POST.get());
            dropSelf(ModBlocks.CONCRETE_POST.get());
            
            // Power Connectors - simple drops
            dropSelf(ModBlocks.POWER_CONNECTOR_LV.get());
            dropSelf(ModBlocks.POWER_CONNECTOR_MV.get());
            dropSelf(ModBlocks.POWER_CONNECTOR_HV.get());
            
            // Data Connectors - simple drops
            dropSelf(ModBlocks.DATA_CONNECTOR_BASIC.get());
            dropSelf(ModBlocks.DATA_CONNECTOR_ADVANCED.get());
            
            // Utility Connectors - simple drops
            dropSelf(ModBlocks.REDSTONE_CONNECTOR.get());
            dropSelf(ModBlocks.STRUCTURAL_CONNECTOR.get());
        }
        
        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                ModBlocks.OAK_POST.get(),
                ModBlocks.BIRCH_POST.get(),
                ModBlocks.SPRUCE_POST.get(),
                ModBlocks.IRON_POST.get(),
                ModBlocks.CONCRETE_POST.get(),
                ModBlocks.POWER_CONNECTOR_LV.get(),
                ModBlocks.POWER_CONNECTOR_MV.get(),
                ModBlocks.POWER_CONNECTOR_HV.get(),
                ModBlocks.DATA_CONNECTOR_BASIC.get(),
                ModBlocks.DATA_CONNECTOR_ADVANCED.get(),
                ModBlocks.REDSTONE_CONNECTOR.get(),
                ModBlocks.STRUCTURAL_CONNECTOR.get()
            );
        }
    }
}