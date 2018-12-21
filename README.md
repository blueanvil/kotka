# kotka
A simple and thin Kotlin wrapper for Kafka

# Example
```kotlin
val kotka = Kotka(kafkaServers = "localhost:59099", replicationFactor = 1)
val topic = "topic1"

// Setup a consumer
kotka.consumer(topic = topic, threads = 8, messageClass = Message::class) { message ->
    //do something with message
}

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
