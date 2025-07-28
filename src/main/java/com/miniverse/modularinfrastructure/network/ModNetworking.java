package com.miniverse.modularinfrastructure.network;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Network packet registration for Modular Infrastructure
 */
@EventBusSubscriber(modid = ModularInfrastructure.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {
    
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        
        // Register wire sync packet
        registrar.playToClient(
            MessageWireSync.TYPE,
            MessageWireSync.CODEC,
            MessageWireSync::handle
        );
    }
}