package com.miniverse.modularinfrastructure.network;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.wire.Connection;
import com.miniverse.modularinfrastructure.api.wire.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wire.GlobalWireNetwork;
import com.miniverse.modularinfrastructure.api.wire.WireType;
import com.miniverse.modularinfrastructure.api.wire.ModWireTypes;
import com.miniverse.modularinfrastructure.api.wire.WireUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.core.SectionPos;
import java.util.Set;
import java.util.HashSet;

/**
 * Network packet for synchronizing wire connections between server and client
 * 
 * Based on Immersive Engineering's MessageWireSync
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public record MessageWireSync(Connection connection, Operation operation) implements CustomPacketPayload {
    public static final Type<MessageWireSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("modularinfrastructure", "wire_sync"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageWireSync> CODEC = StreamCodec.of(
        MessageWireSync::write,
        MessageWireSync::read
    );

    public enum Operation {
        ADD, REMOVE, UPDATE
    }

    public static void write(RegistryFriendlyByteBuf buf, MessageWireSync msg) {
        // Write operation
        buf.writeEnum(msg.operation);
        
        // Write connection
        Connection conn = msg.connection;
        
        // Write endpoints
        buf.writeBlockPos(conn.getEndA().position());
        buf.writeInt(conn.getEndA().index());
        buf.writeBlockPos(conn.getEndB().position());
        buf.writeInt(conn.getEndB().index());
        
        // Write wire type ID
        buf.writeUtf(conn.getWireType().getId());
        
        // Write internal flag
        buf.writeBoolean(conn.isInternal());
    }

    public static MessageWireSync read(RegistryFriendlyByteBuf buf) {
        Operation op = buf.readEnum(Operation.class);
        
        // Read endpoints
        BlockPos posA = buf.readBlockPos();
        int indexA = buf.readInt();
        BlockPos posB = buf.readBlockPos();
        int indexB = buf.readInt();
        
        // Read wire type
        String typeId = buf.readUtf();
        WireType type = ModWireTypes.getWireType(typeId);
        
        // Read internal flag
        boolean internal = buf.readBoolean();
        
        ConnectionPoint cpA = new ConnectionPoint(posA, indexA);
        ConnectionPoint cpB = new ConnectionPoint(posB, indexB);
        Connection conn = new Connection(type, cpA, cpB);
        if (internal) {
            conn.setInternal(true);
        }
        
        return new MessageWireSync(conn, op);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                GlobalWireNetwork network = GlobalWireNetwork.getNetwork(level);
                ModularInfrastructure.LOGGER.info("Client received wire sync: {} for connection {}", operation, connection);
                
                switch (operation) {
                    case ADD -> network.addConnection(connection);
                    case REMOVE -> network.removeConnection(connection);
                    case UPDATE -> {
                        network.removeConnection(connection);
                        network.addConnection(connection);
                    }
                }
                
                // Force affected sections to re-render
                Set<SectionPos> sectionsToRerender = new HashSet<>();
                WireUtils.forEachRenderPoint(connection, ($, $2, section) -> sectionsToRerender.add(section));
                for (SectionPos section : sectionsToRerender) {
                    Minecraft.getInstance().levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}