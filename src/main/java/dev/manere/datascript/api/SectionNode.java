package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface SectionNode extends ConfigNode {
    @NotNull
    Set<ConfigNode> nodes();
}
