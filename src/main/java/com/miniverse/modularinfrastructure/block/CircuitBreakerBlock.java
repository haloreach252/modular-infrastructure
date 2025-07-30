package com.miniverse.modularinfrastructure.block;

import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.blockentity.CircuitBreakerBlockEntity;
import com.miniverse.modularinfrastructure.api.wires.IWireCoil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Circuit breaker block that can interrupt wire connections
 * Supports all wire types and can be toggled manually or via redstone
 */
public class CircuitBreakerBlock extends ConnectorBlock {
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    
    @Override
    protected VoxelShape getBaseShape() {
        // Slightly larger than standard connectors - 10x14x10
        return Block.box(3, 0, 3, 13, 14, 13);
    }
    
    public CircuitBreakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ENABLED, true)
                .setValue(POWERED, false));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED, POWERED);
    }
    
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // Check if player is holding a wire coil
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof IWireCoil) {
            // Don't toggle when holding a wire coil, let the wire connection happen
            return InteractionResult.PASS;
        }
        
        if (!level.isClientSide) {
            // Toggle the circuit breaker
            boolean enabled = state.getValue(ENABLED);
            level.setBlock(pos, state.setValue(ENABLED, !enabled), 3);
            
            // Notify the block entity of the state change
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CircuitBreakerBlockEntity breaker) {
                breaker.onStateChanged(!enabled);
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3);
                
                // If receiving redstone power, disable the breaker (break the circuit)
                if (powered && state.getValue(ENABLED)) {
                    level.setBlock(pos, state.setValue(ENABLED, false), 3);
                    
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof CircuitBreakerBlockEntity breaker) {
                        breaker.onStateChanged(false);
                    }
                }
            }
        }
    }
    
    public void scheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
        }
    }
    
    @Override
    public String getWireCategory() {
        // Circuit breakers accept all wire categories
        return "universal";
    }
    
    @Override
    public int getTier() {
        // Circuit breakers work with all tiers
        return -1;
    }
    
    @Override
    public boolean canAcceptWire(com.miniverse.modularinfrastructure.api.wires.WireType wireType) {
        // Accept all wire types
        return true;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitBreakerBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide && blockEntityType == ModBlockEntities.CIRCUIT_BREAKER.get()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof CircuitBreakerBlockEntity breaker) {
                    CircuitBreakerBlockEntity.serverTick(lvl, pos, st, breaker);
                }
            };
        }
        return null;
    }
}