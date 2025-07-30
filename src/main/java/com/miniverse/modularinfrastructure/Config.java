package com.miniverse.modularinfrastructure;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    // Wire settings
    public static class Wires {
        public static final ModConfigSpec.BooleanValue ENABLE_WIRE_DAMAGE;
        public static final ModConfigSpec.BooleanValue BLOCKS_BREAK_WIRES;
        
        static {
            BUILDER.push("wires");
            
            ENABLE_WIRE_DAMAGE = BUILDER
                    .comment("Whether wires can damage entities")
                    .define("enableWireDamage", true);
                    
            BLOCKS_BREAK_WIRES = BUILDER
                    .comment("Whether blocks placed in wire paths will break the wires")
                    .define("blocksBreakWires", true);
                    
            BUILDER.pop();
        }
    }
    
    static final ModConfigSpec SPEC = BUILDER.build();
}
