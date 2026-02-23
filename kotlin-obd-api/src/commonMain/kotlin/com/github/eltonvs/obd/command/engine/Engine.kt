package com.github.eltonvs.obd.command.engine

import com.github.eltonvs.obd.command.*

/**
 * Vehicle speed command, returns speed in km/h.
 *
 * Data returned as single byte, formula: speed = A
 */
public class SpeedCommand : IntegerObdCommand() {
    override val tag: String = "SPEED"
    override val name: String = "Vehicle Speed"
    override val mode: String = "01"
    override val pid: String = "0D"
    override val defaultUnit: String = "Km/h"
    override val bytesToProcess: Int = 1
}

/**
 * Engine RPM command, returns RPM value.
 *
 * Data returned as two bytes, formula: RPM = (A*256 + B) / 4
 */
public class RPMCommand : IntegerObdCommand() {
    override val tag: String = "ENGINE_RPM"
    override val name: String = "Engine RPM"
    override val mode: String = "01"
    override val pid: String = "0C"
    override val defaultUnit: String = "RPM"
    override val multiplier: Float = 0.25f
    override val decimalPlaces: Int = 0
}

/**
 * Mass air flow command, returns mass air flow in g/s.
 *
 * Data returned as two bytes, formula: MAF = (A*256 + B) / 100
 */
public class MassAirFlowCommand : FloatObdCommand() {
    override val tag: String = "MAF"
    override val name: String = "Mass Air Flow"
    override val mode: String = "01"
    override val pid: String = "10"
    override val defaultUnit: String = "g/s"
    override val multiplier: Float = 0.01f
    override val decimalPlaces: Int = 2
}

/**
 * Engine runtime command, returns runtime formatted as HH:MM:SS.
 *
 * Data returned as two bytes (seconds), formula: runtime = A*256 + B
 */
public class RuntimeCommand : DurationObdCommand() {
    override val tag: String = "ENGINE_RUNTIME"
    override val name: String = "Engine Runtime"
    override val mode: String = "01"
    override val pid: String = "1F"
    override val formatAsTime: Boolean = true
}

/**
 * Engine load command, returns load as percentage.
 *
 * Data returned as single byte, formula: load = A * 100 / 255
 */
public class LoadCommand : PercentageObdCommand() {
    override val tag: String = "ENGINE_LOAD"
    override val name: String = "Engine Load"
    override val mode: String = "01"
    override val pid: String = "04"
    override val bytesToProcess: Int = 1
}

/**
 * Absolute engine load command, returns load as percentage (can exceed 100%).
 *
 * Data returned as two bytes, formula: load = (A*256 + B) * 100 / 255
 */
public class AbsoluteLoadCommand : PercentageObdCommand() {
    override val tag: String = "ENGINE_ABSOLUTE_LOAD"
    override val name: String = "Engine Absolute Load"
    override val mode: String = "01"
    override val pid: String = "43"
}

/**
 * Throttle position command, returns position as percentage.
 *
 * Data returned as single byte, formula: position = A * 100 / 255
 */
public class ThrottlePositionCommand : PercentageObdCommand() {
    override val tag: String = "THROTTLE_POSITION"
    override val name: String = "Throttle Position"
    override val mode: String = "01"
    override val pid: String = "11"
    override val bytesToProcess: Int = 1
}

/**
 * Relative throttle position command, returns position as percentage.
 *
 * Data returned as single byte, formula: position = A * 100 / 255
 */
public class RelativeThrottlePositionCommand : PercentageObdCommand() {
    override val tag: String = "RELATIVE_THROTTLE_POSITION"
    override val name: String = "Relative Throttle Position"
    override val mode: String = "01"
    override val pid: String = "45"
    override val bytesToProcess: Int = 1
}
