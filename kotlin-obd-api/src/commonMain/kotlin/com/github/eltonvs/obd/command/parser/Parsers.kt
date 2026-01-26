package com.github.eltonvs.obd.command.parser

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.TypedValue
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.calculatePercentage
import com.github.eltonvs.obd.command.formatToDecimalPlaces

/**
 * Functional interface for parsing OBD raw responses into typed values.
 *
 * Implement this interface to create custom parsers for OBD command responses.
 * Parsers are composable and can be reused across multiple commands.
 *
 * Example:
 * ```kotlin
 * val speedParser = ObdParser<Long> { rawResponse ->
 *     val speed = rawResponse.bufferedValue[2].toLong()
 *     TypedValue.IntegerValue(speed, unit = "km/h")
 * }
 *
 * val result = speedParser.parse(rawResponse)
 * println(result.value)  // 80
 * ```
 *
 * @param T The type of value this parser produces
 * @see Parsers
 * @see TypedValue
 */
public fun interface ObdParser<T> {
    /**
     * Parse the raw response and return a typed value.
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return A [TypedValue] containing the parsed result
     */
    public fun parse(rawResponse: ObdRawResponse): TypedValue<T>
}

/**
 * Factory object providing pre-built parsers for common OBD response patterns.
 *
 * Use these parsers directly or as building blocks for more complex parsing logic.
 * All parsers follow OBD-II standard conventions for data interpretation.
 *
 * Example usage:
 * ```kotlin
 * // Create parsers
 * val speedParser = Parsers.integer(bytesToProcess = 1, unit = "km/h")
 * val throttleParser = Parsers.percentage(bytesToProcess = 1)
 * val coolantParser = Parsers.temperature()
 *
 * // Use in commands
 * val speedCommand = obdCommand {
 *     tag = "SPEED"
 *     name = "Vehicle Speed"
 *     mode = "01"
 *     pid = "0D"
 *     parseWith(speedParser)
 * }
 * ```
 *
 * @see ObdParser
 * @see ObdCommandBuilder
 */
