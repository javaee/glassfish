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

archive_cts(){
	cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/
	cp $TS_HOME/bin/xml/config_vi.log $WORKSPACE/results
	cp $TS_HOME/bin/xml/smoke.log $WORKSPACE/results
	cp $S1AS_HOME/domains/domain1/logs/server.log* $WORKSPACE/results
	cp $TS_HOME/bin/ts.jte $WORKSPACE/results
	echo $BUILD_ID > $WORKSPACE/results/count.txt
	${GREP} "Number of Tests Passed" $WORKSPACE/results/smoke.log >> $WORKSPACE/results/count.txt
	${GREP} "Number of Tests Failed" $WORKSPACE/results/smoke.log >> $WORKSPACE/results/count.txt
	${GREP} "Number of Tests with Errors" $WORKSPACE/results/smoke.log >> $WORKSPACE/results/count.txt
	cat count.txt | ${SED} -e 's/\[javatest.batch\] Number/Number/g' > $WORKSPACE/results/CTS-GP-count.txt
	rm $WORKSPACE/results/count.txt
}

test_run_cts_smoke(){
	TS_HOME=$WORKSPACE/javaee-smoke
	CTS_SMOKE=http://busgo1208.us.oracle.com/JWSCQE/links/builds/tcks/javaee_cts/8/promoted/
	CTS_SMOKE_BUNDLE=javaee-smoke-8.0_latest.zip
	CTS_EXCLUDE_LIST=ts.jtx

	# MACHINE CONFIGURATION
	pwd
	uname -a
	java -version

	# some clean up
	rm -rf /tmp/JTreport
	rm -rf /tmp/JTwork
	rm -rf /disk1/java_re/.javatest

	# XXX Trying this as a test - touch all the files in the glassfish distribution
	# find glassfish5 -exec touch {} \;
	# XXX End test

	wget $CTS_SMOKE/$CTS_SMOKE_BUNDLE
	unzip -q $CTS_SMOKE_BUNDLE

	cd $TS_HOME/bin
	#cp $CTS_SMOKE/$CTS_EXCLUDE_LIST .
	cp ts.jte ts.jte.orig

	#mv ts.jte ts.jte.orig
	# 08/31/2012 [jill] This ${SED} command includes the addition of javax.jms-api.jar (to support new JMS 2.0 jar) in front of old javax.jms.jar.
	${SED} -e "s@javaee.home=@javaee\.home=$S1AS_HOME@g" -e "s@javaee.home.ri=@javaee\.home\.ri=$S1AS_HOME@g" -e "s/^orb\.host=/orb\.host=localhost/g"  -e "s/^mailHost=/mailHost=localhost/g" -e "s/^mailuser1=/mailuser1=java_re@sun\.com/g" -e "s/^mailFrom=.*/mailFrom=java_re@sun\.com/g" -e "s/orb.host.ri=/orb.host.ri=localhost/g" -e "s/^work\.dir=\/files/work\.dir=\/tmp/g" -e "s/^report\.dir=\/files/report\.dir=\/tmp/g" -e "s/^tz=.*/tz=US\/Pacific/g" -e "s/modules\/gf-client.jar/lib\/gf-client.jar/g" -e "s/\${pathsep}\${ri\.modules}\/javax\.jms\.jar/\${pathsep}\${ri\.modules}\/javax\.jms-api\.jar\${pathsep}\$\{ri\.modules}\/javax\.jms\.jar/g" -e "s/\${pathsep}\${s1as\.modules}\/javax\.jms\.jar/\${pathsep}\${s1as\.modules}\/javax\.jms-api\.jar\${pathsep}\$\{s1as\.modules}\/javax\.jms\.jar/g" ts.jte > ts.jte.new
	mv ts.jte.new ts.jte


	# Temp fix for weld [06/06/2014 jlsato]
	${SED} -e "s/implementation\.classes\.ri=/implementation\.classes\.ri=\${ri\.modules}\/cdi-api\.jar\${pathsep}\${ri\.modules}\/cdi-api-fragment\.jar\${pathsep}/g" ts.jte > ts.jte.new
	mv ts.jte.new ts.jte
	${SED} -e "s/implementation\.classes=/implementation\.classes=\${s1as\.modules}\/cdi-api\.jar\${pathsep}\${s1as\.modules}\/cdi-api-fragment\.jar\${pathsep}/g" ts.jte > ts.jte.new
	mv ts.jte.new ts.jte
	# End temp fix for weld

	# Temp fix for Pavel   [10/31/2013 jlsato]
	#${SED} -e "s/implementation\.classes\.ri=/implementation\.classes\.ri=\${ri\.modules}\/tyrus-container-grizzly-client\.jar\${pathsep}/g" ts.jte > ts.jte.new
	#mv ts.jte.new ts.jte
	#${SED} -e "s/implementation\.classes=/implementation\.classes=\${s1as\.modules}\/tyrus-container-grizzly-client\.jar\${pathsep}/g" ts.jte > ts.jte.new
	#mv ts.jte.new ts.jte
	# Fix the 02_Dec smoketest bundle [12/03/2013 jlsato]
	${SED} -e "s/tyrus-container-grizzly\.jar/tyrus-container-grizzly-client\.jar/g" ts.jte > ts.jte.new
	mv ts.jte.new ts.jte
	# End temp fix for Pavel

	# Temp fix to set javamail password [12/03/2013 jlsato]
	${SED} -e "s/javamail\.password=/javamail\.password\=cts1/g" ts.jte > ts.jte.new
	mv ts.jte.new ts.jte
	# End temp fix for javamail password

	cd $TS_HOME/bin/xml
	export ANT_HOME=$TS_HOME/tools/ant
	export PATH=$ANT_HOME/bin:$PATH

	# SECURITY MANAGER ON
	$S1AS_HOME/bin/asadmin start-domain
	$S1AS_HOME/bin/asadmin create-jvm-options "-Djava.security.manager"
	$S1AS_HOME/bin/asadmin stop-domain

	$TS_HOME/tools/ant/bin/ant -Dreport.dir=$WORKSPACE/$BUILD_NUMBER/JTReport -Dwork.dir=$WORKSPACE/$BUILD_NUMBER/JTWork -f smoke.xml smoke

	#POST CLEANUPS
	kill_process

	#ARCHIVING
	archive_cts
}

