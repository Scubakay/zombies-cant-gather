package com.scubakay.zombiescantgather.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.custom.StringList;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

import static com.scubakay.zombiescantgather.command.PermissionManager.TRACKER_LOG_PERMISSION;

@SuppressWarnings("FieldCanBeLocal")
public class ModConfig {
    private final StringList DEFAULT_ZOMBIE_ITEMS = StringList.of("minecraft:glow_ink_sac");
    private final StringList DEFAULT_PIGLIN_ITEMS = StringList.of();
    private final boolean DEFAULT_ENABLE_TRACKER = false;
    private final boolean DEFAULT_SHOW_TRACKER_LOGS = false;
    private final boolean DEFAULT_TRACK_CUSTOM_NAMED_MOBS = false;
    private final boolean DEFAULT_BROADCAST_TRACKED_MOBS = false;
    private final int DEFAULT_PERMISSION_LEVEL = 4;

    public final ConfigEntry<StringList> zombiesCantGather;
    public final ConfigEntry<StringList> piglinsCantGather;
    public final ConfigEntry<Boolean> enableTracker;
    public final ConfigEntry<Boolean> showTrackerLogs;
    public final ConfigEntry<Boolean> trackCustomNamedMobs;
    public final ConfigEntry<Boolean> broadcastTrackedMobs;
    public final ConfigEntry<Integer> permissionLevel;

    public ModConfig(ConfigBuilder builder) {
        zombiesCantGather = builder
                .entry("zombies_blacklist", DEFAULT_ZOMBIE_ITEMS)
                .comment("==== BLACKLISTS ====\n" +
                        "Zombies can't pick up these items");
        piglinsCantGather = builder
                .entry("piglins_blacklist", DEFAULT_PIGLIN_ITEMS)
                .comment("Piglins can't pick up these items");
        enableTracker = builder
                .entry("enable_tracker", DEFAULT_ENABLE_TRACKER)
                .comment("==== TRACKER ====\n" +
                        "Track mobs holding blacklisted items");
        showTrackerLogs = builder
                .entry("show_tracker_logs", DEFAULT_SHOW_TRACKER_LOGS)
                .comment("Show tracker updates in the console");
        trackCustomNamedMobs = builder
                .entry("track_custom_named_mobs", DEFAULT_TRACK_CUSTOM_NAMED_MOBS)
                .comment("Track mobs with a custom name");
        broadcastTrackedMobs = builder
                .entry("broadcast_tracked_mobs", DEFAULT_BROADCAST_TRACKED_MOBS)
                .comment(String.format("Broadcast tracker updates to OPs and users with the '%s' permission", TRACKER_LOG_PERMISSION));
        permissionLevel = builder
                .entry("permission_level", DEFAULT_PERMISSION_LEVEL)
                .comment("""
                        ==== PERMISSIONS ====
                        The permission level needed to access Zombies Can't Gather commands.
                        See https://minecraft.wiki/w/Permission_level for permission levels. For more advanced
                        permission customization, use a permission manager like LuckPerms""");
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
