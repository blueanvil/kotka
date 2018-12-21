package com.blueanvil.kotka

import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Cosmin Marginean
 */
class KotkaTest {

    val kotka = Kotka(kafkaServers = "localhost:59099", replicationFactor = 1)

    @Test
    fun simpleSendAndConsumer() {
        val messages = Collections.synchronizedList(ArrayList<Message>())
        val topic = uuid()

        kotka.consumer(topic = topic, threads = 1, messageClass = Message::class) { message ->
            messages.add(message)
        }

        kotka.send(topic = topic, message = Message("Jacky Chan", 55, listOf("The Dragon", "Jack E Chan")))

        wait(5, 500, "Message was not consumed") {
            messages.size == 1 && messages[0].name == "Jacky Chan"
                    && messages[0].age == 55
                    && messages[0].aliases.contains("The Dragon")
                    && messages[0].aliases.contains("Jack E Chan")
        }
    }

    @Test
    fun annotatedMessage() {
        val messages = Collections.synchronizedList(ArrayList<AnnotatedMessage>())

        kotka.consumer(AnnotatedMessage::class) { message ->
            messages.add(message)
        }

        kotka.send(AnnotatedMessage("Dolph Lundgren"))

        wait(5, 500, "Message was not consumed") {
            messages.size == 1 && messages[0].name == "Dolph Lundgren"
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun expectConsumerError() {
        kotka.consumer(Message::class) { message ->
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun expectProducerError() {
        kotka.send(Message("Dolph Lundgren", 1, listOf("none")))
    }
}