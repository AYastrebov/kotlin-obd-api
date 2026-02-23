package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.command.parser.ObdParser
import com.github.eltonvs.obd.command.parser.Parsers

/**
 * DSL builder for creating OBD commands without subclassing.
 *
 * Use this builder to create custom commands inline without defining a new class.
 * The builder supports various parsing strategies for common OBD value types.
 *
 * Example usage:
 * ```kotlin
 * val boostPressure = obdCommand {
 *     tag = "BOOST_PRESSURE"
 *     name = "Turbo Boost Pressure"
 *     mode = "22"
 *     pid = "1234"
 *     unit = "kPa"
 *     category = CommandCategory.PRESSURE
 *     parseAsFloat(bytesToProcess = 2, multiplier = 0.1f, decimalPlaces = 1)
 * }
 * ```
 *
 * @see obdCommand
 * @see obdCommand
 */
public class ObdCommandBuilder {
    /**
     * Unique identifier for this command.
     *
     * Used for registry lookups and command identification.
     * Must be unique across all registered commands.
     */
    public var tag: String = ""

    /**
     * Human-readable name for this command.
     *
     * Displayed in user interfaces and logs.
     */
    public var name: String = ""

    /**
     * OBD mode for this command.
     *
     * Common modes:
     * - "01": Current data (live sensor data)
     * - "02": Freeze frame data
     * - "03": Stored DTCs
     * - "04": Clear DTCs
     * - "09": Vehicle information
     * - "22": Manufacturer-specific data
     */
    public var mode: String = ""

    /**
     * Parameter ID (PID) within the mode.
     *
     * For standard mode 01, this is typically 2 hex characters (e.g., "0D" for speed).
     * For manufacturer modes, this may be 4 hex characters (e.g., "1234").
     */
    public var pid: String = ""

    /**
     * Unit string for the response value.
     *
     * Examples: "km/h", "%", "°C", "kPa", "RPM"
     */
    public var unit: String = ""

    /**
     * Category for grouping this command.
     *
     * Defaults to [CommandCategory.CUSTOM] for user-defined commands.
     */
    public var category: CommandCategory = CommandCategory.CUSTOM

    /**
     * Whether to skip digit check during response validation.
     *
     * Set to true for commands with non-standard response formats.
     */
    public var skipDigitCheck: Boolean = false

    private var parser: ObdParser<*>? = null
    private var parserFactory: ((String) -> ObdParser<*>)? = null

    /**
     * Configure parsing as an integer value.
     *
     * Use this for whole number values like speed, RPM, or distance.
     *
     * @param bytesToProcess Number of bytes to read from response (-1 for all)
     * @param multiplier Multiplier applied to raw value (default: 1)
     * @param offset Offset added after multiplication (default: 0)
     */
    public fun parseAsInteger(
        bytesToProcess: Int = -1,
        multiplier: Float = 1f,
        offset: Long = 0
    ) {
        parserFactory = { resolvedUnit ->
            Parsers.integer(
                bytesToProcess = bytesToProcess,
                multiplier = multiplier,
                offset = offset,
                unit = resolvedUnit
            )
        }
    }

    /**
     * Configure parsing as a float value.
     *
     * Use this for values requiring decimal precision like voltage or mass air flow.
     *
     * @param bytesToProcess Number of bytes to read from response (-1 for all)
     * @param multiplier Multiplier applied to raw value (default: 1)
     * @param offset Offset added after multiplication (default: 0)
     * @param decimalPlaces Decimal places for string formatting (default: 2)
     */
    public fun parseAsFloat(
        bytesToProcess: Int = -1,
        multiplier: Float = 1f,
        offset: Float = 0f,
        decimalPlaces: Int = 2
    ) {
        parserFactory = { resolvedUnit ->
            Parsers.float(
                bytesToProcess = bytesToProcess,
                multiplier = multiplier,
                offset = offset,
                decimalPlaces = decimalPlaces,
                unit = resolvedUnit
            )
        }
    }

