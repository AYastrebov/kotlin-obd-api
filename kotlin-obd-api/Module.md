# Module kotlin-obd-api

Kotlin Multiplatform OBD-II API library for communicating with vehicles via the OBD-II (On-Board Diagnostics) protocol.

## Features

- Full Kotlin Multiplatform support (JVM, Android, iOS, macOS, Linux, Windows, JS, WASM)
- Coroutine-based async API
- Comprehensive set of OBD-II commands
- Extensible command architecture
- Type-safe responses

## Getting Started

Add the dependency to your project:

```kotlin
implementation("com.github.eltonvs.obd:kotlin-obd-api:<version>")
```

## Package Structure

| Package | Description |
|---------|-------------|
| [com.github.eltonvs.obd.command] | OBD command definitions and base classes |
| [com.github.eltonvs.obd.command.control] | Control module commands |
| [com.github.eltonvs.obd.command.engine] | Engine-related commands |
| [com.github.eltonvs.obd.command.fuel] | Fuel system commands |
| [com.github.eltonvs.obd.command.pressure] | Pressure sensor commands |
| [com.github.eltonvs.obd.command.temperature] | Temperature sensor commands |
| [com.github.eltonvs.obd.connection] | Connection handling and communication |

# Package com.github.eltonvs.obd.command

Core OBD command infrastructure including base classes, response handling, and command execution.

# Package com.github.eltonvs.obd.command.control

Commands for reading control module information such as DTCs (Diagnostic Trouble Codes).

# Package com.github.eltonvs.obd.command.engine

Engine-related commands including RPM, load, and runtime.

# Package com.github.eltonvs.obd.command.fuel

Fuel system commands for reading fuel level, consumption, and related data.

# Package com.github.eltonvs.obd.command.pressure

Pressure sensor commands for manifold, fuel rail, and barometric pressure.

# Package com.github.eltonvs.obd.command.temperature

Temperature sensor commands for coolant, intake air, and ambient temperature.

# Package com.github.eltonvs.obd.connection

Connection handling for establishing and managing OBD-II adapter communication.
