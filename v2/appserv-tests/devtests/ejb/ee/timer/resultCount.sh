#!/bin/sh

FILE=$APS_HOME/test_resultsValid.xml
echo "input file=$FILE"

TOTAL=19

PASSED=`grep "status value" $FILE | grep "pass" | wc -l`
FAILED=`grep "status value" $FILE | grep "fail" | wc -l`
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

rm $APS_HOME/devtests/ejb/ee/timer/count.txt
touch $APS_HOME/devtests/ejb/ee/timer/count.txt

echo "PASSED=$PASSED">>$APS_HOME/devtests/ejb/ee/timer/count.txt
echo "FAILED=$FAILED">>$APS_HOME/devtests/ejb/ee/timer/count.txt
echo "DNR=$DNR">>$APS_HOME/devtests/ejb/ee/timer/count.txt
echo "TOTAL=$TOTAL">>$APS_HOME/devtests/ejb/ee/timer/count.txt
