package com.github.eltonvs.obd.command

import kotlin.test.Test
import kotlin.test.assertEquals

class BytesToIntTests {
    @Test
    fun `test valid results for bytesToInt`() {
        listOf(
            Triple(intArrayOf(0x0), 0 to -1, 0L),
            Triple(intArrayOf(0x1), 0 to -1, 1L),
            Triple(intArrayOf(0x1), 0 to 1, 1L),
            Triple(intArrayOf(0x10), 0 to -1, 16L),
            Triple(intArrayOf(0x11), 0 to -1, 17L),
            Triple(intArrayOf(0xFF), 0 to -1, 255L),
            Triple(intArrayOf(0xFF, 0xFF), 0 to -1, 65535L),
            Triple(intArrayOf(0xFF, 0xFF), 0 to 1, 255L),
            Triple(intArrayOf(0xFF, 0x00), 0 to -1, 65280L),
            Triple(intArrayOf(0xFF, 0x00), 0 to 1, 255L),
            Triple(intArrayOf(0x41, 0x0D, 0x40), 2 to -1, 64L),
            Triple(intArrayOf(0x41, 0x0D, 0x40, 0xFF), 2 to 1, 64L)
        ).forEach { (bufferedValue, params, expected) ->
            val (start, bytesToProcess) = params
            val result = bytesToInt(bufferedValue, start = start, bytesToProcess = bytesToProcess)
            assertEquals(expected, result, "Failed for: ${bufferedValue.contentToString()}, start=$start, bytesToProcess=$bytesToProcess")
        }
    }
}

class CalculatePercentageTests {
    @Test
    fun `test calculatePercentage with various values`() {
        listOf(
            intArrayOf(0x41, 0x00, 0x00) to 0f,
            intArrayOf(0x41, 0x00, 0x80) to 50.196f,
            intArrayOf(0x41, 0x00, 0xFF) to 100f,
            intArrayOf(0x41, 0x00, 0x7F) to 49.804f
        ).forEach { (bufferedValue, expected) ->
            val result = calculatePercentage(bufferedValue)
            assertEquals(expected, result, 0.01f, "Failed for: ${bufferedValue.contentToString()}")
        }
    }

    @Test
    fun `test calculatePercentage with bytesToProcess parameter`() {
        val bufferedValue = intArrayOf(0x41, 0x00, 0x80, 0xFF)
        val result = calculatePercentage(bufferedValue, bytesToProcess = 1)
        assertEquals(50.196f, result, 0.01f)
    }
}

class GetBitAtIntTests {
    @Test
    fun `test Int getBitAt for various positions`() {
        // Value 0b10110100 = 180
        val value = 0b10110100
        listOf(
            1 to 1,  // MSB
            2 to 0,
            3 to 1,
            4 to 1,
            5 to 0,
            6 to 1,
            7 to 0,
            8 to 0   // LSB
        ).forEach { (position, expected) ->
            assertEquals(expected, value.getBitAt(position, 8), "Failed for position: $position")
        }
    }

    @Test
    fun `test Int getBitAt with default last parameter`() {
        val value = 0x80000000.toInt()  // MSB set in 32-bit
        assertEquals(1, value.getBitAt(1))
        assertEquals(0, value.getBitAt(32))
    }
}

class GetBitAtLongTests {
    @Test
    fun `test Long getBitAt for various positions`() {
        val value = 0b10110100L
        listOf(
            1 to 1,
            2 to 0,
            3 to 1,
            4 to 1,
            5 to 0,
            6 to 1,
            7 to 0,
            8 to 0
        ).forEach { (position, expected) ->
            assertEquals(expected, value.getBitAt(position, 8), "Failed for position: $position")
        }
    }
}

class FormatToDecimalPlacesTests {
    @Test
    fun `test formatToDecimalPlaces with various values`() {
        listOf(
            Triple(0f, 2, "0.00"),
            Triple(1.5f, 2, "1.50"),
            Triple(1.234f, 2, "1.23"),
            Triple(1.235f, 2, "1.24"),
            Triple(1.999f, 2, "2.00"),
            Triple(10f, 3, "10.000"),
            Triple(0.1f, 3, "0.100"),
            Triple(123.456789f, 3, "123.457")
        ).forEach { (value, decimalPlaces, expected) ->
            val result = formatToDecimalPlaces(value, decimalPlaces)
            assertEquals(expected, result, "Failed for value: $value, decimalPlaces: $decimalPlaces")
        }
    }

    @Test
    fun `test formatToDecimalPlaces with single decimal place`() {
        assertEquals("5.0", formatToDecimalPlaces(5f, 1))
        assertEquals("5.5", formatToDecimalPlaces(5.5f, 1))
        assertEquals("5.6", formatToDecimalPlaces(5.55f, 1))
    }
}

class ConvertHexToStringTests {
    @Test
    fun `test convertHexToString with valid hex`() {
        assertEquals("ABC", convertHexToString("414243"))
        assertEquals("Hello", convertHexToString("48656C6C6F"))
        assertEquals("", convertHexToString(""))
    }

    @Test
    fun `test convertHexToString with invalid hex returns partial result`() {
        // Invalid hex characters should be skipped
        val result = convertHexToString("41GG42")
        assertEquals("AB", result)
    }
}

class FormatToHexTests {
    @Test
    fun `test Int formatToHex`() {
        listOf(
            0 to "00",
            1 to "01",
            15 to "0F",
            16 to "10",
            255 to "FF",
            256 to "100"
        ).forEach { (value, expected) ->
            assertEquals(expected, value.formatToHex(), "Failed for value: $value")
        }
    }
}
