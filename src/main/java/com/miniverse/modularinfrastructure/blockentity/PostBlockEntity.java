package com.miniverse.modularinfrastructure.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.miniverse.modularinfrastructure.ModBlockEntities;
import com.miniverse.modularinfrastructure.block.PostBlock;

public class PostBlockEntity extends BlockEntity {
    private int width = 8; // Default 8px width
    private PostMaterial material = PostMaterial.WOOD_OAK;
    
    // TODO: Wire connections, attachments, etc.
    
    public PostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POST_BLOCK_ENTITY.get(), pos, state);
        this.width = PostBlock.getPixelWidth(state);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Width", this.width);
        tag.putString("Material", this.material.name());
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.width = tag.getInt("Width");
        if (tag.contains("Material")) {
            this.material = PostMaterial.valueOf(tag.getString("Material"));
        }
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, PostBlockEntity blockEntity) {
        // TODO: Handle wire network updates, animations, etc.
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public void setWidth(int width) {
        if (PostBlock.isValidWidth(width) && this.width != width) {
            this.width = width;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }
    
    public PostMaterial getMaterial() {
        return this.material;
    }
    
    public void setMaterial(PostMaterial material) {
        if (this.material != material) {
            this.material = material;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }
    
    public enum PostMaterial {
        WOOD_OAK("oak"),
        WOOD_BIRCH("birch"),
        WOOD_SPRUCE("spruce"),
        WOOD_DARK_OAK("dark_oak"),
        WOOD_ACACIA("acacia"),
        WOOD_CHERRY("cherry"),
        WOOD_MANGROVE("mangrove"),
        METAL_IRON("iron"),
        METAL_STEEL("steel"),
        METAL_ALUMINUM("aluminum"),
        CONCRETE("concrete"),
        CONCRETE_REINFORCED("reinforced_concrete"),
        COMPOSITE("composite");
        
        private final String textureName;
        
        PostMaterial(String textureName) {
            this.textureName = textureName;
        }
        
        public String getTextureName() {
            return textureName;
        }
    }
}