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
    public static <T> ConfigValue<T> value(final @NotNull T value) {
        return new ConfigValue<>(value);
    }

    @NotNull
    public <N> ConfigValue<N> map(final @NotNull Function<T, N> function) {
        return new ConfigValue<>(function.apply(value));
    }

    @NotNull
    public ConfigValue<T> accept(final @NotNull Consumer<T> consumer) {
        consumer.accept(value);
        return this;
    }

    @NotNull
    public <N> N mapAndGet(final @NotNull Function<T, N> function) {
        return map(function).get();
    }

    @NotNull
    public <N> ConfigValue<N> cast(final @NotNull Class<N> ignoredType) {
        return new ConfigValue<>((N) get());
    }

    @NotNull
    public T get() {
        return value;
    }
}
