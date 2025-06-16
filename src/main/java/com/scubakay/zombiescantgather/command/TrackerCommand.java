package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.scubakay.zombiescantgather.state.EntityTracker;
import com.scubakay.zombiescantgather.state.TrackedEntity;
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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;
import static com.scubakay.zombiescantgather.command.PermissionManager.*;
import static com.scubakay.zombiescantgather.command.PermissionManager.hasPermission;
import static com.scubakay.zombiescantgather.command.RootCommand.ROOT_COMMAND;

@SuppressWarnings("SameReturnValue")
public class TrackerCommand {
    public static final String TRACKER_COMMAND = "tracker";
    public static final String TRACKER_RESET_COMMAND = "reset";
    public static final String TRACKER_TELEPORT_COMMAND = "teleport";
    public static final String TRACKER_TOGGLE_COMMAND = "toggle";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess ignoredRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        LiteralCommandNode<ServerCommandSource> tracker = CommandManager
                .literal(TRACKER_COMMAND)
                .requires(ctx -> (MOD_CONFIG.enableTracker.get() || hasPermission(ctx, CONFIGURE_MOD_PERMISSION)) && hasPermission(ctx, TRACKER_PERMISSION))
                .executes(ctx -> list(ctx, 1))
                .build();
        RootCommand.getRoot(dispatcher).addChild(tracker);

        tracker.addChild(CommandPagination.getPageCommand(
                ctx -> list(ctx, IntegerArgumentType.getInteger(ctx, "page")),
                ctx -> MOD_CONFIG.enableTracker.get() && PermissionManager.hasPermission(ctx, TRACKER_PERMISSION)));

        LiteralCommandNode<ServerCommandSource> reset = CommandManager
                .literal(TRACKER_RESET_COMMAND)
                .requires(ctx -> MOD_CONFIG.enableTracker.get() && hasPermission(ctx, TRACKER_RESET_PERMISSION))
                .executes(TrackerCommand::reset)
                .build();
        tracker.addChild(reset);

        LiteralCommandNode<ServerCommandSource> teleport = CommandManager
                .literal(TRACKER_TELEPORT_COMMAND)
                .requires(ctx -> MOD_CONFIG.enableTracker.get() && hasPermission(ctx, TRACKER_TELEPORT_PERMISSION))
                .then(CommandManager
                        .argument("uuid", UuidArgumentType.uuid())
                        .requires(ctx -> MOD_CONFIG.enableTracker.get() && hasPermission(ctx, TRACKER_TELEPORT_PERMISSION))
                        .executes(ctx -> teleport(ctx, UuidArgumentType.getUuid(ctx, "uuid"))))
                .build();
        tracker.addChild(teleport);

