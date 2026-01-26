package com.github.eltonvs.obd.command.egr

import com.github.eltonvs.obd.command.*
import com.github.eltonvs.obd.command.formatToDecimalPlaces

public class CommandedEgrCommand : ObdCommand() {
    override val tag: String = "COMMANDED_EGR"
    override val name: String = "Commanded EGR"
    override val mode: String = "01"
    override val pid: String = "2C"

    override val defaultUnit: String = "%"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculatePercentage(it.bufferedValue, bytesToProcess = 1), 1)
    }
}

public class EgrErrorCommand : ObdCommand() {
    override val tag: String = "EGR_ERROR"
    override val name: String = "EGR Error"
    override val mode: String = "01"
    override val pid: String = "2D"

    override val defaultUnit: String = "%"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(bytesToInt(it.bufferedValue, bytesToProcess = 1) * (100f / 128) - 100, 1)
    }
}
