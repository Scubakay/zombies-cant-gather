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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

import static com.scubakay.zombiescantgather.state.EntityTracker.DIMENSION;
import static com.scubakay.zombiescantgather.state.EntityTracker.TRACKED_COUNT;

public class TrackedCommand extends RootCommand {
    public static void register(CommandDispatcher<ServerCommandSource> ignoredDispatcher, CommandRegistryAccess ignoredRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        LiteralCommandNode<ServerCommandSource> tracked = CommandManager
                .literal("tracker")
                .build();
        RootCommand.getRoot().addChild(tracked);

        LiteralCommandNode<ServerCommandSource> list = CommandManager
                .literal("list")
                .executes(ctx -> list(ctx, 1))
                .then(CommandManager
                        .argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> list(ctx, IntegerArgumentType.getInteger(ctx, "page")))
                )
                .build();
        tracked.addChild(list);

        LiteralCommandNode<ServerCommandSource> reset = CommandManager
                .literal("reset")
                .executes(TrackedCommand::reset)
                .build();
        tracked.addChild(reset);
    }

    public static int list(CommandContext<ServerCommandSource> context, int page) {
        List<NbtCompound> tracker = EntityTracker
                .getServerState(context.getSource().getServer())
                .getTrackedEntities()
                .values()
                .stream()
                .sorted(Comparator.comparingInt(x -> -x.getInt(TRACKED_COUNT)))
                .toList();

        final int PAGE_SIZE = 10;
        int size = tracker.size();
        int pages = size / PAGE_SIZE + 1;
        int startItem = Integer.min(pages - 1, page - 1) * PAGE_SIZE;
        int endItem = Integer.min(size, startItem + PAGE_SIZE);

        CommandUtil.reply(context, Text.literal(String.format("\n§7Tracked §f%s§7 entities with blacklisted items:", size)));

        for (int i = startItem; i < endItem; i++) {
            NbtCompound entity = tracker.get(i);
            final Text tpButton = getTpButton(context, entity);
            final Text trackerRow = getTrackerRow(entity);
            CommandUtil.reply(context, tpButton.copy().append(trackerRow));
        }

        CommandUtil.reply(context, getNavigationText(page, pages));

        return Command.SINGLE_SUCCESS;
    }

    public static int reset(CommandContext<ServerCommandSource> context) {
        EntityTracker.getServerState(context.getSource().getServer()).clear();
        return Command.SINGLE_SUCCESS;
    }

    private static Text getTpButton(CommandContext<ServerCommandSource> context, NbtCompound entity) {
        if (!PermissionManager.hasPermission(context.getSource(), PermissionManager.TELEPORT_PERMISSION)) {
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
                        .withBold(true))
                .append(Text.literal(getTpCommand(entity))
                        .styled(style -> style
                                .withColor(Colors.WHITE)
                                .withBold(false)));
    }

    private static Text getTrackerRow(NbtCompound entity) {
        return Text
                .literal("Loaded ") // Loaded
                .styled(style -> style
                        .withColor(Colors.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getTpCommand(entity)))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTrackerRowToolTip(entity))))
                .append(Text.literal(entity.getString("CustomName")) // <name>
                        .styled(style -> style
                                .withColor(Colors.WHITE)))
                .append(Text.literal(" ")
                        .styled(style -> style
                                .withColor(Colors.GRAY)))
                .append(Text.literal(getLoadedCountString(entity))) // <loadedCount>
                .append(Text.literal(" time(s) holding ") // time(s) holding
                        .styled(style -> style
                                .withColor(Colors.GRAY)))
                .append(Text.literal(getFirstHandItemString(entity))
                        .styled(style -> style
                                .withColor(Colors.WHITE)));
    }

    private static Text getTrackerRowToolTip(NbtCompound entity) {
        return Text.literal(entity.getString("CustomName"))
                .styled(style -> style.withBold(true))
                .append(Text.literal("\nDimension: ")
                        .styled((Style style) -> style
                                .withColor(Colors.GRAY)
                                .withBold(false)))
                .append(Text.literal(getDimensionAsString(entity))
                        .styled(style -> style
                                .withColor(getDimensionColor(entity))))
                .append(Text.literal("\nLocation: ")
                        .styled(style -> style
                                .withBold(false)
                                .withColor(Colors.GRAY)))
                .append(Text.literal(getBlockPos(entity).toShortString())
                        .styled(style -> style
                                .withBold(false)
                                .withColor(Colors.WHITE)));
    }

    private static Text getNavigationText(int page, int pages) {
        Text navigation = Text.empty();
        if (page > 2) {
            navigation = navigation.copy().append(Text.literal("<< ").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zombiescantgather tracked list"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("First page")))));
        }
        if (page > 1) {
            navigation = navigation.copy().append(Text.literal("< ").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zombiescantgather tracked list " + (page - 1)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Previous page")))));
        }
        navigation = navigation.copy().append(Text.literal(page + "/" + pages));
        if (page < pages) {
            navigation = navigation.copy().append(Text.literal(" >").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zombiescantgather tracked list " + (page + 1)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Next page")))));
        }
        if (page < pages - 1) {
            navigation = navigation.copy().append(Text.literal(" >>").styled(style -> style.withColor(Colors.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zombiescantgather tracked list " + pages))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Last page")))));
        }
        return navigation;
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
            dimensionColor = Colors.BLUE;
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
        final String dimension = getDimensionAsString(entity);

        String title = "Teleport to entity ";
        if (dimension.equals(DimensionTypes.OVERWORLD_ID.toString())) {
            title += "the Overworld\n";
        } else if (dimension.equals(DimensionTypes.THE_NETHER_ID.toString())) {
            title += "the Nether\n";
        } else if (dimension.equals(DimensionTypes.THE_END_ID.toString())) {
            title += "the End\n";
        }
        return title;
    }
}