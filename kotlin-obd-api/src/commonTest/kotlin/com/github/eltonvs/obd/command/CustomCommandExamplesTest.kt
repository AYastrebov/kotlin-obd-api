package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.command.parser.ObdParser
import com.github.eltonvs.obd.command.parser.Parsers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Helper for multiplatform decimal formatting
private fun formatFloat(value: Float, decimalPlaces: Int): String {
    var factor = 1.0
    repeat(decimalPlaces) { factor *= 10.0 }
    val rounded = kotlin.math.round(value * factor) / factor
    val intPart = rounded.toLong()
    val decPart = kotlin.math.abs(((rounded - intPart) * factor).toLong())
    return if (decimalPlaces > 0) "$intPart.${decPart.toString().padStart(decimalPlaces, '0')}" else "$intPart"
}

/**
 * This test file demonstrates how consumers can create custom OBD commands.
 *
 * There are three main approaches:
 * 1. **Specialized Base Classes** - Extend IntegerObdCommand, PercentageObdCommand, etc.
 * 2. **DSL Builder** - Use obdCommand { } for quick inline commands
 * 3. **TypedObdCommand<T>** - Extend directly for complex custom parsing
 *
 * All approaches provide type-safe responses via the TypedValue system.
 */
class CustomCommandExamplesTest {

    // =========================================================================
    // APPROACH 1: Specialized Base Classes
    // =========================================================================
    // Use these when your command fits a common pattern (integer, percentage,
    // temperature, etc.). They handle parsing automatically.

    /**
     * Example: Custom speed command using IntegerObdCommand
     *
     * IntegerObdCommand is ideal for commands that return whole number values.
     * Configure bytesToProcess, multiplier, and offset as needed.
     */
    class CustomSpeedCommand : IntegerObdCommand() {
        override val tag = "CUSTOM_SPEED"
        override val name = "Custom Vehicle Speed"
        override val mode = "01"
        override val pid = "0D"
        override val defaultUnit = "km/h"
        override val bytesToProcess = 1  // Speed uses 1 byte (A)
        override val multiplier = 1f     // Value = A * 1
        override val offset = 0L         // No offset needed
        override val category = CommandCategory.ENGINE
    }

    @Test
    fun `example - custom speed command using IntegerObdCommand`() {
        val command = CustomSpeedCommand()

        // Simulate OBD response: "41 0D 50" where 0x50 = 80 km/h
        val rawResponse = ObdRawResponse("41 0D 50", 0)
        val response = command.handleResponse(rawResponse)

        // String value for display
        assertEquals("80", response.value)
        assertEquals("km/h", response.unit)

        // Type-safe access to the numeric value
        assertEquals(80L, response.asInt())
        assertNotNull(response.typedValue)
        assertTrue(response.typedValue is TypedValue.IntegerValue)
    }

    /**
     * Example: Custom RPM command with formula A*256+B divided by 4
     *
     * Some values use 2 bytes with a divisor. Use multiplier for this.
     */
    class CustomRpmCommand : IntegerObdCommand() {
        override val tag = "CUSTOM_RPM"
        override val name = "Custom Engine RPM"
        override val mode = "01"
        override val pid = "0C"
        override val defaultUnit = "RPM"
        override val bytesToProcess = 2    // RPM uses 2 bytes (A, B)
        override val multiplier = 0.25f    // Value = (A*256 + B) / 4
        override val category = CommandCategory.ENGINE
    }