    /**
     * Configure parsing as a percentage value.
     *
     * Uses standard OBD percentage formula: (rawValue * 100) / 255
     * Automatically sets unit to "%" if not already set.
     *
     * @param bytesToProcess Number of bytes to read from response (-1 for all)
     * @param decimalPlaces Decimal places for string formatting (default: 1)
     */
    public fun parseAsPercentage(
        bytesToProcess: Int = -1,
        decimalPlaces: Int = 1
    ) {
        if (unit.isEmpty()) {
            unit = "%"
        }
        parserFactory = { _ ->
            Parsers.percentage(
                bytesToProcess = bytesToProcess,
                decimalPlaces = decimalPlaces
            )
        }
    }

    /**
     * Configure parsing as a temperature value.
     *
     * Uses standard OBD temperature formula: rawValue + offset
     * Default offset is -40°C (standard OBD temperature offset).
     *
     * @param bytesToProcess Number of bytes to read from response (default: 1)
     * @param offset Temperature offset (default: -40)
     * @param decimalPlaces Decimal places for string formatting (default: 1)
     */
    public fun parseAsTemperature(
        bytesToProcess: Int = 1,
        offset: Float = -40f,
        decimalPlaces: Int = 1
    ) {
        parserFactory = { resolvedUnit ->
            Parsers.temperature(
                bytesToProcess = bytesToProcess,
                offset = offset,
                decimalPlaces = decimalPlaces,
                unit = resolvedUnit.ifEmpty { "°C" }
            )
        }
    }

    /**
     * Configure parsing as a pressure value.
     *
     * Calculates: pressure = (rawValue * multiplier) + offset
     *
     * @param bytesToProcess Number of bytes to read from response (default: 1)
     * @param multiplier Multiplier applied to raw value (default: 1)
     * @param offset Offset added after multiplication (default: 0)
     * @param decimalPlaces Decimal places for string formatting (default: 1)
     */
    public fun parseAsPressure(
        bytesToProcess: Int = 1,
        multiplier: Float = 1f,
        offset: Float = 0f,
        decimalPlaces: Int = 1
    ) {
        parserFactory = { resolvedUnit ->
            Parsers.pressure(
                bytesToProcess = bytesToProcess,
                multiplier = multiplier,
                offset = offset,
                decimalPlaces = decimalPlaces,
                unit = resolvedUnit.ifEmpty { "kPa" }
            )
        }
    }

    /**
     * Configure parsing as a duration value.
     *
     * Returns value in seconds with optional HH:MM:SS formatting.
     *
     * @param bytesToProcess Number of bytes to read from response (-1 for all)
     * @param formatAsTime If true, formats as "HH:MM:SS"; if false, shows seconds
     */
    public fun parseAsDuration(
        bytesToProcess: Int = -1,
        formatAsTime: Boolean = true
    ) {
        parser = Parsers.duration(
            bytesToProcess = bytesToProcess,
            formatAsTime = formatAsTime
        )
        parserFactory = null
    }

    /**
     * Configure parsing as a boolean value based on a bit.
     *
     * Checks a specific bit position in the response data.
     *
     * @param byteIndex Index of byte to check (0-based from data start)
     * @param bitPosition Position of bit to check (1-based from MSB)
     * @param bitWidth Total bits in the byte (default: 8)
     * @param trueString String for true value (default: "ON")
     * @param falseString String for false value (default: "OFF")
     */
    public fun parseAsBoolean(
        byteIndex: Int = 0,
        bitPosition: Int = 1,
        bitWidth: Int = 8,
        trueString: String = "ON",
        falseString: String = "OFF"
    ) {
        parser = Parsers.boolean(
            byteIndex = byteIndex,
            bitPosition = bitPosition,
            bitWidth = bitWidth,
            trueString = trueString,
            falseString = falseString
        )
        parserFactory = null
    }

    /**
     * Configure parsing with an enum mapping.
     *
     * Maps raw byte values to enum constants.
     *
     * @param E The enum type
     * @param mapping Map of raw values to enum constants
     * @param default Default enum value for unmapped raw values
     * @param bytesToProcess Number of bytes to read (default: 1)
     * @param stringTransform Function to convert enum to display string
     */
    public fun <E : Enum<E>> parseAsEnum(
        mapping: Map<Int, E>,
        default: E,
        bytesToProcess: Int = 1,
        stringTransform: (E) -> String = { it.name }
    ) {
        parser = Parsers.enum(
            mapping = mapping,
            default = default,
            bytesToProcess = bytesToProcess,
            stringTransform = stringTransform
        )
        parserFactory = null
    }

