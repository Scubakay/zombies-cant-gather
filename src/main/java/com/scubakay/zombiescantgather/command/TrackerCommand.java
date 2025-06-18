package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.scubakay.zombiescantgather.config.ModConfig;
import com.scubakay.zombiescantgather.state.EntityTracker;
import com.scubakay.zombiescantgather.state.TrackedEntity;
import com.scubakay.zombiescantgather.util.CommandReply;
import com.scubakay.zombiescantgather.util.CommandPagination;
import com.scubakay.zombiescantgather.util.CommandUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.*;

import static com.scubakay.zombiescantgather.command.PermissionManager.*;
import static com.scubakay.zombiescantgather.command.RootCommand.ROOT_COMMAND;

@SuppressWarnings("SameReturnValue")
public class TrackerCommand {
    private static final String TRACKER_COMMAND = "tracker";
    private static final String TRACKER_RESET_COMMAND = "reset";
    private static final String TRACKER_TELEPORT_COMMAND = "teleport";
    private static final String TRACKER_PURGE_COMMAND = "purge";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess ignoredRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        CommandNode<ServerCommandSource> tracker = RootCommand.getRoot(dispatcher).addChild(CommandManager
                .literal(TRACKER_COMMAND)
                .requires(ctx -> ModConfig.enableTracker && hasPermission(ctx, TRACKER_PERMISSION))
                .executes(TrackerCommand::list)
                .build());

        tracker.addChild(CommandPagination
                .getPageCommand(TrackerCommand::list)
                .requires(ctx -> ModConfig.enableTracker && PermissionManager.hasPermission(ctx, TRACKER_PERMISSION))
                .build());

        tracker.addChild(CommandManager
                .literal(TRACKER_RESET_COMMAND)
                .requires(ctx -> ModConfig.enableTracker && hasPermission(ctx, TRACKER_RESET_PERMISSION))
                .executes(TrackerCommand::reset)
                .build());

        tracker.addChild(CommandManager
                .literal(TRACKER_TELEPORT_COMMAND)
                .requires(ctx -> ModConfig.enableTracker && hasPermission(ctx, TRACKER_TELEPORT_PERMISSION))
                .then(CommandManager
                        .argument("uuid", UuidArgumentType.uuid())
                        .requires(ctx -> ModConfig.enableTracker && hasPermission(ctx, TRACKER_TELEPORT_PERMISSION))
                        .executes(ctx -> teleport(ctx, UuidArgumentType.getUuid(ctx, "uuid"))))
                .build());

