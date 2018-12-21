# kotka
A simple and thin Kotlin wrapper for Kafka

# Main component
```kotlin
val kotka = Kotka(kafkaServers = "localhost:59099", replicationFactor = 1)
```

# Standard consumer setup
```kotlin
kotka.consumer(topic = "topic1", threads = 8, messageClass = Message::class) { message ->
    // process message
}
```

# Send a message on a topic
```kotlin
kotka.send(topic = topic, message = Message(...)))
```

# Gradle dependency

```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.blueanvil:kotka:1.0.0'
}
```
