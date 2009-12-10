#!/bin/bash
setEnv() {
 export MYSOURCE_DIR=src
 export OTHER_SOURCE_DIRS=tests:optional/mbeans
 export SOURCEPATH=${MYSOURCE_DIR}:${OTHER_SOURCE_DIRS}
 export OUT_DIR=docs/javadoc
 export SUB_PKGS=com.sun.cli
 export JAVADOC=javadoc
 export RJ=./required-jars
 export CLASSPATH=$RJ/jmxri.jar:$RJ/jmxremote.jar:$RJ/jmxremote_optional.jar:$RJ/sasl.jar:$RJ/sunsasl.jar:$RJ/junit.jar:$RJ/javax77.jar
}

initialize()
{
 if [ -d ${OUT_DIR} ] ; then
  /usr/bin/rm -rf ${OUT_DIR}
 fi
 /usr/bin/mkdir -p ${OUT_DIR}
}

askUser()
{
 echo "Proceed? (y for yes, n for no)"
 read ans
 if [ ${ans} == "y" ] ; then
  echo "going ahead ..."
 else
  echo "bye"
  exit 1
 fi
}

echoMessage()
{
 echo "Generating javadoc for ${SUB_PKGS} in ${OUT_DIR}"
 #askUser
}

generateJavadoc()
{
  ${JAVADOC} -d ${OUT_DIR} -protected -source 1.4 -subpackages ${SUB_PKGS} -classpath ${CLASSPATH} -link http://java.sun.com/j2se/1.4/docs/api -link http://java.sun.com/j2se/1.5.0/docs/api/ -link http://llcs.red.iplanet.com:8080/other-javadoc/jsr77/ -sourcepath ${SOURCEPATH} -linksource -windowtitle "jmxcmd" 
}


#
setEnv
initialize
echoMessage
generateJavadoc


