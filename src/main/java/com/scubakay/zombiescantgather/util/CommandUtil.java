package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandUtil {
    public static void reply(CommandContext<ServerCommandSource> context, String reply) {
        context.getSource().sendFeedback(() -> Text.literal(reply), false);
    }

    public static void reply(CommandContext<ServerCommandSource> context, Text reply) {
        context.getSource().sendFeedback(() -> reply, false);
    }
}
