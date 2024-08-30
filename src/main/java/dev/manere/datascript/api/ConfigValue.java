package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigValue<T> {
    private final T value;

    public ConfigValue(final T value) {
        this.value = value;
    }

    @NotNull
    public <N> ConfigValue<N> map(final @NotNull Function<T, N> function) {
        return new ConfigValue<>(function.apply(value));
    }

    public void accept(final @NotNull Consumer<T> consumer) {
        consumer.accept(value);
    }

    @NotNull
    public <N> ConfigValue<N> cast(final @NotNull Class<N> ignoredType) {
        return new ConfigValue<>((N) get());
    }

    public T get() {
        return value;
    }
}
