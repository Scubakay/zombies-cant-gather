package com.scubakay.zombiescantgather.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.UUID;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_ID;

public class EntityTracker extends PersistentState {
    public static final String TRACKED_ENTITIES = "TrackedEntities";
    public static final String TRACKED_COUNT = "TrackedCount";

    private final HashMap<UUID, NbtCompound> trackedEntities = new HashMap<>();

    public void remove(UUID uuid) {
        this.trackedEntities.remove(uuid);
        this.markDirty();
    }

    public void clear() {
        this.trackedEntities.clear();
        this.markDirty();
    }

    public HashMap<UUID, NbtCompound> getTrackedEntities() {
        return this.trackedEntities;
    }

    public int trackEntity(UUID uuid, NbtCompound entity) {
        NbtCompound trackedEntity = this.trackedEntities.get(uuid);
        int count = 0;
        if (trackedEntity != null) {
            count = trackedEntity.getInt(TRACKED_COUNT);
        }
        entity.putInt(TRACKED_COUNT, ++count);
        this.trackedEntities.put(uuid, entity);
        this.markDirty();
        return count;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound entitiesNbt = new NbtCompound();
        this.trackedEntities.forEach((uuid, entity) -> entitiesNbt.put(uuid.toString(), entity));
        nbt.put(TRACKED_ENTITIES, entitiesNbt);
        return nbt;
    }

    public static EntityTracker createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        EntityTracker state = new EntityTracker();
        NbtCompound entitiesNbt = tag.getCompound(TRACKED_ENTITIES);
        entitiesNbt.getKeys().forEach(key -> {
            NbtCompound entityNbt = entitiesNbt.getCompound(key);
            state.trackedEntities.put(UUID.fromString(key), entityNbt);
        });
        return state;
    }

    private static final Type<EntityTracker> type = new Type<>(
            EntityTracker::new,
            EntityTracker::createFromNbt,
            null
    );

    public static EntityTracker getWorldState(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        EntityTracker state = manager.getOrCreate(type, MOD_ID);
        state.markDirty();
        return state;
    }
}
