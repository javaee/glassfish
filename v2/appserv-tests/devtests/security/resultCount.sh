#!/bin/sh

FILES="$APS_HOME/test_resultsValid.xml $APS_HOME/security-gtest-results.xml"

TOTAL=744
PASSED=0
FAILED=0
for i in $FILES
do
	echo "input file=$i"
	P=`grep "pass" $i | grep "status value" | wc -l`
	F=`grep "fail" $i | grep "status value" | wc -l`
	PASSED=`expr $PASSED + $P`
	FAILED=`expr $FAILED + $F`
done
TOTAL_RUN=`expr $PASSED + $FAILED `
DNR=`expr $TOTAL - $TOTAL_RUN `

echo ""
echo "************************"
echo "PASSED=   $PASSED"
echo "------------  ========="
echo "FAILED=   $FAILED"
echo "------------  ========="
echo "DID NOT RUN=   $DNR"
echo "------------  ========="
echo "Total Expected=$TOTAL"
echo "************************"
echo ""

echo "************************">$APS_HOME/devtests/security/count.txt;
date>>$APS_HOME/devtests/security/count.txt;
echo "-----------------------">>$APS_HOME/devtests/security/count.txt;
echo "PASSED=   $PASSED">>$APS_HOME/devtests/security/count.txt;
echo "------------  =========">>$APS_HOME/devtests/security/count.txt;
echo "FAILED=   $FAILED">>$APS_HOME/devtests/security/count.txt;
echo "------------  =========">>$APS_HOME/devtests/security/count.txt;
echo "DID NOT RUN=   $DNR">>$APS_HOME/devtests/security/count.txt;
echo "------------  =========">>$APS_HOME/devtests/security/count.txt;
echo "Total Expected=$TOTAL">>$APS_HOME/devtests/security/count.txt;
echo "************************">>$APS_HOME/devtests/security/count.txt;
