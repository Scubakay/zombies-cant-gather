package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.scubakay.zombiescantgather.state.EntityTracker;
import com.scubakay.zombiescantgather.state.TrackedEntity;
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
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.scubakay.zombiescantgather.command.PermissionManager.*;

@SuppressWarnings("SameReturnValue")
public class TrackerCommand extends RootCommand {
    public static final String TRACKER_COMMAND = "tracker";
    public static final String TRACKER_RESET_COMMAND = "reset";
    public static final String TRACKER_TELEPORT_COMMAND = "teleport";

    public static void register(CommandDispatcher<ServerCommandSource> ignoredDispatcher, CommandRegistryAccess ignoredRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        LiteralCommandNode<ServerCommandSource> tracker = CommandManager
                .literal(TRACKER_COMMAND)
                .requires(ctx -> hasPermission(ctx, TRACKER_PERMISSION))
                .executes(ctx -> list(ctx, 1))
                .then(CommandManager
                        .argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> list(ctx, IntegerArgumentType.getInteger(ctx, "page"))))
                .build();
        RootCommand.getRoot().addChild(tracker);

        LiteralCommandNode<ServerCommandSource> reset = CommandManager
                .literal(TRACKER_RESET_COMMAND)
                .requires(ctx -> hasPermission(ctx, TRACKER_RESET_PERMISSION))
                .executes(TrackerCommand::reset)
                .build();
        tracker.addChild(reset);

        LiteralCommandNode<ServerCommandSource> teleport = CommandManager
                .literal(TRACKER_TELEPORT_COMMAND)
                .requires(ctx -> hasPermission(ctx, TRACKER_TELEPORT_PERMISSION))
                .then(CommandManager
                        .argument("uuid", UuidArgumentType.uuid())
                        .executes(ctx -> teleport(ctx, UuidArgumentType.getUuid(ctx, "uuid"))))
                .build();
        tracker.addChild(teleport);

    }

    public static int list(CommandContext<ServerCommandSource> context, int currentPage) {
        List<TrackedEntity> tracker = EntityTracker
                .getServerState(context.getSource().getServer())
                .getTrackedEntities()
                .values()
                .stream()
                .sorted(Comparator.comparingInt(x -> -x.count))
                .toList();

        CommandUtil.reply(context, Text.literal(String.format("\n§7Tracked §f%s§7 entities with blacklisted items:", tracker.size())));

        final PaginationParameters pagination = getPagination(tracker, currentPage);
        for (int i = pagination.startItem; i < pagination.endItem; i++) {
            CommandUtil.reply(context, getTrackerRow(context.getSource(), tracker.get(i)));
        }

        CommandUtil.reply(context, getPaginationText(pagination));

        return Command.SINGLE_SUCCESS;
    }

    public static int reset(CommandContext<ServerCommandSource> context) {
        EntityTracker.getServerState(context.getSource().getServer()).clear();
        return Command.SINGLE_SUCCESS;
    }

    public static int teleport(CommandContext<ServerCommandSource> context, UUID uuid) {
        if (context.getSource().isExecutedByPlayer()) {
            // Get entity pos and world
            HashMap<UUID, TrackedEntity> tracker = EntityTracker.getServerState(context.getSource().getServer()).getTrackedEntities();
            TrackedEntity entity = tracker.get(uuid);
            if (entity == null) {
                CommandUtil.reply(context, Text.literal("Can't teleport: entity removed from tracker").withColor(Colors.RED));
                return 0;
            }
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(entity.dimension));
            ServerWorld world = context.getSource().getServer().getWorld(key);

            // Get player and teleport
            ServerPlayerEntity player = context.getSource().getPlayer();
            assert player != null;
            TeleportTarget target = new TeleportTarget(world, entity.pos.toBottomCenterPos(), Vec3d.ZERO, player.getYaw(), player.getPitch(), TeleportTarget.NO_OP);
            player.teleportTo(target);
        } else {
            CommandUtil.reply(context, Text.literal("Only players can run the teleport command"));
        }
        return Command.SINGLE_SUCCESS;
    }

    public static Text getTrackerRow(ServerCommandSource context, TrackedEntity entity) {
        return getTpButton(context, entity).copy()
                .append(Text.literal(String.format("(%dx) ", entity.count))
                        .styled(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "1"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTrackerRowToolTip(entity)))
                                .withFormatting(Formatting.GRAY))
                        .append(Text.literal(entity.name) // <name>
                                .styled(style -> style
                                        .withFormatting(Formatting.WHITE)))
                        .append(Text.literal(" is holding ")
                                .styled(style -> style
                                        .withFormatting(Formatting.GRAY)))
                        .append(Text.literal(entity.item)
                                .styled(style -> style
                                        .withFormatting(Formatting.WHITE))));
    }

    private static Text getTrackerRowToolTip(TrackedEntity entity) {
        return Text.literal(entity.name)
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
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getTpCommand(entity)))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTpButtonToolTIp(entity))));
    }

    private static Text getTpButtonToolTIp(TrackedEntity entity) {
        return Text.literal(entity.name)
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
                .append(Text.literal(entity.pos.toShortString())
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)
                                .withFormatting(Formatting.ITALIC)
                                .withBold(false)))
                .append(Text.literal("\nHolding: ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(entity.item)
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)
                                .withFormatting(Formatting.ITALIC)
                                .withBold(false)))
                .append(Text.literal("\nLoaded: ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(String.format("%d times", entity.count))
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)
                                .withFormatting(Formatting.ITALIC)
                                .withBold(false)));
    }

    private record PaginationParameters(
            int lastPage,
            int startItem,
            int endItem,
            int pageSize,
            int currentPage,
            int previousPage,
            int nextPage
    ) {}

    private static @NotNull PaginationParameters getPagination(List<TrackedEntity> tracker, int currentPage) {
        final int pageSize = 10;
        int pages = tracker.size() / pageSize;
        int startItem = Integer.min(pages, currentPage - 1) * pageSize;
        int endItem = Integer.min(tracker.size(), startItem + pageSize);
        int previousPage = currentPage - 1;
        int nextPage = currentPage + 1;
        return new PaginationParameters(pages, startItem, endItem, pageSize, currentPage, previousPage, nextPage);
    }

    private static Text getPaginationText(PaginationParameters pagination) {
        Text navigation = Text.empty();
        if (pagination.currentPage > 2) {
            navigation = navigation.copy().append(Text.literal("<< ").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(0)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("First page")))));
        }
        if (pagination.currentPage > 1) {
            navigation = navigation.copy().append(Text.literal("< ").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(pagination.previousPage)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Previous page")))));
        }
        navigation = navigation.copy().append(Text.literal(pagination.currentPage + "/" + pagination.lastPage));
        if (pagination.currentPage < pagination.lastPage) {
            navigation = navigation.copy().append(Text.literal(" >").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(pagination.nextPage)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Next page")))));
        }
        if (pagination.currentPage + 1 < pagination.lastPage) {
            navigation = navigation.copy().append(Text.literal(" >>").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(pagination.lastPage)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Last page")))));
        }
        return navigation;
    }

    private static @NotNull String getPageCommand(int page) {
        return String.format("/%s %s %s", ROOT_COMMAND, TRACKER_COMMAND, page);
    }

    private static @NotNull String getTpCommand(TrackedEntity entity) {
        return String.format("/%s %s %s %s", ROOT_COMMAND, TRACKER_COMMAND, TRACKER_TELEPORT_COMMAND, entity.uuid);
    }
}