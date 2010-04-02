#!/bin/bash

export WORKSPACE=`pwd`
export BROWSER=firefox
export WORK_DIR=`pwd`/gf_instance

export SELENIUM_PORT=$[ ( $RANDOM % 2048 )  + 1024 ]
export GUI_PORT=$[ ( $RANDOM % 2048 )  + 1024 ]
export GUI_ADMIN_PORT=$[ ( $RANDOM % 2048 )  + 1024 ]
export GUI_JMS_PORT=$[ ( $RANDOM % 2048 )  + 1024 ]
export GUI_JMX_PORT=$[ ( $RANDOM % 2048 )  + 1024 ]
export GUI_ORB_PORT=$[ ( $RANDOM % 2048 )  + 1024 ]
export GUI_SSL_PORT=$[ ( $RANDOM % 2048 )  + 1024 ]
export GUI_ORB_SSL_PORT=$[ ( $RANDOM % 2048 )  + 1024 ]
export GUI_ORB_SSL_MUTUALAUTH_PORT=$[ ( $RANDOM % 2048 )  + 1024 ]

rm -rf $WORK_DIR
mkdir -p $WORK_DIR
cd $WORK_DIR

if [ ! -f glassfish.zip ]; then
    # get the latest GF 
    #wget -O glassfish.zip http://hudson.glassfish.org/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip
    cp ../../../distributions/glassfish/target/glassfish.zip .
fi
unzip -q glassfish.zip
cd -

export S1AS_HOME=$WORK_DIR/glassfishv3/glassfish

#Setup GlassFish Domain
$S1AS_HOME/bin/asadmin delete-domain domain1
$S1AS_HOME/bin/asadmin create-domain --adminport ${GUI_ADMIN_PORT} --nopassword=true --domainproperties jms.port=${GUI_JMS_PORT}:domain.jmxPort=${GUI_JMX_PORT}:orb.listener.port=${GUI_ORB_PORT}:http.ssl.port=${GUI_SSL_PORT}:orb.ssl.port=${GUI_ORB_SSL_PORT}:orb.mutualauth.port=${GUI_ORB_SSL_MUTUALAUTH_PORT} --instanceport ${GUI_PORT} domain1

#(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
$S1AS_HOME/bin/asadmin start-domain

mvn -Dbrowser=$BROWSER -Dadmin.port=$GUI_ADMIN_PORT -Dselenium.port=$SELENIUM_PORT test || true

$S1AS_HOME/bin/asadmin stop-domain
