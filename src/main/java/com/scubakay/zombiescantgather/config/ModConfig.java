package com.scubakay.zombiescantgather.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.custom.StringList;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private final StringList DEFAULT_ZOMBIE_ITEMS = StringList.of("minecraft:glow_ink_sac");
    private final StringList DEFAULT_PIGLIN_ITEMS = StringList.of();

    public ConfigEntry<StringList> zombiesCantGather;
    public ConfigEntry<StringList> piglinsCantGather;

    public ModConfig(ConfigBuilder builder) {

        zombiesCantGather = builder
                .entry("zombiescantgather_items", DEFAULT_ZOMBIE_ITEMS)
                .comment("List of items that zombies should not pick up");
        piglinsCantGather = builder
                .entry("piglinscantgather_items", DEFAULT_PIGLIN_ITEMS)
                .comment("List of items that piglins should not pick up");
    }

    public void addZombieItem(String item) {
        List<String> list = new ArrayList<>(zombiesCantGather.get());
        if (list.contains(item)) {
            throw new IllegalArgumentException("Item already exists: " + item);
        }
        list.add(item);
        zombiesCantGather.set(StringList.of(list)).save();
    }

    public void removeZombieItem(String item) {
        List<String> list = new ArrayList<>(zombiesCantGather.get());
        if (!list.contains(item)) {
            throw new IllegalArgumentException("Item not found: " + item);
        }
        list.remove(item);
        zombiesCantGather.set(StringList.of(list)).save();
    }

    public void resetZombieItems() {
        zombiesCantGather.set(DEFAULT_ZOMBIE_ITEMS).save();
    }

    public void addPiglinItem(String item) {
        List<String> list = new ArrayList<>(piglinsCantGather.get());
        if (list.contains(item)) {
            throw new IllegalArgumentException("Item already exists: " + item);
        }
        list.add(item);
        piglinsCantGather.set(StringList.of(list)).save();
    }

    public void removePiglinItem(String item) {
        List<String> list = new ArrayList<>(piglinsCantGather.get());
        if (!list.contains(item)) {
            throw new IllegalArgumentException("Item not found: " + item);
        }
        list.remove(item);
        piglinsCantGather.set(StringList.of(list)).save();
    }

    public void resetPiglinItems() {
        piglinsCantGather.set(DEFAULT_PIGLIN_ITEMS).save();
    }
}
