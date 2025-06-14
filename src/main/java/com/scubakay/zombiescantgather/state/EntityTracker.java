package com.scubakay.zombiescantgather.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scubakay.zombiescantgather.command.PermissionManager;
import com.scubakay.zombiescantgather.command.TrackerCommand;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.scubakay.zombiescantgather.ZombiesCantGather.*;
import static com.scubakay.zombiescantgather.command.PermissionManager.hasPermission;

public class EntityTracker extends PersistentState {
    public static final Codec<EntityTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Uuids.CODEC, TrackedEntity.CODEC).fieldOf("tracker_entities").forGetter(EntityTracker::get)
    ).apply(instance, EntityTracker::new));

    private final Map<UUID, TrackedEntity> trackedEntities;

    public EntityTracker() {
        this(new HashMap<>());
    }

    public EntityTracker(Map<UUID, TrackedEntity> trackedEntities) {
        this.trackedEntities = trackedEntities;
    }

    public Map<UUID, TrackedEntity> get() {
        return this.trackedEntities;
    }

    public TrackedEntity get(UUID uuid) {
        return this.trackedEntities.get(uuid);
    }

    public void remove(UUID uuid) {
        this.trackedEntities.remove(uuid);
        this.markDirty();
    }

    public void clear() {
        this.trackedEntities.clear();
        this.markDirty();
    }

    public void track(MobEntity entity) {
        TrackedEntity trackedEntity = new TrackedEntity(entity);

        final TrackedEntity existingEntity = this.trackedEntities.get(trackedEntity.getUuid());
        if (existingEntity != null) {
            trackedEntity.addCount(existingEntity.getCount());
        }

        this.trackedEntities.put(trackedEntity.getUuid(), trackedEntity);
        this.markDirty();
        if (MOD_CONFIG.showTrackerLogs.get()) {
            LOGGER.info("Loaded {} {} time(s) holding blacklisted item \"{}\" at {}", trackedEntity.getName(), trackedEntity.getCount(), trackedEntity.getItem(), trackedEntity.getPos().toShortString());
        }
        if (MOD_CONFIG.broadcastTrackedMobs.get()){
            Objects.requireNonNull(entity.getServer()).getPlayerManager().getPlayerList().stream()
                    .filter(player -> hasPermission(player, PermissionManager.TRACKER_LOG_PERMISSION))
                    .forEach(player -> player.getCommandSource().sendMessage(TrackerCommand.getTrackerRow(player.getCommandSource(), trackedEntity)));
        }
    }

    static String getSaveKey(String namespace) {
        return "zombies_cant_gather_" + namespace;
    }

    public static PersistentStateType<EntityTracker> createStateType(String id) {
        return new PersistentStateType<>(EntityTracker.getSaveKey(id), EntityTracker::new, CODEC, null);
    }

    public static EntityTracker getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        EntityTracker state = serverWorld.getPersistentStateManager().getOrCreate(createStateType("entity_tracker"));
        state.markDirty();
        return state;
    }
}