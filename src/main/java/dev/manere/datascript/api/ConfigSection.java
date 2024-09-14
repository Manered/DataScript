package dev.manere.datascript.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Represents a section within the configuration, containing multiple configuration nodes.
 */
@SuppressWarnings("unchecked")
public record ConfigSection(@NotNull SectionNode section) {
    /**
     * Retrieves a configuration node by its key.
     *
     * @param key the key of the node
     * @return the corresponding ConfigNode, or null if not found
     */
    @Nullable
    @ApiStatus.Internal
    private ConfigNode node(final @NotNull String key) {
        for (final ConfigNode node : section.nodes()) if (node.name().equals(key)) return node;
        return null;
    }

    /**
     * Retrieves a list of values from the configuration node associated with the key.
     *
     * @param key the key of the configuration node
     * @return the list of values, or an empty synchronized list if not found
     */
    @NotNull
    public List<Object> listValue(final @NotNull String key) {
        final ConfigNode node = node(key);
        if (node == null) return Collections.synchronizedList(new ArrayList<>());

        return node instanceof ScalarNode<?> scalar ? (List<Object>) scalar.value() : Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Retrieves a list of values from the configuration node, or returns a fallback list if the key is not found.
     *
     * @param key the key of the configuration node
     * @param fallback the fallback list to return if not found
     * @param <E> the type of the list elements
     * @return the list of values, or the fallback list if not found
     */
    @NotNull
    public <E> List<E> listValue(final @NotNull String key, final @NotNull List<E> fallback) {
        final ConfigNode node = node(key);
        if (node == null) return fallback;

        return node instanceof ScalarNode<?> scalar ? (List<E>) scalar.value() : fallback;
    }

    /**
     * Retrieves a list of values from the configuration node, based on a specified type.
     *
     * @param key the key of the configuration node
     * @param ignoredElementType the expected type of elements
     * @param <E> the type of the elements in the list
     * @return the list of values, or an empty list if not found
     */
    @NotNull
    public <E> List<E> listValue(final @NotNull String key, final @NotNull Class<E> ignoredElementType) {
        final ConfigNode node = node(key);
        if (node == null) return Collections.synchronizedList(new ArrayList<>());

        return node instanceof ScalarNode<?> scalar ? (List<E>) scalar.value() : Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Retrieves a list of values from the configuration node, with a fallback option if not found.
     *
     * @param key the key of the configuration node
     * @param ignoredElementType the expected type of elements
     * @param fallback the fallback list to return if not found
     * @param <E> the type of the elements in the list
     * @return the list of values, or the fallback list if not found
     */
    @NotNull
    public <E> List<E> listValue(final @NotNull String key, final @NotNull Class<E> ignoredElementType, final @NotNull List<E> fallback) {
        final ConfigNode node = node(key);
        if (node == null) return fallback;

        return node instanceof ScalarNode<?> scalar ? (List<E>) scalar.value() : fallback;
    }

    /**
     * Retrieves the configuration value associated with the key.
     *
     * @param key the key of the configuration node
     * @return the ConfigValue, or null if not found
     */
    @Nullable
    public ConfigValue<@NotNull Object> value(final @NotNull String key) {
        final ConfigNode node = node(key);
        if (node == null) return null;

        return node instanceof ScalarNode<?> scalar ? new ConfigValue<>(scalar.value()) : null;
    }

    /**
     * Retrieves the configuration value associated with the key, or returns a fallback value if not found.
     *
     * @param key the key of the configuration node
     * @param fallback the fallback value
     * @param <T> the type of the value
     * @return the configuration value, or the fallback if not found
     */
    @NotNull
    public <T> ConfigValue<@NotNull T> value(final @NotNull String key, final @NotNull T fallback) {
        final ConfigNode node = node(key);
        if (node == null) return new ConfigValue<>(fallback);

        return node instanceof ScalarNode<?> scalar ? (ConfigValue<T>) new ConfigValue<>(scalar.value()) : new ConfigValue<>(fallback);
    }

    /**
     * Retrieves the configuration value associated with the key.
     *
     * @param key the key of the configuration node
     * @param ignoredType the expected type of elements
     * @param <T> the type of the value
     * @return the configuration value, or null if not found
     */
    @Nullable
    public <T> ConfigValue<@NotNull T> value(final @NotNull String key, final @NotNull Class<T> ignoredType) {
        final ConfigNode node = node(key);
        if (node == null) return null;

        return node instanceof ScalarNode<?> scalar ? (ConfigValue<T>) new ConfigValue<>(scalar.value()) : null;
    }

    /**
     * Retrieves the configuration value associated with the key, or returns a fallback value if not found.
     *
     * @param key the key of the configuration node
     * @param ignoredType the expected type of elements
     * @param fallback the fallback value
     * @param <T> the type of the value
     * @return the configuration value, or the fallback if not found
     */
    @NotNull
    public <T> ConfigValue<@NotNull T> value(final @NotNull String key, final @NotNull Class<T> ignoredType, final @NotNull T fallback) {
        final ConfigNode node = node(key);
        if (node == null) return new ConfigValue<>(fallback);

        return node instanceof ScalarNode<?> scalar ? (ConfigValue<T>) new ConfigValue<>(scalar.value()) : new ConfigValue<>(fallback);
    }

    /**
     * Renames a configuration node.
     *
     * @param key the current key of the configuration node
     * @param newKey the new key to rename to
     */
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

    /**
     * Sets the value of a configuration node by key. If the node doesn't exist, it is created.
     * If the value is null, the node will be removed.
     *
     * @param key   the key of the configuration node
     * @param value the value to set, or null to remove the node
     * @param <T>   the type of the value
     */
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

    /**
     * Creates a new scalar node with the given key and value.
     *
     * @param key   the key of the new configuration node
     * @param value the value to set for the new configuration node
     * @param <T>   the type of the value
     */
    @ApiStatus.Internal
    private <T> void create(final @NotNull String key, final @NotNull T value) {
        section.nodes().add(new ScalarNode<>(value) {
            @NotNull
            public String name() {
                return key;
            }
        });
    }

    /**
     * Unsets or removes a configuration node by key.
     *
     * @param key the key of the configuration node to remove
     */
    public void unset(final @NotNull String key) {
        final ConfigNode node = node(key);
        if (node == null) return;

        section.nodes().remove(node);
    }

    /**
     * Retrieves a subsection of the configuration by key.
     *
     * @param key the key of the subsection
     * @return the corresponding ConfigSection, or null if not found
     */
    @Nullable
    public ConfigSection section(final @NotNull String key) {
        final ConfigNode node = node(key);
        return node instanceof SectionNode sectionNode ? new ConfigSection(sectionNode) : null;
    }

    /**
     * Creates a new section with the specified key. If a section already exists with the key, it is returned.
     *
     * @param key the key for the new section
     * @return the created or existing ConfigSection
     * @throws IllegalArgumentException if the key is "~root"
     */
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

    /**
     * Returns an unmodifiable set of configuration nodes in the section.
     *
     * @return an unmodifiable set of ConfigNode
     */
    @NotNull
    @Unmodifiable
    public Set<ConfigNode> nodes() {
        return Collections.unmodifiableSet(section.nodes());
    }

    /**
     * Returns an unmodifiable set of keys representing the configuration nodes in the section.
     *
     * @return an unmodifiable set of keys
     */
    @NotNull
    @Unmodifiable
    public Set<String> keys() {
        final Set<String> keys = Collections.newSetFromMap(new ConcurrentHashMap<>());

        for (final ConfigNode node : nodes()) {
            keys.add(node.name());
        }

        return keys;
    }

    /**
     * Checks if this section is the root section.
     *
     * @return true if this is the root section, false otherwise
     */
    public boolean isRoot() {
        return section instanceof RootSection;
    }

    /**
     * Stores the fields of a given object into the configuration section under a specified section key.
     *
     * @param sectionKey the key of the section
     * @param object     the object to store fields from
     */
    public void store(final @NotNull String sectionKey, final @NotNull Object object) {
        final Class<?> clazz = object.getClass();

        section(sectionKey, section -> {
            if (clazz.isRecord()) {
                for (final RecordComponent component : clazz.getRecordComponents()) {
                    if (component.isAnnotationPresent(IgnoreField.class)) continue;

                    section.set(normalizeNaming(component.getName()), component.getAccessor().getDefaultValue());
                }
            }

            for (final Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(IgnoreField.class)) continue;

                try {
                    section.set(normalizeNaming(field.getName()), field.get(object));
                } catch (final IllegalAccessException ignored) {
                }
            }
        });
    }

    /**
     * Retrieves or creates a section by key. If the section does not exist, a new one is created.
     *
     * @param key the key of the section
     * @return the existing or new ConfigSection
     */
    @NotNull
    public ConfigSection sectionOrNew(final @NotNull String key) {
        final ConfigSection section = section(key);
        return section == null ? createSection(key) : section;
    }

    /**
     * Performs an action on the section specified by key. If the section doesn't exist, it is created.
     *
     * @param key      the key of the section
     * @param consumer the action to perform on the section
     */
    public void section(final @NotNull String key, final @NotNull Consumer<ConfigSection> consumer) {
        consumer.accept(sectionOrNew(key));
    }

    /**
     * Normalizes a string by converting it to lowercase and replacing certain characters with spaces.
     * <p>
     * Example:
     * </p>
     * <ul>
     *   <li>FirstName -> first name</li>
     *   <li>First name -> first name</li>
     *   <li>firstName -> first name</li>
     * </ul>
     * @param input the input string to normalize
     * @return the normalized string
     */
    @NotNull
    public String normalizeNaming(@NotNull String input) {
        if (input.isEmpty() || input.isBlank()) return input;

        // Insert space before uppercase letters
        final String spacedOut = input.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");

        // Convert to lowercase
        final String lowercased = spacedOut.toLowerCase();

        // Replace non-alphanumeric characters (except spaces) with spaces
        final String normalized = lowercased.replaceAll("[^a-z0-9 ]", " ");

        // Replace multiple spaces with a single space and trim
        return normalized.replaceAll("\\s+", " ").trim();
    }

    /**
     * Returns the section node for this configuration section.
     *
     * @return the section node
     */
    @Override
    @NotNull
    public SectionNode section() {
        return section;
    }
}
