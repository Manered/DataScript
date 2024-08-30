# DataScript
> A human readable configuration format/language with a Java implementation. Meant to be easy to understand, write and use.

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
    "Runs the following code:"
    "print('Hello World!')"
  ]
}
```

Node Types:

| Name         | Description                                                                                                                                                  |
| ---          | ---                                                                                                                                                          |
| Scalar Node  | Contains a value. This is considered a tuple, map, etc. A format would be `key = <value>`                                                                    |
| Section Node | Has a Set of children, which are all implementations of a node. An empty one would be `key = {}`. A section can store multiple sections and multiple scalars |

Data Types:
| Name | Example |
| ---- | ------- |
| TODO | TODO    |

(Every single Java primitive type.)

Java Usage:
```java
final File file = /* Your file. */;
final Configuration configuration = DataScriptConfiguration.builder()
  .file(file)
  .build();
 
configuration.loadAsync().thenAcceptAsync(root -> {
  // Creates or retrieves a section, then executes a consumer.
  root.section("button", button -> {
    // You can get values with value(...), listValue(...).
    // You can set values with set(key, value)
  });

  // You can get, set or whatever you want here. All of the methods you need are in ConfigSection
  // You could use saveAsync() but you're already on a new thread so you would join() or just waste additional time.
  config.save();
});
```
