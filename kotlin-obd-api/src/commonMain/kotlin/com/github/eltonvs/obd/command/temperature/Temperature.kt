package com.github.eltonvs.obd.command.temperature

import com.github.eltonvs.obd.command.CommandCategory
import com.github.eltonvs.obd.command.TemperatureObdCommand

public class AirIntakeTemperatureCommand : TemperatureObdCommand() {
    override val tag: String = "AIR_INTAKE_TEMPERATURE"
    override val name: String = "Air Intake Temperature"
    override val mode: String = "01"
    override val pid: String = "0F"
}

public class AmbientAirTemperatureCommand : TemperatureObdCommand() {
    override val tag: String = "AMBIENT_AIR_TEMPERATURE"
    override val name: String = "Ambient Air Temperature"
    override val mode: String = "01"
    override val pid: String = "46"
}

public class EngineCoolantTemperatureCommand : TemperatureObdCommand() {
    override val tag: String = "ENGINE_COOLANT_TEMPERATURE"
    override val name: String = "Engine Coolant Temperature"
    override val mode: String = "01"
    override val pid: String = "05"
}

public class OilTemperatureCommand : TemperatureObdCommand() {
    override val tag: String = "ENGINE_OIL_TEMPERATURE"
    override val name: String = "Engine Oil Temperature"
    override val mode: String = "01"
    override val pid: String = "5C"
}
