package com.blueanvil.kotka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

/**
 * @author Cosmin Marginean
 */
class Producer<T : Any>(kafkaServers: String,
                        val messageSerializer: (T) -> String = ToJson()) {

    val producer: KafkaProducer<String, String>

    init {
        val prodProps = Properties()
        prodProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers)
        prodProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        prodProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        producer = KafkaProducer(prodProps)
    }

    fun send(topic: String, message: T) {
        val msgString = messageSerializer(message)
        producer.send(ProducerRecord<String, String>(topic, uuid(), msgString))
    }
}
