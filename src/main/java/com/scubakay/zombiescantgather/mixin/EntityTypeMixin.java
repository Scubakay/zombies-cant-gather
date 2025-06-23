package com.scubakay.zombiescantgather.mixin;

import com.scubakay.zombiescantgather.config.ModConfig;
import com.scubakay.zombiescantgather.state.EntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Function;
//? >= 1.21.6 {
import net.minecraft.storage.ReadView;
//?} else {
/*import net.minecraft.nbt.NbtCompound;
*///?}

//? >= 1.21.2 {
import net.minecraft.entity.SpawnReason;
//?}

@Mixin(EntityType.class)
public class EntityTypeMixin {
    @Inject(method = "method_17843", at = @At("RETURN"))
    //? >= 1.21.6 {
    private static void zombiesCantGather$checkForbiddenItems(ReadView readView, World world, SpawnReason spawnReason, Function<Entity, Entity> function, Entity entity, CallbackInfoReturnable<Entity> cir) {
    //?} else >= 1.21.2 {
    /*private static void zombiesCantGather$checkForbiddenItems(NbtCompound nbtCompound, World world, SpawnReason spawnReason, Function<Entity, Entity> function, Entity entity, CallbackInfoReturnable<Entity> cir) {
    *///?} else {
    /*private static void zombiesCantGather$checkForbiddenItems(NbtCompound nbtCompound, World world, Function<Entity, Entity> function, Entity entity, CallbackInfoReturnable<Entity> cir) {
    *///?}
        if (ModConfig.enableTracker && world instanceof ServerWorld && entity instanceof MobEntity mobEntity) {
            if (mobEntity.getCustomName() == null || ModConfig.trackCustomNamedMobs) {
                ItemStack item = mobEntity/*? >= 1.21.5 {*/.getMainHandStack()/* } else { *//*.getHandItems().iterator().next()*//* } */;
                if (entity instanceof ZombieEntity zombie && ModConfig.zombiesBlacklist.contains(item.getItem().toString())) {
                    EntityTracker.getServerState(world.getServer()).track(zombie);
                } else if (entity instanceof PiglinEntity piglin && ModConfig.piglinsBlacklist.contains(item.getItem().toString())) {
                    EntityTracker.getServerState(world.getServer()).track(piglin);
                }
            }
        }
    }
}