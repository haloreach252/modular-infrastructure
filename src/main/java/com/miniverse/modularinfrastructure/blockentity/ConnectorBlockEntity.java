package com.miniverse.modularinfrastructure.blockentity;

import com.miniverse.modularinfrastructure.api.wire.WireType;
import com.miniverse.modularinfrastructure.block.ConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Base block entity for all connectors
 * Handles wire connections and network participation
 */
public abstract class ConnectorBlockEntity extends BlockEntity {
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
     */
    public Vec3 getConnectionPoint() {
        // Get the facing direction from the block state
        if (getBlockState().getBlock() instanceof ConnectorBlock) {
            Direction facing = getBlockState().getValue(ConnectorBlock.FACING);
            Vec3 center = Vec3.atCenterOf(worldPosition);
            
            // Offset the connection point based on facing direction
            // Connection point should be on the face of the connector
            return switch (facing) {
                case NORTH -> center.add(0, 0, -0.3);
                case SOUTH -> center.add(0, 0, 0.3);
                case EAST -> center.add(0.3, 0, 0);
                case WEST -> center.add(-0.3, 0, 0);
                case UP -> center.add(0, 0.3, 0);
                case DOWN -> center.add(0, -0.3, 0);
            };
        }
        
        // Fallback to center
        return Vec3.atCenterOf(worldPosition);
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
            
            if (pos != null) {
                // TODO: Look up wire type by name
                // For now, we'll need a wire type registry
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