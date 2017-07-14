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

unzip_test_sources(){
	unzip -d main/  $WORKSPACE/bundles/tests-workspace.zip > /dev/null
}
delete_test_sources(){
	rm -rf $WORKSPACE/main
	rm -rf $WORKSPACE/bundles
} 
download_test_zip(){
	mkdir bundles
	scp -o "StrictHostKeyChecking no" ${PARENT_NODE}:${PARENT_WS_PATH}/bundles/tests-workspace.zip bundles
}
	
###########################
#Start Script
###########################

run_test(){
	TEST_ID=$1
	delete_test_sources
	download_test_zip
	unzip_test_sources
	found=false
	for runtest in `find . -name run_test\.sh`; do
		for testid in `$runtest list_test_ids`; do
			if [[ "$testid" = "$TEST_ID" ]]; then
				found=true
				break
			fi
		done
		if [[ "$found" = true ]]; then
			$runtest run_test_id $TEST_ID
			break
		fi
	done
	if [[ "$found" = false ]]; then
		echo Invalid Test Id.
		exit 1
	fi

}

generate_platform(){
	uname -nsp > /tmp/platform
	scp -o "StrictHostKeyChecking no" -r /tmp/platform ${PARENT_NODE}:${PARENT_WS_PATH}/test-results/$TEST_ID
}

list_test_ids(){
	for runtest in `find . -name run_test\.sh`; do
		echo `$runtest list_test_ids`
	done
}

list_group_test_ids(){
	test_groups=`find . -type d -name test_groups` 
	test_id_arr+=(`cat  $test_groups/$1 |tr "\n" " "`)
	echo ${test_id_arr[*]}
}

OPT=$1
TEST_ID=$2

case $OPT in
	list_test_ids )
		if [[ -z $2 ]]; then
			list_test_ids
		else
			list_group_test_ids $2
		fi;;
		
	run_test )
		trap generate_platform EXIT
		run_test $TEST_ID ;;
esac
 
