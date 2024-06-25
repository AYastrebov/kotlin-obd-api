package com.github.eltonvs.obd.command.temperature

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.formatToDecimalPlaces


private fun calculateTemperature(rawValue: IntArray): Float = bytesToInt(rawValue, bytesToProcess = 1) - 40f

class AirIntakeTemperatureCommand : ObdCommand() {
    override val tag = "AIR_INTAKE_TEMPERATURE"
    override val name = "Air Intake Temperature"
    override val mode = "01"
    override val pid = "0F"

    override val defaultUnit = "°C"
    override val handler = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateTemperature(it.bufferedValue), 1)
    }
}

class AmbientAirTemperatureCommand : ObdCommand() {
    override val tag = "AMBIENT_AIR_TEMPERATURE"
    override val name = "Ambient Air Temperature"
    override val mode = "01"
    override val pid = "46"

    override val defaultUnit = "°C"
    override val handler = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateTemperature(it.bufferedValue), 1)
    }
}

class EngineCoolantTemperatureCommand : ObdCommand() {
    override val tag = "ENGINE_COOLANT_TEMPERATURE"
    override val name = "Engine Coolant Temperature"
    override val mode = "01"
    override val pid = "05"

    override val defaultUnit = "°C"
    override val handler = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateTemperature(it.bufferedValue), 1)
    }
}

class OilTemperatureCommand : ObdCommand() {
    override val tag = "ENGINE_OIL_TEMPERATURE"
    override val name = "Engine Oil Temperature"
    override val mode = "01"
    override val pid = "5C"

    override val defaultUnit = "°C"
    override val handler = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculateTemperature(it.bufferedValue), 1)
    }
}