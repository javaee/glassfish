#!/bin/sh

skip() {
    FILE=$1
    echo Parsing ${FILE}
    cat ${FILE} | while read LINE
    do
        NAME=`echo $LINE | sed -e 's/[   ].*//'`
        if [ -d "${NAME}" ]
        then
            echo excluding \"${NAME}\"
            sed -e "s@^ *<ant dir=\"${NAME}\" target=\"all\"/>@<!--&-->@" build.xml > build.xml.sed
            mv build.xml.sed build.xml
        else
            if [ ! -z "${NAME}" ]
            then
                echo "***** ${NAME} is not a valid test directory *****"
            fi
        fi
    done
}

echo start
if [ -z "${JOB_NAME}" -o "$JOB_NAME" = "webtier-dev-tests-v3-source" ]
then
    JOB_NAME=webtier-dev-tests-v3
fi

if [ -f "${JOB_NAME}.skip" ]
then
    skip ${JOB_NAME}.skip
fi

