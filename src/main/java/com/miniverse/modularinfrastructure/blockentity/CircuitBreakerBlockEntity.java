package com.miniverse.modularinfrastructure.blockentity;

import com.google.common.collect.ImmutableList;
import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.api.IEApi;
import com.miniverse.modularinfrastructure.api.wires.Connection;
import com.miniverse.modularinfrastructure.api.wires.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.LocalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.WireApi;
import com.miniverse.modularinfrastructure.api.wires.WireType;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.EnergyTransferHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.LocalNetworkHandler;
import com.miniverse.modularinfrastructure.api.wires.redstone.IRedstoneConnector;
import com.miniverse.modularinfrastructure.api.wires.redstone.RedstoneNetworkHandler;
import com.miniverse.modularinfrastructure.block.CircuitBreakerBlock;
import com.miniverse.modularinfrastructure.common.wires.WireConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Block entity for circuit breakers that can interrupt wire connections
 * When disabled, it prevents power/data flow through connected wires
 */
public class CircuitBreakerBlockEntity extends ConnectorBlockEntity implements 
        EnergyTransferHandler.EnergyConnector,
        IRedstoneConnector {
    // State tracking
    private boolean enabled = true;
    
    // Energy pass-through tracking
    private int energyThroughput = 0;
    private static final int MAX_THROUGHPUT = 100000; // High limit for all tiers
    
    // Redstone pass-through
    private byte[] redstoneValues = new byte[16];
    
    
    public CircuitBreakerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CIRCUIT_BREAKER.get(), pos, state);
        // Circuit breakers can have many connections
        this.maxConnections = 16;
        // Initialize enabled state from block state if available
        if (state.hasProperty(CircuitBreakerBlock.ENABLED)) {
            this.enabled = state.getValue(CircuitBreakerBlock.ENABLED);
        }
    }
    
    /**
     * Called when the breaker state changes
     */
    public void onStateChanged(boolean newEnabled) {
        this.enabled = newEnabled;
        setChanged();
        
        // Reset energy throughput when disabled
        if (!enabled) {
            energyThroughput = 0;
            Arrays.fill(redstoneValues, (byte) 0);
        }
        
        // Update neighbors to trigger network updates
        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            
            // Force the wire network to reset its cached paths
            GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(level);
            if (globalNet != null) {
                for (ConnectionPoint cp : getConnectionPoints()) {
                    LocalWireNetwork localNet = globalNet.getLocalNet(cp);
                    if (localNet != null) {
                        // Reset the energy transfer handler to clear cached paths
                        EnergyTransferHandler energyHandler = localNet.getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
                        if (energyHandler != null) {
                            energyHandler.reset();
                        }
                        
                        // Reset the redstone handler as well
                        RedstoneNetworkHandler redstoneHandler = localNet.getHandler(IEApi.ieLoc("redstone"), RedstoneNetworkHandler.class);
                        if (redstoneHandler != null) {
                            redstoneHandler.updateValues();
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public Collection<ConnectionPoint> getConnectionPoints() {
        // Circuit breaker uses 2 connection points - input and output
        // This allows it to properly break the connection when disabled
        return Arrays.asList(
            new ConnectionPoint(worldPosition, 0), // Input
            new ConnectionPoint(worldPosition, 1)  // Output
        );
    }
    
    @Override
    public Collection<Connection> getInternalConnections() {
        // Circuit breakers use internal connections to bridge wires when enabled
        if (!enabled) {
            return Collections.emptyList();
        }
        // When enabled, create an internal connection between the two connection points
        return Collections.singletonList(new Connection(worldPosition, 0, 1));
    }
    
    @Override
    public boolean allowsConnectionThrough(Connection conn, ConnectionPoint localEnd) {
        // Only allow connections through when the circuit breaker is enabled
        return enabled;
    }
    
    // Energy Connector implementation
    @Override
    public boolean isSource(ConnectionPoint cp) {
        // When enabled, act as source if we have energy passing through
        return enabled && energyThroughput > 0;
    }
    
    @Override
    public boolean isSink(ConnectionPoint cp) {
        // When enabled, always act as sink to receive energy
        return enabled;
    }
    
    @Override
    public int getAvailableEnergy() {
        // Pass through the energy we received
        return enabled ? energyThroughput : 0;
    }
    
    @Override
    public int getRequestedEnergy() {
        // Request max throughput when enabled
        return enabled ? MAX_THROUGHPUT : 0;
    }
    
    @Override
    public void insertEnergy(int amount) {
        if (enabled) {
            // Store energy for pass-through
            energyThroughput = Math.min(amount, MAX_THROUGHPUT);
        }
    }
    
    @Override
    public void extractEnergy(int amount) {
        if (enabled) {
            // Energy extracted, reduce throughput
            energyThroughput = Math.max(0, energyThroughput - amount);
        }
    }
    
    @Override
    public void onEnergyPassedThrough(double amount) {
        // Track energy passing through
        if (enabled) {
            energyThroughput = (int) Math.min(amount, MAX_THROUGHPUT);
        }
    }
    
    // Redstone Connector implementation
    @Override
    public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler) {
        if (enabled && handler != null) {
            // Get the highest signal from other connectors
            byte[] values = handler.getValuesExcluding(cp);
            System.arraycopy(values, 0, redstoneValues, 0, 16);
        } else {
            // Clear signals when disabled
            Arrays.fill(redstoneValues, (byte) 0);
        }
        
        // Update block when redstone changes
        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }
    
    @Override
    public void updateInput(byte[] signals, ConnectionPoint cp) {
        // Circuit breakers don't emit their own signals
        // They only pass through signals when enabled via the internal connection
        // The redstone network will handle signal propagation through the internal connection
    }
    
    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        // Request energy and redstone handlers
        return ImmutableList.of(
            EnergyTransferHandler.ID,
            IEApi.ieLoc("redstone")
        );
    }
    
    @Override
    public boolean canConnectCable(WireType type, ConnectionPoint target, net.minecraft.core.Vec3i offset) {
        // Can always connect cables to circuit breaker
        // Allow multiple connections per point
        return connections.size() < getMaxConnectionsInternal();
    }
    
    
    @Override
    public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) {
        // Circuit breaker has two connection points on opposite sides
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof CircuitBreakerBlock)) {
            return new Vec3(0.5, 0.5, 0.5);
        }
        
        net.minecraft.core.Direction facing = state.getValue(CircuitBreakerBlock.FACING);
        
        // Point 0 (input) is on the facing side, point 1 (output) is on the opposite side
        net.minecraft.core.Direction side = here.index() == 0 ? facing : facing.getOpposite();
        
        // Connection points are at the edge of the block
        double lengthFromHalf = 0.5 - type.getRenderDiameter() / 2;
        
        return new Vec3(
            0.5 + lengthFromHalf * side.getStepX(),
            0.5 + lengthFromHalf * side.getStepY(),
            0.5 + lengthFromHalf * side.getStepZ()
        );
    }
    
    @Override
    protected double getConnectorLength() {
        // Circuit breakers extend further than standard connectors
        return WireConfig.ConnectorOffsets.CIRCUIT_BREAKER_LENGTH; // 12 pixels from edge
    }
    
    /**
     * Server tick for handling state updates
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, CircuitBreakerBlockEntity be) {
        // Check if the enabled state matches the block state
        boolean blockEnabled = state.getValue(CircuitBreakerBlock.ENABLED);
        if (blockEnabled != be.enabled) {
            be.onStateChanged(blockEnabled);
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("enabled", enabled);
        tag.putInt("energyThroughput", energyThroughput);
        tag.putByteArray("redstoneValues", redstoneValues);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        enabled = tag.getBoolean("enabled");
        energyThroughput = tag.getInt("energyThroughput");
        if (tag.contains("redstoneValues")) {
            redstoneValues = tag.getByteArray("redstoneValues");
            if (redstoneValues.length != 16) {
                redstoneValues = new byte[16];
            }
        }
    }
}