package com.scubakay.zombiescantgather.util;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.test.PositionedException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TestUtil {
    /**
     * Spawn a Zombie and an item on the same block and expect the zombie to pick up the item.
     */
    public static void entityDoesPickUpItem(TestContext context, EntityType entity, Item item) {
        BlockPos blockPos = new BlockPos(0, 1, 0);
        context.setBlockState(blockPos, Blocks.SMOOTH_STONE);

        BlockPos mobPos = blockPos.up(1);
        MobEntity mob = context.spawnMob(entity, mobPos);
        mob.setCanPickUpLoot(true);

        ItemEntity itemEntity = context.spawnItem(item, mobPos);

        context.waitAndRun(20, () -> {
            context.dontExpectItemAt(item, mobPos, 1);
            context.expectEntityHoldingItem(mobPos, entity, item);
        });
        context.killEntity(mob);
        context.killEntity(itemEntity);
    }

    /**
     * Spawn a Zombie and an item on the same block and expect the zombie to not pick up the item.
     */
    public static void entityDoesNotPickUpItem(TestContext context, EntityType entity, Item item) {
        BlockPos blockPos = new BlockPos(0, 1, 0);
        context.setBlockState(blockPos, Blocks.SMOOTH_STONE);

        BlockPos mobPos = blockPos.up(1);
        MobEntity mob = context.spawnMob(entity, mobPos);
        mob.setCanPickUpLoot(true);

        ItemEntity itemEntity = context.spawnItem(item, mobPos);

        context.waitAndRun(20, () -> {
            context.expectItemAt(item, mobPos, 1);
            dontExpectEntityHoldingItem(context, mobPos, entity, item);
        });
        context.killEntity(mob);
        context.killEntity(itemEntity);
    }

    /**
     * The zombie should not be holding the item
     */
    public static <E extends LivingEntity> void dontExpectEntityHoldingItem(TestContext context, BlockPos pos, EntityType<E> entityType, Item item) {
        BlockPos blockPos = context.getAbsolutePos(pos);
        List<E> list = context.getWorld().getEntitiesByType(entityType, new Box(blockPos), Entity::isAlive);
        if (list.isEmpty()) {
            throw new PositionedException("Expected entity of type: " + entityType, blockPos, pos, context.getTick());
        } else {
            for (E livingEntity : list) {
                if (livingEntity.isHolding(item)) {
                    throw new PositionedException("Entity should not be holding: " + item, blockPos, pos, context.getTick());
                }
            }
        }
    }
}
