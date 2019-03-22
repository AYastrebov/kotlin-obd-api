package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import java.util.regex.Pattern


private fun calculate(rawValue: String): Int {
    val a = rawValue[2].toInt()
    val b = rawValue[3].toInt()
    return a * 256 + b
}

class ModuleVoltageCommand : ObdCommand() {
    override val tag = "CONTROL_MODULE_VOLTAGE"
    override val name = "Control Module Power Supply"
    override val mode = "01"
    override val pid = "42"

    override val defaultUnit = "V"
    override val handler = { x: String -> "%.2f".format(calculate(x) / 1000f) }
}

class TimingAdvanceCommand : ObdCommand() {
    override val tag = "TIMING_ADVANCE"
    override val name = "Timing Advance"
    override val mode = "01"
    override val pid = "0E"

    override val defaultUnit = "°"
    override val handler = { x: String -> "%.2f".format(x[2].toInt() / 2f - 64) }
}

class VINCommand : ObdCommand() {
    override val tag = "VIN"
    override val name = "Vehicle Identification Number (VIN)"
    override val mode = "09"
    override val pid = "02"

    override val defaultUnit = ""
    override val handler = ::parseVIN

    private fun parseVIN(rawValue: String): String {
        val workingData =
            if (rawValue.contains(":")) {
                // CAN(ISO-15765) protocol.
                // 9 is xxx490201, xxx is bytes of information to follow.
                val value = rawValue.replace(".:", "").substring(9)
                if (STARTS_WITH_ALPHANUM_PATTERN.matcher(convertHexToString(value)).find()) {
                    rawValue.replace("0:49", "").replace(".:", "")
                } else {
                    value
                }
            } else {
                // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
                rawValue.replace("49020.", "")
            }
        return convertHexToString(workingData).replace("[\u0000-\u001f]", "")
    }

    private fun convertHexToString(hex: String): String =
        hex.chunked(2).map { Integer.parseInt(it, 16).toChar() }.joinToString("")

    companion object {
        private val STARTS_WITH_ALPHANUM_PATTERN = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
    }
}