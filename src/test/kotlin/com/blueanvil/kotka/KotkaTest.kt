package com.blueanvil.kotka

import org.testcontainers.containers.KafkaContainer
import org.testng.Assert.assertTrue
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicLong
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
        val messages = Collections.synchronizedList(ArrayList<TestMessage>())
        val topic = uuid()

        kotka.consumer(topic = topic, threads = 1, messageClass = TestMessage::class) { message ->
            messages.add(message)
        }

        kotka.send(topic = topic, message = TestMessage("Jackie Chan", 55, listOf("The Dragon", "Jack E Chan")))

        wait(15, 500, "Consumer hasn't finished") { messages.size == 1 }
        val first = messages.first()
        assertTrue(
                first.name == "Jackie Chan"
                        && first.age == 55
                        && first.aliases.contains("The Dragon")
                        && first.aliases.contains("Jack E Chan"))
    }

    @Test
    fun parallelism() {
        val threads = Collections.synchronizedList(ArrayList<String>())
        val topic = uuid()

        kotka.consumer(topic = topic, threads = 4, messageClass = TestMessage::class) {
            threads.add(Thread.currentThread().name)
            Thread.sleep(3000)
        }

        repeat(20) { kotka.send(topic = topic, message = TestMessage("Peter Griffin", 55, listOf("The Family Guy"))) }

        wait(60, 500) { threads.size == 20 }
        for (i in 1..4) {
            assertTrue(threads.contains("kotka.$topic.$i"))
        }
    }

    @Test
    fun pubSub() {
        val messages = Collections.synchronizedList(ArrayList<TestMessage>())
        val topic = uuid()

        kotka.consumer(topic = topic, threads = 4, messageClass = TestMessage::class, pubSub = true) { message ->
            messages.add(message)
        }

        kotka.send(topic = topic, message = TestMessage("Peter Griffin", 55, listOf("The Family Guy")))
        wait(15, 500, "Consumer hasn't finished") { messages.size == 4 }
    }

    @Test
    fun multipleConsumersOnSameTopic() {
        val excelCount = AtomicLong(0)
        val pngToPdfCount = AtomicLong(0)
        val topic = "multipleConsumers-${UUID.randomUUID()}"

        kotka.consumer(topic, 2, GenerateExcel::class) { excelCount.incrementAndGet() }
        kotka.consumer(topic, 4, PngToPdf::class) { pngToPdfCount.incrementAndGet() }

        kotka.send(topic, GenerateExcel())
        kotka.send(topic, PngToPdf())
        kotka.send(topic, PngToPdf())
        kotka.send(topic, GenerateExcel())
        kotka.send(topic, PngToPdf())

        wait(10, 100) { excelCount.get() == 2L && pngToPdfCount.get() == 3L }
    }

}