public object Parsers {
    /**
     * Creates a parser for integer values.
     *
     * Formula: `value = (rawBytes * multiplier) + offset`
     *
     * The raw bytes are combined using big-endian ordering:
     * - 1 byte: A
     * - 2 bytes: A*256 + B
     * - 3 bytes: A*65536 + B*256 + C
     *
     * @param bytesToProcess Number of bytes to read (-1 for all data bytes)
     * @param multiplier Multiplier to apply to raw value (default: 1)
     * @param offset Offset to add after multiplication (default: 0)
     * @param unit Unit string for the value (default: empty)
     * @return Parser that produces [TypedValue.IntegerValue]
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
     * Formula: `value = (rawBytes * multiplier) + offset`
     *
     * Use this for values requiring decimal precision.
     *
     * @param bytesToProcess Number of bytes to read (-1 for all data bytes)
     * @param multiplier Multiplier to apply to raw value (default: 1)
     * @param offset Offset to add after multiplication (default: 0)
     * @param decimalPlaces Number of decimal places for string representation (default: 2)
     * @param unit Unit string for the value (default: empty)
     * @return Parser that produces [TypedValue.FloatValue]
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
     * Uses standard OBD-II percentage formula: `percentage = (rawValue * 100) / 255`
     *
     * This maps 0x00 to 0% and 0xFF to 100%.
     *
     * @param bytesToProcess Number of bytes to read (-1 for all data bytes)
     * @param decimalPlaces Number of decimal places for string representation (default: 1)
     * @return Parser that produces [TypedValue.PercentageValue]
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
     * Uses standard OBD-II temperature formula: `temperature = rawValue + offset`
     *
     * The default offset of -40 allows representing temperatures from -40°C to 215°C
     * with a single byte (standard OBD-II convention).
     *
     * @param bytesToProcess Number of bytes to read (default: 1)
     * @param offset Temperature offset (default: -40 for standard OBD)
     * @param decimalPlaces Number of decimal places for string representation (default: 1)
     * @param unit Unit string for the value (default: "°C")
     * @return Parser that produces [TypedValue.TemperatureValue]
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
     * Formula: `pressure = (rawBytes * multiplier) + offset`
     *
     * Different PIDs use different multipliers (e.g., fuel pressure uses 3).
     *
     * @param bytesToProcess Number of bytes to read (default: 1)
     * @param multiplier Multiplier to apply to raw value (default: 1)
     * @param offset Offset to add after multiplication (default: 0)
     * @param decimalPlaces Number of decimal places for string representation (default: 1)
     * @param unit Unit string for the value (default: "kPa")
     * @return Parser that produces [TypedValue.PressureValue]
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
     * Creates a parser that maps raw values to arbitrary result values.
     *
     * Use this for lookup tables where specific byte values correspond to
     * specific results (strings, enums, etc.).
     *
     * @param T The type of result values
     * @param mapping Map from raw byte values to result values
     * @param default Default value when raw value is not in mapping
     * @param bytesToProcess Number of bytes to read (default: 1)
     * @param stringTransform Function to convert value to string (default: toString)
     * @return Parser that produces appropriate [TypedValue] based on result type
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
     * Maps raw byte values to enum constants using the provided mapping.
     * Returns the default value when the raw value is not in the mapping.
     *
     * @param E The enum type
     * @param mapping Map from raw byte values to enum constants
     * @param default Default enum value for unmapped raw values
     * @param bytesToProcess Number of bytes to read (default: 1)
     * @param stringTransform Function to convert enum to string (default: name)
     * @return Parser that produces [TypedValue.EnumValue]
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
     * Checks a specific bit in the response data. The bit position is 1-based
     * from the MSB (most significant bit).
     *
     * @param byteIndex Index of byte to check (0-based from data start, after mode/PID)
     * @param bitPosition Position of bit to check (1 = MSB, 8 = LSB)
     * @param bitWidth Total bits in the byte (default: 8)
     * @param trueString String representation for true (default: "ON")
     * @param falseString String representation for false (default: "OFF")
     * @return Parser that produces [TypedValue.BooleanValue]
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
     * Optionally formats the result as HH:MM:SS for human-readable display.
     *
     * @param bytesToProcess Number of bytes to read (-1 for all data bytes)
     * @param formatAsTime Whether to format as "HH:MM:SS" (true) or raw seconds (false)
     * @return Parser that produces [TypedValue.DurationValue]
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
     * Useful for VIN, calibration IDs, or other text-based responses.
     *
     * @param bytesToProcess Number of bytes to read (-1 for all data bytes)
     * @param startOffset Offset from the start of data bytes
     * @return Parser that produces [TypedValue.StringValue]
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
     * Use this for commands that return several different values in a single response.
     * Each parser in the map processes the same raw response independently.
     *
     * Example:
     * ```kotlin
     * val multiParser = Parsers.composite(mapOf(
     *     "temperature" to Parsers.temperature(),
     *     "pressure" to Parsers.pressure()
     * ))
     * ```
     *
     * @param parsers Map of field names to their respective parsers
     * @return Parser that produces [TypedValue.CompositeValue]
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
 *
 * Creates a new parser that applies a transformation to the result of this parser.
 *
 * Example:
 * ```kotlin
 * val kelvinParser = Parsers.temperature()
 *     .map { temp -> TypedValue.TemperatureValue(temp.value + 273.15f, unit = "K") }
 * ```
 *
 * @param T Original value type
 * @param R Transformed value type
 * @param transform Transformation function
 * @return New parser producing transformed values
 */
public fun <T, R> ObdParser<T>.map(transform: (TypedValue<T>) -> TypedValue<R>): ObdParser<R> =
    ObdParser { rawResponse -> transform(this.parse(rawResponse)) }

/**
 * Extension function to add a unit to parser output.
 *
 * Creates a new parser that adds the specified unit to results from this parser.
 *
 * Example:
 * ```kotlin
 * val speedParser = Parsers.integer(bytesToProcess = 1).withUnit("km/h")
 * ```
 *
 * @param T Value type
 * @param unit Unit string to add
 * @return New parser with unit added to results
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
