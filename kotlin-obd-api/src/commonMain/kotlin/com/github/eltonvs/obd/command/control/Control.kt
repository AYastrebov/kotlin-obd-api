package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.*
import com.github.eltonvs.obd.command.RegexPatterns.BUS_INIT_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.STARTS_WITH_ALPHANUM_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN
import com.github.eltonvs.obd.command.convertHexToString


public class ModuleVoltageCommand : FloatObdCommand() {
    override val tag: String = "CONTROL_MODULE_VOLTAGE"
    override val name: String = "Control Module Power Supply"
    override val mode: String = "01"
    override val pid: String = "42"
    override val defaultUnit: String = "V"
    override val multiplier: Float = 0.001f
    override val decimalPlaces: Int = 2
    override val category: CommandCategory = CommandCategory.CONTROL
}

public class TimingAdvanceCommand : FloatObdCommand() {
    override val tag: String = "TIMING_ADVANCE"
    override val name: String = "Timing Advance"
    override val mode: String = "01"
    override val pid: String = "0E"
    override val defaultUnit: String = "°"
    override val bytesToProcess: Int = 1
    override val multiplier: Float = 0.5f
    override val offset: Float = -64f
    override val decimalPlaces: Int = 2
    override val category: CommandCategory = CommandCategory.CONTROL
}

public class VINCommand : ObdCommand() {
    override val tag: String = "VIN"
    override val name: String = "Vehicle Identification Number (VIN)"
    override val mode: String = "09"
    override val pid: String = "02"
    override val defaultUnit: String = ""
    override val category: CommandCategory = CommandCategory.DIAGNOSTIC

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<String> {
        val vin = parseVIN(removeAll(rawResponse.value, WHITESPACE_PATTERN, BUS_INIT_PATTERN))
        return TypedValue.StringValue(vin)
    }

    private fun parseVIN(rawValue: String): String {
        val workingData =
            if (rawValue.contains(":")) {
                // CAN(ISO-15765) protocol.
                val value = rawValue.replace(".:".toRegex(), "").substring(9)
                if (STARTS_WITH_ALPHANUM_PATTERN.matches(convertHexToString(value))) {
                    rawValue.replace("0:49", "").replace(".:".toRegex(), "")
                } else {
                    value
                }
            } else {
                // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
                rawValue.replace("49020.".toRegex(), "")
            }

        return convertHexToString(workingData).replace("[\u0000-\u001f]".toRegex(), "")
    }
}
