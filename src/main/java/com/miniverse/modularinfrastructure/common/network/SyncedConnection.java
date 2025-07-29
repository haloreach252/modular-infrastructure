// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.common.network;

import com.miniverse.modularinfrastructure.api.utils.codec.IEStreamCodecs;
import com.miniverse.modularinfrastructure.api.wires.Connection;
import com.miniverse.modularinfrastructure.api.wires.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wires.WireType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record SyncedConnection(
		ConnectionPoint start, ConnectionPoint end, WireType type, Vec3 offsetStart, Vec3 offsetEnd
)
{
	public static final StreamCodec<ByteBuf, SyncedConnection> CODEC = StreamCodec.composite(
			ConnectionPoint.CODECS.streamCodec(), SyncedConnection::start,
			ConnectionPoint.CODECS.streamCodec(), SyncedConnection::end,
			WireType.CODECS.streamCodec(), SyncedConnection::type,
			IEStreamCodecs.VEC3_STREAM_CODEC, SyncedConnection::offsetStart,
			IEStreamCodecs.VEC3_STREAM_CODEC, SyncedConnection::offsetEnd,
			SyncedConnection::new
	);

	public SyncedConnection(Connection connection)
	{
		this(
				connection.getEndA(), connection.getEndB(),
				connection.type,
				connection.getEndAOffset(), connection.getEndBOffset()
		);
	}

	public Connection toConnection()
	{
		return new Connection(type, start, end, offsetStart, offsetEnd);
	}
}