---
description: Step-by-step guide to migrate a Java 8 Maven project to Java 21 LTS
---

# Java 8 → Java 21 Migration Workflow

## Pre-requisites

1. **Install JDK 21 LTS** (Temurin recommended):
   ```powershell
   # Windows — download from https://adoptium.net/temurin/releases/?version=21
   # Or via winget:
   winget install EclipseAdoptium.Temurin.21.JDK
   ```
2. **Set JAVA_HOME** to JDK 21 path
3. **Verify**: `java -version` should show `openjdk version "21.x.x"`

---

## Phase 1: Build System (pom.xml)

### 1.1 Update compiler source/target
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <!-- Or use release instead (preferred in Java 9+): -->
    <maven.compiler.release>21</maven.compiler.release>
</properties>
```

> **Note:** `maven.compiler.release` is preferred over source/target in Java 9+.
> It combines source, target, AND boot classpath in one setting, preventing
> accidental use of APIs not available in the target version.

### 1.2 Update maven-compiler-plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version> <!-- Minimum 3.11+ for Java 21 -->
</plugin>
```

### 1.3 Update maven-shade-plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.0</version> <!-- Minimum 3.5+ for Java 21 bytecode -->
</plugin>
```

### 1.4 Update dependencies to Java 21 compatible versions
| Dependency | Current | Minimum for Java 21 | Recommended |
|-----------|---------|---------------------|-------------|
| commons-io | 2.15.1 | 2.15.1 ✅ | 2.18.0 |
| ini4j | 0.5.4 | ❌ Abandoned | Replace with commons-configuration2 2.11.0 |
| junit | 4.13.2 | Works but legacy | JUnit 5 (5.11.x) |
| commons-compress | 1.26.0 | 1.26.0 ✅ | 1.27.1 |
| xz | 1.9 | 1.9 ✅ | 1.10 |

### 1.5 Update Launch4j JRE minimum
In `launch4j/config/exe-standalone.xml` and `exe-with-jar.xml`:
```xml
<jre>
    <path>%JAVA_HOME%;%PATH%</path>
    <minVersion>21</minVersion>  <!-- Was: 8 -->
</jre>
```

---

## Phase 2: Language Feature Adoption

### 2.1 Text Blocks (Java 13+)
Replace multi-line string concatenation with text blocks:
```java
// Before (Java 8)
String json = "[\n" +
    "  \"arg1\",\n" +
    "  \"arg2\"\n" +
    "]";

// After (Java 21)
String json = """
    [
      "arg1",
      "arg2"
    ]
    """;
```

### 2.2 Switch Expressions (Java 14+)
```java
// Before
String prefix;
switch (type) {
    case VIDEO:    prefix = "v"; break;
    case AUDIO:    prefix = "a"; break;
    case SUBTITLE: prefix = "s"; break;
    default:       prefix = ""; break;
}

// After
String prefix = switch (type) {
    case VIDEO    -> "v";
    case AUDIO    -> "a";
    case SUBTITLE -> "s";
};
```

### 2.3 Records (Java 16+)
Replace simple data classes with records:
```java
// Before
public class TrackProfile {
    private final String name;
    private final boolean defaultTrack;
    // constructor, getters, equals, hashCode, toString...
}

// After
public record TrackProfile(String name, boolean defaultTrack) {}
```

### 2.4 Pattern Matching for instanceof (Java 16+)
```java
// Before
if (obj instanceof String) {
    String s = (String) obj;
    s.toLowerCase();
}

// After
if (obj instanceof String s) {
    s.toLowerCase();
}
```

### 2.5 Sealed Classes (Java 17+)
```java
// Restrict which classes can extend an abstract class
public sealed class TrackType permits VideoTrack, AudioTrack, SubtitleTrack {}
```

### 2.6 var (Local Variable Type Inference, Java 10+)
```java
// Before
DefaultListModel<String> modelFiles = new DefaultListModel<>();
JFileChooser chooser = new JFileChooser();

// After
var modelFiles = new DefaultListModel<String>();
var chooser = new JFileChooser();
```

### 2.7 Stream.toList() (Java 16+)
```java
// Before
List<String> list = stream.collect(Collectors.toList());

// After
List<String> list = stream.toList(); // Returns unmodifiable list
```

### 2.8 String methods (Java 11+)
```java
// Before
if (str != null && !str.trim().isEmpty())
str.chars().filter(c -> c == ' ').count()

