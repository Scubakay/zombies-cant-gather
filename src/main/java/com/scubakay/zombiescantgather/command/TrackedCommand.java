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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.Comparator;
import java.util.List;

import static com.scubakay.zombiescantgather.state.EntityTracker.DIMENSION;
import static com.scubakay.zombiescantgather.state.EntityTracker.TRACKED_COUNT;

public class TrackedCommand extends RootCommand {
    public static void register(CommandDispatcher<ServerCommandSource> ignoredDispatcher, CommandRegistryAccess ignoredRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        LiteralCommandNode<ServerCommandSource> tracked = CommandManager
                .literal("tracked")
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
        EntityTracker tracker = EntityTracker.getWorldState(context);
        int size = tracker.getTrackedEntities().size();
        CommandUtil.reply(context, Text.literal(String.format("\n§7Tracked §f%s§7 entities with blacklisted items:", size)));
        List<NbtCompound> trackedEntities = tracker.getTrackedEntities().values().stream().sorted(Comparator.comparingInt(x -> -x.getInt(TRACKED_COUNT))).toList();

        final int PAGE_SIZE = 10;
        int pages = size / PAGE_SIZE + 1;
        int startItem = Integer.min(pages - 1, page - 1) * PAGE_SIZE;
        int endItem = Integer.min(size, startItem + PAGE_SIZE);

        for (int i = startItem; i < endItem; i++) {
            NbtCompound entity = trackedEntities.get(i);
            NbtCompound firstHandItem = (NbtCompound) entity.getList("HandItems", NbtElement.COMPOUND_TYPE).getFirst();
            NbtList positionList = entity.getList("Pos", NbtElement.DOUBLE_TYPE);
            Vec3d pos = new Vec3d(positionList.getDouble(0), positionList.getDouble(1), positionList.getDouble(2));
            int loadedCount = entity.getInt(TRACKED_COUNT);
            String dimension = entity.getString(DIMENSION);
            final int dimensionColor = getDimensionColor(dimension);
            String tpCommand = String.format("/tp @s %s", entity.getUuid("UUID").toString());

            final Text tpButton = getTpButton(context, dimensionColor, tpCommand);
            final Text toolTip = getTrackerRowToolTip(entity, dimension, dimensionColor, pos);
            final Text trackerRow = getTrackerRow(toolTip, entity, loadedCount, firstHandItem);
            CommandUtil.reply(context, tpButton.copy().append(trackerRow));
        }

        CommandUtil.reply(context, getNavigationText(page, pages));

        return Command.SINGLE_SUCCESS;
    }

    private static int getDimensionColor(String dimension) {
        int dimensionColor;
        if (dimension.equals(DimensionTypes.OVERWORLD_ID.toString())) {
            dimensionColor = Colors.BLUE;
        } else if (dimension.equals(DimensionTypes.THE_NETHER_ID.toString())) {
            dimensionColor = Colors.RED;
        } else {
            dimensionColor = Colors.YELLOW;
        }
        return dimensionColor;
    }

    private static Text getTpButton(CommandContext<ServerCommandSource> context, int dimensionColor, String tpCommand) {
        Text tpButton = Text.empty();
        if (PermissionManager.hasPermission(context.getSource(), PermissionManager.TELEPORT_PERMISSION)) {
            tpButton = Text.literal("[tp] ")
                    .styled(style -> style
                            .withColor(dimensionColor)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpCommand))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(tpCommand)))
                    );
        }
        return tpButton;
    }

    private static Text getTrackerRowToolTip(NbtCompound entity, String dimension, int dimensionColor, Vec3d pos) {
        return Text.literal(entity.getString("CustomName"))
                .styled(style -> style.withBold(true))
                .append(Text.literal("\nDimension: ")
                        .styled((Style style) -> style
                                .withColor(Colors.WHITE)
                                .withBold(false)))
                .append(Text.literal(dimension)
                        .styled(style -> style
                                .withColor(dimensionColor)))
                .append(Text.literal("\nLocation: ")
                        .styled(style -> style
                                .withBold(false)))
                .append(Text.literal(pos.toString()));
    }

    private static Text getTrackerRow(Text hoverMessage, NbtCompound entity, int loadedCount, NbtCompound firstHandItem) {
        return Text
                .literal("Loaded ") // Loaded
                .styled(style -> style
                        .withColor(Colors.GRAY)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage)))
                .append(Text.literal(entity.getString("CustomName")) // <name>
                        .styled(style -> style
                                .withColor(Colors.WHITE)))
                .append(Text.literal(" ")
                        .styled(style -> style
                                .withColor(Colors.GRAY)))
                .append(Text.literal(String.valueOf(loadedCount))) // <loadedCount>
                .append(Text.literal(" time(s) holding ") // time(s) holding
                        .styled(style -> style
                                .withColor(Colors.GRAY)))
                .append(Text.literal(firstHandItem.getString("id"))
                        .styled(style -> style
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

    public static int reset(CommandContext<ServerCommandSource> context) {
        EntityTracker.getWorldState(context).clear();
        return Command.SINGLE_SUCCESS;
    }
}
