package com.blueanvil.kotka

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * @author Cosmin Marginean
 */
class Kotka(private val kafkaServers: String,
            private val config: KotkaConfig) {

    private val producer = Producer(kafkaServers, config.objectMapper, config.producerProps)
    private val consumers = ConcurrentHashMap<String, Consumer>()

    fun <T : Any> send(topic: String, message: T) {
        producer.send(topic, message)
    }

    fun <T : Any> consumer(topic: String,
                           threads: Int,
                           messageClass: KClass<T>,
                           pubSub: Boolean = false,
                           messageHandler: (T) -> Unit) {
        createTopic(topic)
        val consumer = consumers.getOrPut(topic) {
            Consumer(kafkaServers = kafkaServers,
                    topic = topic,
                    threads = threads,
                    pubSub = pubSub,
                    config = config)
        }
        consumer.addHandler(messageClass, messageHandler)
    }

    fun createTopic(topic: String) {
        val properties = Properties()
        properties.setProperty("bootstrap.servers", kafkaServers)
        val client = AdminClient.create(properties)

        if (client.listTopics().names().get().contains(topic)) {
            log.info("Topic $topic already exists. Skipping")
        } else {
            client.createTopics(listOf(NewTopic(topic, config.partitionCount, config.replicationFactor))).values()[topic]!!.get()
            wait(10, 500, "Topic $topic was not created") { client.listTopics().names().get().contains(topic) }
            log.info("Created topic $topic")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Kotka::class.java)
    }
}