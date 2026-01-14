package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;


public class Commands {
    public static final String ROOT_COMMAND = "zombiescantgather";
    public static final String ALIAS_COMMAND = "zcg";
    public static final String CONFIG_COMMAND = "config";
    private static CommandNode<ServerCommandSource> root;
    private static CommandNode<ServerCommandSource> config;

    public static CommandNode<ServerCommandSource> getRoot(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (root == null) {
            root = dispatcher.register(CommandManager.literal(ROOT_COMMAND));
            dispatcher.register(CommandManager.literal(ALIAS_COMMAND).redirect(root));
        }
        return root;
    }

    public static CommandNode<ServerCommandSource> getConfig(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (config == null) {
            config = CommandManager
                    .literal(CONFIG_COMMAND)
                    .requires(source -> PermissionManager.hasPermission(source, PermissionManager.CONFIGURE_MOD_PERMISSION))
                    .build();
            getRoot(dispatcher).addChild(config);
        }
        return config;
    }
}
