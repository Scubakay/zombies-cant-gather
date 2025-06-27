package com.scubakay.zombiescantgather.mixin;

import com.scubakay.zombiescantgather.config.ModConfig;
import com.scubakay.zombiescantgather.state.EntityTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin {
    @Inject(method = "unload(Lnet/minecraft/world/entity/EntityLike;)V", at = @At("HEAD"))
    private void zombiesCantGather$injectTrackUnloadedEntities(EntityLike entity, CallbackInfo ci) {
        if (ModConfig.enableTracker && entity instanceof MobEntity mob) {
            EntityTracker tracker = EntityTracker.getServerState(Objects.requireNonNull(mob.getServer()));
            tracker.track(mob);
        }
    }
}
