package com.miniverse.modularinfrastructure.blockentity;

import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.block.PowerConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Block entity for power connectors
 * Handles energy storage and distribution through wire networks
 */
public class PowerConnectorBlockEntity extends ConnectorBlockEntity {
    private final PowerConnectorBlock.PowerTier tier;
    private final EnergyStorage energyStorage;
    
    public PowerConnectorBlockEntity(BlockPos pos, BlockState state, PowerConnectorBlock.PowerTier tier) {
        super(ModBlockEntities.POWER_CONNECTOR.get(), pos, state);
        this.tier = tier;
        this.energyStorage = new EnergyStorage(tier.getCapacity(), tier.getCapacity(), tier.getCapacity());
    }
    
    public PowerConnectorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, PowerConnectorBlock.PowerTier.LV);
    }
    
    @Override
    protected double getConnectorLength() {
        // Based on model dimensions - these should match IE's values
        // IE uses: LV=0.5, MV=0.5625, HV=0.75
        return switch (tier) {
            case LV -> 0.5;      // Default/LV connector length
            case MV -> 0.5625;   // MV connector length  
            case HV -> 0.75;     // HV connector length
        };
    }
    
    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // Distribute energy through wire connections
        if (energyStorage.getEnergyStored() > 0 && !connections.isEmpty()) {
            int energyPerConnection = energyStorage.getEnergyStored() / connections.size();
            
            for (WireConnection connection : connections) {
                BlockEntity targetBE = level.getBlockEntity(connection.targetPos);
                if (targetBE != null) {
                    IEnergyStorage targetEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, 
                            connection.targetPos, null, targetBE, null);
                    if (targetEnergy != null && targetEnergy.canReceive()) {
                        int transferred = targetEnergy.receiveEnergy(
                                energyStorage.extractEnergy(energyPerConnection, true), false);
                        energyStorage.extractEnergy(transferred, false);
                    }
                }
            }
        }
    }
    
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", energyStorage.serializeNBT(registries));
        tag.putString("tier", tier.name());
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(registries, tag.get("energy"));
        }
    }
}