        LiteralCommandNode<ServerCommandSource> toggle = CommandManager
                .literal(TRACKER_TOGGLE_COMMAND)
                .requires(ctx -> hasPermission(ctx, CONFIGURE_MOD_PERMISSION))
                .executes(TrackerCommand::toggle)
                .build();
        tracker.addChild(toggle);
    }

    private record PaginationParameters(
            int pageCount,
            int fromIndex,
            int toIndex,
            int pageSize,
            int currentPage
    ) {
    }

    private static @NotNull PaginationParameters getPagination(int trackerSize, int currentPage) {
        final int pageSize = 10;
        int pageCount = trackerSize > 0 ? (trackerSize - 1) / pageSize + 1 : 0;
        int startItem = Math.min(pageCount, currentPage - 1) * pageSize;
        int endItemPlusOne = Math.min(trackerSize, startItem + pageSize);
        return new PaginationParameters(pageCount, startItem, endItemPlusOne, pageSize, currentPage);
    }

    private static String getPageCommand(String command, int page) {
        return String.format("%s page %s", command, page);
    }

    private static Text getPaginationText(PaginationParameters pagination, String command) {
        if (pagination.pageCount == 0) {
            return Text.literal("No mobs with blacklisted items tracked yet")
                    .styled(style -> style
                            .withFormatting(Formatting.GREEN));
        } else if (pagination.pageCount == 1) {
            return Text.empty();
        }
        return Text.literal("<< ")
                .styled(style -> getPageLinkStyle(style, command, pagination.currentPage > 1, "First page", 1))
                .append(Text.literal("< ")
                        .styled(style -> getPageLinkStyle(style, command, pagination.currentPage > 1, "Previous page", pagination.currentPage - 1)))
                .append(Text.literal(pagination.currentPage + "/" + pagination.pageCount)
                        .styled(style -> style.withColor(Colors.WHITE)))
                .append(Text.literal(" >")
                        .styled(style -> getPageLinkStyle(style, command, pagination.currentPage < pagination.pageCount, "Next page", pagination.currentPage + 1)))
                .append(Text.literal(" >>")
                        .styled(style -> getPageLinkStyle(style, command, pagination.currentPage < pagination.pageCount, "Last page", pagination.pageCount)));
    }

    private static @NotNull Style getPageLinkStyle(Style style, String command, boolean clickable, String tooltip, int page) {
        style = style.withColor(clickable ? Colors.GREEN : Colors.GRAY);
        if (clickable) {
            //? >=1.21.5 {
            style = style.withClickEvent(new ClickEvent.RunCommand(getPageCommand(command, page)))
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal(tooltip)));
            //?} else {
                /*style = style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(command, 1)))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(tooltip)))))
                *///?}
        } else {
            //? >=1.21.5 {
            style = style.withClickEvent(null)
                    .withHoverEvent(null);
            //?} else {
                /*style = style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(command, 1)))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(tooltip)))))
                *///?}
        }
        return style;
    }

    // =============== PAGE STUFF ============== //

    public static int list(CommandContext<ServerCommandSource> context, int currentPage) {
        if (!MOD_CONFIG.enableTracker.get()) {
            CommandUtil.reply(context, Text.literal("Tracker is not enabled").withColor(Colors.RED));
            return 0;
        }

        List<TrackedEntity> tracker = EntityTracker
                .getServerState(context.getSource().getServer())
                .get()
                .values()
                .stream()
                .sorted(Comparator.comparingInt(x -> -x.getCount()))
                .toList();

        if (tracker.isEmpty()) {
            CommandUtil.reply(context, Text.literal("No entities holding blacklisted items have been tracked yet").withColor(Colors.GREEN));
            return Command.SINGLE_SUCCESS;
        }

        CommandUtil.reply(context, Text.literal(String.format("\n§7Tracked §f%s§7 entities with blacklisted items:", tracker.size())));

        final PaginationParameters pagination = getPagination(tracker.size(), currentPage);
        tracker.subList(pagination.fromIndex, pagination.toIndex)
                .forEach(entity -> CommandUtil.reply(context, getTrackerRow(context.getSource(), entity)));

        final String command = String.format("/%s %s", ROOT_COMMAND, TRACKER_COMMAND);
        CommandUtil.reply(context, getPaginationText(pagination, command));

        return Command.SINGLE_SUCCESS;
    }

    public static int reset(CommandContext<ServerCommandSource> context) {
        EntityTracker.getServerState(context.getSource().getServer()).clear();
        return Command.SINGLE_SUCCESS;
    }

    public static int teleport(CommandContext<ServerCommandSource> context, UUID uuid) {
        if (context.getSource().isExecutedByPlayer()) {
            // Get entity pos and world
            TrackedEntity entity = EntityTracker.getServerState(context.getSource().getServer()).get(uuid);
            if (entity == null) {
                CommandUtil.reply(context, Text.literal("Can't teleport: entity removed from tracker").withColor(Colors.RED));
                return 0;
            }
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(entity.getDimension()));
            ServerWorld world = context.getSource().getServer().getWorld(key);

            // Get player and teleport
            ServerPlayerEntity player = context.getSource().getPlayer();
            assert player != null;
            TeleportTarget target = new TeleportTarget(world, entity.getPos().toBottomCenterPos(), Vec3d.ZERO, player.getYaw(), player.getPitch(), TeleportTarget.NO_OP);
            player.teleportTo(target);
        } else {
            CommandUtil.reply(context, Text.literal("Only players can run the teleport command"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int toggle(CommandContext<ServerCommandSource> context) {
        MOD_CONFIG.enableTracker.set(!MOD_CONFIG.enableTracker.get()).save();
        if (context.getSource().isExecutedByPlayer()) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            context.getSource().getServer().getPlayerManager().sendCommandTree(player);
        }

        if (MOD_CONFIG.enableTracker.get()) {
            CommandUtil.reply(context, "Tracker §aenabled");
        } else {
            CommandUtil.reply(context, "Tracker §cdisabled");
        }
        return Command.SINGLE_SUCCESS;
    }

    public static Text getTrackerRow(ServerCommandSource context, TrackedEntity entity) {
        return getTpButton(context, entity).copy()
                .append(Text.literal(String.format("(%dx) ", entity.getCount()))
                        .styled(style -> style
                                //? >=1.21.5 {
                                .withClickEvent(new ClickEvent.ChangePage(1))
                                .withHoverEvent(new HoverEvent.ShowText(getTrackerRowToolTip(entity)))
                                //?} else {
                                /*.withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTrackerRowToolTip(entity)))
                                *///?}
                                .withFormatting(Formatting.GRAY))
                        .append(Text.literal(entity.getName()) // <name>
                                .styled(style -> style
                                        .withFormatting(Formatting.WHITE)))
                        .append(Text.literal(" is holding ")
                                .styled(style -> style
                                        .withFormatting(Formatting.GRAY)))
                        .append(Text.literal(entity.getItem())
                                .styled(style -> style
                                        .withFormatting(Formatting.WHITE))));
    }

    private static Text getTrackerRowToolTip(TrackedEntity entity) {
        return Text.literal(entity.getName())
                .styled(style -> style
                        .withFormatting(Formatting.BOLD)
                        .withColor(entity.getDimensionColor())).append(getTooltipDescription(entity));
    }

    private static Text getTpButton(ServerCommandSource context, TrackedEntity entity) {
        if (!hasPermission(context, PermissionManager.TRACKER_TELEPORT_PERMISSION)) {
            return Text.empty();
        }
        return Text.literal("[TP] ")
                .styled(style -> style
                        .withColor(entity.getDimensionColor())
                        //? >=1.21.5 {
                        .withClickEvent(new ClickEvent.RunCommand(getTpCommand(entity)))
                        .withHoverEvent(new HoverEvent.ShowText(getTpButtonToolTIp(entity))));
        //?} else {
                        /*.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getTpCommand(entity)))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTpButtonToolTIp(entity))));
                        *///?}
    }

    private static Text getTpButtonToolTIp(TrackedEntity entity) {
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

    private static @NotNull String getTpCommand(TrackedEntity entity) {
        return String.format("/%s %s %s %s", ROOT_COMMAND, TRACKER_COMMAND, TRACKER_TELEPORT_COMMAND, entity.getUuid());
    }
}
