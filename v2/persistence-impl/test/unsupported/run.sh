#!/bin/sh
# Before you run this script set S1AS_HOME appropriately in your environment.
# You can also set them in this script directly.

if [ "$#" -lt "2" ]
then
    echo "Usage: run <classpath> <main_class> <args>"
    exit 1
fi

# To avoid setting S1AS_HOME in env, you can set them here directly as well.
#S1AS_HOME=H:/olsen/sjsas9/as9
if [ "${S1AS_HOME}" = "" ]
then
    echo "Before you run this script set S1AS_HOME appropriately in your environment."
    exit 1
fi

# ATTN: WINDOWS Users, set these two appropriately for your OS and copy this to a .bat file.
PS=";"
FS=/

# Change the following four vars to run your own app!
USER_JVM_OPTS=-ea
USER_CLASSPATH=$1
shift
MAIN_CLASS=$1
shift
ARGS="$*"
# uncomment this to do debugging.
#DEBUG_OPTS="-ea -Xdebug -Djava.compiler=none -Xrunjdwp${PS}transport=dt_socket,address=5005,server=y"

# Do not change any thing below this line.
PERSISTENCE_API=${S1AS_HOME}${FS}lib${FS}j2ee.jar
COMMONS_LOGGING=${S1AS_HOME}${FS}lib${FS}commons-logging.jar
JAXB=${S1AS_HOME}${FS}lib${FS}jaxrpc-impl.jar${PS}${S1AS_HOME}${FS}lib${FS}activation.jar
#PERSISTENCE_IMPL=${S1AS_HOME}${FS}lib${FS}appserv-cmp.jar
PERSISTENCE_IMPL=../dist/persistence-impl.jar${PS}${S1AS_HOME}${FS}lib${FS}appserv-cmp.jar
ANNOTATION_FRAMEWORK=${S1AS_HOME}${FS}lib${FS}appserv-rt.jar
SYSTEM_CLASSPATH=${PERSISTENCE_API}${PS}${PERSISTENCE_IMPL}${PS}${ANNOTATION_FRAMEWORK}${PS}${JAXB}${PS}${COMMONS_LOGGING}

echo SYSTEM_CLASSPATH=$SYSTEM_CLASSPATH
echo USER_CLASSPATH=$USER_CLASSPATH
echo MAIN=${MAIN_CLASS} ${ARGS}
echo
java -ea ${DEBUG_OPTS} ${USER_JVM_OPTS} -classpath ${SYSTEM_CLASSPATH}${PS}${USER_CLASSPATH} ${MAIN_CLASS} ${ARGS}
