package com.scubakay.zombiescantgather.mixin.midnightlib;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.scubakay.zombiescantgather.command.PermissionManager;
import com.scubakay.zombiescantgather.command.RootCommand;
import eu.midnightdust.lib.util.fabric.PlatformFunctionsImpl;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlatformFunctionsImpl.class)
public class PlatformFunctionsImplMixin {
    @Inject(
            method = "registerCommand",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void zombiesCantGather$registerCorrectedConfigCommand(LiteralArgumentBuilder<ServerCommandSource> command, CallbackInfo ci) {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> RootCommand.getRoot(commandDispatcher)
                .addChild(CommandManager
                        .literal("config")
                        .requires(source -> PermissionManager.hasPermission(source, PermissionManager.CONFIGURE_MOD_PERMISSION))
                        .then(command)
                        .build()
                )));
        ci.cancel();
    }
}
