// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.common.wires;

import com.miniverse.modularinfrastructure.api.utils.DirectionUtils;
import com.miniverse.modularinfrastructure.api.wire.Connection;
import com.miniverse.modularinfrastructure.api.wire.GlobalWireNetwork;
import com.miniverse.modularinfrastructure.api.wire.LocalWireNetwork;
import com.miniverse.modularinfrastructure.api.wire.WireCollisionData;
import com.miniverse.modularinfrastructure.api.wire.WireCollisionData.CollisionInfo;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.ICollisionHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler;
import com.miniverse.modularinfrastructure.api.wire.WireUtils;
import com.miniverse.modularinfrastructure.common.config.IEServerConfig;
import com.miniverse.modularinfrastructure.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class WireCollisions
{
	public static void handleEntityCollision(BlockPos p, Entity e)
	{
		if(!e.level().isClientSide&&IEServerConfig.WIRES.enableWireDamage.get()&&e instanceof LivingEntity living&&
				!(e instanceof Player player&&player.getAbilities().invulnerable))
		{
			GlobalWireNetwork global = GlobalWireNetwork.getNetwork(e.level());
			WireCollisionData wireData = global.getCollisionData();
			Iterator<CollisionInfo> infos = wireData.getCollisionInfo(p).iterator();
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized(infos)
			{
				while(infos.hasNext())
				{
					CollisionInfo info = infos.next();
					LocalWireNetwork local = info.getLocalNet(global);
					for(LocalNetworkHandler h : local.getAllHandlers())
						if(h instanceof ICollisionHandler collisionHandler)
							collisionHandler.onCollided(living, p, info);
				}
			}
		}
	}

	public static void notifyBlockUpdate(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, int flags)
	{
		if(IEServerConfig.WIRES.blocksBreakWires.get()&&!worldIn.isClientSide&&(flags&1)!=0&&!newState.getCollisionShape(worldIn, pos).isEmpty())
		{
			GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(worldIn);
			Collection<CollisionInfo> data = globalNet.getCollisionData().getCollisionInfo(pos);
			if(!data.isEmpty())
			{
				Map<Connection, BlockPos> toBreak = new HashMap<>();
				for(CollisionInfo info : data)
					if(info.isInBlock())
					{
						Vec3 vecA = info.connection().getPoint(0, info.connection().getEndA());
						if(Utils.isVecInBlock(vecA, pos, info.connection().getEndA().position(), 1e-3))
							continue;
						Vec3 vecB = info.connection().getPoint(0, info.connection().getEndB());
						if(Utils.isVecInBlock(vecB, pos, info.connection().getEndB().position(), 1e-3))
							continue;
						BlockPos dropPos = pos;
						if(WireUtils.preventsConnection(worldIn, pos, newState, info.intersectA(), info.intersectB()))
						{
							for(Direction f : DirectionUtils.VALUES)
								if(worldIn.isEmptyBlock(pos.relative(f)))
								{
									dropPos = dropPos.relative(f);
									break;
								}
							toBreak.put(info.connection(), dropPos);
						}
					}
				for(Entry<Connection, BlockPos> b : toBreak.entrySet())
					globalNet.removeAndDropConnection(b.getKey(), b.getValue(), worldIn);
			}
		}
	}
}