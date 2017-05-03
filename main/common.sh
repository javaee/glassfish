#!/bin/bash -e
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

#
# REQUIRED VARIABLES
#
#RE_USER=
#HUDSON_MASTER_HOST=
#STORAGE_HOST=
#JNET_USER=
#JNET_STORAGE_HOST=
#STORAGE_HOST_HTTP=
#WORKSPACE=
#NOTIFICATION_SENDTO
#NOTIFICATION_FROM
#GPG_PASSPHRASE
#HUDSON_HOME
HUDSON_JOB_NAME=gf-master-continous-check
# OS-specific section
if [ `uname | grep -i "sunos" | wc -l | awk '{print $1}'` -eq 1 ] ; then
  GREP="ggrep"
  AWK="gawk"
  SED="gsed"
  BC="gbc"
  export PATH=/gf-hudson-tools/bin:${PATH}
else
  GREP="grep"
  AWK="awk"
  SED="sed"
  BC="bc"
fi

export GREP AWK SED BC

build_init(){
    init_common
    kill_glassfish
    print_env_info
    create_version_info
}

build_re_finalize(){
    archive_bundles
    zip_tests_workspace
    zip_tests_maven_repo
    zip_gf_source
}

build_re_dev(){
    build_init
    dev_build
    merge_junits
    build_re_finalize
}

build_re_weekly(){
    export BUILD_KIND="weekly"
    build_init
    init_weekly
    # delete_svn_tag ${RELEASE_VERSION}
    # svn_checkout ${SVN_REVISION}
    release_prepare
    release_build "clean deploy" "release-phase2,embedded,javaee-api"
    build_re_finalize
    clean_and_zip_workspace
}

