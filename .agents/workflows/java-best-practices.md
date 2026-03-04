---
description: Java best practices, design patterns, and code quality standards for this project
---

# Java Best Practices & Design Patterns Workflow

## 1. Code Style & Conventions
- Use Java naming conventions (camelCase for methods/fields, PascalCase for classes)
- Every class/method should have a clear single responsibility (SRP)
- Prefer immutability: use `final` fields, return defensive copies of collections
- Avoid magic numbers/strings: use named constants
- Max method length: ~30 lines; max class length: ~300 lines (split if larger)

## 2. Design Patterns to Apply
- **Command Pattern**: encapsulate CLI argument building into Command objects instead of string concatenation
- **Strategy Pattern**: file processing logic (apply per-track options) as interchangeable strategies  
- **Observer/Callback**: already partially used; formalize with typed interfaces
- **Factory**: create track option objects via factory methods
- **Builder**: build `ProcessBuilder` command lists using a dedicated CommandBuilder class

## 3. Performance
- Avoid `String` concatenation in loops — use `StringBuilder`
- Prefer `ProcessBuilder(List<String>)` over shell string expansion
- Use try-with-resources consistently (already partially done)
- Avoid `System.gc()` calls — they are hints only and often counterproductive
- Cache frequently accessed data (e.g., language lists)

## 4. Security (see security_audit_report.md)
- Never concatenate user input into shell strings — use argument lists
- Verify file integrity (checksums) after network downloads
- Use temp files with restricted permissions for transient data

## 5. Error Handling
- Never swallow exceptions with empty catch blocks
- Use structured logging (java.util.logging or SLF4J) instead of System.out
- Provide meaningful error messages to the user

## 6. Testing
- Run existing tests: `.\mvnw.cmd test`
- Aim for unit tests on all public utility methods (Utils, InputValidator)
- Mock external process calls in tests

## 7. Dependency Management
- Keep dependencies up to date (check with `.\mvnw.cmd versions:display-dependency-updates`)
- Replace unmaintained libs (ini4j → commons-configuration2)
