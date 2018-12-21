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
            if (messages.size == 0) {
                false
            } else {
                val first = messages[0]
                first.name == "Jacky Chan"
                        && first.age == 55
                        && first.aliases.contains("The Dragon")
                        && first.aliases.contains("Jack E Chan")
            }
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