package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.scubakay.zombiescantgather.util.CommandUtil;
import de.maxhenkel.configbuilder.custom.StringList;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;
import static com.scubakay.zombiescantgather.command.PermissionManager.*;
import static com.scubakay.zombiescantgather.util.CommandUtil.FANCY_MOD_NAME;

public class PiglinsCantGatherCommand {
    private static final SuggestionProvider<ServerCommandSource> LISTED_ITEM_SUGGESTIONS = (context, builder) -> {
        for (String s : MOD_CONFIG.piglinsCantGather.get()) builder.suggest(s);
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess ignoredCommandRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        dispatcher.register(CommandManager
                .literal("zombiescantgather")
                .requires(ctx -> PermissionManager.hasPermission(ctx, ROOT_PERMISSION))
                .then(CommandManager
                        .literal("piglin")
                        .then(CommandManager
                                .literal("add")
                                .requires(ctx -> PermissionManager.hasPermission(ctx, ADD_PERMISSION))
                                .then(CommandManager
                                        .argument("item", ItemStackArgumentType.itemStack(ignoredCommandRegistryAccess))
                                        .executes(ctx -> add(ctx, ItemStackArgumentType.getItemStackArgument(ctx, "item")))
                                ))
                        .then(CommandManager
                                .literal("remove")
                                .requires(ctx -> PermissionManager.hasPermission(ctx, REMOVE_PERMISSION))
                                .then(CommandManager
                                        .argument("listedItem", ItemStackArgumentType.itemStack(ignoredCommandRegistryAccess))
                                        .suggests(LISTED_ITEM_SUGGESTIONS)
                                        .executes(ctx -> remove(ctx, ItemStackArgumentType.getItemStackArgument(ctx, "listedItem")))
                                ))
                        .then(CommandManager
                                .literal("list")
                                .requires(ctx -> PermissionManager.hasPermission(ctx, LIST_PERMISSION))
                                .executes(PiglinsCantGatherCommand::list)
                        )));
    }

    public static int add(CommandContext<ServerCommandSource> context, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            MOD_CONFIG.addPiglinItem(item);
            CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins can't gather §f" + item);
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins already can't gather §f" + item);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int remove(CommandContext<ServerCommandSource> context, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            MOD_CONFIG.removePiglinItem(item);
            CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins can gather §f" + item + "§7 again");
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins can already gather §f" + item);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int list(CommandContext<ServerCommandSource> context) {
        StringList piglinItems = MOD_CONFIG.piglinsCantGather.get();
        CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins can't pick up these items:");
        piglinItems.forEach((item) -> CommandUtil.reply(context, "§f" + item));
        return Command.SINGLE_SUCCESS;
    }
}
