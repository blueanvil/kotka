# kotka
A simple and thin Kotlin wrapper for Kafka

# Example
```
val kotka = Kotka("localhost:59099", 1)
val topic = "topic1"

// Setup a consumer
kotka.consumer(topic, 1, Message::class) { message ->
    //do something with message
}

kotka.send(topic, Message(...)))
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
