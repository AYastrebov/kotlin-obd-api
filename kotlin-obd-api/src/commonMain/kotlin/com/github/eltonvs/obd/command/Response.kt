package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.command.RegexPatterns.BUS_INIT_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.COLON_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN

/**
 * Applies a pipeline of transformation functions to a value.
 *
 * @param T The type of value being transformed
 * @param functions The transformation functions to apply in order
 * @return The transformed value after all functions have been applied
 */
public fun <T> T.pipe(vararg functions: (T) -> T): T =
    functions.fold(this) { value, f -> f(value) }

/**
 * Represents a raw response received from the OBD adapter.
 *
 * This class handles the initial processing of the raw string response,
 * including whitespace removal, bus initialization message cleanup, and
 * conversion to a byte array for further parsing.
 *
 * @property value The raw string response from the OBD adapter
 * @property elapsedTime Time in milliseconds taken to receive the response
 */
public data class ObdRawResponse(
    val value: String,
    val elapsedTime: Long
) {
    private val valueProcessorPipeline: Array<(String) -> String> by lazy {
        arrayOf<(String) -> String>(
            {
                /*
                 * Imagine the following response 41 0c 00 0d.
                 *
                 * ELM sends strings!! So, ELM puts spaces between each "byte". And pay
                 * attention to the fact that I've put the word byte in quotes, because 41
                 * is actually TWO bytes (two chars) in the socket. So, we must do some more
                 * processing...
                 */
                removeAll(WHITESPACE_PATTERN, it) // removes all [ \t\n\x0B\f\r]
            },
            {
                /*
                 * Data may have echo or informative text like "INIT BUS..." or similar.
                 * The response ends with two carriage return characters. So we need to take
                 * everything from the last carriage return before those two (trimmed above).
                 */
                removeAll(BUS_INIT_PATTERN, it)
            },
            {
                removeAll(COLON_PATTERN, it)
            }
        )
    }

    /**
     * The response value after processing (whitespace and bus init messages removed).
     *
     * This is a cleaned-up version of the raw value with all whitespace,
     * bus initialization messages, and colons removed.
     */
    public val processedValue: String by lazy { value.pipe(*valueProcessorPipeline) }

    /**
     * The response as an array of integer byte values.
     *
     * Each pair of hex characters in [processedValue] is converted to its integer value.
     * For example, "410D50" becomes [0x41, 0x0D, 0x50] = [65, 13, 80].
     *
     * This array is used by parsers to extract data bytes from the response.
     * Typically:
     * - Index 0: Mode + 0x40 (response mode)
     * - Index 1+: PID bytes (1 or 2 bytes depending on mode)
     * - Remaining: Data bytes
     */
    public val bufferedValue: IntArray by lazy { processedValue.chunked(2) { it.toString().toInt(radix = 16) }.toIntArray() }
}

/**
 * Represents a processed OBD command response with both string and typed values.
 *
 * This class combines the original command, raw response, parsed string value,
 * and optionally a strongly-typed value for type-safe access to the result.
 *
 * Example usage:
 * ```kotlin
 * val response = command.handleResponse(rawResponse)
 *
 * // String access (backward compatible)
 * println("${response.value} ${response.unit}")  // "80 km/h"
 *
 * // Type-safe access (new)
 * val speed = response.asInt()  // 80L
 * val typed = response.typedValue as? TypedValue.IntegerValue
 * ```
 *
 * @property command The command that produced this response
 * @property rawResponse The raw response from the OBD adapter
 * @property value The parsed string value for display
 * @property unit The unit string (e.g., "km/h", "%", "°C")
 * @property typedValue Strongly-typed value for type-safe access
 * @see TypedValue
 */
public data class ObdResponse(
    val command: ObdCommand,
    val rawResponse: ObdRawResponse,
    val value: String,
    val unit: String = "",
    val typedValue: TypedValue<*> = TypedValue.StringValue(value)
) {
    /**
     * The formatted value including unit, using the command's format method.
     *
     * @return Formatted string like "80 km/h" or "25.5 °C"
     */
    public val formattedValue: String get() = command.format(this)

    /**
     * Returns the value as Long if it's an [TypedValue.IntegerValue], null otherwise.
     *
     * Use this for integer values like speed, RPM, or distance.
     *
     * @return The Long value, or null if not an integer type
     */
    public fun asInt(): Long? = (typedValue as? TypedValue.IntegerValue)?.value

    /**
     * Returns the value as Float if it's a [TypedValue.FloatValue], null otherwise.
     *
     * Use this for floating-point values like voltage or mass air flow.
     *
     * @return The Float value, or null if not a float type
     */
    public fun asFloat(): Float? = (typedValue as? TypedValue.FloatValue)?.value

    /**
     * Returns the value as Float if it's a [TypedValue.PercentageValue], null otherwise.
     *
     * Use this for percentage values like throttle position or engine load.
     *
     * @return The percentage as Float (0-100), or null if not a percentage type
     */
    public fun asPercentage(): Float? = (typedValue as? TypedValue.PercentageValue)?.value

    /**
     * Returns the value as Float if it's a [TypedValue.TemperatureValue], null otherwise.
     *
     * Use this for temperature values like coolant or intake air temperature.
     *
     * @return The temperature as Float, or null if not a temperature type
     */
    public fun asTemperature(): Float? = (typedValue as? TypedValue.TemperatureValue)?.value

    /**
     * Returns the value as Float if it's a [TypedValue.PressureValue], null otherwise.
     *
     * Use this for pressure values like fuel pressure or intake manifold pressure.
     *
     * @return The pressure as Float, or null if not a pressure type
     */
    public fun asPressure(): Float? = (typedValue as? TypedValue.PressureValue)?.value

    /**
     * Returns the value as Boolean if it's a [TypedValue.BooleanValue], null otherwise.
     *
     * Use this for boolean flags like MIL status or A/C status.
     *
     * @return The Boolean value, or null if not a boolean type
     */
    public fun asBoolean(): Boolean? = (typedValue as? TypedValue.BooleanValue)?.value

    /**
     * Returns the value as the specified enum type if it's an [TypedValue.EnumValue], null otherwise.
     *
     * Use this for enumerated values like fuel type or OBD standard.
     *
     * @param E The expected enum type
     * @return The enum value, or null if not an enum type or wrong enum type
     */
    @Suppress("UNCHECKED_CAST")
    public fun <E : Enum<E>> asEnum(): E? = (typedValue as? TypedValue.EnumValue<E>)?.value

    /**
     * Returns the value as a List if it's a [TypedValue.ListValue], null otherwise.
     *
     * Use this for multi-value responses like DTCs or supported PIDs.
     *
     * @param T The expected element type
     * @return The list of values, or null if not a list type
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T> asList(): List<T>? = (typedValue as? TypedValue.ListValue<T>)?.value

    /**
     * Returns the value as a Map if it's a [TypedValue.CompositeValue], null otherwise.
     *
     * Use this for multi-sensor responses where a single command returns
     * multiple named values.
     *
     * @return Map of field names to values, or null if not a composite type
     */
    public fun asComposite(): Map<String, Any>? = (typedValue as? TypedValue.CompositeValue)?.value

    /**
     * Returns the duration in seconds if it's a [TypedValue.DurationValue], null otherwise.
     *
     * Use this for time values like engine runtime or time since codes cleared.
     *
     * @return Duration in seconds as Long, or null if not a duration type
     */
    public fun asDuration(): Long? = (typedValue as? TypedValue.DurationValue)?.value
}
