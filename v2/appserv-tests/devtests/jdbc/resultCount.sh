#!/bin/sh

total=101
OUTPUT_FILE=jdbc.output

for each in `grep "Total PASS" ${OUTPUT_FILE} | awk '{print $4}'`
do
     pass=$(( $pass + $each ))
done

for each in `grep "Total FAIL" ${OUTPUT_FILE} | awk '{print $4}'`
do
     fail=$(( $fail + $each ))
done

echo "------------------------"
echo "TOTAL = ${total}"
echo "------------------------"
echo "PASSED = ${pass}"
echo "FAILED = ${fail}"
echo "------------------------"

