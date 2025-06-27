package com.scubakay.zombiescantgather.mixin;

import com.scubakay.zombiescantgather.config.ModConfig;
import com.scubakay.zombiescantgather.state.EntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
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
        if (ModConfig.enableTracker && world instanceof ServerWorld && entity instanceof MobEntity mob) {
            EntityTracker tracker = EntityTracker.getServerState(Objects.requireNonNull(mob.getServer()));
            tracker.track(mob);
        }
    }
}