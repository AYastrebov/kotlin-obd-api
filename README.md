<p align="center">
  <img width="300px" src="img/kotlin-obd-api-logo.png" />
</p>

<h1 align="center">Kotlin OBD API</h1>

<p align="center">
  <a href="https://github.com/eltonvs/kotlin-obd-api/releases"><img src="https://img.shields.io/github/v/release/eltonvs/kotlin-obd-api?color=7F52FF&logo=github" alt="GitHub Release"/></a>
  <a href="https://github.com/eltonvs/kotlin-obd-api/actions?query=workflow%3ACI"><img src="https://img.shields.io/github/actions/workflow/status/eltonvs/kotlin-obd-api/ci.yml?branch=master&logo=github" alt="CI Status"/></a>
  <a href="https://github.com/eltonvs/kotlin-obd-api/blob/master/LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License Apache 2.0"/></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin 2.3"/>
  <img src="https://img.shields.io/badge/Platforms-Android%20%7C%20JVM%20%7C%20iOS%20%7C%20macOS%20%7C%20Linux%20%7C%20Windows%20%7C%20JS%20%7C%20Wasm-lightgrey" alt="Platforms"/>
</p>

<p align="center">
  <i>A lightweight Kotlin Multiplatform library for querying and parsing OBD-II vehicle diagnostics.</i>
</p>

---

## Features

- **Kotlin Multiplatform** - Works on JVM, Android, iOS, macOS, Linux, Windows, JS (Node.js), and WebAssembly
- **Type-Safe Responses** - Get strongly-typed values (integers, percentages, temperatures, etc.) instead of raw strings
- **100+ Built-in Commands** - Speed, RPM, temperatures, fuel data, trouble codes, and more
- **Easy to Extend** - Create custom commands by extending base classes
- **Flexible Connection** - Plug in any transport layer (Bluetooth, WiFi, USB) via [kotlinx-io](https://github.com/Kotlin/kotlinx-io)
- **Built-in Caching** - LRU cache for commands that don't change frequently (like VIN)
- **Thread-Safe** - Safe to use from multiple coroutines

## Installation

Add the dependency to your project:

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.github.eltonvs.obd:kotlin-obd-api:1.4.0")
}
```

### Gradle (Groovy)

```gradle
dependencies {
    implementation 'com.github.eltonvs.obd:kotlin-obd-api:1.4.0'
}
```

> **Note:** Check the [releases page](https://github.com/eltonvs/kotlin-obd-api/releases) for the latest version.

## Quick Start

### 1. Create a Connection

Get a `Source` and `Sink` from your connection interface using [kotlinx-io](https://github.com/Kotlin/kotlinx-io):

```kotlin
val obdConnection = ObdDeviceConnection(source, sink)
```

On JVM, you can convert Java streams:

```kotlin
import kotlinx.io.asSource
import kotlinx.io.asSink
import kotlinx.io.buffered

val obdConnection = ObdDeviceConnection(
    inputStream.asSource().buffered(),
    outputStream.asSink().buffered()
)
```

### 2. Run Commands

```kotlin
// Get vehicle speed
val response = obdConnection.run(SpeedCommand())
println("Speed: ${response.value} ${response.unit}") // "Speed: 60 Km/h"

// Use caching for static data like VIN
val vinResponse = obdConnection.run(VINCommand(), useCache = true)

// Add delay between commands if needed
val rpmResponse = obdConnection.run(RPMCommand(), delayTime = 500L)
```

### 3. Access Response Data

The `ObdResponse` object provides:

| Attribute | Type | Description |
| :- | :- | :- |
| `command` | `ObdCommand` | The command that was executed |
| `rawResponse` | `ObdRawResponse` | Raw hex data from the vehicle |
| `value` | `String` | Parsed human-readable value |
| `unit` | `String` | Unit of measurement (Km/h, RPM, °C, etc.) |
| `typedValue` | `TypedValue<T>` | Strongly-typed value |

## Type-Safe Commands

All commands return `TypedValue` responses:

```kotlin
val response = obdConnection.run(SpeedCommand())

