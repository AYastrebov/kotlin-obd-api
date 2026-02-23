package com.github.eltonvs.obd.command

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObdResponseTypedValueTests {

    private val dummyCommand = object : ObdCommand() {
        override val tag = "TEST"
        override val name = "Test Command"
        override val mode = "01"
        override val pid = "00"
        override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<*> =
            TypedValue.StringValue(rawResponse.value)
    }

    private val dummyRawResponse = ObdRawResponse("410000", 0)

    @Test
    fun `ObdResponse with default typedValue returns null for typed accessors`() {
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "100",
            unit = "Km/h"
        )
        // typedValue defaults to StringValue("100")
        assertNull(response.asInt())
        assertNull(response.asFloat())
        assertNull(response.asPercentage())
        assertNull(response.asTemperature())
        assertNull(response.asPressure())
        assertNull(response.asBoolean())
        assertNull(response.asComposite())
        assertNull(response.asDuration())
    }

    @Test
    fun `asInt returns value for IntegerValue`() {
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "100",
            unit = "Km/h",
            typedValue = TypedValue.IntegerValue(100L, unit = "Km/h")
        )
        assertEquals(100L, response.asInt())
        assertNull(response.asFloat())
    }

    @Test
    fun `asFloat returns value for FloatValue`() {
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "3.14",
            typedValue = TypedValue.FloatValue(3.14f, decimalPlaces = 2)
        )
        assertEquals(3.14f, response.asFloat()!!, 0.001f)
        assertNull(response.asInt())
    }

    @Test
    fun `asPercentage returns value for PercentageValue`() {
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "75.5",
            unit = "%",
            typedValue = TypedValue.PercentageValue(75.5f, decimalPlaces = 1)
        )
        assertEquals(75.5f, response.asPercentage()!!, 0.01f)
    }

    @Test
    fun `asTemperature returns value for TemperatureValue`() {
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "85.0",
            unit = "°C",
            typedValue = TypedValue.TemperatureValue(85.0f, decimalPlaces = 1)
        )
        assertEquals(85.0f, response.asTemperature()!!, 0.01f)
    }

    @Test
    fun `asPressure returns value for PressureValue`() {
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "101.3",
            unit = "kPa",
            typedValue = TypedValue.PressureValue(101.3f, decimalPlaces = 1)
        )
        assertEquals(101.3f, response.asPressure()!!, 0.01f)
    }

    @Test
    fun `asBoolean returns value for BooleanValue`() {
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "ON",
            typedValue = TypedValue.BooleanValue(true, stringValue = "ON")
        )
        assertEquals(true, response.asBoolean())
    }

    @Test
    fun `asComposite returns value for CompositeValue`() {
        val map = mapOf("a" to 1, "b" to 2)
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "a=1, b=2",
            typedValue = TypedValue.CompositeValue(map, stringValue = "a=1, b=2")
        )
        assertEquals(map, response.asComposite())
    }

    @Test
    fun `asDuration returns value for DurationValue`() {
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "01:00:00",
            typedValue = TypedValue.DurationValue(seconds = 3600L, formatAsTime = true)
        )
        assertEquals(3600L, response.asDuration())
    }

    @Test
    fun `asList returns value for ListValue`() {
        val list = listOf("P0100", "P0200")
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "P0100, P0200",
            typedValue = TypedValue.ListValue(list)
        )
        assertEquals(list, response.asList<String>())
    }

    @Test
    fun `backward compatibility - formattedValue works without typedValue`() {
        val response = ObdResponse(
            command = dummyCommand,
            rawResponse = dummyRawResponse,
            value = "100",
            unit = "Km/h"
        )
        assertEquals("100Km/h", response.formattedValue)
    }
}
