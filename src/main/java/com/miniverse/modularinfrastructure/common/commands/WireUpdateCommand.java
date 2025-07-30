package com.miniverse.modularinfrastructure.common.commands;

import com.miniverse.modularinfrastructure.api.wires.Connection;
import com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.LocalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.utils.WireUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WireUpdateCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("modularwires")
            .requires(source -> source.hasPermission(2)) // Require OP level 2
            .then(Commands.literal("update")
                .executes(context -> updateAllWires(context.getSource()))
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 1000))
                    .executes(context -> updateWiresInRadius(
                        context.getSource(), 
                        IntegerArgumentType.getInteger(context, "radius")
                    ))
                )
            );
        
        dispatcher.register(command);
    }
    
    private static int updateAllWires(CommandSourceStack source) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("This command can only be run on the server"));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("Updating all wire connections..."), true);
        
        GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(level);
        
        // Collect all connections from all local networks
        Set<Connection> allConnections = new HashSet<>();
        for (LocalWireNetwork localNet : globalNet.getLocalNets()) {
            for (var cp : localNet.getConnectionPoints()) {
                allConnections.addAll(localNet.getConnections(cp));
            }
        }
        
        int updatedCount = updateConnections(globalNet, allConnections);
        
        source.sendSuccess(() -> Component.literal("Updated " + updatedCount + " wire connections"), true);
        return updatedCount;
    }
    
    private static int updateWiresInRadius(CommandSourceStack source, int radius) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("This command can only be run on the server"));
            return 0;
        }
        
        Vec3 pos = source.getPosition();
        BlockPos centerPos = BlockPos.containing(pos);
        
        source.sendSuccess(() -> Component.literal("Updating wire connections within " + radius + " blocks..."), true);
        
        GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(level);
        Set<Connection> connectionsToUpdate = new HashSet<>();
        
        // Find all connections within radius
        for (LocalWireNetwork localNet : globalNet.getLocalNets()) {
            for (var cp : localNet.getConnectionPoints()) {
                for (Connection conn : localNet.getConnections(cp)) {
                    BlockPos endA = conn.getEndA().position();
                    BlockPos endB = conn.getEndB().position();
                    
                    // Check if either end is within radius
                    if (endA.closerThan(centerPos, radius) || endB.closerThan(centerPos, radius)) {
                        connectionsToUpdate.add(conn);
                    }
                }
            }
        }
        
        int updatedCount = updateConnections(globalNet, connectionsToUpdate);
        
        source.sendSuccess(() -> Component.literal("Updated " + updatedCount + " wire connections"), true);
        return updatedCount;
    }
    
    private static int updateConnections(GlobalWireNetwork globalNet, Collection<Connection> connections) {
        int count = 0;
        
        for (Connection conn : connections) {
            // Skip internal connections
            if (conn.isInternal()) {
                continue;
            }
            
            // Calculate new offsets based on current config
            Vec3 newOffsetA = WireUtils.getConnectionOffset(globalNet, conn.getEndA(), conn.getEndB(), conn.type);
            Vec3 newOffsetB = WireUtils.getConnectionOffset(globalNet, conn.getEndB(), conn.getEndA(), conn.type);
            
            // Reset the catenary data with new offsets
            conn.resetCatenaryData(newOffsetA, newOffsetB);
            count++;
        }
        
        // Network will be saved automatically as SavedData
        
        return count;
    }
}