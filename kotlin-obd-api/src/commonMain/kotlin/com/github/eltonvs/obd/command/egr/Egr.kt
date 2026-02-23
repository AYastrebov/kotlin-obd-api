package com.github.eltonvs.obd.command.egr

import com.github.eltonvs.obd.command.*

public class CommandedEgrCommand : PercentageObdCommand() {
    override val tag: String = "COMMANDED_EGR"
    override val name: String = "Commanded EGR"
    override val mode: String = "01"
    override val pid: String = "2C"
    override val bytesToProcess: Int = 1
    override val category: CommandCategory = CommandCategory.EMISSION
}

public class EgrErrorCommand : FloatObdCommand() {
    override val tag: String = "EGR_ERROR"
    override val name: String = "EGR Error"
    override val mode: String = "01"
    override val pid: String = "2D"
    override val defaultUnit: String = "%"
    override val bytesToProcess: Int = 1
    override val decimalPlaces: Int = 1
    override val category: CommandCategory = CommandCategory.EMISSION

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<Float> {
        val rawValue = bytesToInt(rawResponse.bufferedValue, bytesToProcess = 1)
        val calculated = rawValue * (100f / 128) - 100
        return TypedValue.FloatValue(
            value = calculated,
            decimalPlaces = decimalPlaces,
            unit = defaultUnit
        )
    }
}
