#!/usr/bin/env bash


ZOOVERSION=$2
ZOOPORT=$3


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
mkdir -p $SCRIPT_DIR/temp/packages

TOOLDIR=$SCRIPT_DIR/temp/zookeeper-${ZOOVERSION}
PACKAGE=$SCRIPT_DIR/temp/packages/zookeeper-${ZOOVERSION}.tar.gz
PIDFILE=$SCRIPT_DIR/temp/zookeeper-${ZOOVERSION}/zlogs/zookeeper_server.pid


killZookeeper() {
    echo "*** Stopping Zookeeper"
    # Kill if it exists
    if [ -e $PIDFILE ]; then
        PID=`cat $PIDFILE`
        echo "Killing Zookeeper with PID $PID from $PIDFILE"
        kill -9 $PID
    fi
}

startZookeeper() {
    echo "*** Starting Zookeeper"
    # Download if not present
    if [ ! -e $PACKAGE ]; then
        DONWLOAD_URL="http://www-eu.apache.org/dist/zookeeper/zookeeper-${ZOOVERSION}/zookeeper-${ZOOVERSION}.tar.gz"
        echo "Downloading $DONWLOAD_URL"
        curl "$DONWLOAD_URL" > $PACKAGE
    fi


    # Unpack
    rm -rf $TOOLDIR
    mkdir -p $TOOLDIR/zlogs

    tar xf $PACKAGE -C $TOOLDIR --strip-components=1
    mv $TOOLDIR/conf/zoo_sample.cfg $TOOLDIR/conf/zoo.cfg

    sed -i.bak "s/clientPort.*/clientPort=$ZOOPORT/" $TOOLDIR/conf/zoo.cfg
    sed -i.bak "s/dataDir.*//" $TOOLDIR/conf/zoo.cfg
    echo "dataDir=$TOOLDIR/zlogs" >> $TOOLDIR/conf/zoo.cfg
    echo "admin.serverPort=19999" >> $TOOLDIR/conf/zoo.cfg

    # Start
    cd $TOOLDIR/bin
    ./zkServer.sh start
}

case "$1" in
start)
        killZookeeper
        startZookeeper
        ;;

stop)
        killZookeeper
        ;;

esac

