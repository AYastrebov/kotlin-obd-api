# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin Multiplatform library for communicating with OBD-II vehicle adapters. Provides a type-safe interface to execute OBD commands, parse hex responses, and access vehicle diagnostic data.

**Platforms:** JVM, Android, iOS, macOS, Linux, Windows, JS, WebAssembly

## Build & Test Commands

```bash
./gradlew build                                   # Build all targets
./gradlew allTests                                # Run all platform tests
./gradlew :kotlin-obd-api:jvmTest                 # Run JVM tests only (fastest)
./gradlew :kotlin-obd-api:jvmTest --tests "com.github.eltonvs.obd.command.engine.EngineTests"  # Single test class
./gradlew dokkaGenerate                           # Generate API docs
```

The project is a single Gradle module at `:kotlin-obd-api`. Root project name is `obd-api`.

## Architecture

### Unified command system

`ObdCommand` is the single abstract base class. All commands implement `parseTypedValue(rawResponse: ObdRawResponse): TypedValue<*>` to return typed responses. The `handleResponse()` method calls `parseTypedValue()` and wraps the result in `ObdResponse`.

### Command class hierarchy

`ObdCommand` → abstract base commands in `CommonCommands.kt`:
- `IntegerObdCommand`, `FloatObdCommand`, `PercentageObdCommand`, `TemperatureObdCommand`, `PressureObdCommand`, `EnumObdCommand<E>`, `BooleanObdCommand`, `DurationObdCommand`

Each base command computes its value from formula properties (`bytesToProcess`, `multiplier`, `offset`, etc.) — override properties instead of implementing `parseTypedValue` directly.

### Response processing pipeline

```
Raw hex string → ObdRawResponse(value, elapsedTime)
  → processedValue (lazy): remove whitespace → remove bus init → remove colons
  → bufferedValue (lazy): chunked hex pairs → IntArray
  → command.handleResponse() → ObdResponse(value, unit, typedValue)
```

### Connection

`ObdDeviceConnection` takes `Source` (input) and `Sink` (output) from kotlinx-io. Uses `>` as response delimiter. Has LRU cache (mutex-protected) for repeated command reads. All I/O uses `Dispatchers.Default` (not IO) for multiplatform compatibility.

### Command registration

`CommandRegistry` provides static discovery by mode/pid. Commands register via `CommandRegistry.register(tag, category, mode, pid) { Factory() }`.

## Code Conventions

- **Explicit API mode** — all public declarations need explicit visibility modifiers
- **Java 17 toolchain**
- Package structure by functionality: `engine/`, `temperature/`, `pressure/`, `fuel/`, `control/`, `at/`, `egr/`
- All I/O operations are `suspend` functions
- Tests use `runTest` from kotlinx-coroutines-test and `Buffer` from kotlinx-io for mock streams
- Test files live in `commonTest/kotlin/` — shared across all platforms

## Key File Paths

| File | Purpose |
|------|---------|
| `kotlin-obd-api/src/commonMain/kotlin/com/github/eltonvs/obd/command/ObdCommand.kt` | Base command class with parseTypedValue |
| `kotlin-obd-api/src/commonMain/kotlin/com/github/eltonvs/obd/command/TypedObdCommand.kt` | CommandCategory enum |
| `kotlin-obd-api/src/commonMain/kotlin/com/github/eltonvs/obd/command/CommonCommands.kt` | All abstract base commands (Integer, Float, Percentage, etc.) |
| `kotlin-obd-api/src/commonMain/kotlin/com/github/eltonvs/obd/command/Response.kt` | ObdRawResponse, ObdResponse, processing pipeline |
| `kotlin-obd-api/src/commonMain/kotlin/com/github/eltonvs/obd/command/TypedValue.kt` | Sealed TypedValue hierarchy |
| `kotlin-obd-api/src/commonMain/kotlin/com/github/eltonvs/obd/connection/ObdDeviceConnection.kt` | I/O + caching |
| `kotlin-obd-api/src/commonMain/kotlin/com/github/eltonvs/obd/command/CommandRegistry.kt` | Command discovery |
| `kotlin-obd-api/build.gradle.kts` | Build config, publishing, Dokka |
| `gradle/libs.versions.toml` | Dependency versions |
