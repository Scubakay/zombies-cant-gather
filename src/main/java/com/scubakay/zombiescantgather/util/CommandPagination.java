package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

public class CommandPagination {
    public static final String PAGE_COMMAND = "page";

    public static LiteralCommandNode<ServerCommandSource> getPageCommand(Command<ServerCommandSource> command, Predicate<ServerCommandSource> requirement) {
        return CommandManager
                .literal(PAGE_COMMAND)
                .then(CommandManager
                        .argument("page", IntegerArgumentType.integer(1))
                        .requires(requirement)
                        .executes(command))
                .build();
    }
}
