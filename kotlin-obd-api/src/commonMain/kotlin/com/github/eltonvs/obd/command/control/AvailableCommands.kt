package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.*


public class AvailablePIDsCommand(private val range: AvailablePIDsRanges) : ObdCommand() {
    override val tag: String = "AVAILABLE_COMMANDS_${range.name}"
    override val name: String = "Available Commands - ${range.displayName}"
    override val mode: String = "01"
    override val pid: String = range.pid
    override val defaultUnit: String = ""
    override val category: CommandCategory = CommandCategory.CONTROL

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<List<Int>> {
        val pids = parsePIDs(rawResponse.processedValue)
        return TypedValue.ListValue(
            value = pids.toList(),
            stringValue = pids.joinToString(",") { it.formatToHex() }
        )
    }

    private fun parsePIDs(rawValue: String): IntArray {
        val value = rawValue.toLong(radix = 16)
        val initialPID = range.pid.toInt(radix = 16)
        return (1..33).fold(intArrayOf()) { acc, i ->
            if (value.getBitAt(i) == 1) acc.plus(i + initialPID) else acc
        }
    }

    public enum class AvailablePIDsRanges(public val displayName: String, internal val pid: String) {
        PIDS_01_TO_20("PIDs from 01 to 20", "00"),
        PIDS_21_TO_40("PIDs from 21 to 40", "20"),
        PIDS_41_TO_60("PIDs from 41 to 60", "40"),
        PIDS_61_TO_80("PIDs from 61 to 80", "60"),
        PIDS_81_TO_A0("PIDs from 81 to A0", "80")
    }
}
