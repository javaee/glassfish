export APS_HOME=<Your-Workspace>/v2/appserv-tests

export S1AS_HOME=<installed-glassfish> (eg.,, /tmp/glassfish3/glassfish)

export ANT_OPTS="-Xss128k -Xmx2048m -XX:MaxPermSize=256m"

Download ant-tasks.jar[1] and copy it under /tmp

export CLASSPATH=/tmp/ant-tasks-3.1.1-SNAPSHOT.jar:$S1AS_HOME/lib/embedded/glassfish-embedded-static-shell.jar:$CLASSPATH

cp $APS_HOME/devtests/embedded/config/common.xml $APS_HOME/config
cp $APS_HOME/devtests/embedded/config/run.xml $APS_HOME/config

cd $APS_HOME/devtests/web

JOB_NAME=webtier-dev-tests-embedded ./exclude-jobs.sh

######### First set ###########

ant init web-container finish-report

########## Second set  #########

ant init  servlet taglib finish-report

########## Third set  #########

ant init security comet el http-connector jsp finish-report

[1] http://maven.glassfish.org/content/groups/glassfish/org/glassfish/ant-tasks/3.1.1-SNAPSHOT/ant-tasks-3.1.1-SNAPSHOT.jar
