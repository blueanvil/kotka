# kotka
A simple and thin Kotlin wrapper for Kafka

## Gradle dependency

```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.blueanvil:kotka:1.0.2'
}
```

## Maven dependency
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
    
<dependencies>
    <dependency>
        <groupId>com.github.blueanvil</groupId>
        <artifactId>kotka</artifactId>
        <version>1.0.2</version>
    </dependency>
</dependencies>
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

## Pub-Sub Consumer
```kotlin
kotka.consumer(topic = topic, threads = 4, messageClass = Message::class, pubSub = true) { message ->
    // process message
}
```
or, if using `@KotkaMessage`:
```kotlin
@KotkaMessage(topic = "test-annotated-pubsub-message", threads = 4, pubSub = true)
data class AnnotatedMessagePubSub(val name: String)


kotka.consumer(AnnotatedMessagePubSub::class) { message ->
    //process message
}
```
