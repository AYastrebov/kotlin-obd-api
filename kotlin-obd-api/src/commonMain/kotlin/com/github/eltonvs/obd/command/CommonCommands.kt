package com.github.eltonvs.obd.command

/**
 * Base class for OBD commands that return integer values.
 *
 * Handles common patterns like reading bytes, applying multipliers, and offsets.
 *
 * Example usage:
 * ```kotlin
 * class VehicleSpeedCommand : IntegerObdCommand() {
 *     override val tag = "SPEED"
 *     override val name = "Vehicle Speed"
 *     override val mode = "01"
 *     override val pid = "0D"
 *     override val defaultUnit = "Km/h"
 *     override val bytesToProcess = 1
 * }
 * ```
 */
public abstract class IntegerObdCommand : TypedObdCommand<Long>() {
    /**
     * Number of bytes to process from the response.
     * Use -1 to process all available bytes.
     */
    public open val bytesToProcess: Int = -1

    /**
     * Multiplier to apply to the raw value
     */
    public open val multiplier: Float = 1f

    /**
     * Offset to add to the calculated value (applied after multiplier)
     */
    public open val offset: Long = 0

    /**
     * Number of decimal places for the string representation.
     * Only used when multiplier produces non-integer results.
     */
    public open val decimalPlaces: Int = 0

    override val category: CommandCategory = CommandCategory.ENGINE

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Long> {
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        val calculated = (rawValue * multiplier).toLong() + offset
        return TypedValue.IntegerValue(
            value = calculated,
            unit = defaultUnit
        )
    }
}

/**
 * Base class for OBD commands that return percentage values.
 *
 * Automatically calculates percentage from raw byte values (value * 100 / 255).
 *
 * Example usage:
 * ```kotlin
 * class EngineLoadCommand : PercentageObdCommand() {
 *     override val tag = "ENGINE_LOAD"
 *     override val name = "Engine Load"
 *     override val mode = "01"
 *     override val pid = "04"
 *     override val bytesToProcess = 1
 * }
 * ```
 */
public abstract class PercentageObdCommand : TypedObdCommand<Float>() {
    /**
     * Number of bytes to process from the response.
     * Use -1 to process all available bytes.
     */
    public open val bytesToProcess: Int = -1

    /**
     * Number of decimal places for the string representation
     */
    public open val decimalPlaces: Int = 1

    override val defaultUnit: String = "%"
    override val category: CommandCategory = CommandCategory.ENGINE

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Float> {
        val percentage = calculatePercentage(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        return TypedValue.PercentageValue(
            value = percentage,
            decimalPlaces = decimalPlaces
        )
    }
}

/**
 * Base class for OBD commands that return temperature values.
 *
 * Handles the standard OBD temperature calculation with configurable offset.
 *
 * Example usage:
 * ```kotlin
 * class CoolantTemperatureCommand : TemperatureObdCommand() {
 *     override val tag = "COOLANT_TEMP"
 *     override val name = "Engine Coolant Temperature"
 *     override val mode = "01"
 *     override val pid = "05"
 * }
 * ```
 */
public abstract class TemperatureObdCommand : TypedObdCommand<Float>() {
    /**
     * Temperature offset applied to the raw value.
     * Standard OBD temperature formula: value - 40
     */
    public open val temperatureOffset: Float = -40f

    /**
     * Number of bytes to process from the response
     */
    public open val bytesToProcess: Int = 1

    /**
     * Number of decimal places for the string representation
     */
    public open val decimalPlaces: Int = 1

    override val defaultUnit: String = "°C"
    override val category: CommandCategory = CommandCategory.TEMPERATURE

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Float> {
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        val temperature = rawValue + temperatureOffset
        return TypedValue.TemperatureValue(
            value = temperature,
            decimalPlaces = decimalPlaces,
            unit = defaultUnit
        )
    }
}

/**
 * Base class for OBD commands that return pressure values.
 *
 * Example usage:
 * ```kotlin
 * class FuelPressureCommand : PressureObdCommand() {
 *     override val tag = "FUEL_PRESSURE"
 *     override val name = "Fuel Pressure"
 *     override val mode = "01"
 *     override val pid = "0A"
 *     override val multiplier = 3f
 * }
 * ```
 */
public abstract class PressureObdCommand : TypedObdCommand<Float>() {
    /**
     * Number of bytes to process from the response
     */
    public open val bytesToProcess: Int = 1

    /**
     * Multiplier to apply to the raw value
     */
    public open val multiplier: Float = 1f

    /**
     * Offset to add to the calculated value
     */
    public open val offset: Float = 0f

    /**
     * Number of decimal places for the string representation
     */
    public open val decimalPlaces: Int = 1

