---
description: Java functional programming patterns - lambdas, streams, Optional, method references, and functional interfaces
---

# Java Functional Programming Skill

## 1. Lambda Expressions

### Basic Syntax
```java
// Full form
(Type param1, Type param2) -> { statements; return value; }

// Single expression (return implicit)
(param1, param2) -> expression

// Single parameter (parentheses optional)
param -> expression

// No parameters
() -> expression
```

### Replace Anonymous Classes with Lambdas
```java
// Before — anonymous inner class
button.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        handleClick(e);
    }
});

// After — lambda
button.addActionListener(e -> handleClick(e));

// After — method reference (even cleaner)
button.addActionListener(this::handleClick);
```

### Swing-Specific Lambdas
```java
// ActionListener
btnProcess.addActionListener(e -> processFiles());

// FocusAdapter → FocusListener lambda (only if one method needed)
// FocusAdapter has 2 methods, so use adapter or split:
txtField.addFocusListener(new FocusAdapter() {
    @Override
    public void focusLost(FocusEvent e) {
        validateField();
    }
});
// Note: FocusListener has 2 abstract methods = NOT a functional interface
// Use adapter pattern for multi-method listeners

// Runnable
EventQueue.invokeLater(() -> {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    new JMkvpropedit().setVisible(true);
});

// SwingWorker (not a functional interface, but can use lambdas inside)
SwingUtilities.invokeLater(() -> txtOutput.append(line));
```

---

## 2. Method References

### Four Types
```java
// 1. Static method reference
Function<String, Integer> parser = Integer::parseInt;

// 2. Instance method of a particular object
Consumer<String> printer = System.out::println;
list.forEach(System.out::println);

// 3. Instance method of an arbitrary object of a type
Function<String, String> upper = String::toUpperCase;
list.sort(String::compareToIgnoreCase);

// 4. Constructor reference
Supplier<ArrayList<String>> factory = ArrayList::new;
Function<String, File> fileFactory = File::new;
```

### When to Use
```java
// Use method reference when lambda just calls a single method with same params
list.forEach(item -> System.out.println(item));  // Lambda
list.forEach(System.out::println);                // Method ref — prefer this

// DON'T use when you need to transform arguments
list.forEach(item -> process(item, defaultConfig)); // Keep lambda
```

---

## 3. Functional Interfaces (java.util.function)

### Core Interfaces
```java
// Predicate<T> — takes T, returns boolean
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<File> exists = File::exists;
list.removeIf(String::isEmpty);

// Function<T, R> — takes T, returns R
Function<String, Integer> length = String::length;
Function<File, String> getName = File::getName;

// Consumer<T> — takes T, returns void
Consumer<String> log = LOGGER::info;
list.forEach(log);

// Supplier<T> — takes nothing, returns T
Supplier<JFileChooser> lazyChooser = JFileChooser::new;

// UnaryOperator<T> — takes T, returns T (specialization of Function<T,T>)
UnaryOperator<String> trim = String::trim;

// BiFunction<T, U, R> — takes T and U, returns R
BiFunction<String, String, File> createFile = (dir, name) -> new File(dir, name);

// BiConsumer<T, U> — takes T and U, returns void
BiConsumer<JCheckBox, Boolean> setEnabled = JCheckBox::setEnabled;

// BiPredicate<T, U>
BiPredicate<String, String> contains = String::contains;
```

### Composing Functions
```java
// Predicate composition
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<String> notBlank = s -> !s.isBlank();
Predicate<String> valid = notEmpty.and(notBlank).and(s -> s.length() < 100);

// Function composition
Function<String, String> sanitize = String::trim;
Function<String, String> pipeline = sanitize
    .andThen(String::toLowerCase)
    .andThen(s -> s.replaceAll("[^a-z0-9]", ""));
```

### Custom Functional Interfaces
```java
// Define your own when the standard ones don't fit
@FunctionalInterface
interface TrackProcessor {
    String[] buildCommandLine(int trackIndex, String fileName);
}

// Use with lambdas
TrackProcessor videoProcessor = (idx, name) -> {
    // build video command args
    return new String[]{"--edit", "track:v" + idx};
};
```

---

## 4. Streams API

