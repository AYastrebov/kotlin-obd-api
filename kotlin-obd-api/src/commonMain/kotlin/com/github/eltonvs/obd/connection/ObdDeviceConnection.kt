package com.github.eltonvs.obd.connection

import com.github.eltonvs.obd.command.*
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.measureTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlin.time.TimeMark
import kotlin.time.TimeSource


public class ObdDeviceConnection(
    private val inputStream: Source,
    private val outputStream: Sink,
    private val maxCacheSize: Int = 100,
    private val cacheTtlMs: Long = 0
) : AutoCloseable {
    private val responseCache = mutableMapOf<String, CacheEntry>()
    private val cacheAccessOrder = mutableListOf<String>()
    private val cacheMutex = Mutex()

    private class CacheEntry(val response: ObdRawResponse, val timestamp: TimeMark)

    /**
     * Closes the connection and clears the response cache.
     *
     * Exceptions from closing streams are intentionally swallowed as streams
     * may already be closed or in an error state. This ensures cleanup proceeds
     * even if one stream fails to close.
     */
    override fun close() {
        // Use tryLock for multiplatform compatibility (runBlocking unavailable on JS/Wasm).
        // If the lock is held by a concurrent run(), the cache will be cleared by the GC
        // after the connection is no longer referenced.
        if (cacheMutex.tryLock()) {
            try {
                responseCache.clear()
                cacheAccessOrder.clear()
            } finally {
                cacheMutex.unlock()
            }
        }
        runCatching { inputStream.close() }
        runCatching { outputStream.close() }
    }

    private suspend fun putInCache(key: String, value: ObdRawResponse) {
        cacheMutex.withLock {
            // Remove from current position if exists
            cacheAccessOrder.remove(key)
            // Add to end (most recently used)
            cacheAccessOrder.add(key)
            responseCache[key] = CacheEntry(value, TimeSource.Monotonic.markNow())
            // Evict oldest if over capacity
            while (cacheAccessOrder.size > maxCacheSize) {
                val oldest = cacheAccessOrder.removeFirst()
                responseCache.remove(oldest)
            }
        }
    }

    private suspend fun getFromCache(key: String): ObdRawResponse? {
        return cacheMutex.withLock {
            val entry = responseCache[key] ?: return@withLock null
            if (cacheTtlMs > 0 && entry.timestamp.elapsedNow().inWholeMilliseconds >= cacheTtlMs) {
                responseCache.remove(key)
                cacheAccessOrder.remove(key)
                return@withLock null
            }
            // Move to end (most recently used)
            cacheAccessOrder.remove(key)
            cacheAccessOrder.add(key)
            entry.response
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
    private suspend fun sendCommand(command: ObdCommand, delayTime: Long) {
        withContext(Dispatchers.Default) {
            outputStream.write("${command.rawCommand}\r".encodeToByteArray())
            outputStream.flush()
            if (delayTime > 0) {
                delay(delayTime)
            }
        }
    }

    private suspend fun readRawData(maxRetries: Int, retryDelayMs: Long): String {
        val res = StringBuilder()
        var retriesCount = 0

        return withContext(Dispatchers.Default) {
            // read until '>' arrives or retries exhausted
            while (retriesCount <= maxRetries) {
                if (inputStream.request(1)) {
                    val c = inputStream.readByte().toInt().toChar()
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
