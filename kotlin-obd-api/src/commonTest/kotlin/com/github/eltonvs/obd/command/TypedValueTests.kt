package com.github.eltonvs.obd.command

import kotlin.test.Test
import kotlin.test.assertEquals

class TypedValueTests {

    @Test
    fun `IntegerValue stores value and generates string`() {
        val value = TypedValue.IntegerValue(42, unit = "Km/h")
        assertEquals(42L, value.value)
        assertEquals("42", value.stringValue)
        assertEquals("Km/h", value.unit)
    }

    @Test
    fun `FloatValue with decimal places`() {
        val value = TypedValue.FloatValue(123.456f, decimalPlaces = 2, unit = "g/s")
        assertEquals(123.456f, value.value, 0.001f)
        assertEquals("123.46", value.stringValue)
        assertEquals("g/s", value.unit)
    }

    @Test
    fun `PercentageValue formats correctly`() {
        val value = TypedValue.PercentageValue(75.5f, decimalPlaces = 1)
        assertEquals(75.5f, value.value, 0.01f)
        assertEquals("75.5", value.stringValue)
        assertEquals("%", value.unit)
    }

    @Test
    fun `TemperatureValue with default unit`() {
        val value = TypedValue.TemperatureValue(85.0f, decimalPlaces = 1)
        assertEquals(85.0f, value.value, 0.01f)
        assertEquals("85.0", value.stringValue)
        assertEquals("°C", value.unit)
    }

    @Test
    fun `PressureValue with custom unit`() {
        val value = TypedValue.PressureValue(101.3f, decimalPlaces = 1, unit = "bar")
        assertEquals(101.3f, value.value, 0.01f)
        assertEquals("101.3", value.stringValue)
        assertEquals("bar", value.unit)
    }

    @Test
    fun `StringValue stores value directly`() {
        val value = TypedValue.StringValue("Gasoline")
        assertEquals("Gasoline", value.value)
        assertEquals("Gasoline", value.stringValue)
    }

    @Test
    fun `EnumValue stores enum and name`() {
        val value = TypedValue.EnumValue(TestEnum.VALUE_A)
        assertEquals(TestEnum.VALUE_A, value.value)
        assertEquals("VALUE_A", value.stringValue)
    }

    @Test
    fun `EnumValue with custom string`() {
        val value = TypedValue.EnumValue(TestEnum.VALUE_B, stringValue = "Custom B")
        assertEquals(TestEnum.VALUE_B, value.value)
        assertEquals("Custom B", value.stringValue)
    }

    @Test
    fun `BooleanValue true`() {
        val value = TypedValue.BooleanValue(true, stringValue = "ON")
        assertEquals(true, value.value)
        assertEquals("ON", value.stringValue)
    }

    @Test
    fun `BooleanValue false`() {
        val value = TypedValue.BooleanValue(false, stringValue = "OFF")
        assertEquals(false, value.value)
        assertEquals("OFF", value.stringValue)
    }

    @Test
    fun `ListValue with separator`() {
        val value = TypedValue.ListValue(listOf("A", "B", "C"), separator = "|")
        assertEquals(listOf("A", "B", "C"), value.value)
        assertEquals("A|B|C", value.stringValue)
    }

    @Test
    fun `CompositeValue stores map`() {
        val map = mapOf("sensor1" to 100, "sensor2" to 200)
        val value = TypedValue.CompositeValue(map, stringValue = "S1=100, S2=200")
        assertEquals(map, value.value)
        assertEquals("S1=100, S2=200", value.stringValue)
    }

    @Test
    fun `DurationValue formatted as time`() {
        val value = TypedValue.DurationValue(seconds = 3661L, formatAsTime = true)
        assertEquals(3661L, value.value)
        assertEquals("01:01:01", value.stringValue)
    }

    @Test
    fun `DurationValue not formatted`() {
        val value = TypedValue.DurationValue(seconds = 3661L, formatAsTime = false)
        assertEquals(3661L, value.value)
        assertEquals("3661", value.stringValue)
    }

    @Test
    fun `DurationValue zero`() {
        val value = TypedValue.DurationValue(seconds = 0L, formatAsTime = true)
        assertEquals(0L, value.value)
        assertEquals("00:00:00", value.stringValue)
    }

    @Test
    fun `DurationValue max time`() {
        val value = TypedValue.DurationValue(seconds = 65535L, formatAsTime = true)
        assertEquals(65535L, value.value)
        assertEquals("18:12:15", value.stringValue)
    }

    private enum class TestEnum {
        VALUE_A, VALUE_B
    }
}
