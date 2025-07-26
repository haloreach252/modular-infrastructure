package com.miniverse.modularinfrastructure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.blockentity.PostBlockEntity;
import com.mojang.serialization.MapCodec;

import javax.annotation.Nullable;

public class PostBlock extends BaseEntityBlock {
    // Width property: 4-16 pixels in 2px increments (stored as 2-8 for efficiency)
    public static final IntegerProperty WIDTH = IntegerProperty.create("width", 2, 8);
    
    // Codec for serialization
    public static final MapCodec<PostBlock> CODEC = simpleCodec(PostBlock::new);
    
    // VoxelShape cache for different widths
    private static final VoxelShape[] SHAPES = new VoxelShape[7];
    
    static {
        // Pre-calculate shapes for each width
        for (int i = 2; i <= 8; i++) {
            int actualWidth = i * 2; // Convert to pixels
            double radius = actualWidth / 2.0;
            double min = 8.0 - radius;
            double max = 8.0 + radius;
            SHAPES[i - 2] = Shapes.box(min / 16.0, 0.0, min / 16.0, max / 16.0, 1.0, max / 16.0);
        }
    }
    
    public PostBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WIDTH, 4)); // Default 8px width
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WIDTH);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(WIDTH) - 2];
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        int width = 4; // Default 8px
        
        // Check if item has width data in custom data component
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains("PostWidth")) {
            width = customData.copyTag().getInt("PostWidth");
            width = Math.max(2, Math.min(8, width)); // Clamp to valid range
        }
        
        return this.defaultBlockState().setValue(WIDTH, width);
    }
    
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            // Shift+right-click to cycle size
            int currentWidth = state.getValue(WIDTH);
            int newWidth = currentWidth == 8 ? 2 : currentWidth + 1;
            level.setBlock(pos, state.setValue(WIDTH, newWidth), 3);
            
            // Update block entity
            if (level.getBlockEntity(pos) instanceof PostBlockEntity postEntity) {
                postEntity.setWidth(newWidth * 2);
            }
            
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PostBlockEntity(pos, state);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.POST_BLOCK_ENTITY.get(), PostBlockEntity::tick);
    }
    
    public static int getPixelWidth(BlockState state) {
        return state.getValue(WIDTH) * 2;
    }
    
    public static boolean isValidWidth(int pixelWidth) {
        return pixelWidth >= 4 && pixelWidth <= 16 && pixelWidth % 2 == 0;
    }
}