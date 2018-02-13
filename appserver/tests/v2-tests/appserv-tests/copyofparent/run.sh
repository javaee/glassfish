#!/bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
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

S1AS_HOME=${S1AS_HOME}
APS_HOME=${APS_HOME}
JAVA_HOME=${S1AS_HOME}/jdk
ANT_HOME=${S1AS_HOME}/lib/ant

PATH=${S1AS_HOME}/bin:${ANT_HOME}/bin:${JAVA_HOME}/bin:$PATH

export S1AS_HOME
export JAVA_HOME
export APS_HOME
export ANT_HOME
export PATH


echo "APS_HOME set to: ${APS_HOME}"
echo "ANT_HOME set to: ${ANT_HOME}"
echo "S1AS_HOME set to: ${S1AS_HOME}"
echo "JAVA_HOME set to: ${JAVA_HOME}";

usage(){
    echo "";
    echo "Usage:";
    echo "";
    echo "sh run.sh <sqe|dev|all>"
    echo "";
    echo " 'sqe' runs SQE QuickLook Tests";
    echo " 'dev' runs Development unit Tests";
    echo " 'all' runs the entire workspace";
    exit 0;
}

stopPB(){
SYS=`uname`
case $SYS in
   Windows*)
      OS=win32;;   #Windows environments
   Linux*)
      OS=linux;;   #Linux environments
   SunOS*)
      OS=solaris;; #Solaris environments
   *)
      OS=unknown;;    #All the rest
esac

if [ $OS = "win32" ]; then
    for x in `ps|grep "startPB"|cut -d' ' -f 2`
    do
        kill -9 $x
    done
    for x in `ps|grep "pointbase"|cut -d' ' -f 2`
    do
        kill -9 $x
    done
else
    for x in `ps -ef|grep "startPB"|cut -d' ' -f 6`
    do
        kill -9 $x
    done
    for x in `ps -ef|grep "pointbase"|cut -d' ' -f 6`
    do
        kill -9 $x
    done
fi
}

startservers(){
    ant -buildfile ${APS_HOME}/build.xml startAS &
    ant -buildfile ${APS_HOME}/build.xml startPB &
    sleep 180;
}

stopservers(){
    echo "stopping appserver...";
    ant -buildfile ${APS_HOME}/build.xml stopAS;
    echo "done!";
    echo "killing pointbase processes...";
    stopPB;
    echo "done!";
}

run(){
    startservers;
    ant -buildfile ${APS_HOME}/build.xml ${TEST_TARGET};
    ant -buildfile ${APS_HOME}/build.xml report;
    stopservers;
}

#------ main --------------

if [ $# -lt 1 ]; then
    usage;
else
    if [ "$1" = "sqe" ]; then
        TEST_TARGET="sqetests";
    elif [ "$1" = "dev" ]; then
        TEST_TARGET="devtests";
    elif [ "$1" = "all" ]; then
        TEST_TARGET="all";
    else
        usage; 
    fi
    echo "granting permissions...";
    echo "grant codeBase \"file:${APS_HOME}/-\" {permission java.security.AllPermission;};" >> $S1AS_HOME/lib/appclient/client.policy;
    echo "done."
    run ${TEST_TARGET};
fi
#------ end main -----------------

