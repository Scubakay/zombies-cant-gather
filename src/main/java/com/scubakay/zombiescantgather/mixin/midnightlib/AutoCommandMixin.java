package com.scubakay.zombiescantgather.mixin.midnightlib;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.scubakay.zombiescantgather.ZombiesCantGather;
import eu.midnightdust.lib.config.AutoCommand;
import eu.midnightdust.lib.util.PlatformFunctions;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@SuppressWarnings("UnusedMixin")
@Mixin(AutoCommand.class)
public class AutoCommandMixin {
    @ModifyArg(
            method = "<init>(Ljava/lang/reflect/Field;Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Leu/midnightdust/lib/util/PlatformFunctions;registerCommand(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)V"
            ),
            remap = false
    )
    private LiteralArgumentBuilder<ServerCommandSource> zombiesCantGather$putConfigUnderModRoot(LiteralArgumentBuilder<ServerCommandSource> command, @Local(argsOnly = true) String modid, @Local() LiteralArgumentBuilder<ServerCommandSource> originalCommand) {
        if (ZombiesCantGather.MOD_ID.equals(modid)) {
            PlatformFunctions.registerCommand(originalCommand);
        }
        return command;
    }
}