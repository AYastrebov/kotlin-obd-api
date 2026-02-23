package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.CommandCategory
import com.github.eltonvs.obd.command.ObdRawResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OxygenSensorVoltageCommandTests {

    @Test
    fun `test command properties for each sensor`() {
        val pids = listOf("14", "15", "16", "17", "18", "19", "1A", "1B")
        OxygenSensorBank.entries.forEachIndexed { index, sensor ->
            val command = OxygenSensorVoltageCommand(sensor)
            assertEquals("01", command.mode)
            assertEquals(pids[index], command.pid)
            assertEquals("V", command.defaultUnit)
            assertEquals(CommandCategory.FUEL, command.category)
            assertEquals("O2_VOLTAGE_${sensor.name}", command.tag)
        }
    }

    @Test
    fun `test voltage calculation`() {
        // Response: 41 14 FF 80
        // Byte A = 0xFF (255) -> voltage = 255/200 = 1.275V
        // Byte B = 0x80 (128) -> fuel trim = (128*100/128) - 100 = 0%
        val rawResponse = ObdRawResponse(value = "4114FF80", elapsedTime = 0)
        val command = OxygenSensorVoltageCommand(OxygenSensorBank.BANK_1_SENSOR_1)
        val response = command.handleResponse(rawResponse)

        val composite = response.asComposite()
        assertNotNull(composite)
        val voltage = composite["voltage"] as Float
        val fuelTrim = composite["fuelTrim"] as Float
        assertTrue(voltage > 1.27f && voltage < 1.28f, "Expected ~1.275V, got $voltage")
        assertTrue(fuelTrim > -0.1f && fuelTrim < 0.1f, "Expected ~0%, got $fuelTrim")
    }

    @Test
    fun `test zero voltage and min fuel trim`() {
        // Byte A = 0x00 -> voltage = 0V
        // Byte B = 0x00 -> fuel trim = (0*100/128) - 100 = -100%
        val rawResponse = ObdRawResponse(value = "41140000", elapsedTime = 0)
        val command = OxygenSensorVoltageCommand(OxygenSensorBank.BANK_1_SENSOR_1)
        val response = command.handleResponse(rawResponse)

        val composite = response.asComposite()
        assertNotNull(composite)
        val voltage = composite["voltage"] as Float
        val fuelTrim = composite["fuelTrim"] as Float
        assertEquals(0f, voltage)
        assertEquals(-100f, fuelTrim)
    }

    @Test
    fun `test max voltage and max fuel trim`() {
        // Byte A = 0xFF (255) -> voltage = 1.275V
        // Byte B = 0xFF (255) -> fuel trim = (255*100/128) - 100 = 99.21875%
        val rawResponse = ObdRawResponse(value = "41 14 FF FF", elapsedTime = 0)
        val command = OxygenSensorVoltageCommand(OxygenSensorBank.BANK_1_SENSOR_1)
        val response = command.handleResponse(rawResponse)

        val composite = response.asComposite()
        assertNotNull(composite)
        val voltage = composite["voltage"] as Float
        val fuelTrim = composite["fuelTrim"] as Float
        assertTrue(voltage > 1.27f && voltage < 1.28f, "Expected ~1.275V, got $voltage")
        assertTrue(fuelTrim > 99f && fuelTrim < 100f, "Expected ~99.2%, got $fuelTrim")
    }

    @Test
    fun `test mid range values`() {
        // Byte A = 0x64 (100) -> voltage = 100/200 = 0.5V
        // Byte B = 0x40 (64) -> fuel trim = (64*100/128) - 100 = -50%
        val rawResponse = ObdRawResponse(value = "41146440", elapsedTime = 0)
        val command = OxygenSensorVoltageCommand(OxygenSensorBank.BANK_1_SENSOR_1)
        val response = command.handleResponse(rawResponse)

        val composite = response.asComposite()
        assertNotNull(composite)
        val voltage = composite["voltage"] as Float
        val fuelTrim = composite["fuelTrim"] as Float
        assertEquals(0.5f, voltage)
        assertEquals(-50f, fuelTrim)
    }
}
