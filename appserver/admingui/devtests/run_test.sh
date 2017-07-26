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

list_test_ids(){
  echo admingui_all
}

merge_junit_xmls(){
  JUD_DIR=$1
  JUD=$WORKSPACE/results/junitreports/test_results_junit.xml
  rm -f ${JUD} || true
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >> ${JUD}
  echo "<testsuites>" >> ${JUD}
  find ${JUD_DIR} -name "*.xml" -type f -exec cat '{}' \; | ${SED} 's/<?xml version=\"1.0\" encoding=\"UTF-8\" ?>//g' >> ${JUD}
  echo -e "\n</testsuites>" >> ${JUD}
}

test_run(){
  export PWD=$(date | md5sum | cut -d' ' -f 1)
  touch $APS_HOME/password.txt 
  chmod 600 $APS_HOME/password.txt
  echo "AS_ADMIN_PASSWORD=" > $APS_HOME/password.txt
  echo "AS_ADMIN_NEWPASSWORD=$PWD" >> $APS_HOME/password.txt
  $S1AS_HOME/bin/asadmin --user admin --passwordfile $APS_HOME/password.txt change-admin-password
  $S1AS_HOME/bin/asadmin start-domain
  echo "AS_ADMIN_PASSWORD=$PWD" > $APS_HOME/password.txt
  $S1AS_HOME/bin/asadmin --passwordfile $APS_HOME/password.txt enable-secure-admin
  $S1AS_HOME/bin/asadmin restart-domain
  cd $APS_HOME/../../admingui/devtests/
  export DISPLAY=127.0.0.1:1	
  mvn -Dmaven.repo.local=$WORKSPACE/repository -DsecureAdmin=true -Dpasswordfile=$APS_HOME/password.txt test | tee $TEST_RUN_LOG
  $S1AS_HOME/bin/asadmin stop-domain
  rm -rf $APS_HOME/password.txt	
  cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/
  cp $TEST_RUN_LOG $WORKSPACE/results/
  cp $WORKSPACE/glassfish5/glassfish/domains/domain1/logs/server.log* $WORKSPACE/results/ || true  
}

run_test_id(){
  #a common util script located at main/appserver/tests/common_test.sh
  source `dirname $0`/../../tests/common_test.sh
  kill_process
  delete_gf
  export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=384m"
  download_test_resources glassfish.zip tests-maven-repo.zip version-info.txt
  unzip_test_resources $WORKSPACE/bundles/glassfish.zip "$WORKSPACE/bundles/tests-maven-repo.zip -d $WORKSPACE/repository"
  cd `dirname $0`
  test_init
  #run the actual test function
  test_run
  merge_junit_xmls $WORKSPACE/main/appserver/admingui/devtests/target/surefire-reports
  change_junit_report_class_names
  upload_test_results
  delete_bundle
}

OPT=$1
TEST_ID=$2
case $OPT in
  list_test_ids )
    list_test_ids;;
  run_test_id )
    run_test_id $TEST_ID ;;
esac
