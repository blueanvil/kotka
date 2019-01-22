package com.blueanvil.kotka

import org.junit.Assert
import org.junit.Test
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Cosmin Marginean
 */
class KotkaTest {

    val kotka = Kotka(kafkaServers = "localhost:59099",
            config = KotkaConfig(
                    partitionCount = 64,
                    replicationFactor = 1,
                    consumerProps = mapOf("max.poll.records" to "1").toProperties(),
                    producerProps = mapOf("batch.size" to "1").toProperties(),
                    pollTimeout = Duration.ofMillis(100)))


    @Test
    fun simpleSendAndConsumer() {
        val messages = Collections.synchronizedList(ArrayList<Message>())
        val topic = uuid()

        kotka.consumer(topic = topic, threads = 1, messageClass = Message::class) { message ->
            messages.add(message)
        }

        kotka.send(topic = topic, message = Message("Jackie Chan", 55, listOf("The Dragon", "Jack E Chan")))

        wait(15, 500, "Consumer hasn't finished") { messages.size == 1 }
        val first = messages.first()
        Assert.assertTrue(
                first.name == "Jackie Chan"
                        && first.age == 55
                        && first.aliases.contains("The Dragon")
                        && first.aliases.contains("Jack E Chan"))
    }

    @Test
    fun annotatedMessage() {
        val messages = Collections.synchronizedList(ArrayList<AnnotatedMessage>())

        kotka.consumer(AnnotatedMessage::class) { message ->
            messages.add(message)
        }

        kotka.send(AnnotatedMessage("Dolph Lundgren"))

        wait(15, 500, "Consumer hasn't finished") { messages.size == 1 }
        Assert.assertTrue(messages[0].name == "Dolph Lundgren")
    }

    @Test(expected = IllegalArgumentException::class)
    fun expectConsumerError() {
        kotka.consumer(Message::class) {
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun expectProducerError() {
        kotka.send(Message("Dolph Lundgren", 1, listOf("none")))
    }

    @Test
    fun parallelism() {
        val threads = Collections.synchronizedList(ArrayList<String>())
        val topic = uuid()

        kotka.consumer(topic = topic, threads = 4, messageClass = Message::class) {
            threads.add(Thread.currentThread().name)
            Thread.sleep(500)
        }

        repeat(20) { kotka.send(topic = topic, message = Message("Peter Griffin", 55, listOf("The Family Guy"))) }

        wait(15, 500, "Consumer hasn't finished") { threads.size == 20 }
        for (i in 1..4) {
            Assert.assertTrue(threads.contains("kotka.$topic.$i"))
        }
    }

    @Test
    fun annotatedParallelism() {
        val threads = Collections.synchronizedList(ArrayList<String>())

        kotka.consumer(AnnotatedMessageParallel::class) {
            threads.add(Thread.currentThread().name)
            Thread.sleep(500)
        }

        repeat(20) { kotka.send(AnnotatedMessageParallel("Change the conversation")) }

        wait(15, 500, "Consumer hasn't finished") { threads.size == 20 }
        for (i in 1..4) {
            Assert.assertTrue(threads.contains("kotka.test-annotated-parallel-message.$i"))
        }
    }

    @Test
    fun pubSub() {
        val messages = Collections.synchronizedList(ArrayList<Message>())
        val topic = uuid()

        kotka.consumer(topic = topic, threads = 4, messageClass = Message::class, pubSub = true) { message ->
            messages.add(message)
        }

        kotka.send(topic = topic, message = Message("Peter Griffin", 55, listOf("The Family Guy")))
        wait(15, 500, "Consumer hasn't finished") { messages.size == 4 }
    }

    @Test
    fun annotatedPubSub() {
        val messages = Collections.synchronizedList(ArrayList<AnnotatedMessagePubSub>())

        kotka.consumer(AnnotatedMessagePubSub::class) { message ->
            messages.add(message)
        }

        kotka.send(AnnotatedMessagePubSub("Sunshine"))
        wait(15, 500, "Consumer hasn't finished") { messages.size == 4 }
    }
}