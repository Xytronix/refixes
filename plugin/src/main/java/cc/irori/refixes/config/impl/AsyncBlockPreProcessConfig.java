package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;
import cc.irori.refixes.config.field.ConfigField;

public class AsyncBlockPreProcessConfig extends Configuration<AsyncBlockPreProcessConfig> {

    public static final ConfigurationKey<AsyncBlockPreProcessConfig, Boolean> ASYNC_BLOCK_PRE_PROCESS =
            new ConfigurationKey<>("AsyncBlockPreProcess", ConfigField.BOOLEAN, true);
    public static final ConfigurationKey<AsyncBlockPreProcessConfig, Integer> MAX_CHUNKS_PER_SECOND =
            new ConfigurationKey<>("MaxChunksPerSecond", ConfigField.INTEGER, 36);
    public static final ConfigurationKey<AsyncBlockPreProcessConfig, Integer> MAX_CHUNKS_PER_TICK =
            new ConfigurationKey<>("MaxChunksPerTick", ConfigField.INTEGER, 4);

    private static final AsyncBlockPreProcessConfig INSTANCE = new AsyncBlockPreProcessConfig();

    public AsyncBlockPreProcessConfig() {
        register(ASYNC_BLOCK_PRE_PROCESS, MAX_CHUNKS_PER_SECOND, MAX_CHUNKS_PER_TICK);
    }

    public static AsyncBlockPreProcessConfig get() {
        return INSTANCE;
    }
}
