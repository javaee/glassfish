#!/bin/bash -ex
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

#u
# Usage: appserv-tests/devtests/web/hudson.sh [-d <url for download glassfish>]
#     [-d <directory for storing glassfish.zip>] [ -s <job name for skip file>]
#
# Hudson setup:
#
# Source Code Management: Subversion Modules
# Repository URL: https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/web
# Local module directory: appserv-tests/devtests/web
# Repository URL: https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/lib
# Local module directory: appserv-tests/lib
# Repository URL: https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/config
# Local module directory: appserv-tests/config
# Repository URL: https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/util
# Local module directory: appserv-tests/util
#
# Build after other projects are build: mirror-glassfish-repository
#
# The following TCP ports are assigned by Hudson to avoid collision
#   WEBTIER_ADMIN_PORT 
#   WEBTIER_JMS_PORT
#   WEBTIER_JMX_PORT 
#   WEBTIER_ORB_PORT
#   WEBTIER_HTTP_PORT
#   WEBTIER_HTTPS_PORT
#   WEBTIER_ALTERNATE_PORT
#   WEBTIER_ORB_SSL_PORT
#   WEBTIER_ORB_SSL_MUTUALAUTH_PORT
#   WEBTIER_INSTANCE_PORT
#   WEBTIER_INSTANCE_PORT_2
#   WEBTIER_INSTANCE_PORT_3
#   WEBTIER_INSTANCE_HTTPS_PORT
#
# If the script is used locally, WORKSPACE need to be defined as the parent of appserv-tests.
# And GlassFish will be installed in $WORKSPACE.
#
# Record finderprints of files to track usage: glassfish-v3-image/glassfish.zip
#     Fingerprint all archived artifacts
#
# Archive the artifacts: appserv-tests/test_results*.*,glassfish-v3-image/glassfish5/glassfish/domains/domain1/logs/*
#
# Publish SQE test result report
#     SQE report XMLs: appserv-tests/test_resultsValid.xml
#
# E-mail Notification
#     Recipients: <....>@oracle.com
#     Send e-mail for every unstable build

kill_processes() {
    uname=`uname | awk '{print $1}'`
    case "$uname" in
        CYGWIN*) KILL="taskkill /F /T /PID";;
        *) KILL="kill -9";;
    esac

    (ps -aef | grep java | grep ASMain | grep -v grep | awk '{print $2}' | xargs $KILL > /dev/null 2>&1) || true
    (jps | grep Main | grep -v grep | awk '{print $1}' | xargs $KILL > /dev/null 2>&1) || true
    (ps -aef | grep derby | grep -v grep | awk '{print $2}' | xargs $KILL > /dev/null 2>&1) || true
}

is_target(){
    case "$1" in
        "jsp" | \
        "taglib" | \
        "el" | \
        "servlet" | \
        "web-container" | \
        "security" | \
        "http-connector" | \
        "comet" | \
        "misc" | \
        "weblogicDD" | \
        "clustering" | \
        "ha" | \
        "embedded-all" | \
        "all") echo 1;;
        *) echo 0;;
    esac
}

get_test_target(){
	case $1 in
		web_all )
			TARGET=all
			export TARGET;;
	esac

}


