package com.miniverse.modularinfrastructure.block;

import com.miniverse.modularinfrastructure.api.wires.WireType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for all connector blocks
 * Connectors attach to posts and allow wire connections
 */
public abstract class ConnectorBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    
    // Cache for dynamic hitboxes based on connector type and facing
    private final Map<Direction, VoxelShape> shapeCache = new ConcurrentHashMap<>();
    
    // Base shape for UP-facing connector (override in subclasses for different sizes)
    protected VoxelShape getBaseShape() {
        // Default shape: 8x12x8 centered, extending upward from base
        return Block.box(4, 0, 4, 12, 12, 12);
    }
    
    protected ConnectorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        
        // The connector should face in the same direction as the clicked face
        // (pointing OUT from the support block, like IE does)
        Direction facing = clickedFace;
        BlockPos supportPos = pos.relative(clickedFace.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        
        if (isValidSupport(supportState, level, supportPos, clickedFace.getOpposite())) {
            return this.defaultBlockState().setValue(FACING, facing);
        }
        
        // If that doesn't work, check all directions for a valid support
        for (Direction dir : Direction.values()) {
            BlockPos checkPos = pos.relative(dir.getOpposite());
            BlockState checkState = level.getBlockState(checkPos);
            if (isValidSupport(checkState, level, checkPos, dir.getOpposite())) {
                return this.defaultBlockState().setValue(FACING, dir);
            }
        }
        
        return null; // Can't place without support
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return shapeCache.computeIfAbsent(facing, this::calculateRotatedShape);
    }
    
    /**
     * Calculate the rotated shape for a given facing direction
     */
    private VoxelShape calculateRotatedShape(Direction facing) {
        VoxelShape baseShape = getBaseShape();
        
        // If facing UP, return base shape as-is
        if (facing == Direction.UP) {
            return baseShape;
        }
        
        // Get the bounds of the base shape
        double minX = baseShape.min(Direction.Axis.X) * 16;
        double minY = baseShape.min(Direction.Axis.Y) * 16;
        double minZ = baseShape.min(Direction.Axis.Z) * 16;
        double maxX = baseShape.max(Direction.Axis.X) * 16;
        double maxY = baseShape.max(Direction.Axis.Y) * 16;
        double maxZ = baseShape.max(Direction.Axis.Z) * 16;
        
        // Calculate dimensions
        double width = maxX - minX;
        double height = maxY - minY;
        double depth = maxZ - minZ;
        
        // Rotate the shape based on facing (connector extends in facing direction)
        return switch (facing) {
            case UP -> baseShape; // Default orientation
            case DOWN -> Block.box(minX, 16 - height, minZ, maxX, 16, maxZ);
            case NORTH -> Block.box(minX, minY + (16 - height) / 2, 0, maxX, minY + (16 + height) / 2, height);
            case SOUTH -> Block.box(minX, minY + (16 - height) / 2, 16 - height, maxX, minY + (16 + height) / 2, 16);
            case WEST -> Block.box(0, minY + (16 - height) / 2, minZ, height, minY + (16 + height) / 2, maxZ);
            case EAST -> Block.box(16 - height, minY + (16 - height) / 2, minZ, 16, minY + (16 + height) / 2, maxZ);
        };
    }
    
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, 
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // Remove connector if support is removed
        Direction facing = state.getValue(FACING);
        // Check if the block behind the connector (opposite of facing) was removed
        if (direction == facing.getOpposite() && !isValidSupport(neighborState, level, neighborPos, direction)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    
    /**
     * Check if a block state is a valid support for this connector
     */
    protected boolean isValidSupport(BlockState state, BlockGetter level, BlockPos supportPos, Direction face) {
        // Allow placement on any non-air block (more permissive than buttons)
        return !state.isAir();
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    /**
     * Get the wire category this connector accepts
     */
    public abstract String getWireCategory();
    
    /**
     * Get the tier of this connector (for tiered systems like power)
     */
    public abstract int getTier();
    
    /**
     * Check if this connector can accept a specific wire type
     */
    public boolean canAcceptWire(WireType wireType) {
        return wireType.getCategory().equals(getWireCategory());
    }
}