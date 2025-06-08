package com.scubakay.zombiescantgather.util;

import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

public class MinecraftUtil {
    public static String itemStackArgumentToString(ItemStackArgument itemStackArgument) {
        return Registries.ITEM.getId(itemStackArgument.getItem()).toString();
    }

    public static String itemToString(Item item) {
        return Registries.ITEM.getId(item).toString();
    }
}
