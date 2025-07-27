package com.miniverse.modularinfrastructure.block;

import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.api.wire.WireType;
import com.miniverse.modularinfrastructure.blockentity.PowerConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Power connector block for different voltage tiers
 */
public class PowerConnectorBlock extends ConnectorBlock {
    public enum PowerTier {
        LV(1, WireType.POWER_LV, "Low Voltage", 256),
        MV(2, WireType.POWER_MV, "Medium Voltage", 1024),
        HV(3, WireType.POWER_HV, "High Voltage", 4096);
        
        private final int tier;
        private final String category;
        private final String displayName;
        private final int capacity;
        
        PowerTier(int tier, String category, String displayName, int capacity) {
            this.tier = tier;
            this.category = category;
            this.displayName = displayName;
            this.capacity = capacity;
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
        
        public int getCapacity() {
            return capacity;
        }
    }
    
    private final PowerTier powerTier;
    
    public PowerConnectorBlock(Properties properties, PowerTier powerTier) {
        super(properties);
        this.powerTier = powerTier;
    }
    
    @Override
    public String getWireCategory() {
        return powerTier.getCategory();
    }
    
    @Override
    public int getTier() {
        return powerTier.getTier();
    }
    
    public PowerTier getPowerTier() {
        return powerTier;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PowerConnectorBlockEntity(pos, state, powerTier);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide && blockEntityType == ModBlockEntities.POWER_CONNECTOR.get()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof PowerConnectorBlockEntity powerBE) {
                    powerBE.tick();
                }
            };
        }
        return null;
    }
    
    @Override
    protected VoxelShape getBaseShape() {
        // Power connectors have different sizes based on tier
        return switch(powerTier) {
            case LV -> Block.box(4, 0, 4, 12, 9, 12);  // LV: 8x9x8
            case MV -> Block.box(4, 0, 4, 12, 10, 12); // MV: 8x10x8
            case HV -> Block.box(3, 0, 3, 13, 12, 13); // HV: 10x12x10
        };
    }
}