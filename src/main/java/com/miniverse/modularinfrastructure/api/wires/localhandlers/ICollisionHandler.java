// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.api.wires.localhandlers;

import com.miniverse.modularinfrastructure.api.wires.WireCollisionData.CollisionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

public interface ICollisionHandler
{
	//TODO more params!
	void onCollided(LivingEntity e, BlockPos pos, CollisionInfo info);
}