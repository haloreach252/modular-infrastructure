package com.miniverse.modularinfrastructure;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ModularInfrastructure.MODID)
public class ModularInfrastructure {
    public static final String MODID = "modularinfrastructure";
    public static final String MOD_ID = MODID; // Alternative constant name for compatibility
    public static final Logger LOGGER = LogUtils.getLogger();
    
    // Registries
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creative Tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MODULAR_TAB = CREATIVE_MODE_TABS.register("modular_tab", 
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.modularinfrastructure"))
            .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .icon(() -> ModBlocks.OAK_POST.get().asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // Post blocks will be added here
                ModBlocks.addCreativeTabItems(output);
                // Tools will be added here
                ModItems.addCreativeTabItems(output);
            }).build());

    public ModularInfrastructure(IEventBus modEventBus, ModContainer modContainer) {
        // Register event listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(com.miniverse.modularinfrastructure.datagen.DataGenerators::gatherData);
        
        // Register deferred registries
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        
        // Initialize content
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        com.miniverse.modularinfrastructure.common.data.ModDataAttachments.register(modEventBus);
        com.miniverse.modularinfrastructure.api.IEApiDataComponents.register(modEventBus);
        
        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Modular Infrastructure Common Setup");
        
        // Register wire types
        event.enqueueWork(() -> {
            com.miniverse.modularinfrastructure.common.wires.ModWireTypes.register();
            
            // Initialize GlobalWireNetwork static fields
            com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork.GET_NET_UNCACHED.set(
                level -> com.miniverse.modularinfrastructure.common.wires.WireNetworkCreator.getOrCreateNetwork(level)
            );
            com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork.SANITIZE_CONNECTIONS.set(() -> false);
            com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork.VALIDATE_CONNECTIONS.set(() -> false);
            
            // Initialize wire coil use handler
            com.miniverse.modularinfrastructure.api.wires.utils.WirecoilUtils.COIL_USE.set(
                new com.miniverse.modularinfrastructure.common.wires.WireCoilUseHandler()
            );
            
            // Register local network handlers
            com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler.register(
                com.miniverse.modularinfrastructure.api.wires.localhandlers.EnergyTransferHandler.ID,
                com.miniverse.modularinfrastructure.api.wires.localhandlers.EnergyTransferHandler::new
            );
            com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler.register(
                com.miniverse.modularinfrastructure.api.wires.redstone.RedstoneNetworkHandler.ID,
                com.miniverse.modularinfrastructure.api.wires.redstone.RedstoneNetworkHandler::new
            );
            com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler.register(
                com.miniverse.modularinfrastructure.api.wires.localhandlers.WireDamageHandler.ID,
                com.miniverse.modularinfrastructure.api.wires.localhandlers.WireDamageHandler::new
            );
        });
    }
    
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        LOGGER.info("Registering capabilities");
        
        // Register energy capability for power connectors
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntities.POWER_CONNECTOR.get(),
            (be, side) -> {
                // Expose capability only on the opposite side of facing (where the connector attaches to machines)
                // Also expose for null (direct access)
                if (side == null || side == be.getBlockState().getValue(com.miniverse.modularinfrastructure.block.PowerConnectorBlock.FACING).getOpposite()) {
                    return be.getEnergyCapability();
                }
                return null;
            }
        );
    }
}
