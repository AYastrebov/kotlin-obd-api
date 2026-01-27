package com.github.eltonvs.obd.command

/**
 * Sealed class hierarchy representing typed response values from OBD commands.
 *
 * This provides type-safe access to parsed command results while maintaining
 * backward compatibility with string-based responses. Each subclass represents
 * a specific type of OBD data (integer, percentage, temperature, etc.).
 *
 * Example usage:
 * ```kotlin
 * val response = command.handleResponse(rawResponse)
 * when (val typed = response.typedValue) {
 *     is TypedValue.IntegerValue -> println("Integer: ${typed.value}")
 *     is TypedValue.PercentageValue -> println("Percentage: ${typed.value}%")
 *     is TypedValue.TemperatureValue -> println("Temperature: ${typed.value}°C")
 *     // ... handle other types
 * }
 * ```
 *
 * @param T The underlying Kotlin type of the value (Long, Float, String, etc.)
 * @see ObdResponse.typedValue
 * @see TypedObdCommand
 */
public sealed class TypedValue<out T> {
    /**
     * The strongly-typed value.
     *
     * Access this property to get the parsed value in its native type
     * without needing to parse strings.
     */
    public abstract val value: T

    /**
     * String representation of the value for display and backward compatibility.
     *
     * This is the same value that would be returned by legacy string-based handlers.
     * Formatting (decimal places, etc.) is applied based on the value type.
     */
    public abstract val stringValue: String

    /**
     * Unit string associated with this value (e.g., "km/h", "%", "°C").
     *
     * May be empty if the value has no associated unit.
     */
    public open val unit: String = ""

    /**
     * Creates a copy of this value with a different unit string.
     *
     * @param newUnit The new unit string to use
     * @return A new TypedValue with the updated unit
     */
    public abstract fun withUnit(newUnit: String): TypedValue<T>

    /**
     * Represents an integer value using Long for wide range support.
     *
     * Use this for whole number values like speed, RPM, distance, etc.
     *
     * @property value The integer value as Long
     * @property stringValue String representation of the value
     * @property unit Optional unit string (e.g., "km/h", "RPM")
     */
    public data class IntegerValue(
        override val value: Long,
        override val stringValue: String = value.toString(),
        override val unit: String = ""
    ) : TypedValue<Long>() {
        override fun withUnit(newUnit: String): TypedValue<Long> = copy(unit = newUnit)
    }

    /**
     * Represents a floating-point value with configurable decimal places.
     *
     * Use this for values that require decimal precision like voltage,
     * mass air flow, or other calculated values.
     *
     * @property value The float value
     * @property stringValue Formatted string representation
     * @property unit Optional unit string (e.g., "V", "g/s")
     */
    public data class FloatValue(
        override val value: Float,
        override val stringValue: String,
        override val unit: String = ""
    ) : TypedValue<Float>() {
        /**
         * Creates a FloatValue with automatic string formatting.
         *
         * @param value The float value
         * @param decimalPlaces Number of decimal places for string representation (default: 2)
         * @param unit Optional unit string
         */
        public constructor(value: Float, decimalPlaces: Int = 2, unit: String = "") : this(
            value = value,
            stringValue = formatToDecimalPlaces(value, decimalPlaces),
            unit = unit
        )

        override fun withUnit(newUnit: String): TypedValue<Float> = copy(unit = newUnit)
    }

    /**
     * Represents a percentage value (typically 0-100, but can exceed for absolute load values).
     *
     * The unit defaults to "%" and the value represents the percentage as a float
     * (e.g., 50.0 means 50%).
     *
     * @property value The percentage as a float (0-100 range typically)
     * @property stringValue Formatted string representation
     * @property unit Unit string, defaults to "%"
     */
    public data class PercentageValue(
        override val value: Float,
        override val stringValue: String,
        override val unit: String = "%"
    ) : TypedValue<Float>() {
        /**
         * Creates a PercentageValue with automatic string formatting.
         *
         * @param value The percentage value (0-100 typically)
         * @param decimalPlaces Number of decimal places for string representation (default: 1)
         */
        public constructor(value: Float, decimalPlaces: Int = 1) : this(
            value = value,
            stringValue = formatToDecimalPlaces(value, decimalPlaces)
        )

        override fun withUnit(newUnit: String): TypedValue<Float> = copy(unit = newUnit)
    }

    /**
     * Represents a temperature value, typically in Celsius.
     *
     * OBD temperature values are commonly offset by -40°C from the raw byte value.
     *
     * @property value The temperature in the specified unit
     * @property stringValue Formatted string representation
     * @property unit Temperature unit, defaults to "°C"
     */
    public data class TemperatureValue(
        override val value: Float,
        override val stringValue: String,
        override val unit: String = "°C"
    ) : TypedValue<Float>() {
        /**
         * Creates a TemperatureValue with automatic string formatting.
         *
         * @param value The temperature value
         * @param decimalPlaces Number of decimal places for string representation (default: 1)
         * @param unit Temperature unit (default: "°C")
         */
        public constructor(value: Float, decimalPlaces: Int = 1, unit: String = "°C") : this(
            value = value,
            stringValue = formatToDecimalPlaces(value, decimalPlaces),
            unit = unit
        )

        override fun withUnit(newUnit: String): TypedValue<Float> = copy(unit = newUnit)
    }

