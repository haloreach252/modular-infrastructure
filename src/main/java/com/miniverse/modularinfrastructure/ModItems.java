package com.miniverse.modularinfrastructure;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import com.miniverse.modularinfrastructure.item.PostConfiguratorItem;
import com.miniverse.modularinfrastructure.item.WireConnectorItem;
import com.miniverse.modularinfrastructure.item.WireCuttersItem;

public class ModItems {
    // Tools
    public static final DeferredItem<PostConfiguratorItem> POST_CONFIGURATOR = 
        ModularInfrastructure.ITEMS.register("post_configurator", 
            () -> new PostConfiguratorItem(new Item.Properties().stacksTo(1)));
    
    public static final DeferredItem<WireConnectorItem> WIRE_CONNECTOR = 
        ModularInfrastructure.ITEMS.register("wire_connector", 
            () -> new WireConnectorItem(new Item.Properties().stacksTo(1)));
    
    public static final DeferredItem<WireCuttersItem> WIRE_CUTTERS = 
        ModularInfrastructure.ITEMS.register("wire_cutters", 
            () -> new WireCuttersItem(new Item.Properties().stacksTo(1).durability(250)));
    
    public static void init() {
        // Static initialization
    }
    
    public static void addCreativeTabItems(CreativeModeTab.Output output) {
        output.accept(POST_CONFIGURATOR.get());
        output.accept(WIRE_CONNECTOR.get());
        output.accept(WIRE_CUTTERS.get());
    }
}