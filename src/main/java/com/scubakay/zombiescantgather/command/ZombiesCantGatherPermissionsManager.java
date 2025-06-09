package com.scubakay.zombiescantgather.command;

import de.maxhenkel.admiral.permissions.PermissionManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;

public class ZombiesCantGatherPermissionsManager implements PermissionManager<ServerCommandSource> {
    @SuppressWarnings("unused")
    public static final String ROOT_PERMISSION = "zombiescantgather";
    public static final String ADD_PERMISSION = "zombiescantgather.add";
    public static final String REMOVE_PERMISSION = "zombiescantgather.remove";
    public static final String LIST_PERMISSION = "zombiescantgather.list";
    private static final int DEFAULT_PERMISSION_LEVEL = 4;

    private static final boolean fabricPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    @Override
    public boolean hasPermission(ServerCommandSource source, String permission) {
        if (source.hasPermissionLevel(DEFAULT_PERMISSION_LEVEL)) return true;
        return fabricPermissionsApi && Permissions.check(source, permission);
    }
}
