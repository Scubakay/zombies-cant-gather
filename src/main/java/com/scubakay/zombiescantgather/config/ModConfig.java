package com.scubakay.zombiescantgather.config;

import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;

import java.util.List;

@SuppressWarnings("CanBeFinal")
public class ModConfig extends MidnightConfig {
    public static final String general = "general";
    public static final String tracker = "tracker";

    @Entry(category = general)
    public static List<String> zombiesBlacklist = Lists.newArrayList("minecraft:glow_ink_sac");
    @Entry(category = general)
    public static List<String> piglinsBlacklist = Lists.newArrayList();
    @Hidden
    @Entry(category = general)
    public static int permissionLevel = 4;

    @Comment(category = tracker)
    public static Comment trackerDescription;

    @Comment(category = tracker)
    public static Comment spacer1;

    @Entry(category = tracker)
    public static boolean enableTracker = false;
    //? >= 1.21.5 {
    @Condition(requiredOption = "zombiescantgather:enableTracker", visibleButLocked = true)
     //?}
    @Entry(category = tracker)
    public static boolean trackCustomNamedMobs = false;
    //? >= 1.21.5 {
    @Condition(requiredOption = "zombiescantgather:enableTracker", visibleButLocked = true)
    //?}
    @Entry(category = tracker)
    public static boolean showTrackerLogs = false;
    //? >= 1.21.5 {
    @Condition(requiredOption = "zombiescantgather:enableTracker", visibleButLocked = true)
     //?}
    @Entry(category = tracker)
    public static boolean broadcastTrackedMobs = false;
}
