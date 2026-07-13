package cc.irori.refixes.config.impl;

import cc.irori.refixes.config.Configuration;
import cc.irori.refixes.config.ConfigurationKey;
import cc.irori.refixes.config.field.ConfigField;

public class ListenerConfig extends Configuration<ListenerConfig> {

    public static final ConfigurationKey<ListenerConfig, Boolean> UNKNOWN_BLOCK_CLEANER =
            new ConfigurationKey<>("UnknownBlockCleaner", ConfigField.BOOLEAN, false);
    public static final ConfigurationKey<ListenerConfig, Boolean> UNKNOWN_BLOCK_CLEANER_SCAN_FLUIDS =
            new ConfigurationKey<>("UnknownBlockCleanerScanFluids", ConfigField.BOOLEAN, false);
    public static final ConfigurationKey<ListenerConfig, Boolean> UNKNOWN_BLOCK_CLEANER_SCAN_CONTAINERS =
            new ConfigurationKey<>("UnknownBlockCleanerScanContainers", ConfigField.BOOLEAN, false);
    public static final ConfigurationKey<ListenerConfig, Boolean> UNKNOWN_BLOCK_CLEANER_SCAN_PLAYER_INVENTORIES =
            new ConfigurationKey<>("UnknownBlockCleanerScanPlayerInventories", ConfigField.BOOLEAN, false);
    public static final ConfigurationKey<ListenerConfig, String[]> UNKNOWN_BLOCK_CLEANER_EXCLUDE =
            new ConfigurationKey<>("UnknownBlockCleanerExclude", ConfigField.STRING_ARRAY, new String[0]);
    public static final ConfigurationKey<ListenerConfig, Integer> UNKNOWN_BLOCK_CLEANER_BUDGET_MS =
            new ConfigurationKey<>("UnknownBlockCleanerBudgetMs", ConfigField.INTEGER, 10);
    public static final ConfigurationKey<ListenerConfig, Integer> UNKNOWN_BLOCK_CLEANER_INTERVAL_MS =
            new ConfigurationKey<>("UnknownBlockCleanerIntervalMs", ConfigField.INTEGER, 50);

    private static final ListenerConfig INSTANCE = new ListenerConfig();

    public ListenerConfig() {
        register(
                UNKNOWN_BLOCK_CLEANER,
                UNKNOWN_BLOCK_CLEANER_SCAN_FLUIDS,
                UNKNOWN_BLOCK_CLEANER_SCAN_CONTAINERS,
                UNKNOWN_BLOCK_CLEANER_SCAN_PLAYER_INVENTORIES,
                UNKNOWN_BLOCK_CLEANER_EXCLUDE,
                UNKNOWN_BLOCK_CLEANER_BUDGET_MS,
                UNKNOWN_BLOCK_CLEANER_INTERVAL_MS);
    }

    public static ListenerConfig get() {
        return INSTANCE;
    }
}
