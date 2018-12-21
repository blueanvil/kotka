# kotka
A simple and thin Kotlin wrapper for Kafka

## Gradle dependency

```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.blueanvil:kotka:1.0.0'
}
```

## Standard flow
```kotlin
val kotka = Kotka(kafkaServers = "localhost:59099", replicationFactor = 1)


// Register a consumer
kotka.consumer(topic = "topic1", threads = 8, messageClass = Message::class) { message ->
    // process message
}


// Send a message on a topic
kotka.send(topic = topic, message = Message(...)))
```

## Using `@KotkaMessage`
```kotlin
// Annotated class where topic and thread count are specified
@KotkaMessage(topic = "test-annotated-message", threads = 8)
data class AnnotatedMessage(val name: String)

// Consumer only requires annotated class
kotka.consumer(AnnotatedMessage::class) { message ->
     messages.add(message)
}

// Sending a message doesn't require specifying a topic
kotka.send(AnnotatedMessage("..."))
```
