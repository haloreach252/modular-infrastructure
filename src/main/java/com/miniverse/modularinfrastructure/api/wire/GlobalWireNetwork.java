package com.miniverse.modularinfrastructure.api.wire;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.network.ClientSyncManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Global wire network that manages all local wire networks in a dimension
 * 
 * Based heavily on Immersive Engineering's GlobalWireNetwork
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
@EventBusSubscriber(modid = ModularInfrastructure.MODID)
public class GlobalWireNetwork extends SavedData {
    private static final String DATA_NAME = "modularinfrastructure_wirenetwork";
    
    // Cache for performance
    private static WeakReference<ServerLevel> lastServerWorld = new WeakReference<>(null);
    private static WeakReference<GlobalWireNetwork> lastServerNet = new WeakReference<>(null);
    
    // Client-side cache
    private static WeakReference<Level> lastClientWorld = new WeakReference<>(null);
    private static GlobalWireNetwork lastClientNet = null;
    
    private final Map<ConnectionPoint, LocalWireNetwork> localNetsByPos = new HashMap<>();
    private final Set<LocalWireNetwork> localNetSet = new HashSet<>();
    
    // Queue for connectors that need to be loaded
    private final Map<BlockPos, IImmersiveConnectable> queuedLoads = new LinkedHashMap<>();
    
    private boolean validateNextTick = false;
    
    // Wire collision data for interaction and rendering
    private final WireCollisionData collisionData;
    
    // Sync manager for client-server communication
    private final IWireSyncManager syncManager;
    
    // Proxy provider for unloaded chunks
    private final IICProxyProvider proxyProvider;
    
    public GlobalWireNetwork(boolean isClientSide, IICProxyProvider proxyProvider, IWireSyncManager syncManager) {
        this.collisionData = new WireCollisionData(this, isClientSide);
        this.proxyProvider = proxyProvider;
        this.syncManager = syncManager;
    }
    
