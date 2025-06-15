package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
}
