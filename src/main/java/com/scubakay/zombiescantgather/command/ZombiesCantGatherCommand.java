package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.context.CommandContext;
import com.scubakay.zombiescantgather.ZombiesCantGather;
import de.maxhenkel.admiral.annotations.Command;
import de.maxhenkel.admiral.annotations.Name;
import de.maxhenkel.admiral.annotations.RequiresPermission;
import de.maxhenkel.configbuilder.custom.StringList;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.scubakay.zombiescantgather.command.ZombiesCantGatherPermissionsManager.*;

@Command("zombiescantgather")
public class ZombiesCantGatherCommand {
    private static final String MOD_NAME = "§a[Zombies Can't Gather] §7";

    @Command("add")
    @RequiresPermission(ADD_PERMISSION)
    public void add(CommandContext<ServerCommandSource> context, @Name("item") ItemStackArgument itemStackArgument) {
        String item = getIdentifierFromItemStackArgument(itemStackArgument);
        try {
            ZombiesCantGather.MOD_CONFIG.addItem(item);
            context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "Zombies can't gather §f" + item), false);
        } catch (IllegalArgumentException ex) {
            context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "Zombies already can't gather §f" + item), false);
        }
    }

    @Command("remove")
    @RequiresPermission(REMOVE_PERMISSION)
    public void list(CommandContext<ServerCommandSource> context, ItemStackArgument itemStackArgument) {
        String item = getIdentifierFromItemStackArgument(itemStackArgument);
        try {
            ZombiesCantGather.MOD_CONFIG.removeItem(item);
            context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "Removed §f" + item), false);
        } catch (IllegalArgumentException ex) {
            context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "§f" + item + "§7 was not found"), false);
        }
    }

    @Command("list")
    @RequiresPermission(LIST_PERMISSION)
    public void list(CommandContext<ServerCommandSource> context) {
        StringList items = ZombiesCantGather.MOD_CONFIG.zombiesCantGather.get();
        context.getSource().sendFeedback(() -> Text.literal(MOD_NAME + "Zombies can't pick up these items:"), false);
        items.forEach((item) -> context.getSource().sendFeedback(() -> Text.literal("§f" + item), false));
    }

    private static String getIdentifierFromItemStackArgument(ItemStackArgument itemStackArgument) {
        return Registries.ITEM.getId(itemStackArgument.getItem()).toString();
    }
}
