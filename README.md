# JMkvpropedit

A Java GUI for mkvpropedit (part of MKVToolNix).

## Prerequisites

- Java 8 or higher
- Maven
- MKVToolNix installed (mkvpropedit executable must be in path or configured)

## Running

### Option 1: Using the Launcher (Recommended)
Double-click `run.bat`.
*Note: If you haven't built the project yet and don't have Maven installed globally, this might fail. See "Build" section.*

### Option 2: Command Line
```bash
java -jar target/jmkvpropedit-1.5.2.jar
```
Or run `JMkvpropedit.exe`.

## Build

### VS Code (Recommended)
1. Install the **Extension Pack for Java** (includes Maven support).
2. Open the **Maven** view in the side bar.
3. Expand `JMkvpropedit` -> `Lifecycle`.
4. Click **`clean`** and then **`package`**.
5. Once "BUILD SUCCESS" appears, you can use `run.bat`.

### Command Line (Requires Maven installed)
```bash
mvn clean package
```

## Structure

- `src/main/java`: Source code.
- `src/main/resources`: Resources (images, properties).
- `launch4j`: Launch4j wrapper tool (bundled).
- `scripts`: Helper scripts.
