package com.blueanvil.kotka

import org.testcontainers.containers.KafkaContainer
import org.testng.Assert.assertTrue
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Cosmin Marginean
 */
class KotkaTest {

    companion object {
        var kafkaContainer: KafkaContainer = KafkaContainer()
        lateinit var kotka: Kotka
    }

    @BeforeSuite
    fun beforeTests() {
        kafkaContainer.start()
        kotka = Kotka(kafkaServers = kafkaContainer.bootstrapServers,
                config = KotkaConfig(
                        partitionCount = 64,
                        replicationFactor = 1,
                        consumerProps = mapOf("max.poll.records" to "1").toProperties(),
                        producerProps = mapOf("batch.size" to "1").toProperties(),
                        pollTimeout = Duration.ofMillis(100)))
    }

    @AfterSuite
    fun afterTests() {
        kafkaContainer.close()
    }

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
        assertTrue(
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
        assertTrue(messages[0].name == "Dolph Lundgren")
    }

    @Test(expectedExceptions = [IllegalArgumentException::class])
    fun expectConsumerError() {
        kotka.consumer(Message::class) {
        }
    }

    @Test(expectedExceptions = [IllegalArgumentException::class])
    fun expectProducerError() {
        kotka.send(Message("Dolph Lundgren", 1, listOf("none")))
    }

    @Test
    fun parallelism() {
        val threads = Collections.synchronizedList(ArrayList<String>())
        val topic = uuid()

        kotka.consumer(topic = topic, threads = 4, messageClass = Message::class) {
            threads.add(Thread.currentThread().name)
            Thread.sleep(3000)
        }

        repeat(20) { kotka.send(topic = topic, message = Message("Peter Griffin", 55, listOf("The Family Guy"))) }

        wait(60, 500, "Consumer hasn't finished") { threads.size == 20 }
        for (i in 1..4) {
            assertTrue(threads.contains("kotka.$topic.$i"))
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
            assertTrue(threads.contains("kotka.test-annotated-parallel-message.$i"))
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