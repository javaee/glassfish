#!/bin/sh
# Usage: deploy <enhanced_classes_dir> <unenhanced_classes_dir>
# Before you run this script set S1AS_HOME and ASM appropriately in your envirinment.
# You can also set them in this script directly.
# Contact: Sanjeeb.Sahoo@Sun.COM
#
# You can ALSO use this script to run your own application
# so that you don't have to worry about classpath settings etc.

if [ "$#" -ne "2" ]
then
    echo "Usage: deploy <enhanced_classes_dir> <unenhanced_classes_dir>"
    exit 1
fi

# To avoid setting S1AS_HOME and ASM in env, you can set them here directly as well.
#S1AS_HOME=
# since this is not yet installed in app server lib, we pick it from workspace.
ASM=../lib/asm.jar

if [ "${S1AS_HOME}" = "" ]
then
    echo "Before you run this script set S1AS_HOME and ASM appropriately in your environment."
    exit 1
fi
if [ "${ASM}" = "" ]
then
    echo "Before you run this script set ASM appropriately in your envirinment."
    echo "ASM should point to asm.jar, which you can check out as following command:"
    echo "cvs -d :pserver:<user>@rejuniper.sfbay.sun.com:/cvs update -p glassfish/persistence-impl/lib/asm.jar > ./asm.jar"
    exit 1
fi
if [ ! -e "$1" ]
then
    echo "Creating <enhanced_classes_dir>: " $1
    mkdir -p $1
fi

# ATTN: WINDOWS Users, set these two appropriately for your OS and copy this to a .bat file.
PS=";"
FS=/

# Change the following four vars to run your own app!
MAIN_CLASS=com.sun.persistence.deployment.impl.reflection.StandaloneDeployer
ARGS="deploy $*"
USER_JVM_OPTS=
USER_CLASSPATH=
# uncomment this to do debugging.
#DEBUG_OPTS="-ea -Xdebug -Djava.compiler=none -Xrunjdwp${PS}transport=dt_socket,address=5005,server=y"

# Do not change any thing below this line.
PERSISTENCE_API=${S1AS_HOME}${FS}lib${FS}j2ee.jar
#COMMONS_LOGGING=${S1AS_HOME}${FS}lib${FS}commons-logging.jar
JAXB=${S1AS_HOME}${FS}lib${FS}jaxrpc-impl.jar${PS}${S1AS_HOME}${FS}lib${FS}activation.jar
PERSISTENCE_IMPL=${S1AS_HOME}${FS}lib${FS}appserv-cmp.jar
ANNOTATION_FRAMEWORK=${S1AS_HOME}${FS}lib${FS}appserv-rt.jar
SYSTEM_CLASSPATH=${PERSISTENCE_API}${PS}${PERSISTENCE_IMPL}${PS}${ANNOTATION_FRAMEWORK}${PS}${JAXB}${PS}${COMMONS_LOGGING}${PS}${ASM}

echo CLASSPATH="${SYSTEM_CLASSPATH}${PS}${USER_CLASSPATH}"
java -ea ${DEBUG_OPTS} ${USER_JVM_OPTS} -classpath "${SYSTEM_CLASSPATH}${PS}${USER_CLASSPATH}" ${MAIN_CLASS} ${ARGS}
