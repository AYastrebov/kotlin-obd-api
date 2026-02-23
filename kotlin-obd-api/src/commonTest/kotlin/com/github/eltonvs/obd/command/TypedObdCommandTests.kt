package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.formatFloat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TypedObdCommandTests {

    @Test
    fun `TypedObdCommand populates both value and typedValue`() {
        val command = TestTypedIntegerCommand()
        val rawResponse = ObdRawResponse("410D64", 0) // Speed = 100
        val response = command.handleResponse(rawResponse)

        assertEquals("100", response.value)
        assertEquals("Km/h", response.unit)
        assertNotNull(response.typedValue)
        assertTrue(response.typedValue is TypedValue.IntegerValue)
        assertEquals(100L, response.asInt())
    }

    @Test
    fun `IntegerObdCommand parses simple value`() {
        val command = TestIntegerCommand()
        val rawResponse = ObdRawResponse("410D64", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(100L, response.asInt())
        assertEquals("Km/h", response.unit)
    }

    @Test
    fun `IntegerObdCommand applies multiplier and offset`() {
        val command = TestIntegerCommandWithMultiplier()
        val rawResponse = ObdRawResponse("410C1000", 0) // Raw = 4096
        val response = command.handleResponse(rawResponse)

        // 4096 / 4 = 1024
        assertEquals(1024L, response.asInt())
    }

    @Test
    fun `PercentageObdCommand calculates percentage`() {
        val command = TestPercentageCommand()
        val rawResponse = ObdRawResponse("410480", 0) // 128 = 50.2%
        val response = command.handleResponse(rawResponse)

        assertEquals(50.2f, response.asPercentage()!!, 0.1f)
        assertEquals("%", response.unit)
    }

    @Test
    fun `PercentageObdCommand 100 percent`() {
        val command = TestPercentageCommand()
        val rawResponse = ObdRawResponse("4104FF", 0) // 255 = 100%
        val response = command.handleResponse(rawResponse)

        assertEquals(100.0f, response.asPercentage()!!, 0.1f)
    }

    @Test
    fun `TemperatureObdCommand applies offset`() {
        val command = TestTemperatureCommand()
        val rawResponse = ObdRawResponse("410578", 0) // 120 - 40 = 80°C
        val response = command.handleResponse(rawResponse)

        assertEquals(80.0f, response.asTemperature()!!, 0.1f)
        assertEquals("°C", response.unit)
    }

    @Test
    fun `PressureObdCommand applies multiplier`() {
        val command = TestPressureCommand()
        val rawResponse = ObdRawResponse("410A64", 0) // 100 * 3 = 300 kPa
        val response = command.handleResponse(rawResponse)

        assertEquals(300.0f, response.asPressure()!!, 0.1f)
    }

    @Test
    fun `BooleanObdCommand evaluates condition`() {
        val command = TestBooleanCommand()
        val rawResponse = ObdRawResponse("410180", 0) // Bit 0 set = true
        val response = command.handleResponse(rawResponse)

        assertEquals(true, response.asBoolean())
        assertEquals("ON", response.value)
    }

    @Test
    fun `DurationObdCommand formats time`() {
        val command = TestDurationCommand()
        val rawResponse = ObdRawResponse("411F0E10", 0) // 3600 seconds
        val response = command.handleResponse(rawResponse)

        assertEquals(3600L, response.asDuration())
        assertEquals("01:00:00", response.value)
    }

    @Test
    fun `FloatObdCommand calculates with multiplier`() {
        val command = TestFloatCommand()
        val rawResponse = ObdRawResponse("41109511", 0) // 38161 * 0.01 = 381.61
        val response = command.handleResponse(rawResponse)

        assertEquals(381.61f, response.asFloat()!!, 0.01f)
    }

    // Test command implementations
    private class TestTypedIntegerCommand : ObdCommand() {
        override val tag = "TEST_TYPED"
        override val name = "Test Typed"
        override val mode = "01"
        override val pid = "0D"
        override val defaultUnit = "Km/h"
        override val category = CommandCategory.ENGINE

        override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<*> {
            val value = bytesToInt(rawResponse.bufferedValue, bytesToProcess = 1)
            return TypedValue.IntegerValue(value, unit = defaultUnit)
        }
    }

    private class TestIntegerCommand : IntegerObdCommand() {
        override val tag = "TEST_INT"
        override val name = "Test Integer"
        override val mode = "01"
        override val pid = "0D"
        override val defaultUnit = "Km/h"
        override val bytesToProcess = 1
    }

    private class TestIntegerCommandWithMultiplier : IntegerObdCommand() {
        override val tag = "TEST_RPM"
        override val name = "Test RPM"
        override val mode = "01"
        override val pid = "0C"
        override val defaultUnit = "RPM"
        override val bytesToProcess = -1
        override val multiplier = 0.25f
    }

    private class TestPercentageCommand : PercentageObdCommand() {
        override val tag = "TEST_PERCENT"
        override val name = "Test Percentage"
        override val mode = "01"
        override val pid = "04"
        override val bytesToProcess = 1
    }

    private class TestTemperatureCommand : TemperatureObdCommand() {
        override val tag = "TEST_TEMP"
        override val name = "Test Temperature"
        override val mode = "01"
        override val pid = "05"
    }

    private class TestPressureCommand : PressureObdCommand() {
        override val tag = "TEST_PRESSURE"
        override val name = "Test Pressure"
        override val mode = "01"
        override val pid = "0A"
        override val multiplier = 3f
    }

    private class TestBooleanCommand : BooleanObdCommand() {
        override val tag = "TEST_BOOL"
        override val name = "Test Boolean"
        override val mode = "01"
        override val pid = "01"

        override fun evaluateBoolean(rawResponse: ObdRawResponse): Boolean {
            return rawResponse.bufferedValue[2] and 0x80 != 0
        }
    }

    private class TestDurationCommand : DurationObdCommand() {
        override val tag = "TEST_DURATION"
        override val name = "Test Duration"
        override val mode = "01"
        override val pid = "1F"
    }

    private class TestFloatCommand : FloatObdCommand() {
        override val tag = "TEST_FLOAT"
        override val name = "Test Float"
        override val mode = "01"
        override val pid = "10"
        override val defaultUnit = "g/s"
        override val multiplier = 0.01f
        override val decimalPlaces = 2
    }
}

class EnumObdCommandTests {
    enum class TestFuelType { GASOLINE, DIESEL, ELECTRIC, UNKNOWN }

    private class TestEnumCommand : EnumObdCommand<TestFuelType>() {
        override val tag = "TEST_ENUM"
        override val name = "Test Enum"
        override val mode = "01"
        override val pid = "51"
        override val enumMapping = mapOf(
            0x01 to TestFuelType.GASOLINE,
            0x04 to TestFuelType.DIESEL,
            0x08 to TestFuelType.ELECTRIC
        )
        override val defaultEnumValue = TestFuelType.UNKNOWN

        override fun formatEnum(value: TestFuelType): String = when (value) {
            TestFuelType.GASOLINE -> "Gasoline"
            TestFuelType.DIESEL -> "Diesel"
            TestFuelType.ELECTRIC -> "Electric"
            TestFuelType.UNKNOWN -> "Unknown"
        }
    }

    @Test
    fun `EnumObdCommand maps known value`() {
        val command = TestEnumCommand()
        val rawResponse = ObdRawResponse("415101", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(TestFuelType.GASOLINE, response.asEnum<TestFuelType>())
        assertEquals("Gasoline", response.value)
    }

    @Test
    fun `EnumObdCommand returns default for unknown value`() {
        val command = TestEnumCommand()
        val rawResponse = ObdRawResponse("4151FF", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(TestFuelType.UNKNOWN, response.asEnum<TestFuelType>())
        assertEquals("Unknown", response.value)
    }
}
