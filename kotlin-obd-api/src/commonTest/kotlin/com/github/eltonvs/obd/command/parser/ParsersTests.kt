package com.github.eltonvs.obd.command.parser

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.TypedValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParsersTests {

    @Test
    fun `integer parser basic`() {
        val parser = Parsers.integer(bytesToProcess = 1)
        val rawResponse = ObdRawResponse("410D64", 0) // 100

        val result = parser.parse(rawResponse)
        assertTrue(result is TypedValue.IntegerValue)
        assertEquals(100L, result.value)
    }

    @Test
    fun `integer parser with multiplier`() {
        val parser = Parsers.integer(bytesToProcess = -1, multiplier = 0.25f, unit = "RPM")
        val rawResponse = ObdRawResponse("410C1000", 0) // 4096 * 0.25 = 1024

        val result = parser.parse(rawResponse)
        assertEquals(1024L, (result as TypedValue.IntegerValue).value)
        assertEquals("RPM", result.unit)
    }

    @Test
    fun `integer parser with offset`() {
        val parser = Parsers.integer(bytesToProcess = 1, offset = -40)
        val rawResponse = ObdRawResponse("410578", 0) // 120 - 40 = 80

        val result = parser.parse(rawResponse)
        assertEquals(80L, (result as TypedValue.IntegerValue).value)
    }

    @Test
    fun `float parser`() {
        val parser = Parsers.float(bytesToProcess = -1, multiplier = 0.01f, decimalPlaces = 2, unit = "g/s")
        val rawResponse = ObdRawResponse("41109511", 0) // 38161 * 0.01 = 381.61

        val result = parser.parse(rawResponse)
        assertTrue(result is TypedValue.FloatValue)
        assertEquals(381.61f, result.value, 0.01f)
        assertEquals("g/s", result.unit)
    }

    @Test
    fun `percentage parser`() {
        val parser = Parsers.percentage(bytesToProcess = 1)
        val rawResponse = ObdRawResponse("4104FF", 0) // 255 = 100%

        val result = parser.parse(rawResponse)
        assertTrue(result is TypedValue.PercentageValue)
        assertEquals(100.0f, result.value, 0.1f)
        assertEquals("%", result.unit)
    }

    @Test
    fun `percentage parser half value`() {
        val parser = Parsers.percentage(bytesToProcess = 1)
        val rawResponse = ObdRawResponse("410480", 0) // 128 ≈ 50.2%

        val result = parser.parse(rawResponse)
        assertEquals(50.2f, (result as TypedValue.PercentageValue).value, 0.1f)
    }

    @Test
    fun `temperature parser with default offset`() {
        val parser = Parsers.temperature()
        val rawResponse = ObdRawResponse("410578", 0) // 120 - 40 = 80°C

        val result = parser.parse(rawResponse)
        assertTrue(result is TypedValue.TemperatureValue)
        assertEquals(80.0f, result.value, 0.1f)
        assertEquals("°C", result.unit)
    }

    @Test
    fun `temperature parser with custom offset`() {
        val parser = Parsers.temperature(offset = 0f)
        val rawResponse = ObdRawResponse("410550", 0) // 80 + 0 = 80°C

        val result = parser.parse(rawResponse)
        assertEquals(80.0f, (result as TypedValue.TemperatureValue).value, 0.1f)
    }

    @Test
    fun `pressure parser`() {
        val parser = Parsers.pressure(multiplier = 3f)
        val rawResponse = ObdRawResponse("410A64", 0) // 100 * 3 = 300 kPa

        val result = parser.parse(rawResponse)
        assertTrue(result is TypedValue.PressureValue)
        assertEquals(300.0f, result.value, 0.1f)
    }

    @Test
    fun `enum parser maps value`() {
        val parser = Parsers.enum(
            mapping = mapOf(1 to TestEnum.VALUE_A, 2 to TestEnum.VALUE_B),
            default = TestEnum.UNKNOWN
        )
        val rawResponse = ObdRawResponse("415101", 0)

        val result = parser.parse(rawResponse)
        assertTrue(result is TypedValue.EnumValue<*>)
        assertEquals(TestEnum.VALUE_A, result.value)
    }

    @Test
    fun `enum parser returns default for unknown`() {
        val parser = Parsers.enum(
            mapping = mapOf(1 to TestEnum.VALUE_A),
            default = TestEnum.UNKNOWN
        )
        val rawResponse = ObdRawResponse("4151FF", 0)

        val result = parser.parse(rawResponse)
        assertEquals(TestEnum.UNKNOWN, (result as TypedValue.EnumValue<*>).value)
    }

    @Test
    fun `boolean parser true`() {
        val parser = Parsers.boolean(byteIndex = 0, bitPosition = 1, trueString = "YES", falseString = "NO")
        val rawResponse = ObdRawResponse("410180", 0) // Byte 2 (first data byte) = 0x80, bit 1 = 1

        val result = parser.parse(rawResponse)
        assertTrue(result is TypedValue.BooleanValue)
        assertEquals(true, result.value)
        assertEquals("YES", result.stringValue)
    }

    @Test
    fun `boolean parser false`() {
        val parser = Parsers.boolean(byteIndex = 0, bitPosition = 1, trueString = "YES", falseString = "NO")
        val rawResponse = ObdRawResponse("410140", 0) // Byte 2 = 0x40, bit 1 = 0

        val result = parser.parse(rawResponse)
        assertEquals(false, (result as TypedValue.BooleanValue).value)
        assertEquals("NO", result.stringValue)
    }

    @Test
    fun `duration parser formatted`() {
        val parser = Parsers.duration(formatAsTime = true)
        val rawResponse = ObdRawResponse("411F0E10", 0) // 3600 seconds

        val result = parser.parse(rawResponse)
        assertTrue(result is TypedValue.DurationValue)
        assertEquals(3600L, result.value)
        assertEquals("01:00:00", result.stringValue)
    }

    @Test
    fun `duration parser not formatted`() {
        val parser = Parsers.duration(formatAsTime = false)
        val rawResponse = ObdRawResponse("411F0E10", 0) // 3600 seconds

        val result = parser.parse(rawResponse)
        assertEquals("3600", (result as TypedValue.DurationValue).stringValue)
    }

    @Test
    fun `mapped parser with string mapping`() {
        val parser = Parsers.mapped(
            mapping = mapOf(1 to "Gasoline", 4 to "Diesel"),
            default = "Unknown"
        )
        val rawResponse = ObdRawResponse("415101", 0)

        val result = parser.parse(rawResponse)
        assertTrue(result is TypedValue.StringValue)
        assertEquals("Gasoline", result.value)
    }

    @Test
    fun `withUnit extension adds unit`() {
        val parser = Parsers.integer(bytesToProcess = 1).withUnit("mph")
        val rawResponse = ObdRawResponse("410D64", 0)

        val result = parser.parse(rawResponse)
        assertEquals("mph", (result as TypedValue.IntegerValue).unit)
    }

    private enum class TestEnum {
        VALUE_A, VALUE_B, UNKNOWN
    }
}
