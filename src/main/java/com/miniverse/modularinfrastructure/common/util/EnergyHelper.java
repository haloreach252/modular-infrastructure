// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.common.util;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.energy.EnergyStorage;

public class EnergyHelper
{
	public static final String ENERGY_KEY = "energy";

	public static void deserializeFrom(EnergyStorage storage, CompoundTag mainTag, Provider provider)
	{
		Tag subtag;
		if(mainTag.contains(ENERGY_KEY, Tag.TAG_INT))
			subtag = mainTag.get(ENERGY_KEY);
		else
			subtag = IntTag.valueOf(0);
		storage.deserializeNBT(provider, subtag);
	}

	public static void serializeTo(EnergyStorage storage, CompoundTag mainTag, Provider provider)
	{
		mainTag.put(ENERGY_KEY, storage.serializeNBT(provider));
	}
}