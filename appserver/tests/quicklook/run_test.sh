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
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
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

copy_ql_results(){
	cp $WORKSPACE/glassfish5/glassfish/domains/domain1/logs/server.log* $WORKSPACE/results/ || true
	cp $TEST_RUN_LOG $WORKSPACE/results/
	cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/
	cp -r test-output/* $WORKSPACE/results/
	cp test-output/TESTS-TestSuites.xml $WORKSPACE/results/junitreports/test_results_junit.xml
	cp quicklook_summary.txt $WORKSPACE/results || true
}

run_test_id(){
	source `dirname $0`/../common_test.sh
	kill_process
	delete_gf
	ql_init	
	if [[ $1 = "ql_gf_full_profile_all" ]]; then
	    download_test_resources glassfish.zip tests-maven-repo.zip version-info.txt
		unzip_test_resources $WORKSPACE/bundles/glassfish.zip "$WORKSPACE/bundles/tests-maven-repo.zip -d $WORKSPACE/repository"	    	
		cd $WORKSPACE/main/appserver/tests/quicklook/
		mvn -Dglassfish.home=$WORKSPACE/glassfish5/glassfish -Dmaven.repo.local=$WORKSPACE/repository -Ptest_gd_security,report test | tee $TEST_RUN_LOG
		copy_ql_results
	elif [[ $1 = "ql_gf_nucleus_all" || $1 = "nucleus_admin_all" ]]; then
		download_test_resources nucleus-new.zip tests-maven-repo.zip version-info.txt
		unzip_test_resources $WORKSPACE/bundles/nucleus-new.zip "$WORKSPACE/bundles/tests-maven-repo.zip -d $WORKSPACE/repository"
		if [[ $1 = "ql_gf_nucleus_all" ]]; then
			cd $WORKSPACE/main/nucleus/tests/quicklook
		elif [[ $1 = "nucleus_admin_all"  ]]; then
			cd $WORKSPACE/main/nucleus/tests/admin
		fi
		mvn -Dmaven.test.failure.ignore=true -Dnucleus.home=$WORKSPACE/nucleus -Dmaven.repo.local=$WORKSPACE/repository clean test | tee $TEST_RUN_LOG
		cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/
		if [[ $1 = "ql_gf_nucleus_all" ]]; then
			merge_junit_xmls $WORKSPACE/main/nucleus/tests/quicklook/target/surefire-reports/junitreports
		elif [[ $1 = "nucleus_admin_all"  ]]; then
			merge_junit_xmls $WORKSPACE/main/nucleus/tests/admin/target/surefire-reports/junitreports
		fi
		cp $WORKSPACE/nucleus/domains/domain1/logs/server.log* $WORKSPACE/results
		cp $TEST_RUN_LOG $WORKSPACE/results/
	elif [[ $1 = "ql_gf_web_profile_all" || $1 = "ql_gf_embedded_profile_all" ]]; then
		download_test_resources web.zip tests-maven-repo.zip version-info.txt
		unzip_test_resources $WORKSPACE/bundles/web.zip "$WORKSPACE/bundles/tests-maven-repo.zip -d $WORKSPACE/repository"
		cd $WORKSPACE/main/appserver/tests/quicklook/
		if [[ $1 = "ql_gf_web_profile_all" ]]; then
			mvn -Dglassfish.home=$WORKSPACE/glassfish5/glassfish -Dmaven.repo.local=$WORKSPACE/repository -Ptest_wd_security,report test | tee $TEST_RUN_LOG
		elif [[ $1 = "ql_gf_embedded_profile_all" ]]; then
			mvn -Dglassfish.home=$WORKSPACE/glassfish5/glassfish -Dmaven.repo.local=$WORKSPACE/repository -Ptest_em,report test | tee $TEST_RUN_LOG
		fi
		copy_ql_results
	else
		echo "Invalid Test Id"
		exit 1
	fi
    change_junit_report_class_names
}

post_test_run(){
    if [[ $? -ne 0 ]]; then
    	if [[ $TEST_ID = "ql_gf_full_profile_all" || $TEST_ID = "ql_gf_web_profile_all" || $TEST_ID = "ql_gf_embedded_profile_all" ]]; then
	  		copy_ql_results || true
	  	fi
	  	if [[ $TEST_ID = "ql_gf_nucleus_all" || $TEST_ID = "nucleus_admin_all" ]]; then
	  		cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/ || true
	  		cp $WORKSPACE/nucleus/domains/domain1/logs/server.log* $WORKSPACE/results || true
		    cp $TEST_RUN_LOG $WORKSPACE/results/ || true
	  	fi
	fi
    upload_test_results
    delete_bundle
    cd -
}

merge_junit_xmls(){
  JUD_DIR=$1
  JUD=$WORKSPACE/results/junitreports/test_results_junit.xml
  rm -f ${JUD} || true
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >> ${JUD}
  echo "<testsuites>" >> ${JUD}
  find ${JUD_DIR} -name "*.xml" -type f -exec cat '{}' \; | ${SED} 's/<?xml version=\"1.0\" encoding=\"UTF-8\"?>//g' >> ${JUD}
  echo "</testsuites>" >> ${JUD}
}

list_test_ids(){
	echo ql_gf_full_profile_all ql_gf_nucleus_all ql_gf_web_profile_all ql_gf_embedded_profile_all nucleus_admin_all
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
