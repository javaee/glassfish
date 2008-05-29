#!/bin/sh +x
# Change the maven local repo path as per your environment
MVN_LOCAL_REPO=$HOME/.m2/repository

MVN_HK2_PATH=$MVN_LOCAL_REPO/com/sun/enterprise/
VERSION=0.3.2-SNAPSHOT
CLASSPATH=$MVN_HK2_PATH/hk2-dependency-verifier/$VERSION/hk2-dependency-verifier-$VERSION.jar:$MVN_HK2_PATH/hk2-core/$VERSION/hk2-core-$VERSION.jar:$MVN_HK2_PATH/auto-depends/$VERSION/auto-depends-$VERSION.jar:$MVN_LOCAL_REPO/org/apache/bcel/bcel/5.2/bcel-5.2.jar

java $JDEBUG -DExcludedPatterns="javax." -cp $CLASSPATH -DdebugOutput=/tmp/closure.txt com.sun.enterprise.tools.verifier.hk2.PackageAnalyser $*
