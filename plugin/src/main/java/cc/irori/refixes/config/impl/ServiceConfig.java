package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;

public class ServiceConfig extends Configuration<ServiceConfig> {

    private static final ConfigurationKey<ServiceConfig, PerPlayerHotRadiusConfig> PER_PLAYER_RADIUS_CONFIG =
            ConfigurationKey.subConfig("PerPlayerHotRadius", PerPlayerHotRadiusConfig.get());

    private static final ServiceConfig INSTANCE = new ServiceConfig();

    public ServiceConfig() {
        register(PER_PLAYER_RADIUS_CONFIG);
    }

    public static ServiceConfig get() {
        return INSTANCE;
    }
}
