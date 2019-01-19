package com.blueanvil.kotka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

/**
 * @author Cosmin Marginean
 */
class Producer(kafkaServers: String,
               private val objectMapper: ObjectMapper = jacksonObjectMapper(),
               producerProps: Properties? = null) {

    val producer: KafkaProducer<String, String>

    init {
        val allProps = Properties()

        if (producerProps != null) {
            allProps.putAll(producerProps)
        }

        allProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers)
        allProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        allProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        producer = KafkaProducer(allProps)
    }

    fun send(topic: String, message: Any) {
        val msgString = objectMapper.writeValueAsString(message)
        producer.send(ProducerRecord<String, String>(topic, uuid(), msgString))
    }
}
