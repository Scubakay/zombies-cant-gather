package com.scubakay.zombiescantgather;

import com.scubakay.zombiescantgather.command.RootCommand;
import com.scubakay.zombiescantgather.command.TrackerCommand;
import com.scubakay.zombiescantgather.command.BlacklistCommand;
import com.scubakay.zombiescantgather.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZombiesCantGather implements ModInitializer {
    public static final String MOD_ID = "zombiescantgather";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfig MOD_CONFIG;

    public static String getSaveKey(String namespace) {
        return "zombiescantgather_" + namespace;
    }

    @Override
    public void onInitialize() {
        MOD_CONFIG = ModConfig.getModConfig();
        CommandRegistrationCallback.EVENT.register(RootCommand::register);
        CommandRegistrationCallback.EVENT.register(BlacklistCommand::register);
        CommandRegistrationCallback.EVENT.register(TrackerCommand::register);
    }
}