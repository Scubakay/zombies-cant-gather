package com.scubakay.zombiescantgather.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.UUID;

public class TrackedEntity {
    public static final Codec<TrackedEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(TrackedEntity::getUuid),
            Codec.STRING.fieldOf("name").forGetter(TrackedEntity::getName),
            Codec.STRING.fieldOf("item").forGetter(TrackedEntity::getItem),
            BlockPos.CODEC.fieldOf("pos").forGetter(TrackedEntity::getPos),
            Codec.STRING.fieldOf("dimension").forGetter(TrackedEntity::getDimension),
            Codec.INT.fieldOf("count").forGetter(TrackedEntity::getCount)
            ).apply(instance, TrackedEntity::new)
    );

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
        dimension = entity.getWorld().getDimensionEntry().getIdAsString();
        count = 1;
    }

    public TrackedEntity(UUID uuid, String name, String item, BlockPos pos, String dimension, int count) {
        this.uuid = uuid;
        this.name = name;
        this.item = item;
        this.pos = pos;
        this.dimension = dimension;
        this.count = count;
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
}