    /**
     * Represents a pressure value, typically in kilopascals (kPa).
     *
     * @property value The pressure in the specified unit
     * @property stringValue Formatted string representation
     * @property unit Pressure unit, defaults to "kPa"
     */
    public data class PressureValue(
        override val value: Float,
        override val stringValue: String,
        override val unit: String = "kPa"
    ) : TypedValue<Float>() {
        /**
         * Creates a PressureValue with automatic string formatting.
         *
         * @param value The pressure value
         * @param decimalPlaces Number of decimal places for string representation (default: 1)
         * @param unit Pressure unit (default: "kPa")
         */
        public constructor(value: Float, decimalPlaces: Int = 1, unit: String = "kPa") : this(
            value = value,
            stringValue = formatToDecimalPlaces(value, decimalPlaces),
            unit = unit
        )

        override fun withUnit(newUnit: String): TypedValue<Float> = copy(unit = newUnit)
    }

    /**
     * Represents a plain string value.
     *
     * Use this for text responses like VIN, calibration IDs, or custom string values.
     *
     * @property value The string value
     * @property stringValue Same as value by default
     * @property unit Optional unit string (usually empty for strings)
     */
    public data class StringValue(
        override val value: String,
        override val stringValue: String = value,
        override val unit: String = ""
    ) : TypedValue<String>() {
        override fun withUnit(newUnit: String): TypedValue<String> = copy(unit = newUnit)
    }

    /**
     * Represents an enumeration value for commands that return discrete states.
     *
     * Use this for values that map to a fixed set of options like fuel type,
     * OBD standard, or ignition type.
     *
     * @param E The enum type
     * @property value The enum constant
     * @property stringValue String representation, defaults to enum name
     * @property unit Optional unit string (usually empty for enums)
     */
    public data class EnumValue<E : Enum<E>>(
        override val value: E,
        override val stringValue: String = value.name,
        override val unit: String = ""
    ) : TypedValue<E>() {
        override fun withUnit(newUnit: String): TypedValue<E> = copy(unit = newUnit)
    }

    /**
     * Represents a boolean on/off value.
     *
     * Use this for status flags like MIL status, A/C status, or other binary states.
     *
     * @property value The boolean value
     * @property stringValue String representation (e.g., "ON", "OFF", "true", "false")
     * @property unit Optional unit string (usually empty for booleans)
     */
    public data class BooleanValue(
        override val value: Boolean,
        override val stringValue: String = value.toString(),
        override val unit: String = ""
    ) : TypedValue<Boolean>() {
        override fun withUnit(newUnit: String): TypedValue<Boolean> = copy(unit = newUnit)
    }

    /**
     * Represents a list of values for commands that return multiple items.
     *
     * Use this for DTCs, supported PIDs, or other multi-value responses.
     *
     * @param T The type of elements in the list
     * @property value The list of values
     * @property stringValue Joined string representation
     * @property unit Optional unit string
     */
    public data class ListValue<T>(
        override val value: List<T>,
        override val stringValue: String,
        override val unit: String = ""
    ) : TypedValue<List<T>>() {
        /**
         * Creates a ListValue with automatic string formatting.
         *
         * @param value The list of values
         * @param separator Separator for joining elements (default: ", ")
         */
        public constructor(value: List<T>, separator: String = ", ") : this(
            value = value,
            stringValue = value.joinToString(separator)
        )

        override fun withUnit(newUnit: String): TypedValue<List<T>> = copy(unit = newUnit)
    }

    /**
     * Represents a composite/map value for commands returning multiple named values.
     *
     * Use this for multi-sensor responses where a single command returns
     * several different measurements.
     *
     * @property value Map of field names to their values
     * @property stringValue Formatted string representation of all fields
     * @property unit Optional unit string
     */
    public data class CompositeValue(
        override val value: Map<String, Any>,
        override val stringValue: String,
        override val unit: String = ""
    ) : TypedValue<Map<String, Any>>() {
        override fun withUnit(newUnit: String): TypedValue<Map<String, Any>> = copy(unit = newUnit)
    }

    /**
     * Represents a duration/time value in seconds.
     *
     * Can be formatted as HH:MM:SS or as raw seconds.
     * Use this for runtime, time since codes cleared, etc.
     *
     * @property value Duration in seconds
     * @property stringValue Formatted string (HH:MM:SS or seconds)
     * @property unit Optional unit string
     */
    public data class DurationValue(
        override val value: Long,
        override val stringValue: String,
        override val unit: String = ""
    ) : TypedValue<Long>() {
        /**
         * Creates a DurationValue from seconds with optional time formatting.
         *
         * @param seconds The duration in seconds
         * @param formatAsTime If true, formats as "HH:MM:SS"; if false, uses raw seconds
         */
        public constructor(seconds: Long, formatAsTime: Boolean) : this(
            value = seconds,
            stringValue = if (formatAsTime) {
                val hh = seconds / 3600
                val mm = (seconds % 3600) / 60
                val ss = seconds % 60
                listOf(hh, mm, ss).joinToString(":") { it.toString().padStart(2, '0') }
            } else {
                seconds.toString()
            }
        )

        override fun withUnit(newUnit: String): TypedValue<Long> = copy(unit = newUnit)
    }
}
