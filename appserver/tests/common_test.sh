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


# OS-specific section
if [ `uname | grep -i "sunos" | wc -l | awk '{print $1}'` -eq 1 ] ; then
  GREP="ggrep"
  AWK="gawk"
  SED="gsed"
  BC="gbc"
  export PATH=/gf-hudson-tools/bin:${PATH}
else
  GREP="grep"
  AWK="awk"
  SED="sed"
  BC="bc"
fi
export GREP AWK SED BC

kill_clean(){
    if [ ${#1} -ne 0 ]
    then
        kill -9 ${1} || true
    fi
}

kill_process(){
	printf "\n%s \n\n" "===== KILL THEM ALL ====="
    kill_clean `jps | grep ASMain | awk '{print $1}'`
    kill_clean `jps | grep DerbyControl | awk '{print $1}'`
    kill_clean `jps | grep DirectoryServer | awk '{print $1}'`
}
test_init(){
	printf "\n%s \n\n" "===== V2 DEV TESTS INIT ====="
	S1AS_HOME=$WORKSPACE/glassfish5/glassfish; export S1AS_HOME
	ANT_HOME=/net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/ant-1.7.1; export ANT_HOME
	APS_HOME=$WORKSPACE/main/appserver/tests/appserv-tests; export APS_HOME
	TEST_RUN_LOG=tests-run.log; export TEST_RUN_LOG
  export M2_HOME=$MAVEN_3_0_3
        #workaround for OSGI timestamp issue
        find $S1AS_HOME -type f | xargs touch > /dev/null
	echo S1AS_HOME is $S1AS_HOME
	echo ANT_HOME is $ANT_HOME
  echo M2_HOME is $M2_HOME
	echo APS_HOME is $APS_HOME
	PATH=$M2_HOME/bin:$ANT_HOME/bin:$PATH; export PATH
	java -version
	ant -version
	rm -rf $WORKSPACE/results
	mkdir -p $WORKSPACE/results/junitreports
}

ql_init(){
	printf "\n%s \n\n" "===== QUICK LOOK INIT ====="
	export M2_HOME=$MAVEN_3_0_3
	export PATH=$MAVEN_3_0_3/bin:$JAVA_HOME/bin:/usr/bin:/usr/local/bin:/usr/home/java_re/bin:$PATH
	TEST_RUN_LOG=tests-run.log; export TEST_RUN_LOG
	java -version
	mvn -version
	rm -rf $WORKSPACE/results
	mkdir -p $WORKSPACE/results/junitreports
}

download_test_resources(){
	printf "\n%s \n\n" "===== DOWNLOAD TEST RESOURCES ====="
	for i in "$@"; do
		echo downloading $i
		scp -o "StrictHostKeyChecking no" ${PARENT_NODE}:${PARENT_WS_PATH}/bundles/$i bundles
	done
}

zip_test_results(){
	printf "\n%s \n\n" "===== ZIP THE TESTS RESULTS ====="
    zip -r $WORKSPACE/results.zip $WORKSPACE/results > /dev/nul
}

upload_test_results(){
	printf "\n%s \n\n" "===== UPLOADING THE TESTS RESULTS ====="
	scp -o "StrictHostKeyChecking no" -r $WORKSPACE/results/ ${PARENT_NODE}:${PARENT_WS_PATH}/test-results/$TEST_ID/
}

unzip_test_resources(){
	printf "\n%s \n\n" "===== UNZIP TEST RESOURCES ====="
	for i in "$@"; do
		unzip $i > /dev/null
	done
}


copy_test_artifects(){
	printf "\n%s \n\n" "===== COPY TEST ARTIFECTs ====="
        zip -r $WORKSPACE/results/domainArchive.zip $S1AS_HOME/domains
	cp $S1AS_HOME/domains/domain1/logs/server.log* $WORKSPACE/results/ || true
	cp $TEST_RUN_LOG $WORKSPACE/results/
	cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/
	cp $APS_HOME/test_results*.* $WORKSPACE/results/ || true
	cp `pwd`/*/*logs.zip $WORKSPACE/results/ || true
	cp `pwd`/*/*/*logs.zip $WORKSPACE/results/ || true
}


generate_junit_report(){
	printf "\n%s \n\n" "===== GENERATE JUNIT REPORT ====="
	TD=$APS_HOME/test_resultsValid.xml
	JUD=$APS_HOME/test_results_junit.xml
	TESTSUITE_NAME=$1

	cat ${TD} | ${AWK} -v suitename=${TESTSUITE_NAME} '
	  BEGIN {
	    totaltests = 0;
	    totalfailures = 0;
	    totalerrors = 0;
	  }
	  function getPropVal(str){
	    split(str, a, "=");
	    val = a[2];
	    # remove quotes
	    gsub("\"","",val);
	    return val;
	  }
	   function removeXMLTag(str){  
	    # remove xml tag quotes
	    gsub("</.*>","",str);
	    gsub("<.*>","",str);
	    gsub(">","",str);
	    return str;
	  }
	  /status value=/ {
	    result=getPropVal($0);
	    result=removeXMLTag(result);
	  }
	  /<testsuite>/ {
	    getline;
	    getline;
	    testunit=removeXMLTag($0);
	    gsub("\"","",testunit);
	  }
	  /<testcase>/ {
	    getline;
	    testname=removeXMLTag($0);
	    gsub("\"","",testname);
	  }
	  /<\/testcase>/{
	    classname=testunit
	    # printing testcase to out
	    out = out sprintf(" <testcase classname=\"%s\" name=\"%s\" time=\"0.0\">\n", classname, testname);
	    if (result == "fail") {
	     out = out "  <failure message=\"NA\" type=\"NA\"/>\n";
	     totalfailures++;
	    } else if (result == "did_not_run") {
	     out = out "  <error message=\"NA\" type=\"NA\"/>\n";
	     totalerrors++;
	    }
	    out = out " </testcase>\n";

	    totaltests++;
	    result="";
	    testname="";
	  }
	  END {
	    print "<?xml version=\"1.0\" ?>"
	    printf "<testsuite tests=\"%d\" failures=\"%d\" errors=\"%d\" name=\"%s\">\n", totaltests, totalfailures, totalerrors, suitename;
	    printf "%s", out;
	    print "</testsuite>"
	  }' > ${JUD}
	cp $JUD $WORKSPACE/results/junitreports
}

change_junit_report_class_names(){
  ${SED} -i 's/\([a-zA-Z-]\w*\)\./\1-/g' $WORKSPACE/results/junitreports/*.xml
  ${SED} -i "s/\bclassname=\"/classname=\"${TEST_ID}./g" $WORKSPACE/results/junitreports/*.xml
}


check_successful_run(){
	printf "\n%s \n\n" "===== CHECK SUCCESSFUL RUN ====="
	#checking that test_results.html is generated to make sure the build is not failed
	FILE=$APS_HOME/test_results.html
	if [ -f $FILE ];then
   		echo "File $FILE exists.Test build successful"
	else
   		echo "File $FILE does not exist.There is problem in test build."
   exit 1
fi
}

delete_gf(){
	printf "\n%s \n\n" "===== DELETE GLASSFISH AND MAVEN LOCAL REPO AND NUCLEUS ====="
    rm -rf $WORKSPACE/glassfish5
    rm -rf $WORKSPACE/repository
    rm -rf $WORKSPACE/nucleus
} 

delete_bundle(){
	printf "\n%s \n\n" "===== DELETE BUNDEL ====="
	rm -rf $WORKSPACE/bundles
}
