package com.miniverse.modularinfrastructure.network;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.wire.*;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages synchronization of wire networks between server and clients
 * 
 * Based heavily on Immersive Engineering's WireSyncManager
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
@EventBusSubscriber(modid = ModularInfrastructure.MODID)
public class WireSyncManager implements IWireSyncManager {
    private static final SetMultimap<UUID, ChunkPos> wireWatchedChunksByPlayer = HashMultimap.create();

    private static void sendMessagesForChunk(Level w, ChunkPos pos, ServerPlayer player, boolean add) {
        GlobalWireNetwork net = GlobalWireNetwork.getNetwork(w);
        Collection<ConnectionPoint> connsInChunk = net.getAllConnectorsIn(pos);
        final MessageWireSync.Operation operation = add ? MessageWireSync.Operation.ADD : MessageWireSync.Operation.REMOVE;
        
        for (ConnectionPoint cp : connsInChunk) {
            LocalWireNetwork localNet = net.getNullableLocalNet(cp);
            if (localNet != null) {
                for (Connection conn : localNet.getConnections(cp)) {
                    if (shouldSendConnection(conn, pos, player, add, cp)) {
                        ModularInfrastructure.LOGGER.debug("Sending connection {} ({}) for chunk change at {}", conn, add, pos);
                        PacketDistributor.sendToPlayer(player, new MessageWireSync(conn, operation));
                    }
                }
            }
        }
    }

    private static boolean shouldSendConnection(Connection conn, ChunkPos pos, ServerPlayer player, boolean add,
                                                ConnectionPoint currEnd) {
        if (conn.isInternal()) {
            return false;
        }
        ConnectionPoint other = conn.getOtherEnd(currEnd);
        ChunkPos otherChunk = new ChunkPos(other.position());
        if (otherChunk.equals(pos)) {
            return conn.isPositiveEnd(currEnd);
        } else if (add) {
            return wireWatchedChunksByPlayer.get(player.getUUID()).contains(otherChunk);
        } else {
            return !wireWatchedChunksByPlayer.get(player.getUUID()).contains(otherChunk);
        }
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch ev) {
        wireWatchedChunksByPlayer.put(ev.getPlayer().getUUID(), ev.getPos());
        sendMessagesForChunk(ev.getLevel(), ev.getPos(), ev.getPlayer(), true);
    }

    @SubscribeEvent
    public static void onChunkUnWatch(ChunkWatchEvent.UnWatch ev) {
        wireWatchedChunksByPlayer.remove(ev.getPlayer().getUUID(), ev.getPos());
        sendMessagesForChunk(ev.getLevel(), ev.getPos(), ev.getPlayer(), false);
    }

    @Override
    public void onConnectionAdded(Connection c) {
        if (c.isInternal()) {
            return;
        }
        sendToPlayersWatchingConnection(c, MessageWireSync.Operation.ADD);
    }

    @Override
    public void onConnectionRemoved(Connection c) {
        if (c.isInternal()) {
            return;
        }
        sendToPlayersWatchingConnection(c, MessageWireSync.Operation.REMOVE);
    }

    @Override
    public void onConnectionEndpointsChanged(Connection c) {
        if (c.isInternal()) {
            return;
        }
        sendToPlayersWatchingConnection(c, MessageWireSync.Operation.UPDATE);
    }

    private void sendToPlayersWatchingConnection(Connection c, MessageWireSync.Operation op) {
        ChunkPos chunkA = new ChunkPos(c.getEndA().position());
        ChunkPos chunkB = new ChunkPos(c.getEndB().position());
        
        ModularInfrastructure.LOGGER.info("Sending wire sync {} for chunks {} and {}", op, chunkA, chunkB);
        
        Set<ServerPlayer> playersToSend = new HashSet<>();
        for (var entry : wireWatchedChunksByPlayer.asMap().entrySet()) {
            if (entry.getValue().contains(chunkA) || entry.getValue().contains(chunkB)) {
                // For now, we'll send to all players since findPlayer returns null
                // This needs a proper fix to get player references
                ModularInfrastructure.LOGGER.info("Player {} is watching relevant chunks", entry.getKey());
            }
        }

        // As a temporary fix, send to all players
        MessageWireSync message = new MessageWireSync(c, op);
        PacketDistributor.sendToAllPlayers(message);
        ModularInfrastructure.LOGGER.info("Sent wire sync to all players");
    }

    private ServerPlayer findPlayer(UUID uuid) {
        // Since this is called from a context where we're processing chunk watch events,
        // we don't have direct access to the server. The calling code should handle
        // null returns gracefully.
        // In practice, players are found through the chunk watch events themselves.
        return null;
    }
}