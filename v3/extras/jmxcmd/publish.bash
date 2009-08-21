#!/bin/bash

echo "Out of date, do not use"

exit

setEnv()
{
	export PUBLISH_ROOT=/llc/docroot
	export BINARIES_ROOT=$PUBLISH_ROOT/binaries
	export LATEST_BINARIES=$BINARIES_ROOT/latest
}

removeExisting()
{
 /usr/bin/mkdir -p $PUBLISH_ROOT
 
 if [ -d $PUBLISH_ROOT/javadoc ] ; then
  /usr/bin/rm -rf $PUBLISH_ROOT/javadoc
 fi
 
 if [ -d $LATEST_BINARIES ] ; then
  /usr/bin/rm -rf $LATEST_BINARIES
 fi
}

askUser()
{
 echo "Publish jmxcmd? (y for yes, n for no)"
 read ans
 if [ ${ans} == "y" ] ; then
  echo "going ahead ..."
 else
  echo "bye"
  exit 1
 fi
}


copyBinaries()
{
	mkdir -p ${BINARIES_ROOT}
	mkdir -p ${LATEST_BINARIES}

# do jcmd
	cp jcmd.jar ${LATEST_BINARIES}
	cp scripts/jcmd ${LATEST_BINARIES}
	chmod +x ${LATEST_BINARIES}/jcmd
	
	unix2dos -ascii scripts/jcmd.bat ${LATEST_BINARIES}/jcmd.bat

#do jmxcmd
	cp jmxcmd.jar ${LATEST_BINARIES}
	cp scripts/*.jmxcmd ${LATEST_BINARIES}
	cp scripts/jmxcmd ${LATEST_BINARIES}
	chmod +x ${LATEST_BINARIES}/jmxcmd
	
	unix2dos -ascii scripts/jmxcmd.bat ${LATEST_BINARIES}/jmxcmd.bat
	
# copied, but we don't want it
	rm -rf $PUBLISH_ROOT/CVS
}

publishDocs()
{
	cp -r docs/* ${PUBLISH_ROOT}
}

#
setEnv
#askUser
./make-jcmd.bash
./make-jmxcmd.bash
./create-javadoc.bash
echo "Copying binaries..."
removeExisting
copyBinaries
publishDocs

echo "Done."





