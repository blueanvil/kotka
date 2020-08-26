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
    compile 'com.github.blueanvil:kotka:1.1'
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

## Pub-Sub Consumer
```kotlin
kotka.consumer(topic = topic, threads = 4, messageClass = Message::class, pubSub = true) { message ->
    // process message
}
```

## Multiple consumers on the same topic (since 1.1)
Kotka allows wiring multiple consumers on the same topic. This is useful when the same topic must be used
for a series of operations which have similar scope, but different implementations.

For example, we might need a topic that handles the generation of reports, but we want to have
different kinds of reports for different requirements. Also, we want to be able to configure the capacity (number of threads) for
report generation as a single setting.

```
kotka.consumer(topic, 2, GenerateExcel::class) { ... }
kotka.consumer(topic, 4, PngToPdf::class) { ... }

kotka.send(topic, GenerateExcel())
kotka.send(topic, PngToPdf())
kotka.send(topic, PngToPdf())
kotka.send(topic, GenerateExcel())
kotka.send(topic, PngToPdf())
```

# License Information
The code is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
