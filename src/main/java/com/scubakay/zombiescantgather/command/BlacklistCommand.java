package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.scubakay.zombiescantgather.util.CommandPagination;
import com.scubakay.zombiescantgather.util.CommandUtil;
import de.maxhenkel.configbuilder.custom.StringList;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;

import java.util.stream.Stream;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;
import static com.scubakay.zombiescantgather.command.PermissionManager.*;
import static com.scubakay.zombiescantgather.command.RootCommand.ALIAS_COMMAND;

public class BlacklistCommand {
    public enum Blacklist {
        PIGLIN("piglin", "piglins"),
        ZOMBIE("zombie", "zombies");

        private final String name;
        private final String plural;

        Blacklist(final String name, final String plural) {
            this.name = name;
            this.plural = plural;
        }

        public String toString() { return name; }
        public String toPlural() { return plural; }
    }

    private static final String ITEM_ARGUMENT = "item";

    private static final String ADDED_REPLY = "%s§7 can't gather §f%s§7";
    private static final String REMOVED_REPLY = "%s§7 can gather §f%s§7 again";
    private static final String DUPLICATE_REPLY = "%s§7 is already on the §f%s§7 blacklist";
    private static final String NOT_FOUND_REPLY = "%s§7 not found in §f%s§7 blacklist";
    private static final String INVALID_ENTITY_REPLY = "%s blacklist is not supported";
    private static final String RESET_ITEMS_REPLY = "§7Reset §f%s§7 items";
    private static final String BLACKLIST_HEADER_REPLY = "\n%s§7 can't pick up §f%s§7 items:";
    private static final String BLACKLIST_ROW_REPLY = "- §f%s";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry, RegistrationEnvironment ignoredEnv) {
        RootCommand.getRoot(dispatcher).addChild(getBlacklistNode(registry, Blacklist.PIGLIN));
        RootCommand.getRoot(dispatcher).addChild(getBlacklistNode(registry, Blacklist.ZOMBIE));
    }

    private static LiteralCommandNode<ServerCommandSource> getBlacklistNode(CommandRegistryAccess registry, Blacklist blacklist) {
        LiteralCommandNode<ServerCommandSource> piglinNode = getEntityNode(blacklist); // zcg <entity>
        piglinNode.addChild(getAddNode(registry, blacklist)); // zcg <entity> add <item>
        piglinNode.addChild(getRemoveNode(registry, blacklist)); // zcg <entity> remove <item>
        piglinNode.addChild(getResetNode(blacklist)); // zcg <entity> reset
        return piglinNode;
    }

    public static int add(CommandContext<ServerCommandSource> context, Blacklist type, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            switch(type) {
                case PIGLIN:
                    MOD_CONFIG.addPiglinItem(item);
                    CommandUtil.reply(context, ADDED_REPLY, type.toPlural(), item);
                    break;
                case ZOMBIE:
                    MOD_CONFIG.addZombieItem(item);
                    CommandUtil.reply(context, ADDED_REPLY, type.toPlural(), item);
                    break;
            }
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, DUPLICATE_REPLY, item, type);
            return 0;
        }
    }

    public static int remove(CommandContext<ServerCommandSource> context, Blacklist type, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            switch(type) {
                case Blacklist.PIGLIN:
                    MOD_CONFIG.removePiglinItem(item);
                    CommandUtil.reply(context, REMOVED_REPLY, type.toPlural(), item);
                    break;
                case Blacklist.ZOMBIE:
                    MOD_CONFIG.removeZombieItem(item);
                    CommandUtil.reply(context, REMOVED_REPLY, type.toPlural(), item);
                    break;
            }
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, NOT_FOUND_REPLY, item, type);
            return 0;
        }
    }

    public static int list(CommandContext<ServerCommandSource> context, Blacklist type) {
        StringList items = switch (type) {
            case Blacklist.PIGLIN -> MOD_CONFIG.piglinsCantGather.get();
            case Blacklist.ZOMBIE -> MOD_CONFIG.zombiesCantGather.get();
        };
        if (items != null) {
            CommandPagination pagination = new CommandPagination(context, items.stream().toList().size(), 2);
            CommandUtil.reply(context, BLACKLIST_HEADER_REPLY, type.toPlural(), pagination.elementCount);
            items.subList(pagination.fromIndex, pagination.toIndex).forEach((item) ->
                    CommandUtil.reply(context, BLACKLIST_ROW_REPLY, item));
            CommandUtil.reply(context, pagination.getPagination(String.format("/%s %s", ALIAS_COMMAND, type), String.format("No items on %s blacklist", type)));
            return Command.SINGLE_SUCCESS;
        } else {
            CommandUtil.reply(context, INVALID_ENTITY_REPLY, type);
            return 0;
        }
    }

    public static int reset(CommandContext<ServerCommandSource> context, Blacklist type) {
        switch (type) {
            case Blacklist.PIGLIN -> MOD_CONFIG.resetPiglinItems();
            case Blacklist.ZOMBIE -> MOD_CONFIG.resetZombieItems();
        }
        CommandUtil.reply(context, RESET_ITEMS_REPLY, type);
        return Command.SINGLE_SUCCESS;
    }

    private static LiteralCommandNode<ServerCommandSource> getEntityNode(Blacklist entity) {
        return CommandManager
                .literal(entity.toString())
                .requires(ctx -> hasPermission(ctx, BLACKLIST_PERMISSION))
                .executes(ctx -> list(ctx, entity))
                .then(CommandPagination.getPageCommand(ctx -> list(ctx, entity)))
                .build();
    }

    public static LiteralCommandNode<ServerCommandSource> getAddNode(CommandRegistryAccess registry, Blacklist entityType) {
        LiteralCommandNode<ServerCommandSource> addNode = CommandManager
                .literal("add")
                .requires(ctx -> PermissionManager.hasPermission(ctx, BLACKLIST_ADD_PERMISSION))
                .build();
        ArgumentCommandNode<ServerCommandSource, ItemStackArgument> itemNode = CommandManager
                .argument(ITEM_ARGUMENT, ItemStackArgumentType.itemStack(registry))
                .requires(ctx -> hasPermission(ctx, BLACKLIST_ADD_PERMISSION))
                .suggests((context, builder) -> {
                    Stream<Item> stream = Registries.ITEM.stream();
                    switch (entityType) {
                        case Blacklist.PIGLIN:
                            stream.filter(x -> !MOD_CONFIG.piglinsCantGather.get().contains(x.asItem().toString()))
                                    .forEach(x -> builder.suggest(x.asItem().toString()));
                            break;
                        case Blacklist.ZOMBIE:
                            stream.filter(x -> !MOD_CONFIG.zombiesCantGather.get().contains(x.asItem().toString()))
                                    .forEach(x -> builder.suggest(x.asItem().toString()));
                            break;
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> add(ctx, entityType, ItemStackArgumentType.getItemStackArgument(ctx, ITEM_ARGUMENT)))
                .build();
        addNode.addChild(itemNode);
        return addNode;
    }

    public static LiteralCommandNode<ServerCommandSource> getRemoveNode(CommandRegistryAccess registry, Blacklist entityType) {
        LiteralCommandNode<ServerCommandSource> removeNode = CommandManager
                .literal("remove")
                .requires(ctx -> PermissionManager.hasPermission(ctx, BLACKLIST_ADD_PERMISSION))
                .build();
        ArgumentCommandNode<ServerCommandSource, ItemStackArgument> itemNode = CommandManager
                .argument(ITEM_ARGUMENT, ItemStackArgumentType.itemStack(registry))
                .requires(ctx -> hasPermission(ctx, BLACKLIST_REMOVE_PERMISSION))
                .suggests((context, builder) -> {
                    switch (entityType) {
                        case Blacklist.PIGLIN:
                            for (String s : MOD_CONFIG.piglinsCantGather.get()) builder.suggest(s);
                            break;
                        case Blacklist.ZOMBIE:
                            for (String s : MOD_CONFIG.zombiesCantGather.get()) builder.suggest(s);
                            break;
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> remove(ctx, entityType, ItemStackArgumentType.getItemStackArgument(ctx, ITEM_ARGUMENT)))
                .build();
        removeNode.addChild(itemNode);
        return removeNode;
    }

    public static LiteralCommandNode<ServerCommandSource> getResetNode(Blacklist entityType) {
        return CommandManager
                .literal("reset")
                .requires(ctx -> hasPermission(ctx, BLACKLIST_RESET_PERMISSION))
                .executes(ctx -> reset(ctx, entityType))
                .build();
    }
}
