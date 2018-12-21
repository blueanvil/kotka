package com.blueanvil.kotka

/**
 * @author Cosmin Marginean
 */
@KotkaMessage(topic = "test-annotated-pubsub-message", threads = 4, pubSub = true)
data class AnnotatedMessagePubSub(val name: String)
