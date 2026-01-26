package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.formatToDecimalPlaces


private fun calculateFuelAirRatio(rawValue: IntArray): Float = bytesToInt(rawValue, bytesToProcess = 2) * (2 / 65_536f)

public class CommandedEquivalenceRatioCommand : ObdCommand() {
    override val tag: String = "COMMANDED_EQUIVALENCE_RATIO"
    override val name: String = "Fuel-Air Commanded Equivalence Ratio"
    override val mode: String = "01"
    override val pid: String = "44"

    override val defaultUnit: String = "F/A"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateFuelAirRatio(it.bufferedValue), 2)
    }
}

public class FuelAirEquivalenceRatioCommand(oxygenSensor: OxygenSensor) : ObdCommand() {
    override val tag: String = "FUEL_AIR_EQUIVALENCE_RATIO_${oxygenSensor.name}"
    override val name: String = "Fuel-Air Equivalence Ratio - ${oxygenSensor.displayName}"
    override val mode: String = "01"
    override val pid: String = oxygenSensor.pid

    override val defaultUnit: String = "F/A"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateFuelAirRatio(it.bufferedValue), 2)
    }

    public enum class OxygenSensor(public val displayName: String, internal val pid: String) {
        OXYGEN_SENSOR_1("Oxygen Sensor 1", "34"),
        OXYGEN_SENSOR_2("Oxygen Sensor 2", "35"),
        OXYGEN_SENSOR_3("Oxygen Sensor 3", "36"),
        OXYGEN_SENSOR_4("Oxygen Sensor 4", "37"),
        OXYGEN_SENSOR_5("Oxygen Sensor 5", "38"),
        OXYGEN_SENSOR_6("Oxygen Sensor 6", "39"),
        OXYGEN_SENSOR_7("Oxygen Sensor 7", "3A"),
        OXYGEN_SENSOR_8("Oxygen Sensor 8", "3B"),
    }
}
