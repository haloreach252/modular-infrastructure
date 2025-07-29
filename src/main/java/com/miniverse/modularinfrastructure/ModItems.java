package com.miniverse.modularinfrastructure;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import com.miniverse.modularinfrastructure.item.PostConfiguratorItem;
import com.miniverse.modularinfrastructure.item.WireConnectorItem;
import com.miniverse.modularinfrastructure.item.WireCuttersItem;
import com.miniverse.modularinfrastructure.common.items.WireCoilItem;
import com.miniverse.modularinfrastructure.common.wires.ModWireTypes;

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
    
    // Wire Coils
    public static final DeferredItem<WireCoilItem> WIRECOIL_COPPER = 
        ModularInfrastructure.ITEMS.register("wirecoil_copper", 
            () -> new WireCoilItem(ModWireTypes.COPPER_LV));
    
    public static final DeferredItem<WireCoilItem> WIRECOIL_ELECTRUM = 
        ModularInfrastructure.ITEMS.register("wirecoil_electrum", 
            () -> new WireCoilItem(ModWireTypes.ELECTRUM));
    
    public static final DeferredItem<WireCoilItem> WIRECOIL_STEEL = 
        ModularInfrastructure.ITEMS.register("wirecoil_steel", 
            () -> new WireCoilItem(ModWireTypes.STEEL));
    
    public static final DeferredItem<WireCoilItem> WIRECOIL_DATA = 
        ModularInfrastructure.ITEMS.register("wirecoil_data", 
            () -> new WireCoilItem(ModWireTypes.DATA_CABLE));
    
    public static final DeferredItem<WireCoilItem> WIRECOIL_DENSE_DATA = 
        ModularInfrastructure.ITEMS.register("wirecoil_dense_data", 
            () -> new WireCoilItem(ModWireTypes.DENSE_CABLE));
    
    public static final DeferredItem<WireCoilItem> WIRECOIL_REDSTONE = 
        ModularInfrastructure.ITEMS.register("wirecoil_redstone", 
            () -> new WireCoilItem(ModWireTypes.REDSTONE_WIRE));
    
    public static final DeferredItem<WireCoilItem> WIRECOIL_ROPE = 
        ModularInfrastructure.ITEMS.register("wirecoil_rope", 
            () -> new WireCoilItem(ModWireTypes.ROPE));
    
    public static void init() {
        // Static initialization
    }
    
    public static void addCreativeTabItems(CreativeModeTab.Output output) {
        output.accept(POST_CONFIGURATOR.get());
        output.accept(WIRE_CONNECTOR.get());
        output.accept(WIRE_CUTTERS.get());
        
        // Wire Coils
        output.accept(WIRECOIL_COPPER.get());
        output.accept(WIRECOIL_ELECTRUM.get());
        output.accept(WIRECOIL_STEEL.get());
        output.accept(WIRECOIL_DATA.get());
        output.accept(WIRECOIL_DENSE_DATA.get());
        output.accept(WIRECOIL_REDSTONE.get());
        output.accept(WIRECOIL_ROPE.get());
    }
}