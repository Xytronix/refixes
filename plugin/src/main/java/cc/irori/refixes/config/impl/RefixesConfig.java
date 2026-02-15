package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;

public class RefixesConfig extends Configuration {

    private static final RefixesConfig INSTANCE = new RefixesConfig();

    private static final ConfigurationKey<RefixesConfig, SanitizerConfig> SANITIZER_CONFIG =
            ConfigurationKey.subConfig("SanitizerConfig", SanitizerConfig.getInstance());

    public RefixesConfig() {
        register(SANITIZER_CONFIG);
    }

    public static RefixesConfig getInstance() {
        return INSTANCE;
    }
}
