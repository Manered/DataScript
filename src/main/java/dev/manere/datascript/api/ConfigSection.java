package dev.manere.datascript.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public record ConfigSection(@NotNull SectionNode section) {
    @Nullable
    private ConfigNode node(final @NotNull String key) {
        for (final ConfigNode node : section.nodes()) if (node.name().equals(key)) return node;
        return null;
    }

    @NotNull
    public List<Object> listValue(final @NotNull String key) {
        final ConfigNode node = node(key);
        if (node == null) return Collections.synchronizedList(new ArrayList<>());

        return node instanceof ScalarNode<?> scalar ? (List<Object>) scalar.value() : Collections.synchronizedList(new ArrayList<>());
    }

    @NotNull
    public <E> List<E> listValue(final @NotNull String key, final @NotNull List<E> fallback) {
        final ConfigNode node = node(key);
        if (node == null) return fallback;

        return node instanceof ScalarNode<?> scalar ? (List<E>) scalar.value() : fallback;
    }

    @NotNull
    public <E> List<E> listValue(final @NotNull String key, final @NotNull Class<E> ignoredElementType) {
        final ConfigNode node = node(key);
        if (node == null) return Collections.synchronizedList(new ArrayList<>());

        return node instanceof ScalarNode<?> scalar ? (List<E>) scalar.value() : Collections.synchronizedList(new ArrayList<>());
    }

    @NotNull
    public <E> List<E> listValue(final @NotNull String key, final @NotNull Class<E> ignoredElementType, final @NotNull List<E> fallback) {
        final ConfigNode node = node(key);
        if (node == null) return fallback;

        return node instanceof ScalarNode<?> scalar ? (List<E>) scalar.value() : fallback;
    }

    @Nullable
    public ConfigValue<@NotNull Object> value(final @NotNull String key) {
        final ConfigNode node = node(key);
        if (node == null) return null;

        return node instanceof ScalarNode<?> scalar ? new ConfigValue<>(scalar.value()) : null;
    }

    @NotNull
    public <T> ConfigValue<@NotNull T> value(final @NotNull String key, final @NotNull T fallback) {
        final ConfigNode node = node(key);
        if (node == null) return new ConfigValue<>(fallback);

        return node instanceof ScalarNode<?> scalar ? (ConfigValue<T>) new ConfigValue<>(scalar.value()) : new ConfigValue<>(fallback);
    }

    @Nullable
    public <T> ConfigValue<@NotNull T> value(final @NotNull String key, final @NotNull Class<T> ignoredType) {
        final ConfigNode node = node(key);
        if (node == null) return null;

        return node instanceof ScalarNode<?> scalar ? (ConfigValue<T>) new ConfigValue<>(scalar.value()) : null;
    }

    @NotNull
    public <T> ConfigValue<@NotNull T> value(final @NotNull String key, final @NotNull Class<T> ignoredType, final @NotNull T fallback) {
        final ConfigNode node = node(key);
        if (node == null) return new ConfigValue<>(fallback);

        return node instanceof ScalarNode<?> scalar ? (ConfigValue<T>) new ConfigValue<>(scalar.value()) : new ConfigValue<>(fallback);
    }

    public void rename(final @NotNull String key, final @NotNull String newKey) {
        final ConfigNode node = node(key);
        if (node == null) return;

        section.nodes().remove(node);

        if (node instanceof ScalarNode<?> scalarNode) {
            section.nodes().add(new ScalarNode<Object>(scalarNode.value()) {
                @NotNull
                public String name() {
                    return newKey;
                }
            });
        } else if (node instanceof SectionNode sectionNode) {
            section.nodes().add(new SectionNode() {
                private final Set<ConfigNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

                {
                    nodes.addAll(sectionNode.nodes());
                }

                @NotNull
                @Override
                public Set<ConfigNode> nodes() {
                    return nodes;
                }

                @NotNull
                @Override
                public String name() {
                    return newKey;
                }
            });
        }
    }

    public <T> void set(final @NotNull String key, final @Nullable T value) {
        if (value == null) {
            unset(key);
            return;
        }

        final ConfigNode node = node(key);

        if (node == null) {
            create(key, value);
        } else {
            if (node instanceof ScalarNode<?>) try {
                final ScalarNode<T> scalar = (ScalarNode<T>) node;
                scalar.set(value);
            } catch (final Exception e) {
                unset(key);
                create(key, value);
            }
            else {
                unset(key);
                create(key, value);
            }
        }
    }

    private <T> void create(final @NotNull String key, final @NotNull T value) {
        section.nodes().add(new ScalarNode<>(value) {
            @NotNull
            public String name() {
                return key;
            }
        });
    }

    public void unset(final @NotNull String key) {
        final ConfigNode node = node(key);
        if (node == null) return;

        section.nodes().remove(node);
    }

    @Nullable
    public ConfigSection section(final @NotNull String key) {
        final ConfigNode node = node(key);
        return node instanceof SectionNode sectionNode ? new ConfigSection(sectionNode) : null;
    }

    @NotNull
    public ConfigSection createSection(final @NotNull String key) {
        if (key.equalsIgnoreCase("~root")) throw new IllegalArgumentException();

        final ConfigSection sectionFound = section(key);
        if (sectionFound != null) return sectionFound;

        final SectionNode sectionNode = new SectionNode() {
            private final Set<ConfigNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

            @NotNull

            public Set<ConfigNode> nodes() {
                return nodes;
            }

            @NotNull

            public String name() {
                return key;
            }
        };

        section.nodes().add(sectionNode);
        return new ConfigSection(sectionNode);
    }

    @NotNull
    @Unmodifiable
    public Set<ConfigNode> nodes() {
        return Collections.unmodifiableSet(section.nodes());
    }

    @NotNull
    @Unmodifiable
    public Set<String> keys() {
        final Set<String> keys = Collections.newSetFromMap(new ConcurrentHashMap<>());

        for (final ConfigNode node : nodes()) {
            keys.add(node.name());
        }

        return keys;
    }

    public boolean isRoot() {
        return section instanceof RootSection;
    }

    public void store(final @NotNull String sectionKey, final @NotNull Object object) {
        final Class<?> clazz = object.getClass();

        section(sectionKey, section -> {
            if (clazz.isRecord()) {
                for (final RecordComponent component : clazz.getRecordComponents()) {
                    if (component.isAnnotationPresent(IgnoreField.class)) continue;

                    section.set(component.getName(), component.getAccessor().getDefaultValue());
                }
            }

            for (final Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(IgnoreField.class)) continue;

                try {
                    section.set(field.getName(), field.get(object));
                } catch (final IllegalAccessException ignored) {
                }
            }
        });
    }

    @NotNull
    public ConfigSection sectionOrNew(final @NotNull String key) {
        final ConfigSection section = section(key);
        return section == null ? createSection(key) : section;
    }

    public void section(final @NotNull String key, final @NotNull Consumer<ConfigSection> consumer) {
        consumer.accept(sectionOrNew(key));
    }

    @Override
    @NotNull
    public SectionNode section() {
        return section;
    }
}
