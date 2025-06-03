package com.scubakay.zombiescantgather.mixin;

import com.scubakay.zombiescantgather.ZombiesCantGather;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//? if >= 1.19.3 {
import net.minecraft.registry.Registries;
//?} else {
/*import net.minecraft.util.registry.Registry;
 *///?}

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
        Identifier id = /*? >= 1.19.3 {*/Registries/*} else {*//*Registry*//*?}*/
                .ITEM.getId(stack.getItem());
        return ZombiesCantGather.modConfig.zombiesCantGather.get().contains(id.toString());
    }
}
