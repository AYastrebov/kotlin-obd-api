package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.*
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.measureTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.io.InternalIoApi
import kotlinx.io.Source


class ObdDeviceConnection(
    private val inputStream: Source,
    private val outputStream: Source
) {
    private val responseCache = mutableMapOf<ObdCommand, ObdRawResponse>()

    suspend fun run(
        command: ObdCommand,
        useCache: Boolean = false,
        delayTime: Long = 0,
        maxRetries: Int = 5,
    ): ObdResponse {
        val obdRawResponse =
            if (useCache && responseCache[command] != null) {
                responseCache.getValue(command)
            } else {
                runCommand(command, delayTime, maxRetries).also {
                    // Save response to cache
                    if (useCache) {
                        responseCache[command] = it
                    }
                }
            }

        return command.handleResponse(obdRawResponse)
    }

    private suspend fun runCommand(command: ObdCommand, delayTime: Long, maxRetries: Int): ObdRawResponse {
        val (rawData, elapsedTime) = measureTime {
            sendCommand(command, delayTime)
            readRawData(maxRetries)
        }
        return ObdRawResponse(rawData, elapsedTime)
    }

    @OptIn(InternalIoApi::class)
    private suspend fun sendCommand(command: ObdCommand, delayTime: Long) {
        withContext(Dispatchers.Default) {
            outputStream.buffer.write("${command.rawCommand}\r".encodeToByteArray())
            outputStream.buffer.flush()
            if (delayTime > 0) {
                delay(delayTime)
            }
        }
    }

    @OptIn(InternalIoApi::class)
    private suspend fun readRawData(maxRetries: Int): String {
        var b: Byte
        var c: Char
        val res = StringBuilder()
        var retriesCount = 0

        return withContext(Dispatchers.Default) {
            // read until '>' arrives OR end of stream reached (-1)
            while (retriesCount <= maxRetries) {
                if (inputStream.buffer.size > 0) {
                    b = inputStream.readByte()
                    if (b < 0) {
                        break
                    }
                    c = b.toInt().toChar()
                    if (c == '>') {
                        break
                    }
                    res.append(c)
                } else {
                    retriesCount += 1
                    delay(500)
                }
            }

            removeAll(SEARCHING_PATTERN, res.toString()).trim()
        }
    }
}