package com.github.eltonvs.obd.command

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CommandBuilderTests {

    @Test
    fun `build simple integer command`() {
        val command = obdCommand {
            tag = "TEST_SPEED"
            name = "Test Speed"
            mode = "01"
            pid = "0D"
            unit = "Km/h"
            category = CommandCategory.ENGINE
            parseAsInteger(bytesToProcess = 1)
        }

        assertEquals("TEST_SPEED", command.tag)
        assertEquals("Test Speed", command.name)
        assertEquals("01", command.mode)
        assertEquals("0D", command.pid)
        assertEquals("Km/h", command.defaultUnit)
        assertEquals("01 0D", command.rawCommand)
    }

    @Test
    fun `command handles response correctly`() {
        val command = obdCommand {
            tag = "TEST_SPEED"
            name = "Test Speed"
            mode = "01"
            pid = "0D"
            unit = "Km/h"
            parseAsInteger(bytesToProcess = 1)
        }

        val rawResponse = ObdRawResponse("410D64", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals("100", response.value)
        assertEquals("Km/h", response.unit)
        assertNotNull(response.typedValue)
        assertEquals(100L, response.asInt())
    }

    @Test
    fun `build percentage command`() {
        val command = obdCommand {
            tag = "TEST_LOAD"
            name = "Test Load"
            mode = "01"
            pid = "04"
            parseAsPercentage(bytesToProcess = 1)
        }

        val rawResponse = ObdRawResponse("4104FF", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(100.0f, response.asPercentage()!!, 0.1f)
        assertEquals("%", response.unit)
    }

    @Test
    fun `build temperature command`() {
        val command = obdCommand {
            tag = "TEST_TEMP"
            name = "Test Temp"
            mode = "01"
            pid = "05"
            unit = "°C"
            parseAsTemperature()
        }

        val rawResponse = ObdRawResponse("410578", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(80.0f, response.asTemperature()!!, 0.1f)
    }

    @Test
    fun `build pressure command`() {
        val command = obdCommand {
            tag = "TEST_PRESSURE"
            name = "Test Pressure"
            mode = "01"
            pid = "0A"
            unit = "kPa"
            parseAsPressure(multiplier = 3f)
        }

        val rawResponse = ObdRawResponse("410A64", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(300.0f, response.asPressure()!!, 0.1f)
    }

    @Test
    fun `build float command`() {
        val command = obdCommand {
            tag = "TEST_MAF"
            name = "Test MAF"
            mode = "01"
            pid = "10"
            unit = "g/s"
            parseAsFloat(multiplier = 0.01f, decimalPlaces = 2)
        }

        val rawResponse = ObdRawResponse("41109511", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(381.61f, response.asFloat()!!, 0.01f)
    }

    @Test
    fun `build duration command`() {
        val command = obdCommand {
            tag = "TEST_RUNTIME"
            name = "Test Runtime"
            mode = "01"
            pid = "1F"
            parseAsDuration()
        }

        val rawResponse = ObdRawResponse("411F0E10", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(3600L, response.asDuration())
        assertEquals("01:00:00", response.value)
    }

    @Test
    fun `build boolean command`() {
        val command = obdCommand {
            tag = "TEST_MIL"
            name = "Test MIL"
            mode = "01"
            pid = "01"
            parseAsBoolean(byteIndex = 0, bitPosition = 1, trueString = "ON", falseString = "OFF")
        }

        val rawResponse = ObdRawResponse("410180", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(true, response.asBoolean())
        assertEquals("ON", response.value)
    }

    @Test
    fun `build enum command`() {
        val command = obdCommand {
            tag = "TEST_FUEL"
            name = "Test Fuel Type"
            mode = "01"
            pid = "51"
            parseAsEnum(
                mapping = mapOf(1 to TestFuel.GASOLINE, 4 to TestFuel.DIESEL),
                default = TestFuel.UNKNOWN
            )
        }

        val rawResponse = ObdRawResponse("415101", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(TestFuel.GASOLINE, response.asEnum<TestFuel>())
    }

    @Test
    fun `build mapped command`() {
        val command = obdCommand {
            tag = "TEST_STATUS"
            name = "Test Status"
            mode = "01"
            pid = "51"
            parseAsMapped(
                mapping = mapOf(0 to "Off", 1 to "On", 2 to "Standby"),
                default = "Unknown"
            )
        }

        val rawResponse = ObdRawResponse("415101", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals("On", response.value)
    }

    @Test
    fun `build with custom parser`() {
        val command = obdCommand {
            tag = "TEST_CUSTOM"
            name = "Test Custom"
            mode = "01"
            pid = "00"
            parseWith { rawResponse ->
                val value = rawResponse.bufferedValue[2] + rawResponse.bufferedValue[3]
                TypedValue.IntegerValue(value.toLong())
            }
        }

        val rawResponse = ObdRawResponse("41001020", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals(48L, response.asInt()) // 0x10 + 0x20 = 48
    }

    @Test
    fun `build with legacy handler`() {
        val command = obdCommand {
            tag = "TEST_LEGACY"
            name = "Test Legacy"
            mode = "01"
            pid = "00"
            parseWith { rawResponse ->
                "Custom: ${rawResponse.bufferedValue[2]}"
            }
        }

        val rawResponse = ObdRawResponse("410064", 0)
        val response = command.handleResponse(rawResponse)

        assertEquals("Custom: 100", response.value)
    }

    @Test
    fun `missing tag throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            obdCommand {
                name = "Test"
                mode = "01"
                pid = "00"
            }
        }
    }

    @Test
    fun `missing name throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            obdCommand {
                tag = "TEST"
                mode = "01"
                pid = "00"
            }
        }
    }

    @Test
    fun `missing mode throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            obdCommand {
                tag = "TEST"
                name = "Test"
                pid = "00"
            }
        }
    }

    @Test
    fun `missing pid throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            obdCommand {
                tag = "TEST"
                name = "Test"
                mode = "01"
            }
        }
    }

    @Test
    fun `typedObdCommand returns TypedObdCommand`() {
        val command = typedObdCommand {
            tag = "TEST"
            name = "Test"
            mode = "01"
            pid = "0D"
            parseAsInteger(bytesToProcess = 1)
        }

        assertTrue(command is TypedObdCommand<*>)
    }

    @Test
    fun `skipDigitCheck is configurable`() {
        val command = obdCommand {
            tag = "TEST"
            name = "Test"
            mode = "01"
            pid = "00"
            skipDigitCheck = true
        }

        assertTrue(command.skipDigitCheck)
    }

    private enum class TestFuel {
        GASOLINE, DIESEL, UNKNOWN
    }
}
