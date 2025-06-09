package com.scubakay.zombiescantgather.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.custom.StringList;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private final StringList ZOMBIES_CANT_GATHER_DEFAULT = StringList.of(Items.GLOW_INK_SAC.toString());
    private final StringList PIGLINS_CANT_GATHER_DFAULT = StringList.of();

    public ConfigEntry<StringList> zombiesCantGather;
    public ConfigEntry<StringList> piglinsCantGather;

    public ModConfig(ConfigBuilder builder) {
        zombiesCantGather = builder
                .entry("zombiescantgather_items", ZOMBIES_CANT_GATHER_DEFAULT)
                .comment("List of items that zombies should not pick up");
        piglinsCantGather = builder
                .entry("piglinscantgather_items", PIGLINS_CANT_GATHER_DFAULT)
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

    public void reset() {
        zombiesCantGather.set(ZOMBIES_CANT_GATHER_DEFAULT).save();
        piglinsCantGather.set(PIGLINS_CANT_GATHER_DFAULT).save();
    }
}
