#!/bin/sh
# Before you run this script set S1AS_HOME appropriately in your environment.
# You can also set them in this script directly.

if [ "$#" -lt "3" ]
then
    echo "Usage: compile <dest_dir> <classpath> <source_files>"
    exit 1
fi

# To avoid setting S1AS_HOME in env, you can set them here directly as well.
#S1AS_HOME=
if [ "${S1AS_HOME}" = "" ]
then
    echo "Before you run this script set S1AS_HOME appropriately in your environment."
    exit 1
fi

# ATTN: WINDOWS Users, set these two appropriately for your OS and copy this to a .bat file.
PS=";"
FS=/

# Change the following four vars to run your own app!
USER_JAVAC_OPTS=
DEST_DIR=$1
shift
USER_CLASSPATH=$1
shift
SOURCE_FILES=$*

if [ ! -e ${DEST_DIR} ]
then
   mkdir -p ${DEST_DIR}
fi

# Do not change any thing below this line.
PERSISTENCE_API=${S1AS_HOME}${FS}lib${FS}j2ee.jar
PERSISTENCE_IMPL=${S1AS_HOME}${FS}lib${FS}appserv-cmp.jar
SYSTEM_CLASSPATH=${PERSISTENCE_API}${PS}${PERSISTENCE_IMPL}${PS}${USER_CLASSPATH}

echo compiling ${SOURCE_FILES}
#echo CLASSPATH="${SYSTEM_CLASSPATH}${PS}${USER_CLASSPATH}"
javac ${USER_JAVAC_OPTS} -d ${DEST_DIR} -classpath "${SYSTEM_CLASSPATH}${PS}${USER_CLASSPATH}" ${SOURCE_FILES}