### Creating Streams
```java
// From collections
List<String> files = getFiles();
files.stream()                        // Sequential stream
files.parallelStream()                // Parallel stream

// From arrays
String[] arr = {"a", "b", "c"};
Arrays.stream(arr)

// From values
Stream.of("a", "b", "c")

// From range
IntStream.range(0, nVideo)            // 0, 1, ..., nVideo-1
IntStream.rangeClosed(1, nVideo)      // 1, 2, ..., nVideo

// Infinite streams
Stream.generate(() -> "default")
Stream.iterate(0, n -> n + 1)
Stream.iterate(0, n -> n < 10, n -> n + 1)  // Java 9+
```

### Intermediate Operations (lazy, return Stream)
```java
.filter(Predicate)        // Keep elements matching condition
.map(Function)            // Transform each element
.flatMap(Function)        // Transform & flatten nested streams
.distinct()               // Remove duplicates
.sorted()                 // Natural order
.sorted(Comparator)       // Custom order
.peek(Consumer)           // Debug: look at each element without modifying
.limit(n)                 // Take first n
.skip(n)                  // Skip first n
.takeWhile(Predicate)     // Java 9+: take while condition is true
.dropWhile(Predicate)     // Java 9+: skip while condition is true
```

### Terminal Operations (eager, trigger execution)
```java
.forEach(Consumer)                    // Execute for each
.forEachOrdered(Consumer)            // Preserve encounter order
.collect(Collector)                   // Reduce to collection
.toList()                             // Java 16+: unmodifiable list
.toArray()                            // To array
.reduce(identity, BinaryOperator)     // Fold to single value
.count()                              // Number of elements
.anyMatch(Predicate)                  // At least one matches?
.allMatch(Predicate)                  // All match?
.noneMatch(Predicate)                 // None match?
.findFirst()                          // First element (Optional)
.findAny()                            // Any element (Optional)
.min(Comparator)                      // Minimum (Optional)
.max(Comparator)                      // Maximum (Optional)
```

### Collectors
```java
.collect(Collectors.toList())                    // Mutable list
.collect(Collectors.toUnmodifiableList())         // Immutable (Java 10+)
.collect(Collectors.toSet())                      // Set
.collect(Collectors.toMap(keyFn, valueFn))        // Map
.collect(Collectors.joining(", "))                // Concatenate strings
.collect(Collectors.groupingBy(classifier))       // Group by key
.collect(Collectors.partitioningBy(predicate))    // Split into 2 groups
.collect(Collectors.counting())                   // Count
.collect(Collectors.summarizingInt(fn))            // Statistics
```

### Practical Examples for This Project
```java
// Before — iterate with index
for (int i = 0; i < modelFiles.size(); i++) {
    cmdLineGeneral[i] = "";
    cmdLineGeneralOpt[i] = "";
}

// After — stream
IntStream.range(0, modelFiles.size()).forEach(i -> {
    cmdLineGeneral[i] = "";
    cmdLineGeneralOpt[i] = "";
});

// Before — check if any track has edits
boolean hasEdits = false;
for (int j = 0; j < nVideo; j++) {
    if (chbEditVideo[j].isSelected()) {
        hasEdits = true;
        break;
    }
}

// After — stream with anyMatch
boolean hasEdits = videoTrackControls.stream()
    .anyMatch(tc -> tc.chbEdit.isSelected());

// Before — build command for all tracks
for (int j = 0; j < nVideo; j++) {
    if (chbEditVideo[j].isSelected()) {
        // build command...
    }
}

// After — stream + filter + map
String videoArgs = videoTrackControls.stream()
    .filter(tc -> tc.chbEdit.isSelected())
    .map(tc -> tc.buildCommandLine(
        videoTrackControls.indexOf(tc) + 1, 0, ""))
    .filter(args -> !args[0].isEmpty())
    .map(args -> args[0])
    .collect(Collectors.joining());

// Filter files by extension
List<File> mkvFiles = Arrays.stream(directory.listFiles())
    .filter(f -> f.getName().endsWith(".mkv"))
    .sorted(Comparator.comparing(File::getName))
    .collect(Collectors.toList());
```

---

## 5. Optional

### Creating Optionals
```java
Optional<String> present = Optional.of("value");          // Non-null
Optional<String> maybe   = Optional.ofNullable(value);    // Nullable
Optional<String> empty   = Optional.empty();              // Empty
```

