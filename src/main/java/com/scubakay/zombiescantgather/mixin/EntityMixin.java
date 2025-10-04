package com.scubakay.zombiescantgather.mixin;

import com.scubakay.zombiescantgather.config.ModConfig;
import com.scubakay.zombiescantgather.state.EntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    //? if >=1.21.9 {
    public abstract World getEntityWorld();
    //?} else {
    /*public abstract World getWorld();
    *///?}

    @Shadow
    public abstract UUID getUuid();

    @Shadow
    public abstract EntityType<?> getType();

    @Inject(method = "setRemoved", at = @At(value = "HEAD", target = "Lnet/minecraft/entity/Entity;stopRiding()V"))
    public void zombiesCantGather$injectDiscard(Entity.RemovalReason reason, CallbackInfo ci) {
        if (ModConfig.enableTracker && !this/*? if >=1.21.9 {*/.getEntityWorld()/*?} else {*//*.getWorld()*//*?}*/.isClient()) {
            // Only remove from tracker if entity is actually killed or discarded, not just unloaded
            if ((this.getType() == EntityType.ZOMBIE || this.getType() == EntityType.PIGLIN) && (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED)) {
                EntityTracker tracker = EntityTracker.getServerState(Objects.requireNonNull(this/*? if >=1.21.9 {*/.getEntityWorld()/*?} else {*//*.getWorld()*//*?}*/.getServer()));
                tracker.remove(this.getUuid());
            }
        }
    }
}