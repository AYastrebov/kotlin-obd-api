package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.TypedValue
import kotlin.test.Test
import kotlin.test.assertEquals

class MILOnCommandTests {
    @Test
    fun `test valid MIL on responses handler`() {
        listOf(
            "410100452100" to false,
            "410100000000" to false,
            "41017F000000" to false,
            "41017FFFFFFF" to false,
            "410180000000" to true,
            "410180FFFFFF" to true,
            "4101FFFFFFFF" to true
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = MILOnCommand().run {
                handleResponse(rawResponse)
            }
            assertEquals("MIL is ${if (expected) "ON" else "OFF"}", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }

    @Test
    fun `test short response handler returns MIL off`() {
        // Test the handler directly to verify getOrElse provides safe array access
        // Response with fewer than 3 bytes should default to MIL off (using getOrElse with default 0)
        val command = MILOnCommand()
        listOf(
            "4101" to false,      // Only 2 bytes (mode + pid), missing data byte
            "410100" to false,    // Only 3 bytes, third byte is 0
            "41" to false,        // Only 1 byte
            "" to false           // Empty response
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            // Test parseTypedValue directly to bypass validation (which would throw for invalid responses)
            val result = command.parseTypedValue(rawResponse)
            assertEquals(expected, (result as TypedValue.BooleanValue).value, "parseTypedValue failed for short response: '$rawValue'")
        }
    }

    @Test
    fun `test minimal valid response returns MIL off`() {
        // Test with a minimal but valid hex response
        val rawResponse = ObdRawResponse(value = "410100", elapsedTime = 0)
        val obdResponse = MILOnCommand().run {
            handleResponse(rawResponse)
        }
        assertEquals("MIL is OFF", obdResponse.formattedValue)
    }
}

class DistanceMILOnCommandTests {
    @Test
    fun `test valid distance MIL on responses handler`() {
        listOf(
            "41210000" to 0,
            "41215C8D" to 23_693,
            "4121FFFF" to 65_535
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = DistanceMILOnCommand().run {
                handleResponse(rawResponse)
            }
            assertEquals("${expected}Km", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class TimeSinceMILOnCommandTests {
    @Test
    fun `test valid time since MIL on responses handler`() {
        listOf(
            "414D0000" to 0,
            "414D5C8D" to 23_693,
            "414DFFFF" to 65_535
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = TimeSinceMILOnCommand().run {
                handleResponse(rawResponse)
            }
            assertEquals("${expected}min", obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}
