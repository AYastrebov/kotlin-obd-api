package com.github.eltonvs.obd.command

/**
 * Base class for OBD commands that return integer values.
 *
 * Handles common patterns like reading bytes, applying multipliers, and offsets.
 * Extend this class for commands returning whole number values like speed, RPM, or distance.
 *
 * The formula used is: `value = (rawBytes * multiplier) + offset`
 *
 * Example usage:
 * ```kotlin
 * class VehicleSpeedCommand : IntegerObdCommand() {
 *     override val tag = "SPEED"
 *     override val name = "Vehicle Speed"
 *     override val mode = "01"
 *     override val pid = "0D"
 *     override val defaultUnit = "km/h"
 *     override val bytesToProcess = 1
 * }
 * ```
 *
 * @see TypedObdCommand
 * @see TypedValue.IntegerValue
 */
public abstract class IntegerObdCommand : TypedObdCommand<Long>() {
    /**
     * Number of bytes to process from the response.
     *
     * For single-byte values (0-255), use 1.
     * For two-byte values (0-65535), use 2.
     * Use -1 to process all available data bytes.
     */
    public open val bytesToProcess: Int = -1

    /**
     * Multiplier to apply to the raw value.
     *
     * For example, RPM uses 0.25f (divide by 4), while speed uses 1f.
     */
    public open val multiplier: Float = 1f

    /**
     * Offset to add to the calculated value (applied after multiplier).
     *
     * For example, timing advance uses an offset of -64.
     */
    public open val offset: Long = 0

    /**
     * Number of decimal places for the string representation.
     *
     * Only used when multiplier produces non-integer results.
     * Set to 0 for pure integer display.
     */
    public open val decimalPlaces: Int = 0

    override val category: CommandCategory = CommandCategory.ENGINE

    /**
     * Parses the raw response and returns an [TypedValue.IntegerValue].
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return Integer value calculated as (rawBytes * multiplier) + offset
     */
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
 * Automatically calculates percentage from raw byte values using the formula:
 * `percentage = (rawValue * 100) / 255`
 *
 * This is the standard OBD-II percentage calculation where 0x00 = 0% and 0xFF = 100%.
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
 *
 * @see TypedObdCommand
 * @see TypedValue.PercentageValue
 */
public abstract class PercentageObdCommand : TypedObdCommand<Float>() {
    /**
     * Number of bytes to process from the response.
     *
     * Most percentage values use 1 byte. Use -1 to process all available bytes.
     */
    public open val bytesToProcess: Int = -1

    /**
     * Number of decimal places for the string representation.
     *
     * Default is 1 (e.g., "50.2%").
     */
    public open val decimalPlaces: Int = 1

    override val defaultUnit: String = "%"
    override val category: CommandCategory = CommandCategory.ENGINE

    /**
     * Parses the raw response and returns a [TypedValue.PercentageValue].
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return Percentage value (0-100 range typically)
     */
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
 * The standard formula is: `temperature = rawValue + offset` (typically offset = -40).
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
 *
 * @see TypedObdCommand
 * @see TypedValue.TemperatureValue
 */
public abstract class TemperatureObdCommand : TypedObdCommand<Float>() {
    /**
     * Temperature offset applied to the raw value.
     *
     * Standard OBD temperature formula: `value - 40`
     * This allows a range of -40°C to 215°C with a single byte.
     */
    public open val temperatureOffset: Float = -40f

    /**
     * Number of bytes to process from the response.
     *
     * Most temperature values use 1 byte.
     */
    public open val bytesToProcess: Int = 1

    /**
     * Number of decimal places for the string representation.
     */
    public open val decimalPlaces: Int = 1

    override val defaultUnit: String = "°C"
    override val category: CommandCategory = CommandCategory.TEMPERATURE

    /**
     * Parses the raw response and returns a [TypedValue.TemperatureValue].
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return Temperature value with offset applied
     */
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
 * Calculates pressure using: `pressure = (rawValue * multiplier) + offset`
 *
 * Example usage:
 * ```kotlin
 * class FuelPressureCommand : PressureObdCommand() {
 *     override val tag = "FUEL_PRESSURE"
 *     override val name = "Fuel Pressure"
 *     override val mode = "01"
 *     override val pid = "0A"
 *     override val multiplier = 3f  // Fuel pressure = A * 3
 * }
 * ```
 *
 * @see TypedObdCommand
 * @see TypedValue.PressureValue
 */
public abstract class PressureObdCommand : TypedObdCommand<Float>() {
    /**
     * Number of bytes to process from the response.
     */
    public open val bytesToProcess: Int = 1

    /**
     * Multiplier to apply to the raw value.
     *
     * Different pressure PIDs use different multipliers (e.g., 3 for fuel pressure).
     */
    public open val multiplier: Float = 1f

    /**
     * Offset to add to the calculated value.
     */
    public open val offset: Float = 0f

    /**
     * Number of decimal places for the string representation.
     */
    public open val decimalPlaces: Int = 1

    override val defaultUnit: String = "kPa"
    override val category: CommandCategory = CommandCategory.PRESSURE

