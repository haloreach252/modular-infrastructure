package com.miniverse.modularinfrastructure.common.config;

import com.miniverse.modularinfrastructure.common.wires.IEWireTypes;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;

import java.util.EnumMap;
import java.util.Map;

public class IEServerConfig {
    public static final ModConfigSpec CONFIG_SPEC;
    public static final Wires WIRES;
    
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        WIRES = new Wires(builder);
        CONFIG_SPEC = builder.build();
    }
    
    public static class Wires {
        public final BooleanValue enableWireDamage;
        public final BooleanValue blocksBreakWires;
        public final Map<IEWireTypes.IEWireType, WireConfig> wireConfigs = new EnumMap<>(IEWireTypes.IEWireType.class);
        public final Map<IEWireTypes.IEWireType, EnergyWireConfig> energyWireConfigs = new EnumMap<>(IEWireTypes.IEWireType.class);
        
        Wires(ModConfigSpec.Builder builder) {
            builder.comment("Configuration related to Modular Infrastructure wires").push("wires");
            
            // Create energy wire configs
            energyWireConfigs.put(
                IEWireTypes.IEWireType.COPPER,
                new EnergyWireConfig(builder, "copper", 16, 2048, 0.0125)
            );
            energyWireConfigs.put(
                IEWireTypes.IEWireType.ELECTRUM,
                new EnergyWireConfig(builder, "electrum", 16, 8192, 0.003)
            );
            energyWireConfigs.put(
                IEWireTypes.IEWireType.STEEL,
                new EnergyWireConfig(builder, "hv", 32, 32768, 0.0008)
            );
            
            // Create regular wire configs
            wireConfigs.put(
                IEWireTypes.IEWireType.STRUCTURE_ROPE,
                new WireConfig(builder, "rope", 32)
            );
            wireConfigs.put(
                IEWireTypes.IEWireType.STRUCTURE_STEEL,
                new WireConfig(builder, "cable", 32)
            );
            wireConfigs.put(
                IEWireTypes.IEWireType.REDSTONE,
                new WireConfig(builder, "redstone", 32)
            );
            wireConfigs.put(
                IEWireTypes.IEWireType.COPPER_INSULATED,
                new WireConfig(builder, "insulated_copper", 16)
            );
            wireConfigs.put(
                IEWireTypes.IEWireType.ELECTRUM_INSULATED,
                new WireConfig(builder, "insulated_electrum", 16)
            );
            
            // Add energy wire configs to wire configs map
            wireConfigs.putAll(energyWireConfigs);
            
            enableWireDamage = builder
                .comment("If this is enabled, wires connected to power sources will cause damage to entities touching them")
                .define("enableWireDamage", true);
                
            blocksBreakWires = builder
                .comment("If this is enabled, placing a block in a wire will break it (drop the wire coil)")
                .define("blocksBreakWires", true);
                
            builder.pop();
        }
        
        public static class WireConfig {
            public final IntValue maxLength;
            
            protected WireConfig(ModConfigSpec.Builder builder, String name, int defLength, boolean doPop) {
                builder.push(name);
                maxLength = builder.comment("The maximum length of " + name + " wires")
                        .defineInRange("maxLength", defLength, 0, Integer.MAX_VALUE);
                if (doPop)
                    builder.pop();
            }
            
            public WireConfig(ModConfigSpec.Builder builder, String name, int defLength) {
                this(builder, name, defLength, true);
            }
        }
        
        public static class EnergyWireConfig extends WireConfig {
            public final IntValue transferRate;
            public final IntValue connectorRate;
            public final DoubleValue lossRatio;
            
            public EnergyWireConfig(Builder builder, String name, int defLength, int defRate, double defLoss) {
                super(builder, name, defLength, false);
                this.transferRate = builder.comment("The transfer rate of " + name + " wire in FE/t")
                        .defineInRange("transferRate", defRate, 0, Integer.MAX_VALUE);
                this.lossRatio = builder.comment("The percentage of received power lost every 16 blocks of distance in " + name + " wire. This means exponential loss!")
                        .defineInRange("distanceLoss", defLoss, 0, 1);
                this.connectorRate = builder
                        .comment("In- and output rates of " + name + " wire connectors. This is independent of the transfer rate of the wires.")
                        .defineInRange("wireConnectorInput", defRate / 8, 0, Integer.MAX_VALUE);
                builder.pop();
            }
        }
    }
}