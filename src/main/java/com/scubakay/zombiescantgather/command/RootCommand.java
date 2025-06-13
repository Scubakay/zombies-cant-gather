package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RootCommand {
    public static String ROOT_COMMAND = "zombiescantgather";
    private static LiteralCommandNode<ServerCommandSource> root;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess ignoredCommandRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        dispatcher.getRoot().addChild(getRoot());
    }

    protected static LiteralCommandNode<ServerCommandSource> getRoot() {
        if (root == null) {
            root = CommandManager
                    .literal(ROOT_COMMAND)
                    .build();
        }
        return root;
    }
}
