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

findbugs_run(){
	M2_HOME=/net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/apache-maven-3.0.3
	MAVEN_OPTS="-Xmx512m -Xms256m -XX:MaxPermSize=1024m"; export MAVEN_OPTS
	MAVEN_REPO=$WORKSPACE/repository
	MAVEN_SETTINGS=$M2_HOME/settings-nexus.xml

	CLASSPATH=$WORKSPACE/findbugstotext; export CLASSPATH
	PATH=$M2_HOME/bin:$JAVA_HOME/bin:$PATH; export PATH
	mvn -version
	echo $WORKSPACE

	# Run findbugs on open source ws.
	echo "Running findbugs on ws"
	cd $WORKSPACE/main
	mvn -e -s $MAVEN_SETTINGS -Dmaven.repo.local=$WORKSPACE/repository -Pfindbugs clean install
	mvn -e -s $MAVEN_SETTINGS -Dmaven.repo.local=$WORKSPACE/repository -Pfindbugs findbugs:findbugs

}

findbugs_low_priority_all_run(){
  M2_HOME=/net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/apache-maven-3.0.3
  MAVEN_OPTS="-Xmx512m -Xms256m -XX:MaxPermSize=1024m"; export MAVEN_OPTS
  MAVEN_REPO=$WORKSPACE/repository
  MAVEN_SETTINGS=$M2_HOME/settings-nexus.xml

  CLASSPATH=$WORKSPACE/findbugstotext; export CLASSPATH
  PATH=$M2_HOME/bin:$JAVA_HOME/bin:$PATH; export PATH
  mvn -version
  echo $WORKSPACE

  # Run findbugs on open source ws.
  echo "Running findbugs on ws"
  cd $WORKSPACE/main
  mvn -e -s $MAVEN_SETTINGS -Dmaven.repo.local=$WORKSPACE/repository -Pfindbugs clean install
  mvn -e -s $MAVEN_SETTINGS -Dmaven.repo.local=$WORKSPACE/repository -B -Pfindbugs -Dfindbugs.threshold=Low findbugs:findbugs

}

generate_findbugs_result(){
	rm -rf $WORKSPACE/results
	mkdir -p $WORKSPACE/results/findbugs_results

	# check findbbugs
	set +e
	cd /net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/findbugs-tool-latest; ./findbugscheck $WORKSPACE/main
	if [ $? -ne 0 ]
	then
	   echo "FAILED" > $WORKSPACE/results/findbugs_results/findbugscheck.log
	else
	   echo "SUCCESS" > $WORKSPACE/results/findbugs_results/findbugscheck.log
	fi
	set -e
	# archive the findbugs results
	for i in `find $WORKSPACE/main -name findbugsXml.xml`
	do
	   cp $i $WORKSPACE/results/findbugs_results/`echo $i | sed s@"$WORKSPACE"@@g | sed s@"/"@"_"@g`
	done
}

generate_findbugs_low_priority_all_result(){
  rm -rf $WORKSPACE/results
  mkdir -p $WORKSPACE/results/findbugs_low_priority_all_results

  # check findbbugs
  set +e
  cd /net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/findbugs-tool-latest; ./fbcheck $WORKSPACE/main
  if [ $? -ne 0 ]
  then
     echo "FAILED" > $WORKSPACE/results/findbugs_low_priority_all_results/findbugscheck.log
  else
     echo "SUCCESS" > $WORKSPACE/results/findbugs_low_priority_all_results/findbugscheck.log
  fi
  set -e
  # archive the findbugs results
  for i in `find $WORKSPACE/main -name findbugsXml.xml`
  do
     cp $i $WORKSPACE/results/findbugs_low_priority_all_results/`echo $i | sed s@"$WORKSPACE"@@g | sed s@"/"@"_"@g`
  done
}

run_test_id(){
	source `dirname $0`/../common_test.sh
	kill_process
	rm main.zip rm version-info.txt || true
	download_test_resources main.zip version-info.txt
	rm -rf main || true
	unzip_test_resources "$WORKSPACE/bundles/main.zip -d main/"
  case ${TEST_ID} in
    findbugs_all)
   	  findbugs_run
     	generate_findbugs_result;;
    findbugs_low_priority_all)
      findbugs_low_priority_all_run
      generate_findbugs_low_priority_all_result;;
  esac
}

post_test_run(){
    if [[ $? -ne 0 ]]; then
    	if [[ $TEST_ID = "findbugs_all" ]]; then
	  		generate_findbugs_result || true
	  	fi
	  	if [[ $TEST_ID = "findbugs_low_priority_all" ]]; then
	  		generate_findbugs_low_priority_all_result || true
	  	fi
	fi
    upload_test_results
    delete_bundle
}


list_test_ids(){
	echo findbugs_all
  echo findbugs_low_priority_all
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