test_run(){
	export WEBTIER_ADMIN_PORT=45707
	export WEBTIER_JMS_PORT=45708
	export WEBTIER_JMX_PORT=45709
	export WEBTIER_ORB_PORT=45710
	export WEBTIER_HTTP_PORT=45711
	export WEBTIER_HTTPS_PORT=45712
	export WEBTIER_ALTERNATE_PORT=45713
	export WEBTIER_ORB_SSL_PORT=45714
	export WEBTIER_ORB_SSL_MUTUALAUTH_PORT=45715
	export WEBTIER_INSTANCE_PORT=45716
	export WEBTIER_INSTANCE_PORT_2=45717
	export WEBTIER_INSTANCE_PORT_3=45718
	export WEBTIER_INSTANCE_HTTPS_PORT=45719

    while getopts u:s:d:t: flag; do
        case $flag in
            u)
                download=1;
                if [ "x$OPTARG" != "x" ]; then
                    GLASSFISH_DOWNLOAD_URL=$OPTARG;
                fi
                ;;
            s)
                SKIP_NAME=$OPTARG;
                ;;
            d) 
                DOWNLOAD_DIR=$OPTARG
                ;;
            t)
                TARGET=$OPTARG
                if [ `is_target $TARGET` -eq 0 ] ;  then
                   echo "Unknown target" 
                   exit
                elif [ "$TARGET" != "all" ] ; then
                    TARGET="$TARGET finish-report"
                fi
                ;;
            \?)
                echo "Illegal options"
                exit
                ;;
        esac
    done
    shift $(( OPTIND - 1 ));


    if [ "x$download" = "x1" ]; then
        cd $DOWNLOAD_DIR
        curl -O glassfish.zip $GLASSFISH_DOWNLOAD_URL
    fi

    export AS_LOGFILE=$S1AS_HOME/cli.log 
    #export AS_DEBUG=true 

    #Copy over the modified run.xml for dumping thread stack
    #cp ../../run.xml $PWD/appserv-tests/config

	rm -rf $S1AS_HOME/domains/domain1
    cd $APS_HOME

    echo "AS_ADMIN_PASSWORD=" > temppwd
    $S1AS_HOME/bin/asadmin --user admin --passwordfile $APS_HOME/config/adminpassword.txt create-domain --adminport ${WEBTIER_ADMIN_PORT} --domainproperties jms.port=${WEBTIER_JMS_PORT}:domain.jmxPort=${WEBTIER_JMX_PORT}:orb.listener.port=${WEBTIER_ORB_PORT}:http.ssl.port=${WEBTIER_HTTPS_PORT}:orb.ssl.port=${WEBTIER_ORB_SSL_PORT}:orb.mutualauth.port=${WEBTIER_ORB_SSL_MUTUALAUTH_PORT} --instanceport ${WEBTIER_HTTP_PORT} domain1

    if [ `uname | grep -n  'Linux' | wc -l` -eq 1 ] ; then
        HOST="localhost.localdomain"
    else
        HOST="localhost"
    fi

    #Create 
    echo "admin.domain=domain1
    admin.domain.dir=\${env.S1AS_HOME}/domains
    admin.port=${WEBTIER_ADMIN_PORT}
    admin.user=admin
    admin.host=$HOST
    http.port=${WEBTIER_HTTP_PORT}
    https.port=${WEBTIER_HTTPS_PORT}
    http.host=$HOST
    http.address=127.0.0.1
    http.alternate.port=${WEBTIER_ALTERNATE_PORT}
    orb.port=${WEBTIER_ORB_PORT}
    admin.password=
    ssl.password=changeit
    master.password=changeit
    admin.password.file=\${env.APS_HOME}/config/adminpassword.txt
    appserver.instance.name=server
    config.dottedname.prefix=server
    resources.dottedname.prefix=domain.resources
    results.mailhost=$HOST
    results.mailer=QLTestsForPEInstallOrDASInEEInstall@sun.com
    results.mailee=yourname@sun.com
    autodeploy.dir=\${env.S1AS_HOME}/domains/\${admin.domain}/autodeploy
    precompilejsp=true
    jvm.maxpermsize=192m
    ENABLE_REPLICATION=false
    appserver.instance.dir=\${admin.domain.dir}/\${admin.domain}
    cluster.name=clusterA
    instance.name=inst1
    instance.name.2=inst2
    instance.name.3=inst3
    instance.http.port=${WEBTIER_INSTANCE_PORT}
    instance.https.port=${WEBTIER_INSTANCE_HTTPS_PORT}
    instance.http.port.2=${WEBTIER_INSTANCE_PORT_2}
    instance.http.port.3=${WEBTIER_INSTANCE_PORT_3}
    nodeagent.name=localhost-domain1
    " > config.properties

    kill_processes

    cd $APS_HOME/devtests/web
    cp build.xml build.xml.orig
    ./exclude-jobs.sh $SKIP_NAME

    ant $TARGET |tee $TEST_RUN_LOG

    #restore original build.xml 
    mv build.xml.orig build.xml

    kill_processes
    (cat web.output | grep FAIL | grep -v "Total FAIL") || true
}

run_test_id(){
	source `dirname $0`/../../../common_test.sh
	kill_process
	delete_gf
	download_test_resources glassfish.zip version-info.txt
	unzip_test_resources $WORKSPACE/bundles/glassfish.zip
	cd `dirname $0`
	test_init
	get_test_target $1
	test_run -s webtier-dev-tests
	check_successful_run
    generate_junit_report $1
    change_junit_report_class_names
}

list_test_ids(){
    echo web_all
}
post_test_run(){
    copy_test_artifects
    upload_test_results
    delete_bundle
    cd -
}

OPT=$1
TEST_ID=$2

case $OPT in
    list_test_ids )
        list_test_ids;;
    run_test_id )
        trap post_test_run EXIT
        run_test_id $TEST_ID ;;
esac
