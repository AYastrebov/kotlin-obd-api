package com.github.eltonvs.obd.command


public fun bytesToInt(bufferedValue: IntArray, start: Int = 2, bytesToProcess: Int = -1): Long {
    val end = if (bytesToProcess == -1) bufferedValue.size else minOf(start + bytesToProcess, bufferedValue.size)
    var result = 0L
    for (i in start until end) {
        result = (result shl 8) or bufferedValue[i].toLong()
    }
    return result
}

public fun calculatePercentage(bufferedValue: IntArray, bytesToProcess: Int = -1): Float =
    (bytesToInt(bufferedValue, bytesToProcess = bytesToProcess) * 100f) / 255f

public fun Int.getBitAt(position: Int, last: Int = 32): Int = this shr (last - position) and 1

public fun Long.getBitAt(position: Int, last: Int = 32): Int = (this shr (last - position) and 1).toInt()
