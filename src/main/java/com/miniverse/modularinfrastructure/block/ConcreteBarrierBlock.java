package com.miniverse.modularinfrastructure.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
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

public class ConcreteBarrierBlock extends CrossCollisionBlock {
    public static final MapCodec<ConcreteBarrierBlock> CODEC = simpleCodec(ConcreteBarrierBlock::new);
    public static final EnumProperty<BarrierType> TYPE = EnumProperty.create("type", BarrierType.class);
    public static final BooleanProperty IS_BASE = BooleanProperty.create("is_base");
    
    // Barrier shapes - concrete barriers are wider than fences
    private static final VoxelShape STRAIGHT_SHAPE = Block.box(0.0, 0.0, 5.0, 16.0, 16.0, 11.0);
    private static final VoxelShape STRAIGHT_SHAPE_NS = Block.box(5.0, 0.0, 0.0, 11.0, 16.0, 16.0);
    
    // Corner shapes
    private static final VoxelShape CORNER_SHAPE_NE = Shapes.or(
        Block.box(5.0, 0.0, 5.0, 16.0, 16.0, 11.0), // East part
        Block.box(5.0, 0.0, 0.0, 11.0, 16.0, 11.0)  // North part
    );
    private static final VoxelShape CORNER_SHAPE_SE = Shapes.or(
        Block.box(5.0, 0.0, 5.0, 16.0, 16.0, 11.0), // East part
        Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 16.0)  // South part
    );
    private static final VoxelShape CORNER_SHAPE_SW = Shapes.or(
        Block.box(0.0, 0.0, 5.0, 11.0, 16.0, 11.0), // West part
        Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 16.0)  // South part
    );
    private static final VoxelShape CORNER_SHAPE_NW = Shapes.or(
        Block.box(0.0, 0.0, 5.0, 11.0, 16.0, 11.0), // West part
        Block.box(5.0, 0.0, 0.0, 11.0, 16.0, 11.0)  // North part
    );
    
    // T-junction shapes
    private static final VoxelShape T_SHAPE_NORTH = Shapes.or(
        Block.box(0.0, 0.0, 5.0, 16.0, 16.0, 11.0), // East-West part
        Block.box(5.0, 0.0, 0.0, 11.0, 16.0, 11.0)  // North part
    );
    private static final VoxelShape T_SHAPE_EAST = Shapes.or(
        Block.box(5.0, 0.0, 0.0, 11.0, 16.0, 16.0), // North-South part
        Block.box(5.0, 0.0, 5.0, 16.0, 16.0, 11.0)  // East part
    );
    private static final VoxelShape T_SHAPE_SOUTH = Shapes.or(
        Block.box(0.0, 0.0, 5.0, 16.0, 16.0, 11.0), // East-West part
        Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 16.0)  // South part
    );
    private static final VoxelShape T_SHAPE_WEST = Shapes.or(
        Block.box(5.0, 0.0, 0.0, 11.0, 16.0, 16.0), // North-South part
        Block.box(0.0, 0.0, 5.0, 11.0, 16.0, 11.0)  // West part
    );
    
    // Cross shape (junction)
    private static final VoxelShape CROSS_SHAPE = Shapes.or(
        Block.box(0.0, 0.0, 5.0, 16.0, 16.0, 11.0), // East-West part
        Block.box(5.0, 0.0, 0.0, 11.0, 16.0, 16.0)  // North-South part
    );
    
    public ConcreteBarrierBlock(Properties properties) {
        super(2.0F, 2.0F, 16.0F, 16.0F, 24.0F, properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(TYPE, BarrierType.STRAIGHT)
            .setValue(IS_BASE, true)
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(WATERLOGGED, false));
    }
    
    @Override
    public MapCodec<? extends ConcreteBarrierBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, IS_BASE, NORTH, EAST, SOUTH, WEST, WATERLOGGED);
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
        
        // Check if this is a base block (no concrete barrier below)
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        boolean isBase = !(belowState.getBlock() instanceof ConcreteBarrierBlock);
        state = state.setValue(IS_BASE, isBase);
        
        // Determine barrier type based on connections
        BarrierType barrierType = determineBarrierType(north, south, east, west);
        state = state.setValue(TYPE, barrierType);
        
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
            
            // Recalculate barrier type
            boolean north = state.getValue(NORTH);
            boolean south = state.getValue(SOUTH);
            boolean east = state.getValue(EAST);
            boolean west = state.getValue(WEST);
            
            BarrierType newType = determineBarrierType(north, south, east, west);
            state = state.setValue(TYPE, newType);
        } else if (direction == Direction.DOWN) {
            // Check if this is still a base block
            BlockState belowState = level.getBlockState(neighborPos);
            boolean isBase = !(belowState.getBlock() instanceof ConcreteBarrierBlock);
            state = state.setValue(IS_BASE, isBase);
        }
        
        return state;
    }
    
    private BarrierType determineBarrierType(boolean north, boolean south, boolean east, boolean west) {
        int connections = 0;
        if (north) connections++;
        if (south) connections++;
        if (east) connections++;
        if (west) connections++;
        
        switch (connections) {
            case 0:
            case 1:
                // With 0 or 1 connection, always use straight
                return BarrierType.STRAIGHT;
            case 2:
                // Check if straight line or corner
                if ((north && south) || (east && west)) {
                    return BarrierType.STRAIGHT;
                } else {
                    return BarrierType.CORNER;
                }
            case 3:
                return BarrierType.T_JUNCTION;
            case 4:
                return BarrierType.CROSS;
            default:
                return BarrierType.STRAIGHT;
        }
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BarrierType type = state.getValue(TYPE);
        boolean north = state.getValue(NORTH);
        boolean south = state.getValue(SOUTH);
        boolean east = state.getValue(EAST);
        boolean west = state.getValue(WEST);
        
        switch (type) {
            case STRAIGHT:
                // Check orientation
                if (north || south) {
                    return STRAIGHT_SHAPE_NS;
                } else {
                    return STRAIGHT_SHAPE;
                }
            
            case CORNER:
                // Determine which corner based on connections
                if (north && east) return CORNER_SHAPE_NE;
                if (south && east) return CORNER_SHAPE_SE;
                if (south && west) return CORNER_SHAPE_SW;
                if (north && west) return CORNER_SHAPE_NW;
                return STRAIGHT_SHAPE; // Fallback
            
            case T_JUNCTION:
                // Determine which T based on the missing connection
                if (!north) return T_SHAPE_SOUTH;
                if (!east) return T_SHAPE_WEST;
                if (!south) return T_SHAPE_NORTH;
                if (!west) return T_SHAPE_EAST;
                return CROSS_SHAPE; // Fallback
            
            case CROSS:
                return CROSS_SHAPE;
            
            default:
                return STRAIGHT_SHAPE;
        }
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Concrete barriers use the same shape for collision
        return getShape(state, level, pos, context);
    }
    
    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
    
    public boolean connectsTo(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Block block = state.getBlock();
        
        // Connect to other concrete barriers
        if (block instanceof ConcreteBarrierBlock) {
            return true;
        }
        
        // Connect to solid faces
        if (state.isFaceSturdy(level, pos, direction)) {
            return true;
        }
        
        return false;
    }
    
    public enum BarrierType implements StringRepresentable {
        STRAIGHT("straight"),
        CORNER("corner"),
        T_JUNCTION("t_junction"),
        CROSS("cross");
        
        private final String name;
        
        BarrierType(String name) {
            this.name = name;
        }
        
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
