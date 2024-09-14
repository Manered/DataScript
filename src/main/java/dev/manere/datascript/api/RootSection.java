package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the root section of the configuration.
 * The root section can contain multiple {@link ConfigNode} instances.
 */
public class RootSection implements SectionNode {
    private final Set<ConfigNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Retrieves the name of the root section.
     *
     * @return The name of the root section, which is "~root".
     */
    @NotNull
    @Override
    public String name() {
        return "~root";
    }

    /**
     * Returns the set of configuration nodes contained in this root section.
     *
     * @return A set of {@link ConfigNode} instances.
     */
    @NotNull
    @Override
    public Set<ConfigNode> nodes() {
        return nodes;
    }
}