        tracker.addChild(CommandManager
                .literal(TRACKER_PURGE_COMMAND)
                .requires(ctx -> ModConfig.enableTracker && hasPermission(ctx, TRACKER_PURGE_PERMISSION))
                .executes(TrackerCommand::purge)
                .build());
    }

    //region Command Handlers

    private static int list(CommandContext<ServerCommandSource> ctx) {
        if (!ModConfig.enableTracker) {
            CommandUtil.send(ctx, Text.literal("Tracker is not enabled").withColor(Colors.RED));
            return 0;
        }

        List<TrackedEntity> tracker = EntityTracker.getServerState(ctx.getSource().getServer()).getList();
        CommandPagination.builder(ctx, tracker)
                .withHeader(parameters -> Text.literal(String.format("§7Tracked §f%s§7 entities with blacklisted items:", parameters.elementCount())))
                .withRows(TrackerCommand::getTrackerRow, List.of(getTpButton()))
                .withEmptyMessage(parameters -> Text.literal("No mobs with blacklisted items tracked yet"))
                .withRefreshButton()
                .display();
        return Command.SINGLE_SUCCESS;
    }

    private static int purge(CommandContext<ServerCommandSource> ctx) {
        EntityTracker tracker = EntityTracker.getServerState(ctx.getSource().getServer());
        int size = tracker.get().size();
        List<TrackedEntity> remaining = EntityTracker
                .getServerState(ctx.getSource().getServer())
                .purge(ctx);
        CommandPagination.builder(ctx, remaining)
                .withCommand(String.format("/%s %s", ROOT_COMMAND, TRACKER_COMMAND))
                .withHeader(parameters -> Text.literal(String.format("§7Killed §f%s§7 entities, %s left:", size - parameters.elementCount(), parameters.elementCount())))
                .withRows(TrackerCommand::getTrackerRow, List.of(getTpButton()))
                .withEmptyMessage(parameters -> Text.literal("No mobs with blacklisted items tracked yet"))
                .withRefreshButton()
                .display();
        return Command.SINGLE_SUCCESS;
    }

    private static int reset(CommandContext<ServerCommandSource> ctx) {
        EntityTracker.getServerState(ctx.getSource().getServer()).clear();
        return Command.SINGLE_SUCCESS;
    }

    private static int teleport(CommandContext<ServerCommandSource> ctx, UUID uuid) {
        if (ctx.getSource().isExecutedByPlayer()) {
            // Get entity pos and world
            TrackedEntity entity = EntityTracker.getServerState(ctx.getSource().getServer()).get(uuid);
            if (entity == null) {
                CommandUtil.send(ctx, Text.literal("Can't teleport: entity removed from tracker").withColor(Colors.RED));
                return 0;
            }
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(entity.getDimension()));
            ServerWorld world = ctx.getSource().getServer().getWorld(key);

            // Get player and teleport
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            assert player != null;
            TeleportTarget target = new TeleportTarget(world, entity.getPos().toBottomCenterPos(), Vec3d.ZERO, player.getYaw(), player.getPitch(), TeleportTarget.NO_OP);
            player.teleportTo(target);
        } else {
            CommandUtil.send(ctx, Text.literal("Only players can run the teleport command"));
        }
        return Command.SINGLE_SUCCESS;
    }

    //endregion

    //region Utiliy

    public static MutableText getTrackerRow(TrackedEntity entity) {
        return Text.literal(String.format("(%dx) ", entity.getCount()))
                .append(Text.literal(entity.getName()) // <name>
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)))
                .append(Text.literal(" is holding ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)))
                .append(Text.literal(entity.getItem())
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)))
                .styled(style -> CommandUtil.getTooltipStyle(style, getTrackerRowToolTip(entity)).withFormatting(Formatting.GRAY));
    }

    private static Text getTrackerRowToolTip(TrackedEntity entity) {
        return Text.literal(entity.getName())
                .styled(style -> style
                        .withFormatting(Formatting.BOLD)
                        .withColor(entity.getDimensionColor())).append(getTooltipDescription(entity));
    }

    private static CommandReply<TrackedEntity> getTpButton() {
        return CommandReply.<TrackedEntity>get(item -> Text.literal("TP"))
                .requires(player -> !hasPermission(player, PermissionManager.TRACKER_TELEPORT_PERMISSION))
                .withToolTip(TrackerCommand::getTpButtonToolTip)
                .withCommand(TrackerCommand::getTpCommand)
                .withColor(TrackedEntity::getDimensionColor)
                .withBrackets();
    }

    private static Text getTpButtonToolTip(TrackedEntity entity) {
        return Text.literal(entity.getName())
                .styled(style -> style
                        .withColor(entity.getDimensionColor())
                        .withFormatting(Formatting.BOLD)).append(getTooltipDescription(entity))
                .append(Text.literal("\nClick to teleport")
                        .styled(style -> style
                                .withFormatting(Formatting.YELLOW)
                                .withFormatting(Formatting.ITALIC)));

    }

    private static Text getTooltipDescription(TrackedEntity entity) {
        return Text.literal("\nDimension: ")
                .styled(style -> style
                        .withFormatting(Formatting.GRAY)
                        .withBold(false))
                .append(Text.literal(entity.getDimensionName())
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)
                                .withFormatting(Formatting.ITALIC)
                                .withBold(false)))
                .append(Text.literal("\nLocation: ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(entity.getPos().toShortString())
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)
                                .withFormatting(Formatting.ITALIC)
                                .withBold(false)))
                .append(Text.literal("\nHolding: ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(entity.getItem())
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)
                                .withFormatting(Formatting.ITALIC)
                                .withBold(false)))
                .append(Text.literal("\nLoaded: ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(String.format("%d times", entity.getCount()))
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)
                                .withFormatting(Formatting.ITALIC)
                                .withBold(false)));
    }

    private static String getTpCommand(TrackedEntity entity) {
        return String.format("/%s %s %s %s", ROOT_COMMAND, TRACKER_COMMAND, TRACKER_TELEPORT_COMMAND, entity.getUuid());
    }

    //endregion
}
