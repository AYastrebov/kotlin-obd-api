# Kotlin OBD API

A Kotlin Multiplatform library for communicating with OBD-II (On-Board Diagnostics) vehicle adapters.

## Project Overview

This library provides a developer-friendly interface to execute OBD commands, parse responses, and access vehicle diagnostic data (speed, RPM, temperature, fuel level, trouble codes, etc.) without dealing with low-level protocol details.

**Supported Platforms:** JVM, Android, iOS, macOS, Linux, Windows, JS, WebAssembly

## Quick Reference

### Build & Test

```bash
./gradlew build          # Build all targets
./gradlew allTests       # Run all tests
./gradlew dokkaGenerate  # Generate documentation
```

### Project Structure

```
kotlin-obd-api/
├── kotlin-obd-api/src/
│   ├── commonMain/kotlin/com/github/eltonvs/obd/
│   │   ├── command/           # Command framework
│   │   │   ├── ObdCommand.kt         # Base class for all commands
│   │   │   ├── TypedObdCommand.kt    # Type-safe command base
│   │   │   ├── CommonCommands.kt     # Abstract base commands
│   │   │   ├── CommandRegistry.kt    # Command discovery
│   │   │   ├── Response.kt           # ObdRawResponse & ObdResponse
│   │   │   ├── TypedValue.kt         # Sealed class for typed responses
│   │   │   ├── Exceptions.kt         # Exception hierarchy
│   │   │   ├── engine/               # Speed, RPM, load commands
│   │   │   ├── temperature/          # Temperature commands
│   │   │   ├── pressure/             # Pressure commands
│   │   │   ├── fuel/                 # Fuel-related commands
│   │   │   ├── control/              # Diagnostic commands
│   │   │   └── at/                   # ELM327 AT commands
│   │   └── connection/
│   │       └── ObdDeviceConnection.kt  # I/O handler with caching
│   └── commonTest/kotlin/     # Shared test suite
├── build.gradle.kts           # Root build configuration
└── gradle/libs.versions.toml  # Dependency versions
```

## Architecture

### Core Classes

| Class | Purpose |
|-------|---------|
| `ObdCommand` | Abstract base for all OBD commands (tag, name, mode, pid) |
| `TypedObdCommand<T>` | Type-safe command returning `TypedValue<T>` |
| `ObdDeviceConnection` | Handles I/O with adapter, manages LRU cache |
| `ObdRawResponse` | Raw hex response with lazy processing pipeline |
| `ObdResponse` | Processed response with value, unit, and typed value |
| `CommandRegistry` | Static registry for command discovery by mode/pid |

### TypedValue Hierarchy

```kotlin
TypedValue<T>
├── IntegerValue(Long)
├── FloatValue(Float)
├── PercentageValue(Float)
├── TemperatureValue(Float)
├── PressureValue(Float)
├── StringValue(String)
├── EnumValue<E : Enum<E>>
├── BooleanValue(Boolean)
├── ListValue<T>(List<T>)
├── CompositeValue(Map<String, Any>)
└── DurationValue(Long - seconds)
```

### Abstract Base Commands

Extend these for custom commands:

| Class | Use Case |
|-------|----------|
| `IntegerObdCommand` | Whole numbers (speed, RPM, distance) |
| `PercentageObdCommand` | Percentages (0-100 range) |
| `TemperatureObdCommand` | Temperature with -40°C offset |
| `PressureObdCommand` | Pressure calculations |
| `EnumObdCommand<E>` | Discrete states (fuel type) |
| `BooleanObdCommand` | Binary flags (MIL status) |
| `DurationObdCommand` | Time values (runtime) |
| `FloatObdCommand` | Decimal values (voltage, MAF) |

## Code Conventions

- **Kotlin style** enforced via explicit API mode
- **Java 17** toolchain
- **Sealed classes** for exhaustive type checking
- **Suspend functions** for all I/O operations
- **Mutex** for thread-safe cache access
- Package structure by functionality (engine, temperature, pressure, fuel)

## Key Dependencies

| Dependency | Purpose |
|------------|---------|
| `kotlinx-coroutines-core` (1.10.2) | Async/suspend functions |
| `kotlinx-io-core` (0.8.2) | Multiplatform I/O |
| `kotlin-test` | Testing framework |

## Creating Custom Commands

### Simple (string-based):
```kotlin
class CustomCommand : ObdCommand() {
    override val tag = "CUSTOM"
    override val name = "Custom Command"
    override val mode = "01"
    override val pid = "FF"
    override val defaultUnit = "unit"
    override val handler: (ObdRawResponse) -> String = { "parsed value" }
}
```

### Type-safe (recommended):
```kotlin
class CustomCommand : IntegerObdCommand() {
    override val tag = "CUSTOM"
    override val name = "Custom"
    override val mode = "01"
    override val pid = "FF"
    override val defaultUnit = "unit"
    override val bytesToProcess = 2
    override val multiplier = 0.5f
    override val category = CommandCategory.ENGINE
}
```

### Registration:
```kotlin
CommandRegistry.register("CUSTOM", CommandCategory.ENGINE, "01", "FF") { CustomCommand() }
```

## Testing

Tests are in `commonTest/kotlin/` using `runTest` for coroutines. Mock streams using `Buffer` from kotlinx-io.

```bash
./gradlew allTests                              # All platforms
./gradlew commonTest --tests "*ConnectionTest*" # Specific test
```

## Response Processing Pipeline

```
Raw Hex → Remove whitespace → Remove "SEARCHING" → Remove bus init messages
→ Remove colons → Convert to IntArray → Parse with handler → ObdResponse
```

## OBD Modes Supported

- **Mode 01**: Current/live data (100+ PIDs)
- **Mode 03**: Read trouble codes (DTCs)
- **Mode 04**: Clear trouble codes
- **Mode 07**: Pending trouble codes
- **Mode 09**: VIN and vehicle info
- **Mode 0A**: Permanent trouble codes
- **AT Commands**: ELM327 adapter configuration
