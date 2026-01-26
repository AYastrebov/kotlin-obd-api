package com.github.eltonvs.obd.command

/**
 * Sealed class hierarchy representing typed response values from OBD commands.
 * This provides type-safe access to parsed command results while maintaining
 * backward compatibility with string-based responses.
 *
 * @param T The underlying type of the value
 */
public sealed class TypedValue<out T> {
    /**
     * The typed value
     */
    public abstract val value: T

    /**
     * String representation of the value for backward compatibility
     */
    public abstract val stringValue: String

    /**
     * Optional unit associated with this value
     */
    public open val unit: String = ""

    /**
     * Represents an integer value (Long for wide range support)
     */
    public data class IntegerValue(
        override val value: Long,
        override val stringValue: String = value.toString(),
        override val unit: String = ""
    ) : TypedValue<Long>()

    /**
     * Represents a floating-point value
     */
    public data class FloatValue(
        override val value: Float,
        override val stringValue: String,
        override val unit: String = ""
    ) : TypedValue<Float>() {
        public constructor(value: Float, decimalPlaces: Int = 2, unit: String = "") : this(
            value = value,
            stringValue = formatToDecimalPlaces(value, decimalPlaces),
            unit = unit
        )
    }

    /**
     * Represents a percentage value (0-100 or beyond for absolute load)
     */
    public data class PercentageValue(
        override val value: Float,
        override val stringValue: String,
        override val unit: String = "%"
    ) : TypedValue<Float>() {
        public constructor(value: Float, decimalPlaces: Int = 1) : this(
            value = value,
            stringValue = formatToDecimalPlaces(value, decimalPlaces)
        )
    }

    /**
     * Represents a temperature value
     */
    public data class TemperatureValue(
        override val value: Float,
        override val stringValue: String,
        override val unit: String = "°C"
    ) : TypedValue<Float>() {
        public constructor(value: Float, decimalPlaces: Int = 1, unit: String = "°C") : this(
            value = value,
            stringValue = formatToDecimalPlaces(value, decimalPlaces),
            unit = unit
        )
    }

    /**
     * Represents a pressure value
     */
    public data class PressureValue(
        override val value: Float,
        override val stringValue: String,
        override val unit: String = "kPa"
    ) : TypedValue<Float>() {
        public constructor(value: Float, decimalPlaces: Int = 1, unit: String = "kPa") : this(
            value = value,
            stringValue = formatToDecimalPlaces(value, decimalPlaces),
            unit = unit
        )
    }

    /**
     * Represents a string value
     */
    public data class StringValue(
        override val value: String,
        override val stringValue: String = value,
        override val unit: String = ""
    ) : TypedValue<String>()

    /**
     * Represents an enum value
     */
    public data class EnumValue<E : Enum<E>>(
        override val value: E,
        override val stringValue: String = value.name,
        override val unit: String = ""
    ) : TypedValue<E>()

    /**
     * Represents a boolean value
     */
    public data class BooleanValue(
        override val value: Boolean,
        override val stringValue: String = value.toString(),
        override val unit: String = ""
    ) : TypedValue<Boolean>()

    /**
     * Represents a list of values
     */
    public data class ListValue<T>(
        override val value: List<T>,
        override val stringValue: String,
        override val unit: String = ""
    ) : TypedValue<List<T>>() {
        public constructor(value: List<T>, separator: String = ", ") : this(
            value = value,
            stringValue = value.joinToString(separator)
        )
    }

    /**
     * Represents a composite/map value for multi-sensor responses
     */
    public data class CompositeValue(
        override val value: Map<String, Any>,
        override val stringValue: String,
        override val unit: String = ""
    ) : TypedValue<Map<String, Any>>()

    /**
     * Represents a duration/time value
     */
    public data class DurationValue(
        override val value: Long,
        override val stringValue: String,
        override val unit: String = ""
    ) : TypedValue<Long>() {
        /**
         * Create a duration value from seconds, formatted as HH:MM:SS
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
    }
}
