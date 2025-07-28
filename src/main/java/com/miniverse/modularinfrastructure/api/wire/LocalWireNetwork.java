package com.miniverse.modularinfrastructure.api.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Represents a connected component of the wire network
 * All connectors in a LocalWireNetwork are connected by wires
 * 
 * Inspired by Immersive Engineering's LocalWireNetwork
 * Original wire system concepts from Immersive Engineering by BluSunrize
 */
public class LocalWireNetwork {
    private final Map<ConnectionPoint, Collection<Connection>> connections = new HashMap<>();
    private final Map<BlockPos, IImmersiveConnectable> connectors = new HashMap<>();
    private boolean isValid = true;
    private int version = 0;
    
    // For deferred tasks
    private List<Runnable> runNextTick = new ArrayList<>();
    
    public LocalWireNetwork() {
    }
    
    public LocalWireNetwork(CompoundTag nbt, GlobalWireNetwork globalNet) {
        this();
        readFromNBT(nbt, globalNet);
    }
    
    /**
     * Get all connector positions in this network
     */
    public Collection<BlockPos> getConnectorPositions() {
        return Collections.unmodifiableCollection(connectors.keySet());
    }
    
    /**
     * Get a specific connector
     */
    public IImmersiveConnectable getConnector(BlockPos pos) {
        return connectors.get(pos);
    }
    
    public IImmersiveConnectable getConnector(ConnectionPoint cp) {
        return getConnector(cp.position());
    }
    
    /**
     * Get all connections at a specific connection point
     */
    public Collection<Connection> getConnections(ConnectionPoint at) {
        Collection<Connection> conns = connections.get(at);
        if (conns != null) {
            return Collections.unmodifiableCollection(conns);
        } else {
            return Collections.emptySet();
        }
    }
    
    /**
     * Get all connection points in this network
     */
    public Set<ConnectionPoint> getConnectionPoints() {
        return connections.keySet();
    }
    
    /**
     * Add a connector to this network
     */
    void addConnector(ConnectionPoint cp, IImmersiveConnectable connector, GlobalWireNetwork globalNet) {
        version++;
        
        // Ensure no existing connections at this point
        if (connections.containsKey(cp)) {
            throw new IllegalStateException("Connection point " + cp + " already exists in network");
        }
        
        connections.put(cp, new ArrayList<>());
        
        if (!connectors.containsKey(cp.position())) {
            connectors.put(cp.position(), connector);
        }
    }
    
    /**
     * Load a connector into this network (may already exist)
     */
    void loadConnector(ConnectionPoint cp, IImmersiveConnectable connector, GlobalWireNetwork globalNet) {
        version++;
        
        // If connection point doesn't exist, add it
        if (!connections.containsKey(cp)) {
            connections.put(cp, new ArrayList<>());
        }
        
        // Update connector reference
        connectors.put(cp.position(), connector);
    }
    
    /**
     * Add a connection to this network
     */
    void addConnection(Connection conn, GlobalWireNetwork globalNet) {
        version++;
        
        IImmersiveConnectable connA = connectors.get(conn.getEndA().position());
        IImmersiveConnectable connB = connectors.get(conn.getEndB().position());
        
        if (connA == null || connB == null) {
            throw new IllegalStateException("Cannot add connection without both connectors loaded");
        }
        
        // Check for duplicate connections
        if (connections.get(conn.getEndA()).stream()
                .anyMatch(c -> c.getOtherEnd(conn.getEndA()).equals(conn.getEndB()))) {
            return; // Already connected
        }
        
        connections.get(conn.getEndA()).add(conn);
        connections.get(conn.getEndB()).add(conn);
        
        connA.connectCable(conn, conn.getEndA());
        connB.connectCable(conn, conn.getEndB());
    }
    
    /**
     * Remove a connection from this network
     */
    void removeConnection(Connection conn) {
        version++;
        
        // Remove from both endpoints
        for (ConnectionPoint end : new ConnectionPoint[]{conn.getEndA(), conn.getEndB()}) {
            Collection<Connection> conns = connections.get(end);
            if (conns != null) {
                conns.remove(conn);
            }
            
            IImmersiveConnectable connector = connectors.get(end.position());
            if (connector != null) {
                connector.removeCable(conn, end);
            }
        }
    }
    
