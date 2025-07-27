package com.miniverse.modularinfrastructure.block;

import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.api.wire.WireType;
import com.miniverse.modularinfrastructure.blockentity.DataConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Data connector block for different channel capacities
 */
public class DataConnectorBlock extends ConnectorBlock {
    public enum DataTier {
        BASIC(1, WireType.DATA_BASIC, "Basic Data", 8),
        ADVANCED(2, WireType.DATA_ADVANCED, "Advanced Data", 32);
        
        private final int tier;
        private final String category;
        private final String displayName;
        private final int channels;
        
        DataTier(int tier, String category, String displayName, int channels) {
            this.tier = tier;
            this.category = category;
            this.displayName = displayName;
            this.channels = channels;
        }
        
        public int getTier() {
            return tier;
        }
        
        public String getCategory() {
            return category;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getChannels() {
            return channels;
        }
    }
    
    private final DataTier dataTier;
    
    public DataConnectorBlock(Properties properties, DataTier dataTier) {
        super(properties);
        this.dataTier = dataTier;
    }
    
    @Override
    public String getWireCategory() {
        return dataTier.getCategory();
    }
    
    @Override
    public int getTier() {
        return dataTier.getTier();
    }
    
    public DataTier getDataTier() {
        return dataTier;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DataConnectorBlockEntity(pos, state, dataTier);
    }
    
    @Override
    protected VoxelShape getBaseShape() {
        // Data connectors have different sizes based on tier
        return switch(dataTier) {
            case BASIC -> Block.box(4, 0, 4, 12, 11, 12);    // Basic: 8x11x8
            case ADVANCED -> Block.box(3, 0, 3, 13, 11, 13); // Advanced: 10x11x10
        };
    }
}