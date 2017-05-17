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

#
# Map style.
# Returns the value for the supplied key
#
# Args: key array_of_key=val
#
get_value_from_key_val_array(){
  local _array=${2}
  local array=(${_array[*]})
  local i=0
  while [ ${i} -lt ${#array[*]} ]
  do
    entry=(`key_val_as_list ${array[${i}]}`)
    if [ "${entry[0]}" = "${1}" ] ; then
      echo "${entry[1]}"
      return 0
    fi
    i=$((i+1))
  done
}

#
# Converts a key=value string into a list
#
# Args: key=value
#
key_val_as_list(){
    local param_key=`echo ${1} | cut -d '=' -f1`
    local param_value=`echo ${1} | cut -d '=' -f2`
    echo "${param_key} ${param_value}"
}

#
# Returns the build parameters of a given test job run
# As space separated string of PARAM_NAME=PARAM_VALUE
#
# Args: BUILD_NUMBER
#
get_test_job_params(){
  local url="${GLASSFISH_REMOTE_QUEUE_URL}/${1}/api/xml?xpath=//parameter&wrapper=list"
  curl "${url}" | sed \
      -e s@'<list><parameter>'@@g -e s@'</list>'@@g \
      -e s@'<parameter>'@@g -e s@'</parameter>'@@g \
      -e s@'<name>'@@g -e s@'</name>'@@g \
      -e s@'<value>'@'='@g -e s@'</value>'@' '@g -e s@'<value/>'@' '@g 


  if [ ${PIPESTATUS[0]} -ne 0 ] ; then
    # exit with curl status code
    return ${PIPESTATUS[0]}
  fi
}

#
# Returns true if the test job was triggered by the current PARENT_ID and given TEST_ID
#
# Args: params array to match the job
#
is_test_job_match(){
  local array=${2}
  local match_params=(${array[*]})
  set +e
  local job_param=(`get_test_job_params ${1}`)
  local error_code=${?}
  set -e

  if [ ${error_code} -ne 0 ] ; then
    # no match
    echo false
    return 0
  fi
  # match provided params with job params
  local i=0
  while [ ${i} -lt ${#match_params[*]} ]
  do
    local match_entry=(`key_val_as_list  ${match_params[${i}]}`)
    job_value=`get_value_from_key_val_array ${match_entry[0]} "${job_param[*]}"`
    if [ "${job_value}" != "${match_entry[1]}" ] ; then
      echo false
      return 0
    fi
    i=$((i+1))
  done
  # match
  echo true
}

#
# Gets the last build number of the test job.
# This will include the currently on-going runs.
#
get_test_job_last_build_number(){
  local url="${GLASSFISH_REMOTE_QUEUE_URL}/api/xml?xpath=//lastBuild/number/text()"
  curl "${url}"
  local error_code=${?}
  if [ ${error_code} -ne 0 ] ; then
    exit 1
  fi
}

#
# Find a test job for TEST_ID
#
# Args: TEST_ID PREVIOUS_LAST_BUILD
#
find_test_job(){
    local previous_last_build=${1}
    local last_build=`get_test_job_last_build_number`

    # nothing running and nothing new completed
    if [ "${previous_last_build}" = "${last_build}" ] ; then
      return 1
    fi

    # look into the newly completed run
    local i=$((previous_last_build+1))
    while [ ${i} -le ${last_build} ]
    do
      local params
      params[0]="BRANCH=${branch}"
      params[1]="TEST_IDS=${test_ids_triggerd}"
      params[2]="PR_NUMBER"
      params[3]="FORK_ORIGIN=${fork_origin}"
      if `is_test_job_match ${i} "${params[*]}"` ; then
        # the triggered run is already completed
        echo ${i}
        return 0
      fi
      i=$((i+1))
    done

    # not found
    return 1
}

USAGE="Usage:\n\n 1. rq.sh -l ---> List all available test identifiers without running them\n\
	   2. rq.sh -b -a ---> For running all tests\n\
	   3. rq.sh -b -g <test_group_name> ---> For running a test group\n\
	   4. rq.sh -b -t \"<test_id1> <test_id2> <test_id3>\" ---> For running a space separated list of tests"

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
while getopts ":b:t:g:al" opt; do
    case "$opt" in
    b)	branch=$OPTARG;;
    t)  test_ids=($OPTARG);;
    a)  test_ids=(`list_test_ids`);;
	  g)  test_ids=(`list_group_test_ids $OPTARG`);;
	  l)	test_ids=(`list_test_ids`)
		echo ${test_ids[@]} | tr " " "\n"
		exit 0;;
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
fork_origin=`git config --get remote.origin.url`
test_ids_encoded=`echo ${test_ids[@]} | tr ' ' '+'`
params="BRANCH=${branch}&TEST_IDS=${test_ids_encoded}&FORK_ORIGIN=${fork_origin}"
last_build=`get_test_job_last_build_number`
curl -X POST "${GLASSFISH_REMOTE_QUEUE_URL}/buildWithParameters?${params}&delay=0sec" 2> /dev/null
test_ids_triggerd=`echo ${test_ids[@]}`
export branch fork_origin test_ids_triggerd
job_build_number=`find_test_job ${last_build}`
printf "###################################################################\n"
printf "###################################################################\n"
if [[ ! -z ${job_build_number} ]]; then
	printf "RQ triggered successfully. Please find the RQ link below\n"
	printf ${GLASSFISH_REMOTE_QUEUE_URL}/${job_build_number}
	printf "\n"
	printf "###################################################################\n"
	printf "###################################################################\n"
else
	printf "Issue in RQ client.Please check your git settings\n"
	printf "###################################################################\n"
	printf "###################################################################\n"
fi
