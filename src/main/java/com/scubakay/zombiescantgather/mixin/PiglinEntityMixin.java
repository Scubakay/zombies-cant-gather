package com.scubakay.zombiescantgather.mixin;

import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;

@Mixin(PiglinBrain.class)
public class PiglinEntityMixin {
    @Inject(method = "canGather", at = @At("HEAD"), cancellable = true)
    private static void injectPiglinsCantGather(PiglinEntity piglin, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (MOD_CONFIG.zombiesCantGather.get().contains(id.toString())) {
            cir.setReturnValue(false);
        }
    }
}
