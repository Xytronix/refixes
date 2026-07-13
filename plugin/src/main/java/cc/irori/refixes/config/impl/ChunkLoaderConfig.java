package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;
import cc.irori.refixes.config.field.ConfigField;

public class ChunkLoaderConfig extends Configuration<ChunkLoaderConfig> {

    public static final ConfigurationKey<ChunkLoaderConfig, Boolean> ENABLED =
            new ConfigurationKey<>("Enabled", ConfigField.BOOLEAN, true);

    private static final ChunkLoaderConfig INSTANCE = new ChunkLoaderConfig();

    public ChunkLoaderConfig() {
        register(ENABLED);
    }

    public static ChunkLoaderConfig get() {
        return INSTANCE;
    }
}