    @Test
    fun `example - custom RPM command with 2-byte formula`() {
        val command = CustomRpmCommand()

        // Simulate: "41 0C 1A F8" where (0x1A * 256 + 0xF8) / 4 = 1726 RPM
        val rawResponse = ObdRawResponse("41 0C 1A F8", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(1726L, response.asInt())
        assertEquals("RPM", response.unit)
    }

    /**
     * Example: Throttle position using PercentageObdCommand
     *
     * PercentageObdCommand automatically converts byte value to percentage (0-100%).
     */
    class CustomThrottleCommand : PercentageObdCommand() {
        override val tag = "CUSTOM_THROTTLE"
        override val name = "Custom Throttle Position"
        override val mode = "01"
        override val pid = "11"
        override val category = CommandCategory.ENGINE
    }

    @Test
    fun `example - custom throttle using PercentageObdCommand`() {
        val command = CustomThrottleCommand()

        // Simulate: "41 11 80" where 0x80 = 128 -> 128/255 * 100 = 50.2%
        val rawResponse = ObdRawResponse("41 11 80", 0)
        val response = command.handleResponse(rawResponse)

        val percentage = response.asPercentage()
        assertNotNull(percentage)
        assertTrue(percentage > 50f && percentage < 51f)
        assertEquals("%", response.unit)
    }

    /**
     * Example: Coolant temperature using TemperatureObdCommand
     *
     * TemperatureObdCommand applies the standard -40 offset for OBD temperature values.
     */
    class CustomCoolantTempCommand : TemperatureObdCommand() {
        override val tag = "CUSTOM_COOLANT_TEMP"
        override val name = "Custom Coolant Temperature"
        override val mode = "01"
        override val pid = "05"
        override val category = CommandCategory.TEMPERATURE
        // Default temperatureOffset is -40f (standard OBD offset)
    }

    @Test
    fun `example - custom coolant temp using TemperatureObdCommand`() {
        val command = CustomCoolantTempCommand()

        // Simulate: "41 05 7B" where 0x7B = 123 -> 123 - 40 = 83°C
        val rawResponse = ObdRawResponse("41 05 7B", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(83f, response.asTemperature())
        assertEquals("°C", response.unit)
    }

    /**
     * Example: Fuel pressure using PressureObdCommand
     */
    class CustomFuelPressureCommand : PressureObdCommand() {
        override val tag = "CUSTOM_FUEL_PRESSURE"
        override val name = "Custom Fuel Pressure"
        override val mode = "01"
        override val pid = "0A"
        override val multiplier = 3f  // Value = A * 3
        override val category = CommandCategory.FUEL
    }

    @Test
    fun `example - custom fuel pressure using PressureObdCommand`() {
        val command = CustomFuelPressureCommand()

        // Simulate: "41 0A 50" where 0x50 = 80 -> 80 * 3 = 240 kPa
        val rawResponse = ObdRawResponse("41 0A 50", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(240f, response.asPressure())
        assertEquals("kPa", response.unit)
    }

    /**
     * Example: Fuel type using EnumObdCommand
     *
     * EnumObdCommand maps byte values to enum constants using enumMapping.
     */
    enum class CustomFuelType {
        UNKNOWN, GASOLINE, METHANOL, ETHANOL, DIESEL, LPG, CNG, ELECTRIC
    }

    class CustomFuelTypeCommand : EnumObdCommand<CustomFuelType>() {
        override val tag = "CUSTOM_FUEL_TYPE"
        override val name = "Custom Fuel Type"
        override val mode = "01"
        override val pid = "51"
        override val enumMapping = mapOf(
            0 to CustomFuelType.UNKNOWN,
            1 to CustomFuelType.GASOLINE,
            2 to CustomFuelType.METHANOL,
            3 to CustomFuelType.ETHANOL,
            4 to CustomFuelType.DIESEL,
            5 to CustomFuelType.LPG,
            6 to CustomFuelType.CNG,
            7 to CustomFuelType.ELECTRIC
        )
        override val defaultEnumValue = CustomFuelType.UNKNOWN
        override val category = CommandCategory.FUEL
    }

    @Test
    fun `example - custom fuel type using EnumObdCommand`() {
        val command = CustomFuelTypeCommand()

        // Simulate: "41 51 04" where 4 = DIESEL
        val rawResponse = ObdRawResponse("41 51 04", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(CustomFuelType.DIESEL, response.asEnum<CustomFuelType>())
        assertEquals("DIESEL", response.value)
    }

    /**
     * Example: MIL status using BooleanObdCommand
     *
     * BooleanObdCommand requires implementing evaluateBoolean() to determine the boolean value.
     */
    class CustomMilStatusCommand : BooleanObdCommand() {
        override val tag = "CUSTOM_MIL_STATUS"
        override val name = "Custom MIL Status"
        override val mode = "01"
        override val pid = "01"
        override val category = CommandCategory.DIAGNOSTIC

        override fun evaluateBoolean(rawResponse: ObdRawResponse): Boolean {
            // MIL is bit 7 (MSB) of byte A (index 2 in buffered response)
            val byte = rawResponse.bufferedValue.getOrElse(2) { 0 }
            return (byte and 0x80) != 0  // Check if bit 7 is set
        }
    }

    @Test
    fun `example - custom MIL status using BooleanObdCommand`() {
        val command = CustomMilStatusCommand()

        // Simulate: "41 01 81" where bit 7 of 0x81 is set (MIL on)
        val rawResponse = ObdRawResponse("41 01 81 00 00 00", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(true, response.asBoolean())
    }

    /**
     * Example: Run time since engine start using DurationObdCommand
     */
    class CustomRunTimeCommand : DurationObdCommand() {
        override val tag = "CUSTOM_RUN_TIME"
        override val name = "Custom Run Time"
        override val mode = "01"
        override val pid = "1F"
        override val defaultUnit = "s"  // Set the unit explicitly
        override val bytesToProcess = 2  // 2 bytes: A*256 + B
        override val category = CommandCategory.ENGINE
    }

    @Test
    fun `example - custom run time using DurationObdCommand`() {
        val command = CustomRunTimeCommand()

        // Simulate: "41 1F 01 2C" where 0x01*256 + 0x2C = 300 seconds
        val rawResponse = ObdRawResponse("41 1F 01 2C", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(300L, response.asDuration())
        assertEquals("s", response.unit)
    }

    // =========================================================================
    // APPROACH 2: DSL Builder
    // =========================================================================
    // Use the DSL builder for quick, inline command definitions without
    // creating a new class. Great for one-off commands or prototyping.

    @Test
    fun `example - DSL builder for integer command`() {
        // Define a command inline using the DSL
        val speedCommand = obdCommand {
            tag = "DSL_SPEED"
            name = "DSL Speed Command"
            mode = "01"
            pid = "0D"
            unit = "km/h"
            category = CommandCategory.ENGINE
            parseAsInteger(bytesToProcess = 1)
        }

        val rawResponse = ObdRawResponse("41 0D 64", 0)  // 0x64 = 100 km/h
        val response = speedCommand.handleResponse(rawResponse)

        assertEquals("100", response.value)
        assertEquals(100L, response.asInt())
    }

    @Test
    fun `example - DSL builder for percentage command`() {
        val throttleCommand = obdCommand {
            tag = "DSL_THROTTLE"
            name = "DSL Throttle Position"
            mode = "01"
            pid = "11"
            parseAsPercentage()
        }

        val rawResponse = ObdRawResponse("41 11 FF", 0)  // 0xFF = 255 -> 100%
        val response = throttleCommand.handleResponse(rawResponse)

        assertEquals(100f, response.asPercentage())
        assertEquals("%", response.unit)
    }

    @Test
    fun `example - DSL builder for temperature command`() {
        val tempCommand = obdCommand {
            tag = "DSL_TEMP"
            name = "DSL Temperature"
            mode = "01"
            pid = "05"
            unit = "°C"
            parseAsTemperature(offset = -40f)
        }

        val rawResponse = ObdRawResponse("41 05 50", 0)  // 0x50 = 80 -> 80-40 = 40°C
        val response = tempCommand.handleResponse(rawResponse)

        assertEquals(40f, response.asTemperature())
    }

    @Test
    fun `example - DSL builder for float command with formula`() {
        // Voltage = A / 200 (e.g., for O2 sensor)
        val voltageCommand = obdCommand {
            tag = "DSL_VOLTAGE"
            name = "DSL O2 Voltage"
            mode = "01"
            pid = "14"
            unit = "V"
            parseAsFloat(bytesToProcess = 1, multiplier = 0.005f)  // A * 0.005
        }

        val rawResponse = ObdRawResponse("41 14 80 00", 0)  // 0x80 = 128 -> 0.64V
        val response = voltageCommand.handleResponse(rawResponse)

        val voltage = response.asFloat()
        assertNotNull(voltage)
        assertTrue(voltage > 0.63f && voltage < 0.65f)
    }

    @Test
    fun `example - DSL builder for boolean command`() {
        val acCommand = obdCommand {
            tag = "DSL_AC_STATUS"
            name = "DSL A/C Status"
            mode = "01"
            pid = "13"
            // bitPosition is 1-based from MSB, byteIndex is 0-based from data start
            parseAsBoolean(byteIndex = 0, bitPosition = 8)  // Check LSB of first data byte
        }

        val rawResponse = ObdRawResponse("41 13 01", 0)  // Bit 0 (position 8) is set
        val response = acCommand.handleResponse(rawResponse)

        assertEquals(true, response.asBoolean())
    }

    @Test
    fun `example - DSL builder for mapped values`() {
        // Map byte values to descriptive strings
        val statusCommand = obdCommand {
            tag = "DSL_OBD_STATUS"
            name = "DSL OBD Compliance"
            mode = "01"
            pid = "1C"
            parseAsMapped(
                mapping = mapOf(
                    1 to "OBD-II (CARB)",
                    2 to "OBD (EPA)",
                    3 to "OBD + OBD-II",
                    6 to "EOBD",
                    7 to "EOBD + OBD-II"
                ),
                default = "Unknown"
            )
        }

        val rawResponse = ObdRawResponse("41 1C 06", 0)  // 6 = EOBD
        val response = statusCommand.handleResponse(rawResponse)

        assertEquals("EOBD", response.value)
    }

    @Test
    fun `example - DSL builder with custom parser`() {
        // For complex parsing, provide a custom parser function
        val customCommand = obdCommand {
            tag = "DSL_CUSTOM"
            name = "DSL Custom Parser"
            mode = "22"
            pid = "1234"
            unit = "custom"
            parseWith(ObdParser<Float> { rawResponse ->
                val bytes = rawResponse.bufferedValue
                // For mode 22 with 2-byte PID "1234", the response is:
                // bytes[0]=0x62 (mode+40), bytes[1]=0x12, bytes[2]=0x34 (PID), bytes[3+]=data
                val value = if (bytes.size >= 5) {
                    (bytes[3] * 256 + bytes[4]) / 10f
                } else 0f
                TypedValue.FloatValue(value, formatFloat(value, 1), "custom")
            })
        }

        // Response: 62 (mode+40), 12 34 (PID), 01 F4 (data = 0x01F4 = 500 -> 50.0)
        val rawResponse = ObdRawResponse("62 12 34 01 F4", 0)
        val response = customCommand.handleResponse(rawResponse)

        assertEquals(50f, response.asFloat())
    }

    @Test
    fun `example - DSL builder with legacy handler style`() {
        // For backward compatibility, you can use parseWith with a string-returning lambda
        val legacyCommand = obdCommand {
            tag = "DSL_LEGACY"
            name = "DSL Legacy Style"
            mode = "01"
            pid = "0D"
            unit = "km/h"
            parseWith { rawResponse: ObdRawResponse ->
                val speed = rawResponse.bufferedValue.getOrElse(2) { 0 }
                speed.toString()
            }
        }

        val rawResponse = ObdRawResponse("41 0D 64", 0)
        val response = legacyCommand.handleResponse(rawResponse)

        assertEquals("100", response.value)
        // Note: typedValue is StringValue when using legacy handler
    }

    // =========================================================================
    // APPROACH 3: TypedObdCommand<T> Direct Extension
    // =========================================================================
    // Use this for complex commands that don't fit standard patterns,
    // like commands returning multiple values or requiring custom logic.

    /**
     * Example: Command that returns multiple sensor values as a composite
     */
    class MultiSensorCommand : TypedObdCommand<Map<String, Any>>() {
        override val tag = "MULTI_SENSOR"
        override val name = "Multi Sensor Pack"
        override val mode = "22"
        override val pid = "ABCD"
        override val defaultUnit = ""
        override val category = CommandCategory.CUSTOM

        override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Map<String, Any>> {
            val bytes = rawResponse.bufferedValue

            // Parse multiple values from the response
            val temperature = if (bytes.size > 2) bytes[2] - 40 else 0
            val pressure = if (bytes.size > 3) bytes[3] * 3 else 0
            val voltage = if (bytes.size > 4) bytes[4] * 0.005f else 0f

            val values = mapOf(
                "temperature" to temperature,
                "pressure" to pressure,
                "voltage" to voltage
            )

            return TypedValue.CompositeValue(
                value = values,
                stringValue = "T=${temperature}°C, P=${pressure}kPa, V=${formatFloat(voltage, 2)}V"
            )
        }
    }

    @Test
    fun `example - TypedObdCommand for multi-value response`() {
        val command = MultiSensorCommand()

        // Response: 62 (mode+40), AB CD (PID), 50 (temp=80->40°C), 64 (pressure=100->300kPa), 80 (voltage=128->0.64V)
        // bytes[2]=0x50=80, bytes[3]=0x64=100, bytes[4]=0x80=128
        val rawResponse = ObdRawResponse("62 AB 50 64 80", 0)
        val response = command.handleResponse(rawResponse)

        val composite = response.asComposite()
        assertNotNull(composite)
        assertEquals(40, composite["temperature"])
        assertEquals(300, composite["pressure"])
        assertTrue((composite["voltage"] as Float) > 0.63f)
    }

    /**
     * Example: Command with conditional parsing based on response content
     */
    class ConditionalParsingCommand : TypedObdCommand<String>() {
        override val tag = "CONDITIONAL"
        override val name = "Conditional Parser"
        override val mode = "01"
        override val pid = "1C"
        override val defaultUnit = ""
        override val category = CommandCategory.DIAGNOSTIC

        override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<String> {
            val bytes = rawResponse.bufferedValue
            val code = bytes.getOrElse(2) { 0 }

            val standard = when {
                code == 0 -> "Not OBD compliant"
                code in 1..2 -> "OBD-II (US)"
                code in 3..6 -> "OBD + EOBD"
                code in 7..13 -> "EOBD (Europe)"
                code in 14..17 -> "OBD + EOBD + JOBD"
                else -> "Unknown (code: $code)"
            }

            return TypedValue.StringValue(standard, standard)
        }
    }

    @Test
    fun `example - TypedObdCommand with conditional parsing`() {
        val command = ConditionalParsingCommand()

        val rawResponse = ObdRawResponse("41 1C 07", 0)  // Code 7 = EOBD
        val response = command.handleResponse(rawResponse)

        assertEquals("EOBD (Europe)", response.value)
    }

    // =========================================================================
    // USING THE COMMAND REGISTRY
    // =========================================================================
    // Register commands for discovery and lookup by tag, category, or PID.

    @Test
    fun `example - registering and discovering commands`() {
        withTemporaryRegistry {
            // Register custom commands - use the overload that extracts mode/pid from command instance
            CommandRegistry.register(CustomSpeedCommand()) { CustomSpeedCommand() }
            CommandRegistry.register(CustomThrottleCommand()) { CustomThrottleCommand() }
            CommandRegistry.register(CustomCoolantTempCommand()) { CustomCoolantTempCommand() }

            // Lookup by tag
            val speedCmd = CommandRegistry.get("CUSTOM_SPEED")
            assertNotNull(speedCmd)
            assertEquals("Custom Vehicle Speed", speedCmd.name)

            // Lookup by category
            val engineCommands = CommandRegistry.getByCategory(CommandCategory.ENGINE)
            assertEquals(2, engineCommands.size)

            val tempCommands = CommandRegistry.getByCategory(CommandCategory.TEMPERATURE)
            assertEquals(1, tempCommands.size)

            // Lookup by PID (mode and pid are extracted from the command)
            val pid0DCommands = CommandRegistry.findByPid("01", "0D")
            assertEquals(1, pid0DCommands.size)
            assertEquals("CUSTOM_SPEED", pid0DCommands.first().tag)
        }
    }

    // =========================================================================
    // USING COMPOSABLE PARSERS
    // =========================================================================
    // Parsers can be composed and reused across multiple commands.

    @Test
    fun `example - using composable parsers directly`() {
        // Create reusable parsers
        val speedParser = Parsers.integer(bytesToProcess = 1, unit = "km/h")
        val tempParser = Parsers.temperature(offset = -40f)
        val percentParser = Parsers.percentage()

        // Use parsers to parse raw responses
        val speedResponse = ObdRawResponse("41 0D 64", 0)
        val speedValue = speedParser.parse(speedResponse)
        assertEquals(100L, speedValue.value)

        val tempResponse = ObdRawResponse("41 05 78", 0)  // 120 - 40 = 80°C
        val tempValue = tempParser.parse(tempResponse)
        assertEquals(80f, tempValue.value)

        val percentResponse = ObdRawResponse("41 11 80", 0)  // ~50%
        val percentValue = percentParser.parse(percentResponse)
        assertTrue(percentValue.value > 50f && percentValue.value < 51f)
    }

    @Test
    fun `example - enum parser with custom mapping`() {
        val fuelTypeParser = Parsers.enum(
            mapping = mapOf(
                0 to CustomFuelType.UNKNOWN,
                1 to CustomFuelType.GASOLINE,
                2 to CustomFuelType.METHANOL,
                3 to CustomFuelType.ETHANOL,
                4 to CustomFuelType.DIESEL
            ),
            default = CustomFuelType.UNKNOWN
        )

        val response = ObdRawResponse("41 51 01", 0)  // 1 = GASOLINE
        val value = fuelTypeParser.parse(response)

        assertEquals(CustomFuelType.GASOLINE, value.value)
    }

    @Test
    fun `example - mapped parser for string lookup`() {
        val statusParser = Parsers.mapped(
            mapping = mapOf(
                0 to "Off",
                1 to "Idle",
                2 to "Running",
                3 to "Error"
            ),
            default = "Unknown"
        )

        // Response: 41 (mode+40), 00 (PID), 02 (data = "Running")
        val response = ObdRawResponse("41 00 02", 0)
        val value = statusParser.parse(response)

        assertEquals("Running", value.value)
    }
}
