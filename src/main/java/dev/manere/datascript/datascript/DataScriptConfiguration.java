package dev.manere.datascript.datascript;

import dev.manere.datascript.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataScriptConfiguration implements Configuration {
    private final File file;
    private final ConfigSection root = new ConfigSection(new RootSection());

    public DataScriptConfiguration(final @NotNull File file) {
        this.file = file;
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    @NotNull
    @Override
    public ConfigSection root() {
        return root;
    }

    @NotNull
    @Override
    public File file() {
        return file;
    }

    @Override
    public void load() {
        if (!file.exists()) return;

        try (final BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            clear();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                final ConfigNode node = parseNode(line, reader, 0);
                if (node != null) root.section().nodes().add(node);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private ConfigNode parseNode(@NotNull String line, final @NotNull BufferedReader reader, final int depth) throws IOException {
        final String indent = "  ".repeat(depth);
        if (!line.startsWith(indent)) return null;

        line = line.substring(indent.length());

        final String[] parts = line.split("=", 2);
        final String name = parts[0].trim();

        final String key = name
            .replaceAll("\\{", "")
            .replaceAll("}", "")
            .trim();

        if (parts.length == 1) {
            if (line.endsWith("{")) {
                final SectionNode section = new SectionNode() {
                    private final Set<ConfigNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

                    @NotNull
                    @Override
                    public Set<ConfigNode> nodes() {
                        return nodes;
                    }

                    @NotNull
                    @Override
                    public String name() {
                        return key;
                    }
                };

                String childLine;
                while ((childLine = reader.readLine()) != null && !childLine.trim().equals("}")) {
                    final ConfigNode childNode = parseNode(childLine, reader, depth + 1);
                    if (childNode != null) {
                        section.nodes().add(childNode);
                    }
                }

                return section;
            } else if (line.endsWith("{}")) {
                return new SectionNode() {
                    private final Set<ConfigNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

                    @NotNull
                    @Override
                    public Set<ConfigNode> nodes() {
                        return nodes;
                    }

                    @NotNull
                    @Override
                    public String name() {
                        return key;
                    }
                };
            }

            return null;
        }

        final String value = parts[1].trim();

        if (value.startsWith("'") && value.endsWith("'") || value.startsWith("\"") && value.endsWith("\"")) {
            return new ScalarNode<>(value.substring(1, value.length() - 1)) {
                @NotNull
                @Override
                public String name() {
                    return key;
                }
            };
        } else if (value.matches("\\d+")) {
            return new ScalarNode<>(Integer.parseInt(value)) {
                @NotNull
                @Override
                public String name() {
                    return key;
                }
            };
        } else if (value.matches("\\d+L")) {
            return new ScalarNode<>(Long.parseLong(value.substring(0, value.length() - 1))) {
                @NotNull
                @Override
                public String name() {
                    return key;
                }
            };
        } else if (value.matches("\\d+\\.\\d+D")) {
            return new ScalarNode<>(Double.parseDouble(value.substring(0, value.length() - 1))) {
                @NotNull
                @Override
                public String name() {
                    return key;
                }
            };
        } else if (value.matches("'.'C")) {
            return new ScalarNode<>(value.charAt(1)) {
                @NotNull
                @Override
                public String name() {
                    return key;
                }
            };
        } else if (value.matches("\\d+S")) {
            return new ScalarNode<>(Short.parseShort(value.substring(0, value.length() - 1))) {
                @NotNull
                @Override
                public String name() {
                    return key;
                }
            };
        } else if (value.matches("\\d+B")) {
            return new ScalarNode<>(Byte.parseByte(value.substring(0, value.length() - 1))) {
                @NotNull
                @Override
                public String name() {
                    return key;
                }
            };
        } else if (value.startsWith("[")) {
            // Handle list parsing
            final List<Object> list = new ArrayList<>();
            final StringBuilder listContent = new StringBuilder(value);

            // Read until we find the closing bracket
            while (!listContent.toString().trim().endsWith("]")) {
                String nextLine = reader.readLine();
                if (nextLine != null) {
                    listContent.append(nextLine.trim());
                }
            }

            // Remove the surrounding brackets
            final String content = listContent.substring(1, listContent.length() - 1).trim();

            // Split the content by commas and parse individual elements
            final String[] elements = content.split(",");
            for (String element : elements) {
                element = element.trim();
                if (element.matches("\\d+")) {
                    list.add(Integer.parseInt(element));
                } else if (element.matches("\\d+L")) {
                    list.add(Long.parseLong(element.substring(0, element.length() - 1)));
                } else if (element.matches("\\d+\\.\\d+D")) {
                    list.add(Double.parseDouble(element.substring(0, element.length() - 1)));
                } else if (element.matches("'.'C")) {
                    list.add(element.charAt(1));
                } else if (element.matches("\\d+S")) {
                    list.add(Short.parseShort(element.substring(0, element.length() - 1)));
                } else if (element.startsWith("'") && element.endsWith("'") || element.startsWith("\"") && element.endsWith("\"")) {
                    list.add(element.substring(1, element.length() - 1));
                } else {
                    list.add(element);  // default to String
                }
            }

            return new ScalarNode<>(Collections.synchronizedList(list)) {
                @NotNull
                @Override
                public String name() {
                    return key;
                }
            };
        } else {
            return new ScalarNode<>(value) {
                @NotNull
                @Override
                public String name() {
                    return key;
                }
            };
        }
    }

    @Override
    public void save() {
        if (!file.exists()) try {
            assert file.createNewFile();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        try (final BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            for (final ConfigNode node : root.nodes()) writeNode(writer, node, 0);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeNode(final @NotNull BufferedWriter writer, final @NotNull ConfigNode node, final int depth) throws IOException {
        if (depth < 0) throw new IllegalArgumentException();

        final String indent = "  ".repeat(depth);
        final String name = node.name();

        if (node instanceof ScalarNode<?> scalar) {
            final Object value = scalar.value();

            if (value instanceof String string) {
                writer.write(indent + node.name() + " = '" + string + "'");
                writer.newLine();
            } else if (value instanceof Byte _byte) {
                writer.write(indent + node.name() + " = " + _byte + "B");
                writer.newLine();
            } else if (value instanceof Integer integer) {
                writer.write(indent + node.name() + " = " + integer);
                writer.newLine();
            } else if (value instanceof Long _long) {
                writer.write(indent + node.name() + " = " + _long + "L");
                writer.newLine();
            } else if (value instanceof Double _double) {
                writer.write(indent + node.name() + " = " + _double + "D");
                writer.newLine();
            } else if (value instanceof Character character) {
                writer.write(indent + node.name() + " = '" + character + "'C");
                writer.newLine();
            } else if (value instanceof Short _short) {
                writer.write(indent + node.name() + " = " + _short + "S");
                writer.newLine();
            } else if (value instanceof List<?> list) {
                writer.write(indent + node.name() + " = [");

                for (int i = 0; i < list.size(); i++) {
                    final Object element = list.get(i);

                    writer.newLine();

                    if (element instanceof String string) {
                        writer.write(indent + "  '" + string + "'");
                    } else if (element instanceof Integer integer) {
                        writer.write(indent + "  " + integer);
                    } else if (element instanceof Long _long) {
                        writer.write(indent + "  " + _long + "L");
                    } else if (element instanceof Double _double) {
                        writer.write(indent + "  " + _double + "D");
                    } else if (element instanceof Character character) {
                        writer.write(indent + "  " + character + "'C");
                    } else if (element instanceof Short _short) {
                        writer.write(indent + "  " + _short + "S");
                    } else {
                        writer.write(indent + "  " + value);
                    }

                    if (i == list.size() - 1) {
                        writer.append(",");
                        writer.newLine();
                        break;
                    }
                }

                writer.write(indent + "]");
                writer.newLine();
            } else {
                writer.write(indent + node.name() + " = " + value);
                writer.newLine();
            }
        }

        if (node instanceof SectionNode section) {
            if (section.nodes().isEmpty()) {
                writer.write(indent + name + " {}" + "\n");
                return;
            }

            writer.write(indent + name + " {" + "\n");

            for (final ConfigNode child : section.nodes()) {
                writeNode(writer, child, depth + 1);
            }

            writer.write(indent + "}" + "\n");
        }
    }

    public static class Builder {
        private File file;

        @NotNull
        public Builder file(final @NotNull File file) {
            this.file = file;
            return this;
        }

        @NotNull
        public DataScriptConfiguration build() {
            if (file == null) throw new NullPointerException();
            return new DataScriptConfiguration(file);
        }
    }
}
