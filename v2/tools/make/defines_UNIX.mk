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

#
# NOTE: This file is used by Enterprise, Messaging AND Collabra servers.
#       Please do NOT change indiscriminately
#

TOOL_ROOT	=/usr
NSTOOL_ROOT     =/tools/ns

CC              =$(ECHO) define CC in the platform definitions.
C               =$(ECHO) define C  in the platform definitions.
AR              =$(ECHO) define AR in the platform definitions.
LD              =$(ECHO) define LD in the platform definitions.

### for installer
CPPCMD		=$(C) -E

C++C            =$(CC)

### COMMON UNIX BINARIES
PERL_DIR        =$(NSTOOL_ROOT)
PERL5         	=$(PERL_DIR)/bin/perl5
PERL		=$(PERL_DIR)/bin/perl
RM		=$(TOOL_ROOT)/bin/rm
LS		=$(TOOL_ROOT)/bin/ls
CP		=$(TOOL_ROOT)/bin/cp
CP_R		=$(CP) -r
LN		=$(TOOL_ROOT)/bin/ln -f
CMP		=$(TOOL_ROOT)/bin/cmp
MV		=$(TOOL_ROOT)/bin/mv
SED		=$(TOOL_ROOT)/bin/sed
ECHO		=$(TOOL_ROOT)/bin/echo
DATE		=$(TOOL_ROOT)/bin/date
MKDIR           =$(TOOL_ROOT)/bin/mkdir
CHMOD           =$(TOOL_ROOT)/bin/chmod
CHMOD_DASH_R_755 =$(TOOL_ROOT)/bin/chmod -R 755
MKDIR_DASH_P	=$(MKDIR) -p
XARGS           =$(TOOL_ROOT)/bin/xargs
DIRNAME         =$(TOOL_ROOT)/bin/dirname
BASENAME        =$(TOOL_ROOT)/bin/basename
SHELL           =$(TOOL_ROOT)/bin/sh
SLEEP		=$(TOOL_ROOT)/bin/sleep
WC		=$(TOOL_ROOT)/bin/wc
GREP		=$(TOOL_ROOT)/bin/grep
FIND		=$(TOOL_ROOT)/bin/find
TOUCH		=$(TOOL_ROOT)/bin/touch
PRINTF		=$(TOOL_ROOT)/bin/printf
YACC		=$(TOOL_ROOT)/bin/yacc
TR              =$(TOOL_ROOT)/bin/tr
TAR		=$(TOOL_ROOT)/bin/tar
STRIP		=strip
RCP		=$(TOOL_ROOT)/bin/rcp
RCP_CMD		=$(RCP) -r $(RCPUSER)@$(RCPSERVER)

G++		=$(NSTOOL_ROOT)/bin/g++
GCC		=$(NSTOOL_ROOT)/bin/gcc
FLEX		=$(NSTOOL_ROOT)/bin/flex
BISON		=$(NSTOOL_ROOT)/bin/bison
CVS		=$(NSTOOL_ROOT)/bin/cvs
GZIP		=$(NSTOOL_ROOT)/bin/gzip
GUNZIP		=$(NSTOOL_ROOT)/bin/gunzip
ZIP		=$(NSTOOL_ROOT)/bin/zip
UNZIP		=$(NSTOOL_ROOT)/bin/unzip
NMAKE		=$(NSTOOL_ROOT)/bin/gmake -f

##############
## PREFIXES ##
##############

# Modify as needed in platform defines
OBJ =o
SBR =o
CPP =cpp
STATIC_LIB_SUFFIX=a
DYNAMIC_LIB_SUFFIX=so
LIBPREFIX=lib

COMMENT=\#
ifndef LD_LIB_VAR
LD_LIB_VAR=LD_LIBRARY_PATH
endif # !LD_LIB_VAR


###################
### COMMON LIBS ###
###################

NSPR_LIB   = plc4 plds4 nspr4
SEC_LIB+= ssl nss cert secmod key crypto hash secutil dbm

ifeq ($(OS_ARCH),Linux)
LDAP_LIBS  = ldap$(LDAP_LIB_VERSION)$(DLL_PRESUF) prldap50$(DLL_PRESUF)
else
LDAP_LIBS  = ldap$(LDAP_LIB_VERSION)$(DLL_PRESUF) ldappr50$(DLL_PRESUF)
endif

SSLDAP_LIB = ssldap$(LDAP_LIB_VERSION)
ICU_LIBS   = icuuc icui18n icutoolutil icudata
SETUPSDK_LIB = install
VERITY_LIB = vdk200$(DLL_PRESUF)

###############################
### COMMON COMPILER OPTIONS ###
###############################

ifdef DEBUG_BUILD
CC_DEBUG = -g
C_DEBUG  = -g
else
# jsalter: remove -O4 option because not all compilers use
#          that option for high-level optimization
CC_DEBUG =
C_DEBUG  =
endif

ARFLAGS = -r

ZIPFLAGS = -ry

# Unix-generic defines
SYSTEM_DEF += -DXP_UNIX

