package com.blueanvil.kotka

/**
 * @author Cosmin Marginean
 */
@KotkaMessage(topic = "test-annotated-message", threads = 1)
data class AnnotatedMessage(val name: String)
