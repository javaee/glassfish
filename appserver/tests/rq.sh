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

USAGE="Usage:\n\n 1. rq.sh -l ---> List all available test identifiers without running them\n\
	   2. rq.sh -b <branch> -a ---> For running all tests in remote branch\n\
	   3. rq.sh -b <branch> -g <test_group_name> ---> For running a test group\n\
	   4. rq.sh -b <branch> -t \"<test_id1> <test_id2> <test_id3>\" ---> For running a space separated list of tests\n\
	   5. rq.sh -u <glassfish binary url>  -a|-u|-t ---> For running all tests with GlassFish binary provided in the http url.-u option works with -a, -g and -t options as well\n\
	   6. rq.sh -b <branch> -a|-u|-t -e <email-id> ---> For getting the test results in the email id.This works with -a -t and -g options"
	   

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

if [[ -z $GLASSFISH_REMOTE_QUEUE_URL ]]; then
	echo "Please enter hudson url"
	exit 1
fi

OPTIND=1    

output_file=""
verbose=0

if [ $# -eq 0 ];
then
    echo -e $USAGE
    exit 0
fi    
while getopts ":b:t:g:e:u:al" opt; do
    case "$opt" in
    b)	branch=$OPTARG;;
    t)  test_ids=($OPTARG);;
    a)  test_ids=(`list_test_ids`);;
	g)  test_ids=(`list_group_test_ids $OPTARG`);;
	l)	test_ids=(`list_test_ids`)
		echo ${test_ids[@]} | tr " " "\n"
		exit 0;;
	e)  GLASSFISH_REMOTE_QUEUE_EMAIL=$OPTARG;;
	u)  url=$OPTARG;;
    *)	echo -e "Invalid option"
		echo -e $USAGE
		exit 1 ;;         
    esac
done

shift $((OPTIND-1))

if [[ -z $branch && -z $url ]]; then
	echo "Please provide a remote branch or a glassfish binary url to trigger glassfish remote queue"
	echo -e $USAGE
	exit 1
fi
if [[ -z $test_ids ]]; then
	echo "test id is missing"
	echo -e $USAGE
	exit 1
fi
if [[ -z $GLASSFISH_REMOTE_QUEUE_EMAIL && ! -z $branch ]]; then
	echo "EMAIL_ID is missing"
	echo -e $USAGE
	exit 1
fi
fork_origin=`git config --get remote.origin.url`
test_ids_encoded=`echo ${test_ids[@]} | tr ' ' '+'`
params="BRANCH=${branch}&TEST_IDS=${test_ids_encoded}&FORK_ORIGIN=${fork_origin}&URL=${url}&EMAIL_ID=${GLASSFISH_REMOTE_QUEUE_EMAIL}"
status=`curl -s -o /dev/null -w "%{http_code}" -X POST "${GLASSFISH_REMOTE_QUEUE_URL}/buildWithParameters?${params}&delay=0sec"`
echo $status
echo "----------------------------------------------------------------------------"
if [[ ${status} -eq 201 ]]; then
	printf "RQ triggered successfully. You will get the job link via email shortly\n"
	echo "----------------------------------------------------------------------------"
else
	printf "Issue in RQ client.Please check your settings\n"
    echo "----------------------------------------------------------------------------"
fi
