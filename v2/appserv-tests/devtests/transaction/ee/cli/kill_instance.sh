#!/bin/sh

echo process id `$JAVA_HOME/bin/jps -mlVv | grep ASMain | grep "instancename ${1}" | cut -f1 -d' '`
kill -9 `$JAVA_HOME/bin/jps -mlVv | grep ASMain | grep "instancename ${1}" | cut -f1 -d' '`

