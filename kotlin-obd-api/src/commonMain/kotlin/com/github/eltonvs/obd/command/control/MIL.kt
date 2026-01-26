package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.bytesToInt


public class MILOnCommand : ObdCommand() {
    override val tag: String = "MIL_ON"
    override val name: String = "MIL on"
    override val mode: String = "01"
    override val pid: String = "01"

    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        val mil = it.bufferedValue.getOrElse(2) { 0 }
        val milOn = (mil and 0x80) == 128
        milOn.toString()
    }

    override fun format(response: ObdResponse): String {
        val milOn = response.value.toBoolean()
        return "MIL is ${if (milOn) "ON" else "OFF"}"
    }
}

public class DistanceMILOnCommand : ObdCommand() {
    override val tag: String = "DISTANCE_TRAVELED_MIL_ON"
    override val name: String = "Distance traveled with MIL on"
    override val mode: String = "01"
    override val pid: String = "21"

    override val defaultUnit: String = "Km"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> bytesToInt(it.bufferedValue).toString() }
}

public class TimeSinceMILOnCommand : ObdCommand() {
    override val tag: String = "TIME_TRAVELED_MIL_ON"
    override val name: String = "Time run with MIL on"
    override val mode: String = "01"
    override val pid: String = "4D"

    override val defaultUnit: String = "min"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> bytesToInt(it.bufferedValue).toString() }
}
