package com.scubakay.zombiescantgather;

import com.scubakay.zombiescantgather.util.TestUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_CONFIG;

public class ZombiesCantGatherTest implements FabricGameTest {
    private void setupTest(TestContext context) {
        context.killAllEntities();
        MOD_CONFIG.reset();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void zombieCantGatherItemAfterListing(TestContext context) {
        setupTest(context);
        TestUtil.entityDoesPickUpItem(context, EntityType.ZOMBIE, Items.ROTTEN_FLESH);
        context.killAllEntities();
        MOD_CONFIG.addZombieItem(Items.ROTTEN_FLESH.toString());
        TestUtil.entityDoesNotPickUpItem(context, EntityType.ZOMBIE, Items.ROTTEN_FLESH);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void zombieCanGatherItemAfterUnlisting(TestContext context) {
        setupTest(context);
        MOD_CONFIG.addZombieItem(Items.ROTTEN_FLESH.toString());
        TestUtil.entityDoesNotPickUpItem(context, EntityType.ZOMBIE, Items.ROTTEN_FLESH);
        MOD_CONFIG.removeZombieItem(Items.ROTTEN_FLESH.toString());
        TestUtil.entityDoesPickUpItem(context, EntityType.ZOMBIE, Items.ROTTEN_FLESH);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void piglinCantGatherItemAfterListing(TestContext context) {
        setupTest(context);
        TestUtil.entityDoesPickUpItem(context, EntityType.PIGLIN, Items.WOODEN_AXE);
        MOD_CONFIG.addPiglinItem(Items.WOODEN_AXE.toString());
        TestUtil.entityDoesNotPickUpItem(context, EntityType.PIGLIN, Items.WOODEN_AXE);
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void piglinCanGatherItemAfterUnlisting(TestContext context) {
        setupTest(context);
        MOD_CONFIG.addPiglinItem(Items.ROTTEN_FLESH.toString());
        TestUtil.entityDoesNotPickUpItem(context, EntityType.PIGLIN, Items.ROTTEN_FLESH);
        MOD_CONFIG.removePiglinItem(Items.ROTTEN_FLESH.toString());
        TestUtil.entityDoesPickUpItem(context, EntityType.PIGLIN, Items.ROTTEN_FLESH);
        context.complete();
    }
}