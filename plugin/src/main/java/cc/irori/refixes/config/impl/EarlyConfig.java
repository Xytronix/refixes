package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;
import cc.irori.refixes.config.field.ConfigField;

public class EarlyConfig extends Configuration<EarlyConfig> {

    public static final ConfigurationKey<EarlyConfig, Boolean> DISABLE_FLUID_PRE_PROCESS =
            new ConfigurationKey<>("DisableFluidPreProcess", ConfigField.BOOLEAN, true);

    private static final EarlyConfig INSTANCE = new EarlyConfig();

    public EarlyConfig() {
        register(DISABLE_FLUID_PRE_PROCESS);
    }

    public static EarlyConfig get() {
        return INSTANCE;
    }
}
