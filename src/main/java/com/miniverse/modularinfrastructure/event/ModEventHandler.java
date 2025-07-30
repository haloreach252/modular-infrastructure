package com.miniverse.modularinfrastructure.event;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Main event handler for Modular Infrastructure
 */
@EventBusSubscriber(modid = ModularInfrastructure.MODID)
public class ModEventHandler {
    
    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Pre event) {
        final var level = event.getLevel();
        if (level.isClientSide) {
            return;
        }
        
        // Update the wire network
        GlobalWireNetwork.getNetwork(level).update(level);
    }
}