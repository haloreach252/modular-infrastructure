package com.miniverse.modularinfrastructure.blockentity;

import com.google.common.collect.ImmutableList;
import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.api.energy.MutableEnergyStorage;
import com.miniverse.modularinfrastructure.api.wires.ConnectionPoint;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.EnergyTransferHandler;
import com.miniverse.modularinfrastructure.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import com.miniverse.modularinfrastructure.block.PowerConnectorBlock;
import com.miniverse.modularinfrastructure.common.util.EnergyHelper;
import com.miniverse.modularinfrastructure.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Collection;

/**
 * Block entity for power connectors
 * Handles energy storage and distribution through wire networks
 * 
 * Based on Immersive Engineering's EnergyConnectorBlockEntity
 * Original implementation by BluSunrize and the IE team
 * Used under the "Blu's License of Common Sense"
 */
public class PowerConnectorBlockEntity extends ConnectorBlockEntity implements EnergyConnector {
    private final PowerConnectorBlock.PowerTier tier;
    private final boolean relay;
    
    // Dual storage system for network energy transfer
    private final MutableEnergyStorage storageToNet;
    private final MutableEnergyStorage storageToMachine;
    private final ConnectorEnergyStorage energyCap;
    
    // Track energy transferred in current tick
    public int currentTickToMachine = 0;
    public int currentTickToNet = 0;
    
    public PowerConnectorBlockEntity(BlockPos pos, BlockState state, PowerConnectorBlock.PowerTier tier) {
        super(ModBlockEntities.POWER_CONNECTOR.get(), pos, state);
        this.tier = tier;
        this.relay = false; // TODO: Add relay support
        
        // Initialize dual storage system
        this.storageToMachine = new MutableEnergyStorage(getMaxInput(), getMaxInput(), getMaxInput());
        this.storageToNet = new MutableEnergyStorage(getMaxInput(), getMaxInput(), getMaxInput());
        this.energyCap = new ConnectorEnergyStorage();
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
        
        // Transfer energy from network buffer to adjacent machines
        int maxOut = Math.min(storageToMachine.getEnergyStored(), getMaxOutput() - currentTickToMachine);
        if (maxOut > 0) {
            Direction facing = getBlockState().getValue(PowerConnectorBlock.FACING);
            BlockPos targetPos = worldPosition.relative(facing);
            BlockEntity targetBE = level.getBlockEntity(targetPos);
            
            if (targetBE != null) {
                IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, 
                        targetPos, facing.getOpposite(), targetBE, null);
                if (target != null && target.canReceive()) {
                    int inserted = target.receiveEnergy(maxOut, false);
                    storageToMachine.extractEnergy(inserted, false);
                    currentTickToMachine += inserted;
                }
            }
        }
        
        // Reset tick counters
        currentTickToMachine = 0;
        currentTickToNet = 0;
    }
    
    // EnergyConnector implementation
    @Override
    public boolean isSource(ConnectionPoint cp) {
        return !relay;
    }
    
    @Override
    public boolean isSink(ConnectionPoint cp) {
        return !relay;
    }
    
    @Override
    public int getAvailableEnergy() {
        return storageToNet.getEnergyStored();
    }
    
    @Override
    public int getRequestedEnergy() {
        return storageToMachine.getMaxEnergyStored() - storageToMachine.getEnergyStored();
    }
    
    @Override
    public void insertEnergy(int amount) {
        storageToMachine.receiveEnergy(amount, false);
    }
    
    @Override
    public void extractEnergy(int amount) {
        storageToNet.extractEnergy(amount, false);
    }
    
    // Request energy transfer handler for this connector
    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        // Power connectors need both energy transfer and wire damage handlers
        return ImmutableList.of(
            EnergyTransferHandler.ID,
            com.miniverse.modularinfrastructure.api.wires.localhandlers.WireDamageHandler.ID
        );
    }
    
    // Helper methods
    public int getMaxInput() {
        return tier.getCapacity();
    }
    
    public int getMaxOutput() {
        return tier.getCapacity();
    }
    
    // Energy capability for external access
    public IEnergyStorage getEnergyCapability() {
        return energyCap;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // Save dual storage
        CompoundTag toNet = new CompoundTag();
        EnergyHelper.serializeTo(storageToNet, toNet, registries);
        tag.put("toNet", toNet);
        
        CompoundTag toMachine = new CompoundTag();
        EnergyHelper.serializeTo(storageToMachine, toMachine, registries);
        tag.put("toMachine", toMachine);
        
        tag.putString("tier", tier.name());
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // Load dual storage
        CompoundTag toMachine = tag.getCompound("toMachine");
        EnergyHelper.deserializeFrom(storageToMachine, toMachine, registries);
        
        CompoundTag toNet = tag.getCompound("toNet");
        EnergyHelper.deserializeFrom(storageToNet, toNet, registries);
    }
    
    // Inner class for energy capability
    private class ConnectorEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (level.isClientSide || relay) {
                return 0;
            }
            
            maxReceive = Math.min(getMaxInput() - currentTickToNet, maxReceive);
            if (maxReceive <= 0) {
                return 0;
            }
            
            int accepted = Math.min(Math.min(getMaxOutput(), getMaxInput()), maxReceive);
            accepted = Math.min(getMaxOutput() - storageToNet.getEnergyStored(), accepted);
            if (accepted <= 0) {
                return 0;
            }
            
            if (!simulate) {
                storageToNet.modifyEnergyStored(accepted);
                currentTickToNet += accepted;
                setChanged();
            }
            
            return accepted;
        }
        
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return storageToMachine.extractEnergy(maxExtract, simulate);
        }
        
        @Override
        public int getEnergyStored() {
            return storageToNet.getEnergyStored();
        }
        
        @Override
        public int getMaxEnergyStored() {
            return storageToNet.getMaxEnergyStored();
        }
        
        @Override
        public boolean canExtract() {
            return true;
        }
        
        @Override
        public boolean canReceive() {
            return true;
        }
    }
}