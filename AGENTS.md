# Agent Guidelines for JMkvpropedit

This file contains conventions and build instructions for AI agents working on this codebase.

## Project Overview

JMkvpropedit is a Swing-based GUI for mkvpropedit (MKVToolNix). It is built with Maven, targets Java 21, and produces a portable shaded JAR plus an optional Windows `.exe` wrapper.

## Build System

- **Tool**: Maven 3.9+ (wrapper available: `mvnw` / `mvnw.cmd`)
- **Java Version**: 21 (source, target, release)
- **Main Class**: `io.github.brunorex.JMkvpropedit`
- **Default Artifact**: `target/jmkvpropedit-2.3.0.jar`

### Key Commands

```bash
# Compile & run tests
./mvnw test

# Build portable JAR
./mvnw clean package

# Build JAR + Windows EXE (auto-activated on Windows)
mvnw.cmd clean package

# Quality gate (JaCoCo + SpotBugs + OWASP)
./mvnw verify -Pquality
```

## Architecture

The codebase follows a **conservative refactoring** approach: we extract services from the monolithic `JMkvpropedit.java` class without imposing a full MVC framework.

### Package Structure

```
io.github.brunorex
├── JMkvpropedit.java          # Main Swing UI (~5300 lines, monolithic but shrinking)
├── BatchExecutorService.java  # Parallel mkvpropedit execution
├── IniPersistenceService.java # INI file read/write (wraps ini4j)
├── MkvToolsDownloader.java    # MKVToolNix download + ZIP extraction
├── InputValidator.java        # Security validations
├── Commandline.java           # Command-line argument parsing
├── LanguageManager.java       # i18n (properties-based)
├── Utils.java                 # OS detection & string utilities
├── FileDrop.java              # Drag-and-drop support
├── profiles/
│   ├── ProfileManager.java
│   └── TrackProfile.java
└── ... (exceptions, constants, etc.)
```

### Coding Conventions

- **Language**: Java 21 idioms (`var`, `List.of`, records, `String.formatted` where appropriate).
- **Tests**: New tests use **JUnit 5** (Jupiter). Legacy JUnit 4 tests run via the Vintage engine.
- **Mocking**: Mockito 5 (`mockito-junit-jupiter`).
- **Logging**: `java.util.logging` for now (SLF4J migration is a future item).
- **Swing Threading**: Long-running tasks must **never** block the EDT. Use `BatchExecutorService` or `SwingWorker` (`MkvToolsDownloader`).

### Security Rules

1. Validate all file paths with `InputValidator` before use.
2. Prevent path traversal: reject `..` segments in user-provided paths.
3. Verify external downloads with SHA-256 checksums.
4. Sanitize extra commands before passing them to `ProcessBuilder`.

## Git Workflow

- **Branch**: `master`
- **Commit Style**: `type(scope): description` (e.g., `refactor(ui): unify track methods`)
- **Allowed Types**: `feat`, `fix`, `refactor`, `build`, `test`, `chore`, `docs`, `security`
- **Push**: Run `mvnw test` locally before pushing.

## Important Files

| File | Purpose |
|------|---------|
| `pom.xml` | Build config, dependencies, profiles (`windows-exe`, `codesign`, `quality`) |
| `launch4j/config/exe-standalone.xml` | Launch4j config for the EXE wrapper |
| `build/codesign.jks` | Code signing keystore (auto-ignored by `.gitignore`) |
| `src/main/resources/io/github/brunorex/resources/messages*.properties` | i18n strings |

## Known Constraints

- Keep **Swing** (no JavaFX rewrite).
- JAR must remain **portable and self-contained** (`maven-shade-plugin`).
- Do **not** break existing profile INI format.
- Windows file-locking can interfere with JUnit `@TempDir` when using `ini4j`; use system temp files (`File.createTempFile`) for invalid-INI tests.

## Quality Gate Notes

The `quality` profile runs JaCoCo, SpotBugs, and OWASP Dependency-Check.

- **JaCoCo & SpotBugs**: Current versions (JaCoCo 0.8.13, SpotBugs 4.9.x) do not yet support **Java 26** class files (major version 70). If you are running the build on JDK 26, skip these plugins:
  ```bash
  ./mvnw verify -Pquality -Dspotbugs.skip=true -Djacoco.skip=true
  ```
  For full quality gate execution, use a JDK between 21 and 24 (inclusive).
- **OWASP Dependency-Check**: Requires an NVD API key for reasonable execution times. Without a key, the initial download can take 30+ minutes. Register for a free key at https://nvd.nist.gov/developers/request-an-api-key and set it via:
  ```bash
  ./mvnw verify -Pquality -DnvdApiKey=YOUR_KEY
  ```
- **Current Coverage**: ~1% instruction coverage overall, heavily skewed by the untested ~5300-line `JMkvpropedit.java` Swing monolith. The extracted service classes have much higher coverage.

## Testing Tips

- `BatchExecutorService.isExecutableAvailable("java")` is a safe cross-platform test.
- `MkvToolsDownloader.verifyChecksum` is package-private for testing.
- `IniPersistenceService` tests should avoid backslash paths in INI content because `ini4j` interprets `\` as escape; use forward slashes in test fixtures.
