package com.scubakay.zombiescantgather.util;

import java.nio.file.Path;

import static com.scubakay.zombiescantgather.ZombiesCantGather.MOD_ID;

public abstract class SystemUtil {
    public static Path getConfigDirectory() {
        return Path.of(".").resolve("config").resolve(MOD_ID);
    }
    public static Path getConfigFile() {
        return getConfigDirectory().resolve("mod.properties");
    }
}
