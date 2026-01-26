package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.command.parser.ObdParser
import com.github.eltonvs.obd.command.parser.Parsers

/**
 * DSL builder for creating OBD commands without subclassing.
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
 */
public class ObdCommandBuilder {
    /**
     * Unique identifier for this command
     */
    public var tag: String = ""

    /**
     * Human-readable name for this command
     */
    public var name: String = ""

    /**
     * OBD mode (e.g., "01" for current data, "22" for manufacturer-specific)
     */
    public var mode: String = ""

    /**
     * Parameter ID within the mode
     */
    public var pid: String = ""

    /**
     * Unit string for the response value
     */
    public var unit: String = ""

    /**
     * Category for grouping this command
     */
    public var category: CommandCategory = CommandCategory.CUSTOM

    /**
     * Whether to skip digit check during response validation
     */
    public var skipDigitCheck: Boolean = false

    private var parser: ObdParser<*>? = null

    /**
     * Configure parsing as an integer value
     */
    public fun parseAsInteger(
        bytesToProcess: Int = -1,
        multiplier: Float = 1f,
        offset: Long = 0
    ) {
        parser = Parsers.integer(
            bytesToProcess = bytesToProcess,
            multiplier = multiplier,
            offset = offset,
            unit = unit
        )
    }

    /**
     * Configure parsing as a float value
     */
    public fun parseAsFloat(
        bytesToProcess: Int = -1,
        multiplier: Float = 1f,
        offset: Float = 0f,
        decimalPlaces: Int = 2
    ) {
        parser = Parsers.float(
            bytesToProcess = bytesToProcess,
            multiplier = multiplier,
            offset = offset,
            decimalPlaces = decimalPlaces,
            unit = unit
        )
    }

    /**
     * Configure parsing as a percentage value
     */
    public fun parseAsPercentage(
        bytesToProcess: Int = -1,
        decimalPlaces: Int = 1
    ) {
        parser = Parsers.percentage(
            bytesToProcess = bytesToProcess,
            decimalPlaces = decimalPlaces
        )
    }

    /**
     * Configure parsing as a temperature value
     */
    public fun parseAsTemperature(
        bytesToProcess: Int = 1,
        offset: Float = -40f,
        decimalPlaces: Int = 1
    ) {
        parser = Parsers.temperature(
            bytesToProcess = bytesToProcess,
            offset = offset,
            decimalPlaces = decimalPlaces,
            unit = unit.ifEmpty { "°C" }
        )
    }

    /**
     * Configure parsing as a pressure value
     */
    public fun parseAsPressure(
        bytesToProcess: Int = 1,
        multiplier: Float = 1f,
        offset: Float = 0f,
        decimalPlaces: Int = 1
    ) {
        parser = Parsers.pressure(
            bytesToProcess = bytesToProcess,
            multiplier = multiplier,
            offset = offset,
            decimalPlaces = decimalPlaces,
            unit = unit.ifEmpty { "kPa" }
        )
    }

    /**
     * Configure parsing as a duration value
     */
    public fun parseAsDuration(
        bytesToProcess: Int = -1,
        formatAsTime: Boolean = true
    ) {
        parser = Parsers.duration(
            bytesToProcess = bytesToProcess,
            formatAsTime = formatAsTime
        )
    }

    /**
     * Configure parsing as a boolean value based on a bit
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
    }

    /**
     * Configure parsing with an enum mapping
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
    }

    /**
     * Configure parsing with a custom string mapping
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
    }

    /**
     * Configure a custom parser
     */
    public fun <T> parseWith(customParser: ObdParser<T>) {
        parser = customParser
    }

    /**
     * Configure a custom handler function (for backward compatibility)
     */
    public fun parseWith(handler: (ObdRawResponse) -> String) {
        parser = ObdParser { rawResponse ->
            TypedValue.StringValue(handler(rawResponse))
        }
    }

    internal fun build(): ObdCommand {
        require(tag.isNotBlank()) { "tag must be set" }
        require(name.isNotBlank()) { "name must be set" }
        require(mode.isNotBlank()) { "mode must be set" }
        require(pid.isNotBlank()) { "pid must be set" }

        val builtParser = parser ?: ObdParser { rawResponse ->
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
 * Internal command implementation created by the DSL builder
 */
internal class DslObdCommand(
    override val tag: String,
    override val name: String,
    override val mode: String,
    override val pid: String,
    override val defaultUnit: String,
    val category: CommandCategory,
    override val skipDigitCheck: Boolean,
    private val parser: ObdParser<*>
) : TypedObdCommand<Any>() {

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Any> {
        @Suppress("UNCHECKED_CAST")
        return parser.parse(rawResponse) as TypedValue<Any>
    }
}

/**
 * Creates an OBD command using a DSL builder.
 *
 * Example:
 * ```kotlin
 * val customSpeed = obdCommand {
 *     tag = "CUSTOM_SPEED"
 *     name = "Custom Speed Sensor"
 *     mode = "22"
 *     pid = "F400"
 *     unit = "Km/h"
 *     category = CommandCategory.ENGINE
 *     parseAsInteger(bytesToProcess = 1)
 * }
 * ```
 */
public fun obdCommand(block: ObdCommandBuilder.() -> Unit): ObdCommand =
    ObdCommandBuilder().apply(block).build()

/**
 * Creates a typed OBD command using a DSL builder.
 *
 * This variant returns a TypedObdCommand for cases where you need
 * access to the typed command interface.
 */
public fun typedObdCommand(block: ObdCommandBuilder.() -> Unit): TypedObdCommand<*> =
    ObdCommandBuilder().apply(block).build() as TypedObdCommand<*>
