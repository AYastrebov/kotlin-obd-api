package com.github.eltonvs.obd.command.egr

import com.github.eltonvs.obd.command.*
import com.github.eltonvs.obd.command.formatToDecimalPlaces

class CommandedEgrCommand : ObdCommand() {
    override val tag = "COMMANDED_EGR"
    override val name = "Commanded EGR"
    override val mode = "01"
    override val pid = "2C"

    override val defaultUnit = "%"
    override val handler = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculatePercentage(it.bufferedValue, bytesToProcess = 1), 1)
    }
}

class EgrErrorCommand : ObdCommand() {
    override val tag = "EGR_ERROR"
    override val name = "EGR Error"
    override val mode = "01"
    override val pid = "2D"

    override val defaultUnit = "%"
    override val handler = { it: ObdRawResponse ->
        formatToDecimalPlaces(bytesToInt(it.bufferedValue, bytesToProcess = 1) * (100f / 128) - 100, 1)
    }
}
