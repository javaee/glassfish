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


copyright_run(){
  M2_HOME=/net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/apache-maven-3.0.3
  MAVEN_OPTS="-Xmx512m -Xms256m -XX:MaxPermSize=512m"; export MAVEN_OPTS
  MAVEN_REPO=$WORKSPACE/repository
  MAVEN_SETTINGS=$M2_HOME/settings-nexus.xml
  PATH=$M2_HOME/bin:$JAVA_HOME/bin:$PATH; export PATH
  mvn -version
  echo $WORKSPACE
	rm -f $WORKSPACE/main/copyright-files.txt || true
	rm -f $WORKSPACE/copyright-files-temp*.txt || true
	rm -rf $WORKSPACE/main/tmp-users || true
	cd $WORKSPACE/main

	# TODO move the copyright module in main and main's default reactor in a profile, in order to not trigger the default reactor.
	mvn -e -s $MAVEN_SETTINGS -Dmaven.repo.local=$MAVEN_REPO --quiet -Dcopyright.normalize=true org.glassfish.copyright:glassfish-copyright-maven-plugin:copyright > $WORKSPACE/copyright-files-temp-open.txt
	cat $WORKSPACE/copyright-files-temp-open.txt
	cat $WORKSPACE/copyright-files-temp-open.txt | sed s@$PWD/@@g > copyright-files.txt
}

generate_copyright_results(){
  rm -rf $WORKSPACE/results || true
  mkdir -p $WORKSPACE/results/copyright_results

	num=`wc -l copyright-files.txt | awk '{print $1}'`	
	if [ $num -gt 0 ];then	
	  echo "UNSTABLE" > $WORKSPACE/results/copyright_results/copyrightcheck.log
	else
	  echo "SUCCESS" > $WORKSPACE/results/copyright_results/copyrightcheck.log
	fi
  cp copyright-files.txt $WORKSPACE/results/copyright_results/copyright-files.txt
}

run_test_id(){
  source `dirname $0`/../common_test.sh
  kill_process
  rm main.zip rm version-info.txt || true
  download_test_resources main.zip version-info.txt
  rm -rf main || true
  rm -rf .git || true
  unzip_test_resources "$WORKSPACE/bundles/main.zip -d main/"
  copyright_run
  generate_copyright_results
}

post_test_run(){
    if [[ $? -ne 0 ]]; then
      generate_copyright_results
    fi
    upload_test_results
    delete_bundle
}

list_test_ids(){
	echo copyright
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
