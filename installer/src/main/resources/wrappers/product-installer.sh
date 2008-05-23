#!/bin/sh
# 
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. 
# 
# Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved. 
# 
# The contents of this file are subject to the terms of either the GNU 
# General Public License Version 2 only ("GPL") or the Common Development 
# and Distribution License("CDDL") (collectively, the "License").  You 
# may not use this file except in compliance with the License. You can obtain 
# a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html 
# or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific 
# language governing permissions and limitations under the License. 
# 
# When distributing the software, include this License Header Notice in each 
# file and include the License file at glassfish/bootstrap/legal/LICENSE.txt. 
# Sun designates this particular file as subject to the "Classpath" exception 
# as provided by Sun in the GPL Version 2 section of the License file that 
# accompanied this code.  If applicable, add the following below the License 
# Header, with the fields enclosed by brackets [] replaced by your own 
# identifying information: "Portions Copyrighted [year] 
# [name of copyright owner]" 
# 
# Contributor(s): 
# 
# If you wish your version of this file to be governed by only the CDDL or 
# only the GPL Version 2, indicate your decision by adding "[Contributor] 
# elects to include this software in this distribution under the [CDDL or GPL 
# Version 2] license."  If you don't indicate a single choice of license, a 
# recipient has the option to distribute your version of this file under 
# either the CDDL, the GPL Version 2 or to extend the choice of license to 
# its licensees as provided above.  However, if you add GPL Version 2 code 
# and therefore, elected the GPL Version 2 license, then the option applies 
# only if the new code is made subject to such option by the copyright 
# holder. 
# 
 

# Sample fake product.
PRODUCTNAME="glassfish"
ORIG_ARGS=$@

INST_DIR=/tmp/.launcher.$$
JAVA_LOC=${JAVA_HOME}

# binaries needed on both Solaris, Linux, etc.
CAT=/bin/cat
#CD=/bin/cd
CHMOD=/bin/chmod
CP=/bin/cp
CUT=/bin/cut
DIRNAME=/usr/bin/dirname
CPIO=/bin/cpio
FIND=/usr/bin/find
ECHO=/bin/echo
EGREP=/bin/egrep
ID=/usr/bin/id
MKDIR=/bin/mkdir
_PWD=/bin/pwd
RM=/bin/rm
SED=/bin/sed
SU=/bin/su
TOUCH=/bin/touch
UNAME=uname
XAUTH=/openwin/bin/xauth,/usr/X11R6/bin/xauth

TEXTDOMAINDIR="@LOCALEDIR@"

NAME=`basename $0`
MYDIR=`${DIRNAME} $0`
MYDIR=`cd ${MYDIR}; ${_PWD}`

ENGINE_DIR=${MYDIR}/install

# global settings
JAVA_HOME="$JAVA_HOME"				# java home path
JAVAOPTIONS="-Dorg.openinstaller.provider.configurator.class=org.openinstaller.provider.conf.InstallationConfigurator"			  
INSTALLPROPS=""     # install specific properties

# user options
DRYRUN=
ANSWERFILE=
ALTROOT=
DEBUGLEVEL="INFO"
MEDIALOC=
INSTALLABLES=
LOGDIR=/tmp

#-------------------------------------------------------------------------------
# usage only: define what parameters are available here
# input(s):  exitCode
#-------------------------------------------------------------------------------



usage() {
  ${CAT} <<EOF
Usage: $NAME [OPTION]

Options:
  -h, --help
   Show the help message
  -l <dir>, --logdir=<dir>
   Directory to store log files.
  -q, --quiet
   Only output related to severe or fatal problems will be sent to the console and the logfile
  -v, --verbose
   All output, including non-localized debug messages, will be sent to the console and to the
   logfile.
  -t, --text
   Select Text mode instead of GUI mode.  Text mode is automatically selected if no suitable display can be found.
  -n, --dry-run
    Dry run mode. Do not install anything. Configuration answers written to <file>
  -a <file>, --answerfile=<file>
   Pointer to an answer file that can be used by openInstaller's silent mode installer
  -R <path>, --altroot=<path to alternate root>
   Path to alternate root for the installer.  All operations (install, etc) are relative
   to this root directory.

Private options:

  -j <javahome>, --javahome=<javahome> :
    use this java vm instead of searching for suitable one
  -J <joptions>, --jvmoptions=<joptions> :
    specify java vm options.  For example -o "-verbose -Dhttp.proxy=foo.com:3434"
  -p <props>, --properties=<props> :
    specify 1 or more config properties options, separated by comma,
    to the install engine.  For example:
    --properties=Logs-Location=/tmp,Platform-Plugin-Path=c:\\Sun\\ppp

EOF
  exit $1
}

