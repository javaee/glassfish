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

include $(BUILD_ROOT)/make/defines_UNIX.mk
LOCAL_COPY=0


# we use the latest & greatest version as of March 2001
SPARCWORKS_VER = 6.0
# our own copy
# the official, maintained (hopefully!) one:
#SPARCWORKS_DIR = /usr/dist/share/forte_dev_i386,v6.2/SUNWspro
SPARCWORKS_DIR = /usr/dist/share/sunstudio_i386/SUNWspro

# Tool locations
C++C		=$(PRE_CC) $(SPARCWORKS_DIR)/bin/CC
CC		=$(PRE_CC) $(SPARCWORKS_DIR)/bin/CC
C               =$(PRE_C)  $(SPARCWORKS_DIR)/bin/cc
AR              =/usr/ccs/bin/ar
RANLIB          =/usr/ccs/bin/ranlib
YACC            =/usr/ccs/bin/yacc
LD		=$(PRE_LD) /usr/ccs/bin/ld
PROFILER	=$(SPARCWORKS_DIR)/bin/profile
FTP		=/usr/bin/ftp
PERL		=/usr/bin/perl
PERL5		=/usr/bin/perl
LINT		=$(SPARCWORKS_DIR)/bin/lint
STRIP		=/usr/ccs/bin/strip -x
NMAKE           =/opt/usr/local/bin/gmake -f
ZIP             =/usr/bin/zip

# we don't have that stuff
VERITY_LIB=
NO_SEARCH=1
NO_SNMP=1

# Use C++ standard version 
_USE_STDCPLUSPLUS_=1

# what is NLIST?
NLIST            = elf

BASEFLAGS =

ifdef DEBUG_BUILD
CC_DEBUG	= -g -xs $(BASEFLAGS)
C_DEBUG         = -g $(BASEFLAGS)
else
CC_DEBUG        = -dalign -xO4 $(BASEFLAGS)
C_DEBUG         = $(CC_DEBUG)
endif

ifndef JAVA_VERSION
JAVA_VERSION	= 5
endif

SUNSOFT_JDK	= 1

ifeq ($(SUNSOFT_JDK), 1)
JNI_MD_LIBTYPE	=
else
JNI_MD_LIBTYPE  = classic
endif

JNI_MD_NAME	= solaris
JNI_MD_SYSNAME  = i386

# Platform specific build options
LD_DYNAMIC	= -G
ARFLAGS		= -r
PLATFORM_DEF	= -DSVR4 -DSYSV -DSOLARIS -D_REENTRANT -DPLATFORM_SPECIFIC_STATS_ON
PLATFORM_LIB	= $(PRE_PLATFORM_LIB) socket nsl dl posix4 kstat Crun Cstd
PLATFORM_CC_OPTS = -mt -KPIC
PLATFORM_C_OPTS  = -mt -Kpic
PLATFORM_LD_OPTS = -mt -norunpath
SYSTEM_LIBDIRS   += $(SPARCWORKS_DIR)/lib
RPATH_PREFIX = -R 

# template database location.
ifndef NO_STD_DATABASE_DEFINE
TEMPLATE_DATABASE_DIR=$(OBJDIR)
endif

ifdef TEMPLATE_DATABASE_DIR
CC_FLAGS += -ptr$(TEMPLATE_DATABASE_DIR)
LD_FLAGS += -ptr$(TEMPLATE_DATABASE_DIR)
endif

ifdef BROWSE
CC_BROWSE	= -xsb
C_BROWSE        =
LD_BROWSE	=
endif # BROWSE

# No support for mapfiles
USE_MAPFILE=

################
#### XERCES ####
################
XERCES_DEFS += -features=rtti

IDS_NSPR_DIR    =$(NSPR_DIR)
IDS_LDAPSDK_DIR =$(LDAPSDK_DIR)

LDAP_LIB_VERSION=50
LDAP_LIBS  = ldap$(LDAP_LIB_VERSION)$(DLL_PRESUF) prldap50$(DLL_PRESUF)

include $(BUILD_ROOT)/make/defines_$(PLATFORM)_64.mk
