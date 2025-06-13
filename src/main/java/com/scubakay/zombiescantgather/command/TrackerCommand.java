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

import static com.scubakay.zombiescantgather.command.PermissionManager.*;

public class TrackerCommand extends RootCommand {
    public static String TRACKER_COMMAND = "tracker";
    public static String TRACKER_RESET_COMMAND = "reset";
    public static String TRACKER_TELEPORT_COMMAND = "teleport";

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

    public static int list(CommandContext<ServerCommandSource> context, int current_page) {
        List<TrackedEntity> tracker = EntityTracker
                .getServerState(context.getSource().getServer())
                .getTrackedEntities()
                .values()
                .stream()
                .sorted(Comparator.comparingInt(x -> -x.count))
                .toList();

        final int PAGE_SIZE = 10;
        int pages = tracker.size() / PAGE_SIZE;
        int startItem = Integer.min(pages, current_page-1) * PAGE_SIZE;
        int endItem = Integer.min(tracker.size(), startItem + PAGE_SIZE);

        CommandUtil.reply(context, Text.literal(String.format("\n§7Tracked §f%s§7 entities with blacklisted items:", tracker.size())));

        for (int i = startItem; i < endItem; i++) {
            TrackedEntity entity = tracker.get(i);
            final Text tpButton = getTpButton(context, entity);
            final Text trackerRow = getTrackerRow(entity);
            CommandUtil.reply(context, tpButton.copy().append(trackerRow));
        }

        CommandUtil.reply(context, getNavigationText(current_page, pages));

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

    private static Text getTpButton(CommandContext<ServerCommandSource> context, TrackedEntity entity) {
        if (!hasPermission(context.getSource(), PermissionManager.TRACKER_TELEPORT_PERMISSION)) {
            return Text.empty();
        }
        return Text.literal("[TP] ")
                .styled(style -> style
                        .withColor(entity.getDimensionColor())
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getTpCommand(entity)))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTpButtonToolTIp(entity))));
    }

    private static Text getTpButtonToolTIp(TrackedEntity entity) {
        return Text.literal(getTpButtonTitle(entity))
                .styled(style -> style
                        .withColor(entity.getDimensionColor())
                        .withFormatting(Formatting.BOLD))
                .append(Text.literal("Runs: ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(getTpCommand(entity))
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)
                                .withFormatting(Formatting.ITALIC)
                                .withBold(false)));
    }

    private static Text getTrackerRow(TrackedEntity entity) {
        return Text.literal(String.format("(%dx) ", entity.count))
                .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getTpCommand(entity)))
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
                                .withFormatting(Formatting.WHITE)));
    }

    private static Text getTrackerRowToolTip(TrackedEntity entity) {
        return Text.literal(entity.name)
                .styled(style -> style
                        .withFormatting(Formatting.BOLD)
                        .withColor(entity.getDimensionColor()))
                .append(Text.literal("\nDimension: ")
                        .styled((Style style) -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(entity.dimension)
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)))
                .append(Text.literal("\nLocation: ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(entity.pos.toShortString())
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)
                                .withBold(false)));
    }

    private static Text getNavigationText(int current_page, int last_page) {
        int previous_page = current_page - 1;
        int next_page = current_page + 1;
        Text navigation = Text.empty();
        if (current_page > 2) {
            navigation = navigation.copy().append(Text.literal("<< ").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(0)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("First page")))));
        }
        if (current_page > 1) {
            navigation = navigation.copy().append(Text.literal("< ").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(previous_page)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Previous page")))));
        }
        navigation = navigation.copy().append(Text.literal(current_page + "/" + last_page));
        if (current_page < last_page) {
            navigation = navigation.copy().append(Text.literal(" >").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(next_page)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Next page")))));
        }
        if (current_page+1 < last_page) {
            navigation = navigation.copy().append(Text.literal(" >>").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getPageCommand(last_page)))
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

    private static @NotNull String getTpButtonTitle(TrackedEntity entity) {
        return String.format("Teleport to %s in the %s\n", entity.name, entity.getDimensionName());
    }
}