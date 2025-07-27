package com.miniverse.modularinfrastructure.datagen;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.ModBlocks;
import com.miniverse.modularinfrastructure.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    
    public ModLanguageProvider(PackOutput output) {
        super(output, ModularInfrastructure.MODID, "en_us");
    }
    
    @Override
    protected void addTranslations() {
        // Creative Tab
        add("itemGroup.modularinfrastructure", "Modular Infrastructure");
        
        // Blocks
        add(ModBlocks.OAK_POST.get(), "Oak Post");
        add(ModBlocks.BIRCH_POST.get(), "Birch Post");
        add(ModBlocks.SPRUCE_POST.get(), "Spruce Post");
        add(ModBlocks.IRON_POST.get(), "Iron Post");
        add(ModBlocks.CONCRETE_POST.get(), "Concrete Post");
        
        // Connectors
        add(ModBlocks.POWER_CONNECTOR_LV.get(), "LV Power Connector");
        add(ModBlocks.POWER_CONNECTOR_MV.get(), "MV Power Connector");
        add(ModBlocks.POWER_CONNECTOR_HV.get(), "HV Power Connector");
        add(ModBlocks.DATA_CONNECTOR_BASIC.get(), "Basic Data Connector");
        add(ModBlocks.DATA_CONNECTOR_ADVANCED.get(), "Advanced Data Connector");
        add(ModBlocks.REDSTONE_CONNECTOR.get(), "Redstone Connector");
        add(ModBlocks.STRUCTURAL_CONNECTOR.get(), "Structural Connector");
        
        // Items/Tools
        add(ModItems.POST_CONFIGURATOR.get(), "Post Configurator");
        add(ModItems.WIRE_CONNECTOR.get(), "Wire Connector");
        add(ModItems.WIRE_CUTTERS.get(), "Wire Cutters");
        
        // Tooltips
        add("item.modularinfrastructure.post_configurator.tooltip", "Configure post properties");
        add("item.modularinfrastructure.wire_connector.tooltip", "Connect wires between posts");
        add("item.modularinfrastructure.wire_cutters.tooltip", "Cut wire connections");
        
        // Config
        add("modularinfrastructure.configuration.title", "Modular Infrastructure Configs");
    }
}