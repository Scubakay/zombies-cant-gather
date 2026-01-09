package com.scubakay.zombiescantgather.state;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;

//? if >=1.21.5 {
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;
//?} else {
/*import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
*///?}

import java.util.UUID;

public class TrackedEntity {
    private final UUID uuid;
    private final String name;
    private final String item;
    private final BlockPos pos;
    private final String dimension;
    private int count;

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getItem() {
        return item;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getDimension() {
        return dimension;
    }

    public int getCount() {
        return count;
    }

    public void addCount(int i) {
        count += i;
    }

    public TrackedEntity(MobEntity entity) {
        uuid = entity.getUuid();
        name = entity.getName().getString();
        item = entity.getMainHandStack().getItem().asItem().toString();
        pos = entity.getBlockPos();

        //? if >=1.21.9 {
        dimension = entity.getEntityWorld().getDimensionEntry().getIdAsString();
        //?} else {
        /*dimension = entity.getWorld().getDimensionEntry().getIdAsString();
        *///?}
        count = 1;
    }

    public boolean isInDimensionType(Identifier type) {
        return dimension.equals(type.toString());
    }

    public String getDimensionName() {
        if (isInDimensionType(DimensionTypes.OVERWORLD.getValue())) {
            return "Overworld";
        } else if (isInDimensionType(DimensionTypes.THE_NETHER.getValue())) {
            return "Nether";
        } else if (isInDimensionType(DimensionTypes.THE_END.getValue())) {
            return "End";
        } else {
            return dimension;
        }
    }

    public int getDimensionColor() {
        if (isInDimensionType(DimensionTypes.OVERWORLD.getValue())) {
            return Colors.GREEN;
        } else if (isInDimensionType(DimensionTypes.THE_NETHER.getValue())) {
            return Colors.RED;
        } else if (isInDimensionType(DimensionTypes.THE_END.getValue())) {
            return Colors.YELLOW;
        } else {
            return Colors.WHITE;
        }
    }

    //? if >=1.21.5 {
    public TrackedEntity(UUID uuid, String name, String item, BlockPos pos, String dimension, int count) {
        this.uuid = uuid;
        this.name = name;
        this.item = item;
        this.pos = pos;
        this.dimension = dimension;
        this.count = count;
    }

    public static final Codec<TrackedEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Uuids.CODEC.fieldOf(TrackerKeys.UUID).forGetter(TrackedEntity::getUuid),
                    Codec.STRING.fieldOf(TrackerKeys.NAME).forGetter(TrackedEntity::getName),
                    Codec.STRING.fieldOf(TrackerKeys.ITEM).forGetter(TrackedEntity::getItem),
                    BlockPos.CODEC.fieldOf(TrackerKeys.POS).forGetter(TrackedEntity::getPos),
                    Codec.STRING.fieldOf(TrackerKeys.DIMENSION).forGetter(TrackedEntity::getDimension),
                    Codec.INT.fieldOf(TrackerKeys.COUNT).forGetter(TrackedEntity::getCount)
            ).apply(instance, TrackedEntity::new)
    );

    //?} else {
    /*public TrackedEntity(NbtCompound nbt) {
        this.uuid = nbt.getUuid(TrackerKeys.UUID);
        this.name = nbt.getString(TrackerKeys.NAME);
        this.item = nbt.getString(TrackerKeys.ITEM);
        this.pos = nbtToBlockPos(nbt);
        this.dimension = nbt.getString(TrackerKeys.DIMENSION);
        this.count = nbt.getInt(TrackerKeys.COUNT);
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

    private @NotNull BlockPos nbtToBlockPos(NbtCompound nbt) {
        NbtCompound posNbt = nbt.getCompound(TrackerKeys.POS);
        return new BlockPos(
                posNbt.getInt("x"),
                posNbt.getInt("y"),
                posNbt.getInt("z")
        );
    }

    private @NotNull NbtCompound blockPosToNbt() {
        NbtCompound posNbt = new NbtCompound();
        posNbt.putInt("x", pos.getX());
        posNbt.putInt("y", pos.getY());
        posNbt.putInt("z", pos.getZ());
        return posNbt;
    }
    *///?}

    public static class TrackerKeys {
        public static final String UUID = "Uuid";
        public static final String NAME = "Name";
        public static final String ITEM = "Item";
        public static final String POS = "Position";
        public static final String COUNT = "Count";
        public static final String DIMENSION = "Dimension";
        public static final String HASH_MAP = "Mobs";
    }
}
