package com.github.eltonvs.obd.command.temperature

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.formatToDecimalPlaces


private fun calculateTemperature(rawValue: IntArray): Float = bytesToInt(rawValue, bytesToProcess = 1) - 40f

public class AirIntakeTemperatureCommand : ObdCommand() {
    override val tag: String = "AIR_INTAKE_TEMPERATURE"
    override val name: String = "Air Intake Temperature"
    override val mode: String = "01"
    override val pid: String = "0F"

    override val defaultUnit: String = "°C"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateTemperature(it.bufferedValue), 1)
    }
}

public class AmbientAirTemperatureCommand : ObdCommand() {
    override val tag: String = "AMBIENT_AIR_TEMPERATURE"
    override val name: String = "Ambient Air Temperature"
    override val mode: String = "01"
    override val pid: String = "46"

    override val defaultUnit: String = "°C"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateTemperature(it.bufferedValue), 1)
    }
}

public class EngineCoolantTemperatureCommand : ObdCommand() {
    override val tag: String = "ENGINE_COOLANT_TEMPERATURE"
    override val name: String = "Engine Coolant Temperature"
    override val mode: String = "01"
    override val pid: String = "05"

    override val defaultUnit: String = "°C"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateTemperature(it.bufferedValue), 1)
    }
}

public class OilTemperatureCommand : ObdCommand() {
    override val tag: String = "ENGINE_OIL_TEMPERATURE"
    override val name: String = "Engine Oil Temperature"
    override val mode: String = "01"
    override val pid: String = "5C"

    override val defaultUnit: String = "°C"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateTemperature(it.bufferedValue), 1)
    }
}
