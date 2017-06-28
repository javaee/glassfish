#!/bin/bash -ex
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

#Contract 1. returns the TEST ID, which you assigned in step 3.a
list_test_ids(){
  echo connector_all
}
 
test_run(){
  #test functions goes here, maven test or ant test etc.
  export HUDSON=true
  export ROOT=`pwd`
  echo $ROOT
  cd $APS_HOME/../v2-tests/appserv-tests
  ant startDomain startDerby
  antTarget="clean all report"
  cd $ROOT
  time ant $antTarget | tee $TEST_RUN_LOG
  antStatus=$?
  cd $APS_HOME/../v2-tests/appserv-tests
  ant stopDomain stopDerby
}
 
#Contract 2. does the clean up, downloads the tests/build sources and eventually runs tests
run_test_id(){
  #a common util script located at main/appserver/tests/common_test.sh
  source `dirname $0`/../../../../common_test.sh
  kill_process
  delete_gf
  download_test_resources glassfish.zip version-info.txt
  unzip_test_resources $WORKSPACE/bundles/glassfish.zip
  cd `dirname $0`
  test_init

  #run the actual test function
  test_run
 
  delete_bundle
  cd -
}
 
#Contract 3. script init code.
OPT=$1
TEST_ID=$2
case $OPT in
  list_test_ids )
    list_test_ids;;
  run_test_id )
    run_test_id $TEST_ID ;;
  connector_all )
    test_run;;
esac