    override val defaultUnit: String = "kPa"
    override val category: CommandCategory = CommandCategory.PRESSURE

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Float> {
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        val pressure = rawValue * multiplier + offset
        return TypedValue.PressureValue(
            value = pressure,
            decimalPlaces = decimalPlaces,
            unit = defaultUnit
        )
    }
}

/**
 * Base class for OBD commands that return enum values.
 *
 * Maps raw byte values to enum constants.
 *
 * Example usage:
 * ```kotlin
 * enum class FuelType {
 *     GASOLINE, DIESEL, ELECTRIC, HYBRID, UNKNOWN
 * }
 *
 * class FuelTypeCommand : EnumObdCommand<FuelType>() {
 *     override val tag = "FUEL_TYPE"
 *     override val name = "Fuel Type"
 *     override val mode = "01"
 *     override val pid = "51"
 *     override val enumMapping = mapOf(
 *         0x01 to FuelType.GASOLINE,
 *         0x04 to FuelType.DIESEL,
 *         0x08 to FuelType.ELECTRIC,
 *         0x11 to FuelType.HYBRID
 *     )
 *     override val defaultEnumValue = FuelType.UNKNOWN
 * }
 * ```
 *
 * @param E The enum type this command returns
 */
public abstract class EnumObdCommand<E : Enum<E>> : TypedObdCommand<E>() {
    /**
     * Mapping from raw byte values to enum constants
     */
    public abstract val enumMapping: Map<Int, E>

    /**
     * Default value when the raw value is not in the mapping
     */
    public abstract val defaultEnumValue: E

    /**
     * Number of bytes to process from the response
     */
    public open val bytesToProcess: Int = 1

    /**
     * Custom string formatter for the enum value.
     * Override to provide custom display names.
     */
    public open fun formatEnum(value: E): String = value.name

    override val category: CommandCategory = CommandCategory.CONTROL

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<E> {
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess).toInt()
        val enumValue = enumMapping[rawValue] ?: defaultEnumValue
        return TypedValue.EnumValue(
            value = enumValue,
            stringValue = formatEnum(enumValue),
            unit = defaultUnit
        )
    }
}

/**
 * Base class for OBD commands that return boolean values.
 *
 * Example usage:
 * ```kotlin
 * class MILStatusCommand : BooleanObdCommand() {
 *     override val tag = "MIL_STATUS"
 *     override val name = "Malfunction Indicator Lamp"
 *     override val mode = "01"
 *     override val pid = "01"
 *
 *     override fun evaluateBoolean(rawResponse: ObdRawResponse): Boolean {
 *         return rawResponse.bufferedValue[2].getBitAt(7, 8) == 1
 *     }
 * }
 * ```
 */
public abstract class BooleanObdCommand : TypedObdCommand<Boolean>() {
    /**
     * String representation for true value
     */
    public open val trueString: String = "ON"

    /**
     * String representation for false value
     */
    public open val falseString: String = "OFF"

    /**
     * Evaluate the raw response and determine the boolean value
     */
    public abstract fun evaluateBoolean(rawResponse: ObdRawResponse): Boolean

    override val category: CommandCategory = CommandCategory.CONTROL

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Boolean> {
        val value = evaluateBoolean(rawResponse)
        return TypedValue.BooleanValue(
            value = value,
            stringValue = if (value) trueString else falseString,
            unit = defaultUnit
        )
    }
}

/**
 * Base class for OBD commands that return duration/time values.
 *
 * Example usage:
 * ```kotlin
 * class EngineRuntimeCommand : DurationObdCommand() {
 *     override val tag = "RUNTIME"
 *     override val name = "Engine Runtime"
 *     override val mode = "01"
 *     override val pid = "1F"
 * }
 * ```
 */
public abstract class DurationObdCommand : TypedObdCommand<Long>() {
    /**
     * Number of bytes to process from the response
     */
    public open val bytesToProcess: Int = -1

    /**
     * Whether to format the duration as HH:MM:SS
     */
    public open val formatAsTime: Boolean = true

    override val category: CommandCategory = CommandCategory.ENGINE

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Long> {
        val seconds = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        return TypedValue.DurationValue(
            seconds = seconds,
            formatAsTime = formatAsTime
        )
    }
}

/**
 * Base class for OBD commands that return float values with custom calculations.
 *
 * Example usage:
 * ```kotlin
 * class MassAirFlowCommand : FloatObdCommand() {
 *     override val tag = "MAF"
 *     override val name = "Mass Air Flow"
 *     override val mode = "01"
 *     override val pid = "10"
 *     override val defaultUnit = "g/s"
 *     override val multiplier = 0.01f
 *     override val decimalPlaces = 2
 * }
 * ```
 */
public abstract class FloatObdCommand : TypedObdCommand<Float>() {
    /**
     * Number of bytes to process from the response
     */
    public open val bytesToProcess: Int = -1

    /**
     * Multiplier to apply to the raw value
     */
    public open val multiplier: Float = 1f

    /**
     * Offset to add to the calculated value
     */
    public open val offset: Float = 0f

    /**
     * Number of decimal places for the string representation
     */
    public open val decimalPlaces: Int = 2

    override val category: CommandCategory = CommandCategory.ENGINE

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Float> {
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = bytesToProcess)
        val calculated = rawValue * multiplier + offset
        return TypedValue.FloatValue(
            value = calculated,
            decimalPlaces = decimalPlaces,
            unit = defaultUnit
        )
    }
}
