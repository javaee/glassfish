#!/bin/sh

FILE=$APS_HOME/test_resultsValid.xml
echo "input file=$FILE"

TOTAL=226
PASSED=`grep "\"pass\"" $FILE | wc -l`
FAILED=`grep "\"fail\"" $FILE | wc -l`
TOTAL_RUN=`expr $PASSED + $FAILED `
DNR=`expr $TOTAL - $TOTAL_RUN `

echo "------------------------"
echo "TOTAL = ${TOTAL}"
echo "------------------------"
echo "PASSED = ${PASSED}"
echo "FAILED = ${FAILED}"
echo "DID NOT RUN = ${DNR}"
echo "------------------------"

echo "************************">>$APS_HOME/devtests/jdbc/count.txt;
date>>$APS_HOME/devtests/jdbc/count.txt;
echo "-----------------------">>$APS_HOME/devtests/jdbc/count.txt;
echo "PASSED=   $PASSED">>$APS_HOME/devtests/jdbc/count.txt;
echo "------------  =========">>$APS_HOME/devtests/jdbc/count.txt;
echo "FAILED=   $FAILED">>$APS_HOME/devtests/jdbc/count.txt;
echo "------------  =========">>$APS_HOME/devtests/jdbc/count.txt;
echo "DID NOT RUN=   $DNR">>$APS_HOME/devtests/jdbc/count.txt;
echo "------------  =========">>$APS_HOME/devtests/jdbc/count.txt;
echo "Total Expected=$TOTAL">>$APS_HOME/devtests/jdbc/count.txt;
echo "************************">>$APS_HOME/devtests/jdbc/count.txt;

