#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

# User customizable variable definitions only

# The BUILD_ROOT variable must be defined
BUILD_ROOT=.
# include bootstrap variable definitions
include $(BUILD_ROOT)/../bootstrap/defines.mk
# include bootstrap rule definitions
include $(BUILD_ROOT)/../bootstrap/rules.mk

# see runall target
include config.properties
#ANT_DIR= $(BUILD_ROOT)/../publish/$(JAVA_OBJDIR)/ant
ANT_DIR= ${S1AS_HOME}/lib/ant
APS_DIR= $(shell pwd)
ifeq ($(OS_ARCH), WINNT)
#ANT_JAVA_HOME=$(subst \,/,$(S1AS_HOME))/jdk
ANT_JAVA_HOME=${JAVA_HOME}
else
#ANT_JAVA_HOME=$(S1AS_HOME)/jdk
ANT_JAVA_HOME=${JAVA_HOME}
endif
ifeq ($(OS_ARCH), Darwin)
ANT_JAVA_HOME=/Library/Java/Home 
endif
JDK_DIR= $(ANT_JAVA_HOME)
ANT_HOME= ${S1AS_HOME}/lib/ant
ANT_ENV= JAVA_HOME=$(ANT_JAVA_HOME) ANT_HOME=$(ANT_HOME) ANT_OPTS=-Xmx256m

# User customizable rules

DB_PORT_1=9092
DB_PORT_2=9093

ifeq  ($(OS_ARCH),WINNT)
CLASSPATH=$(S1AS_HOME)/lib/mail.jar;$(S1AS_HOME)/lib/activation.jar;$(S1AS_HOME)/lib/commons-logging.jar
else
CLASSPATH=$(S1AS_HOME)/lib/mail.jar:$(S1AS_HOME)/lib/activation.jar:$(S1AS_HOME)/lib/commons-logging.jar
endif

runtest:: cleanup
	echo $(ANT_JAVA_HOME)
#	$(BS_MAKE) start
	$(BS_MAKE) $(ANT_ENV) ANT_TARGETS=all-pe antit
#	$(BS_MAKE) stop

runtest-ee:: cleanupclone runtest rename_results runtest-ee-2

runtest-ee-standalone::cleanup runtest-ee-2

runtest-ee-2:: 
#	JAVA_HOME=$(JDK_DIR) sh $(APS_HOME)/eeconfig/eeunsetup
#	$(BS_MAKE) start-ee
	$(BS_MAKE) $(ANT_ENV) ANT_TARGETS=only-ee antit
#	$(BS_MAKE) stop-ee

rename_results::
	mv count.txt PE_count.txt
	mv test_results.html PE_test_results.html
	mv runtest.output PE_runtest.output
	mv test_results.xml PE_test_results.xml
	mv test_resultsValid.xml PE_test_resultsValid.xml

startPB::
	$(BS_MAKE) $(ANT_ENV) ANT_TARGETS=preparePB antit
ifeq  ($(OS_ARCH),WINNT)
	JAVA_HOME=$(JDK_DIR) sh startpb.win.sh $(DB_PORT_1) &
	JAVA_HOME=$(JDK_DIR) sh startpb.win.sh $(DB_PORT_2) &
else
	JAVA_HOME=$(JDK_DIR) sh startpb.sh $(DB_PORT_1) &
	JAVA_HOME=$(JDK_DIR) sh startpb.sh $(DB_PORT_2) &
endif

stopPB::
ifeq  ($(OS_ARCH),WINNT)
	stoppb.bat $(ANT_JAVA_HOME) $(DB_PORT_1)
	stoppb.bat $(ANT_JAVA_HOME) $(DB_PORT_2)
else
	JAVA_HOME=$(JDK_DIR) sh stoppb.sh $(DB_PORT_1)
	JAVA_HOME=$(JDK_DIR) sh stoppb.sh $(DB_PORT_2)
endif

mail::
	$(BS_MAKE) CLASSPATH=$(CLASSPATH) $(ANT_ENV) ANT_TARGETS=mail antit

prepare::
	$(BS_MAKE) CLASSPATH=$(CLASSPATH) $(ANT_ENV) ANT_TARGETS=preparePB antit

start:: startPB
	JAVA_HOME=$(JDK_DIR) sh start-domain.sh

start-ee:: startPB
	JAVA_HOME=$(JDK_DIR) sh -x $(APS_HOME)/eeconfig/eesetup

stop:: 
	JAVA_HOME=$(JDK_DIR) sh stop-domain.sh
	$(BS_MAKE) stopPB

stop-ee:: 
	JAVA_HOME=$(JDK_DIR) sh $(APS_HOME)/eeconfig/stopee
	$(BS_MAKE) stopPB
run:: cleanup
	$(BS_MAKE) CLASSPATH="$(CLASSPATH)" $(ANT_ENV) ANT_TARGETS=runtest antit

run-ee:: cleanup
	$(BS_MAKE) CLASSPATH="$(CLASSPATH)" $(ANT_ENV) ANT_TARGETS="ee runtest" antit

cleanup::
	rm -f $(APS_HOME)/count.txt
	rm -f $(APS_HOME)/*.output
	rm -f $(APS_HOME)/test_results*

cleanupclone::
	rm -f $(APS_HOME)/*count.txt
	rm -f $(APS_HOME)/*.output
	rm -f $(APS_HOME)/*test_results*

clobber::
	rm -f $(APS_DIR)/count.txt
	rm -f $(APS_DIR)/*.output
	rm -f $(APS_DIR)/test_results*
	rm -rf $(APS_DIR)/build


# If you set s1as.home in config.properties you can then run 'gmake runall'
# from this directory without having to set any environment variables.
# (note: s1as.home should use / in the path even on windows)
runall:: clobber
	@echo using s1as instance at $(s1as.home)
	ANT_HOME=$(ANT_DIR) APS_HOME=$(APS_DIR) S1AS_HOME=$(s1as.home) \
	JAVA_HOME=$(JDK_DIR) $(BS_MAKE) runtest

runall-ee:: clobber
	@echo using s1as instance at $(s1as.home)
	ANT_HOME=$(ANT_DIR) APS_HOME=$(APS_DIR) S1AS_HOME=$(s1as.home) \
	JAVA_HOME=$(JDK_DIR) $(BS_MAKE) runtest-ee
