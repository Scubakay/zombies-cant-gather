package com.scubakay.zombiescantgather.mixin;

import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin {
    @Redirect(method = "canGather", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean zombiesCantGather$redirectIsOf(ItemStack stack, Item item) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return MOD_CONFIG.zombiesCantGather.get().contains(id.toString());
    }
}
