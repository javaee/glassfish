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

locate_java() {

    # Search path for locating java
    java_locs="$JAVA_HOME/bin:/bin:/usr/bin:/usr/java/bin:$PATH"
    # Convert colons to spaces
    java_locs=`echo $java_locs | tr ":" " "`

    for j in $java_locs; do
        # Check if version is sufficient
        major=0
        minor=0
        if [ -x "$j/java" ]; then
            version=`"$j/java" -version 2>&1 | grep version | cut -d'"' -f2`
            major=`echo $version | cut -d'.' -f1`
            minor=`echo $version | cut -d'.' -f2`
        fi

        # We want 1.6 or newer
        if [ "$major" -eq "1" -a "$minor" -ge "6" ];  then
            echo "$j/java"
            return
        fi
        if [ "$major" -gt "1" ];  then
            echo "$j/java"
            return
        fi
    done

    echo ""
}

locate_jar() {

    # Search path for locating jar
    jar_locs="$JAVA_HOME/bin:/bin:/usr/bin:/usr/java/bin:$PATH"
    # Convert colons to spaces
    jar_locs=`echo $jar_locs | tr ":" " "`

    for j in $jar_locs; do
        if [ -x "$j/jar" ]; then
            echo "$j/jar"
	    return
        fi
    done

    echo ""
}

ARGS=""
export ARGS 
_POSIX2_VERSION=199209
export _POSIX2_VERSION

#validate JAVA_HOME, leave full validation to OI.
my_java=`locate_java`

if [ -z "$my_java" ]; then
    echo
    echo "Could not locate a suitable Java runtime."
    echo "Please ensure that you have Java 6 or newer installed on your system"
    echo "and accessible in your PATH or by setting JAVA_HOME"
    exit 105
fi

my_java_bin=`dirname $my_java`
JAVA_HOME=`dirname $my_java_bin`
export JAVA_HOME

my_jar=`locate_jar`

if [ -z "$my_jar" ]; then
    echo
    echo "Could not locate a suitable jar utility."
    echo "Please ensure that you have Java 6 or newer installed on your system"
    echo "and accessible in your PATH or by setting JAVA_HOME"
    exit 105
fi

CHECK_FOR_DISPLAY=1
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
	-R)
		echo "Invalid Argument, -R option is not applicable to this release."
		exit 101
	;;
	-r)
		echo "Invalid Argument, -r option is not applicable to this release."
		exit 101
	;;
	-h)
	CHECK_FOR_DISPLAY=0
	;;
	-help)
	CHECK_FOR_DISPLAY=0
	;;
	-s)
	CHECK_FOR_DISPLAY=0
	ARGS=`echo ${ARGS} -p Display-Mode=SILENT `
	;;
	esac
shift
done

#We don't have to check for this variable in Silent Mode
if [ ${CHECK_FOR_DISPLAY} -eq 1 ]
then
	if [ -z "${DISPLAY}" ]
	then
       	 echo "This program requires DISPLAY environment variable to be set."
       	 echo "Please re-run after assigning an appropriate value to DISPLAY".
       	 exit 106
	fi
fi

tmpdir_name=`date +%m%d%y%H%M%S`
tmpdir_path=/tmp/install.${tmpdir_name}
mkdir ${tmpdir_path}
if [ $? -ne 0 ]; then
 	echo "Unable to create temporary directory, exiting..."
    	exit 1
fi

echo "Extracting archive, please wait..."
tail +189l $0 > ${tmpdir_path}/tmp.jar
cd ${tmpdir_path}
$my_jar xvf tmp.jar 
$my_jar xvf ./Product/Packages/Engine.zip 
$my_jar xvf ./Product/Packages/Resources.zip 
$my_jar xvf ./Product/Packages/metadata.zip 
rm tmp.jar
chmod ugo+x product-installer.sh
chmod ugo+x install/bin/engine-wrapper
echo "InstallHome.directory.INSTALL_HOME=$HOME/glassfishv3" > install.properties
sh product-installer.sh $ARGS
rm -rf ${tmpdir_path}/*
exit $?
