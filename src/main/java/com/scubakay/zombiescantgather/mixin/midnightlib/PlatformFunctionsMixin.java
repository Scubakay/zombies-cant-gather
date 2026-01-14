package com.scubakay.zombiescantgather.mixin.midnightlib;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.scubakay.zombiescantgather.ZombiesCantGather;
import com.scubakay.zombiescantgather.command.Commands;
import eu.midnightdust.lib.util.PlatformFunctions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnusedMixin")
@Mixin(PlatformFunctions.class)
public class PlatformFunctionsMixin {
    @Inject(
            method = "registerCommand",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void zombiesCantGather$registerCommand(LiteralArgumentBuilder<ServerCommandSource> command, CallbackInfo ci) {
        CommandNode<ServerCommandSource> configChild = command.build().getChild(ZombiesCantGather.MOD_ID);
        if (configChild != null) {
            CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
                    configChild.getChildren().forEach(child ->
                            Commands.getConfig(dispatcher).addChild(child)));
            ci.cancel();
        }
    }
}
