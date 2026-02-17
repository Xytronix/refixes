package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;
import cc.irori.refixes.config.field.ConfigField;

public class TickSleepOptimizationConfig extends Configuration<TickSleepOptimizationConfig> {

    public static final ConfigurationKey<TickSleepOptimizationConfig, Boolean> ENABLED =
            new ConfigurationKey<>("Enabled", ConfigField.BOOLEAN, true);
    public static final ConfigurationKey<TickSleepOptimizationConfig, Long> SPIN_THRESHOLD_NANOS =
            new ConfigurationKey<>("SpinThresholdNanos", ConfigField.LONG, 500_000L); // 0.5 ms

    private static final TickSleepOptimizationConfig INSTANCE = new TickSleepOptimizationConfig();

    public TickSleepOptimizationConfig() {
        register(ENABLED, SPIN_THRESHOLD_NANOS);
    }

    public static TickSleepOptimizationConfig get() {
        return INSTANCE;
    }
}
