# kotka
A simple and thin Kotlin wrapper for Kafka

## Gradle dependency

```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.blueanvil:kotka:1.0.1'
}
```

## Maven dependencies
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
    
    ...
<dependency>
    <groupId>com.github.blueanvil</groupId>
    <artifactId>kotka</artifactId>
    <version>1.0.1</version>
</dependency>
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


// Consumer only requires an instance of a message annotated with @KotkaMessage
kotka.consumer(AnnotatedMessage::class) { message ->
     messages.add(message)
}

// Similarly, sending a message doesn't require a topic anymore
kotka.send(AnnotatedMessage("..."))
```
