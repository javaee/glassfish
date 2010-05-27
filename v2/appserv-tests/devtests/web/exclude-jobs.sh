#!/bin/sh

skip() {
    FILE=$1
    echo Parsing ${FILE}
    cat ${FILE} | while read LINE
    do
        NAME=${LINE%% *}
        if [ -d "${NAME}" ]
        then
            echo excluding \"${NAME}\"
            sed -e "s@^ *<ant dir=\"${NAME}\" target=\"all\"/>@<!--&-->@" build.xml > build.xml.sed
            mv build.xml.sed build.xml
        else
            echo "***** ${NAME} is not a valid test directory *****" 
        fi
    done
}

if [ -z "${JOB_NAME}" ]
then
    echo no JOB_NAME defined.  using the default of webtier-dev-tests-v3
    JOB_NAME=webtier-dev-tests-v3
fi

JOB_NAME=${JOB_NAME%%-source}
if [ -f "${JOB_NAME}.skip" ]
then
    echo skip ${JOB_NAME}.skip
fi
