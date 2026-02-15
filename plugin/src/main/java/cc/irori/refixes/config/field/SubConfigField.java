package cc.irori.refixes.config.field;

import cc.irori.refixes.config.Configuration;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class SubConfigField<T extends Configuration> implements ConfigField<T> {

    private final BuilderCodec<T> codec;

    public SubConfigField(BuilderCodec<T> codec) {
        this.codec = codec;
    }

    @Override
    public T valueForRead(T value) {
        return value;
    }

    @Override
    public T valueForStore(T value) {
        return value;
    }

    @Override
    public BuilderCodec<T> getCodec() {
        return codec;
    }
}
