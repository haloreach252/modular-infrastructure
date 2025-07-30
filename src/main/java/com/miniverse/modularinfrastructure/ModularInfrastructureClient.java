package com.miniverse.modularinfrastructure;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ModularInfrastructure.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ModularInfrastructure.MODID, value = Dist.CLIENT)
public class ModularInfrastructureClient {
    public ModularInfrastructureClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ModularInfrastructure.LOGGER.info("HELLO FROM CLIENT SETUP");
        ModularInfrastructure.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        
        // Register item properties
        event.enqueueWork(() -> {
            registerItemProperties();
        });
    }
    
    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Block entity renderers removed - using section-based rendering instead
        // Wire rendering is now handled by WireRenderer using AddSectionGeometryEvent
    }
    
    private static void registerItemProperties() {
        net.minecraft.client.renderer.item.ItemProperties.register(
            ModItems.POST_CONFIGURATOR.get(),
            ResourceLocation.fromNamespaceAndPath(ModularInfrastructure.MODID, "mode"),
            (stack, level, entity, seed) -> {
                var mode = getConfiguratorMode(stack);
                return switch (mode) {
                    case COPY -> 0.0f;
                    case PASTE -> 1.0f;
                    case BATCH -> 2.0f;
                };
            }
        );
    }
    
    private static com.miniverse.modularinfrastructure.item.PostConfiguratorItem.Mode getConfiguratorMode(net.minecraft.world.item.ItemStack stack) {
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains("Mode")) {
            String modeName = customData.copyTag().getString("Mode");
            try {
                return com.miniverse.modularinfrastructure.item.PostConfiguratorItem.Mode.valueOf(modeName);
            } catch (IllegalArgumentException e) {
                // Invalid mode, return default
            }
        }
        return com.miniverse.modularinfrastructure.item.PostConfiguratorItem.Mode.COPY;
    }
}
