package com.github.eltonvs.obd.command.engine

import com.github.eltonvs.obd.command.*
import com.github.eltonvs.obd.command.formatToDecimalPlaces


public class SpeedCommand : ObdCommand() {
    override val tag: String = "SPEED"
    override val name: String = "Vehicle Speed"
    override val mode: String = "01"
    override val pid: String = "0D"

    override val defaultUnit: String = "Km/h"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        bytesToInt(it.bufferedValue, bytesToProcess = 1).toString()
    }
}

public class RPMCommand : ObdCommand() {
    override val tag: String = "ENGINE_RPM"
    override val name: String = "Engine RPM"
    override val mode: String = "01"
    override val pid: String = "0C"

    override val defaultUnit: String = "RPM"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        (bytesToInt(it.bufferedValue) / 4).toString()
    }
}

public class MassAirFlowCommand : ObdCommand() {
    override val tag: String = "MAF"
    override val name: String = "Mass Air Flow"
    override val mode: String = "01"
    override val pid: String = "10"

    override val defaultUnit: String = "g/s"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(bytesToInt(it.bufferedValue) / 100f, 2)
    }
}

public class RuntimeCommand : ObdCommand() {
    override val tag: String = "ENGINE_RUNTIME"
    override val name: String = "Engine Runtime"
    override val mode: String = "01"
    override val pid: String = "1F"

    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> parseRuntime(it.bufferedValue) }

    private fun parseRuntime(rawValue: IntArray): String {
        val seconds = bytesToInt(rawValue)
        val hh = seconds / 3600
        val mm = (seconds % 3600) / 60
        val ss = seconds % 60
        return listOf(hh, mm, ss).joinToString(":") { it.toString().padStart(2, '0') }
    }
}

public class LoadCommand : ObdCommand() {
    override val tag: String = "ENGINE_LOAD"
    override val name: String = "Engine Load"
    override val mode: String = "01"
    override val pid: String = "04"

    override val defaultUnit: String = "%"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculatePercentage(it.bufferedValue, bytesToProcess = 1), 1)
    }
}

public class AbsoluteLoadCommand : ObdCommand() {
    override val tag: String = "ENGINE_ABSOLUTE_LOAD"
    override val name: String = "Engine Absolute Load"
    override val mode: String = "01"
    override val pid: String = "43"

    override val defaultUnit: String = "%"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculatePercentage(it.bufferedValue), 1)
    }
}

public class ThrottlePositionCommand : ObdCommand() {
    override val tag: String = "THROTTLE_POSITION"
    override val name: String = "Throttle Position"
    override val mode: String = "01"
    override val pid: String = "11"

    override val defaultUnit: String = "%"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculatePercentage(it.bufferedValue, bytesToProcess = 1), 1)
    }
}

public class RelativeThrottlePositionCommand : ObdCommand() {
    override val tag: String = "RELATIVE_THROTTLE_POSITION"
    override val name: String = "Relative Throttle Position"
    override val mode: String = "01"
    override val pid: String = "45"

    override val defaultUnit: String = "%"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculatePercentage(it.bufferedValue, bytesToProcess = 1), 1)
    }
}
