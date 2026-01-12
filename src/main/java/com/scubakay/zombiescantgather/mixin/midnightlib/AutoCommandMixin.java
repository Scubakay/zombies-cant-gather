package com.scubakay.zombiescantgather.mixin.midnightlib;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.scubakay.zombiescantgather.command.Commands;
import com.scubakay.zombiescantgather.command.PermissionManager;
import eu.midnightdust.lib.config.AutoCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("UnusedMixin")
@Mixin(AutoCommand.class)
public class AutoCommandMixin {
    @Redirect(
            method = "<init>(Ljava/lang/reflect/Field;Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Leu/midnightdust/lib/util/PlatformFunctions;registerCommand(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)V"
            ),
            remap = false
    )
    private static void zombiesCantGather$putConfigUnderModRoot(LiteralArgumentBuilder<ServerCommandSource> ignoredCommand, @Local LiteralArgumentBuilder<ServerCommandSource> command) {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registry, environment) -> Commands.getRoot(dispatcher)
                .addChild(CommandManager
                        .literal("config")
                        .requires(source -> PermissionManager.hasPermission(source, PermissionManager.CONFIGURE_MOD_PERMISSION))
                        .then(command)
                        .build())));
    }
}