#Unix xerces defines
####################
### XERCES Build ###
####################
ifdef XERCES
XERCES_DEFS += -DXML_USE_NATIVE_TRANSCODER -DXML_USE_INMEM_MESSAGELOADER
DEFINES += $(XERCES_DEFS)
INCLUDES+=-I$(INTERNAL_ROOT)/include/xmlparser
INCLUDES+=-I$(INTERNAL_ROOT)/include/xmlparser/dom
endif


# Library expansion code.

REAL_LIBS=$(addprefix -l,$(LIBS))
EXE_REAL_LIBS=$(addprefix -l,$(EXE_LIBS))
EXE1_REAL_LIBS=$(addprefix -l,$(EXE1_LIBS))
EXE2_REAL_LIBS=$(addprefix -l,$(EXE2_LIBS))
EXE3_REAL_LIBS=$(addprefix -l,$(EXE3_LIBS))
EXE4_REAL_LIBS=$(addprefix -l,$(EXE4_LIBS))
EXE5_REAL_LIBS=$(addprefix -l,$(EXE5_LIBS))
EXE6_REAL_LIBS=$(addprefix -l,$(EXE6_LIBS))
EXE7_REAL_LIBS=$(addprefix -l,$(EXE7_LIBS))
EXE8_REAL_LIBS=$(addprefix -l,$(EXE8_LIBS))
EXE9_REAL_LIBS=$(addprefix -l,$(EXE9_LIBS))
EXE10_REAL_LIBS=$(addprefix -l,$(EXE10_LIBS))
EXE11_REAL_LIBS=$(addprefix -l,$(EXE11_LIBS))
EXE12_REAL_LIBS=$(addprefix -l,$(EXE12_LIBS))
EXE13_REAL_LIBS=$(addprefix -l,$(EXE13_LIBS))
EXE14_REAL_LIBS=$(addprefix -l,$(EXE14_LIBS))
EXE15_REAL_LIBS=$(addprefix -l,$(EXE15_LIBS))
EXE16_REAL_LIBS=$(addprefix -l,$(EXE16_LIBS))
EXE17_REAL_LIBS=$(addprefix -l,$(EXE17_LIBS))
EXE18_REAL_LIBS=$(addprefix -l,$(EXE18_LIBS))
EXE19_REAL_LIBS=$(addprefix -l,$(EXE19_LIBS))
EXE20_REAL_LIBS=$(addprefix -l,$(EXE20_LIBS))

DLL_REAL_LIBS=$(addprefix -l,$(DLL_LIBS))
DLL1_REAL_LIBS=$(addprefix -l,$(DLL1_LIBS))
DLL2_REAL_LIBS=$(addprefix -l,$(DLL2_LIBS))
DLL3_REAL_LIBS=$(addprefix -l,$(DLL3_LIBS))
DLL4_REAL_LIBS=$(addprefix -l,$(DLL4_LIBS))
DLL5_REAL_LIBS=$(addprefix -l,$(DLL5_LIBS))

REAL_LIBDIRS=$(addprefix -L,$(LIBDIRS))
EXE_REAL_LIBDIRS=$(addprefix -L,$(EXE_LIBDIRS))
EXE1_REAL_LIBDIRS=$(addprefix -L,$(EXE1_LIBDIRS))
EXE2_REAL_LIBDIRS=$(addprefix -L,$(EXE2_LIBDIRS))
EXE3_REAL_LIBDIRS=$(addprefix -L,$(EXE3_LIBDIRS))
EXE4_REAL_LIBDIRS=$(addprefix -L,$(EXE4_LIBDIRS))
EXE5_REAL_LIBDIRS=$(addprefix -L,$(EXE5_LIBDIRS))
EXE6_REAL_LIBDIRS=$(addprefix -L,$(EXE6_LIBDIRS))
EXE7_REAL_LIBDIRS=$(addprefix -L,$(EXE7_LIBDIRS))
EXE8_REAL_LIBDIRS=$(addprefix -L,$(EXE7_LIBDIRS))
EXE9_REAL_LIBDIRS=$(addprefix -L,$(EXE7_LIBDIRS))
EXE10_REAL_LIBDIRS=$(addprefix -L,$(EXE10_LIBDIRS))
EXE11_REAL_LIBDIRS=$(addprefix -L,$(EXE11_LIBDIRS))
EXE12_REAL_LIBDIRS=$(addprefix -L,$(EXE12_LIBDIRS))
EXE13_REAL_LIBDIRS=$(addprefix -L,$(EXE13_LIBDIRS))
EXE14_REAL_LIBDIRS=$(addprefix -L,$(EXE14_LIBDIRS))
EXE15_REAL_LIBDIRS=$(addprefix -L,$(EXE15_LIBDIRS))
EXE16_REAL_LIBDIRS=$(addprefix -L,$(EXE16_LIBDIRS))
EXE17_REAL_LIBDIRS=$(addprefix -L,$(EXE17_LIBDIRS))
EXE18_REAL_LIBDIRS=$(addprefix -L,$(EXE18_LIBDIRS))
EXE19_REAL_LIBDIRS=$(addprefix -L,$(EXE19_LIBDIRS))
EXE20_REAL_LIBDIRS=$(addprefix -L,$(EXE20_LIBDIRS))
# test

#ruslan: UNIX needs to look up libraries here as well
SYSTEM_LIBDIRS += $(WORK_ROOT)/lib
