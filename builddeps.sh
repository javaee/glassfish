#!/bin/bash
set -x

if [ -e trinidad ] ; then
    svn up trinidad
else
    svn co https://svn.apache.org/repos/asf/myfaces/trinidad/trunk trinidad
    cd trinidad
    patch -p0 < ../trinidad.patch
    cd trinidad-build
    mvn -Dmaven.test.skip=true install
    cd -
fi

cd trinidad
mvn -Dmaven.test.skip=true -f trinidad-api/pom.xml install
mvn -Dmaven.test.skip=true -f trinidad-impl/pom.xml install
