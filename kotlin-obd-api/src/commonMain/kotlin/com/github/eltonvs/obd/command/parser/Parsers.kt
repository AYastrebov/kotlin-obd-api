package com.github.eltonvs.obd.command.parser

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.TypedValue
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.calculatePercentage
import com.github.eltonvs.obd.command.formatToDecimalPlaces

/**
 * A parser that transforms an OBD raw response into a typed value.
 *
 * @param T The type of value this parser produces
 */
public fun interface ObdParser<T> {
    /**
     * Parse the raw response and return a typed value
     */
    public fun parse(rawResponse: ObdRawResponse): TypedValue<T>
}

/**
 * Collection of pre-built parsers for common OBD response patterns.
 *
 * Usage:
 * ```kotlin
 * val speedParser = Parsers.integer(bytesToProcess = 1)
 * val throttleParser = Parsers.percentage(bytesToProcess = 1)
 * val coolantParser = Parsers.temperature()
 * ```
 */
public object Parsers {
    /**
     * Creates a parser for integer values.
     *
     * @param bytesToProcess Number of bytes to read (-1 for all)
     * @param multiplier Multiplier to apply to raw value
     * @param offset Offset to add after multiplication
     * @param unit Unit string for the value
     */
    public fun integer(
        bytesToProcess: Int = -1,
        multiplier: Float = 1f,
        offset: Long = 0,
        unit: String = ""
    ): ObdParser<Long> = ObdParser { rawResponse ->
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        val calculated = (rawValue * multiplier).toLong() + offset
        TypedValue.IntegerValue(
            value = calculated,
            unit = unit
        )
    }

    /**
     * Creates a parser for float values.
     *
     * @param bytesToProcess Number of bytes to read (-1 for all)
     * @param multiplier Multiplier to apply to raw value
     * @param offset Offset to add after multiplication
     * @param decimalPlaces Number of decimal places for string representation
     * @param unit Unit string for the value
     */
    public fun float(
        bytesToProcess: Int = -1,
        multiplier: Float = 1f,
        offset: Float = 0f,
        decimalPlaces: Int = 2,
        unit: String = ""
    ): ObdParser<Float> = ObdParser { rawResponse ->
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        val calculated = rawValue * multiplier + offset
        TypedValue.FloatValue(
            value = calculated,
            stringValue = formatToDecimalPlaces(calculated, decimalPlaces),
            unit = unit
        )
    }

