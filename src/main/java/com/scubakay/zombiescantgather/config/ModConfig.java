package com.scubakay.zombiescantgather.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.custom.StringList;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class ModConfig {
    private final StringList DEFAULT_ZOMBIE_ITEMS = StringList.of("minecraft:glow_ink_sac");
    private final StringList DEFAULT_PIGLIN_ITEMS = StringList.of();
    private final boolean DEFAULT_ENABLE_TRACKER = false;
    private final boolean DEFAULT_SHOW_TRACKER_LOGS = false;
    private final boolean DEFAULT_TRACK_CUSTOM_NAMED_MOBS = false;
    private final boolean DEFAULT_BROADCAST_TRACKED_MOBS = false;

    public final ConfigEntry<StringList> zombiesCantGather;
    public final ConfigEntry<StringList> piglinsCantGather;
    public final ConfigEntry<Boolean> enableTracker;
    public final ConfigEntry<Boolean> showTrackerLogs;
    public final ConfigEntry<Boolean> trackCustomNamedMobs;
    public final ConfigEntry<Boolean> broadcastTrackedMobs;

    public ModConfig(ConfigBuilder builder) {
        zombiesCantGather = builder
                .entry("zombiescantgather_items", DEFAULT_ZOMBIE_ITEMS)
                .comment("List of items that zombies should not pick up");
        piglinsCantGather = builder
                .entry("piglinscantgather_items", DEFAULT_PIGLIN_ITEMS)
                .comment("List of items that piglins should not pick up");
        enableTracker = builder
                .entry("enable_tracker", DEFAULT_ENABLE_TRACKER)
                .comment("Enable/disable tracker");
        showTrackerLogs = builder
                .entry("show_tracker_logs", DEFAULT_SHOW_TRACKER_LOGS)
                .comment("Show tracker logs");
        trackCustomNamedMobs = builder
                .entry("track_custom_named_mobs", DEFAULT_TRACK_CUSTOM_NAMED_MOBS)
                .comment("Track mobs with a custom name");
        broadcastTrackedMobs = builder
                .entry("broadcast_tracked_mobs", DEFAULT_BROADCAST_TRACKED_MOBS)
                .comment("Broadcast entity log message to OPs");
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
