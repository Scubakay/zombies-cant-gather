package com.scubakay.zombiescantgather.command;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;

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

    public static final String CONFIGURE_MOD_PERMISSION = ROOT_PERMISSION + ".configure";

    private static final boolean fabricPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    public static boolean hasPermission(ServerPlayerEntity player, String permission) {
        if (player.hasPermissionLevel(MOD_CONFIG.permissionLevel.get())) return true;
        return fabricPermissionsApi && Permissions.check(player, permission);
    }

    public static boolean hasPermission(ServerCommandSource source, String permission) {
        if (source.hasPermissionLevel(MOD_CONFIG.permissionLevel.get())) return true;
        return fabricPermissionsApi && Permissions.check(source, permission);
    }
}
