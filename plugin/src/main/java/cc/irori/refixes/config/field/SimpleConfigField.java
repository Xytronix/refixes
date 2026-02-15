package cc.irori.refixes.config.field;

import com.hypixel.hytale.codec.Codec;

class SimpleConfigField<T> implements ConfigField<T> {

    private final Codec<T> codec;

    public SimpleConfigField(Codec<T> codec) {
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
    public Codec<T> getCodec() {
        return codec;
    }
}
