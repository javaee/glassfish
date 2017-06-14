#!/bin/sh

FILE=/space/selfmanagementResult.txt
echo "input file=$FILE"

TOTAL=12
PASSED=`grep "PASSED" $FILE | wc -l`
FAILED=`grep "FAILED" $FILE | wc -l`
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
echo "Please see /space/selfmanagementResult.txt for details"

