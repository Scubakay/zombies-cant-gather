package com.scubakay.zombiescantgather.config;

import com.scubakay.zombiescantgather.util.CommandUpdatingConfig;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("CanBeFinal")
public class ModConfig extends CommandUpdatingConfig {
    public static final String general = "general";
    public static final String tracker = "tracker";

    @Entry(category = general)
    public static List<String> zombiesBlacklist = new ArrayList<>(List.of("minecraft:glow_ink_sac"));
    @Entry(category = general)
    public static List<String> piglinsBlacklist = new ArrayList<>();
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
