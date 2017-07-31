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

test_run(){
  if [[ $1 = "deployment_cluster_all" ]]; then
    DEPL_TARGET=CLUSTER
    export DEPL_TARGET
  fi

  export HUDSON=true
  export ROOT=`pwd`

  # The first command-line argument is the (optional) predecessor job from which
  # to get the revision under test and the glassfish.zip file to expand.
  # Default: gf-trunk-build-continuous

  if [ -x "/usr/bin/cygpath" ]
  then
    ROOT=`cygpath -d $ROOT`
    echo "Windows ROOT: $ROOT"
    export CYGWIN=nontsec
  fi
  antTarget="all-ee"
  if [ -z "$DEPL_TARGET" ]
  then
      $S1AS_HOME/bin/asadmin start-domain
      antTarget="all"
  fi
  # Get rid of any lingering password file from an earlier run
  rm ~/.asadminpass || true

  time ant $antTarget | tee $TEST_RUN_LOG
  antStatus=$?
  # Copy the report to $APS_HOME
  cp tests-results.xml $APS_HOME/tests-results.xml
  cp results.html $APS_HOME/test_results.html 

  if [ -z "$DEPL_TARGET" ]
  then
      $S1AS_HOME/bin/asadmin stop-domain
  fi
   if [[ $1 = "deployment_cluster_all" ]]; then
      cp -r $APS_HOME/devtests/deployment/server-logs/ $WORKSPACE/results/
  fi  
  #
  echo DEPL_TARGET is $DEPL_TARGET
  if [ $antStatus -ne 0 ]
  then
      ps -ef 
      exit $antStatus
  fi
}

generate_junit_report_deployment(){
  printf "\n%s \n\n" "===== GENERATE JUNIT REPORT ====="
  TD=$APS_HOME/tests-results.xml
  JUD=$APS_HOME/test_results_junit.xml
  TESTSUITE_NAME=$1

  cat ${TD} | ${AWK} -v suitename=${TESTSUITE_NAME} '
  BEGIN{
    RS="</test>";
    FS="=";
    print "<testsuites>";
    print " <testsuite>";
    id=0;
  }
  {
    if ( NF > 1 ) {
      print "  <testcase classname=\"DeploymentTest\" name=\"" \
        substr($2,2,index($2,"description")-4) \
        id \
        "\">"		
      
      # searching for FAILED in field 4,5,6
      # if not found, test PASSED
      failure=1
      match($6,"FAILED")
      if( RLENGTH == -1) {
        match($5,"FAILED")
        if( RLENGTH == -1) {
          match($4,"FAILED")
          if( RLENGTH == -1) {
            failure=0;
          }
        }
      } 
      if( failure == 1 ) {
        print "   <failure type=\"testfailure\"/>"
      }
      
      print "  </testcase>"
      id++;
    }
  }
  END{
    print " </testsuite>";
    print "</testsuites>";
  }' > ${JUD}
  cp $JUD $WORKSPACE/results/junitreports
}

run_test_id(){
  source `dirname $0`/../../../common_test.sh
  kill_process
  delete_gf
  download_test_resources glassfish.zip version-info.txt
  unzip_test_resources $WORKSPACE/bundles/glassfish.zip
  cd `dirname $0`
  test_init
  test_run ${1}
  check_successful_run
  generate_junit_report_deployment $1
  change_junit_report_class_names
  }

post_test_run(){
    copy_test_artifects
    upload_test_results
    delete_bundle
    cd -
}


list_test_ids(){
  echo deployment_all deployment_cluster_all
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

