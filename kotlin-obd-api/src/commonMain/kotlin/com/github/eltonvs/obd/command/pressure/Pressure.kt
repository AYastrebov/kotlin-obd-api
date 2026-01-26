package com.github.eltonvs.obd.command.pressure

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.formatToDecimalPlaces


public class BarometricPressureCommand : ObdCommand() {
    override val tag: String = "BAROMETRIC_PRESSURE"
    override val name: String = "Barometric Pressure"
    override val mode: String = "01"
    override val pid: String = "33"

    override val defaultUnit: String = "kPa"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> bytesToInt(it.bufferedValue, bytesToProcess = 1).toString() }
}

public class IntakeManifoldPressureCommand : ObdCommand() {
    override val tag: String = "INTAKE_MANIFOLD_PRESSURE"
    override val name: String = "Intake Manifold Pressure"
    override val mode: String = "01"
    override val pid: String = "0B"

    override val defaultUnit: String = "kPa"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> bytesToInt(it.bufferedValue, bytesToProcess = 1).toString() }
}

public class FuelPressureCommand : ObdCommand() {
    override val tag: String = "FUEL_PRESSURE"
    override val name: String = "Fuel Pressure"
    override val mode: String = "01"
    override val pid: String = "0A"

    override val defaultUnit: String = "kPa"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> (bytesToInt(it.bufferedValue, bytesToProcess = 1) * 3).toString() }
}

public class FuelRailPressureCommand : ObdCommand() {
    override val tag: String = "FUEL_RAIL_PRESSURE"
    override val name: String = "Fuel Rail Pressure"
    override val mode: String = "01"
    override val pid: String = "22"

    override val defaultUnit: String = "kPa"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(bytesToInt(it.bufferedValue) * 0.079f, 3)
    }
}

public class FuelRailGaugePressureCommand : ObdCommand() {
    override val tag: String = "FUEL_RAIL_GAUGE_PRESSURE"
    override val name: String = "Fuel Rail Gauge Pressure"
    override val mode: String = "01"
    override val pid: String = "23"

    override val defaultUnit: String = "kPa"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> (bytesToInt(it.bufferedValue) * 10).toString() }
}
