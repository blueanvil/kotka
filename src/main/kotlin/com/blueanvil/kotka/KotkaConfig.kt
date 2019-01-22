package com.blueanvil.kotka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import java.time.Duration
import java.util.*

/**
 * @author Cosmin Marginean
 */
data class KotkaConfig(val replicationFactor: Short,
                       val partitionCount: Int = 256,
                       val consumerProps: Properties? = null,
                       val producerProps: Properties? = null,
                       val pollTimeout: Duration = Duration.ofMillis(500),
                       val objectMapper: ObjectMapper = jacksonObjectMapper(),
                       val logging: Logging = Logging()) {

    data class Logging(val logMessageOnError: Boolean = true,
                       val errorMessageLogEllipsis: Int = 2048) {

        fun error(log: Logger, kafkaMessage: String, t: Throwable, messageText: (String) -> String) {
            if (logMessageOnError) {
                log.error(messageText(kafkaMessage.elipsis(errorMessageLogEllipsis)), t)
            }
        }
    }
}