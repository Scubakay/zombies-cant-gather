package com.scubakay.zombiescantgather.state;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

import static com.scubakay.zombiescantgather.ZombiesCantGather.*;

public class EntityTracker extends PersistentState {
    public static final String TRACKER_KEY = "TrackerEntities";

    private final HashMap<UUID, TrackedEntity> trackedEntities = new HashMap<>();

    public void remove(UUID uuid) {
        this.trackedEntities.remove(uuid);
        this.markDirty();
    }

    public void clear() {
        this.trackedEntities.clear();
        this.markDirty();
    }

    public HashMap<UUID, TrackedEntity> getTrackedEntities() {
        return this.trackedEntities;
    }

    public void trackEntity(MobEntity entity) {
        TrackedEntity trackedEntity = new TrackedEntity(entity);

        final TrackedEntity existingEntity = this.trackedEntities.get(trackedEntity.uuid);
        if (existingEntity != null) {
            trackedEntity.count += existingEntity.count;
        }

        this.trackedEntities.put(trackedEntity.uuid, trackedEntity);
        this.markDirty();
        if (MOD_CONFIG.showTrackerLogs.get()){
            LOGGER.info("Loaded {} {} time(s) holding blacklisted item \"{}\" at {}", trackedEntity.name, trackedEntity.count, trackedEntity.item, trackedEntity.pos.toShortString());
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound entitiesNbt = new NbtCompound();
        this.trackedEntities.forEach((uuid, entity) -> entitiesNbt.put(uuid.toString(), entity.toNbt()));
        nbt.put(TRACKER_KEY, entitiesNbt);
        return nbt;
    }

    public static EntityTracker createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        EntityTracker state = new EntityTracker();
        NbtCompound entitiesNbt = tag.getCompound(TRACKER_KEY);
        entitiesNbt.getKeys().forEach(key -> {
            NbtCompound entityNbt = entitiesNbt.getCompound(key);
            state.trackedEntities.put(UUID.fromString(key), new TrackedEntity(entityNbt));
        });
        return state;
    }

    private static final Type<EntityTracker> type = new Type<>(
            EntityTracker::new,
            EntityTracker::createFromNbt,
            null
    );

    public static EntityTracker getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        EntityTracker state = serverWorld.getPersistentStateManager().getOrCreate(type, MOD_ID);
        state.markDirty();
        return state;
    }
}