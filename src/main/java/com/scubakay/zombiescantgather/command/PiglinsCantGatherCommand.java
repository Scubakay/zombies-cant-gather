package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.scubakay.zombiescantgather.ZombiesCantGather;
import de.maxhenkel.configbuilder.custom.StringList;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PiglinsCantGatherCommand {
    private static final String MOD_NAME = "§a[Zombies Can't Gather] §7";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess ignoredCommandRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        dispatcher.register(CommandManager
                .literal("piglinscantgather")
                .requires(source -> source.hasPermissionLevel(4)) // Must be OP to execute
                .then(CommandManager
                        .literal("add")
                        .then(CommandManager
                                .argument("item", ItemStackArgumentType.itemStack(ignoredCommandRegistryAccess))
                                .executes(ctx -> add(ctx, ItemStackArgumentType.getItemStackArgument(ctx, "item")))
                        ))
                .then(CommandManager
                        .literal("remove")
                        .then(CommandManager
                                .argument("item", ItemStackArgumentType.itemStack(ignoredCommandRegistryAccess))
                                .executes(ctx -> remove(ctx, ItemStackArgumentType.getItemStackArgument(ctx, "item")))
                        ))
                .then(CommandManager
                        .literal("list")
                        .executes(PiglinsCantGatherCommand::list)
                ));
    }

    public static int add(CommandContext<ServerCommandSource> context, ItemStackArgument itemStackArgument) {
        String item = getIdentifierFromItemStackArgument(itemStackArgument);
        try {
            ZombiesCantGather.modConfig.addPiglinItem(item);
            context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "Added §f" + item), false);
            return 1;
        } catch (IllegalArgumentException ex) {
            context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "§f" + item + "§7 has already been added"), false);
            return 0;
        }
    }

    public static int remove(CommandContext<ServerCommandSource> context, ItemStackArgument itemStackArgument) {
        String item = getIdentifierFromItemStackArgument(itemStackArgument);
        try {
            ZombiesCantGather.modConfig.removePiglinItem(item);
            context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "Removed §f" + item), false);
            return 1;
        } catch (IllegalArgumentException ex) {
            context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "§f" + item + "§7 was not found"), false);
            return 0;
        }
    }

    private static int list(CommandContext<ServerCommandSource> context) {
        StringList items = ZombiesCantGather.modConfig.piglinsCantGather.get();
        context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "Piglins can't pick up these items:"), false);
        items.forEach((item) -> {
            context.getSource().sendFeedback(() -> Text.literal("§f" + item), false);
        });
        return 1;
    }

    private static String getIdentifierFromItemStackArgument(ItemStackArgument itemStackArgument) {
        return Registries.ITEM.getId(itemStackArgument.getItem()).toString();
    }
}
