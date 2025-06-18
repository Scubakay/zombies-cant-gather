package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import org.apache.commons.lang3.StringUtils;

public class CommandUtil {
    public static void reply(CommandContext<ServerCommandSource> context, String reply, Object... args) {
        String message = StringUtils.capitalize(String.format(reply, args));
        context.getSource().sendFeedback(() -> Text.literal(StringUtil.capitalize(message)), false);
    }

    public static void reply(CommandContext<ServerCommandSource> context, Text reply) {
        context.getSource().sendFeedback(() -> reply, false);
    }
}