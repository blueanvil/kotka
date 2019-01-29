#!/usr/bin/env bash

if [ ! -e etc/bluebash ]; then
    git clone https://github.com/blueanvil/bluebash etc/bluebash
fi

etc/bluebash/src/tools/zookeeper.sh start etc/temp 3.5.4-beta 52188
sleep 10
etc/bluebash/src/tools/kafka.sh start etc/temp 2.1.0 59099 52188
sleep 5