package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RootSection implements SectionNode {
    private final Set<ConfigNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @NotNull
    @Override
    public String name() {
        return "~root";
    }

    @NotNull
    @Override
    public Set<ConfigNode> nodes() {
        return nodes;
    }
}
