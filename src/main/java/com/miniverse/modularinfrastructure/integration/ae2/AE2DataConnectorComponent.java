package com.miniverse.modularinfrastructure.integration.ae2;

import com.miniverse.modularinfrastructure.api.wires.Connection;
import com.miniverse.modularinfrastructure.api.wires.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.LocalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.WireType;
import com.miniverse.modularinfrastructure.blockentity.DataConnectorBlockEntity;
import com.miniverse.modularinfrastructure.common.wires.ModWireTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import appeng.api.networking.*;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.implementations.IPowerChannelState;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * AE2 integration component for data connectors
 * Handles all AE2-specific functionality
 */
public class AE2DataConnectorComponent implements IAE2DataConnector, IInWorldGridNodeHost, IPowerChannelState {
    
    private final DataConnectorBlockEntity host;
    private final IManagedGridNode mainNode;
    private AECableType cableType = AECableType.NONE;
    private boolean isPowered = false;
    private boolean isOnline = false;
    
    public AE2DataConnectorComponent(DataConnectorBlockEntity host) {
        this.host = host;
        
        // Initialize the managed grid node
        // Set flags based on tier - Advanced tier gets DENSE_CAPACITY for 32 channel base capacity
        GridFlags[] flags = host.getDataTier() == com.miniverse.modularinfrastructure.block.DataConnectorBlock.DataTier.ADVANCED 
            ? new GridFlags[] { GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY }
            : new GridFlags[] { GridFlags.REQUIRE_CHANNEL };
            
        mainNode = GridHelper.createManagedNode(this, new ConnectorNodeListener())
            .setFlags(flags)
            .setIdlePowerUsage(0.5) // Small power draw
            .setInWorldNode(true)
            .setExposedOnSides(EnumSet.allOf(Direction.class)); // Expose on all sides initially
    }
    
    @Override
    public void onLoad() {
        GridHelper.onFirstTick(host, be -> {
            if (!mainNode.isReady()) {
                mainNode.create(host.getLevel(), host.getBlockPos());
            }
            updateCableType();
        });
    }
    
    @Override
    public void onRemoved() {
        mainNode.destroy();
    }
    
    @Override
    public void onClearRemoved() {
        GridHelper.onFirstTick(host, be -> scheduleInit());
    }
    
    private void scheduleInit() {
        if (!mainNode.isReady()) {
            mainNode.create(host.getLevel(), host.getBlockPos());
        }
        updateCableType();
    }
    
    /**
     * Determine the effective cable type based on connected wires and tier
     */
    private void updateCableType() {
        // Check if we have any data cable connections
        boolean hasDataConnection = false;
        
        if (host.getLevel() != null && !host.getLevel().isClientSide) {
            GlobalWireNetwork wireNetwork = GlobalWireNetwork.getNetwork(host.getLevel());
            ConnectionPoint ourPoint = new ConnectionPoint(host.getBlockPos(), 0);
            
            LocalWireNetwork localNet = wireNetwork.getLocalNet(ourPoint);
            if (localNet != null) {
                for (Connection conn : localNet.getConnections(ourPoint)) {
                    WireType wireType = conn.type;
                    if (wireType == ModWireTypes.DATA_CABLE || wireType == ModWireTypes.DENSE_CABLE) {
                        hasDataConnection = true;
                        break;
                    }
                }
            }
        }
        
        // Set cable type based on tier and connections
        if (hasDataConnection) {
            // Advanced tier (32 channels) uses dense cable type
            if (host.getDataTier() == com.miniverse.modularinfrastructure.block.DataConnectorBlock.DataTier.ADVANCED) {
                cableType = AECableType.DENSE_COVERED;
            } else {
                // Basic tier (8 channels) uses normal cable type
                cableType = AECableType.COVERED;
            }
        } else {
            cableType = AECableType.NONE;
        }
    }
    
    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        mainNode.saveToNBT(tag);
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        mainNode.loadFromNBT(tag);
    }
    
    // IInWorldGridNodeHost implementation
    
    @Override
    @Nullable
    public IGridNode getGridNode(Direction dir) {
        return mainNode.getNode();
    }
    
    @Override
    @Nullable
    public AECableType getCableConnectionType(Direction dir) {
        return cableType;
    }
    
    // IPowerChannelState implementation
    
    @Override
    public boolean isPowered() {
        return isPowered;
    }
    
    @Override
    public boolean isActive() {
        return isOnline && isPowered;
    }
    
    // Node listener to handle grid events
    private class ConnectorNodeListener implements IGridNodeListener<AE2DataConnectorComponent> {
        
        @Override
        public void onStateChanged(AE2DataConnectorComponent nodeOwner, IGridNode node, State state) {
            boolean wasOnline = isOnline;
            boolean wasPowered = isPowered;
            
            isOnline = node.isOnline();
            isPowered = node.isPowered();
            
            if (wasOnline != isOnline || wasPowered != isPowered) {
                host.setChanged();
                if (host.getLevel() != null) {
                    host.getLevel().sendBlockUpdated(host.getBlockPos(), host.getBlockState(), host.getBlockState(), 3);
                }
            }
        }
        
        @Override
        public void onSaveChanges(AE2DataConnectorComponent nodeOwner, IGridNode node) {
            host.setChanged();
        }
        
        @Override
        public void onGridChanged(AE2DataConnectorComponent nodeOwner, IGridNode node) {
            // Update cable type when grid changes
            updateCableType();
        }
    }
    
    /**
     * Get the IGridNode if available
     */
    @Nullable
    public IGridNode getGridNode() {
        return mainNode.getNode();
    }
    
    /**
     * Check if this component is ready (node created)
     */
    public boolean isReady() {
        return mainNode.isReady();
    }
    
    @Override
    public int getEffectiveChannelCapacity(int baseChannels) {
        // If we don't have a grid node yet, return base channels
        IGridNode node = mainNode.getNode();
        if (node == null || node.getGrid() == null) {
            return baseChannels;
        }
        
        // Get the channel mode from the grid's pathing service
        var pathingService = node.getGrid().getPathingService();
        var channelMode = pathingService.getChannelMode();
        
        // Handle infinite channel mode
        if (channelMode == appeng.api.networking.pathing.ChannelMode.INFINITE) {
            return Integer.MAX_VALUE;
        }
        
        // Apply the channel capacity factor
        return baseChannels * channelMode.getCableCapacityFactor();
    }
}