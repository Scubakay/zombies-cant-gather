package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.scubakay.zombiescantgather.command.PermissionManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.scubakay.zombiescantgather.command.PermissionManager.*;

public class CommandUtil {
    public static final String FANCY_MOD_NAME = "§a[Zombies Can't Gather] §7";

    public static void reply(CommandContext<ServerCommandSource> context, String reply) {
        CommandUtil.reply(context, reply, true);
    }

    public static void reply(CommandContext<ServerCommandSource> context, String reply, boolean modName) {
        String message = modName ? FANCY_MOD_NAME + reply : reply;
        context.getSource().sendFeedback(() -> Text.literal(message), false);
    }

    public static void reply(CommandContext<ServerCommandSource> context, Text reply) {
        context.getSource().sendFeedback(() -> reply, false);
    }

    public static LiteralCommandNode<ServerCommandSource> getAddNode(CommandRegistryAccess ignoredCommandRegistryAccess, final Command<ServerCommandSource> command) {
        LiteralCommandNode<ServerCommandSource> addNode = CommandManager
                .literal("add")
                .requires(ctx -> PermissionManager.hasPermission(ctx, ADD_PERMISSION))
                .build();
        ArgumentCommandNode<ServerCommandSource, ItemStackArgument> itemNode = CommandManager
                .argument("item", ItemStackArgumentType.itemStack(ignoredCommandRegistryAccess))
                .executes(command)
                .build();
        addNode.addChild(itemNode);
        return addNode;
    }

    public static LiteralCommandNode<ServerCommandSource> getRemoveNode(CommandRegistryAccess ignoredCommandRegistryAccess, SuggestionProvider<ServerCommandSource> suggestions, final Command<ServerCommandSource> command) {
        LiteralCommandNode<ServerCommandSource> removeNode = CommandManager
                .literal("remove")
                .requires(ctx -> hasPermission(ctx, REMOVE_PERMISSION))
                .build();
        ArgumentCommandNode<ServerCommandSource, ItemStackArgument> itemNode = CommandManager
                .argument("listedItem", ItemStackArgumentType.itemStack(ignoredCommandRegistryAccess))
                .suggests(suggestions)
                .executes(command)
                .build();
        removeNode.addChild(itemNode);
        return removeNode;
    }

    public static LiteralCommandNode<ServerCommandSource> getListNode(final Command<ServerCommandSource> command) {
        return CommandManager
                .literal("list")
                .requires(ctx -> hasPermission(ctx, LIST_PERMISSION))
                .executes(command)
                .build();
    }

    public static LiteralCommandNode<ServerCommandSource> getResetNode(final Command<ServerCommandSource> command) {
        return CommandManager
                .literal("reset")
                .requires(ctx -> hasPermission(ctx, RESET_PERMISSION))
                .executes(command)
                .build();
    }
}