### Using Optionals
```java
// DON'T do this — defeats the purpose
if (optional.isPresent()) {
    return optional.get();
}

// DO this — functional chaining
optional.orElse("default")                    // Default value
optional.orElseGet(() -> computeDefault())    // Lazy default
optional.orElseThrow()                        // Java 10+: NoSuchElementException
optional.orElseThrow(() -> new RuntimeException("missing"))

optional.ifPresent(value -> process(value))   // Execute if present
optional.ifPresentOrElse(                      // Java 9+
    value -> process(value),
    () -> handleAbsence()
)

optional.map(String::toUpperCase)             // Transform if present
optional.flatMap(this::findById)              // Unwrap nested Optional
optional.filter(s -> s.length() > 3)          // Filter
optional.stream()                              // Java 9+: to Stream(0 or 1)
optional.or(() -> Optional.of("fallback"))    // Java 9+: alternative Optional
```

### Practical Examples
```java
// Before — null check
String exePath = ini.get("General", "mkvpropedit");
if (exePath != null) {
    if (exePath.equals("mkvpropedit")) {
        chbMkvPropExeDef.setSelected(true);
    } else {
        txtMkvPropExe.setText(exePath);
    }
}

// After — Optional
Optional.ofNullable(ini.get("General", "mkvpropedit"))
    .ifPresent(path -> {
        if ("mkvpropedit".equals(path)) {
            chbMkvPropExeDef.setSelected(true);
            chbMkvPropExeDef.setEnabled(false);
        } else {
            txtMkvPropExe.setText(path);
            chbMkvPropExeDef.setSelected(false);
            chbMkvPropExeDef.setEnabled(true);
        }
    });

// Find first MKV file
Optional<File> firstMkv = Arrays.stream(files)
    .filter(f -> f.getName().endsWith(".mkv"))
    .findFirst();
```

---

## 6. Effectively Final & Closures

### Rules for Lambdas
```java
// Variables captured by lambdas must be effectively final
int count = 0;
list.forEach(item -> count++);  // ❌ COMPILE ERROR: count is not effectively final

// Use AtomicInteger or array wrapper for mutable state
AtomicInteger count = new AtomicInteger(0);
list.forEach(item -> count.incrementAndGet());  // ✅

// Or use reduce/collect instead
long count = list.stream().filter(predicate).count();  // ✅ Better
```

### Swing Event Handlers
```java
// ✅ This works because nVideo is effectively final at capture time
final int trackIdx = nVideo;  // Capture current value
chbEditVideo[nVideo].addActionListener(e -> toggleVideo(trackIdx));

// ❌ This would fail because cbVideo.getSelectedIndex() changes
// chbEditVideo[nVideo].addActionListener(e -> toggleVideo(nVideo));
// nVideo gets incremented later!
```

---

## 7. Anti-Patterns to Avoid

### Don't Use Streams for Simple Iterations
```java
// ❌ Overkill — use a for loop
IntStream.range(0, 3).forEach(i -> list.add(defaults[i]));

// ✅ Simple and clear
for (int i = 0; i < 3; i++) list.add(defaults[i]);
```

### Don't Catch Exceptions Inside Streams
```java
// ❌ Messy
files.stream().map(f -> {
    try { return Files.readString(f.toPath()); }
    catch (IOException e) { throw new UncheckedIOException(e); }
});

// ✅ Extract to a method
files.stream().map(this::readFileSafe);

private String readFileSafe(File f) {
    try { return Files.readString(f.toPath()); }
    catch (IOException e) { throw new UncheckedIOException(e); }
}
```

### Don't Modify External State in Streams
```java
// ❌ Side effects in map
List<String> results = new ArrayList<>();
stream.map(x -> { results.add(x); return x; }); // BAD

// ✅ Use collect
List<String> results = stream.collect(Collectors.toList());
```

### Don't Use parallelStream() Without Justification
```java
// ❌ Parallel for small collections or I/O-bound tasks
smallList.parallelStream().forEach(this::processFile);

// ✅ Use parallel only for CPU-intensive operations on large datasets
// and when order doesn't matter
largeList.parallelStream()
    .filter(this::expensiveComputation)
    .collect(Collectors.toList());
```
