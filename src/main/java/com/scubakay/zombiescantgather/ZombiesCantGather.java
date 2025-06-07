package com.scubakay.zombiescantgather;

import com.scubakay.zombiescantgather.command.PiglinsCantGatherCommand;
import com.scubakay.zombiescantgather.command.ZombiesCantGatherCommand;
import com.scubakay.zombiescantgather.command.ZombiesCantGatherPermissionsManager;
import com.scubakay.zombiescantgather.config.ModConfig;
import de.maxhenkel.admiral.MinecraftAdmiral;
import de.maxhenkel.configbuilder.ConfigBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ZombiesCantGather implements ModInitializer {
    public static final String MOD_ID = "zombiescantgather";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfig MOD_CONFIG;
    public static ZombiesCantGatherPermissionsManager PERMISSIONS_MANAGER;

    @Override
    public void onInitialize() {
        MOD_CONFIG = ConfigBuilder.builder(ModConfig::new)
                .path(getConfigFile())
                .strict(true)
                .saveAfterBuild(true)
                .build();

        PERMISSIONS_MANAGER = new ZombiesCantGatherPermissionsManager();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MinecraftAdmiral.builder(dispatcher, registryAccess)
                .addCommandClasses(
                        ZombiesCantGatherCommand.class
                )
                .setPermissionManager(PERMISSIONS_MANAGER)
                .build());
    }

    public Path getConfigDirectory() {
        return Path.of(".").resolve("config").resolve(MOD_ID);
    }
    public Path getConfigFile() {
        return getConfigDirectory().resolve("mod.properties");
    }
}