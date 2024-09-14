package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a configuration node with a name.
 */
@FunctionalInterface
public interface ConfigNode {
    /**
     * Returns the name of this configuration node.
     *
     * @return the name of the node
     */
    @NotNull
    String name();
}