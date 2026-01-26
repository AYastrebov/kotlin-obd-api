package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.*
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.measureTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.InternalIoApi
import kotlinx.io.Source


public class ObdDeviceConnection(
    private val inputStream: Source,
    private val outputStream: Source,
    private val maxCacheSize: Int = 100
) : AutoCloseable {
    private val responseCache = mutableMapOf<String, ObdRawResponse>()
    private val cacheAccessOrder = mutableListOf<String>()
    private val cacheMutex = Mutex()

    /**
     * Closes the connection and clears the response cache.
     *
     * Exceptions from closing streams are intentionally swallowed as streams
     * may already be closed or in an error state. This ensures cleanup proceeds
     * even if one stream fails to close.
     */
    override fun close() {
        responseCache.clear()
        cacheAccessOrder.clear()
        runCatching { inputStream.close() }
        runCatching { outputStream.close() }
    }

    private suspend fun putInCache(key: String, value: ObdRawResponse) {
        cacheMutex.withLock {
            // Remove from current position if exists
            cacheAccessOrder.remove(key)
            // Add to end (most recently used)
            cacheAccessOrder.add(key)
            responseCache[key] = value
            // Evict oldest if over capacity
            while (cacheAccessOrder.size > maxCacheSize) {
                val oldest = cacheAccessOrder.removeFirst()
                responseCache.remove(oldest)
            }
        }
    }

    private suspend fun getFromCache(key: String): ObdRawResponse? {
        return cacheMutex.withLock {
            val value = responseCache[key] ?: return@withLock null
            // Move to end (most recently used)
            cacheAccessOrder.remove(key)
            cacheAccessOrder.add(key)
            value
        }
    }

    public suspend fun run(
        command: ObdCommand,
        useCache: Boolean = false,
        delayTime: Long = 0,
        maxRetries: Int = 5,
        retryDelayMs: Long = 500,
    ): ObdResponse {
        val cacheKey = command.rawCommand
        val obdRawResponse =
            if (useCache) {
                getFromCache(cacheKey) ?: runCommand(command, delayTime, maxRetries, retryDelayMs).also {
                    putInCache(cacheKey, it)
                }
            } else {
                runCommand(command, delayTime, maxRetries, retryDelayMs)
            }

        return command.handleResponse(obdRawResponse)
    }

    private suspend fun runCommand(
        command: ObdCommand,
        delayTime: Long,
        maxRetries: Int,
        retryDelayMs: Long
    ): ObdRawResponse {
        val (rawData, elapsedTime) = measureTime {
            sendCommand(command, delayTime)
            readRawData(maxRetries, retryDelayMs)
        }
        return ObdRawResponse(rawData, elapsedTime)
    }

    // Note: Dispatchers.Default is used instead of Dispatchers.IO for Kotlin Multiplatform
    // compatibility. kotlinx-io buffers are non-blocking and work efficiently on Default.
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
    private suspend fun readRawData(maxRetries: Int, retryDelayMs: Long): String {
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
                    delay(retryDelayMs)
                }
            }

            removeAll(SEARCHING_PATTERN, res.toString()).trim()
        }
    }
}
