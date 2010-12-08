#!/bin/sh

echo process id `$JAVA_HOME/bin/jps -mlVv | grep ${1} | cut -f1 -d' '`
kill -9 `$JAVA_HOME/bin/jps -mlVv | grep ${1} | cut -f1 -d' '`

