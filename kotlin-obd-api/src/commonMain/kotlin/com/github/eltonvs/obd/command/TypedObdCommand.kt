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

/**
 * Abstract base class for typed OBD commands that provide type-safe response values.
 *
 * Extend this class to create commands that return strongly-typed values while
 * maintaining backward compatibility with the string-based [ObdCommand] interface.
 *
 * Example:
 * ```kotlin
 * class CustomSpeedCommand : TypedObdCommand<Long>() {
 *     override val tag = "CUSTOM_SPEED"
 *     override val name = "Custom Speed"
 *     override val mode = "01"
 *     override val pid = "0D"
 *     override val defaultUnit = "Km/h"
 *     override val category = CommandCategory.ENGINE
 *
 *     override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Long> {
 *         val speed = bytesToInt(rawResponse.bufferedValue, bytesToProcess = 1)
 *         return TypedValue.IntegerValue(speed, unit = defaultUnit)
 *     }
 * }
 * ```
 *
 * @param T The type of value this command returns
 */
public abstract class TypedObdCommand<T> : ObdCommand() {
    /**
     * The category this command belongs to
     */
    public open val category: CommandCategory = CommandCategory.UNKNOWN

    /**
     * Parse the raw response and return a strongly-typed value.
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return A [TypedValue] containing the parsed result
     */
    public abstract fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<T>

    /**
     * Bridges to legacy handler by extracting stringValue from typed result.
     * This ensures backward compatibility with code expecting string responses.
     */
    final override val handler: (ObdRawResponse) -> String
        get() = { parseTypedValue(it).stringValue }

    /**
     * Handles the response and returns an [ObdResponse] with both
     * string value and typed value populated.
     */
    override fun handleResponse(rawResponse: ObdRawResponse): ObdResponse {
        val checkedRawResponse = BadResponseException.checkForExceptions(this, rawResponse)
        val typedValue = parseTypedValue(checkedRawResponse)
        return ObdResponse(
            command = this,
            rawResponse = checkedRawResponse,
            value = typedValue.stringValue,
            unit = defaultUnit,
            typedValue = typedValue
        )
    }
}
