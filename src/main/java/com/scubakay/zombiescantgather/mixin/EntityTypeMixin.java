package com.scubakay.zombiescantgather.mixin;

import com.scubakay.zombiescantgather.state.EntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Function;
//? >= 1.21.2 {
import net.minecraft.entity.SpawnReason;
//?}

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;

@Mixin(EntityType.class)
public class EntityTypeMixin {
    @Inject(method = "method_17843", at = @At("RETURN"))
    private static void zombiesCantGather$checkForbiddenItems(NbtCompound nbtCompound, World world, /*? >= 1.21.2 {*/SpawnReason spawnReason,/*?}*/ Function<Entity, Entity> function, Entity entity, CallbackInfoReturnable<Entity> cir) {
        if (MOD_CONFIG.enableTracker.get() && world instanceof ServerWorld && entity instanceof MobEntity mobEntity) {
            if (mobEntity.getCustomName() == null || MOD_CONFIG.trackCustomNamedMobs.get()) {
                //? >= 1.21.5 {
                ItemStack item = mobEntity.getMainHandStack();
                //?} else {
                /*ItemStack item = mobEntity.getHandItems().iterator().next();
                *///?}
                if (entity instanceof ZombieEntity zombie && MOD_CONFIG.zombiesCantGather.get().contains(item.getItem().toString())) {
                    EntityTracker.getServerState(world.getServer()).track(zombie);
                } else if (entity instanceof PiglinEntity piglin && MOD_CONFIG.piglinsCantGather.get().contains(item.getItem().toString())) {
                    EntityTracker.getServerState(world.getServer()).track(piglin);
                }
            }
        }
    }
}