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
import com.miniverse.modularinfrastructure.api.wires.WireType;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.EnergyTransferHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.WireDamageHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.WireDamageHandler.IShockingWire;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.ILocalHandlerConstructor;
import com.miniverse.modularinfrastructure.api.tool.IElectricEquipment;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public class ModWireTypes
{
	// Power wires
	public static final ShockingWire COPPER_LV = new ShockingWire("modularinfrastructure:copper_lv", 64, 0.125, 0xd4804a, 2048, WireType.LV_CATEGORY, 0.05f);
	public static final ShockingWire ELECTRUM = new ShockingWire("modularinfrastructure:electrum_mv", 128, 0.15625, 0xeda045, 8192, WireType.MV_CATEGORY, 0.1f);
	public static final ShockingWire STEEL = new ShockingWire("modularinfrastructure:steel_hv", 256, 0.1875, 0x6e6e6e, 32768, WireType.HV_CATEGORY, 0.3f);
	
	// Data wires
	public static final BasicWire DATA_CABLE = new BasicWire("modularinfrastructure:data_cable", 32, 0.125, 0x24c6e8, "modularinfrastructure:data");
	public static final BasicWire DENSE_CABLE = new BasicWire("modularinfrastructure:dense_cable", 64, 0.1875, 0x1a9dc4, "modularinfrastructure:data");
	
	// Redstone wire
	public static final BasicWire REDSTONE_WIRE = new BasicWire("modularinfrastructure:redstone_wire", 32, 0.125, 0xff0000, WireType.REDSTONE_CATEGORY);
	
	// Structural wire
	public static final BasicWire ROPE = new BasicWire("modularinfrastructure:rope", 32, 0.25, 0x967969, WireType.STRUCTURE_CATEGORY);
	
	// Register all wire types
	public static void register()
	{
		// Wire types will be registered when the mod initializes
	}
	
	// Basic wire implementation
	private static class BasicWire extends WireType
	{
		private final String uniqueName;
		private final int maxLength;
		private final double renderDiameter;
		private final int color;
		private final String category;
		
		public BasicWire(String uniqueName, int maxLength, double renderDiameter, int color, String category)
		{
			super();
			this.uniqueName = uniqueName;
			this.maxLength = maxLength;
			this.renderDiameter = renderDiameter;
			this.color = color;
			this.category = category;
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
			return 1.005;
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
		
		public ShockingWire(String uniqueName, int maxLength, double renderDiameter, int color, int transferRate, String category, float damageRadius)
		{
			super(uniqueName, maxLength, renderDiameter, color, category);
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
			return ImmutableList.of(WireDamageHandler.ID);
		}
	}
}