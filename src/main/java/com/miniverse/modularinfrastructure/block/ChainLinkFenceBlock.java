package com.miniverse.modularinfrastructure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.MapCodec;

public class ChainLinkFenceBlock extends CrossCollisionBlock {
    public static final MapCodec<ChainLinkFenceBlock> CODEC = simpleCodec(ChainLinkFenceBlock::new);
    public static final EnumProperty<FenceType> TYPE = EnumProperty.create("type", FenceType.class);
    
    // Base shapes for the different fence types
    private static final VoxelShape POST_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape[] SIDE_SHAPES = makeFenceShapes(2.0F, 2.0F, 16.0F, 0.0F, 16.0F);
    private static final VoxelShape[] COLLISION_SHAPES = makeFenceShapes(2.0F, 2.0F, 24.0F, 0.0F, 24.0F);
    
    // Single fence shapes (fence + post at one end)
    private static final VoxelShape SINGLE_SHAPE_EW = Shapes.or(
        Block.box(0.0, 0.0, 7.0, 12.0, 16.0, 9.0), // Fence extending west
        Block.box(12.0, 0.0, 6.0, 16.0, 16.0, 10.0) // Post on east
    );
    private static final VoxelShape SINGLE_SHAPE_NS = Shapes.or(
        Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 12.0), // Fence extending north
        Block.box(6.0, 0.0, 12.0, 10.0, 16.0, 16.0) // Post on south
    );
    
    // Edge fence shapes (shorter fence from center + center post)
    private static final VoxelShape EDGE_SHAPE_EW = Shapes.or(
        Block.box(0.0, 0.0, 7.0, 6.0, 16.0, 9.0), // Fence extending west from near center
        Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0) // Center post
    );
    private static final VoxelShape EDGE_SHAPE_NS = Shapes.or(
        Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 6.0), // Fence extending north from near center
        Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0) // Center post
    );
    
    public ChainLinkFenceBlock(Properties properties) {
        super(2.0F, 2.0F, 16.0F, 16.0F, 24.0F, properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(TYPE, FenceType.SINGLE)
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(WATERLOGGED, false));
    }
    
    @Override
    public MapCodec<? extends ChainLinkFenceBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, NORTH, EAST, SOUTH, WEST, WATERLOGGED);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluidstate = context.getLevel().getFluidState(pos);
        
        // Start with default state
        BlockState state = this.defaultBlockState()
            .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
        
        // Check connections
        boolean north = this.connectsTo(level.getBlockState(pos.north()), level, pos.north(), Direction.SOUTH);
        boolean south = this.connectsTo(level.getBlockState(pos.south()), level, pos.south(), Direction.NORTH);
        boolean east = this.connectsTo(level.getBlockState(pos.east()), level, pos.east(), Direction.WEST);
        boolean west = this.connectsTo(level.getBlockState(pos.west()), level, pos.west(), Direction.EAST);
        
        state = state
            .setValue(NORTH, north)
            .setValue(SOUTH, south)
            .setValue(EAST, east)
            .setValue(WEST, west);
        
        // Check if there's a fence below and determine its type
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        
        // Determine fence type based on connections and fence below
        state = state.setValue(TYPE, determineFenceType(north, south, east, west, belowState, level, belowPos));
        
        return state;
    }
    
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, 
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        
        // Update connection for the specific direction
        if (direction.getAxis().isHorizontal()) {
            BooleanProperty property = PROPERTY_BY_DIRECTION.get(direction);
            boolean connects = this.connectsTo(neighborState, level, neighborPos, direction.getOpposite());
            state = state.setValue(property, connects);
            
            // Recalculate fence type
            boolean north = state.getValue(NORTH);
            boolean south = state.getValue(SOUTH);
            boolean east = state.getValue(EAST);
            boolean west = state.getValue(WEST);
            
            // Check fence below
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            
            state = state.setValue(TYPE, determineFenceType(north, south, east, west, belowState, level, belowPos));
        } else if (direction == Direction.DOWN) {
            // If the block below changed, recalculate our type
            boolean north = state.getValue(NORTH);
            boolean south = state.getValue(SOUTH);
            boolean east = state.getValue(EAST);
            boolean west = state.getValue(WEST);
            
            state = state.setValue(TYPE, determineFenceType(north, south, east, west, neighborState, level, neighborPos));
        }
        
        return state;
    }
    
    private FenceType determineFenceType(boolean north, boolean south, boolean east, boolean west, 
                                       BlockState belowState, BlockGetter level, BlockPos belowPos) {
        int connections = 0;
        if (north) connections++;
        if (south) connections++;
        if (east) connections++;
        if (west) connections++;
        
        // Check if we need an edge piece
        // Simple rule: if we have 1 connection and there's a corner below, use edge
        if (connections == 1 && belowState.getBlock() instanceof ChainLinkFenceBlock) {
            FenceType belowType = belowState.getValue(TYPE);
            if (belowType == FenceType.CORNER) {
                return FenceType.EDGE;
            }
        }
        
        switch (connections) {
            case 0:
            case 1:
                return FenceType.SINGLE;
            case 2:
                // Check if straight line or corner
                if ((north && south) || (east && west)) {
                    return FenceType.STRAIGHT;
                } else {
                    return FenceType.CORNER;
                }
            case 3:
                return FenceType.T_SHAPE;
            case 4:
                return FenceType.CROSS;
            default:
                return FenceType.SINGLE; // Should never happen
        }
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        FenceType type = state.getValue(TYPE);
        boolean north = state.getValue(NORTH);
        boolean south = state.getValue(SOUTH);
        boolean east = state.getValue(EAST);
        boolean west = state.getValue(WEST);
        
        // Handle special cases for single and edge
        if (type == FenceType.SINGLE) {
            // Single fence: post is opposite to connection
            // Default EW shape: fence extends west (x=0-12), post on east (x=12-16)
            // Default NS shape: fence extends north (z=0-12), post on south (z=12-16)
            
            if (north) {
                return SINGLE_SHAPE_NS; // Fence north, post south
            } else if (east) {
                return rotateShape(SINGLE_SHAPE_EW, 180); // Rotate 180 so fence east, post west
            } else if (south) {
                return rotateShape(SINGLE_SHAPE_NS, 180); // Rotate 180 so fence south, post north
            } else if (west) {
                return SINGLE_SHAPE_EW; // Default: fence west, post east
            } else {
                // No connections - use default east-west orientation
                return SINGLE_SHAPE_EW;
            }
        }
        
        if (type == FenceType.EDGE) {
            // Edge should extend towards the connection
            // Default EW shape extends west (from x=0 to x=6)
            // Default NS shape extends north (from z=0 to z=6)
            if (north) {
                return EDGE_SHAPE_NS; // Default already extends north
            } else if (east) {
                return rotateShape(EDGE_SHAPE_EW, 180); // Rotate to extend east
            } else if (south) {
                return rotateShape(EDGE_SHAPE_NS, 180); // Rotate to extend south
            } else if (west) {
                return EDGE_SHAPE_EW; // Default already extends west
            } else {
                // No connections - use default
                return EDGE_SHAPE_EW;
            }
        }
        
        // Standard logic for other fence types
        VoxelShape shape = POST_SHAPE;
        
        // Add fence shapes based on connections
        if (!north && !south && !east && !west) {
            shape = Shapes.or(shape, SIDE_SHAPES[Direction.WEST.get2DDataValue()]);
            shape = Shapes.or(shape, SIDE_SHAPES[Direction.EAST.get2DDataValue()]);
        } else {
            if (north) shape = Shapes.or(shape, SIDE_SHAPES[Direction.NORTH.get2DDataValue()]);
            if (south) shape = Shapes.or(shape, SIDE_SHAPES[Direction.SOUTH.get2DDataValue()]);
            if (east) shape = Shapes.or(shape, SIDE_SHAPES[Direction.EAST.get2DDataValue()]);
            if (west) shape = Shapes.or(shape, SIDE_SHAPES[Direction.WEST.get2DDataValue()]);
        }
        
        return shape;
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Use the same shape as visual shape for consistency
        return getShape(state, level, pos, context);
    }
    
    private static VoxelShape rotateShape(VoxelShape shape, int degrees) {
        VoxelShape[] buffer = new VoxelShape[]{ shape, Shapes.empty() };
        int times = (degrees / 90) % 4;
        
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> 
                buffer[1] = Shapes.or(buffer[1], Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX))
            );
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        
        return buffer[0];
    }
    
    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
    
    public boolean connectsTo(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Block block = state.getBlock();
        
        // Connect to other chain link fences
        if (block instanceof ChainLinkFenceBlock) {
            return true;
        }
        
        // Connect to solid faces
        if (state.isFaceSturdy(level, pos, direction)) {
            return true;
        }
        
        // Could add more connection rules here (posts, other fence types, etc.)
        return false;
    }
    
    private static VoxelShape[] makeFenceShapes(float nodeWidth, float extensionWidth, float nodeHeight, float extensionBottom, float extensionHeight) {
        float f = 8.0F - nodeWidth;
        float f1 = 8.0F + nodeWidth;
        float f2 = 8.0F - extensionWidth;
        float f3 = 8.0F + extensionWidth;
        VoxelShape voxelshape = Block.box((double)f, 0.0, (double)f, (double)f1, (double)nodeHeight, (double)f1);
        VoxelShape voxelshape1 = Block.box((double)f2, (double)extensionBottom, 0.0, (double)f3, (double)extensionHeight, (double)f3);
        VoxelShape voxelshape2 = Block.box((double)f2, (double)extensionBottom, (double)f2, 16.0, (double)extensionHeight, (double)f3);
        VoxelShape voxelshape3 = Block.box(0.0, (double)extensionBottom, (double)f2, (double)f3, (double)extensionHeight, (double)f3);
        VoxelShape voxelshape4 = Block.box((double)f2, (double)extensionBottom, (double)f2, (double)f3, (double)extensionHeight, 16.0);
        
        // Create array indexed by Direction.get2DDataValue()
        // SOUTH = 0, WEST = 1, NORTH = 2, EAST = 3
        VoxelShape[] shapes = new VoxelShape[4];
        shapes[Direction.NORTH.get2DDataValue()] = voxelshape1; // North (negative Z)
        shapes[Direction.EAST.get2DDataValue()] = voxelshape2;  // East (positive X)
        shapes[Direction.SOUTH.get2DDataValue()] = voxelshape4; // South (positive Z)
        shapes[Direction.WEST.get2DDataValue()] = voxelshape3;  // West (negative X)
        return shapes;
    }
    
    public enum FenceType implements StringRepresentable {
        SINGLE("single"),      // No connections or 1 connection
        STRAIGHT("straight"),  // 2 opposite connections  
        CORNER("corner"),      // 2 perpendicular connections
        T_SHAPE("t_shape"),    // 3 connections
        CROSS("cross"),        // 4 connections
        EDGE("edge");          // 1 connection above a corner, doesn't extend past corner
        
        private final String name;
        
        FenceType(String name) {
            this.name = name;
        }
        
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}