package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static com.scubakay.zombiescantgather.command.PermissionManager.*;

public class RootCommand {
    private static LiteralCommandNode<ServerCommandSource> root;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess ignoredCommandRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        dispatcher.getRoot().addChild(getRoot());
    }

    protected static LiteralCommandNode<ServerCommandSource> getRoot() {
        if (root == null) {
            root = CommandManager
                    .literal("zombiescantgather")
                    .requires(ctx -> hasPermission(ctx, ROOT_PERMISSION))
                    .build();
        }
        return root;
    }

    protected static LiteralCommandNode<ServerCommandSource> getAdd(CommandRegistryAccess ignoredCommandRegistryAccess, final Command<ServerCommandSource> command) {
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

    protected static LiteralCommandNode<ServerCommandSource> getRemove(CommandRegistryAccess ignoredCommandRegistryAccess, SuggestionProvider<ServerCommandSource> suggestions, final Command<ServerCommandSource> command) {
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

    protected static LiteralCommandNode<ServerCommandSource> getList(final Command<ServerCommandSource> command) {
        return CommandManager
                .literal("list")
                .requires(ctx -> hasPermission(ctx, LIST_PERMISSION))
                .executes(command)
                .build();
    }

    protected static LiteralCommandNode<ServerCommandSource> getReset(final Command<ServerCommandSource> command) {
        return CommandManager
                .literal("reset")
                .requires(ctx -> hasPermission(ctx, RESET_PERMISSION))
                .executes(command)
                .build();
    }
}
