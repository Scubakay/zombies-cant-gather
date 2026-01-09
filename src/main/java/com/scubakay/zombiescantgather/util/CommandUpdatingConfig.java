package com.scubakay.zombiescantgather.util;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Updates all players command trees when updating MidnightConfig
 */
public class CommandUpdatingConfig extends MidnightConfig {
    private static MinecraftServer server;

    static {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> CommandUpdatingConfig.server = server);
    }

    @Override
    public void writeChanges() {
        super.writeChanges();
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach(player -> server.getPlayerManager().sendCommandTree(player));
        }
    }
}
