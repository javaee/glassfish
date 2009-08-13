#!/bin/sh
setEnv()
{
	export TEMP_DIR=./temp
	export PUBLISH_DIR=./publish
	export SCRIPTS_DIR=./scripts
	
}

initialize()
{
 if [ -d ${TEMP_DIR} ] ; then
  rm -rf ${TEMP_DIR}
 fi
 mkdir -p ${TEMP_DIR}
 
 if [ -d ${PUBLISH_DIR} ] ; then
  rm -rf ${PUBLISH_DIR}
 fi
 mkdir -p ${PUBLISH_DIR}
 mkdir -p ${PUBLISH_DIR}/jars
}

askUser()
{
 echo "Produce mc.jar? (y for yes, n for no)"
 read ans
 if [ ${ans} == "y" ] ; then
  echo "going ahead ..."
 else
  echo "bye"
  exit 1
 fi
}


copy()
{
    cp required-jars/jmxtools.jar ${TEMP_DIR}/jmxtools.jar
	cp required-jars/jmxremote_optional.jar ${TEMP_DIR}/jmxremote_optional.jar
	cp required-jars/javax77.jar ${TEMP_DIR}/javax77.jar
	
	cp jars/mc.jar ${TEMP_DIR}
	cp jars/mc-optional.jar ${TEMP_DIR}
}

unjar()
{
	echo "Unjarring "$1"..."
	jar xf $1
}

unjarAll()
{
	CWD=`pwd`
	cd ${TEMP_DIR}
	
	unjar javax77.jar
	unjar jmxtools.jar
	unjar jmxremote_optional.jar
	
	unjar mc.jar
	unjar mc-optional.jar
	cd $CWD
}

jarMC()
{
	echo "Creating mc.jar..."
	CWD=`pwd`
	cd ${TEMP_DIR}
	jar cf	mc.jar com javax META-INF
	cd $CWD
}


copyScripts()
{
    cp ${SCRIPTS_DIR}/mc ${PUBLISH_DIR}
    cp ${SCRIPTS_DIR}/mc.bat ${PUBLISH_DIR}
    cp ${SCRIPTS_DIR}/*.mc ${PUBLISH_DIR}
}

cleanup()
{
	rm -rf ${TEMP_DIR}
}


#
setEnv
initialize
#askUser
#copy
#unjarAll
#jarMC
#mv ${TEMP_DIR}/mc.jar $PUBLISH_DIR/mc.jar
cp target/mc.jar $PUBLISH_DIR/jars
cp /v3/glassfishv3/glassfish/modules/amx-core.jar $PUBLISH_DIR/jars
cp /v3/glassfishv3/glassfish/modules/jmxremote_optional-repackaged.jar $PUBLISH_DIR/jars/jmxremote_optional.jar
cp /v3/glassfishv3/glassfish/modules/javax.management.j2ee.jar $PUBLISH_DIR/jars



copyScripts
cleanup
echo $PUBLISH_DIR
ls -lR $PUBLISH_DIR

