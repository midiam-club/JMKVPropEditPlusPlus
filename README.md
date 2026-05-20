# JMkvpropedit

A Java GUI for [mkvpropedit](https://mkvtoolnix.download/doc/mkvpropedit.html) (part of MKVToolNix).

## Prerequisites

- **Java 21** or higher (LTS recommended)
- **Maven 3.9+** (or use the included wrapper `mvnw` / `mvnw.cmd`)
- **MKVToolNix** installed (mkvpropedit executable must be in PATH or configured)

## Running

### Option 1: Using the Launcher (Recommended on Windows)
Double-click `JMkvpropedit.exe`.

### Option 2: Command Line (Universal)
```bash
java -jar target/jmkvpropedit-2.3.0.jar
```

### Option 3: Development Launch
```bash
# Linux / macOS
./mvnw exec:java -Dexec.mainClass="io.github.brunorex.JMkvpropedit"

# Windows
mvnw.cmd exec:java -Dexec.mainClass="io.github.brunorex.JMkvpropedit"
```

## Build

### Multiplatform JAR (Default)
```bash
./mvnw clean package
```
Produces `target/jmkvpropedit-2.3.0.jar` — a self-contained, portable JAR with all dependencies shaded inside.

### Windows .exe Wrapper
On Windows, the `windows-exe` profile activates automatically and generates `JMkvpropedit.exe` via launch4j.
```bash
mvnw.cmd clean package
```

### Code Signing (Optional)
If a keystore exists at `build/codesign.jks`, the `codesign` profile signs the EXE automatically during packaging.
Generate a self-signed dev certificate:
```bash
keytool -genkeypair -alias codesign -keyalg RSA -keysize 2048 -validity 365 \
  -keystore build/codesign.jks -storepass jmkvpropedit \
  -dname "CN=JMkvpropedit, O=JMkvpropedit, C=ES" \
  -ext "ExtendedKeyUsage=codeSigning"
```

### Quality Gate (Optional)
```bash
./mvnw verify -Pquality
```
Runs JaCoCo (coverage), SpotBugs (static analysis), and OWASP Dependency-Check (CVE scanning).

## Features

- **Modern Look & Feel** — Powered by [FlatLaf](https://www.formdev.com/flatlaf/) with Light, Dark, Darcula, and IntelliJ themes (persisted across sessions).
- **Batch Processing** — Edit metadata for multiple MKV files in parallel.
- **Profile System** — Save and load track configurations as reusable profiles.
- **Auto-Download MKVToolNix** — If mkvpropedit is missing on Windows, the app can download it automatically with SHA-256 verification.
- **Security Hardened** — Input validation, path traversal prevention, command sanitization, and download checksum verification.
- **Multiplatform** — Windows, macOS, and Linux support.

## Structure

- `src/main/java` — Source code.
- `src/main/resources` — Resources (images, i18n properties).
- `src/test/java` — Unit tests (JUnit 5 + Mockito).
- `launch4j/` — Launch4j wrapper configuration and binaries.
- `build/` — Build artifacts (code signing keystore, etc.).

## Technology Stack

| Component | Version |
|-----------|---------|
| Java | 21 LTS |
| Swing + FlatLaf | 3.5.4 |
| Apache Commons Compress | 1.27.1 |
| ini4j | 0.5.4 |
| JUnit | 5.12.2 |
| Mockito | 5.17.0 |

## License

See the project's license file for details.
