package com.scubakay.zombiescantgather.mixin.midnightlib;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(MidnightConfig.class)
public class MidnightConfigMixin {
    @Unique
    private static MinecraftServer server;

    static {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> MidnightConfigMixin.server = server);
    }

    @Inject(method = "write", at = @At("RETURN"))
    private static void zombiesCantGather$updateCommandTree(String modid, CallbackInfo ci) {
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach(player -> server.getPlayerManager().sendCommandTree(player));
        }
    }
}
