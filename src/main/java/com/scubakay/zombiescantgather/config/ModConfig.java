package com.scubakay.zombiescantgather.config;

import eu.midnightdust.lib.config.MidnightConfig;

import java.util.List;

@SuppressWarnings("CanBeFinal")
public class ModConfig extends MidnightConfig {
    public static final String general = "general";
    public static final String tracker = "tracker";

    @Entry(category = general)
    public static List<String> zombiesBlacklist = List.of("minecraft:glow_ink_sac");
    @Entry(category = general)
    public static List<String> piglinsBlacklist = List.of();
    @Hidden
    @Entry(category = general)
    public static int permissionLevel = 4;

    @Entry(category = tracker)
    public static boolean enableTracker = false;
    @Condition(requiredOption = "zombiescantgather:enableTracker", visibleButLocked = true)
    @Entry(category = tracker)
    public static boolean showTrackerLogs = false;
    @Condition(requiredOption = "zombiescantgather:enableTracker", visibleButLocked = true)
    @Entry(category = tracker)
    public static boolean trackCustomNamedMobs = false;
    @Condition(requiredOption = "zombiescantgather:enableTracker", visibleButLocked = true)
    @Entry(category = tracker)
    public static boolean broadcastTrackedMobs = false;
}
