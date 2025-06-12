package com.scubakay.zombiescantgather.mixin;

import com.scubakay.zombiescantgather.state.EntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract UUID getUuid();

    @Inject(method = "setRemoved", at = @At(value = "HEAD", target = "Lnet/minecraft/entity/Entity;stopRiding()V"))
    public void zombiesCantGather$injectDiscard(Entity.RemovalReason reason, CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            EntityTracker tracker = EntityTracker.getWorldState((ServerWorld) this.getWorld());
            tracker.remove(this.getUuid());
        }
    }
}
