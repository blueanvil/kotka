package com.blueanvil.kotka

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KClass

/**
 * @author Cosmin Marginean
 */
class Kotka(private val kafkaServers: String,
            private val replicationFactor: Short,
            private val partitionCount: Int = 256,
            messageSerializer: (Any) -> String = ToJson()) {

    private val producer = Producer(kafkaServers, messageSerializer)

    fun <T : Any> send(topic: String, message: T) {
        producer.send(topic, message)
    }

    fun <T : Any> consumer(topic: String,
                           threads: Int,
                           messageClass: KClass<T>,
                           messageHandler: (T) -> Unit) {
        createTopic(topic)
        Consumer(kafkaServers, topic, threads, messageClass).run(messageHandler)
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