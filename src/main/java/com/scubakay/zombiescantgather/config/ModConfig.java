package com.scubakay.zombiescantgather.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.custom.StringList;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public ConfigEntry<StringList> zombiesCantGather;

    public ModConfig(ConfigBuilder builder) {
        zombiesCantGather = builder
                .entry("zombiescantgather_items", StringList.of("minecraft:glow_ink_sac"))
                .comment("List of items that zombies should not pick up");
    }

    public void addItem(String item) {
        List<String> list = new ArrayList<>(zombiesCantGather.get());
        if (list.contains(item)) {
            throw new IllegalArgumentException("Item already exists: " + item);
        }
        list.add(item);
        zombiesCantGather.set(StringList.of(list)).save();
    }

    public void removeItem(String item) {
        List<String> list = new ArrayList<>(zombiesCantGather.get());
        if (!list.contains(item)) {
            throw new IllegalArgumentException("Item not found: " + item);
        }
        list.remove(item);
        zombiesCantGather.set(StringList.of(list)).save();
    }
}
