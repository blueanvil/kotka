#!/usr/bin/env bash

etc/tools/zookeeper.sh     start 3.5.4-beta 52188
sleep 10
etc/tools/kafka.sh         start 2.1.0 59099 52188
sleep 10