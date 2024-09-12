package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface Configuration {
    @NotNull
    ConfigSection root();

    @NotNull
    File file();

    default void clear() {
        clearSection(root().section());
    }

    private void clearSection(SectionNode section) {
        final Set<ConfigNode> nodesToRemove = new HashSet<>(section.nodes());
        for (final ConfigNode node : nodesToRemove) {
            if (node instanceof SectionNode sectionNode) clearSection(sectionNode);
            section.nodes().remove(node);
        }
    }

    void loadFromDisk();

    void saveToDisk();

    void loadFromString(final @NotNull String contents);

    @NotNull
    String saveToString();
}
