package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Vec3d;

import static com.scubakay.zombiescantgather.state.EntityTracker.TRACKED_COUNT;

public class TrackedCommand extends RootCommand {
    public static void register(CommandDispatcher<ServerCommandSource> ignoredDispatcher, CommandRegistryAccess ignoredRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        LiteralCommandNode<ServerCommandSource> tracked = CommandManager
                .literal("tracked")
                .build();
        RootCommand.getRoot().addChild(tracked);

        LiteralCommandNode<ServerCommandSource> list = CommandManager
                .literal("list")
                .executes(TrackedCommand::list)
                .build();
        tracked.addChild(list);

        LiteralCommandNode<ServerCommandSource> reset = CommandManager
                .literal("reset")
                .executes(TrackedCommand::reset)
                .build();
        tracked.addChild(reset);
    }

    public static int list(CommandContext<ServerCommandSource> context) {
        EntityTracker tracker = EntityTracker.getWorldState(context.getSource().getWorld());
        int size = tracker.getTrackedEntities().size();
        CommandUtil.reply(context, Text.literal(String.format("§7Tracked §f%s§7 entities with blacklisted items:", size)));
        tracker.getTrackedEntities().forEach((key, value) -> {
            NbtCompound firstHandItem = (NbtCompound) value.getList("HandItems", NbtElement.COMPOUND_TYPE).getFirst();
            NbtList positionList = value.getList("Pos", NbtElement.DOUBLE_TYPE);
            Vec3d pos = new Vec3d(positionList.getDouble(0), positionList.getDouble(1), positionList.getDouble(2));
            int loadedCount = value.getInt(TRACKED_COUNT);

            String tpCommand = String.format("/tp @s %.1f %.1f %.1f", pos.x, pos.y + 1, pos.z);

            Text tpButton = Text.empty();
            if (PermissionManager.hasPermission(context.getSource(), PermissionManager.TELEPORT_PERMISSION)) {
                tpButton = Text.literal("[tp] ").styled(style -> style.withColor(Colors.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpCommand))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(tpCommand))));
            }

            Text message = tpButton.copy().append(Text.literal(String.format(
                    "§7Loaded §f%s§7 §f%s§7 time(s) holding §f%s§7",
                    value.getString("CustomName"),
                    loadedCount,
                    firstHandItem.getString("id")
            )));
            CommandUtil.reply(context, message);
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int reset(CommandContext<ServerCommandSource> context) {
        EntityTracker.getWorldState(context.getSource().getWorld()).clear();
        return Command.SINGLE_SUCCESS;
    }
}
