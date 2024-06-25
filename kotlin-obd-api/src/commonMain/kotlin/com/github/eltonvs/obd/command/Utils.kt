package com.github.eltonvs.obd.command

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.TimeSource

internal fun formatToDecimalPlaces(value: Float, decimalPlaces: Int): String {
    val multiplier = 10.0.pow(decimalPlaces).toFloat()
    val roundedValue = (value * multiplier).roundToInt() / multiplier
    return buildString {
        append(roundedValue)
        val parts = split('.')
        if (parts.size == 1) {
            // No decimal point, add it with the correct number of zeros
            append(".${"0".repeat(decimalPlaces)}")
        } else {
            // Has decimal point, ensure it has the correct number of decimal places
            val decimals = parts[1]
            if (decimals.length < decimalPlaces) {
                append("0".repeat(decimalPlaces - decimals.length))
            }
        }
    }
}

internal fun convertHexToString(hex: String): String {
    val chunks = hex.chunked(2)
    val charArray = chunks.map { it.toInt(16).toChar() }.toCharArray()
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