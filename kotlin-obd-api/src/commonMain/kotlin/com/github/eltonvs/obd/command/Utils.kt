package com.github.eltonvs.obd.command

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.TimeSource

internal fun formatToDecimalPlaces(value: Float, decimalPlaces: Int): String {
    if (decimalPlaces == 0) {
        return value.roundToInt().toString()
    }
    val multiplier = 10.0.pow(decimalPlaces).toFloat()
    val rounded = (value * multiplier).roundToInt() / multiplier
    val str = rounded.toString()
    val parts = str.split('.')
    return if (parts.size == 1) {
        "$str.${"0".repeat(decimalPlaces)}"
    } else {
        val decimals = parts[1]
        if (decimals.length < decimalPlaces) {
            "$str${"0".repeat(decimalPlaces - decimals.length)}"
        } else {
            str
        }
    }
}

internal fun convertHexToString(hex: String): String {
    val chunks = hex.chunked(2)
    val charArray = chunks.mapNotNull { chunk ->
        runCatching { chunk.toInt(16).toChar() }.getOrNull()
    }.toCharArray()
    return charArray.concatToString()
}

internal fun Int.formatToHex(): String {
    return toString(16).padStart(2, '0').uppercase()
}

internal inline fun <T> measureTime(block: () -> T): Pair<T, Long> {
    val start = TimeSource.Monotonic.markNow()
    val result = block()
    val elapsed = start.elapsedNow().inWholeMilliseconds
    return result to elapsed
}