#!/bin/bash -e
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

USAGE="Usage: rq -b BRANCH -a ---> For runing all the test ids\n\
	   or  rq -b BRANCH -g TESTGROUPNAME ---> For runing a TESTGROUPNAME\n\
	   or  rq -b BRANCH -t TESTIDS ---> For runing a space separated set of TESTIDS"

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

if [[ -z $HUDSON_URL ]]; then
	echo "Please enter hudson url"
	exit 1
fi

OPTIND=1    

output_file=""
verbose=0

while getopts ":b:t:g:a" opt; do
    case "$opt" in
    b)	branch=$OPTARG;;
    t)  test_ids=($OPTARG);;
    a)  test_ids=(`list_test_ids`);;
	g)  test_ids=(`list_group_test_ids $OPTARG`);;
    *)	echo -e "Invalid option"
		echo -e $USAGE
		exit 1 ;;         
    esac
done

shift $((OPTIND-1))

if [[ -z $branch ]]; then
	echo "branch is missing"
	echo -e $USAGE
	exit 1
fi
if [[ -z $test_ids ]]; then
	echo "test id is missing"
	echo -e $USAGE
	exit 1
fi

test_ids_encoded=`echo ${test_ids[@]} | tr ' ' '+'`
params="BRANCH=${branch}&TEST_IDS=${test_ids_encoded}"
curl -X POST "${HUDSON_URL}/buildWithParameters?${params}&delay=0sec" 2> /dev/null