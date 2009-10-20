#!/bin/sh

FILE=$APS_HOME/test_resultsValid.xml
echo "input file=$FILE"

TOTAL=111
PASSED=`grep "pass" $FILE | wc -l`
FAILED=`grep "fail" $FILE | wc -l`
TOTAL_RUN=`expr $PASSED + $FAILED `
DNR=`expr $TOTAL - $TOTAL_RUN `

echo "------------------------"
echo "TOTAL = ${TOTAL}"
echo "------------------------"
echo "PASSED = ${PASSED}"
echo "FAILED = ${FAILED}"
echo "DID NOT RUN = ${DNR}"
echo "------------------------"