// After
if (str != null && !str.isBlank())    // Java 11
str.repeat(3)                          // Java 11
str.strip()                            // Java 11 (Unicode-aware trim)
str.indent(4)                          // Java 12
```

---

## Phase 3: API Migration

### 3.1 Files API improvements
```java
// Before (Java 8)
BufferedReader reader = new BufferedReader(new InputStreamReader(
    proc.getInputStream()));

// After (Java 21) — try-with-resources + transferTo
try (var reader = new BufferedReader(new InputStreamReader(
        proc.getInputStream()))) {
    reader.transferTo(writer);
}
```

### 3.2 HttpClient (Java 11+) — replaces HttpURLConnection
```java
// Before (Java 8)
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
InputStream in = conn.getInputStream();

// After (Java 21)
var client = HttpClient.newHttpClient();
var request = HttpRequest.newBuilder(URI.create(url)).GET().build();
var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
InputStream in = response.body();
```

### 3.3 ProcessBuilder improvements
```java
// Java 9+: ProcessHandle for PID and process info
long pid = process.pid();
ProcessHandle.current().info().command(); // Get command name
```

### 3.4 Path.of() instead of new File()
```java
// Before
File file = new File("path/to/file");

// After
Path path = Path.of("path", "to", "file");
```

### 3.5 List.of(), Map.of() — Immutable collections (Java 9+)
```java
// Before
List<String> list = Collections.unmodifiableList(Arrays.asList("a", "b"));

// After
List<String> list = List.of("a", "b");
Map<String, Integer> map = Map.of("key", 1);
```

---

## Phase 4: Module System (Optional)

### 4.1 Add module-info.java (if modularizing)
```java
// src/main/java/module-info.java
module io.github.brunorex.jmkvpropedit {
    requires java.desktop;      // Swing
    requires java.logging;      // java.util.logging
    requires java.prefs;        // Preferences
    requires org.apache.commons.io;
    requires org.apache.commons.compress;

    exports io.github.brunorex;
}
```

> **Note:** Module system is optional. Many desktop apps work fine on the classpath.
> Only add module-info.java if you want explicit dependency control.

---

## Phase 5: Removed/Changed APIs

### 5.1 APIs removed in Java 9-21 (check these!)
| Removed API | Replacement | Removed In |
|------------|-------------|------------|
| `javax.xml.bind` (JAXB) | `jakarta.xml.bind` (add dependency) | Java 11 |
| `java.activation` | `jakarta.activation` | Java 11 |
| `java.corba` | N/A | Java 11 |
| `Nashorn` JavaScript engine | GraalJS | Java 15 |
| `SecurityManager` | Deprecated for removal | Java 17 |
| `Applet` API | N/A | Java 17 |
| `finalize()` | `Cleaner` or try-with-resources | Deprecated Java 9 |

### 5.2 Swing-specific changes
- **FlatLaf**: Consider using [FlatLaf](https://www.formdev.com/flatlaf/) for modern look-and-feel on Java 21
- **HiDPI**: Java 9+ has automatic HiDPI scaling on Windows/macOS
- **JavaFX**: Removed from JDK in Java 11, now separate (OpenJFX)

### 5.3 Locale constructor deprecated (Java 19+)
```java
// Before (deprecated)
new Locale("es")

// After
Locale.of("es")      // Java 19+
Locale.forLanguageTag("es")  // Java 7+ (alternative)
```

---

## Phase 6: Verification Checklist

// turbo-all
1. Set JAVA_HOME to JDK 21
2. Run `.\mvnw.cmd clean compile` — fix compilation errors
3. Run `.\mvnw.cmd test` — fix test failures
4. Run `.\mvnw.cmd package` — verify JAR and EXE generation
5. Launch the application and test all tabs (Video, Audio, Subtitle, Attachments)
6. Test with actual MKV files if available
7. Check for deprecation warnings: `.\mvnw.cmd compile -Xlint:deprecation`

---

## Migration Priority for This Project

1. **P1** — Update `pom.xml` (compiler, plugins, dependencies)
2. **P2** — Replace `new Locale()` → `Locale.of()` (deprecated)
3. **P3** — Replace `HttpURLConnection` → `HttpClient` in `MkvToolsDownloader.java`
4. **P4** — Adopt switch expressions, text blocks, var
5. **P5** — Replace `ini4j` → `commons-configuration2`
6. **P6** — Migrate JUnit 4 → JUnit 5
7. **P7** — Consider FlatLaf for modern UI
