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
import com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.WireDamageHandler;

import java.util.Collection;
import java.util.Map;

public class ModWireTypes
{
	// Power wires
	public static final WireType COPPER_LV = new WireType(64, 0.125, 0xd4804a)
			.withTransferRate(2048) // LV transfer rate
			.setCategory(WireType.POWER_CATEGORY)
			.setUniqueName("modularinfrastructure:copper_lv")
			.addLocalHandler(EnergyTransferHandler::new)
			.addLocalHandler(WireDamageHandler.INSTANCE::create);
	
	public static final WireType ELECTRUM = new WireType(128, 0.15625, 0xeda045)
			.withTransferRate(8192) // MV transfer rate
			.setCategory(WireType.POWER_CATEGORY)
			.setUniqueName("modularinfrastructure:electrum_mv")
			.addLocalHandler(EnergyTransferHandler::new)
			.addLocalHandler(WireDamageHandler.INSTANCE::create);
	
	public static final WireType STEEL = new WireType(256, 0.1875, 0x6e6e6e)
			.withTransferRate(32768) // HV transfer rate
			.setCategory(WireType.POWER_CATEGORY)
			.setUniqueName("modularinfrastructure:steel_hv")
			.addLocalHandler(EnergyTransferHandler::new)
			.addLocalHandler(WireDamageHandler.INSTANCE::create);
	
	// Data wires
	public static final WireType DATA_CABLE = new WireType(32, 0.125, 0x24c6e8)
			.setCategory("modularinfrastructure:data")
			.setUniqueName("modularinfrastructure:data_cable");
	
	public static final WireType DENSE_CABLE = new WireType(64, 0.1875, 0x1a9dc4)
			.setCategory("modularinfrastructure:data")
			.setUniqueName("modularinfrastructure:dense_cable");
	
	// Redstone wire
	public static final WireType REDSTONE_WIRE = new WireType(32, 0.125, 0xff0000)
			.setCategory(WireType.REDSTONE_CATEGORY)
			.setUniqueName("modularinfrastructure:redstone_wire");
	
	// Structural wire
	public static final WireType ROPE = new WireType(32, 0.25, 0x967969)
			.setCategory(WireType.STRUCTURE_CATEGORY)
			.setUniqueName("modularinfrastructure:rope");
	
	// Register all wire types
	public static void register()
	{
		// Wire types will be registered when the mod initializes
	}
}