#!/bin/sh
#This script is a wrapper script for uninstallation process. This in turn 
#invokes Open Installer scripts after validating the environment/parameters.
#Currently it is not localized.

#check the environment for the uninstaller to run.
checkEnvArgs() {
#Check if the image has OI uninstallers.
if [ -f ${CWD}/install/bin/engine-wrapper -a  -f ${CWD}/install/bin/uninstaller ]
then
#Change permissions on OI launcher scripts
	/bin/chmod u+x ${CWD}/install/bin/engine-wrapper
	/bin/chmod u+x ${CWD}/install/bin/uninstaller

else
	echo "Required files for uninstaller missing from this directory. Aborting installation"
	exit ${OI_FILES_MISSING}
fi

#Check if config is available
if [ ! -d ${CWD}/var/install/config/glassfish ]
then
	echo "Config directory is missing from this installation. Aborting uninstallation."
	exit ${CONFIG_DIRECTORY_MISSING}
fi

#Check if metadata is available
if [ ! -d ${CWD}/metadata ]
then
	echo "metadata directory is missing from this installation. Aborting uninstallation."
	exit ${METADATA_DIRECTORY_MISSING}
fi
}

#Run the uninstaller with required args.
fireUninstaller() {
cd ${CWD}/install/bin
#Pass in any additional arguments passed to the script.
./uninstaller -s ${CWD}/var/install/config/glassfish -m file://${CWD}/metadata -p Default-Product-ID=glassfish -p Pkg-Format=zip -J "-Dorg.openinstaller.provider.configurator.class=org.openinstaller.provider.conf.InstallationConfigurator" $*
#Pass the exit code from OI back to the env.
exit $?
}

# Starts here..
# This would also get us the absolute path, in case users want to run 
# this script from other directories.
CURDIR=`dirname $0`
CWD=`cd $CURDIR; pwd`

#Error Codes
OI_FILES_MISSING=101
CONFIG_DIRECTORY_MISSING=102
METADATA_DIRECTORY_MISSING=103

#validate the required files and directories and the environment
checkEnvArgs

#Invoke uninstaller with required arguments
fireUninstaller $*

