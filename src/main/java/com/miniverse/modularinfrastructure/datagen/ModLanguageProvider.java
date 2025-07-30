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
        add(ModBlocks.CIRCUIT_BREAKER.get(), "Circuit Breaker");
        
        // Fencing
        add(ModBlocks.CHAIN_LINK_FENCE.get(), "Chain Link Fence");
        
        // Items/Tools
        add(ModItems.POST_CONFIGURATOR.get(), "Post Configurator");
        add(ModItems.WIRE_CONNECTOR.get(), "Wire Connector");
        add(ModItems.WIRE_CUTTERS.get(), "Wire Cutters");
        
        // Wire Coils
        add(ModItems.WIRECOIL_COPPER.get(), "Copper Wire Coil");
        add(ModItems.WIRECOIL_ELECTRUM.get(), "Electrum Wire Coil");
        add(ModItems.WIRECOIL_STEEL.get(), "Steel Wire Coil");
        add(ModItems.WIRECOIL_DATA.get(), "Data Cable Coil");
        add(ModItems.WIRECOIL_DENSE_DATA.get(), "Dense Data Cable Coil");
        add(ModItems.WIRECOIL_REDSTONE.get(), "Redstone Wire Coil");
        add(ModItems.WIRECOIL_ROPE.get(), "Structural Rope");
        
        // Tooltips
        add("item.modularinfrastructure.post_configurator.tooltip", "Configure post properties");
        add("item.modularinfrastructure.wire_connector.tooltip", "Connect wires between posts");
        add("item.modularinfrastructure.wire_cutters.tooltip", "Cut wire connections");
        
        // Wire Coil Tooltips
        add("tooltip.modularinfrastructure.wire_coil.type", "Type: %s");
        add("tooltip.modularinfrastructure.wire_coil.max_length", "Max Length: %s blocks");
        add("tooltip.modularinfrastructure.wire_coil.linking", "Linking from: %s");
        
        // Wire Messages
        add("message.modularinfrastructure.wire_connection_cancelled", "Wire connection cancelled");
        add("message.modularinfrastructure.wire_too_long", "Wire too long! Maximum length: %s blocks");
        add("message.modularinfrastructure.wire_connected", "Wire connected!");
        add("message.modularinfrastructure.wire_connection_failed", "Connection failed - incompatible connector or no space");
        add("message.modularinfrastructure.wire_linking", "Wire linking started at %s");
        
        // Chat Messages for Wire Connections
        add("modularinfrastructure.chat.error.wrongCable", "This cable is not compatible with this connector");
        add("modularinfrastructure.chat.error.wrongDimension", "Connection must be in the same dimension");
        add("modularinfrastructure.chat.error.sameConnection", "Cannot connect to the same point");
        add("modularinfrastructure.chat.error.tooFar", "Connection is too far");
        add("modularinfrastructure.chat.error.invalidPoint", "Invalid connection point");
        add("modularinfrastructure.chat.error.connectionExists", "Connection already exists");
        add("modularinfrastructure.chat.error.cantSee", "Cannot see connection point - path is obstructed");
        add("modularinfrastructure.chat.info.connectionStarted", "Connection started at [%s, %s, %s]");
        
        // Config
        add("modularinfrastructure.configuration.title", "Modular Infrastructure Configs");
    }
}