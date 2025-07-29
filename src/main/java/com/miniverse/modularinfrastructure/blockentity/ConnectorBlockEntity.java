package com.miniverse.modularinfrastructure.blockentity;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.api.TargetingInfo;
import com.miniverse.modularinfrastructure.api.wires.*;
import com.miniverse.modularinfrastructure.api.wires.impl.ImmersiveConnectableBlockEntity;
import com.miniverse.modularinfrastructure.block.ConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base block entity for all connectors
 * Handles wire connections and network participation
 * 
 * Based on Immersive Engineering's connector system
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public abstract class ConnectorBlockEntity extends ImmersiveConnectableBlockEntity {
    // List of connections from this connector
    protected List<WireConnection> connections = new ArrayList<>();
    
    // Maximum number of connections this connector can have
    protected int maxConnections = 8;
    
    protected ConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    /**
     * Add a wire connection to this connector
     */
    public boolean addConnection(BlockPos otherPos, WireType wireType) {
        if (connections.size() >= maxConnections) {
            return false;
        }
        
        // Check if wire type is compatible
        ConnectorBlock block = (ConnectorBlock) getBlockState().getBlock();
        if (!block.canAcceptWire(wireType)) {
            return false;
        }
        
        // Check if already connected to this position
        for (WireConnection conn : connections) {
            if (conn.targetPos.equals(otherPos)) {
                return false;
            }
        }
        
        connections.add(new WireConnection(otherPos, wireType));
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        return true;
    }
    
    // IImmersiveConnectable implementation
    
    @Override
    public Collection<ConnectionPoint> getConnectionPoints() {
        // Most connectors have a single connection point at index 0
        return Collections.singletonList(new ConnectionPoint(worldPosition, 0));
    }
    
    @Override
    public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) {
        // Match IE's implementation
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof ConnectorBlock)) {
            ModularInfrastructure.LOGGER.error("ConnectorBlockEntity at {} has invalid block state: {}", worldPosition, state);
            return new Vec3(0.5, 0.5, 0.5);
        }
        
        // Get the facing direction (now points OUT from the block like IE)
        Direction facing = state.getValue(ConnectorBlock.FACING);
        // IE uses getFacing().getOpposite() but our facing is already correct
        Direction side = facing.getOpposite();
        
        // getConnectorLength returns the distance from block edge (like IE's LENGTH values)
        // We need to convert to distance from center by subtracting 0.5
        double lengthFromHalf = getConnectorLength() - type.getRenderDiameter() / 2 - 0.5;
        
        ModularInfrastructure.LOGGER.debug("getConnectionOffset at {} - facing: {}, side: {}, connectorLength: {}, wireRadius: {}, lengthFromHalf: {}", 
            worldPosition, facing, side, getConnectorLength(), type.getRenderDiameter() / 2, lengthFromHalf);
        
        // Return absolute position in block space
        Vec3 result = new Vec3(
            0.5 + lengthFromHalf * side.getStepX(),
            0.5 + lengthFromHalf * side.getStepY(),
            0.5 + lengthFromHalf * side.getStepZ()
        );
        
        ModularInfrastructure.LOGGER.info("getConnectionOffset result: {}", result);
        return result;
    }
    
    /**
     * Get the length of the connector from block edge to connection point
     * This matches IE's LENGTH values (distance from block face)
     * Override this in subclasses for specific connector types
     */
    protected double getConnectorLength() {
        // Default length for generic connectors (matching IE's default)
        return 0.5; // 8 pixels from edge (center of block)
    }
    
    @Override
    public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset) {
        // Default implementation - single connection point at index 0
        // Override in subclasses for connectors with multiple connection points
        return new ConnectionPoint(worldPosition, 0);
    }
    
    @Override
    public boolean canConnect() {
        // Connectors can always connect to wires
        return true;
    }
    
    @Override
    public BlockPos getConnectionMaster(@Nullable WireType cableType, TargetingInfo target) {
        // For simple connectors, they are their own master
        return worldPosition;
    }
    
    @Override
    public boolean canConnectCable(WireType type, ConnectionPoint target, Vec3i offset) {
        if (connections.size() >= maxConnections) {
            return false;
        }
        ConnectorBlock block = (ConnectorBlock) getBlockState().getBlock();
        return block.canAcceptWire(type);
    }
    
    @Override
    public void connectCable(WireType type, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) {
        // Network handles the actual connection storage
    }
    
    @Override
    public void removeCable(Connection connection, ConnectionPoint point) {
        // Network handles the actual connection removal
    }
    
    @Override
    public Collection<Connection> getInternalConnections() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean isProxy() {
        return false;
    }
    
    /**
     * Remove a wire connection from this connector
     */
    public boolean removeConnection(BlockPos otherPos) {
        boolean removed = connections.removeIf(conn -> conn.targetPos.equals(otherPos));
        if (removed) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            }
        }
        return removed;
    }
    
    /**
     * Get all connections from this connector
     */
    public List<WireConnection> getConnections() {
        return new ArrayList<>(connections);
    }
    
    /**
     * Get the connection point offset for wire rendering
     * @deprecated Use getConnectionOffset instead - this method may be removed
     */
    @Deprecated
    public Vec3 getConnectionPoint() {
        // Use the same logic as getConnectionOffset for consistency
        ConnectionPoint here = new ConnectionPoint(worldPosition, 0);
        ConnectionPoint other = new ConnectionPoint(worldPosition, 0); // Dummy point
        Vec3 offset = getConnectionOffset(here, other, com.miniverse.modularinfrastructure.common.wires.ModWireTypes.COPPER_LV);
        
        // Convert from block-relative to world coordinates
        return Vec3.atLowerCornerOf(worldPosition).add(offset);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        ListTag connectionList = new ListTag();
        for (WireConnection conn : connections) {
            CompoundTag connTag = new CompoundTag();
            connTag.put("pos", NbtUtils.writeBlockPos(conn.targetPos));
            connTag.putString("type", conn.wireType.getUniqueName());
            connectionList.add(connTag);
        }
        tag.put("connections", connectionList);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        connections.clear();
        ListTag connectionList = tag.getList("connections", Tag.TAG_COMPOUND);
        for (int i = 0; i < connectionList.size(); i++) {
            CompoundTag connTag = connectionList.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(connTag, "pos").orElse(null);
            String typeName = connTag.getString("type");
            
            if (pos != null && !typeName.isEmpty()) {
                WireType wireType = com.miniverse.modularinfrastructure.common.wires.ModWireTypes.getWireType(typeName);
                if (wireType != null) {
                    connections.add(new WireConnection(pos, wireType));
                }
            }
        }
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    
    /**
     * Data class for wire connections
     */
    public static class WireConnection {
        public final BlockPos targetPos;
        public final WireType wireType;
        
        public WireConnection(BlockPos targetPos, WireType wireType) {
            this.targetPos = targetPos;
            this.wireType = wireType;
        }
    }
}