#-------------------------------------------------------------------------------
# perform actual operation for the script: install/uninstall
# input(s):  none
# output(s): instCode
#-------------------------------------------------------------------------------
perform() {

ENGINE_OPS="-m file://${MYDIR}/metadata/"
ENGINE_OPS="${ENGINE_OPS} -a file://${MYDIR}/install.properties"
ENGINE_OPS="${ENGINE_OPS} -i file://${MYDIR}/Product/"
ENGINE_OPS="${ENGINE_OPS} -p Default-Product-ID=${PRODUCTNAME}"
ENGINE_OPS="${ENGINE_OPS} -p Pkg-Format=zip"
ENGINE_OPS="${ENGINE_OPS} -C ${MYDIR}/commons-codec-1.3.jar:${MYDIR}/registration-api.jar:${MYDIR}/registration-impl.jar:${MYDIR}/sysnet-registration.jar"


# add ubi-enabled packaging tool location to environment so that it
# is picked up by PH engine.
#
INSTALL_OSTOOLS=${MYDIR}
export INSTALL_OSTOOLS

if [ -n "${DRYRUN}" ] ; then
    ENGINE_OPS="${ENGINE_OPS} -n ${DRYRUN}"
fi

if [ -n "${ANSWERFILE}" ] ; then
    ENGINE_OPS="${ENGINE_OPS} -a ${ANSWERFILE}"
fi

if [ -n "${ALTROOT}" ] ; then
    ENGINE_OPS="${ENGINE_OPS} -R ${ALTROOT}"
fi

if [ -n "${LOGLEVEL}" ] ; then
    ENGINE_OPS="${ENGINE_OPS} -l ${LOGLEVEL}"
fi


if [ -n "${LOGDIR}" ] ; then
    ENGINE_OPS="${ENGINE_OPS} -p Logs-Location=${LOGDIR}"
fi

if [ -n "${JAVA_HOME}" ] ; then
    ENGINE_OPS="${ENGINE_OPS} -j ${JAVA_HOME}"
fi

if [ -n "${INSTALLPROPS}" ] ; then
    ENGINE_OPS="${ENGINE_OPS} ${INSTALLPROPS}"
fi

if [ -n "${INSTALLABLES}" ] ; then
    ENGINE_OPS="${ENGINE_OPS} -i ${INSTALLABLES}"
fi

${ENGINE_DIR}/bin/engine-wrapper -J "${JAVAOPTIONS}" ${ENGINE_OPS}
instCode=$?

}

#-------------------------------------------------------------------------------
# cleanup temporary files
#-------------------------------------------------------------------------------
cleanup() {

if [ ! -d ${INST_DIR} ] ; then
    return
fi

# Preventative measure to not nuke entire system
cd ${INST_DIR}
_pwd=`pwd`

if [ ${_pwd} != "/" ] ; then
    cd /
    echo "Cleaning up temporary environment."
    ${RM} -rf ${INST_DIR}
fi

}


#-------------------------------------------------------------------------------
# retrieve bundled JVM from Media based on os and platfo${RM}
# input(s):  none
# output(s): JAVAMEDIAPATH
#-------------------------------------------------------------------------------
getBundledJvm() {
  JAVAMEDIAPATH=""
  case `${UNAME} -s` in
    "SunOS")
       case `${UNAME} -p` in
         "sparc")
           JAVAMEDIAPATH="usr/jdk/instances/@pp.jre.pkg.basedirname@/"
           ;;
         "i386")
           JAVAMEDIAPATH="usr/jdk/instances/@pp.jre.pkg.basedirname@/"
           ;;
         *)
           echo  "Unknown platform, exiting"
	   exit 1
           ;;
       esac
       ;;
    "Linux")
       JAVAMEDIAPATH="usr/java/@pp.jre.rpm.basedirname@"
       ;;
    "HP-UX")
       JAVAMEDIAPATH="HP-UX"
       ;;
    "AIX")
       JAVAMEDIAPATH="AIX"
       ;;
    *)
      "Do not recognize `uname -p` platform, no JVM available"
      exit 1
      ;;
  esac
}

