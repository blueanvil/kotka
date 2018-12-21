package com.blueanvil.kotka

/**
 * @author Cosmin Marginean
 */
@KotkaMessage(topic = "test-annotated-parallel-message", threads = 4)
data class AnnotatedMessageParallel(val name: String)
