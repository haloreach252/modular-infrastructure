package com.miniverse.modularinfrastructure.api;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.wires.utils.WireLink;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Data components for the API
 * Based on Immersive Engineering's implementation
 */
public class IEApiDataComponents {
    private static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(
            net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, ModularInfrastructure.MOD_ID
    );
    
    public static final Supplier<DataComponentType<WireLink>> WIRE_LINK = REGISTER.register(
            "wire_link",
            () -> DataComponentType.<WireLink>builder()
                    .persistent(WireLink.CODECS.codec())
                    .networkSynchronized(WireLink.CODECS.streamCodec())
                    .build()
    );
    
    public static void register(net.neoforged.bus.api.IEventBus modBus) {
        REGISTER.register(modBus);
    }
}