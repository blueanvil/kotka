package com.blueanvil.kotka

import java.time.Duration
import java.util.*

/**
 * @author Cosmin Marginean
 */
fun wait(seconds: Long, sleepMs: Long, errorMessage: String, condition: () -> Boolean) {
    var success: Boolean
    var startTime = System.nanoTime()
    while (true) {
        success = condition()
        val elapsedSeconds = Duration.ofNanos(System.nanoTime() - startTime).seconds
        if (success || elapsedSeconds >= seconds) {
            break
        }
        Thread.sleep(sleepMs)
    }
    if (!success) {
        throw RuntimeException(errorMessage)
    }
}

fun uuid(): String {
    return UUID.randomUUID().toString().toLowerCase().replace("-".toRegex(), "")
}
