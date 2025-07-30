// Originally from Immersive Engineering, adapted under its license
/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.common.wires;

import com.miniverse.modularinfrastructure.api.Lib;
import com.miniverse.modularinfrastructure.api.wires.*;
import com.miniverse.modularinfrastructure.api.wires.IConnectionTemplate.TemplateConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;

public class WireTemplateHelper
{
	private static final String CONNECTIONS_KEY = Lib.MODID+":connections";

	public static void fillConnectionsInArea(
			Level worldIn, BlockPos startPos, Vec3i size, IConnectionTemplate template
	)
	{
		template.getStoredConnections().clear();
		GlobalWireNetwork net = GlobalWireNetwork.getNetwork(worldIn);
		if (net == null)
			return;
		BlockPos endPos = startPos.offset(size).offset(-1, -1, -1);
		BoundingBox box = BoundingBox.fromCorners(startPos, endPos);
		Vec3i offset = new Vec3i(box.minX(), box.minY(), box.minZ());
		for(BlockPos pos : BlockPos.betweenClosed(startPos, endPos))
		{
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(!(te instanceof IImmersiveConnectable))
				continue;
			for(ConnectionPoint cp : ((IImmersiveConnectable)te).getConnectionPoints())
				for(Connection conn : net.getLocalNet(cp).getConnections(cp))
				{
					if(conn.isInternal())
						continue;
					ConnectionPoint otherEnd = conn.getOtherEnd(cp);
					if(otherEnd.compareTo(cp) < 0||!box.isInside(otherEnd.position()))
						// only add once and only if fully in captured area
						continue;
					template.getStoredConnections().add(new TemplateConnection(
							new ConnectionPoint(pos.subtract(offset), cp.index()),
							new ConnectionPoint(otherEnd.position().subtract(offset), otherEnd.index()),
							conn.type
					));
				}
		}
	}

	public static void addConnectionsFromTemplate(
			ServerLevelAccessor iworld, IConnectionTemplate template, StructurePlaceSettings orientation, BlockPos startPos
	)
	{
		if(template.getStoredConnections().isEmpty())
			return;
		Level world = iworld.getLevel();
		GlobalWireNetwork net = GlobalWireNetwork.getNetwork(world);
		if (net == null)
			return;
		for(TemplateConnection relative : template.getStoredConnections())
		{
			ConnectionPoint endA = getAbsolutePoint(relative.endA(), orientation, world, startPos);
			ConnectionPoint endB = getAbsolutePoint(relative.endB(), orientation, world, startPos);
			if(endA==null||endB==null)
				continue;
			net.addConnection(new Connection(relative.type(), endA, endB, net));
		}
	}

	public static void addConnectionsToNBT(IConnectionTemplate template, CompoundTag out)
	{
		if(template.getStoredConnections().isEmpty())
			return;
		ListTag connectionsNBT = new ListTag();
		for(TemplateConnection c : template.getStoredConnections())
			connectionsNBT.add(c.toNBT());
		out.put(CONNECTIONS_KEY, connectionsNBT);
	}

	public static void readConnectionsFromNBT(CompoundTag compound, IConnectionTemplate template)
	{
		ListTag connectionsNBT = compound.getList(CONNECTIONS_KEY, Tag.TAG_COMPOUND);
		template.getStoredConnections().clear();
		for(int i = 0; i < connectionsNBT.size(); i++)
			template.getStoredConnections().add(new TemplateConnection(connectionsNBT.getCompound(i)));
	}

	@Nullable
	private static ConnectionPoint getAbsolutePoint(
			ConnectionPoint relative, StructurePlaceSettings orientation, Level world, BlockPos base
	)
	{
		BlockPos absolutePos = StructureTemplate.calculateRelativePosition(orientation, relative.position()).offset(base);
		BlockEntity connector = world.getBlockEntity(absolutePos);
		if(!(connector instanceof IImmersiveConnectable))
			return null;
		ConnectionPoint point = new ConnectionPoint(absolutePos, relative.index());
		if(!((IImmersiveConnectable)connector).getConnectionPoints().contains(point))
			return null;
		return point;
	}
}