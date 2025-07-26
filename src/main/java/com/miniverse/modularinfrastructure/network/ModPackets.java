package com.miniverse.modularinfrastructure.network;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.network.packets.UpdatePostItemWidthPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ModularInfrastructure.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModPackets {
    
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ModularInfrastructure.MODID)
            .versioned("1.0");
        
        registrar.playToServer(
            UpdatePostItemWidthPacket.TYPE,
            UpdatePostItemWidthPacket.STREAM_CODEC,
            UpdatePostItemWidthPacket::handle
        );
    }
}