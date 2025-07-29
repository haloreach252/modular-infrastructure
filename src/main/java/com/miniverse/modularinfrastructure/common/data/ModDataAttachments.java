package com.miniverse.modularinfrastructure.common.data;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork;
import com.miniverse.modularinfrastructure.common.wires.WireNetworkCreator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModDataAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = 
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ModularInfrastructure.MOD_ID);
    
    public static final Supplier<AttachmentType<GlobalWireNetwork>> WIRE_NETWORK = ATTACHMENT_TYPES.register(
        "wire_network",
        () -> AttachmentType.builder(WireNetworkCreator.CREATOR)
            .serialize(WireNetworkCreator.SERIALIZER)
            .build()
    );
    
    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}