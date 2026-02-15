package cc.irori.refixes.config;

import cc.irori.refixes.config.field.ConfigField;
import cc.irori.refixes.config.field.SubConfigField;

public record ConfigurationKey<C extends Configuration, T>(String name, ConfigField<T> field, T defaultValue) {

    public static <C extends Configuration, T extends Configuration> ConfigurationKey<C, T> subConfig(
            String name, T configuration) {
        return new ConfigurationKey<>(name, new SubConfigField<>(configuration.getCodec()), configuration);
    }
}
