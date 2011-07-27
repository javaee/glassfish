export APS_HOME=<Your-Workspace>/v2/appserv-tests

export S1AS_HOME=<installed-glassfish> (eg.,, /tmp/glassfish3/glassfish)

export ANT_OPTS="-Xss128k -Xmx2048m -XX:MaxPermSize=256m"

export CDI_TESTHOME=$APS_HOME/devtests/cdi
export CDI_TESTRUNLOG=$CDI_TESTHOME/cditests-run.log
export CDITESTSUMMARY=$CDI_TESTHOME/cditests-summary.log

Download ant-tasks.jar[1] and copy it under /tmp

export CLASSPATH=/tmp/ant-tasks-3.1.1-SNAPSHOT.jar:$S1AS_HOME/lib/embedded/glassfish-embedded-static-shell.jar:$S1AS_HOME/lib/gf-client.jar:$CLASSPATH

export GF_EMBEDDED_ENABLE_CLI=true

cp $APS_HOME/devtests/embedded/config/common.xml $APS_HOME/config
cp $APS_HOME/devtests/embedded/config/run.xml $APS_HOME/config

cd $APS_HOME/devtests/cdi

########## Running CDI tests #########
ant start-server all stop-server

[1] http://maven.glassfish.org/content/groups/glassfish/org/glassfish/ant-tasks/3.1.1-SNAPSHOT/ant-tasks-3.1.1-SNAPSHOT.jar
