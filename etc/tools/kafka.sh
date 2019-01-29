#!/usr/bin/env bash

KAFKA_VERSION=$2
KAFKA_PORT=$3
ZOOPORT=$4

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
mkdir -p $SCRIPT_DIR/temp/packages

TOOLDIR=$SCRIPT_DIR/temp/kafka
PACKAGE=$SCRIPT_DIR/temp/packages/kafka-${KAFKA_VERSION}.tar.gz

killKafka() {
    echo "*** Stop Kafka"
    # Kill if it exists
    if [ -e $TOOLDIR/bin/kafka-server-stop.sh ]; then
        echo "Killing Kafka first"
        $TOOLDIR/bin/kafka-server-stop.sh
    fi
}

startKafka() {
    echo "*** Starting Kafka"

    # Download if not present
    if [ ! -e $PACKAGE ]; then
        DONWLOAD_URL="http://www-eu.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_2.12-${KAFKA_VERSION}.tgz"
        echo "Downloading $DONWLOAD_URL"
        curl "$DONWLOAD_URL" > $PACKAGE
    fi


    # Unpack
    rm -rf $TOOLDIR
    mkdir -p $TOOLDIR/klogs

    tar xf $PACKAGE -C $TOOLDIR --strip-components=1

    sed -i.bak "s/listeners.*/listeners=PLAINTEXT:\/\/:${KAFKA_PORT}/" $TOOLDIR/config/server.properties
    sed -i.bak "s/metadata.broker.list.*/metadata.broker.list=localhost:${KAFKA_PORT}/" $TOOLDIR/config/producer.properties
    sed -i.bak "s/zookeeper.connect=.*/zookeeper.connect=localhost:${ZOOPORT}/" $TOOLDIR/config/consumer.properties
    sed -i.bak "s/zookeeper.connect=.*/zookeeper.connect=localhost:${ZOOPORT}/" $TOOLDIR/config/server.properties
    sed -i.bak "s/log.dirs.*//" $TOOLDIR/config/server.properties
    echo "log.dirs=$TOOLDIR/klogs" >> $TOOLDIR/config/server.properties
    echo "advertised.host.name=localhost" >> $TOOLDIR/config/server.properties
    echo "port=${KAFKA_PORT}" >> $TOOLDIR/config/server.properties
    sed -i.bak "s/clientPort.*/clientPort=${ZOOPORT}/" $TOOLDIR/config/zookeeper.properties

    mkdir $TOOLDIR/logs

    $TOOLDIR/bin/kafka-server-start.sh -daemon $TOOLDIR/config/server.properties
    echo "Kafka started"
}


case "$1" in
start)
        killKafka
        startKafka
        ;;

stop)
        killKafka
        ;;

esac

