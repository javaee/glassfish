#!/bin/sh

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

