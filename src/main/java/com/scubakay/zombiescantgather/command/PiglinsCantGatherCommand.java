package com.scubakay.zombiescantgather.command;

import com.mojang.brigadier.context.CommandContext;
import com.scubakay.zombiescantgather.util.CommandUtil;
import de.maxhenkel.admiral.annotations.Command;
import de.maxhenkel.admiral.annotations.Name;
import de.maxhenkel.admiral.annotations.RequiresPermission;
import de.maxhenkel.configbuilder.custom.StringList;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.server.command.ServerCommandSource;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;
import static com.scubakay.zombiescantgather.command.ZombiesCantGatherPermissionsManager.*;
import static com.scubakay.zombiescantgather.util.CommandUtil.FANCY_MOD_NAME;

@Command({"zombiescantgather", "piglin"})
public class PiglinsCantGatherCommand {
    @Command("add")
    @RequiresPermission(ADD_PERMISSION)
    public void add(CommandContext<ServerCommandSource> context, @Name("item") ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            MOD_CONFIG.addPiglinItem(item);
            CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins can't gather §f" + item);
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins already can't gather §f" + item);
        }
    }

    @Command("remove")
    @RequiresPermission(REMOVE_PERMISSION)
    public void list(CommandContext<ServerCommandSource> context, ItemStackArgument itemStackArgument) {
        String item = itemStackArgument.getItem().toString();
        try {
            MOD_CONFIG.removePiglinItem(item);
            CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins can gather §f" + item + "§7 again");
        } catch (IllegalArgumentException ex) {
            CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins can already gather §f" + item);
        }
    }

    @Command("list")
    @RequiresPermission(LIST_PERMISSION)
    public void list(CommandContext<ServerCommandSource> context) {
        StringList piglinItems = MOD_CONFIG.piglinsCantGather.get();
        CommandUtil.reply(context, FANCY_MOD_NAME + "Piglins can't pick up these items:");
        piglinItems.forEach((item) -> CommandUtil.reply(context, "§f" + item));
    }
}