    /**
     * Get the wire network for a given level
     */
    public static GlobalWireNetwork getNetwork(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            // Check cache first
            if (serverLevel == lastServerWorld.get()) {
                GlobalWireNetwork cached = lastServerNet.get();
                if (cached != null) {
                    return cached;
                }
            }
            
            // Load or create network
            GlobalWireNetwork network = serverLevel.getDataStorage()
                .computeIfAbsent(
                    new Factory<>(
                        () -> new GlobalWireNetwork(false, new DefaultProxyProvider(), new com.miniverse.modularinfrastructure.network.WireSyncManager()),
                        (tag, provider) -> load(tag, provider),
                        null
                    ),
                    DATA_NAME
                );
            
            // Update cache
            lastServerWorld = new WeakReference<>(serverLevel);
            lastServerNet = new WeakReference<>(network);
            
            return network;
        } else {
            // Client-side network - cache it per world
            if (level != lastClientWorld.get() || lastClientNet == null) {
                ModularInfrastructure.LOGGER.info("Creating new client-side GlobalWireNetwork instance for level {}", level);
                lastClientWorld = new WeakReference<>(level);
                lastClientNet = new GlobalWireNetwork(true, new DefaultProxyProvider(), new ClientSyncManager());
            }
            return lastClientNet;
        }
    }
    
    /**
     * Clear cached networks when world unloads
     */
    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() == lastServerWorld.get()) {
            lastServerWorld = new WeakReference<>(null);
            lastServerNet = new WeakReference<>(null);
        } else if (event.getLevel() == lastClientWorld.get()) {
            lastClientWorld = new WeakReference<>(null);
            lastClientNet = null;
        }
    }
    
    /**
     * Add a connection to the network
     */
    public void addConnection(Connection conn) {
        processQueuedLoads();
        
        ConnectionPoint posA = conn.getEndA();
        ConnectionPoint posB = conn.getEndB();
        
        ModularInfrastructure.LOGGER.info("GlobalWireNetwork: Adding connection from {} to {}", posA, posB);
        LocalWireNetwork netA = getLocalNet(posA);
        LocalWireNetwork netB = getLocalNet(posB);
        
        LocalWireNetwork joined;
        if (netA != netB) {
            // Merge networks
            joined = netA.merge(netB);
            for (ConnectionPoint cp : joined.getConnectionPoints()) {
                putLocalNet(cp, joined);
            }
        } else {
            joined = netA;
        }
        
        joined.addConnection(conn, this);
        collisionData.addConnection(conn);
        syncManager.onConnectionAdded(conn);
        validateNextTick = true;
        setDirty();
    }
    
    /**
     * Remove a connection from the network
     */
    public void removeConnection(Connection conn) {
        processQueuedLoads();
        
        LocalWireNetwork oldNet = getNullableLocalNet(conn.getEndA());
        if (oldNet == null) {
            return;
        }
        
        oldNet.removeConnection(conn);
        splitNet(oldNet);
        collisionData.removeConnection(conn);
        syncManager.onConnectionRemoved(conn);
        setDirty();
    }
    
    /**
     * Remove all connections at a specific connector
     */
    public void removeAllConnectionsAt(IImmersiveConnectable connector, ConnectionConsumer handler) {
        processQueuedLoads();
        
        for (ConnectionPoint cp : connector.getConnectionPoints()) {
            removeAllConnectionsAt(cp, handler);
        }
    }
    
    /**
     * Remove all connections at a specific connection point
     */
    public void removeAllConnectionsAt(ConnectionPoint pos, ConnectionConsumer handler) {
        processQueuedLoads();
        
        LocalWireNetwork net = getLocalNet(pos);
        List<Connection> conns = new ArrayList<>(net.getConnections(pos));
        
        for (Connection conn : conns) {
            handler.accept(conn);
            removeConnection(conn);
        }
        
        validateNextTick = true;
    }
    
    /**
     * Remove a connector from the network
     */
    public void removeConnector(IImmersiveConnectable connector) {
        processQueuedLoads();
        
        Set<LocalWireNetwork> netsToRemoveFrom = new HashSet<>();
        BlockPos pos = connector.getPosition();
        
        for (ConnectionPoint cp : connector.getConnectionPoints()) {
            LocalWireNetwork local = getNullableLocalNet(cp);
            if (local != null) {
                putLocalNet(cp, null);
                netsToRemoveFrom.add(local);
            }
        }
        
        for (LocalWireNetwork net : netsToRemoveFrom) {
            net.removeConnector(pos);
            if (net.getConnectionPoints().isEmpty()) {
                localNetSet.remove(net);
            } else {
                splitNet(net);
            }
        }
        
        validateNextTick = true;
    }
    
    /**
     * Called when a connector is loaded
     */
    public void onConnectorLoad(IImmersiveConnectable connector, Level level) {
        ModularInfrastructure.LOGGER.info("Queueing connector load at position: {}", connector.getPosition());
        queuedLoads.put(connector.getPosition(), connector);
    }
    
    /**
     * Called when a connector is unloaded
     */
    public void onConnectorUnload(IImmersiveConnectable connector) {
        // For now, we'll keep the connections in memory
        // In a full implementation, we'd create proxy connectors
    }
    
    /**
     * Get the local network at a connection point
     */
    public LocalWireNetwork getLocalNet(ConnectionPoint pos) {
        processQueuedLoads();
        
        LocalWireNetwork existing = localNetsByPos.get(pos);
        if (existing != null) {
            return existing;
        }
        
        // Create new network with proxy connector
        LocalWireNetwork newNet = new LocalWireNetwork();
        IImmersiveConnectable proxy = new ProxyConnector(pos);
        ModularInfrastructure.LOGGER.info("Creating new local network with proxy connector at {}", pos);
        newNet.addConnector(pos, proxy, this);
        localNetSet.add(newNet);
        localNetsByPos.put(pos, newNet);
        return newNet;
    }
    
    /**
     * Get the local network at a connection point, or null if none exists
     */
    public LocalWireNetwork getNullableLocalNet(ConnectionPoint pos) {
        processQueuedLoads();
        return localNetsByPos.get(pos);
    }
    
    /**
     * Update the network (called each tick)
     */
    public void update(Level level) {
        if (validateNextTick) {
            validate(level);
            validateNextTick = false;
        }
        
        processQueuedLoads();
        
        if (!level.isClientSide()) {
            // Update all local networks
            for (LocalWireNetwork net : localNetSet.toArray(new LocalWireNetwork[0])) {
                net.update(level);
            }
        }
    }
    
    /**
     * Process queued connector loads
     */
    private void processQueuedLoads() {
        if (queuedLoads.isEmpty()) {
            return;
        }
        
        ModularInfrastructure.LOGGER.info("Processing {} queued connector loads", queuedLoads.size());
        Map<BlockPos, IImmersiveConnectable> toProcess = new LinkedHashMap<>(queuedLoads);
        queuedLoads.clear();
        
        for (Map.Entry<BlockPos, IImmersiveConnectable> entry : toProcess.entrySet()) {
            IImmersiveConnectable connector = entry.getValue();
            ModularInfrastructure.LOGGER.info("Processing connector at {}", connector.getPosition());
            
            for (ConnectionPoint cp : connector.getConnectionPoints()) {
                LocalWireNetwork existingNet = getNullableLocalNet(cp);
                if (existingNet != null) {
                    // Connector already loaded, update the connector reference
                    IImmersiveConnectable existing = existingNet.getConnector(cp.position());
                    if (existing != connector && !existing.isProxy()) {
                        // Skip - connector already loaded with a different instance
                        continue;
                    }
                }
                
                LocalWireNetwork local = getLocalNet(cp);
                local.loadConnector(cp, connector, this);
            }
            
            // Add internal connections for new connectors
            for (Connection c : connector.getInternalConnections()) {
                if (!connectionExists(c)) {
                    addConnection(c);
                }
            }
            
            validateNextTick = true;
        }
    }
    
    private boolean connectionExists(Connection conn) {
        LocalWireNetwork net = getNullableLocalNet(conn.getEndA());
        if (net == null) {
            return false;
        }
        return net.getConnections(conn.getEndA()).contains(conn);
    }
    
    /**
     * Split a network if it's no longer fully connected
     */
    private void splitNet(LocalWireNetwork oldNet) {
        Collection<LocalWireNetwork> newNets = oldNet.split(this);
        for (LocalWireNetwork net : newNets) {
            for (ConnectionPoint cp : net.getConnectionPoints()) {
                putLocalNet(cp, net);
            }
        }
    }
    
    /**
     * Update the local net mapping for a connection point
     */
    private void putLocalNet(ConnectionPoint cp, LocalWireNetwork net) {
        LocalWireNetwork oldNet = localNetsByPos.get(cp);
        if (oldNet != null && net != null && oldNet.isValid()) {
            oldNet.setInvalid();
            localNetSet.remove(oldNet);
        }
        
        if (net != null) {
            localNetsByPos.put(cp, net);
            localNetSet.add(net);
        } else {
            localNetsByPos.remove(cp);
        }
    }
    
    /**
     * Validate the network structure
     */
    private void validate(Level level) {
        if (level.isClientSide()) {
            return;
        }
        
        // Basic validation - ensure all connection points are properly mapped
        for (LocalWireNetwork net : localNetSet) {
            for (ConnectionPoint cp : net.getConnectionPoints()) {
                if (localNetsByPos.get(cp) != net) {
                    System.err.println("Connection point " + cp + " not properly mapped to its network");
                }
            }
        }
    }
    
    /**
     * Get all local networks (for rendering purposes)
     */
    public Set<LocalWireNetwork> getAllLocalNetworks() {
        return new HashSet<>(localNetSet);
    }
    
    /**
     * Get collision data for wire interactions
     */
    public WireCollisionData getCollisionData() {
        return collisionData;
    }
    
    /**
     * Get all connectors in a chunk position
     */
    public Collection<ConnectionPoint> getAllConnectorsIn(ChunkPos pos) {
        List<ConnectionPoint> result = new ArrayList<>();
        BlockPos min = pos.getWorldPosition();
        BlockPos max = min.offset(15, Integer.MAX_VALUE, 15);
        
        for (ConnectionPoint cp : localNetsByPos.keySet()) {
            BlockPos cpPos = cp.position();
            if (cpPos.getX() >= min.getX() && cpPos.getX() <= max.getX() &&
                cpPos.getZ() >= min.getZ() && cpPos.getZ() <= max.getZ()) {
                result.add(cp);
            }
        }
        
        return result;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag localsList = new ListTag();
        for (LocalWireNetwork local : localNetSet) {
            localsList.add(local.writeToNBT());
        }
        tag.put("locals", localsList);
        return tag;
    }
    
    public static GlobalWireNetwork load(CompoundTag tag, HolderLookup.Provider registries) {
        GlobalWireNetwork network = new GlobalWireNetwork(false, new DefaultProxyProvider(), new com.miniverse.modularinfrastructure.network.WireSyncManager());
        
        ListTag localsList = tag.getList("locals", Tag.TAG_COMPOUND);
        for (Tag localTag : localsList) {
            LocalWireNetwork local = new LocalWireNetwork((CompoundTag) localTag, network);
            for (ConnectionPoint cp : local.getConnectionPoints()) {
                network.putLocalNet(cp, local);
            }
        }
        
        return network;
    }
    
    /**
     * Functional interface for connection consumers
     */
    @FunctionalInterface
    public interface ConnectionConsumer {
        void accept(Connection connection);
    }
    
    /**
     * Simple proxy connector for unloaded chunks
     */
    private static class ProxyConnector implements IImmersiveConnectable {
        private final ConnectionPoint point;
        
        public ProxyConnector(ConnectionPoint point) {
            this.point = point;
        }
        
        @Override
        public BlockPos getPosition() {
            return point.position();
        }
        
        @Override
        public Collection<ConnectionPoint> getConnectionPoints() {
            return Collections.singletonList(point);
        }
        
        @Override
        public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) {
            return Vec3.atCenterOf(point.position());
        }
        
        @Override
        public boolean canConnectCable(WireType type, ConnectionPoint target) {
            return true;
        }
        
        @Override
        public void connectCable(Connection connection, ConnectionPoint point) {
        }
        
        @Override
        public void removeCable(Connection connection, ConnectionPoint point) {
        }
        
        @Override
        public Collection<Connection> getInternalConnections() {
            return Collections.emptyList();
        }
        
        @Override
        public boolean isProxy() {
            return true;
        }
    }
}