package com.scubakay.zombiescantgather.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandUtil {
    public static final String FANCY_MOD_NAME = "§a[Zombies Can't Gather] §7";

    public static String getIdentifierFromItemStackArgument(ItemStackArgument itemStackArgument) {
        return Registries.ITEM.getId(itemStackArgument.getItem()).toString();
    }

    public static void reply(CommandContext<ServerCommandSource> context, String reply) {
        context.getSource().sendFeedback(() -> Text.literal(reply), false);
    }
}
