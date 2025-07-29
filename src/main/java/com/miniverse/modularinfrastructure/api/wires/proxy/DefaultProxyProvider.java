// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.api.wires.proxy;

import com.miniverse.modularinfrastructure.api.wires.Connection;
import com.miniverse.modularinfrastructure.api.wires.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wires.IImmersiveConnectable;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.Collection;

public record DefaultProxyProvider(Level world) implements IICProxyProvider
{

	@Override
	public CompoundTag toNBT(IImmersiveConnectable proxy)
	{
		Preconditions.checkArgument(proxy instanceof IICProxy, "Expected IICProxy, got "+proxy);
		return ((IICProxy)proxy).writeToNBT();
	}

	@Override
	public IImmersiveConnectable fromNBT(CompoundTag nbt)
	{
		return IICProxy.readFromNBT(world, nbt);
	}

	@Override
	public IImmersiveConnectable create(BlockPos pos, Collection<Connection> internal, Collection<ConnectionPoint> points)
	{
		return new IICProxy(world, pos, internal, points);
	}
}