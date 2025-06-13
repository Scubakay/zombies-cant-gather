package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.scubakay.zombiescantgather.state.EntityTracker;
import com.scubakay.zombiescantgather.util.CommandUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

import static com.scubakay.zombiescantgather.command.PermissionManager.*;
import static com.scubakay.zombiescantgather.state.EntityTracker.DIMENSION;
import static com.scubakay.zombiescantgather.state.EntityTracker.TRACKED_COUNT;

public class TrackedCommand extends RootCommand {
    public static String TRACKER_COMMAND = "tracker";
    public static String TRACKER_LIST_COMMAND = "list";
    public static String TRACKER_RESET_COMMAND = "reset";

    public static void register(CommandDispatcher<ServerCommandSource> ignoredDispatcher, CommandRegistryAccess ignoredRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        LiteralCommandNode<ServerCommandSource> tracked = CommandManager
                .literal(TRACKER_COMMAND)
                .requires(ctx -> hasPermission(ctx, TRACKER_PERMISSION))
                .build();
        RootCommand.getRoot().addChild(tracked);

        LiteralCommandNode<ServerCommandSource> list = CommandManager
                .literal(TRACKER_LIST_COMMAND)
                .requires(ctx -> hasPermission(ctx, TRACKER_LIST_PERMISSION))
                .executes(ctx -> list(ctx, 1))
                .then(CommandManager
                        .argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> list(ctx, IntegerArgumentType.getInteger(ctx, "page"))))
                .build();
        tracked.addChild(list);

        LiteralCommandNode<ServerCommandSource> reset = CommandManager
                .literal(TRACKER_RESET_COMMAND)
                .requires(ctx -> hasPermission(ctx, TRACKER_RESET_PERMISSION))
                .executes(TrackedCommand::reset)
                .build();
        tracked.addChild(reset);
    }

    public static int list(CommandContext<ServerCommandSource> context, int current_page) {
        List<NbtCompound> tracker = EntityTracker
                .getServerState(context.getSource().getServer())
                .getTrackedEntities()
                .values()
                .stream()
                .sorted(Comparator.comparingInt(x -> -x.getInt(TRACKED_COUNT)))
                .toList();

        final int PAGE_SIZE = 10;
        int pages = tracker.size() / PAGE_SIZE;
        int startItem = Integer.min(pages, current_page-1) * PAGE_SIZE;
        int endItem = Integer.min(tracker.size(), startItem + PAGE_SIZE);

        CommandUtil.reply(context, Text.literal(String.format("\n§7Tracked §f%s§7 entities with blacklisted items:", tracker.size())));

        for (int i = startItem; i < endItem; i++) {
            NbtCompound entity = tracker.get(i);
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

    private static Text getTpButton(CommandContext<ServerCommandSource> context, NbtCompound entity) {
        if (!hasPermission(context.getSource(), PermissionManager.TRACKER_TELEPORT_PERMISSION)) {
            return Text.empty();
        }
        return Text.literal("[TP] ")
                .styled(style -> style
                        .withColor(getDimensionColor(entity))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, getTpCommand(entity)))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTpButtonToolTIp(entity))));
    }

    private static Text getTpButtonToolTIp(NbtCompound entity) {
        return Text.literal(getTpButtonTitle(entity))
                .styled(style -> style
                        .withColor(getDimensionColor(entity))
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

    private static Text getTrackerRow(NbtCompound entity) {
        return Text.literal(String.format("(%sx) ", getLoadedCountString(entity)))
                .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getTpCommand(entity)))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTrackerRowToolTip(entity)))
                        .withFormatting(Formatting.GRAY))
                .append(Text.literal(getEntityName(entity)) // <name>
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)))
                .append(Text.literal(" is holding ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)))
                .append(Text.literal(getFirstHandItemString(entity))
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)));
    }

    private static Text getTrackerRowToolTip(NbtCompound entity) {
        return Text.literal(getEntityName(entity))
                .styled(style -> style
                        .withFormatting(Formatting.BOLD)
                        .withColor(getDimensionColor(entity)))
                .append(Text.literal("\nDimension: ")
                        .styled((Style style) -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(getDimensionName(entity))
                        .styled(style -> style
                                .withFormatting(Formatting.WHITE)))
                .append(Text.literal("\nLocation: ")
                        .styled(style -> style
                                .withFormatting(Formatting.GRAY)
                                .withBold(false)))
                .append(Text.literal(getBlockPos(entity).toShortString())
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
        return String.format("/%s %s %s %s", ROOT_COMMAND, TRACKER_COMMAND, TRACKER_LIST_COMMAND, page);
    }

    private static @NotNull String getTpCommand(NbtCompound entity) {
        final String dimension = getDimensionAsString(entity);
        final BlockPos pos = getBlockPos(entity);
        return String.format("/execute in %s run tp @s %d %d %d", dimension, pos.getX(), pos.getY(), pos.getZ());
    }

    private static String getDimensionAsString(NbtCompound entity) {
        return entity.getString(DIMENSION);
    }

    private static @NotNull BlockPos getBlockPos(NbtCompound entity) {
        NbtList positionList = entity.getList("Pos", NbtElement.DOUBLE_TYPE);
        return new BlockPos((int) positionList.getDouble(0), (int) positionList.getDouble(1), (int) positionList.getDouble(2));
    }

    private static int getDimensionColor(NbtCompound entity) {
        final String dimension = getDimensionAsString(entity);

        int dimensionColor = Colors.WHITE;
        if (dimension.equals(DimensionTypes.OVERWORLD_ID.toString())) {
            dimensionColor = Colors.GREEN;
        } else if (dimension.equals(DimensionTypes.THE_NETHER_ID.toString())) {
            dimensionColor = Colors.RED;
        } else if (dimension.equals(DimensionTypes.THE_END_ID.toString())) {
            dimensionColor = Colors.YELLOW;
        }
        return dimensionColor;
    }

    private static String getFirstHandItemString(NbtCompound entity) {
        return ((NbtCompound) entity.getList("HandItems", NbtElement.COMPOUND_TYPE).getFirst()).getString("id");
    }

    private static @NotNull String getLoadedCountString(NbtCompound entity) {
        return String.valueOf(entity.getInt(TRACKED_COUNT));
    }

    private static @NotNull String getTpButtonTitle(NbtCompound entity) {
        return String.format("Teleport to %s in the %s\n", getEntityName(entity), getDimensionName(entity));
    }

    private static String getDimensionName(NbtCompound entity) {
        final String dimension = getDimensionAsString(entity);
        if (dimension.equals(DimensionTypes.OVERWORLD_ID.toString())) {
            return "Overworld";
        } else if (dimension.equals(DimensionTypes.THE_NETHER_ID.toString())) {
            return "Nether";
        } else if (dimension.equals(DimensionTypes.THE_END_ID.toString())) {
            return "End";
        } else {
            return dimension;
        }
    }

    private static String getEntityName(NbtCompound entity) {
        return entity.getString("CustomName").replace("\"", "");
    }
}