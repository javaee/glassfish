#!/bin/sh

total=28
pass=0
fail=0
OUTPUT_FILE=$ROOT/connector.output
echo "------------------------"
which grep
echo "------------------------"
which awk
echo "------------------------"
echo  ${OUTPUT_FILE}
echo "------------------------"
ps -p $$
for each in `grep "Total PASS:" ${OUTPUT_FILE} | awk '{print $4}'`
do
     pass=$(( $pass + $each ))
done

for each in `grep "Total FAIL:" ${OUTPUT_FILE} | awk '{print $4}'`
do
     fail=$(( $fail + $each ))
done

echo "------------------------"
echo "TOTAL = ${total}"
echo "------------------------"
echo "PASSED = ${pass}"
echo "FAILED = ${fail}"
echo "------------------------"

if [ $fail -gt 0 ]
then
   echo "One or More Test(s) Failed."
   exit 1;
else
   echo "Tests Successful."
   exit 0;
fi
