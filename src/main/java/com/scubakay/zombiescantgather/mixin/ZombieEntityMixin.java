package com.scubakay.zombiescantgather.mixin;

import com.scubakay.zombiescantgather.ZombiesCantGather;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin {
    @Redirect(
        method = "canGather",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"
        )
    )
    private boolean zombiesCantGather$redirectIsOf(ItemStack stack, Item item) {
        return ZombiesCantGather.MOD_CONFIG.zombiesCantGather.get().contains(stack.getItem().toString());
    }
}
