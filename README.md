# DataScript
> A human-readable configuration format/language with a Java implementation. Meant to be easy to understand, write and use.

---

Example:
```kt
button {
  text = "Execute"
  position {
    x = 0
    y = 0
  }
  size {
    length = 500
    height = 150
  }
  tooltip = [
    "Runs the following code:",
    "print('Hello World!')"
  ]
}
```

Node Types:

| Name         | Description                                                                                                                                                  |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scalar Node  | Contains a value. This is considered a tuple, map, etc. A format would be `key = <value>`                                                                    |
| Section Node | Has a Set of children, which are all implementations of a node. An empty one would be `key = {}`. A section can store multiple sections and multiple scalars |

Data Types:

| Name      | Example(s)                                     |
|-----------|------------------------------------------------|
| String    | `'String 1'`, `"String 2"`                     |
| Integer   | `69`, `2147483647`                             |
| Long      | `69L`                                          |
| Double    | `10.5D`                                        |
| Character | `'A'C`                                         |
| Short     | `256S`                                         |
| Byte      | `13B`                                          |
| List      | `["0", "1", "2", "3"]`                         |
| Boolean   | `true`, `false` (case insensitive)             |
| UUID      | `uuid('4ad4c78c-d4a4-4d25-91cf-4f001efc46c0')` |

(Every single Java primitive type and UUID's.)

Java Usage:

```java
final File file = new File("my_file.ds") /* Your file. */;
final Configuration configuration = DataScriptConfiguration.builder()
    .file(file)
    .build();
 
CompletableFuture.supplyAsync(configuration::load)
    .thenAcceptAsync(root -> {
        // Creates or retrieves a section, then executes a consumer.
        root.section("button", button -> {
            // You can get values with value(...), listValue(...).
            // You can set values with set(key, value)
        });

        // You can get, set or whatever you want here. All of the methods you need are in ConfigSection
        config.save();
    });
```
