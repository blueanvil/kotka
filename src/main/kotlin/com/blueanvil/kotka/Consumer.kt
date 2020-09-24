package com.blueanvil.kotka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

/**
 * @author Cosmin Marginean
 */
class Consumer(private val kafkaServers: String,
               private val topic: String,
               private val threads: Int,
               private val pubSub: Boolean = false,
               private val config: KotkaConfig) {

    private val handlers = ConcurrentHashMap<String, MessageHandler<*>>()

    @Volatile
    private var stopped: Boolean = false

    init {
        val threadCount = AtomicInteger(1)
        val threadPool = Executors.newFixedThreadPool(threads) { runnable ->
            val thread = Thread(Thread.currentThread().threadGroup, runnable, "kotka.$topic.${threadCount.getAndIncrement()}", 0)
            thread.isDaemon = true
            thread
        }

        val futures = ArrayList<Future<*>>()

        repeat(threads) {
            val groupId = if (pubSub) "$topic.${uuid()}" else "$topic.competing-consumer"
            val allProps = allProps(groupId)

            val kafkaConsumer = KafkaConsumer<String, String>(allProps)
            kafkaConsumer.subscribe(listOf(topic))
            val future = threadPool.submit {
                runConsumer(kafkaConsumer, groupId)
            }
            futures.add(future)
        }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                stopped = true
                threadPool.shutdown()
                threadPool.awaitTermination(30, TimeUnit.SECONDS)
            }
        })
    }

    fun <T : Any> addHandler(messageClass: KClass<T>, handlerFunction: (T) -> Unit) {
        handlers[messageClass.qualifiedName!!] = MessageHandler(messageClass, handlerFunction)
    }

    private fun allProps(groupId: String): Properties {
        val allProps = Properties()
        if (config.consumerProps != null) {
            allProps.putAll(config.consumerProps)
        }
        allProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServers
        allProps[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        allProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        allProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        allProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        allProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        return allProps
    }

    private fun runConsumer(kafkaConsumer: KafkaConsumer<String, String>, groupId: String) {
        log.info("Running consumer topic=$topic, threads=$threads, pubSub=$pubSub, groupId=$groupId")
        while (!stopped) {
            val records = kafkaConsumer.poll(config.pollTimeout)
            log.trace("($topic/$groupId) Received ${records.count()} messages after poll")
            processRecords(records)
        }

        kafkaConsumer.unsubscribe()
        kafkaConsumer.close()
        log.info("Stopped consumer for topic $topic and group $groupId")
    }

    private fun processRecords(records: ConsumerRecords<String, String>) {
        for (record in records) {
            if (stopped) {
                break
            }

            val msgStr = record.value()
            try {
                val firstPipe = msgStr.indexOf('|')
                val handlerName = msgStr.substring(0, firstPipe)
                val handlerInfo = handlers[handlerName]!!
                handlerInfo.invoke(config, msgStr.substring(firstPipe + 1))
            } catch (t: Throwable) {
                config.logging.error(log, msgStr, t) { "($topic) Error handling message: $it" }
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Consumer::class.java)
    }
}

data class MessageHandler<T : Any>(val messageClass: KClass<T>,
                                   val handlerFunction: (T) -> Unit) {

    fun invoke(config: KotkaConfig, messageStr: String) {
        val message = config.objectMapper.readValue(messageStr, messageClass.javaObjectType)
        handlerFunction(message)
    }
}