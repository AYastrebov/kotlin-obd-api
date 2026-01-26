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

# Package com.github.eltonvs.obd.command

Core OBD command infrastructure including base classes, response handling, and command execution.

# Package com.github.eltonvs.obd.connection

Connection handling for establishing and managing OBD-II adapter communication.