    /**
     * Configure parsing with a custom string mapping.
     *
     * Maps raw byte values to descriptive strings.
     *
     * @param mapping Map of raw values to result strings
     * @param default Default string for unmapped raw values
     * @param bytesToProcess Number of bytes to read (default: 1)
     */
    public fun parseAsMapped(
        mapping: Map<Int, String>,
        default: String = "Unknown",
        bytesToProcess: Int = 1
    ) {
        parser = Parsers.mapped(
            mapping = mapping,
            default = default,
            bytesToProcess = bytesToProcess
        )
        parserFactory = null
    }

    /**
     * Configure a custom typed parser.
     *
     * Use this for complex parsing logic that doesn't fit standard patterns.
     *
     * @param T The type of value produced by the parser
     * @param customParser Parser function that transforms raw response to typed value
     */
    public fun <T> parseWith(customParser: ObdParser<T>) {
        parser = customParser
        parserFactory = null
    }

    /**
     * Configure a custom string handler.
     *
     * This creates a string-based parser. The result will be wrapped in [TypedValue.StringValue].
     *
     * @param handler Function that transforms raw response to string
     */
    public fun parseWith(handler: (ObdRawResponse) -> String) {
        parser = ObdParser { rawResponse ->
            TypedValue.StringValue(handler(rawResponse))
        }
        parserFactory = null
    }

    /**
     * Builds the OBD command from the configured properties.
     *
     * @return A new [ObdCommand] instance
     * @throws IllegalArgumentException if required properties are not set
     */
    internal fun build(): ObdCommand {
        require(tag.isNotBlank()) { "tag must be set" }
        require(name.isNotBlank()) { "name must be set" }
        require(mode.isNotBlank()) { "mode must be set" }
        require(pid.isNotBlank()) { "pid must be set" }

        // Resolve parser factory with final unit value (deferred from parseAs* calls)
        val builtParser = parserFactory?.invoke(unit) ?: parser ?: ObdParser { rawResponse ->
            TypedValue.StringValue(rawResponse.value)
        }

        return DslObdCommand(
            tag = tag,
            name = name,
            mode = mode,
            pid = pid,
            defaultUnit = unit,
            category = category,
            skipDigitCheck = skipDigitCheck,
            parser = builtParser
        )
    }
}

/**
 * Internal command implementation created by the DSL builder.
 *
 * This class wraps the configured parser and properties from [ObdCommandBuilder].
 *
 * @property tag Command identifier
 * @property name Human-readable name
 * @property mode OBD mode
 * @property pid Parameter ID
 * @property defaultUnit Unit string
 * @property category Command category
 * @property skipDigitCheck Whether to skip digit validation
 * @property parser The configured parser
 */
internal class DslObdCommand(
    override val tag: String,
    override val name: String,
    override val mode: String,
    override val pid: String,
    override val defaultUnit: String,
    override val category: CommandCategory,
    override val skipDigitCheck: Boolean,
    private val parser: ObdParser<*>
) : ObdCommand() {

    /**
     * Parses the raw response using the configured parser.
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return Typed value from the parser
     */
    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<*> {
        return parser.parse(rawResponse)
    }
}

/**
 * Creates an OBD command using a DSL builder.
 *
 * This is the primary way to create custom commands without defining a new class.
 * Configure the command properties and parsing strategy within the builder block.
 *
 * Example:
 * ```kotlin
 * val customSpeed = obdCommand {
 *     tag = "CUSTOM_SPEED"
 *     name = "Custom Speed Sensor"
 *     mode = "22"
 *     pid = "F400"
 *     unit = "km/h"
 *     category = CommandCategory.ENGINE
 *     parseAsInteger(bytesToProcess = 1)
 * }
 *
 * // Use the command
 * val response = customSpeed.handleResponse(rawResponse)
 * val speed = response.asInt()
 * ```
 *
 * @param block Configuration block for the command builder
 * @return A new [ObdCommand] with the specified configuration
 * @throws IllegalArgumentException if required properties (tag, name, mode, pid) are not set
 * @see ObdCommandBuilder
 */
public fun obdCommand(block: ObdCommandBuilder.() -> Unit): ObdCommand =
    ObdCommandBuilder().apply(block).build()

