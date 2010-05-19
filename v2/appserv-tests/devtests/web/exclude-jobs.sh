#!/bin/sh

skip() {
    FILE=$1
    echo Parsing ${FILE}
    cat ${FILE} | while read LINE
    do
        if [ "${LINE:0:1}" != "#" ]
        then
            NAME=${LINE%% *}
            echo excluding \"${NAME}\"
            sed -i -e "s@<ant dir=\"${NAME}\" target=\"all\"/>@<!--&-->@" build.xml
        fi
    done
}

if [ -z "${JOB_NAME}" ]
then
    echo no JOB_NAME defined.  using the default of webtier-dev-tests-v3
    JOB_NAME=webtier-dev-tests-v3
fi
if [ -e "${JOB_NAME}.skip" ]
then
    skip ${JOB_NAME}.skip
fi
