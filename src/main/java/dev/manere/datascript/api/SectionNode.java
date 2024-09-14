package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;
import java.util.Set;

/**
 * Interface representing a section node in the configuration.
 * A section node can contain other configuration nodes.
 */
public interface SectionNode extends ConfigNode {

    /**
     * Retrieves the set of configuration nodes contained in this section.
     *
     * @return A set of {@link ConfigNode} instances.
     */
    @NotNull
    Set<ConfigNode> nodes();
}
