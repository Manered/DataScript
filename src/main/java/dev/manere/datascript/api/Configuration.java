package dev.manere.datascript.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Interface representing a configuration with methods to load, save, and manage configuration data.
 */
public interface Configuration {

    /**
     * Returns the root section of the configuration.
     *
     * @return the root ConfigSection
     */
    @NotNull
    ConfigSection root();

    /**
     * Returns the file associated with this configuration.
     *
     * @return the file
     */
    @NotNull
    File file();

    /**
     * Clears all nodes from the root section.
     */
    default void clear() {
        clearSection(root().section());
    }

    /**
     * Recursively clears all nodes from the given section.
     *
     * @param section the section to clear
     */
    @ApiStatus.Internal
    private void clearSection(final @NotNull SectionNode section) {
        final Set<ConfigNode> nodesToRemove = new HashSet<>(section.nodes());
        for (final ConfigNode node : nodesToRemove) {
            if (node instanceof SectionNode sectionNode) clearSection(sectionNode);
            section.nodes().remove(node);
        }
    }

    /**
     * Loads the configuration data from the associated file on disk.
     */
    void loadFromDisk();

    /**
     * Saves the configuration data to the associated file on disk.
     */
    void saveToDisk();

    /**
     * Loads configuration data from a string.
     *
     * @param contents the configuration data as a string
     */
    void loadFromString(final @NotNull String contents);

    /**
     * Saves the current configuration data as a string.
     *
     * @return the configuration data as a string
     */
    @NotNull
    String saveToString();
}
