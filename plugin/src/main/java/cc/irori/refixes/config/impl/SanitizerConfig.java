package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;
import cc.irori.refixes.config.field.ConfigField;

public class SanitizerConfig extends Configuration {

    private static final SanitizerConfig INSTANCE = new SanitizerConfig();

    public static final ConfigurationKey<SanitizerConfig, Boolean> DEFAULT_WORLD_RECOVERY =
            new ConfigurationKey<>("DefaultWorldRecovery", ConfigField.BOOLEAN, true);

    static SanitizerConfig getInstance() {
        return INSTANCE;
    }
}
