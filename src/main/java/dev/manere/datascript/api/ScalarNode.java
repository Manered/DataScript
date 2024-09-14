package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract class representing a scalar node in the configuration,
 * which holds a value of type {@code V}.
 *
 * @param <V> The type of value this scalar node holds.
 */
public abstract class ScalarNode<V> implements ConfigNode {
    private V value;

    /**
     * Constructs a new ScalarNode with the provided value.
     *
     * @param value The initial value of this node.
     */
    public ScalarNode(final @NotNull V value) {
        this.value = value;
    }

    /**
     * Retrieves the value of this node.
     *
     * @return The current value of this node.
     */
    @NotNull
    public V value() {
        return value;
    }

    /**
     * Sets the value of this node.
     *
     * @param value The new value to be set.
     */
    public void set(final @NotNull V value) {
        this.value = value;
    }
}
