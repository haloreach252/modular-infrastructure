package com.miniverse.modularinfrastructure.block;

import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.api.wires.WireType;
import com.miniverse.modularinfrastructure.blockentity.UtilityConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Utility connector block for redstone and structural connections
 */
public class UtilityConnectorBlock extends ConnectorBlock {
    public enum UtilityType {
        REDSTONE(WireType.REDSTONE_CATEGORY, "Redstone"),
        STRUCTURAL(WireType.STRUCTURE_CATEGORY, "Structural");
        
        private final String category;
        private final String displayName;
        
        UtilityType(String category, String displayName) {
            this.category = category;
            this.displayName = displayName;
        }
        
        public String getCategory() {
            return category;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final UtilityType utilityType;
    
    public UtilityConnectorBlock(Properties properties, UtilityType utilityType) {
        super(properties);
        this.utilityType = utilityType;
    }
    
    @Override
    public String getWireCategory() {
        return utilityType.getCategory();
    }
    
    @Override
    public int getTier() {
        return 1; // Utility connectors don't have tiers
    }
    
    public UtilityType getUtilityType() {
        return utilityType;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UtilityConnectorBlockEntity(pos, state, utilityType);
    }
    
    @Override
    protected VoxelShape getBaseShape() {
        // Utility connectors have different shapes based on type
        return switch(utilityType) {
            case REDSTONE -> Block.box(5, 0, 5, 11, 6, 11);   // Redstone: 6x6x6
            case STRUCTURAL -> Block.box(5, 0, 5, 11, 8, 11); // Structural: 6x8x6
        };
    }
}