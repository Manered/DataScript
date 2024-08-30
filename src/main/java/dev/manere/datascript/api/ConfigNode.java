package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ConfigNode {
    @NotNull
    String name();
}