archive_servlet_tck(){
	cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/
	cp $S1AS_HOME/domains/domain1/logs/server.log* $WORKSPACE/results
	cp $WORKSPACE/tests.log $WORKSPACE/results
	cp -r $TS_HOME/report/ $WORKSPACE/results
}

test_run_servlet_tck(){
	export TS_HOME=$WORKSPACE/servlettck
	java -version
	# Java EE 8 servlet tck.
	wget http://busgo1208.us.oracle.com/JWSCQE/links/builds/tcks/javaee_cts/8/nightly/servlettck-4.0_Latest.zip -O servlettck.zip

	unzip -q servlettck.zip

	cd $TS_HOME/bin
	cp ts.jte ts.jte.orig

	cat ts.jte.orig | ${SED} \
	-e "s@webServerHost=@webServerHost=localhost@g" \
	-e "s@webServerPort=@webServerPort=8080@g" \
	-e "s@securedWebServicePort=@securedWebServicePort=8181@g" \
	-e "s@web.home=@web\.home=$S1AS_HOME@g" \
	-e "s@javaee\.home\.ri=@javaee\.home\.ri=$S1AS_HOME@g" \
	-e "s/^orb\.host=/orb\.host=localhost/g"  -e "s/^mailHost=/mailHost=localhost/g" -e "s/^mailuser1=/mailuser1=java_re/g" \
	-e "s/^mailFrom=.*/mailFrom=javaee-re_ww@oracle\.com/g" -e "s/orb.host.ri=/orb.host.ri=localhost/g" \
	-e "s/^work\.dir=\/files/work\.dir=\/tmp/g" -e "s/^report\.dir=\/files/report\.dir=\/tmp/g" \
	-e "s/^tz=.*/tz=US\/Pacific/g" -e "s/modules\/gf-client.jar/lib\/gf-client.jar/g" \
	-e "s/\${pathsep}\${ri\.modules}\/javax\.jms\.jar/\${pathsep}\${ri\.modules}\/javax\.jms-api\.jar\${pathsep}\$\{ri\.modules}\/javax\.jms\.jar/g" \
	-e "s/\${pathsep}\${s1as\.modules}\/javax\.jms\.jar/\${pathsep}\${s1as\.modules}\/javax\.jms-api\.jar\${pathsep}\$\{s1as\.modules}\/javax\.jms\.jar/g" \
	-e "s/implementation\.classes\.ri=/implementation\.classes\.ri=\${ri\.modules}\/cdi-api\.jar\${pathsep}\${ri\.modules}\/cdi-api-fragment\.jar\${pathsep}/g" \
	-e "s/implementation\.classes=/implementation\.classes=\${s1as\.modules}\/cdi-api\.jar\${pathsep}\${s1as\.modules}\/cdi-api-fragment\.jar\${pathsep}/g" \
	-e "s/implementation\.classes\.ri=/implementation\.classes\.ri=\${ri\.modules}\/tyrus-container-grizzly-client\.jar\${pathsep}/g" \
	-e "s/implementation\.classes=/implementation\.classes=\${s1as\.modules}\/tyrus-container-grizzly-client\.jar\${pathsep}/g" \
	-e "s/tyrus-container-grizzly\.jar/tyrus-container-grizzly-client\.jar/g" \
	-e "s/impl\.vi=/impl\.vi\=glassfish/g" \
	> ts.jte

	echo "# Disabling signature tests for CI build pipeline" >> ts.jtx
	echo "com/sun/ts/tests/signaturetest/servlet/ServletSigTest.java#signatureTest" >> ts.jtx

	cd $S1AS_HOME
	bin/asadmin start-domain

	cd $TS_HOME/bin
	ant config.security
	ant deploy.all

	cd $TS_HOME/src/com/sun/ts/tests
	(ant runclient -Dreport.dir=$WORKSPACE/servlettck/report | tee $WORKSPACE/tests.log) || true

	cd $S1AS_HOME
	bin/asadmin stop-domain

	#POST CLEANUPS
	kill_process

	#ARCHIVING
	archive_servlet_tck
}

