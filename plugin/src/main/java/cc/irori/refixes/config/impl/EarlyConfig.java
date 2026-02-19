package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;
import cc.irori.refixes.config.field.ConfigField;

public class EarlyConfig extends Configuration<EarlyConfig> {

    private static final ConfigurationKey<EarlyConfig, TickSleepOptimizationConfig> TICK_SLEEP_OPTIMIZATION_CONFIG =
            ConfigurationKey.subConfig("TickSleepOptimization", TickSleepOptimizationConfig.get());
    private static final ConfigurationKey<EarlyConfig, CylinderVisibilityConfig> CYLINDER_VISIBILITY_CONFIG =
            ConfigurationKey.subConfig("CylinderVisibility", CylinderVisibilityConfig.get());
    private static final ConfigurationKey<EarlyConfig, KDTreeOptimizationConfig> KDTREE_OPTIMIZATION_CONFIG =
            ConfigurationKey.subConfig("KDTreeOptimization", KDTreeOptimizationConfig.get());
    private static final ConfigurationKey<EarlyConfig, AsyncBlockPreProcessConfig> ASYNC_BLOCK_PRE_PROCESS_CONFIG =
            ConfigurationKey.subConfig("AsyncBlockPreProcess", AsyncBlockPreProcessConfig.get());

    public static final ConfigurationKey<EarlyConfig, Boolean> DISABLE_FLUID_PRE_PROCESS =
            new ConfigurationKey<>("DisableFluidPreProcess", ConfigField.BOOLEAN, true);
    public static final ConfigurationKey<EarlyConfig, Boolean> FORCE_SKIP_MOD_VALIDATION =
            new ConfigurationKey<>("ForceSkipModValidation", ConfigField.BOOLEAN, false);

    private static final EarlyConfig INSTANCE = new EarlyConfig();

    public EarlyConfig() {
        register(
                TICK_SLEEP_OPTIMIZATION_CONFIG,
                CYLINDER_VISIBILITY_CONFIG,
                KDTREE_OPTIMIZATION_CONFIG,
                ASYNC_BLOCK_PRE_PROCESS_CONFIG,
                DISABLE_FLUID_PRE_PROCESS,
                FORCE_SKIP_MOD_VALIDATION);
    }

    public static EarlyConfig get() {
        return INSTANCE;
    }
}
