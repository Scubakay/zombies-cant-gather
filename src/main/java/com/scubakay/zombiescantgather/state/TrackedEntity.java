package com.scubakay.zombiescantgather.state;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TrackedEntity {
    public UUID uuid;
    public String name;
    public String item;
    public BlockPos pos;
    public String dimension;
    public int count;

    public TrackedEntity(MobEntity entity) {
        uuid = entity.getUuid();
        name = entity.getName().getLiteralString();
        item = entity.getHandItems().iterator().next().getItem().asItem().toString();
        pos = entity.getBlockPos();
        dimension = entity.getWorld().getDimensionEntry().getIdAsString();
        count = 1;
    }

    public TrackedEntity(NbtCompound nbt) {
        this.uuid = nbt.getUuid(TrackerKeys.UUID);
        this.name = nbt.getString(TrackerKeys.NAME);
        this.item = nbt.getString(TrackerKeys.ITEM);
        this.pos = nbtToBlockPos(nbt);
        this.dimension = nbt.getString(TrackerKeys.DIMENSION);
        this.count = nbt.getInt(TrackerKeys.COUNT);
    }

    private @NotNull BlockPos nbtToBlockPos(NbtCompound nbt) {
        NbtCompound posNbt = nbt.getCompound(TrackerKeys.POS);
        return new BlockPos(
            posNbt.getInt("x"),
            posNbt.getInt("y"),
            posNbt.getInt("z")
        );
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid(TrackerKeys.UUID, uuid);
        nbt.putString(TrackerKeys.NAME, name);
        nbt.putString(TrackerKeys.ITEM, item);
        nbt.put(TrackerKeys.POS, blockPosToNbt());
        nbt.putString(TrackerKeys.DIMENSION, dimension);
        nbt.putInt(TrackerKeys.COUNT, count);
        return nbt;
    }

    private @NotNull NbtCompound blockPosToNbt() {
        NbtCompound posNbt = new NbtCompound();
        posNbt.putInt("x", pos.getX());
        posNbt.putInt("y", pos.getY());
        posNbt.putInt("z", pos.getZ());
        return posNbt;
    }

    public boolean isInDimensionType(Identifier type) {
        return dimension.equals(type.toString());
    }

    public String getDimensionName() {
        if (isInDimensionType(DimensionTypes.OVERWORLD_ID)) {
            return "Overworld";
        } else if (isInDimensionType(DimensionTypes.THE_NETHER_ID)) {
            return "Nether";
        } else if (isInDimensionType(DimensionTypes.THE_END_ID)) {
            return "End";
        } else {
            return dimension;
        }
    }

    public int getDimensionColor() {
        if (isInDimensionType(DimensionTypes.OVERWORLD_ID)) {
            return Colors.GREEN;
        } else if (isInDimensionType(DimensionTypes.THE_NETHER_ID)) {
            return Colors.RED;
        } else if (isInDimensionType(DimensionTypes.THE_END_ID)) {
            return Colors.YELLOW;
        } else {
            return Colors.WHITE;
        }
    }

    public static class TrackerKeys {
        public static final String UUID = "Uuid";
        public static final String NAME = "Name";
        public static final String ITEM = "Item";
        public static final String POS = "Position";
        public static final String COUNT = "Count";
        public static final String DIMENSION = "Dimension";
    }
}