    /**
     * Creates a parser for percentage values.
     *
     * Uses standard OBD percentage formula: value * 100 / 255
     *
     * @param bytesToProcess Number of bytes to read (-1 for all)
     * @param decimalPlaces Number of decimal places for string representation
     */
    public fun percentage(
        bytesToProcess: Int = -1,
        decimalPlaces: Int = 1
    ): ObdParser<Float> = ObdParser { rawResponse ->
        val percentage = calculatePercentage(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        TypedValue.PercentageValue(
            value = percentage,
            decimalPlaces = decimalPlaces
        )
    }

    /**
     * Creates a parser for temperature values.
     *
     * Uses standard OBD temperature formula: value + offset (typically -40)
     *
     * @param bytesToProcess Number of bytes to read
     * @param offset Temperature offset (default -40°C)
     * @param decimalPlaces Number of decimal places for string representation
     * @param unit Unit string for the value
     */
    public fun temperature(
        bytesToProcess: Int = 1,
        offset: Float = -40f,
        decimalPlaces: Int = 1,
        unit: String = "°C"
    ): ObdParser<Float> = ObdParser { rawResponse ->
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        val temperature = rawValue + offset
        TypedValue.TemperatureValue(
            value = temperature,
            decimalPlaces = decimalPlaces,
            unit = unit
        )
    }

    /**
     * Creates a parser for pressure values.
     *
     * @param bytesToProcess Number of bytes to read
     * @param multiplier Multiplier to apply to raw value
     * @param offset Offset to add after multiplication
     * @param decimalPlaces Number of decimal places for string representation
     * @param unit Unit string for the value
     */
    public fun pressure(
        bytesToProcess: Int = 1,
        multiplier: Float = 1f,
        offset: Float = 0f,
        decimalPlaces: Int = 1,
        unit: String = "kPa"
    ): ObdParser<Float> = ObdParser { rawResponse ->
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        val pressure = rawValue * multiplier + offset
        TypedValue.PressureValue(
            value = pressure,
            decimalPlaces = decimalPlaces,
            unit = unit
        )
    }

    /**
     * Creates a parser that maps raw values to arbitrary values.
     *
     * @param mapping Map from raw byte values to result values
     * @param default Default value when raw value is not in mapping
     * @param stringTransform Function to convert value to string representation
     */
    public fun <T> mapped(
        mapping: Map<Int, T>,
        default: T,
        bytesToProcess: Int = 1,
        stringTransform: (T) -> String = { it.toString() }
    ): ObdParser<T> = ObdParser { rawResponse ->
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess).toInt()
        val value = mapping[rawValue] ?: default
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is Enum<*> -> TypedValue.EnumValue(
                value = value as Nothing,
                stringValue = stringTransform(value)
            )
            is String -> TypedValue.StringValue(
                value = value,
                stringValue = stringTransform(value)
            ) as TypedValue<T>
            else -> TypedValue.StringValue(
                value = stringTransform(value),
                stringValue = stringTransform(value)
            ) as TypedValue<T>
        }
    }

    /**
     * Creates a parser for enum values.
     *
     * @param mapping Map from raw byte values to enum constants
     * @param default Default enum value when raw value is not in mapping
     * @param bytesToProcess Number of bytes to read
     * @param stringTransform Function to convert enum to string representation
     */
    public fun <E : Enum<E>> enum(
        mapping: Map<Int, E>,
        default: E,
        bytesToProcess: Int = 1,
        stringTransform: (E) -> String = { it.name }
    ): ObdParser<E> = ObdParser { rawResponse ->
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess).toInt()
        val value = mapping[rawValue] ?: default
        TypedValue.EnumValue(
            value = value,
            stringValue = stringTransform(value)
        )
    }

    /**
     * Creates a parser for boolean values based on a bit position.
     *
     * @param byteIndex Index of the byte to check (0-based from start of data)
     * @param bitPosition Position of the bit to check (1-based from MSB)
     * @param bitWidth Total bits in the byte (typically 8)
     * @param trueString String representation for true
     * @param falseString String representation for false
     */
    public fun boolean(
        byteIndex: Int = 0,
        bitPosition: Int = 1,
        bitWidth: Int = 8,
        trueString: String = "ON",
        falseString: String = "OFF"
    ): ObdParser<Boolean> = ObdParser { rawResponse ->
        val dataStart = 2 // Skip mode and PID bytes
        val byte = rawResponse.bufferedValue.getOrElse(dataStart + byteIndex) { 0 }
        val value = byte shr (bitWidth - bitPosition) and 1 == 1
        TypedValue.BooleanValue(
            value = value,
            stringValue = if (value) trueString else falseString
        )
    }

    /**
     * Creates a parser for duration values in seconds.
     *
     * @param bytesToProcess Number of bytes to read
     * @param formatAsTime Whether to format as HH:MM:SS
     */
    public fun duration(
        bytesToProcess: Int = -1,
        formatAsTime: Boolean = true
    ): ObdParser<Long> = ObdParser { rawResponse ->
        val seconds = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        TypedValue.DurationValue(
            seconds = seconds,
            formatAsTime = formatAsTime
        )
    }

    /**
     * Creates a parser that extracts raw bytes as a string.
     *
     * @param bytesToProcess Number of bytes to read (-1 for all)
     * @param startOffset Offset from the start of data bytes
     */
    public fun rawString(
        bytesToProcess: Int = -1,
        startOffset: Int = 0
    ): ObdParser<String> = ObdParser { rawResponse ->
        val dataStart = 2 + startOffset // Skip mode and PID bytes, plus offset
        val data = rawResponse.bufferedValue.drop(dataStart).let { bytes ->
            if (bytesToProcess == -1) bytes else bytes.take(bytesToProcess)
        }
        val stringValue = data.map { it.toChar() }.joinToString("")
        TypedValue.StringValue(value = stringValue)
    }

    /**
     * Creates a parser that combines multiple parsers for multi-value responses.
     *
     * @param parsers Map of field names to their respective parsers
     */
    public fun composite(
        parsers: Map<String, ObdParser<*>>
    ): ObdParser<Map<String, Any>> = ObdParser { rawResponse ->
        val values = parsers.mapValues { (_, parser) ->
            parser.parse(rawResponse).value as Any
        }
        val stringValue = values.entries.joinToString(", ") { (k, v) -> "$k=$v" }
        TypedValue.CompositeValue(
            value = values,
            stringValue = stringValue
        )
    }
}

/**
 * Extension function to transform parser output.
 */
public fun <T, R> ObdParser<T>.map(transform: (TypedValue<T>) -> TypedValue<R>): ObdParser<R> =
    ObdParser { rawResponse -> transform(this.parse(rawResponse)) }

/**
 * Extension function to add a unit to parser output.
 */
public fun <T> ObdParser<T>.withUnit(unit: String): ObdParser<T> =
    ObdParser { rawResponse ->
        when (val result = this.parse(rawResponse)) {
            is TypedValue.IntegerValue -> result.copy(unit = unit) as TypedValue<T>
            is TypedValue.FloatValue -> result.copy(unit = unit) as TypedValue<T>
            is TypedValue.PercentageValue -> result.copy(unit = unit) as TypedValue<T>
            is TypedValue.TemperatureValue -> result.copy(unit = unit) as TypedValue<T>
            is TypedValue.PressureValue -> result.copy(unit = unit) as TypedValue<T>
            is TypedValue.StringValue -> result.copy(unit = unit) as TypedValue<T>
            is TypedValue.EnumValue<*> -> (result as TypedValue.EnumValue<Nothing>).copy(unit = unit) as TypedValue<T>
            is TypedValue.BooleanValue -> result.copy(unit = unit) as TypedValue<T>
            is TypedValue.ListValue<*> -> (result as TypedValue.ListValue<Nothing>).copy(unit = unit) as TypedValue<T>
            is TypedValue.CompositeValue -> result.copy(unit = unit) as TypedValue<T>
            is TypedValue.DurationValue -> result.copy(unit = unit) as TypedValue<T>
        }
    }
