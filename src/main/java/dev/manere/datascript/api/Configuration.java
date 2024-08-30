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

    void load();

    void save();

    @NotNull
    default CompletableFuture<Configuration> loadAsync() {
        return CompletableFuture.supplyAsync(() -> {
            load();
            return this;
        }).exceptionally(throwable -> this);
    }

    @NotNull
    default CompletableFuture<Configuration> saveAsync() {
        return CompletableFuture.supplyAsync(() -> {
            save();
            return this;
        }).exceptionally(throwable -> this);
    }
}