run_test_id(){
	source `dirname $0`/../common_test.sh
	kill_process
	delete_workspace
	download_test_resources glassfish.zip version-info.txt
	unzip_test_resources $WORKSPACE/bundles/glassfish.zip
	test_init
	if [[ $1 = "cts_smoke_all" ]]; then
		test_run_cts_smoke
		result=$WORKSPACE/results/smoke.log
	elif [[ $1 = "servlet_tck_all" ]]; then
		test_run_servlet_tck
		result=$WORKSPACE/results/tests.log
	else
		echo "Invalid Test ID"
		exit 1
	fi
    cts_to_junit $result $WORKSPACE/results/junitreports/test_results_junit.xml $1
}

post_test_run(){
    if [[ $? -ne 0 ]]; then
    	if [[ $TEST_ID = "cts_smoke_all" ]]; then
	  		archive_cts || true
	  	fi
	  	if [[ $TEST_ID = "servlet_tck_all" ]]; then
	  		archive_servlet_tck || true
	  	fi
	fi
    upload_test_results
    delete_bundle
    cd -
}


list_test_ids(){
	echo cts_smoke_all servlet_tck_all
}

cts_to_junit(){
        junitCategory=$3
	cd $WORKSPACE/results
	rm -rf $2
	cat $1 | ${GREP} -a "\[javatest.batch\] Finished Test" >  results.txt
	tail $((`${GREP} -n "Completed running" results.txt | ${AWK} '{print $1}' | cut -d ':' -f1`-`cat results.txt | wc -l`)) results.txt > summary.txt

	echo "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" >> $2
	echo "<testsuites>" >> $2
	echo "	<testsuite>" >> $2
	for i in `${GREP} "\.\.\.\.\.\.\.\." summary.txt | ${AWK} '{print $4}'`
	do
		line=`echo $i | ${SED} s@"\.\.\.\.\.\.\.\."@" "@g`
		status=`echo $line | ${AWK} '{print $1}'`
		id=`echo $line | ${AWK} '{print $2}'`
		classname=`echo $id | cut -d '#' -f1 | ${SED} s@"\/"@"_"@g | ${SED} s@".java"@@g`
		name=`echo $id | cut -d '#' -f2`

		echo "		<testcase classname=\"${junitCategory}.$classname\" name=\"$name\">" >> $2		
		if [ "${status}" = "FAILED" ]
		then
			echo "			<failure type=\"CtsFailure\"> n/a </failure>" >> $2
		fi
			echo "		</testcase>" >> $2
	done
	echo "	</testsuite>" >> $2
	echo "</testsuites>" >> $2
}

delete_workspace(){
	printf "\n%s \n\n" "===== DELETE WORKSPACE ====="
    rm -rf $WORKSPACE/glassfish5 > /dev/null || true
    rm -rf $WORKSPACE/servlettck > /dev/null  || true
    rm $WORKSPACE/servlettck.zip > /dev/null || true
    rm -rf $WORKSPACE/javaee-smoke > /dev/null || true
    rm $WORKSPACE/javaee-smoke-7.0_latest.zip > /dev/null || true
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