    /**
     * Remove a connector from this network
     */
    void removeConnector(BlockPos pos) {
        version++;
        
        IImmersiveConnectable connector = connectors.get(pos);
        if (connector == null) {
            return;
        }
        
        // Remove all connections from this connector
        for (ConnectionPoint cp : connector.getConnectionPoints()) {
            if (connections.containsKey(cp)) {
                // Remove connections to other connectors
                for (Connection c : new ArrayList<>(getConnections(cp))) {
                    ConnectionPoint other = c.getOtherEnd(cp);
                    Collection<Connection> otherConns = connections.get(other);
                    if (otherConns != null) {
                        otherConns.remove(c);
                    }
                }
                connections.remove(cp);
            }
        }
        
        connectors.remove(pos);
    }
    
    /**
     * Merge another network into this one
     */
    LocalWireNetwork merge(LocalWireNetwork other) {
        LocalWireNetwork result = new LocalWireNetwork();
        
        // Copy all data from both networks
        result.connectors.putAll(this.connectors);
        result.connectors.putAll(other.connectors);
        result.connections.putAll(this.connections);
        result.connections.putAll(other.connections);
        
        return result;
    }
    
    /**
     * Split this network into multiple networks if it's no longer fully connected
     */
    Collection<LocalWireNetwork> split(GlobalWireNetwork globalNet) {
        version++;
        
        Set<ConnectionPoint> toVisit = new HashSet<>(getConnectionPoints());
        Collection<LocalWireNetwork> result = new ArrayList<>();
        
        while (!toVisit.isEmpty()) {
            // Get a connected component
            ConnectionPoint start = toVisit.iterator().next();
            Collection<ConnectionPoint> component = getConnectedComponent(start, toVisit);
            
            if (toVisit.isEmpty() && result.isEmpty()) {
                // Still fully connected
                break;
            }
            
            // Create new network for this component
            LocalWireNetwork newNet = new LocalWireNetwork();
            for (ConnectionPoint cp : component) {
                newNet.addConnector(cp, connectors.get(cp.position()), globalNet);
            }
            
            // Add all connections within this component
            for (ConnectionPoint cp : component) {
                for (Connection c : getConnections(cp)) {
                    if (c.isPositiveEnd(cp) && component.contains(c.getOtherEnd(cp))) {
                        newNet.addConnection(c, globalNet);
                    }
                }
            }
            
            result.add(newNet);
        }
        
        return result;
    }
    
    /**
     * Get all connection points reachable from the start point
     */
    private Collection<ConnectionPoint> getConnectedComponent(ConnectionPoint start, Set<ConnectionPoint> unvisited) {
        Deque<ConnectionPoint> open = new ArrayDeque<>();
        List<ConnectionPoint> component = new ArrayList<>();
        
        open.push(start);
        unvisited.remove(start);
        
        while (!open.isEmpty()) {
            ConnectionPoint current = open.pop();
            component.add(current);
            
            for (Connection c : getConnections(current)) {
                ConnectionPoint other = c.getOtherEnd(current);
                if (unvisited.contains(other)) {
                    unvisited.remove(other);
                    open.push(other);
                }
            }
        }
        
        return component;
    }
    
    /**
     * Update this network (called each tick)
     */
    public void update(Level world) {
        // Run deferred tasks
        List<Runnable> toRun = runNextTick;
        runNextTick = new ArrayList<>();
        for (Runnable r : toRun) {
            r.run();
        }
    }
    
    /**
     * Add a task to run next tick
     */
    public void addAsFutureTask(Runnable task) {
        runNextTick.add(task);
    }
    
    /**
     * Write this network to NBT
     */
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        
        // Save connections
        ListTag wiresList = new ListTag();
        for (ConnectionPoint cp : connections.keySet()) {
            for (Connection conn : connections.get(cp)) {
                if (conn.isPositiveEnd(cp)) { // Only save each connection once
                    wiresList.add(conn.toNBT());
                }
            }
        }
        tag.put("wires", wiresList);
        
        // Save connector positions
        ListTag connectorsList = new ListTag();
        for (BlockPos pos : connectors.keySet()) {
            connectorsList.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("connectors", connectorsList);
        
        return tag;
    }
    
    /**
     * Read this network from NBT
     */
    private void readFromNBT(CompoundTag tag, GlobalWireNetwork globalNet) {
        // Load connections
        ListTag wiresList = tag.getList("wires", Tag.TAG_COMPOUND);
        for (Tag wireTag : wiresList) {
            Connection conn = new Connection((CompoundTag) wireTag);
            // Note: We'll need to validate these connections when connectors are loaded
        }
        
        // Connector positions are stored but actual connectors need to be loaded from the world
    }
    
    public void setInvalid() {
        version++;
        isValid = false;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public int getVersion() {
        return version;
    }
}