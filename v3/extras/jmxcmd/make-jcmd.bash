#!/bin/bash
setEnv()
{
	export TEMP_DIR=./temp
}

initialize()
{
 if [ -d ${TEMP_DIR} ] ; then
  /usr/bin/rm -rf ${TEMP_DIR}
 fi
 /usr/bin/mkdir -p ${TEMP_DIR}
}

askUser()
{
 echo "Produce jcmd.jar? (y for yes, n for no)"
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
	cp jcmd-classes.jar ${TEMP_DIR}/jcmd-classes.jar
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
	
	unjar jcmd-classes.jar
	
	cd $CWD
}

jarJCmd()
{
	echo "Creating jcmd.jar..."
	CWD=`pwd`
	cd ${TEMP_DIR}
	jar cf	jcmd.jar com META-INF
	cd $CWD
	
}

cleanup()
{
	/usr/bin/rm -rf ${TEMP_DIR}
}


#
setEnv
initialize
#askUser
copy
unjarAll
jarJCmd
mv ${TEMP_DIR}/jcmd.jar ./jcmd.jar
cleanup
echo "Done."