init_weekly(){
    BUILD_KIND="weekly"
    require_env_var "GPG_PASSPHRASE"

    ARCHIVE_PATH=${PRODUCT_GF}/${PRODUCT_VERSION_GF}
    if [ ${#BUILD_ID} -gt 0 ]
    then
        ARCHIVE_PATH=${ARCHIVE_PATH}/promoted
    else
        ARCHIVE_PATH=${ARCHIVE_PATH}/release
    fi
    ARCHIVE_MASTER_BUNDLES=${ARCHIVE_PATH}/${BUILD_ID}/archive/bundles
    export BUILD_ID BUILD_KIND ARCHIVE_PATH ARCHIVE_MASTER_BUNDLES
    init_bundles_dir
    init_version 
}

purge_old_nightlies(){
#########################
# PURGE OLDER NIGHTLIES #
#########################

    rm -rf /tmp/purgeNightlies.sh
    cat <<EOF > /tmp/purgeNightlies.sh
#!/bin/bash
# Max builds to keep around
    MAX_BUILDS=21
    cd \$1
    LISTING=\`ls -trd b*\`
    nbuilds=0
    for i in \$LISTING; do
    nbuilds=\`expr \$nbuilds + 1\`
    done
    echo "Total number of builds is \$nbuilds"
    
    while [ \$nbuilds -gt \$MAX_BUILDS ]; do
    oldest_dir=\`ls -trd b* | head -n1\`
    echo "rm -rf \$oldest_dir"
    rm -rf \$oldest_dir
    nbuilds=\`expr \$nbuilds - 1\`
    echo "Number of builds is now \$nbuilds"
    done    
EOF
    ssh ${SSH_MASTER} "rm -rf /tmp/purgeNightlies.sh"
    scp /tmp/purgeNightlies.sh ${SSH_MASTER}:/tmp
    ssh ${SSH_MASTER} "chmod +x /tmp/purgeNightlies.sh ; bash -e /tmp/purgeNightlies.sh /java/re/${ARCHIVE_PATH}"
}

init_nightly(){
    BUILD_KIND="nightly"
    ARCHIVE_PATH=${PRODUCT_GF}/${PRODUCT_VERSION_GF}/nightly
    ARCHIVE_MASTER_BUNDLES=${ARCHIVE_PATH}/${BUILD_ID}-${MDATE}
    export BUILD_KIND ARCHIVE_PATH ARCHIVE_MASTER_BUNDLES
    init_bundles_dir
    init_version
    NIGHTLY_PROMOTED_JOB="${HUDSON_URL}/job/${PROMOTED_JOB_NAME}/api/xml?xpath=//lastStableBuild/url/text()"
    NIGHTLY_PROMOTED_JOB_URL=`curl $NIGHTLY_PROMOTED_JOB`
    NIGHTLY_PROMOTED_BUNDLES="${NIGHTLY_PROMOTED_JOB_URL}artifact/bundles"
    export NIGHTLY_PROMOTED_BUNDLES
}

promote_init(){
    init_common
    if [ "nightly" == "${1}" ]
    then
        init_nightly
    elif [ "weekly" == "${1}" ]
    then
        init_weekly
    fi

    export PROMOTION_SUMMARY=${WORKSPACE_BUNDLES}/${BUILD_KIND}-promotion-summary.txt
    rm -f ${PROMOTION_SUMMARY}
    export JNET_DIR=${JNET_USER}@${JNET_STORAGE_HOST}:/dlc/${ARCHIVE_PATH}
    export JNET_DIR_HTTP=http://download.java.net/${ARCHIVE_PATH}
    export ARCHIVE_STORAGE_BUNDLES=/java/re/${ARCHIVE_MASTER_BUNDLES}
    export SSH_MASTER=${RE_USER}@${HUDSON_MASTER_HOST}
    export SSH_STORAGE=${RE_USER}@${STORAGE_HOST}
    export SCP=${SSH_STORAGE}:${ARCHIVE_STORAGE_BUNDLES}
    export ARCHIVE_URL=http://${STORAGE_HOST_HTTP}/java/re/${ARCHIVE_MASTER_BUNDLES}
    init_storage_area
}

promote_finalize(){
    create_symlinks
    create_index
    add_permission
    send_notification
}

promote_weekly(){
    promote_init "weekly"
    promote_bundle ${PROMOTED_BUNDLES}/web.zip ${PRODUCT_GF}-${PRODUCT_VERSION_GF}-web-${BUILD_ID}.zip
    promote_bundle ${PROMOTED_BUNDLES}/glassfish.zip ${PRODUCT_GF}-${PRODUCT_VERSION_GF}-${BUILD_ID}.zip
    promote_bundle ${PROMOTED_BUNDLES}/nucleus-new.zip nucleus-${PRODUCT_VERSION_GF}-${BUILD_ID}.zip
    promote_bundle ${PROMOTED_BUNDLES}/version-info.txt version-info-${PRODUCT_VERSION_GF}-${BUILD_ID}.txt
    VERSION_INFO="${WORKSPACE_BUNDLES}/version-info-${PRODUCT_VERSION_GF}-${BUILD_ID}.txt"

    # increment build value in both promote-trunk.version and pkgid-trunk.version
    # only when build parameter RELEASE_VERSION has been resolved (i.e not provided explicitly).
    if [ ! -z $INCREMENT_BUILD_ID ]
    then    
        BUILD_ID=`cat ${HUDSON_HOME}/promote-trunk.version`
        PKG_ID=`cat ${HUDSON_HOME}/pkgid-trunk.version`    
        NEXT_ID=$((PKG_ID+1))

        # prepend a 0 if less than 10
        if [ $NEXT_ID -lt 10 ]
        then
            NEXT_BUILD_ID="b0$NEXT_ID"
        else
            NEXT_BUILD_ID="b$NEXT_ID"
        fi
        ssh $SSH_MASTER `echo "echo $NEXT_BUILD_ID > /scratch/java_re/hudson/hudson_install/promote-trunk.version"`
        ssh $SSH_MASTER `echo "echo $NEXT_ID > /scratch/java_re/hudson/hudson_install/pkgid-trunk.version"`
    fi

    promote_finalize
}

promote_nightly(){
    promote_init "nightly"
    promote_bundle ${NIGHTLY_PROMOTED_BUNDLES}/web.zip ${PRODUCT_GF}-${PRODUCT_VERSION_GF}-web-${BUILD_ID}-${MDATE}.zip
    promote_bundle ${NIGHTLY_PROMOTED_BUNDLES}/glassfish.zip ${PRODUCT_GF}-${PRODUCT_VERSION_GF}-${BUILD_ID}-${MDATE}.zip
    promote_bundle ${NIGHTLY_PROMOTED_BUNDLES}/nucleus-new.zip nucleus-${PRODUCT_VERSION_GF}-${BUILD_ID}-${MDATE}.zip
    promote_bundle ${NIGHTLY_PROMOTED_BUNDLES}/version-info.txt version-info-${PRODUCT_VERSION_GF}-${BUILD_ID}-${MDATE}.txt
    promote_bundle ${NIGHTLY_PROMOTED_BUNDLES}/changes.txt changes-${PRODUCT_VERSION_GF}-${BUILD_ID}-${MDATE}.txt
    VERSION_INFO="${WORKSPACE_BUNDLES}/version-info-${PRODUCT_VERSION_GF}-${BUILD_ID}-${MDATE}.txt"
    SCM_REVISION=`head -1 ${VERSION_INFO} | cut -d ":" -f2 | tr " " ""`
    purge_old_nightlies
    # hook for the docker image of the nightly
    curl -H "Content-Type: application/json" \
        --data '{"build": true}' \
        -X POST \
        -k \
        https://registry.hub.docker.com/u/glassfish/nightly/trigger/945d55fc-1d4c-4043-8221-74185d9a4d53/  
    ssh $SSH_MASTER `echo "echo $SCM_REVISION > /scratch/java_re/hudson/hudson_install/last_promoted_nightly_scm_revision"`
    promote_finalize
}

init_weekly(){
    BUILD_KIND="weekly"
    require_env_var "GPG_PASSPHRASE"

    ARCHIVE_PATH=${PRODUCT_GF}/${PRODUCT_VERSION_GF}
    if [ ${#BUILD_ID} -gt 0 ]
    then
        ARCHIVE_PATH=${ARCHIVE_PATH}/promoted
    else
        ARCHIVE_PATH=${ARCHIVE_PATH}/release
    fi
    ARCHIVE_MASTER_BUNDLES=${ARCHIVE_PATH}/${BUILD_ID}/archive/bundles
    export BUILD_ID BUILD_KIND ARCHIVE_PATH ARCHIVE_MASTER_BUNDLES
    init_bundles_dir
    init_version 
}

init_common(){
    require_env_var "HUDSON_HOME"
    BUILD_ID=`cat ${HUDSON_HOME}/promote-trunk.version`
    PKG_ID=`cat ${HUDSON_HOME}/pkgid-trunk.version`

    PRODUCT_GF="glassfish"
    JAVAEE_VERSION=8.0
    MAJOR_VERSION=5
    MINOR_VERSION=0
    PRODUCT_VERSION_GF=${MAJOR_VERSION}.${MINOR_VERSION}
    MICRO_VERSION=
    if [ ! -z $MICRO_VERSION ] && [ ${#MICRO_VERSION} -gt 0 ]; then
        PRODUCT_VERSION_GF=$PRODUCT_VERSION_GF.${MICRO_VERSION} 
    fi

    PROMOTED_JOB_URL=${HUDSON_URL}/job/${PROMOTED_JOB_NAME}/${PROMOTED_NUMBER}
    PROMOTED_BUNDLES=${PROMOTED_JOB_URL}/artifact/bundles/

    IPS_REPO_URL=http://localhost
    IPS_REPO_DIR=${WORKSPACE}/promorepo
    IPS_REPO_PORT=16500
    IPS_REPO_TYPE=sunos-sparc
    UC2_VERSION=2.3
    UC2_BUILD=57
    UC_HOME_URL=http://${STORAGE_HOST_HTTP}/java/re/updatecenter/${UC2_VERSION}
    UC_HOME_URL="${UC_HOME_URL}/promoted/B${UC2_BUILD}/archive/uc2/build"
    MDATE=$(date +%m_%d_%Y)
    DATE=$(date)

    MAVEN_REPO="-Dmaven.repo.local=${WORKSPACE}/repository"
    MAVEN_ARGS="${MAVEN_REPO} -C -nsu -B"
    MAVEN_OPTS="-Xmx1024M -Xms256m -XX:MaxPermSize=512m -XX:-UseGCOverheadLimit"
    if [ ! -z ${PROXY_HOST} ] && [ ! -z ${PROXY_PORT} ]
    then
        MAVEN_OPTS="${MAVEN_OPTS} \
    -Dhttp.proxyHost=$PROXY_HOST \
    -Dhttp.proxyPort=$PROXY_PORT \
    -Dhttp.noProxyHosts='127.0.0.1|localhost|*.oracle.com' \
    -Dhttps.proxyHost=${PROXY_HOST} \
    -Dhttps.proxyPort=${PROXY_PORT} \
    -Dhttps.noProxyHosts='127.0.0.1|localhost|*.oracle.com'"
    fi

    if [ -z $WORKSPACE ]
    then
        WORKSPACE=$PWD
    fi

    export JAVAEE_VERSION \
            MAJOR_VERSION \
            MINOR_VERSION \
            MICRO_VERSION \
            VERSION \
            BUILD_ID \
            PKG_ID \
            RELEASE_VERSION \
            PRODUCT_GF \
            PRODUCT_VERSION_GF \
            MDATE \
            DATE \
            IPS_REPO_URL \
            IPS_REPO_DIR \
            IPS_REPO_PORT \
            IPS_REPO_TYPE \
            PROMOTED_BUNDLES \
            GF_WORKSPACE_URL_SSH \
            GF_WORKSPACE_URL_HTTP \
            MAVEN_OPTS \
            MAVEN_REPO \
            MAVEN_ARGS \
            WORKSPACE
}

init_version(){
    # PRODUCT_GF_VERSION - next version to be released (e.g 4.1)
    # RELEASE_VERSION - used by the Maven release builds
    # VERSION - main version information (used for notification)

	if [ "${BUILD_KIND}" = "weekly" ] ; then
	    # retrieving version-info.txt if promoting a weekly
	    # to resolve value of RELEASE_VERSION
	    if [ "${BUILD_KIND}" = "weekly" ] &&  [ -z ${RELEASE_VERSION} ]
	    then
    		curl ${PROMOTED_BUNDLES}/version-info.txt > ${WORKSPACE_BUNDLES}/version-info.txt	
    		RELEASE_VERSION=`grep 'Maven-Version' ${WORKSPACE_BUNDLES}/version-info.txt | awk '{print $2}'`
    		INCREMENT_BUILD_ID=true
    		rm -f ${WORKSPACE_BUNDLES}/version-info.txt
	    fi
		
	    # deduce BUILD_ID and PRODUCT_VERSION_GF
	    # from the value of RELEASE_VERSION
	    if [ ! -z ${RELEASE_VERSION} ] && [ ${#RELEASE_VERSION} -gt 0 ]
	    then
            IS_NON_FINAL_VERSION=`grep '-'  <<< ${RELEASE_VERSION} | wc -l | awk '{print $1}'`
            if [ ${IS_NON_FINAL_VERSION} -gt 0 ]; then
                # PROMOTED BUILD
                BUILD_ID=`cut -d '-' -f2- <<< ${RELEASE_VERSION}`
                PKG_ID=`sed -e s@"b0"@@g -e s@"b"@@g <<< ${BUILD_ID}`
                PRODUCT_VERSION_GF=`sed s@"-${BUILD_ID}"@@g <<< ${RELEASE_VERSION}`
            else
                # RELEASE BUILD
                PRODUCT_VERSION_GF="${RELEASE_VERSION}"
            fi
            VERSION=${RELEASE_VERSION}
	    else
	        printf "\n==== ERROR: %s RELEASE_VERSION must be defined with a non empty value ! ==== \n\n" "${1}"
	        exit 1
	    fi
    else
        if [ "${BUILD_KIND}" = "nightly" ] ; then
            VERSION="${PRODUCT_VERSION_GF}-${BUILD_ID}-${MDATE}"
        else
            VERSION="${PRODUCT_VERSION_GF}-${BUILD_ID}-${USER}"
        fi
	fi
    export VERSION

    printf "\n%s \n\n" "===== VERSION VALUES ====="
    printf "VERSION=%s \nBUILD_ID=%s \nPKG_ID=%s\n\n" \
        "${VERSION}" \
        "${BUILD_ID}" \
        "${PKG_ID}"
}

init_bundles_dir(){
    WORKSPACE_BUNDLES=${WORKSPACE}/${BUILD_KIND}_bundles
    if [ ! -d "${WORKSPACE_BUNDLES}" ]
    then
        mkdir -p "${WORKSPACE_BUNDLES}"
    fi
    export WORKSPACE_BUNDLES
}

require_env_var(){
    var=`eval echo '\$'"$1"`
    if [ ${#var} -eq 0 ]
    then
        printf "\n==== ERROR: %s VARIABLE MUST BE DEFINED ! ==== \n\n" "${1}"
        exit 1
    fi
}

kill_clean(){
    if [ ${#1} -ne 0 ]
    then
        kill -9 ${1} || true
    fi
}

kill_glassfish(){
    kill_clean `jps | grep ASMain | awk '{print $1}'`
}

print_env_info(){
    printf "\n%s \n\n" "==== ENVIRONMENT INFO ===="
    pwd
    uname -a
    java -version
    mvn --version
    git --version
}

dev_build(){
    printf "\n%s \n\n" "===== DO THE BUILD! ====="
    mvn ${MAVEN_ARGS} -f main/pom.xml clean install \
        -Dmaven.test.failure.ignore=true
}

merge_junits(){
  TEST_ID="build-unit-tests"
  rm -rf ${WORKSPACE}/test-results
  mkdir -p ${WORKSPACE}/test-results/$TEST_ID/results/junitreports
  JUD="${WORKSPACE}/test-results/${TEST_ID}/results/junitreports/test_results_junit.xml"
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" > ${JUD}
  echo "<testsuites>" >> ${JUD}
  for i in `find . -type d -name "surefire-reports"`
  do    
    ls -d -1 ${i}/*.xml | xargs cat | ${SED} 's/<?xml version=\"1.0\" encoding=\"UTF-8\" *?>//g' >> ${JUD}
  done
  echo "</testsuites>" >> ${JUD}
  ${SED} -i 's/\([a-zA-Z-]\w*\)\./\1-/g' ${JUD}
  ${SED} -i "s/\bclassname=\"/classname=\"${TEST_ID}./g" ${JUD}
}

release_build(){
    printf "\n%s \n\n" "===== DO THE BUILD! ====="
    mvn ${MAVEN_ARGS} -f main/pom.xml ${1} \
        -P${2} \
        -Dbuild.id=${PKG_ID} \
        -Dgpg.passphrase="${GPG_PASSPHRASE}" \
        -Dgpg.executable=gpg2 \
        -Dmaven.test.failure.ignore=true
}

release_prepare(){
    printf "\n%s \n\n" "===== UPDATE POMS ====="
    mvn ${MAVEN_ARGS} -f main/pom.xml release:prepare \
        -Dtag=${RELEASE_VERSION} \
        -Drelease-phase-all=true \
        -Darguments="${MAVEN_REPO}" \
        -DreleaseVersion="${RELEASE_VERSION}" \
        -DpreparationGoals="A_NON_EXISTENT_GOAL"  \
        2>&1 | tee mvn_rel.output

    egrep "Unknown lifecycle phase \"A_NON_EXISTENT_GOAL\"" mvn_rel.output
    if [ $? -ne 0 ]; then
	echo "Unable to perform release using maven release plugin."
        echo "Please see ${WORKSPACE}/mvn_rel.output."
	exit 1;
    fi
}

run_findbugs(){
    printf "\n%s \n\n" "===== RUN FINDBUGS ====="
    mvn ${MAVEN_ARGS} -f main/pom.xml install findbugs:findbugs

    printf "\n%s \n\n" "===== PROCESS FINDBUGS RESULTS ====="
    FINDBUGS_RESULTS=${WORKSPACE}/findbugs_results
    mkdir ${FINDBUGS_RESULTS} | true

    # run findbugs-tool
    OLD_PWD=`pwd`
    cd ${HUDSON_HOME}/tools/findbugs-tool-latest
    set +e
    ./findbugscheck ${WORKSPACE}/main
    EXIT_CODE=${?}
    set -e
    if [ ${EXIT_CODE} -ne 0 ]
    then
       echo "FAILED" > ${FINDBUGS_RESULTS}/findbugscheck.log
    else
       echo "SUCESS" > ${FINDBUGS_RESULTS}/findbugscheck.log
    fi
    cd ${OLD_PWD}

    # copy all results
    for i in `find ${WORKSPACE}/main -name findbugsXml.xml`
    do
       target=`sed s@"${WORKSPACE}"@@g | sed s@"/"@"_"@g <<< ${i}`
       cp ${i} ${FINDBUGS_RESULTS}/${target}
    done
}

create_version_info(){
    printf "\n%s\n\n" "==== VERSION INFO ===="
    CURRENT_COMMIT=`git rev-parse --short HEAD`
    echo Git Revision:  ${CURRENT_COMMIT} > ${WORKSPACE}/version-info.txt
    echo Git Branch: `git branch | grep ^\* | cut -d ' ' -f 2` >> ${WORKSPACE}/version-info.txt
    echo Build Date: `date` >> ${WORKSPACE}/version-info.txt
    export CURRENT_COMMIT
}

archive_bundles(){
    printf "\n%s \n\n" "===== ARCHIVE BUNDLES ====="
    rm -rf ${WORKSPACE}/bundles ; mkdir ${WORKSPACE}/bundles
    mv ${WORKSPACE}/version-info.txt $WORKSPACE/bundles
    cp ${WORKSPACE}/$GF_ROOT/main/appserver/distributions/glassfish/target/*.zip ${WORKSPACE}/bundles
    cp ${WORKSPACE}/$GF_ROOT/main/appserver/distributions/web/target/*.zip ${WORKSPACE}/bundles
    cp ${WORKSPACE}/$GF_ROOT/main/nucleus/distributions/nucleus/target/*.zip ${WORKSPACE}/bundles
}

clean_and_zip_workspace(){
    printf "\n%s \n\n" "===== CLEAN AND ZIP THE WORKSPACE ====="
    zip ${WORKSPACE}/bundles/workspace.zip -r main > /dev/null
    # zip promorepo without top-leveldir for sdk build and to push IPS pkgs.
    (cd ${WORKSPACE}/promorepo; zip -ry - .) > ${WORKSPACE}/bundles/promorepo.zip
}

zip_tests_workspace(){
    printf "\n%s \n\n" "===== ZIP THE TESTS WORKSPACE ====="
    zip -r ${WORKSPACE}/bundles/tests-workspace.zip \
        main/nucleus/pom.xml \
        main/nucleus/tests/ \
        main/appserver/pom.xml \
        main/appserver/tests/ \
        -x *.git/* > /dev/null
    cp -p  ${WORKSPACE}/$GF_ROOT/main/appserver/tests/gftest.sh ${WORKSPACE}/bundles
}

zip_gf_source(){
    printf "\n%s \n\n" "===== ZIP THE SOURCE CODE ====="
    zip -r ${WORKSPACE}/bundles/main.zip main/ -x **/target\* > /dev/null
}

zip_tests_maven_repo(){
    printf "\n%s \n\n" "===== ZIP PART OF THE MAVEN REPO REQUIRED FOR TESTING ====="
    pushd ${WORKSPACE}/repository

    # ideally this should be done
    # from a maven plugin...
    zip -r ${WORKSPACE}/bundles/tests-maven-repo.zip \
        org/glassfish/main/common/* \
        org/glassfish/main/grizzly/* \
        org/glassfish/main/glassfish-nucleus-parent/* \
        org/glassfish/main/test-utils/* \
        org/glassfish/main/tests/* \
        org/glassfish/main/admin/* \
        org/glassfish/main/core/* \
        org/glassfish/main/deployment/deployment-common/* \
        org/glassfish/main/deployment/nucleus-deployment/* \
        org/glassfish/main/external/ldapbp-repackaged/* \
        org/glassfish/main/external/nucleus-external/* \
        org/glassfish/main/flashlight/flashlight-framework/* \
        org/glassfish/main/grizzly/grizzly-config/* \
        org/glassfish/main/grizzly/nucleus-grizzly-all/* \
        org/glassfish/main/security/security/* \
        org/glassfish/main/security/security-services/* \
        org/glassfish/main/security/ssl-impl/* \
        org/glassfish/main/security/nucleus-security/* > /dev/null
    popd
}

align_column(){
    max=${1}
    char=${2}
    string=${3}
    stringlength=${#string}
    y=$((max-stringlength))
    while [ $y -gt 0 ] ; do string="${string}${char}" ; y=$((y-1)) ; done
    echo "${string}"
}

aggregated_tests_summary(){
    # Hudson rest API does not give the report
    # parsing html manually...

    curl ${1} 1> tests.html 2> /dev/null

    # need to use gawk on solaris
    if [ `uname | grep -i sunos | wc -l` -eq 1 ]
    then
        AWK="gawk"
    else
        AWK="awk"
    fi

    for i in `${AWK} 'BEGIN{RS="<tr><td class=\"pane"} \
         { if (NR > 2) print $2" "$8" "$11 }' \
         tests.html | sed \
          -e s@'"'@@g \
          -e s@'href=/hudson/job/'@@g \
          -e s@'/testReport/><img style=text-align:right>'@'#'@g \
          -e s@'/aggregatedTestReport/><img style=text-align:right>'@'#'@g \
          -e s@' style=text-align:right>'@'#'@g \
          -e s@'<.*'@@g \
          -e s@'.*>'@@g \
          -e s@'/'@'#'@g`
    do
        sizei=${#i}
        y=`sed s@'#'@@g <<< $i`
        sizey=${#y}
        if [ $((sizei - sizey)) -eq 3 ]
        then
            jobname=`cut -d '#' -f1 <<< $i`
            buildnumber=`cut -d '#' -f2 <<< $i`
            failednumber=`cut -d '#' -f3 <<< $i`
            totalnumber=`cut -d '#' -f4 <<< $i`
            passednumber=$((totalnumber-failednumber))
            printf "%s%s%s%s\n" \
                `align_column 55 "." "$jobname (#${buildnumber})"` \
                `align_column 15 "." "PASSED(${passednumber})"` \
                "FAILED(${failednumber})"
        fi
    done
    rm tests.html
}

create_symlinks(){
    PROMOTE_SCRIPT=/tmp/promotebuild.sh
    cat <<EOF > ${PROMOTE_SCRIPT}
#!/bin/bash -e
# arg1 BUILD_ID
# arg2 PRODUCT_VERSION_GF
# arg3 ARCHIVE_MASTER_BUNDLES
# arg4 JAVAEE_VERSION

cd \$3
rm -rf latest-*

for i in \`ls\`
do
    simple_name=\`echo \${i} | \
        sed -e s@"-\${1}"@@g \
        -e s@"-\${2}"@@g \
        -e s@"--"@"-"@g\` 
    
    ln -fs \${i} "latest-\${simple_name}"
    if [ "\${simple_name}" == "glassfish-ml.zip" ]
    then
        ln -fs \${i} "latest-glassfish.zip"
    fi
    if [ "\${simple_name}" == "web-ml.zip" ]
    then
        ln -fs \${i} "latest-web.zip"
    fi
done

cd /java/re/\${5}
rm -rf latest
ln -s \${1} latest
EOF
    echo "trying to create symlink"
    scp ${PROMOTE_SCRIPT} ${SSH_MASTER}:/tmp
    ssh ${SSH_MASTER} "chmod +x ${PROMOTE_SCRIPT}"
    if [ "weekly" == "${BUILD_KIND}" ]
    then
	    ssh ${SSH_MASTER} \
            "${PROMOTE_SCRIPT} ${BUILD_ID} ${PRODUCT_VERSION_GF} /java/re/${ARCHIVE_MASTER_BUNDLES} ${JAVAEE_VERSION} ${ARCHIVE_PATH}"
    elif [ "nightly" == "${BUILD_KIND}" ]
    then
	    echo "ssh ${SSH_MASTER}  ${PROMOTE_SCRIPT} ${BUILD_ID}-${MDATE} ${PRODUCT_VERSION_GF} /java/re/${ARCHIVE_MASTER_BUNDLES} ${JAVAEE_VERSION} ${ARCHIVE_PATH}"
	    ssh ${SSH_MASTER} \
            "${PROMOTE_SCRIPT} ${BUILD_ID}-${MDATE} ${PRODUCT_VERSION_GF} /java/re/${ARCHIVE_MASTER_BUNDLES} ${JAVAEE_VERSION} ${ARCHIVE_PATH}"
    fi
}

init_storage_area(){
    ssh ${SSH_STORAGE} \
        "rm -rf ${ARCHIVE_STORAGE_BUNDLES} ; mkdir -p ${ARCHIVE_STORAGE_BUNDLES}"
}

scp_jnet(){
    file=`basename ${1}`
    simple_name=`echo ${file} | \
        sed -e s@"${PRODUCT_GF}-${PRODUCT_VERSION_GF}-web"@web@g \
        -e s@"${JAVAEE_VERSION}-${BUILD_ID}-"@@g \
        -e s@"-${JAVAEE_VERSION}-${BUILD_ID}"@@g \
        -e s@"${BUILD_ID}-"@@g \
        -e s@"-${BUILD_ID}"@@g \
        -e s@"-${PRODUCT_VERSION_GF}"@@g \
        -e s@"--"@"-"@g `
    if [ "nightly" == "${BUILD_KIND}" ]
    then
	   simple_name=`echo ${simple_name} | \
	           sed -e s@"-${MDATE}"@@g \
	               -e s@"${MDATE}-"@@g`
    fi

    ssh ${SSH_MASTER} \
        "scp /java/re/${ARCHIVE_MASTER_BUNDLES}/${file} ${JNET_DIR}"
    ssh ${SSH_MASTER} \
        "scp /java/re/${ARCHIVE_MASTER_BUNDLES}/${file} ${JNET_DIR}/latest-${simple_name}"
}

create_promotion_changs(){
    rm ${WORKSPACE_BUNDLES}/${1} | true
    if [[ "nightly" == "${BUILD_KIND}" ]]; then
        PREVIOUS_COMMIT=`cat ${HUDSON_HOME}/last_promoted_nightly_scm_revision`
    fi
    if [[ "weekly" == "${BUILD_KIND}" ]]; then
        PREVIOUS_COMMIT=`cat ${HUDSON_HOME}/last_promoted_weekly_scm_revision`
    fi
    CURRENT_COMMIT=`head -1 ${WORKSPACE_BUNDLES}/version-info-${PRODUCT_VERSION_GF}-${BUILD_ID}-${MDATE}.txt | cut -d ":" -f2 | tr " " ""`
    if [ "${CURRENT_COMMIT}" != "${PREVIOUS_COMMIT}" ] ; then
    cd ${WORKSPACE}/glassfish    
        git log --abbrev-commit --pretty=oneline ${PREVIOUS_COMMIT}..${CURRENT_COMMIT} > ${WORKSPACE_BUNDLES}/${1}
    fi
}

promote_bundle(){
    printf "\n==== PROMOTE_BUNDLE (%s) ====\n\n" ${2}
    if [[ ${1} == *"changes.txt" ]]; then
        create_promotion_changs ${2}
    else
        curl ${1} > ${WORKSPACE_BUNDLES}/${2}
    fi
    scp ${WORKSPACE_BUNDLES}/${2} ${SCP}
    scp_jnet ${WORKSPACE_BUNDLES}/${2}
    if [ "nightly" == "${BUILD_KIND}" ]
    then
	   simple_name=`echo ${2}| tr -d " " | sed \
            -e s@"${PRODUCT_VERSION_GF}-"@@g \
            -e s@"${BUILD_ID}-${MDATE}-"@@g \
            -e s@"-${BUILD_ID}-${MDATE}"@@g \
            -e s@"--"@"-"@g`
    elif [ "weekly" == "${BUILD_KIND}" ]
    then
	   simple_name=`echo ${2}| tr -d " " | sed \
            -e s@"${PRODUCT_VERSION_GF}-"@@g \
            -e s@"${BUILD_ID}-"@@g \
            -e s@"-${BUILD_ID}"@@g \
            -e s@"--"@"-"@g`
    fi
    echo "${simple_name} -> ${ARCHIVE_URL}/${2}" >> ${PROMOTION_SUMMARY}
}

send_notification(){
    local commit=`${GREP} 'Git Revision' ${VERSION_INFO} | cut -d ':' -f2 | ${AWK} '{print $1}'`
    local branch=`${GREP} 'Git Branch' ${VERSION_INFO} | cut -d ':' -f2 | ${AWK} '{print $1}'`
    /usr/lib/sendmail -t << MESSAGE
From: ${NOTIFICATION_FROM}
To: ${NOTIFICATION_SENDTO}
Subject: [ ${PRODUCT_GF}-${PRODUCT_VERSION_GF} ] Trunk ${BUILD_KIND} Build (${VERSION})

Product : ${PRODUCT_GF} ${PRODUCT_VERSION_GF}
Date    : ${DATE}
Version : ${VERSION}

External: ${JNET_DIR_HTTP}
Internal: ${ARCHIVE_URL}
Hudson job: ${PROMOTED_JOB_URL}
Git Commit Id: ${commit}
Git Branch: ${branch}

Aggregated tests summary:

`aggregated_tests_summary ${PROMOTED_JOB_URL}/aggregatedTestReport/`

Promotion summary:

`cat $PROMOTION_SUMMARY`

Thanks,
RE
MESSAGE
}

add_permission(){

    ssh ${SSH_MASTER} \
     "ssh ${JNET_USER}@${JNET_STORAGE_HOST} 'cd /dlc/${PRODUCT_GF}/${PRODUCT_VERSION_GF};chmod o+r nightly/*;chmod o+r promoted/*'"
}

#
# Create index files on JNET_STORAGE_HOST for PRODUCT_GF.
#
create_index(){

    # cp this script to master.
    scp ${WORKSPACE}/glassfish/main/common.sh ${SSH_MASTER}:/tmp

    # cp script from ssh_master to JNET_STORAGE_HOST.
    ssh ${SSH_MASTER} \
        "scp /tmp/common.sh ${JNET_USER}@${JNET_STORAGE_HOST}:/dlc/${PRODUCT_GF}"
   
    # Run the script to create the index files on JNET_STORAGE_HOST.
    ssh ${SSH_MASTER} \
        "ssh ${JNET_USER}@${JNET_STORAGE_HOST} 'cd /dlc/${PRODUCT_GF}; bash -c \"source common.sh; generate_index_html ./ | tee /tmp/gen.out 2>&1\"'"
}

# 
# Generate index files for dlc
# 
# Usage: 
#   source this file
#   generate_index_html ./       // $1 is relative path to TOPLEVELURL
#
# Known issues:
# * Doesn't work for filenames with spaces.
#
TOPLEVELURL=http://download.oracle.com/glassfish
TOPLEVELDIR=./
INDEX_FILENAME=index.html
OS=`uname`

#
# Some OS's have leading "./" in find/ls cmds.
# This is a sanity check to remove leading "./", if exists.
#
trim_dot_slash() {
  # If FILE starts with with "./" then remove it.
  if [[ $1 == ./* ]]; then
    FILE=`echo $1 | cut -c 3-`
  fi
}

#
# $1 - Current dir
# names with spaces will be skipped
# hidden directories and hidden files will be skipped
# files named $INDEX_FILENAME | index.html will be skipped
#
generate_index_html() {

    echo ""
    echo "### generate_index_html called with arg1 = $1"

cat > $1/$INDEX_FILENAME << EOF
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Index of $TOPLEVELURL</title>
    <link rel="stylesheet" href="$TOPLEVELURL/listing_themes/apaxy/style.css" type="text/css">
    <meta name="viewport" content="width=device-width, initial-scale=1">
  </head>
  <body>
    <div class="wrapper">
    <table>
      <tbody>
        <tr> 
          <th>
            <img src="$TOPLEVELURL/listing_themes/apaxy/blank.png" alt="[ICO]">
          </th>
          <th>
            Name
          </th>
          <th>
            Last modified
          </th>
          <th>
          Size
    </th>
        </tr>
EOF

    if [ "$1" != "$TOPLEVELDIR" ]
    then
      printf "sub directory \'$1\'\n"
cat >> $1/$INDEX_FILENAME << EOF
        <tr class="parent">
          <td valign="top">
            <a href="$TOPLEVELURL/$INDEX_FILENAME"><img src="$TOPLEVELURL/listing_themes/apaxy/folder-home.png" alt="[DIR]"></a>
          </td>
          <td>
            <a href="$TOPLEVELURL/`echo $1 | cut -c 3-`/../$INDEX_FILENAME">Parent Directory</a></td><td>&nbsp;
          </td>
          <td align="right">  - </td>
        </tr>
EOF
    else
      printf "\nTop level directory \'$1\'\n"
    fi

  # recurse into sub directories.  
  # Specify maxdepth before type for Linux.
  # No maxdepth on SunOS.
  if [ "$OS" = "SunOS" ]
  then
    FIND_CMD="gfind ${1} -maxdepth 1 -type d"
  else
    FIND_CMD="find ${1%/} -maxdepth 1 -type d"
  fi

  for SUB_DIR in `$FIND_CMD | sort`
  do
    # skip . and current dir to prevent infinite loop
    # skip if the directory does not exist, i.e it has a space in its name
    # skip if the basename starts with a dot, i.e it's hidden
    SUB_DIR_BASENAME=`basename "$SUB_DIR"`
    if [ "$SUB_DIR" != "." ] && [ "$SUB_DIR" != "$1" ] && [ -d "$SUB_DIR" ] && [[ $SUB_DIR_BASENAME != .* ]] && [ "$SUB_DIR_BASENAME" != "listing_themes" ]
    then
      # TODO additional exclusion

      printf "\nFound subdir: $SUB_DIR\n"
      printf "Subdir basename: $SUB_DIR_BASENAME\n"

      # Rather than showing the date/time for the dir, we will show the most recent time of the
      # files within the dir so that users can see files have been updated in the directory.
      LAST_MODIFIED=`find $SUB_DIR -type f | grep -v index.html | xargs ls -lt 2> /dev/null | awk '{ print $6, $7, $8}' | head -1`

      # So that we don't have "//" in links - check if the cut is ""
      if [ "`echo $1 | cut -c 3-`" = "" ]
      then
        HREF="$TOPLEVELURL/$SUB_DIR_BASENAME/$INDEX_FILENAME"
      else
        HREF="$TOPLEVELURL/`echo $1 | cut -c 3-`/$SUB_DIR_BASENAME/$INDEX_FILENAME"
      fi 

cat >> $1/$INDEX_FILENAME << EOF
        <tr>
          <td valign="top">
            <a href="$HREF"><img src="$TOPLEVELURL/listing_themes/apaxy/folder.png" alt="[DIR]"></a>
          </td>
          <td>
            <a href="$HREF">$SUB_DIR_BASENAME/</a>
          </td>
          <td align="right">
            $LAST_MODIFIED 
          </td>
          <td align="right">  - </td>
        </tr>
EOF
      generate_index_html $SUB_DIR 
    fi
  done

  # Generate file list named latest* first.
  if [ "$OS" = "SunOS" ]
  then
    FINDFILES=`gfind $1 -maxdepth 1 -type f -name latest\*`
  else
    FINDFILES=`find ${1%/} -maxdepth 1 -type f -name latest\*`
  fi
  if [ "$FINDFILES" != "" ]; then
    FILELIST1=`echo $FINDFILES | xargs ls -t`
  else
    FILELIST1=""
  fi

  # Now get all files that do not start with latest*.
  if [ "$OS" = "SunOS" ]
  then
    FINDFILES=`gfind $1 -maxdepth 1 -type f -not -name latest\*`
    FINDLINKS=`gfind $1 -maxdepth 1 -type l -not -name latest\*`    
  else
    FINDFILES=`find ${1%/} -maxdepth 1 -type f -not -name latest\*`
    FINDLINKS=`find ${1%/} -maxdepth 1 -type l -not -name latest\*`
  fi
  if [ "$FINDFILES" != "" -o "$FINDLINKS" != "" ]; then
    FILELIST2=`echo $FINDFILES $FINDLINKS | xargs ls -t`
  else
    FILELIST2=""
  fi

  # Concat latest* files + !latest files.
  FILELIST="$FILELIST1 $FILELIST2"
  
  echo ""
  echo "### Processing files from ${1}..."
  for FILE in $FILELIST
  do
    # Remove leading "./" from $FILE, if exists
    trim_dot_slash "$FILE"

    # skip if the file does not exist, i.e it has a space in its name
    # skip if the basename starts with a dot, i.e it's hidden
    FILENAME=`basename "$FILE"`
    if [ -f $FILE ] && [[ $FILENAME != .* ]] && [ "$FILENAME" != "index.html" ] && \
       [ "$FILENAME" != "$INDEX_FILENAME" ] && [ "$FILENAME" != "genIndex.sh" ] && \
       [ "$FILENAME" != "index-test.html" ] && [ "$FILENAME" != "genIndex.sh.sav" ] && \
       [ "$FILENAME" != "common.sh" ]
    then
      # TODO additional exclusion

      printf "Found file: $FILENAME\n"

      # Set a default icon in case nothing found.
      ICON="default.png"

      # XXX Do we want to follow links with -L here?
      # Get time last modified and file size.  Output may OS dependent.
      LAST_MODIFIED=`ls -l $FILE | awk '{ print $6, $7, $8 }'`
      HUMAN_READABLE_SIZE=`ls -lh $FILE | awk '{ print $5 }'`

      EXT="${FILENAME##*.}"
      case "$EXT" in
        "BASH" | "bash" | "sh" | "SH" | "BAT" | "bat") ICON="script.png";;
        "bin" | "BIN") ICON="bin.png";;
        "c" | "C" | "c" | "CPP" | "cpp" | "txt" | "TXT" | "java" | "JAVA" | "conf" | "CONF" | "css" | "CSS" | "html" | "HMTL") ICON="text.png";;
        "exe" | "EXE") ICON="exe.png";;
        "gif" | "GIF" | "png" | "PNG" | "jpg" | "JPG" | "tiff" | "TIFF" | "jpeg" | "JPEG" | "bmp" | "BMP") ICON="image.png";;
        "jar" | "JAR") ICON="java.png";;
        "py" | "PY") ICON="py.png";;
        "rar" | "RAR") ICON="rar.png";;
        "tar" | "TAR" | "gz" | "GZ" | "bz2" | "BZ2") ICON="archive.png";;
        "zip" | "ZIP") ICON="zip.png";;
        "xml" | "XML") ICON="xml.png";;
        "md" | "MD") ICON="markdown.png";;
        "rpm" | "RPM") ICON="rpm.png";;
        "pdf" | "PDF") ICON="pdf.png";;
        "*") ICON="default.png";;
      esac

      # Catch files with no extensions.
      if [ "$FILENAME" = "README"  -o "$FILENAME" = "readme" ]; 
      then
        ICON="readme.png"
      fi

cat >> $1/$INDEX_FILENAME << EOF
        <tr>
          <td valign="top">
            <a href="$TOPLEVELURL/$FILE"><img src="$TOPLEVELURL/listing_themes/apaxy/$ICON" alt="[   ]"></a>
          </td>
          <td>
            <a href="$TOPLEVELURL/$FILE">$FILENAME</a>
          </td>
          <td align="right">$LAST_MODIFIED  </td> 
          <td align="right">$HUMAN_READABLE_SIZE</td>
        </tr>
EOF
    fi
  done

cat >> $1/$INDEX_FILENAME << EOF
      </tbody>
    </table>
    </div>
    <script type="text/javascript">
      // grab the 2nd child and add the parent class. tr:nth-child(2)
      document.getElementsByTagName('tr')[1].className = 'parent';
    </script>
  </body>
</html>
EOF
}


