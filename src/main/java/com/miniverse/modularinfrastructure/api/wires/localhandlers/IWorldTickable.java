// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.api.wires.localhandlers;

import net.minecraft.world.level.Level;

public interface IWorldTickable
{
	void update(Level w);
}