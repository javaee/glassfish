#!/bin/sh

FILE=$APS_HOME/test_resultsValid.xml
echo "input file=$FILE"

TOTAL=303
TOTAL_LITE=30

if [ $# -eq 1 ] && [ $1 = "lite" ]
then TOTAL=$TOTAL_LITE
echo "EJB Lite Test"
fi

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

echo "************************">>$APS_HOME/devtests/ejb/count.txt;
date>>$APS_HOME/devtests/ejb/count.txt;
echo "-----------------------">>$APS_HOME/devtests/ejb/count.txt;
echo "PASSED=   $PASSED">>$APS_HOME/devtests/ejb/count.txt;
echo "------------  =========">>$APS_HOME/devtests/ejb/count.txt;
echo "FAILED=   $FAILED">>$APS_HOME/devtests/ejb/count.txt;
echo "------------  =========">>$APS_HOME/devtests/ejb/count.txt;
echo "DID NOT RUN=   $DNR">>$APS_HOME/devtests/ejb/count.txt;
echo "------------  =========">>$APS_HOME/devtests/ejb/count.txt;
echo "Total Expected=$TOTAL">>$APS_HOME/devtests/ejb/count.txt;
echo "************************">>$APS_HOME/devtests/ejb/count.txt;
