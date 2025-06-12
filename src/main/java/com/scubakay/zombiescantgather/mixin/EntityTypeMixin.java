package com.scubakay.zombiescantgather.mixin;

import com.scubakay.zombiescantgather.state.EntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
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

import static com.scubakay.zombiescantgather.ZombiesCantGather.LOGGER;
import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;

@Mixin(EntityType.class)
public class EntityTypeMixin {
    @Inject(method = "method_17843", at = @At("RETURN"))
    private static void zombiesCantGather$checkForbiddenItems(NbtCompound nbtCompound, World world, SpawnReason spawnReason, Function<Entity, Entity> function, Entity entity, CallbackInfoReturnable<Entity> cir) {
        if (entity instanceof ZombieEntity zombie) {
            ItemStack item = zombie.getHandItems().iterator().next();
            if (!world.isClient() && MOD_CONFIG.zombiesCantGather.get().contains(item.getItem().toString())) {
                EntityTracker state = EntityTracker.getWorldState((ServerWorld) world);
                int count = state.trackEntity(entity.getUuid(), nbtCompound);
                state.markDirty();
                LOGGER.info("Found zombie {} time(s) with blacklisted item \"{}\" at position {}", count, item.getItem(), zombie.getPos());
            }
        } else if (entity instanceof PiglinEntity piglin) {
            ItemStack item = piglin.getHandItems().iterator().next();
            if (!world.isClient() && MOD_CONFIG.zombiesCantGather.get().contains(item.getItem().toString())) {
                EntityTracker state = EntityTracker.getWorldState((ServerWorld) world);
                int count = state.trackEntity(entity.getUuid(), nbtCompound);
                state.markDirty();
                LOGGER.info("Found piglin {} time(s) with blacklisted item \"{}\" at position {}", count, item.getItem(), piglin.getPos());
            }
        }
    }
}
