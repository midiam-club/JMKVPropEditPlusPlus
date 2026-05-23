# Agent Guidelines for JMkvpropedit

This file contains conventions and build instructions for AI agents working on this codebase.

## Project Overview

JMkvpropedit is a Swing-based GUI for mkvpropedit (MKVToolNix). It is built with Maven, targets Java 21, and produces a portable shaded JAR plus an optional Windows `.exe` wrapper.

## Build System

- **Tool**: Maven 3.9+ (wrapper available: `mvnw` / `mvnw.cmd`)
- **Java Version**: 21 (source, target, release)
- **Main Class**: `io.github.brunorex.JMkvpropedit`
- **Default Artifact**: `target/jmkvpropedit-2.5.0.jar`

### Key Commands

```bash
# Compile & run tests
./mvnw test

# Build portable JAR only
./mvnw clean package -DgenerateExe=false

# Build JAR + Windows EXE (default on Windows, or explicit)
mvnw.cmd clean package

# Quality gate (JaCoCo + SpotBugs + OWASP)
./mvnw verify -Pquality
```

## Architecture

The codebase follows a **conservative refactoring** approach: we extract services and UI panels from the monolithic `JMkvpropedit.java` class without imposing a full MVC framework.

### Package Structure

```
io.github.brunorex
├── JMkvpropedit.java          # Main Swing UI (~693 lines, down from ~5300)
├── BatchExecutorService.java  # Parallel mkvpropedit execution
├── IniPersistenceService.java # INI file read/write (wraps ini4j)
├── MkvToolsDownloader.java    # MKVToolNix download + ZIP extraction
├── InputValidator.java        # Security validations
├── Commandline.java           # Command-line argument parsing
├── LanguageManager.java       # i18n (properties-based)
├── Utils.java                 # OS detection & string utilities
├── FileDrop.java              # Drag-and-drop support
├── InputTabPanel.java         # Input file list, drag-and-drop, add/remove buttons
├── GeneralTabPanel.java       # Title, chapters, tags, extra command-line options
├── OptionsTabPanel.java       # Executable path, language, theme selection
├── TrackTabPanel.java         # Track-specific editing (video/audio/subtitles)
├── AttachmentsTabPanel.java   # Attachment management
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
| `pom.xml` | Build config, dependencies, profiles (`windows-exe`, `quality`) |
| `launch4j/config/exe-standalone.xml` | Launch4j config for the EXE wrapper |
| `src/main/resources/io/github/brunorex/resources/messages*.properties` | i18n strings |
| `InputTabPanel.java` | Input file list, drag-and-drop, add/remove buttons |
| `GeneralTabPanel.java` | Title, chapters, tags, extra command-line options |
| `OptionsTabPanel.java` | Executable path, language, theme selection |
| `TrackTabPanel.java` | Track-specific editing (video/audio/subtitles) |
| `AttachmentsTabPanel.java` | Attachment management |

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

## CI / GitHub Actions

The workflow `.github/workflows/build.yml` runs on every push/PR and on tags `v*`:

| Job | Trigger | Platforms | Notes |
|---|---|---|---|
| `build` | push, PR | ubuntu, windows, macos | Builds JAR (+ EXE on Windows when `-DgenerateExe=true`), uploads artifacts |
| `quality` | push, PR | ubuntu | Runs JaCoCo, SpotBugs, OWASP |
| `release` | tag `v*` | windows | Creates GitHub Release with JAR + EXE assets |

### EXE generation

The `windows-exe` Maven profile auto-activates on Windows only when `-DgenerateExe=true` (the default). CI:
- **Build job**: passes `-DgenerateExe=false` to skip EXE creation (prevents CI-specific Launch4j failures).
- **Release job**: builds EXE via a PowerShell step with absolute paths; failure is non-fatal (JAR-only release still succeeds).

### Code-signing secrets

To enable automatic EXE code signing in CI without committing the keystore to Git:

1. Encode the keystore as base64 (PowerShell):
   ```powershell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("build/codesign.jks")) | Set-Content -NoNewline "build/codesign.jks.b64"
   ```
   Or Bash:
   ```bash
   base64 -w 0 build/codesign.jks > build/codesign.jks.b64
   ```
2. Go to **Settings > Secrets and variables > Actions > New repository secret**.
3. Add:
   - `CODESIGN_JKS_B64`   – entire content of `build/codesign.jks.b64`
   - `CODESIGN_STOREPASS` – keystore password
   - `CODESIGN_KEYPASS`   – key password

The CI workflow decodes `CODESIGN_JKS_B64` into `build/codesign.jks` at build time, so the binary keystore never lives in source control. The `codesign` Maven profile auto-activates only when `build/codesign.jks` exists, so PRs from forks that lack the secret will simply skip signing.

## Testing Tips

- `BatchExecutorService.isExecutableAvailable("java")` is a safe cross-platform test.
- `MkvToolsDownloader.verifyChecksum` is package-private for testing.
- `IniPersistenceService` tests should avoid backslash paths in INI content because `ini4j` interprets `\` as escape; use forward slashes in test fixtures.
- **Swing integration tests** (`JMkvpropeditIntegrationTest`) require a graphical environment. They are automatically skipped in headless mode (`java.awt.headless=true`). To run them in CI or headless environments, use a virtual display (Xvfb on Linux) or run locally on Windows/macOS:
  ```bash
  # Windows / macOS / Linux with display
  ./mvnw test

  # Force headless=false explicitly
  JAVA_TOOL_OPTIONS="-Djava.awt.headless=false" ./mvnw test
  ```
- Integration tests navigate extracted panels via reflection (`getInputTabPanel()` helper) rather than accessing `JMkvpropedit` fields directly.
