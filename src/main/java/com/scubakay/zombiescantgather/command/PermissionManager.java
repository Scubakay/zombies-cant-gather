package com.scubakay.zombiescantgather.command;

import com.scubakay.zombiescantgather.config.ModConfig;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionManager {
    @SuppressWarnings("unused")
    public static final String ROOT_PERMISSION = "zombiescantgather";

    public static final String BLACKLIST_PERMISSION = ROOT_PERMISSION + ".blacklist";
    public static final String BLACKLIST_ADD_PERMISSION = BLACKLIST_PERMISSION + ".add";
    public static final String BLACKLIST_REMOVE_PERMISSION = BLACKLIST_PERMISSION + ".remove";
    public static final String BLACKLIST_RESET_PERMISSION = BLACKLIST_PERMISSION + ".reset";

    public static final String TRACKER_PERMISSION = ROOT_PERMISSION + ".tracker";
    public static final String TRACKER_LOG_PERMISSION = TRACKER_PERMISSION + ".log";
    public static final String TRACKER_RESET_PERMISSION = TRACKER_PERMISSION + ".reset";
    public static final String TRACKER_TELEPORT_PERMISSION = TRACKER_PERMISSION + ".teleport";
    public static final String TRACKER_PURGE_PERMISSION = TRACKER_PERMISSION + ".purge";

    public static final String CONFIGURE_MOD_PERMISSION = ROOT_PERMISSION + ".configure";

    private static final boolean fabricPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    public static boolean hasPermission(ServerPlayerEntity player, String permission) {
        return fabricPermissionsApi && Permissions.check(player, permission, PermissionLevel.fromLevel(ModConfig.permissionLevel));
    }

    public static boolean hasPermission(ServerCommandSource source, String permission) {
        return fabricPermissionsApi && Permissions.check(source, permission, PermissionLevel.fromLevel(ModConfig.permissionLevel));
    }
}
