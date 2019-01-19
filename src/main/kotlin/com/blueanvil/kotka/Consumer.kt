package com.blueanvil.kotka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

/**
 * @author Cosmin Marginean
 */
class Consumer<T : Any>(private val kafkaServers: String,
                        private val topic: String,
                        private val threads: Int,
                        private val messageClass: KClass<T>,
                        private val consumerProps: Properties? = null,
                        private val objectMapper: ObjectMapper,
                        private val pubSub: Boolean = false,
                        private val pollTimeout: Duration = Duration.ofMillis(500)) {

    @Volatile
    private var stopped: Boolean = false

    fun run(messageHandler: (T) -> Unit) {
        val threadCount = AtomicInteger(1)
        val threadPool = Executors.newFixedThreadPool(threads) { runnable ->
            val thread = Thread(Thread.currentThread().threadGroup, runnable, "kotka.$topic.${threadCount.getAndIncrement()}", 0)
            thread.isDaemon = false
            thread
        }

        val futures = ArrayList<Future<*>>()

        repeat(threads) {
            val groupId = if (pubSub) "$topic.${uuid()}" else "$topic.competing-consumer"
            val allProps = allProps(groupId)

            val kafkaConsumer = KafkaConsumer<String, String>(allProps)
            kafkaConsumer.subscribe(listOf(topic))
            val future = threadPool.submit {
                runConsumer(kafkaConsumer, groupId, messageHandler)
            }
            futures.add(future)
        }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                stopped = true
                futures.forEach { it.get() }
            }
        })
    }

    private fun allProps(groupId: String): Properties {
        val allProps = Properties()
        if (consumerProps != null) {
            allProps.putAll(consumerProps)
        }
        allProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServers
        allProps[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        allProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        allProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        allProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        allProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        return allProps
    }

    private fun runConsumer(kafkaConsumer: KafkaConsumer<String, String>, groupId: String, messageHandler: (T) -> Unit) {
        log.info("Running consumer for topic '$topic' and group ID '$groupId'")
        while (!stopped) {
            val records = kafkaConsumer.poll(pollTimeout)
            log.trace("($topic/$groupId) Received ${records.count()} messages after poll")
            processRecords(records, messageHandler)
        }

        kafkaConsumer.unsubscribe()
        kafkaConsumer.close()
        log.info("Stopped consumer for topic $topic and group $groupId")
    }

    private fun processRecords(records: ConsumerRecords<String, String>, messageHandler: (T) -> Unit) {
        for (record in records) {
            if (stopped) {
                break
            }

            val msgStr = record.value()
            try {
                messageHandler(objectMapper.readValue(msgStr, messageClass.javaObjectType))
            } catch (t: Throwable) {
                log.error("(topic) Error handling message : $msgStr", t)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Consumer::class.java)
    }
}