#!/bin/sh

skip() {
    FILE=$1
    echo Parsing ${FILE}
    cat ${FILE} | while read LINE
    do
        NAME=${LINE%% *}
        echo excluding \"${NAME}\"
        sed -i -e "s@<ant dir=\"${NAME}\" target=\"all\"/>@<!--&-->@" build.xml
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
