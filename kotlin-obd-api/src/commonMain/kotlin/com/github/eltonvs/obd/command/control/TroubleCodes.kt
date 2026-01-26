package com.github.eltonvs.obd.command.control

import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.RegexPatterns.CARRIAGE_COLON_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.CARRIAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN
import com.github.eltonvs.obd.command.bytesToInt
import com.github.eltonvs.obd.command.removeAll


public class DTCNumberCommand : ObdCommand() {
    override val tag: String = "DTC_NUMBER"
    override val name: String = "Diagnostic Trouble Codes Number"
    override val mode: String = "01"
    override val pid: String = "01"

    override val defaultUnit: String = " codes"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        val codeCount = if (it.bufferedValue.size > 2) {
            it.bufferedValue[2] and 0x7F
        } else {
            0
        }
        codeCount.toString()
    }
}

public class DistanceSinceCodesClearedCommand : ObdCommand() {
    override val tag: String = "DISTANCE_TRAVELED_AFTER_CODES_CLEARED"
    override val name: String = "Distance traveled since codes cleared"
    override val mode: String = "01"
    override val pid: String = "31"

    override val defaultUnit: String = "Km"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> bytesToInt(it.bufferedValue).toString() }
}

public class TimeSinceCodesClearedCommand : ObdCommand() {
    override val tag: String = "TIME_SINCE_CODES_CLEARED"
    override val name: String = "Time since codes cleared"
    override val mode: String = "01"
    override val pid: String = "4E"

    override val defaultUnit: String = "min"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> bytesToInt(it.bufferedValue).toString() }
}

public class ResetTroubleCodesCommand : ObdCommand() {
    override val tag: String = "RESET_TROUBLE_CODES"
    override val name: String = "Reset Trouble Codes"
    override val mode: String = "04"
    override val pid: String = ""
}

public abstract class BaseTroubleCodesCommand : ObdCommand() {
    override val pid: String = ""

    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse -> parseTroubleCodesList(it.value).joinToString(separator = ",") }

    public abstract val carriageNumberPattern: Regex

    public var troubleCodesList: List<String> = listOf()
        private set

    private fun parseTroubleCodesList(rawValue: String): List<String> {
        val canOneFrame: String = removeAll(rawValue, CARRIAGE_PATTERN, WHITESPACE_PATTERN)
        val canOneFrameLength: Int = canOneFrame.length

        val workingData =
            when {
                /* CAN(ISO-15765) protocol one frame: 43yy[codes]
                   Header is 43yy, yy showing the number of data items. */
                (canOneFrameLength <= 16) and (canOneFrameLength % 4 == 0) -> canOneFrame.drop(4)
                /* CAN(ISO-15765) protocol two and more frames: xxx43yy[codes]
                   Header is xxx43yy, xxx is bytes of information to follow, yy showing the number of data items. */
                rawValue.contains(":") -> removeAll(CARRIAGE_COLON_PATTERN, rawValue).drop(7)
                // ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
                else -> removeAll(rawValue, carriageNumberPattern, WHITESPACE_PATTERN)
            }

        /* For each chunk of 4 chars:
           it:  "0100"
           HEX: 0   1    0   0
           BIN: 00000001 00000000
                [][][    hex    ]
                | / /
           DTC: P0100 */
        val troubleCodesList = workingData.chunked(4) {
            val b1 = it.first().toString().toInt(radix = 16)
            val ch1 = (b1 shr 2) and 0b11
            val ch2 = b1 and 0b11
            "${DTC_LETTERS[ch1]}${HEX_ARRAY[ch2]}${it.drop(1)}".padEnd(5, '0')
        }

        val idx = troubleCodesList.indexOf("P0000")
        return (if (idx < 0) troubleCodesList else troubleCodesList.take(idx)).also {
            this.troubleCodesList = it
        }
    }

    protected companion object {
        private val DTC_LETTERS: CharArray = charArrayOf('P', 'C', 'B', 'U')
        private val HEX_ARRAY: CharArray = "0123456789ABCDEF".toCharArray()
    }
}

public class TroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag: String = "TROUBLE_CODES"
    override val name: String = "Trouble Codes"
    override val mode: String = "03"

    override val carriageNumberPattern: Regex = "^43|[\r\n]43|[\r\n]".toRegex()
}

public class PendingTroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag: String = "PENDING_TROUBLE_CODES"
    override val name: String = "Pending Trouble Codes"
    override val mode: String = "07"

    override val carriageNumberPattern: Regex = "^47|[\r\n]47|[\r\n]".toRegex()
}

public class PermanentTroubleCodesCommand : BaseTroubleCodesCommand() {
    override val tag: String = "PERMANENT_TROUBLE_CODES"
    override val name: String = "Permanent Trouble Codes"
    override val mode: String = "0A"

    override val carriageNumberPattern: Regex = "^4A|[\r\n]4A|[\r\n]".toRegex()
}
