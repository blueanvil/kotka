package com.blueanvil.kotka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import kotlin.reflect.KClass

/**
 * @author Cosmin Marginean
 */
class Kotka(private val kafkaServers: String,
            private val replicationFactor: Short,
            private val partitionCount: Int = 256,
            private val consumerProps: Properties? = null,
            producerProps: Properties? = null,
            private val pollTimeout: Duration = Duration.ofMillis(500),
            private val objectMapper: ObjectMapper = jacksonObjectMapper()) {

    private val producer = Producer(kafkaServers, objectMapper, producerProps)

    fun <T : Any> send(message: T) {
        val annotation = annotation(message::class)
        producer.send(annotation.topic, message)
    }

    fun <T : Any> consumer(messageClass: KClass<T>, messageHandler: (T) -> Unit) {
        val annotation = annotation(messageClass)
        consumer(annotation.topic, annotation.threads, messageClass, annotation.pubSub, messageHandler)
    }

    private fun <T : Any> annotation(messageClass: KClass<T>): KotkaMessage {
        val annotation = annotation(messageClass, KotkaMessage::class)
                ?: throw IllegalArgumentException("Message class $messageClass::class is not annotated with ${KotkaMessage::class}")
        return annotation
    }

    fun <T : Any> send(topic: String, message: T) {
        producer.send(topic, message)
    }

    fun <T : Any> consumer(topic: String,
                           threads: Int,
                           messageClass: KClass<T>,
                           pubSub: Boolean = false,
                           messageHandler: (T) -> Unit) {
        createTopic(topic)
        Consumer(kafkaServers = kafkaServers,
                topic = topic,
                threads = threads,
                messageClass = messageClass,
                consumerProps = consumerProps,
                objectMapper = objectMapper,
                pubSub = pubSub,
                pollTimeout = pollTimeout).run(messageHandler)
    }

    fun createTopic(topic: String) {
        val properties = Properties()
        properties.setProperty("bootstrap.servers", kafkaServers)
        val client = AdminClient.create(properties)

        if (client.listTopics().names().get().contains(topic)) {
            log.info("Topic $topic already exists. Skipping")
        } else {
            client.createTopics(listOf(NewTopic(topic, partitionCount, replicationFactor))).values()[topic]!!.get()
            wait(10, 500, "Topic $topic was not created") { client.listTopics().names().get().contains(topic) }
            log.info("Created topic $topic")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Kotka::class.java)
    }
}