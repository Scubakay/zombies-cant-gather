package com.scubakay.zombiescantgather;

import com.scubakay.zombiescantgather.util.MinecraftUtil;
import com.scubakay.zombiescantgather.util.TestUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class ZombiesCantGatherTest implements FabricGameTest {
    private void setupTest(TestContext context) {
        context.killAllEntities();
        ZombiesCantGather.modConfig.reset();
        ZombiesCantGather.modConfig.addItem(MinecraftUtil.itemToString(Items.ROTTEN_FLESH));
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void zombieCanGatherNonListedItem(TestContext context) {
        setupTest(context);
        TestUtil.entityDoesPickUpItem(context, EntityType.ZOMBIE, Items.PAPER);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void zombieCantGatherListedItem(TestContext context) {
        setupTest(context);
        ZombiesCantGather.modConfig.addItem(MinecraftUtil.itemToString(Items.ROTTEN_FLESH));
        TestUtil.entityDoesNotPickUpItem(context, EntityType.ZOMBIE, Items.ROTTEN_FLESH);
        context.complete();
    }
}