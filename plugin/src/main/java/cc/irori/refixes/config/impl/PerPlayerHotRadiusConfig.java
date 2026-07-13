package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;
import cc.irori.refixes.config.field.ConfigField;

public class PerPlayerHotRadiusConfig extends Configuration<PerPlayerHotRadiusConfig> {

    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Boolean> ENABLED =
            new ConfigurationKey<>("Enabled", ConfigField.BOOLEAN, true);
    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Integer> CHECK_INTERVAL_MS =
            new ConfigurationKey<>("CheckIntervalMs", ConfigField.INTEGER, 5000);
    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Integer> MIN_RADIUS =
            new ConfigurationKey<>("MinRadius", ConfigField.INTEGER, 2);
    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Integer> MAX_RADIUS =
            new ConfigurationKey<>("MaxRadius", ConfigField.INTEGER, 8);
    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Double> TPS_LOW_FRACTION =
            new ConfigurationKey<>("TPSLowFraction", ConfigField.DOUBLE, 0.75);
    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Double> TPS_HIGH_FRACTION =
            new ConfigurationKey<>("TPSHighFraction", ConfigField.DOUBLE, 0.90);

    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Boolean> MEMORY_GUARD_ENABLED =
            new ConfigurationKey<>("MemoryGuardEnabled", ConfigField.BOOLEAN, false);
    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Double> MEMORY_HEAP_THRESHOLD =
            new ConfigurationKey<>("MemoryHeapThreshold", ConfigField.DOUBLE, 0.85);
    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Integer> MEMORY_MIN_VIEW_RADIUS =
            new ConfigurationKey<>("MemoryMinViewRadius", ConfigField.INTEGER, 2);
    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Double> MEMORY_VIEW_DECREASE_FACTOR =
            new ConfigurationKey<>("MemoryViewDecreaseFactor", ConfigField.DOUBLE, 0.75);
    public static final ConfigurationKey<PerPlayerHotRadiusConfig, Integer> MEMORY_RECOVERY_WAIT_SECONDS =
            new ConfigurationKey<>("MemoryRecoveryWaitSeconds", ConfigField.INTEGER, 60);

    private static final PerPlayerHotRadiusConfig INSTANCE = new PerPlayerHotRadiusConfig();

    public PerPlayerHotRadiusConfig() {
        register(
                ENABLED,
                CHECK_INTERVAL_MS,
                MIN_RADIUS,
                MAX_RADIUS,
                TPS_LOW_FRACTION,
                TPS_HIGH_FRACTION,
                MEMORY_GUARD_ENABLED,
                MEMORY_HEAP_THRESHOLD,
                MEMORY_MIN_VIEW_RADIUS,
                MEMORY_VIEW_DECREASE_FACTOR,
                MEMORY_RECOVERY_WAIT_SECONDS);
    }

    public static PerPlayerHotRadiusConfig get() {
        return INSTANCE;
    }
}
