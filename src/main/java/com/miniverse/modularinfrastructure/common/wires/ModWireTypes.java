// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.common.wires;

import com.miniverse.modularinfrastructure.api.wires.Connection;
import com.miniverse.modularinfrastructure.api.wires.WireApi;
import com.miniverse.modularinfrastructure.api.wires.WireType;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.EnergyTransferHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.WireDamageHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.WireDamageHandler.IShockingWire;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.ILocalHandlerConstructor;
import com.miniverse.modularinfrastructure.api.tool.IElectricEquipment;
import com.miniverse.modularinfrastructure.common.wires.WireConfig.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModWireTypes
{
	// Wire type registry for NBT serialization
	private static final Map<String, WireType> WIRE_TYPE_REGISTRY = new HashMap<>();
	
	// Power wires - using values from WireConfig
	public static final ShockingWire COPPER_LV = new ShockingWire(
		"modularinfrastructure:copper_lv", 
		PowerWires.COPPER_MAX_LENGTH, 
		PowerWires.COPPER_THICKNESS, 
		PowerWires.COPPER_COLOR, 
		PowerWires.COPPER_TRANSFER_RATE, 
		WireType.LV_CATEGORY, 
		PowerWires.COPPER_DAMAGE_RADIUS,
		PowerWires.COPPER_SAG
	);
	
	public static final ShockingWire ELECTRUM = new ShockingWire(
		"modularinfrastructure:electrum_mv", 
		PowerWires.ELECTRUM_MAX_LENGTH, 
		PowerWires.ELECTRUM_THICKNESS, 
		PowerWires.ELECTRUM_COLOR, 
		PowerWires.ELECTRUM_TRANSFER_RATE, 
		WireType.MV_CATEGORY, 
		PowerWires.ELECTRUM_DAMAGE_RADIUS,
		PowerWires.ELECTRUM_SAG
	);
	
	public static final ShockingWire STEEL = new ShockingWire(
		"modularinfrastructure:steel_hv", 
		PowerWires.STEEL_MAX_LENGTH, 
		PowerWires.STEEL_THICKNESS, 
		PowerWires.STEEL_COLOR, 
		PowerWires.STEEL_TRANSFER_RATE, 
		WireType.HV_CATEGORY, 
		PowerWires.STEEL_DAMAGE_RADIUS,
		PowerWires.STEEL_SAG
	);
	
	// Data wires - using values from WireConfig
	public static final BasicWire DATA_CABLE = new BasicWire(
		"modularinfrastructure:data_cable", 
		DataWires.DATA_MAX_LENGTH, 
		DataWires.DATA_THICKNESS, 
		DataWires.DATA_COLOR, 
		"modularinfrastructure:data",
		DataWires.DATA_SAG
	);
	
	public static final BasicWire DENSE_CABLE = new BasicWire(
		"modularinfrastructure:dense_cable", 
		DataWires.DENSE_MAX_LENGTH, 
		DataWires.DENSE_THICKNESS, 
		DataWires.DENSE_COLOR, 
		"modularinfrastructure:data",
		DataWires.DENSE_SAG
	);
	
	// Redstone wire - using values from WireConfig
	public static final BasicWire REDSTONE_WIRE = new BasicWire(
		"modularinfrastructure:redstone_wire", 
		UtilityWires.REDSTONE_MAX_LENGTH, 
		UtilityWires.REDSTONE_THICKNESS, 
		UtilityWires.REDSTONE_COLOR, 
		WireType.REDSTONE_CATEGORY,
		UtilityWires.REDSTONE_SAG
	);
	
	// Structural wire - using values from WireConfig
	public static final BasicWire ROPE = new BasicWire(
		"modularinfrastructure:rope", 
		UtilityWires.ROPE_MAX_LENGTH, 
		UtilityWires.ROPE_THICKNESS, 
		UtilityWires.ROPE_COLOR, 
		WireType.STRUCTURE_CATEGORY,
		UtilityWires.ROPE_SAG
	);
	
	// Register all wire types
	public static void register()
	{
		// Register wire types with the API
		registerWireType(COPPER_LV);
		registerWireType(ELECTRUM);
		registerWireType(STEEL);
		registerWireType(DATA_CABLE);
		registerWireType(DENSE_CABLE);
		registerWireType(REDSTONE_WIRE);
		registerWireType(ROPE);
	}
	
	private static void registerWireType(WireType type)
	{
		WireApi.registerWireType(type);
		WIRE_TYPE_REGISTRY.put(type.getUniqueName(), type);
	}
	
	public static WireType getWireType(String name)
	{
		return WIRE_TYPE_REGISTRY.get(name);
	}
	
	// Basic wire implementation
	private static class BasicWire extends WireType
	{
		private final String uniqueName;
		private final int maxLength;
		private final double renderDiameter;
		private final int color;
		private final String category;
		private final double slack;
		
		public BasicWire(String uniqueName, int maxLength, double renderDiameter, int color, String category, double slack)
		{
			super();
			this.uniqueName = uniqueName;
			this.maxLength = maxLength;
			this.renderDiameter = renderDiameter;
			this.color = color;
			this.category = category;
			this.slack = slack;
		}
		
		@Override
		public String getUniqueName()
		{
			return uniqueName;
		}
		
		@Override
		public int getColour(Connection connection)
		{
			return color;
		}
		
		@Override
		public double getSlack()
		{
			return slack;
		}
		
		@Override
		public int getMaxLength()
		{
			return maxLength;
		}
		
		@Override
		public ItemStack getWireCoil(Connection con)
		{
			// TODO: Return actual wire coil item when available
			return ItemStack.EMPTY;
		}
		
		@Override
		public double getRenderDiameter()
		{
			return renderDiameter;
		}
		
		@Nonnull
		@Override
		public String getCategory()
		{
			return category;
		}
		
		@Override
		public Collection<ResourceLocation> getRequestedHandlers()
		{
			// Check if this is a data cable and AE2 is loaded
			if ("modularinfrastructure:data".equals(category) && 
			    com.miniverse.modularinfrastructure.integration.ae2.ModAE2Integration.isAE2Loaded()) {
				return ImmutableList.of(com.miniverse.modularinfrastructure.integration.ae2.ModAE2Integration.AE2_NETWORK_BRIDGE_HANDLER);
			}
			return ImmutableList.of();
		}
	}
	
	// Shocking wire implementation for power wires
	private static class ShockingWire extends BasicWire implements IEnergyWire, IShockingWire
	{
		private final int transferRate;
		private final float damageRadius;
		private final IElectricEquipment.ElectricSource electricSource;
		private static final ILocalHandlerConstructor WIRE_DAMAGE_CONSTRUCTOR = WireDamageHandler::new;
		
		public ShockingWire(String uniqueName, int maxLength, double renderDiameter, int color, int transferRate, String category, float damageRadius, double slack)
		{
			super(uniqueName, maxLength, renderDiameter, color, category, slack);
			this.transferRate = transferRate;
			this.damageRadius = damageRadius;
			this.electricSource = new IElectricEquipment.ElectricSource(damageRadius > 0 ? 0.5F : -1);
		}
		
		@Override
		public int getTransferRate()
		{
			return transferRate;
		}
		
		@Override
		public double getBasicLossRate(Connection c)
		{
			return 0.005 * c.getLength() / getMaxLength(); // 0.5% loss ratio
		}
		
		@Override
		public double getLossRate(Connection c, int transferred)
		{
			return 0;
		}
		
		@Override
		public double getDamageRadius()
		{
			return damageRadius;
		}
		
		@Override
		public IElectricEquipment.ElectricSource getElectricSource()
		{
			return electricSource;
		}
		
		@Override
		public float getDamageAmount(Entity e, Connection c, int energy)
		{
			float factor = damageRadius <= 0.05f ? 2F : damageRadius <= 0.1f ? 5F : 15F;
			return factor * energy / getTransferRate() * 8;
		}
		
		@Override
		public Collection<ResourceLocation> getRequestedHandlers()
		{
			// Handlers are requested by connectors, not wire types
			return ImmutableList.of();
		}
	}
}