package com.miniverse.modularinfrastructure.network.packets;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.item.PostBlockItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdatePostItemWidthPacket(InteractionHand hand, int width) implements CustomPacketPayload {
    public static final Type<UpdatePostItemWidthPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(ModularInfrastructure.MODID, "update_post_width")
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePostItemWidthPacket> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(
            (buf, value) -> buf.writeEnum(value),
            buf -> buf.readEnum(InteractionHand.class)
        ), UpdatePostItemWidthPacket::hand,
        StreamCodec.of(
            (buf, value) -> buf.writeVarInt(value),
            RegistryFriendlyByteBuf::readVarInt
        ), UpdatePostItemWidthPacket::width,
        UpdatePostItemWidthPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(UpdatePostItemWidthPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getItemInHand(packet.hand());
            
            if (stack.getItem() instanceof PostBlockItem) {
                PostBlockItem.setStoredWidth(stack, packet.width());
                ModularInfrastructure.LOGGER.debug("Server updated post width - Player: {}, Width: {}", player.getName().getString(), packet.width());
            }
        });
    }
}