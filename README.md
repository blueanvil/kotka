# Kotka
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.com/blueanvil/kotka.svg?branch=master)](https://travis-ci.com/blueanvil/kotka)
[![Coverage Status](https://coveralls.io/repos/github/blueanvil/kotka/badge.svg?branch=master)](https://coveralls.io/github/blueanvil/kotka?branch=master)

Kotka is a thin Kotlin wrapper for Kafka with a simple and clean API, and only two dependencies: Jackson and Slf4J

# Gradle

```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.blueanvil:kotka:1.0.12'
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
// Annotated class where topic and thread count can be specified
@KotkaMessage(topic = "test-annotated-message", threads = 8)
data class AnnotatedMessage(val name: String)


// Consumer only requires an instance of a message annotated with @KotkaMessage
kotka.consumer(AnnotatedMessage::class) { message ->
     // process message
}

// Similarly, sending a message won't require a topic as it's being read from @KotkaMessage.topic
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

# License Information
* The code is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
