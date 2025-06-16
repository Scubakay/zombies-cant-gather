package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RootCommand {
    public static String ROOT_COMMAND = "zombiescantgather";
    public static String ALIAS_COMMAND = "zcg";
    private final LiteralCommandNode<ServerCommandSource> root;
    private final LiteralCommandNode<ServerCommandSource> alias;
    private static RootCommand command;

    public RootCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        root = CommandManager
                .literal(ROOT_COMMAND)
                .build();
        alias = CommandManager
                .literal(ALIAS_COMMAND)
                .build();
        dispatcher.getRoot().addChild(root);
        dispatcher.getRoot().addChild(alias);
    }

    public static RootCommand getRoot(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (command == null) {
            command = new RootCommand(dispatcher);
        }
        return command;
    }

    public CommandNode<ServerCommandSource> addChild(CommandNode<ServerCommandSource> node) {
        this.root.addChild(node);
        this.alias.addChild(node);
        return node;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess ignoredCommandRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        if (command == null) {
            command = new RootCommand(dispatcher);
        }
    }
}