    /**
     * Parses the raw response and returns a [TypedValue.PressureValue].
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return Pressure value calculated as (rawValue * multiplier) + offset
     */
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
 * Maps raw byte values to enum constants using a provided mapping.
 * If the raw value is not in the mapping, returns [defaultEnumValue].
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
 * @see TypedObdCommand
 * @see TypedValue.EnumValue
 */
public abstract class EnumObdCommand<E : Enum<E>> : TypedObdCommand<E>() {
    /**
     * Mapping from raw byte values to enum constants.
     *
     * Keys are integer values from the OBD response, values are the corresponding enum constants.
     */
    public abstract val enumMapping: Map<Int, E>

    /**
     * Default value when the raw value is not in the mapping.
     *
     * This is returned when the vehicle reports an unexpected or unknown value.
     */
    public abstract val defaultEnumValue: E

    /**
     * Number of bytes to process from the response.
     *
     * Most enum values use 1 byte.
     */
    public open val bytesToProcess: Int = 1

    /**
     * Custom string formatter for the enum value.
     *
     * Override to provide custom display names instead of the enum constant name.
     *
     * @param value The enum value to format
     * @return String representation for display
     */
    public open fun formatEnum(value: E): String = value.name

    override val category: CommandCategory = CommandCategory.CONTROL

    /**
     * Parses the raw response and returns a [TypedValue.EnumValue].
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return Enum value from mapping, or defaultEnumValue if not found
     */
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
 * Implement [evaluateBoolean] to determine the boolean value from the raw response.
 * This allows flexible bit checking or other boolean logic.
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
 *         // MIL is bit 7 of byte A
 *         return rawResponse.bufferedValue[2].getBitAt(7, 8) == 1
 *     }
 * }
 * ```
 *
 * @see TypedObdCommand
 * @see TypedValue.BooleanValue
 */
public abstract class BooleanObdCommand : TypedObdCommand<Boolean>() {
    /**
     * String representation for true value.
     *
     * Override to customize (e.g., "Yes", "Active", "Enabled").
     */
    public open val trueString: String = "ON"

    /**
     * String representation for false value.
     *
     * Override to customize (e.g., "No", "Inactive", "Disabled").
     */
    public open val falseString: String = "OFF"

    /**
     * Evaluate the raw response and determine the boolean value.
     *
     * Implement this method to extract the boolean from the response bytes.
     * Common patterns include checking specific bits or comparing values.
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return true or false based on the response data
     */
    public abstract fun evaluateBoolean(rawResponse: ObdRawResponse): Boolean

    override val category: CommandCategory = CommandCategory.CONTROL

    /**
     * Parses the raw response and returns a [TypedValue.BooleanValue].
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return Boolean value determined by [evaluateBoolean]
     */
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
 * Returns values in seconds, with optional formatting as HH:MM:SS.
 * Use this for runtime, time since codes cleared, or other duration values.
 *
 * Example usage:
 * ```kotlin
 * class EngineRuntimeCommand : DurationObdCommand() {
 *     override val tag = "RUNTIME"
 *     override val name = "Engine Runtime"
 *     override val mode = "01"
 *     override val pid = "1F"
 *     override val bytesToProcess = 2  // 2 bytes for runtime
 * }
 * ```
 *
 * @see TypedObdCommand
 * @see TypedValue.DurationValue
 */
public abstract class DurationObdCommand : TypedObdCommand<Long>() {
    /**
     * Number of bytes to process from the response.
     *
     * Runtime typically uses 2 bytes (A*256 + B) for up to 65535 seconds.
     */
    public open val bytesToProcess: Int = -1

    /**
     * Whether to format the duration as HH:MM:SS.
     *
     * If false, displays raw seconds.
     */
    public open val formatAsTime: Boolean = true

    override val category: CommandCategory = CommandCategory.ENGINE

    /**
     * Parses the raw response and returns a [TypedValue.DurationValue].
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return Duration in seconds
     */
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
 * Calculates float values using: `value = (rawValue * multiplier) + offset`
 *
 * Use this for values requiring decimal precision like voltage, mass air flow, etc.
 *
 * Example usage:
 * ```kotlin
 * class MassAirFlowCommand : FloatObdCommand() {
 *     override val tag = "MAF"
 *     override val name = "Mass Air Flow"
 *     override val mode = "01"
 *     override val pid = "10"
 *     override val defaultUnit = "g/s"
 *     override val bytesToProcess = 2
 *     override val multiplier = 0.01f  // MAF = (A*256 + B) / 100
 *     override val decimalPlaces = 2
 * }
 * ```
 *
 * @see TypedObdCommand
 * @see TypedValue.FloatValue
 */
public abstract class FloatObdCommand : TypedObdCommand<Float>() {
    /**
     * Number of bytes to process from the response.
     */
    public open val bytesToProcess: Int = -1

    /**
     * Multiplier to apply to the raw value.
     */
    public open val multiplier: Float = 1f

    /**
     * Offset to add to the calculated value.
     */
    public open val offset: Float = 0f

    /**
     * Number of decimal places for the string representation.
     */
    public open val decimalPlaces: Int = 2

    override val category: CommandCategory = CommandCategory.ENGINE

    /**
     * Parses the raw response and returns a [TypedValue.FloatValue].
     *
     * @param rawResponse The raw response from the OBD adapter
     * @return Float value calculated as (rawValue * multiplier) + offset
     */
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
