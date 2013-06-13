#!/bin/sh

#FILE=$APS_HOME/devtests/cdi/cdi.output
FILE=$APS_HOME/test_resultsValid.xml
echo "input file=$FILE"

TOTAL=128
PASSED=`grep "pass" $FILE | wc -l`
FAILED=`grep "fail" $FILE | wc -l`
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

echo "************************">>$APS_HOME/devtests/cdi/count.txt;
date>>$APS_HOME/devtests/cdi/count.txt;
echo "-----------------------">>$APS_HOME/devtests/cdi/count.txt;
echo "PASSED=   $PASSED">>$APS_HOME/devtests/cdi/count.txt;
echo "------------  =========">>$APS_HOME/devtests/cdi/count.txt;
echo "FAILED=   $FAILED">>$APS_HOME/devtests/cdi/count.txt;
echo "------------  =========">>$APS_HOME/devtests/cdi/count.txt;
echo "DID NOT RUN=   $DNR">>$APS_HOME/devtests/cdi/count.txt;
echo "------------  =========">>$APS_HOME/devtests/cdi/count.txt;
echo "Total Expected=$TOTAL">>$APS_HOME/devtests/cdi/count.txt;
echo "************************">>$APS_HOME/devtests/cdi/count.txt;
