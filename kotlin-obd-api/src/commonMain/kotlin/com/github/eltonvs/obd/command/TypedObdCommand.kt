package com.github.eltonvs.obd.command

/**
 * Categories for grouping OBD commands by their function
 */
public enum class CommandCategory {
    /** Engine-related commands (RPM, load, throttle, etc.) */
    ENGINE,
    /** Fuel-related commands (fuel level, consumption, trim, etc.) */
    FUEL,
    /** Temperature-related commands (coolant, intake, ambient, etc.) */
    TEMPERATURE,
    /** Pressure-related commands (fuel pressure, intake manifold, etc.) */
    PRESSURE,
    /** Emission-related commands (oxygen sensors, EGR, etc.) */
    EMISSION,
    /** Control commands (DTCs, MIL, monitors, etc.) */
    CONTROL,
    /** Diagnostic commands (VIN, ECU info, etc.) */
    DIAGNOSTIC,
    /** AT configuration commands for ELM327 adapter */
    AT_CONFIGURATION,
    /** Custom/user-defined commands */
    CUSTOM,
    /** Unknown or uncategorized commands */
    UNKNOWN
}