// Access the typed value
when (val typed = response.typedValue) {
    is IntegerValue -> println("Speed: ${typed.value} km/h")
    is PercentageValue -> println("Percentage: ${typed.value}%")
    is TemperatureValue -> println("Temp: ${typed.value}°C")
    // ... handle other types
}
```

**Available TypedValue types:**
- `IntegerValue` - Whole numbers (speed, RPM, distance)
- `FloatValue` - Decimal values (voltage, MAF)
- `PercentageValue` - Percentages (throttle position, fuel level)
- `TemperatureValue` - Temperatures (coolant, air intake)
- `PressureValue` - Pressure values (fuel, manifold)
- `DurationValue` - Time values (engine runtime)
- `BooleanValue` - Binary flags (MIL status)
- `StringValue` - Text values (VIN)
- `ListValue` - Lists (trouble codes)
- `EnumValue` - Enumeration values (fuel type)
- `CompositeValue` - Map of named values (monitor status)

## Custom Commands

Extend one of the base classes for automatic parsing:

```kotlin
class CustomSpeedCommand : IntegerObdCommand() {
    override val tag = "CUSTOM_SPEED"
    override val name = "Custom Speed"
    override val mode = "01"
    override val pid = "FF"
    override val defaultUnit = "Km/h"
    override val bytesToProcess = 1
    override val category = CommandCategory.ENGINE
}
```

**Available base classes:**

| Class | Use Case |
|-------|----------|
| `IntegerObdCommand` | Whole numbers |
| `FloatObdCommand` | Decimal values |
| `PercentageObdCommand` | Percentage values (0-100) |
| `TemperatureObdCommand` | Temperatures (handles -40°C offset) |
| `PressureObdCommand` | Pressure calculations |
| `BooleanObdCommand` | Binary flags |
| `DurationObdCommand` | Time values |

## Supported OBD Modes

| Mode | Description |
|------|-------------|
| **01** | Current/live data - 50+ PIDs (speed, RPM, temperatures, fuel, etc.) |
| **03** | Read stored trouble codes (DTCs) |
| **04** | Clear trouble codes and MIL |
| **07** | Read pending trouble codes |
| **09** | Vehicle information (VIN) |
| **0A** | Permanent trouble codes |
| **AT** | ELM327 adapter configuration commands |

> **Note:** Command support varies by vehicle. Use `AvailableCommandsCommand` to query supported PIDs.

## Supported Commands

A selection of commonly used commands:

- **Engine:** Speed, RPM, Load, Runtime, Coolant Temperature, Oil Temperature
- **Fuel:** Level, Pressure, Type, Consumption Rate, Ethanol Level
- **Air:** Intake Temperature, MAF, Throttle Position, Barometric Pressure
- **Diagnostics:** Trouble Codes (stored, pending, permanent), MIL Status, Clear Codes
- **Vehicle Info:** VIN, Control Module Voltage

For the complete list of 100+ commands, see [SUPPORTED_COMMANDS.md](SUPPORTED_COMMANDS.md).

## Contributing

Want to help or have something to add?

1. [Open an issue](https://github.com/eltonvs/kotlin-obd-api/issues) to discuss your idea
2. Fork the repository and create your branch
3. Submit a [Pull Request](https://github.com/eltonvs/kotlin-obd-api/pulls)

## Versioning

We use [SemVer](http://semver.org/) for versioning. For available versions, see the [tags](https://github.com/eltonvs/kotlin-obd-api/tags).

## Authors

- **Elton Viana** - Initial work - Also created [java-obd-api](https://github.com/eltonvs/java-obd-api)

See the list of [contributors](https://github.com/eltonvs/kotlin-obd-api/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Paulo Pires** - Creator of [obd-java-api](https://github.com/pires/obd-java-api), which inspired the initial design
- **[SmartMetropolis Project](http://smartmetropolis.imd.ufrn.br/)** (Digital Metropolis Institute - UFRN, Brazil) - Sponsored initial development
- **[Ivanovitch Silva](https://github.com/ivanovitchm)** - OBD research guidance
