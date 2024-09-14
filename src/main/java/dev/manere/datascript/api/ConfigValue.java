package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a configurable value that can be manipulated or transformed.
 *
 * @param <T> the type of the value
 */
public class ConfigValue<T> {
    private final T value;

    /**
     * Constructs a ConfigValue with the specified value.
     *
     * @param value the value to wrap
     */
    public ConfigValue(final T value) {
        this.value = value;
    }

    /**
     * Creates a new ConfigValue from the given value.
     *
     * @param value the value to wrap
     * @param <T>   the type of the value
     * @return the new ConfigValue
     */
    @NotNull
    public static <T> ConfigValue<T> value(final @NotNull T value) {
        return new ConfigValue<>(value);
    }

    /**
     * Retrieves the underlying value.
     *
     * @return the value
     */
    public T get() {
        return value;
    }

    /**
     * Applies a transformation function to the value and returns the result.
     *
     * @param transformer the function to apply
     * @param <E>         the type of the result
     * @return the transformed value
     */
    public <E> E transform(final Function<T, E> transformer) {
        return transformer.apply(value);
    }

    /**
     * Performs the given action on the value.
     *
     * @param consumer the action to perform
     */
    public void run(final Consumer<T> consumer) {
        consumer.accept(value);
    }
}
