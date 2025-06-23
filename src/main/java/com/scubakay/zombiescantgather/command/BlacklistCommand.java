package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.scubakay.zombiescantgather.config.ModConfig;
import com.scubakay.zombiescantgather.util.CommandReply;
import com.scubakay.zombiescantgather.util.CommandPagination;
import com.scubakay.zombiescantgather.util.CommandUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.scubakay.zombiescantgather.command.Commands.ROOT_COMMAND;
import static com.scubakay.zombiescantgather.command.PermissionManager.*;

@SuppressWarnings("SameReturnValue")
public class BlacklistCommand {
    private static final String ITEM_ARGUMENT = "item";

    private static final String ADDED_REPLY = "§f%s§7 can't gather §f%s§7:";
    private static final String REMOVED_REPLY = "§f%s§7 can gather §f%s§7 again:";
    private static final String DUPLICATE_REPLY = "§f%s§7 is already on the §f%s§7 blacklist";
    private static final String NOT_FOUND_REPLY = "§f%s§7 not found in §f%s§7 blacklist";
    private static final String RESET_ITEMS_REPLY = "§7Reset §f%s§7 items";
    private static final String BLACKLIST_HEADER_REPLY = "§f%s§7 can't pick up §f%s§7 items:";
    private static final String BLACKLIST_ROW_REPLY = "§f%s§7";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry, RegistrationEnvironment ignoredEnv) {
        Commands.getRoot(dispatcher).addChild(getBlacklistNode(registry, Blacklist.PIGLIN));
        Commands.getRoot(dispatcher).addChild(getBlacklistNode(registry, Blacklist.ZOMBIE));
    }

    //region Command Nodes

    private static LiteralCommandNode<ServerCommandSource> getBlacklistNode(CommandRegistryAccess registry, Blacklist blacklist) {
        LiteralCommandNode<ServerCommandSource> piglinNode = getEntityNode(blacklist); // zcg <entity>
        piglinNode.addChild(getAddNode(registry, blacklist)); // zcg <entity> add <item>
        piglinNode.addChild(getRemoveNode(registry, blacklist)); // zcg <entity> remove <item>
        piglinNode.addChild(getResetNode(blacklist)); // zcg <entity> reset
        return piglinNode;
    }

    private static LiteralCommandNode<ServerCommandSource> getEntityNode(Blacklist entity) {
        return CommandManager
                .literal(entity.toString())
                .requires(ctx -> hasPermission(ctx, BLACKLIST_PERMISSION))
                .executes(ctx -> list(ctx, entity))
                .then(CommandPagination.getPageCommand(ctx -> list(ctx, entity)))
                .build();
    }

    private static LiteralCommandNode<ServerCommandSource> getAddNode(CommandRegistryAccess registry, Blacklist entityType) {
        LiteralCommandNode<ServerCommandSource> addNode = CommandManager
                .literal("add")
                .requires(ctx -> PermissionManager.hasPermission(ctx, BLACKLIST_ADD_PERMISSION))
                .build();
        ArgumentCommandNode<ServerCommandSource, ItemStackArgument> itemNode = CommandManager
                .argument(ITEM_ARGUMENT, ItemStackArgumentType.itemStack(registry))
                .requires(ctx -> hasPermission(ctx, BLACKLIST_ADD_PERMISSION))
                .suggests((context, builder) -> {
                    String remaining = builder.getRemainingLowerCase();
                    Stream<Item> stream = Registries.ITEM.stream();
                    switch (entityType) {
                        case Blacklist.PIGLIN:
                            populateBlacklistAddSuggestions(stream, ModConfig.piglinsBlacklist, remaining, builder);
                            break;
                        case Blacklist.ZOMBIE:
                            populateBlacklistAddSuggestions(stream, ModConfig.zombiesBlacklist, remaining, builder);
                            break;
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> add(ctx, entityType, ItemStackArgumentType.getItemStackArgument(ctx, ITEM_ARGUMENT)))
                .build();
        addNode.addChild(itemNode);
        return addNode;
    }

    private static LiteralCommandNode<ServerCommandSource> getRemoveNode(CommandRegistryAccess registry, Blacklist entityType) {
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
                            for (String s : ModConfig.piglinsBlacklist) builder.suggest(s);
                            break;
                        case Blacklist.ZOMBIE:
                            for (String s : ModConfig.zombiesBlacklist) builder.suggest(s);
                            break;
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> remove(ctx, entityType, ItemStackArgumentType.getItemStackArgument(ctx, ITEM_ARGUMENT)))
                .build();
        removeNode.addChild(itemNode);
        return removeNode;
    }

    private static LiteralCommandNode<ServerCommandSource> getResetNode(Blacklist entityType) {
        return CommandManager
                .literal("reset")
                .requires(ctx -> hasPermission(ctx, BLACKLIST_RESET_PERMISSION))
                .executes(ctx -> reset(ctx, entityType))
                .build();
    }

    //endregion

    //region Command Handlers

    private static int list(CommandContext<ServerCommandSource> ctx, Blacklist type) {
        List<String> items = switch (type) {
            case PIGLIN -> ModConfig.piglinsBlacklist;
            case ZOMBIE -> ModConfig.zombiesBlacklist;
        };
        final Function<CommandPagination.Context, Text> header = context ->
                Text.literal(String.format(BLACKLIST_HEADER_REPLY, type.toPlural(), context.elementCount()));
        displayPaginatedBlacklist(ctx, type, items, header);
        return Command.SINGLE_SUCCESS;
    }

    private static void displayPaginatedBlacklist(CommandContext<ServerCommandSource> ctx, Blacklist type, List<String> items, Function<CommandPagination.Context, Text> header) {
        CommandPagination.builder(ctx, items)
                .withCommand(getBlacklistCommand(type))
                .withPageSize(5)
                .withHeader(header)
                .withRows(item -> Text.literal(String.format(BLACKLIST_ROW_REPLY, item)), List.of(getRemoveButton(type)))
                .withEmptyMessage(context -> Text.literal(String.format("No items on %s blacklist", type)))
                .withButton(getAddButton(type))
                .display();
    }

    private static String getBlacklistCommand(Blacklist list) {
        return String.format("/%s %s", ROOT_COMMAND, list);
    }

    private static int add(CommandContext<ServerCommandSource> ctx, Blacklist type, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        List<String> items = switch (type) {
            case PIGLIN -> ModConfig.piglinsBlacklist;
            case ZOMBIE -> ModConfig.zombiesBlacklist;
        };
        final Function<CommandPagination.Context, Text> header;
        if (items.contains(item)) {
            header = context ->
                    Text.literal(String.format(DUPLICATE_REPLY, item, type.toPlural()));
        } else {
            items.add(item);
            header = context ->
                    Text.literal(String.format(ADDED_REPLY, type.toPlural(), item));
        }

        displayPaginatedBlacklist(ctx, type, items, header);
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CommandContext<ServerCommandSource> ctx, Blacklist type, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        List<String> items = switch (type) {
            case PIGLIN -> ModConfig.piglinsBlacklist;
            case ZOMBIE -> ModConfig.zombiesBlacklist;
        };
        int index = items.indexOf(item);
        if (index < 0) {
            CommandUtil.send(ctx, NOT_FOUND_REPLY, item, type);
            return 0;
        }
        items.remove(item);
        final Function<CommandPagination.Context, Text> header = context ->
                Text.literal(String.format(REMOVED_REPLY, type.toPlural(), item));
        displayPaginatedBlacklist(ctx, type, items, header);
        return Command.SINGLE_SUCCESS;
    }

    private static int reset(CommandContext<ServerCommandSource> context, Blacklist type) {
        switch (type) {
            case PIGLIN -> ModConfig.piglinsBlacklist.clear();
            case ZOMBIE -> ModConfig.zombiesBlacklist.clear();
        }

        CommandUtil.send(context, RESET_ITEMS_REPLY, type);
        return Command.SINGLE_SUCCESS;
    }

    //endregion

    //region Utility

    private static CommandReply<List<String>> getAddButton(Blacklist type) {
        return CommandReply.<List<String>>get(item -> Text.literal("Add"))
                .requires(player -> !hasPermission(player, BLACKLIST_ADD_PERMISSION))
                .withToolTip(item -> Text.literal("Blacklist an item"))
                .withSuggestion(item -> String.format("/zcg %s add ", type))
                .withColor(item -> Colors.BLUE)
                .withBrackets();
    }

    private static @NotNull CommandReply<String> getRemoveButton(Blacklist type) {
        return CommandReply.<String>get(item -> Text.literal("X"))
                .requires(player -> !hasPermission(player, BLACKLIST_REMOVE_PERMISSION))
                .withToolTip(item -> Text.literal(String.format("Remove %s from blacklist", item)))
                .withCommand(id -> String.format("/zcg %s remove %s", type, id))
                .withColor(item -> Colors.RED)
                .withBrackets();
    }

    private static void populateBlacklistAddSuggestions(Stream<Item> stream, List<String> blacklist, String remaining, SuggestionsBuilder builder) {
        stream.filter(x -> !blacklist.contains(x.asItem().toString()))
                .filter(x -> x.asItem().toString().toLowerCase().contains(remaining))
                .forEach(x -> builder.suggest(x.asItem().toString()));
    }

    //endregion

    public enum Blacklist {
        PIGLIN("piglin", "piglins"),
        ZOMBIE("zombie", "zombies");

        private final String name;
        private final String plural;

        Blacklist(final String name, final String plural) {
            this.name = name;
            this.plural = plural;
        }

        public String toString() {
            return name;
        }

        public String toPlural() {
            return plural;
        }
    }
}
