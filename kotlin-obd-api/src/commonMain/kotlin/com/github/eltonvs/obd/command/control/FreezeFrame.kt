package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.*
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.command.temperature.EngineCoolantTemperatureCommand

/**
 * Wraps any Mode 01 command to execute it as a Mode 02 Freeze Frame request.
 *
 * Mode 02 returns the same data as Mode 01, but captures the parameter values
 * at the moment a Diagnostic Trouble Code (DTC) was stored. The response format
 * is identical to Mode 01 with an additional frame number byte appended to the PID.
 *
 * @param baseCommand The Mode 01 command to wrap
 * @param frameNumber The freeze frame number to query (0x00 is most common)
 */
public open class FreezeFrameCommand(
    private val baseCommand: ObdCommand,
    private val frameNumber: Int = 0
) : ObdCommand() {
    override val tag: String = "FREEZE_FRAME_${baseCommand.tag}"
    override val name: String = "Freeze Frame ${baseCommand.name}"
    override val mode: String = "02"
    override val pid: String = "${baseCommand.pid} ${frameNumber.toString(16).padStart(2, '0').uppercase()}"
    override val defaultUnit: String = baseCommand.defaultUnit
    override val skipDigitCheck: Boolean = baseCommand.skipDigitCheck
    override val category: CommandCategory = CommandCategory.CONTROL

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<*> =
        baseCommand.parseTypedValue(rawResponse)
}

/**
 * Freeze Frame variant of SpeedCommand.
 * Returns the vehicle speed captured at the time of a DTC.
 */
public class FreezeFrameSpeedCommand(frameNumber: Int = 0) :
    FreezeFrameCommand(SpeedCommand(), frameNumber)

/**
 * Freeze Frame variant of RPMCommand.
 * Returns the engine RPM captured at the time of a DTC.
 */
public class FreezeFrameRPMCommand(frameNumber: Int = 0) :
    FreezeFrameCommand(RPMCommand(), frameNumber)

/**
 * Freeze Frame variant of CoolantTemperatureCommand.
 * Returns the coolant temperature captured at the time of a DTC.
 */
public class FreezeFrameCoolantTempCommand(frameNumber: Int = 0) :
    FreezeFrameCommand(EngineCoolantTemperatureCommand(), frameNumber)
