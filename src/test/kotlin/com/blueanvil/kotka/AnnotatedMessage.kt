package com.blueanvil.kotka

/**
 * @author Cosmin Marginean
 */
@KotkaMessage(topic = "test-annotated-message", threads = 2)
data class AnnotatedMessage(val name: String)
