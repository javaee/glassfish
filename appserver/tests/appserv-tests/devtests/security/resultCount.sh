#!/bin/sh
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
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
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



FILES="$APS_HOME/test_resultsValid.xml $APS_HOME/security-gtest-results.xml"

TOTAL=800
PASSED=0
FAILED=0
for i in $FILES
do
	echo "input file=$i"
	P=`grep "\"pass\"" $i |  wc -l`
	F=`grep "\"fail\"" $i |  wc -l`
	PASSED=`expr $PASSED + $P`
	FAILED=`expr $FAILED + $F`
done
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

echo "************************">$APS_HOME/devtests/security/count.txt;
date>>$APS_HOME/devtests/security/count.txt;
echo "-----------------------">>$APS_HOME/devtests/security/count.txt;
echo "PASSED=   $PASSED">>$APS_HOME/devtests/security/count.txt;
echo "------------  =========">>$APS_HOME/devtests/security/count.txt;
echo "FAILED=   $FAILED">>$APS_HOME/devtests/security/count.txt;
echo "------------  =========">>$APS_HOME/devtests/security/count.txt;
echo "DID NOT RUN=   $DNR">>$APS_HOME/devtests/security/count.txt;
echo "------------  =========">>$APS_HOME/devtests/security/count.txt;
echo "Total Expected=$TOTAL">>$APS_HOME/devtests/security/count.txt;
echo "************************">>$APS_HOME/devtests/security/count.txt;
