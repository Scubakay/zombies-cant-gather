package com.scubakay.zombiescantgather;

import com.scubakay.zombiescantgather.command.ZombiesCantGatherCommand;
import com.scubakay.zombiescantgather.config.ModConfig;
import de.maxhenkel.configbuilder.ConfigBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ZombiesCantGather implements ModInitializer {
    public static final String MOD_ID = "zombiescantgather";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "0.1.0";

    public static ModConfig modConfig;

    @Override
    public void onInitialize() {
        modConfig = ConfigBuilder.builder(ModConfig::new)
                .path(getConfigFile())
                .strict(true)
                .saveAfterBuild(true)
                .build();

        CommandRegistrationCallback.EVENT.register(ZombiesCantGatherCommand::register);
    }

    public Path getConfigDirectory() {
        return Path.of(".").resolve("config").resolve(MOD_ID);
    }
    public Path getConfigFile() {
        return getConfigDirectory().resolve("mod.properties");
    }
}