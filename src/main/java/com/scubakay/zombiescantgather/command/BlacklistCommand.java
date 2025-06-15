package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.scubakay.zombiescantgather.util.CommandUtil;
import de.maxhenkel.configbuilder.custom.StringList;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;
import static com.scubakay.zombiescantgather.command.PermissionManager.*;

public class BlacklistCommand {
    private static final String TYPE_ARGUMENT = "type";
    private static final String ITEM_ARGUMENT = "item";
    private static final String ZOMBIE = "zombie";
    private static final String PIGLIN = "piglin";

    private static final String ADDED_REPLY = "%s§7 can't gather §f%s§7";
    private static final String REMOVED_REPLY = "%s§7 can gather §f%s§7 again";
    private static final String DUPLICATE_REPLY = "%s§7 is already on the §f%s§7 blacklist";
    private static final String NOT_FOUND_REPLY = "%s§7 not found in §f%s§7 blacklist";
    private static final String INVALID_ENTITY_REPLY = "%s blacklist is not supported";
    private static final String RESET_ITEMS_REPLY = "§7Reset §f%s§7 items";
    private static final String BLACKLIST_HEADER_REPLY = "%s§7 can't pick up these items:";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry, RegistrationEnvironment ignoredEnv) {
        ArgumentCommandNode<ServerCommandSource, String> entityNode = getEntityNode(); // zcg <entity>
        RootCommand.getRoot(dispatcher).addChild(entityNode);

        entityNode.addChild(getAddNode(registry)); // zcg <entity> add <item>
        entityNode.addChild(getRemoveNode(registry)); // zcg <entity> remove <item>
        entityNode.addChild(getResetNode()); // zcg <entity> reset
    }

    public static int add(CommandContext<ServerCommandSource> context, String type, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            switch(type) {
                case PIGLIN:
                    MOD_CONFIG.addPiglinItem(item);
                    CommandUtil.reply(context, ADDED_REPLY, type, item);
                    break;
                case ZOMBIE:
                    MOD_CONFIG.addZombieItem(item);
                    CommandUtil.reply(context, ADDED_REPLY, type, item);
                    break;
                default:
                    CommandUtil.reply(context, INVALID_ENTITY_REPLY, type);
                    break;
            }
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, DUPLICATE_REPLY, item, type);
            return 0;
        }
    }

    public static int remove(CommandContext<ServerCommandSource> context, String type, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            switch(type) {
                case PIGLIN:
                    MOD_CONFIG.removePiglinItem(item);
                    CommandUtil.reply(context, REMOVED_REPLY, type, item);
                    break;
                case ZOMBIE:
                    MOD_CONFIG.removeZombieItem(item);
                    CommandUtil.reply(context, REMOVED_REPLY, type, item);
                    break;
                default:
                    CommandUtil.reply(context, INVALID_ENTITY_REPLY, type);
                    break;
            }
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, NOT_FOUND_REPLY, item, type);
            return 0;
        }
    }

    public static int list(CommandContext<ServerCommandSource> context, String type) {
        StringList items = switch (type) {
            case PIGLIN -> MOD_CONFIG.zombiesCantGather.get();
            case ZOMBIE -> MOD_CONFIG.piglinsCantGather.get();
            default -> null;
        };
        if (items != null) {
            CommandUtil.reply(context, BLACKLIST_HEADER_REPLY, type);
            items.forEach((item) -> CommandUtil.reply(context, "- §f" + item));
            return Command.SINGLE_SUCCESS;
        } else {
            CommandUtil.reply(context, INVALID_ENTITY_REPLY, type);
            return 0;
        }
    }

    public static int reset(CommandContext<ServerCommandSource> context, String type) {
        switch (type) {
            case PIGLIN -> MOD_CONFIG.resetPiglinItems();
            case ZOMBIE -> MOD_CONFIG.resetZombieItems();
            default -> {
                CommandUtil.reply(context, INVALID_ENTITY_REPLY, type);
                return 0;
            }
        }
        CommandUtil.reply(context, RESET_ITEMS_REPLY, type);
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentCommandNode<ServerCommandSource, String> getEntityNode() {
        return CommandManager
                .argument(TYPE_ARGUMENT, StringArgumentType.word())
                .requires(ctx -> hasPermission(ctx, BLACKLIST_PERMISSION))
                .suggests((context, builder) -> {
                    builder.suggest(PIGLIN);
                    builder.suggest(ZOMBIE);
                    return builder.buildFuture();
                })
                .executes(ctx -> list(ctx, StringArgumentType.getString(ctx, TYPE_ARGUMENT)))
                .build();
    }

    public static LiteralCommandNode<ServerCommandSource> getAddNode(CommandRegistryAccess registry) {
        LiteralCommandNode<ServerCommandSource> addNode = CommandManager
                .literal("add")
                .requires(ctx -> PermissionManager.hasPermission(ctx, BLACKLIST_ADD_PERMISSION))
                .build();
        ArgumentCommandNode<ServerCommandSource, ItemStackArgument> itemNode = CommandManager
                .argument(ITEM_ARGUMENT, ItemStackArgumentType.itemStack(registry))
                .requires(ctx -> hasPermission(ctx, BLACKLIST_ADD_PERMISSION))
                .executes(ctx -> add(ctx, StringArgumentType.getString(ctx, TYPE_ARGUMENT), ItemStackArgumentType.getItemStackArgument(ctx, ITEM_ARGUMENT)))
                .build();
        addNode.addChild(itemNode);
        return addNode;
    }

    public static LiteralCommandNode<ServerCommandSource> getRemoveNode(CommandRegistryAccess registry) {
        LiteralCommandNode<ServerCommandSource> removeNode = CommandManager
                .literal("remove")
                .requires(ctx -> PermissionManager.hasPermission(ctx, BLACKLIST_ADD_PERMISSION))
                .build();
        ArgumentCommandNode<ServerCommandSource, ItemStackArgument> itemNode = CommandManager
                .argument(ITEM_ARGUMENT, ItemStackArgumentType.itemStack(registry))
                .requires(ctx -> hasPermission(ctx, BLACKLIST_REMOVE_PERMISSION))
                .suggests((context, builder) -> {
                    final String type = StringArgumentType.getString(context, TYPE_ARGUMENT);
                    switch (type) {
                        case PIGLIN:
                            for (String s : MOD_CONFIG.zombiesCantGather.get()) builder.suggest(s);
                            break;
                        case ZOMBIE:
                            for (String s : MOD_CONFIG.piglinsCantGather.get()) builder.suggest(s);
                            break;
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> remove(ctx, StringArgumentType.getString(ctx, TYPE_ARGUMENT), ItemStackArgumentType.getItemStackArgument(ctx, ITEM_ARGUMENT)))
                .build();
        removeNode.addChild(itemNode);
        return removeNode;
    }

    public static LiteralCommandNode<ServerCommandSource> getResetNode() {
        return CommandManager
                .literal("reset")
                .requires(ctx -> hasPermission(ctx, BLACKLIST_RESET_PERMISSION))
                .executes(ctx -> reset(ctx, StringArgumentType.getString(ctx, TYPE_ARGUMENT)))
                .build();
    }
}
