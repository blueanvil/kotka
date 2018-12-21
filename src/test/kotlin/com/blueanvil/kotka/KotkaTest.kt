package com.blueanvil.kotka

import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Cosmin Marginean
 */
class KotkaTest {

    val kotka = Kotka("localhost:59099", 1)

    @Test
    fun simpleSendAndConsumer() {
        val messages = Collections.synchronizedList(ArrayList<SampleMessage>())
        val topic = uuid()
        kotka.consumer(topic, 1, SampleMessage::class) { message ->
            messages.add(message)
        }
        kotka.send(topic, SampleMessage("Jacky Chan", 55, listOf("The Dragon", "Jack E Chan")))
        wait(5, 1000, "Message was not consumed") {
            messages.size == 1 && messages[0].name == "Jacky Chan"
                    && messages[0].age == 55
                    && messages[0].aliases.contains("The Dragon")
                    && messages[0].aliases.contains("Jack E Chan")
        }
    }
}