#-------------------------------------------------------------------------------
# login the user as root user
# use the 'su' command to ask for password and run the installer
#-------------------------------------------------------------------------------
loginAsRoot() {
USERID=`${ID} | ${CUT} -d'(' -f1 | ${CUT} -d'=' -f2`
if [ "$USERID" != "0" ]; then
    ${ECHO}
    echo "To use this installer, you will need to be the system's root user. \n"
    if [ -n "$DISPLAY" ]; then
      tmp_file="/tmp/installer_auth_$USER_$DISPLAY"
      touch $tmp_file
      ${CHMOD} 600 $tmp_file
      ${XAUTH} extract - $DISPLAY > $tmp_file
    fi
    status=1;
    retry=3;
    while [ $status = 1 -a ! $retry = 0 ]; do
      echo "Please enter this system's root user password \n"
      ${SU} root -c "${0} ${ORIG_ARGS}"
      status=$?
      retry=`expr $retry - 1`
      ${ECHO} " "
    done
    if [ "$retry" = 0 ]; then
      echo "Administrative privilege is req'd to perform this operation. Exiting.\n"
      exit 1
    fi
    exit
  fi
  unset userId
  unset status
  unset retry
}

useBundledJvm() {

  getBundledJvm
  JAVA_HOME=${BUNDLED_JAVA_JRE_LOC}/${JAVAMEDIAPATH}
  if [ ! -d ${JAVA_HOME} ] ; then
       echo  "${JAVA_HOME} must be the root directory of a valid JVM installation"
       echo  "Please provide JAVA_HOME as argument with -j option and proceed."
       exit 1
  fi
}

#-------------------------------------------------------------------------------
# ****************************** MAIN THREAD ***********************************
#-------------------------------------------------------------------------------

# Linux has no built-in support for long-style getopts so we use the short style only
LONGOPT="h(help)l:(logdir)q(quiet)v(verbose)t(text)n:(dry-run)a:(answerfile)j:(javahome)R:(altroot)J:(jvmoptions)p:(properties)"
SHORTOPT="hl:n:qvta:R:j:J:p:"

export TEXTDOMAINDIR

OS1=`${UNAME} -s`
OS2=`${UNAME} -r`
if [ "${OS1}" = SunOS ] ; then
    case "${OS2}" in
      2.* | 5.7 | 5.8)
        echo  "openInstaller is only supported on Solaris 9 or later"
        exit 1
        ;;
      5.9)
        OPTSTRING=${SHORTOPT}
        ;;
      *)
        OPTSTRING=${LONGOPT}
        ;;
     esac
else
  # Linux has no built-in support for long-style getopts so we use the short style only
  OPTSTRING=${SHORTOPT}
fi

# check arguments
while getopts "${OPTSTRING}" opt ; do
    case "${opt}" in

	a) ANSWERFILE=${OPTARG}
  ;;

	R) ALTROOT=${OPTARG}

	    if [ ! -d ${ALTROOT} -o ! -r ${ALTROOT} ] ; then
		echo  "${ALTROOT} is not a valid alternate root"
		exit 1
	    fi
	;;

	l) LOGDIR=${OPTARG}

	    if [ ! -d ${LOGDIR} -o ! -w ${LOGDIR} ] ; then
		echo  "${LOGDIR} is not a directory or is not writable"
		exit 1
	    fi
	;;

	q) LOGLEVEL=WARNING
	;;
	v) LOGLEVEL=FINEST
	;;
        t) INSTALLPROPS="${INSTALLPROPS} -p Display-Mode=CUI"
        ;;
    n) DRYRUN=${OPTARG}
    ;;
	j) JAVA_HOME=${OPTARG}

	    if [ ! -d ${JAVA_HOME} -o ! -r ${JAVA_HOME} ] ; then
		echo  "${JAVA_HOME} must be the root directory of a valid JVM installation"
		exit 1
	    fi
	;;

	J) JAVAOPTIONS=${OPTARG}
	;;
	p) INSTALLPROPS="${INSTALLPROPS} -p ${OPTARG}"
	;;
	?|h) usage
	;;
    esac
done

${ECHO}
echo "Welcome to GlassFish V3 installer"
${ECHO}

# check user for access privileges
#loginAsRoot

trap 'cleanup; exit' 1 2 13 15

# overwrite check if user specify javahome to use
if [ -z "$JAVA_HOME" ]; then
    echo "Creating temporary environment..."
    useBundledJvm
else
    echo  "Using the user defined JAVA_HOME : ${JAVA_HOME}"

fi

echo "Entering setup..."
perform
cleanup
exit $instCode
