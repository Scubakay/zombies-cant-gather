package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.scubakay.zombiescantgather.config.ModConfig;
import com.scubakay.zombiescantgather.state.EntityTracker;
import com.scubakay.zombiescantgather.state.TrackedEntity;
import com.scubakay.zombiescantgather.util.CommandButton;
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
    public static final String TRACKER_COMMAND = "tracker";
    public static final String TRACKER_RESET_COMMAND = "reset";
    public static final String TRACKER_TELEPORT_COMMAND = "teleport";

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
    }

    public static int list(CommandContext<ServerCommandSource> context) {
        if (!ModConfig.enableTracker) {
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

        CommandPagination.builder(context, tracker)
                .withHeader(parameters -> Text.literal(String.format("\n§7Tracked §f%s§7 entities with blacklisted items:", parameters.elementCount())))
                .withRows(TrackerCommand::getTrackerRow, List.of(
                        CommandButton.<TrackedEntity>run(item -> Text.literal("TP"))
                                .requires(player -> !hasPermission(player, PermissionManager.TRACKER_TELEPORT_PERMISSION))
                                .withToolTip(TrackerCommand::getTpButtonToolTip)
                                .withCommand(TrackerCommand::getTpCommand)
                                .withColor(TrackedEntity::getDimensionColor)
                                .withBrackets()))
                .withFooter(parameters -> Text.literal("No mobs with blacklisted items tracked yet"))
                .withRefreshButton()
                .display();
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

    public static MutableText getTrackerRow(TrackedEntity entity) {
        return Text.literal(String.format("(%dx) ", entity.getCount()))
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
                                        .withFormatting(Formatting.WHITE)));
    }

    private static Text getTrackerRowToolTip(TrackedEntity entity) {
        return Text.literal(entity.getName())
                .styled(style -> style
                        .withFormatting(Formatting.BOLD)
                        .withColor(entity.getDimensionColor())).append(getTooltipDescription(entity));
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
}
