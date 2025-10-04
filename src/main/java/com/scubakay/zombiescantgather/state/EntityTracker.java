package com.scubakay.zombiescantgather.state;

import com.mojang.brigadier.context.CommandContext;
import com.scubakay.zombiescantgather.ZombiesCantGather;
import com.scubakay.zombiescantgather.command.PermissionManager;
import com.scubakay.zombiescantgather.command.TrackerCommand;
import com.scubakay.zombiescantgather.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

//? if >=1.21.5 {
import net.minecraft.world.PersistentStateType;
import net.minecraft.util.Uuids;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
//?} else {
/*import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
*///?}

import java.util.*;

import static com.scubakay.zombiescantgather.ZombiesCantGather.*;
import static com.scubakay.zombiescantgather.command.PermissionManager.hasPermission;

public class EntityTracker extends PersistentState {
    private final Map<UUID, TrackedEntity> entities;

    public EntityTracker() {
        this(new HashMap<>());
    }

    public EntityTracker(Map<UUID, TrackedEntity> entities) {
        // Ensure the map is always mutable
        this.entities = new HashMap<>(entities);
    }

    public static EntityTracker getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        EntityTracker state = getState(serverWorld);
        state.markDirty();
        return state;
    }

    public Map<UUID, TrackedEntity> get() {
        return this.entities;
    }

    public List<TrackedEntity> getList() {
        return this.entities
                .values()
                .stream()
                .sorted(Comparator.comparingInt(x -> -x.getCount()))
                .toList();
    }

    public TrackedEntity get(UUID uuid) {
        return this.entities.get(uuid);
    }

    public void remove(UUID uuid) {
        this.entities.remove(uuid);
        this.markDirty();
    }

    public void clear() {
        this.entities.clear();
        this.markDirty();
    }

    public void track(MobEntity entity) {
        if (!entityNeedsTracking(entity)) return;

        TrackedEntity trackedEntity = new TrackedEntity(entity);

        final TrackedEntity existingEntity = this.entities.get(trackedEntity.getUuid());
        if (existingEntity != null) {
            trackedEntity.addCount(existingEntity.getCount());
        }

        this.entities.put(trackedEntity.getUuid(), trackedEntity);
        this.markDirty();
        if (ModConfig.showTrackerLogs) {
            LOGGER.info("Loaded {} {} time(s) holding blacklisted item \"{}\" at {}", trackedEntity.getName(), trackedEntity.getCount(), trackedEntity.getItem(), trackedEntity.getPos().toShortString());
        }
        if (ModConfig.broadcastTrackedMobs){
            Objects.requireNonNull(entity./*? if >= 1.21.9 {*/getEntityWorld()./*?}*/getServer()).getPlayerManager().getPlayerList().stream()
                    .filter(player -> hasPermission(player, PermissionManager.TRACKER_LOG_PERMISSION))
                    .forEach(player -> player.getCommandSource().sendMessage(TrackerCommand.getTrackerRow(trackedEntity)));
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean entityNeedsTracking(MobEntity entity) {
        if (entity.getCustomName() == null || ModConfig.trackCustomNamedMobs) {
            ItemStack item = entity/*? if >= 1.21.5 {*/.getMainHandStack()/*?} else {*//*.getHandItems().iterator().next()*//*?}*/;
            if (entity instanceof ZombieEntity && ModConfig.zombiesBlacklist.contains(item.getItem().toString())) {
                return true;
            } else if (entity instanceof PiglinEntity && ModConfig.piglinsBlacklist.contains(item.getItem().toString())) {
                return true;
            }
        }
        return false;
    }

    public List<TrackedEntity> purge(CommandContext<ServerCommandSource> ctx) {
        List<UUID> toRemove = new ArrayList<>();
        this.entities.forEach((uuid, trackedEntity) -> {
            Entity entity = ctx.getSource().getWorld().getEntity(uuid);
            if(entity != null) {
                //? if >= 1.21.2 {
                entity.kill(ctx.getSource().getWorld());
                //?} else {
                /*entity.kill();
                *///?}
                toRemove.add(uuid);
            } else {
                LOGGER.info("Could not find entity {} at {}", trackedEntity.getName(), trackedEntity.getPos().toShortString());
            }
        });
        toRemove.forEach(this.entities::remove);
        this.markDirty();
        return getList();
    }

    //? if >=1.21.5 {
    public static final Codec<EntityTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Uuids.CODEC, TrackedEntity.CODEC).fieldOf(TrackedEntity.TrackerKeys.HASH_MAP).forGetter(EntityTracker::get)
    ).apply(instance, EntityTracker::new));

    public static PersistentStateType<EntityTracker> createStateType(String id) {
        return new PersistentStateType<>(ZombiesCantGather.getSaveKey(id), EntityTracker::new, CODEC, null);
    }

    private static EntityTracker getState(ServerWorld serverWorld) {
        return serverWorld.getPersistentStateManager().getOrCreate(createStateType("entity_tracker"));
    }
    //?} else {
    /*@Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound entitiesNbt = new NbtCompound();
        this.entities.forEach((uuid, entity) -> entitiesNbt.put(uuid.toString(), entity.toNbt()));
        nbt.put(TrackedEntity.TrackerKeys.HASH_MAP, entitiesNbt);
        return nbt;
    }

    public static EntityTracker createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup ignoredRegistryLookup) {
        EntityTracker state = new EntityTracker();
        NbtCompound entitiesNbt = tag.getCompound(TrackedEntity.TrackerKeys.HASH_MAP);
        entitiesNbt.getKeys().forEach(key -> {
            NbtCompound entityNbt = entitiesNbt.getCompound(key);
            state.entities.put(UUID.fromString(key), new TrackedEntity(entityNbt));
        });
        return state;
    }

    private static final Type<EntityTracker> type = new Type<>(
            EntityTracker::new,
            EntityTracker::createFromNbt,
            null
    );

    private static EntityTracker getState(ServerWorld serverWorld) {
        return serverWorld.getPersistentStateManager().getOrCreate(type, MOD_ID);
    }
    *///?}
}