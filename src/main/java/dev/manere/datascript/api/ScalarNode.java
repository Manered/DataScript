package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

public abstract class ScalarNode<V> implements ConfigNode {
    private V value;

    public ScalarNode(final @NotNull V value) {
        this.value = value;
    }

    @NotNull
    public V value() {
        return value;
    }

    public void set(final @NotNull V value) {
        this.value = value;
    }
}
