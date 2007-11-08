#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# 
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License. You can obtain
# a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
# or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
# 
# When distributing the software, include this License Header Notice in each
# file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
# Sun designates this particular file as subject to the "Classpath" exception
# as provided by Sun in the GPL Version 2 section of the License file that
# accompanied this code.  If applicable, add the following below the License
# Header, with the fields enclosed by brackets [] replaced by your own
# identifying information: "Portions Copyrighted [year]
# [name of copyright owner]"
# 
# Contributor(s):
# 
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

# defines for Java build
ifndef JAVAC
JAVAC       = $(EXTERNAL_JDK_DIR)/bin/javac
endif
ifndef JAVAH
JAVAH       = $(EXTERNAL_JDK_DIR)/bin/javah
endif
ifndef JAR
JAR         = $(EXTERNAL_JDK_DIR)/bin/jar
endif
ifndef JAVADOC
JAVADOC     = $(EXTERNAL_JDK_DIR)/bin/javadoc
endif

ifneq ($(OS_ARCH),WINNT)
CUR_DIR=$(shell pwd)
else
# PWD on NT appends CRLF which we must strip:
CUR_DIR=$(shell pwd | $(TR) -d "\r\n")
endif

#
# defs for ant
#

ANT_JAVA_HOME=$(CUR_DIR)/$(EXTERNAL_JDK_DIR)
ANT_HOME=$(CUR_DIR)/$(EXTERNAL_BASE)/ant/$(OBJDIR_NAME)

ifeq ($(OS_ARCH),WINNT)
ANT_BIN=ant.bat
else
ANT_BIN=ant
endif

ifndef ANT_BUILD_FILE
ANT_BUILD_FILE = build.xml
endif

STD_ANT_OPTIONS  = -buildfile $(ANT_BUILD_FILE)
STD_ANT_OPTIONS += -Djava.obj.dir=$(OBJDIR_NAME)

ifeq ($(BUILD_VARIANT), OPTIMIZED)
STD_ANT_OPTIONS += -Ddebug=off -Doptimize=on
else
STD_ANT_OPTIONS += -Ddebug=on -Doptimize=off
endif

_ANT_OPTIONS=$(STD_ANT_OPTIONS) $(ANT_OPTIONS)

ifndef ANT_TARGETS
ANT_TARGETS=all
endif

# Variable containing environment value for running ant
ANT_ENV=JAVA_HOME=$(ANT_JAVA_HOME) ANT_HOME=$(ANT_HOME) ANT_OPTS=-Xmx256m

# Ant command line
# (ANT_ARGS are optional and are specified by the user on the command line)
ANT=$(ANT_HOME)/bin/$(ANT_BIN) $(_ANT_OPTIONS) $(ANT_TARGETS)

# Ant clobber command
ANT_CLOBBER=$(MAKE) MAKEFLAGS='$(MAKEFLAGS)' $(ANT_ENV) ANT_TARGETS=clobber antit
