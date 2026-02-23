package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.*


public class MILOnCommand : BooleanObdCommand() {
    override val tag: String = "MIL_ON"
    override val name: String = "MIL on"
    override val mode: String = "01"
    override val pid: String = "01"

    override fun evaluateBoolean(rawResponse: ObdRawResponse): Boolean {
        val mil = rawResponse.bufferedValue.getOrElse(2) { 0 }
        return (mil and 0x80) == 128
    }

    override fun format(response: ObdResponse): String {
        val milOn = response.asBoolean() ?: false
        return "MIL is ${if (milOn) "ON" else "OFF"}"
    }
}

public class DistanceMILOnCommand : IntegerObdCommand() {
    override val tag: String = "DISTANCE_TRAVELED_MIL_ON"
    override val name: String = "Distance traveled with MIL on"
    override val mode: String = "01"
    override val pid: String = "21"
    override val defaultUnit: String = "Km"
    override val category: CommandCategory = CommandCategory.CONTROL
}

public class TimeSinceMILOnCommand : IntegerObdCommand() {
    override val tag: String = "TIME_TRAVELED_MIL_ON"
    override val name: String = "Time run with MIL on"
    override val mode: String = "01"
    override val pid: String = "4D"
    override val defaultUnit: String = "min"
    override val category: CommandCategory = CommandCategory.CONTROL
}
