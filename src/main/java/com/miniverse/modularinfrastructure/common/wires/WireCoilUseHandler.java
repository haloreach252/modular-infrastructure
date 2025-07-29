package com.miniverse.modularinfrastructure.common.wires;

import com.miniverse.modularinfrastructure.api.IEApiDataComponents;
import com.miniverse.modularinfrastructure.api.wires.*;
import com.miniverse.modularinfrastructure.api.TargetingInfo;
import com.miniverse.modularinfrastructure.api.wires.utils.WireUtils;
import com.miniverse.modularinfrastructure.api.wires.utils.WirecoilUtils;
import com.miniverse.modularinfrastructure.api.wires.utils.WireLink;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.miniverse.modularinfrastructure.api.wires.utils.WireUtils.findObstructingBlocks;

public class WireCoilUseHandler implements WirecoilUtils.UseCallback {
    @Override
    public InteractionResult doCoilUse(IWireCoil coil, Player player, Level world, BlockPos pos, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof IImmersiveConnectable connectable)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        WireType wireType = coil.getWireType(stack);
        if (wireType == null) {
            return InteractionResult.FAIL;
        }

        TargetingInfo targetHere = new TargetingInfo(side, hitX - pos.getX(), hitY - pos.getY(), hitZ - pos.getZ());
        BlockPos masterPos = connectable.getConnectionMaster(wireType, targetHere);
        BlockPos masterOffsetHere = pos.subtract(masterPos);
        tile = world.getBlockEntity(masterPos);
        if (!(tile instanceof IImmersiveConnectable iicHere) || !iicHere.canConnect()) {
            return InteractionResult.PASS;
        }
        ConnectionPoint cpHere = iicHere.getTargetedPoint(targetHere, masterOffsetHere);
        
        if (cpHere == null || !iicHere.canConnectCable(wireType, cpHere, masterOffsetHere) || !coil.canConnectCable(stack, tile)) {
            if (!world.isClientSide) {
                player.displayClientMessage(Component.translatable("modularinfrastructure.chat.error.wrongCable"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!world.isClientSide) {

            final WireLink storedLink = stack.get(IEApiDataComponents.WIRE_LINK);
            if (storedLink == null) {
                stack.set(IEApiDataComponents.WIRE_LINK, WireLink.create(cpHere, world, masterOffsetHere, targetHere));
            } else {
                BlockEntity tileEntityLinkingPos = world.getBlockEntity(storedLink.cp().position());
                int distanceSq = (int)Math.ceil(storedLink.cp().position().distSqr(masterPos));
                int maxLengthSq = coil.getMaxLength(stack); 
                maxLengthSq *= maxLengthSq;
                if (!storedLink.dimension().equals(world.dimension())) {
                    player.displayClientMessage(Component.translatable("modularinfrastructure.chat.error.wrongDimension"), true);
                } else if (storedLink.cp().position().equals(masterPos)) {
                    player.displayClientMessage(Component.translatable("modularinfrastructure.chat.error.sameConnection"), true);
                } else if (distanceSq > maxLengthSq) {
                    player.displayClientMessage(Component.translatable("modularinfrastructure.chat.error.tooFar"), true);
                } else {
                    if (!(tileEntityLinkingPos instanceof IImmersiveConnectable iicLink)) {
                        player.displayClientMessage(Component.translatable("modularinfrastructure.chat.error.invalidPoint"), true);
                    } else {
                        if (!iicLink.canConnectCable(wireType, storedLink.cp(), storedLink.offset()) ||
                                !iicLink.getConnectionMaster(wireType, storedLink.target()).equals(storedLink.cp().position()) ||
                                !coil.canConnectCable(stack, tileEntityLinkingPos)) {
                            player.displayClientMessage(Component.translatable("modularinfrastructure.chat.error.invalidPoint"), true);
                        } else {
                            GlobalWireNetwork net = GlobalWireNetwork.getNetwork(world);
                            boolean connectionExists = false;
                            LocalWireNetwork localA = net.getLocalNet(cpHere);
                            LocalWireNetwork localB = net.getLocalNet(storedLink.cp());
                            if (localA == localB) {
                                Collection<Connection> outputs = localA.getConnections(cpHere);
                                if (outputs != null) {
                                    for (Connection con : outputs) {
                                        if (!con.isInternal() && con.getOtherEnd(cpHere).equals(storedLink.cp())) {
                                            connectionExists = true;
                                        }
                                    }
                                }
                            }
                            if (connectionExists) {
                                player.displayClientMessage(Component.translatable("modularinfrastructure.chat.error.connectionExists"), true);
                            } else {
                                Set<BlockPos> ignore = new HashSet<>();
                                ignore.addAll(iicHere.getIgnored(iicLink));
                                ignore.addAll(iicLink.getIgnored(iicHere));
                                Connection conn = new Connection(wireType, cpHere, storedLink.cp(), net);
                                Set<BlockPos> failedReasons = findObstructingBlocks(world, conn, ignore);
                                if (failedReasons.isEmpty()) {
                                    net.addConnection(conn);
                                    
                                    iicHere.connectCable(wireType, cpHere, iicLink, storedLink.cp());
                                    iicLink.connectCable(wireType, storedLink.cp(), iicHere, cpHere);
                                    
                                    if (!player.getAbilities().instabuild) {
                                        coil.consumeWire(stack, (int)Math.sqrt(distanceSq));
                                    }
                                    ((BlockEntity)iicHere).setChanged();
                                    world.blockEvent(masterPos, ((BlockEntity)iicHere).getBlockState().getBlock(), -1, 0);
                                    BlockState state = world.getBlockState(masterPos);
                                    world.sendBlockUpdated(masterPos, state, state, 3);
                                    ((BlockEntity)iicLink).setChanged();
                                    world.blockEvent(storedLink.cp().position(), tileEntityLinkingPos.getBlockState().getBlock(), -1, 0);
                                    state = world.getBlockState(storedLink.cp().position());
                                    world.sendBlockUpdated(storedLink.cp().position(), state, state, 3);
                                } else {
                                    player.displayClientMessage(Component.translatable("modularinfrastructure.chat.error.cantSee"), true);
                                }
                            }
                        }
                    }
                }
                stack.remove(IEApiDataComponents.WIRE_LINK);
            }
        }
        return InteractionResult.SUCCESS;
    }
}