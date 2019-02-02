package com.blueanvil.kotka

import java.time.Duration
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

/**
 * @author Cosmin Marginean
 */
internal fun wait(seconds: Long, sleepMs: Long, errorMessage: String, condition: () -> Boolean) {
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

internal fun uuid(): String {
    return UUID.randomUUID().toString().toLowerCase().replace("-".toRegex(), "")
}

internal fun <T : Annotation> annotation(cls: KClass<*>, annotationClass: KClass<T>): T? {
    val annotation = cls.annotations.find { it.annotationClass == annotationClass }
    if (annotation != null) {
        return annotation as T
    }

    cls.allSuperclasses.forEach {
        val parentAnnotation = it.annotations.find { a -> a.annotationClass == annotationClass }
        if (parentAnnotation != null) {
            return parentAnnotation as T
        }
    }
    return null
}

internal fun String.elipsis(maxLength: Int): String {
    return if (maxLength > 0 && length > maxLength) {
        "${substring(0, maxLength - 3)}..."
    } else this
}