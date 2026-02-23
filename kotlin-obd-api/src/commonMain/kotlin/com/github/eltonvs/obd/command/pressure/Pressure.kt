package com.github.eltonvs.obd.command.pressure

import com.github.eltonvs.obd.command.CommandCategory
import com.github.eltonvs.obd.command.IntegerObdCommand
import com.github.eltonvs.obd.command.PressureObdCommand

public class BarometricPressureCommand : PressureObdCommand() {
    override val tag: String = "BAROMETRIC_PRESSURE"
    override val name: String = "Barometric Pressure"
    override val mode: String = "01"
    override val pid: String = "33"
    override val bytesToProcess: Int = 1
    override val decimalPlaces: Int = 0
}

public class IntakeManifoldPressureCommand : PressureObdCommand() {
    override val tag: String = "INTAKE_MANIFOLD_PRESSURE"
    override val name: String = "Intake Manifold Pressure"
    override val mode: String = "01"
    override val pid: String = "0B"
    override val bytesToProcess: Int = 1
    override val decimalPlaces: Int = 0
}

public class FuelPressureCommand : PressureObdCommand() {
    override val tag: String = "FUEL_PRESSURE"
    override val name: String = "Fuel Pressure"
    override val mode: String = "01"
    override val pid: String = "0A"
    override val bytesToProcess: Int = 1
    override val multiplier: Float = 3f
    override val decimalPlaces: Int = 0
}

public class FuelRailPressureCommand : PressureObdCommand() {
    override val tag: String = "FUEL_RAIL_PRESSURE"
    override val name: String = "Fuel Rail Pressure"
    override val mode: String = "01"
    override val pid: String = "22"
    override val bytesToProcess: Int = -1
    override val multiplier: Float = 0.079f
    override val decimalPlaces: Int = 3
}

public class FuelRailGaugePressureCommand : PressureObdCommand() {
    override val tag: String = "FUEL_RAIL_GAUGE_PRESSURE"
    override val name: String = "Fuel Rail Gauge Pressure"
    override val mode: String = "01"
    override val pid: String = "23"
    override val bytesToProcess: Int = -1
    override val multiplier: Float = 10f
    override val decimalPlaces: Int = 0
}
