#!/bin/sh
validateSilentFile() {
givenDirPath=`dirname $1`
givenFilePath=`basename $1`
if [ -f $givenDirPath/$givenFilePath ]
then
	absoluteFilePath="`cd $givenDirPath; pwd`/$givenFilePath"
	ARGS=`echo "${ARGS}" "${absoluteFilePath}" `
else
	echo "The silent installation file provided is not accessible. Please rerun this program with an appropriate statefile."
	exit 102
fi
}

validateAnswerFile() {
givenDirPath=`dirname $1`
givenFilePath=`basename $1`
if [ ! -f $givenDirPath/$givenFilePath ]
then
	absoluteFilePath="`cd $givenDirPath; pwd`/$givenFilePath"
	ARGS=`echo "${ARGS}" "${absoluteFilePath}" `
else
	echo "The answer file provided already exists. Please rerun this program by providing a non-existing answer file to be created."
	exit 104
fi
}

ARGS=""
export ARGS 
while [ $# -gt 0 ]
do
arg="$1"
if [ "${arg}" != "-s" ]
then
	ARGS="${ARGS} ${arg} "
fi
	case $arg in -n)
	shift
	if [ -z $1 ]
	then
		echo "Please provide a valid response file along with -n option."
		exit 103
	else
		validateAnswerFile $1
	fi
	;;
	-a)
	shift
	if [ -z $1 ]
	then
		echo "Please provide a valid answer file along with -a option."
		exit 101
	else
		validateSilentFile $1
	fi
	;;
	-s)
	ARGS=`echo ${ARGS} -p Display-Mode=SILENT `
	;;
	esac
shift
done
tmp=`mktemp -d -t install.XXXX`
if [ $? -ne 0 ]; then
    echo "Unable to create temporary directory, exiting..."
    exit 1
fi
echo "Extracting archive, please wait..."
tail +79l $0 > $tmp/tmp.jar
cd $tmp
$JAVA_HOME/bin/jar xvf tmp.jar 
rm tmp.jar
chmod ugo+x product-installer.sh
chmod ugo+x install/bin/engine-wrapper
echo "InstallHome.directory.INSTALL_HOME=$HOME/glassfishv3-prelude" > install.properties
sh product-installer.sh $ARGS
rm -rf ${tmp}/*
exit $?
