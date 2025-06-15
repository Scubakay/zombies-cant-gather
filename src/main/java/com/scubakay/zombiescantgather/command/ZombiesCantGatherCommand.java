package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.scubakay.zombiescantgather.util.CommandUtil;
import de.maxhenkel.configbuilder.custom.StringList;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;

public class ZombiesCantGatherCommand {
    private static final SuggestionProvider<ServerCommandSource> LISTED_ITEM_SUGGESTIONS = (context, builder) -> {
        for (String s : MOD_CONFIG.zombiesCantGather.get()) builder.suggest(s);
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> ignoredDispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        final LiteralCommandNode<ServerCommandSource> zombie = CommandUtil.getBlacklistNode("zombie");
        RootCommand.getRoot().addChild(zombie);

        zombie.addChild(CommandUtil.getAddNode(
                registryAccess,
                ctx -> add(ctx, ItemStackArgumentType.getItemStackArgument(ctx, "item"))
        ));

        zombie.addChild(CommandUtil.getRemoveNode(
                registryAccess,
                LISTED_ITEM_SUGGESTIONS,
                ctx -> remove(ctx, ItemStackArgumentType.getItemStackArgument(ctx, "listedItem"))
        ));

        zombie.addChild(CommandUtil.getListNode(ZombiesCantGatherCommand::list));
        zombie.addChild(CommandUtil.getResetNode(ZombiesCantGatherCommand::reset));
    }

    public static int add(CommandContext<ServerCommandSource> context, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            MOD_CONFIG.addZombieItem(item);
            CommandUtil.reply(context, "Zombies can't gather §f" + item);
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, "Zombies already can't gather §f" + item);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int remove(CommandContext<ServerCommandSource> context, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            MOD_CONFIG.removeZombieItem(item);
            CommandUtil.reply(context, "Zombies can gather §f" + item + "§7 again");
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, "Zombies can already gather §f" + item);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int list(CommandContext<ServerCommandSource> context) {
        StringList zombieItems = MOD_CONFIG.zombiesCantGather.get();
        CommandUtil.reply(context, "Zombies can't pick up these items:");
        zombieItems.forEach((item) -> CommandUtil.reply(context, "§f" + item, false));
        return Command.SINGLE_SUCCESS;
    }

    public static int reset(CommandContext<ServerCommandSource> context) {
        MOD_CONFIG.resetZombieItems();
        CommandUtil.reply(context, "Reset zombie items");
        return Command.SINGLE_SUCCESS;
    }
}
