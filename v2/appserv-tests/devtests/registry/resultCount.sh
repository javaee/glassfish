#!/bin/sh

FILE=./registry.output
echo "input file=$FILE"

TOTAL=3
PASSED=`grep "PASSED" $FILE | wc -l`
FAILED=`grep "FAIL" $FILE | wc -l`
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

echo "************************">>$APS_HOME/devtests/registry/count.txt;
date>>$APS_HOME/devtests/registry/count.txt;
echo "-----------------------">>$APS_HOME/devtests/registry/count.txt;
echo "PASSED=   $PASSED">>$APS_HOME/devtests/registry/count.txt;
echo "------------  =========">>$APS_HOME/devtests/registry/count.txt;
echo "FAILED=   $FAILED">>$APS_HOME/devtests/registry/count.txt;
echo "------------  =========">>$APS_HOME/devtests/registry/count.txt;
echo "DID NOT RUN=   $DNR">>$APS_HOME/devtests/registry/count.txt;
echo "------------  =========">>$APS_HOME/devtests/registry/count.txt;
echo "Total Expected=$TOTAL">>$APS_HOME/devtests/registry/count.txt;
echo "************************">>$APS_HOME/devtests/registry/count.txt;
