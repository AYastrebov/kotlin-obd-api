package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.*

/**
 * Identifies the bank and sensor position for O2 sensor PIDs 0x14-0x1B.
 *
 * @property pid The OBD-II PID hex string
 */
public enum class OxygenSensorBank(internal val pid: String) {
    BANK_1_SENSOR_1("14"),
    BANK_1_SENSOR_2("15"),
    BANK_1_SENSOR_3("16"),
    BANK_1_SENSOR_4("17"),
    BANK_2_SENSOR_1("18"),
    BANK_2_SENSOR_2("19"),
    BANK_2_SENSOR_3("1A"),
    BANK_2_SENSOR_4("1B"),
}

/**
 * O2 Sensor Voltage command for PIDs 0x14-0x1B.
 *
 * Per SAE J1979, each PID returns 2 bytes:
 * - Byte A: Voltage = A / 200 (range 0–1.275 V)
 * - Byte B: Short term fuel trim = (B * 100 / 128) - 100 (range -100% to +99.2%)
 *
 * Returns a [TypedValue.CompositeValue] with keys "voltage" and "fuelTrim".
 *
 * @param sensor The O2 sensor bank/position to query
 */
public class OxygenSensorVoltageCommand(sensor: OxygenSensorBank) : ObdCommand() {
    override val tag: String = "O2_VOLTAGE_${sensor.name}"
    override val name: String = "O2 Sensor Voltage ${sensor.name.replace('_', ' ')}"
    override val mode: String = "01"
    override val pid: String = sensor.pid
    override val defaultUnit: String = "V"
    override val category: CommandCategory = CommandCategory.FUEL

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<*> {
        val bytes = rawResponse.bufferedValue
        val a = if (bytes.size > 2) bytes[2] else 0
        val b = if (bytes.size > 3) bytes[3] else 0

        val voltage = a / 200f
        val fuelTrim = (b * 100f / 128f) - 100f

        return TypedValue.CompositeValue(
            value = mapOf(
                "voltage" to voltage,
                "fuelTrim" to fuelTrim
            ),
            stringValue = "${formatToDecimalPlaces(voltage, 3)}V, ${formatToDecimalPlaces(fuelTrim, 1)}%"
        )
    }
}
