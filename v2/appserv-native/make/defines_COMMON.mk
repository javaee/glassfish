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

#! gmake
#
# This file contains the main defines.  They can be overridden by platform 
# defines

RCPSERVER=iws-files
RCPUSER=ftpman

# for pulling and pushing java bits
SHARE_BASEDIR=/h/iws-files/tinderbox/share
# java builds running on iws-files:/tinderbox
SHARE_SERVER_VER=70se
SHARE_DIR=$(SHARE_BASEDIR)/$(SHARE_SERVER_VER)

###############
#### TOOLS ####
###############

# Versions
PERL_VER	=v5

# Directories:
PUBLISH_ROOT	=$(BUILD_ROOT)/../publish
EXTERNAL_BASE	=$(BUILD_ROOT)/../publish
ifndef WORK_ROOT
WORK_ROOT	=$(EXTERNAL_BASE)/$(OBJDIR_NAME)/$(COMPONENT_NAME)
endif
HOST_WORK_ROOT	=$(EXTERNAL_BASE)/$(HOST_OBJDIR)/$(COMPONENT_NAME)
INTERNAL_ROOT	=$(WORK_ROOT)/internal

THIRD_PARTY_ROOT=$(EXTERNAL_BASE)/$(OBJDIR_NAME)
NSPR_DIR        =$(THIRD_PARTY_ROOT)/nspr
NSS_DIR		=$(THIRD_PARTY_ROOT)/NSsecurity
LDAPSDK_DIR     =$(THIRD_PARTY_ROOT)/ldapsdk
SMARTHEAP_DIR	=$(THIRD_PARTY_ROOT)/smartheap
SMARTHEAP_DIR	=$(THIRD_PARTY_ROOT)/smartheap
ICU_DIR		=$(THIRD_PARTY_ROOT)/icu
VERITY_DIR	=$(THIRD_PARTY_ROOT)/verity
KEYVIEW_DIR	=$(EXTERNAL_BASE)/keyview
MKS_DIR		=$(EXTERNAL_BASE)/mks
SETUPSDK_DIR    =$(THIRD_PARTY_ROOT)/setupsdk

# IDS_NSPR_DIR and IDS_LDAPSDK_DIR are used only by digest auth plugin
# which runs in iDS
IDS_NSPR_DIR    =$(THIRD_PARTY_ROOT)/IDS_nspr
IDS_LDAPSDK_DIR =$(THIRD_PARTY_ROOT)/IDS_ldapsdk

VERGEN_DIR      =src/server/tools/VerGen
VERGEN          =$(VERGEN_DIR)/$(OBJDIR)/VerGen$(EXE)

VERSION_FILE    =$(WORK_ROOT)/Version

PRSTRMS_LIB= prstrms4
ARES_LIB   = ares3

ifdef DEBUG_BUILD
DEFINES+=-DDEBUG -D_DEBUG
VERGEN_FLAGS+=-debug
endif

# Bundled Chili!Soft ASP per-platform disables
SHIP_CHILISOFT=1
ifneq ($(OS_ARCH),WINNT)
SHIP_CHILISOFT=
endif

# SmartHeap per-platform disables
USE_SMARTHEAP=

# SmartHeap linkage (run-time or link-time)
ifdef USE_SMARTHEAP
ifeq ($(OS_ARCH),WINNT)
# run-time linkage for NT
LINK_SMARTHEAP=
else
# link-time linkage for Unix
LINK_SMARTHEAP=1
endif
endif

#i18n defines
ifndef UI_LANGS
ifeq ($(MARKET), ja)
UI_LANGS=ja
endif
ifeq ($(MARKET), eu)
UI_LANGS=fr de es 
endif
endif

NSPR_INCLUDE		=$(NSPR_DIR)/include
NSPR_INCLUDE_FULL	=$(NSPR_DIR)/include/nspr
ICU_INCLUDE		=$(ICU_DIR)/include
SMARTHEAP_INCLUDE	=$(SMARTHEAP_DIR)/include 

# for the iDS 5.0 Digest Authentication Plugin
IDS_NSPR_INCLUDE	=$(IDS_NSPR_DIR)/include
IDS_NSPR_INCLUDE_FULL	=$(IDS_NSPR_DIR)/include/nspr

#
#JDK_INFORMATION
#
#EXTERNAL_JDK_DIR		= $(EXTERNAL_BASE)/$(HOST_OBJDIR)/jdk
ifeq ($(OS_ARCH),Darwin)
EXTERNAL_JDK_DIR                = /System/Library/Frameworks/JavaVM.framework/Home
else 
EXTERNAL_JDK_DIR		= $(JAVA_HOME)
endif
JAVA				= $(EXTERNAL_JDK_DIR)/bin/java

#Java defines
ifeq ($(OS_ARCH),WINNT)
CLASSPATH_SEP	= ;
else
CLASSPATH_SEP	= :
endif

JDKCLASSES = $(EXTERNAL_JDK_DIR)/lib/tools.jar$(CLASSPATH_SEP)$(EXTERNAL_JDK_DIR)/jre/lib/rt.jar
SYSCLASSPATH = $(JDKCLASSES)

JNI_MD_LIBNAME = jvm
JNI_MD_LIBDIR  = lib
DEFINES += -DJAVA_VERSION=$(JAVA_VERSION) -DJNI_MD_SYSNAME=\"$(JNI_MD_SYSNAME)\"
JNI_INCLUDES = -I$(EXTERNAL_JDK_DIR)/include -I$(EXTERNAL_JDK_DIR)/include/$(JNI_MD_NAME)
JVM_LIBDIR = $(EXTERNAL_JDK_DIR)/jre/$(JNI_MD_LIBDIR)/$(JNI_MD_SYSNAME)/$(JNI_MD_LIBTYPE)

# JavaScript location
JS_DIR		= $(THIRD_PARTY_ROOT)/js
JS_LIBNAME	= js

# DEBUG/OPTIMIZE SETTINGS.  Override as needed in the platform definitions
CC_DEBUG=
C_DEBUG=
LD_DEBUG=

# PROFILE SETTINGS. Override as needed in the platform definitions
CC_PROFILE=
LD_PROFILE=

# BROWSE SETTINGS. Override as needed in the platform definitions
CC_BROWSE=
C_BROWSE=
LD_BROWSE=

# PURIFY SETTINGS. Override as needed.  Define PURIFY to enable
CC_PURIFY=
PRELINK=

# QUANTIFY SETTINGS. Override as needed in the platform definitions.
CC_QUANTIFY=

ifndef NO_STD_IMPORT
SYSTEM_INC	= -I$(WORK_ROOT)/include -I$(INTERNAL_ROOT)/include -I$(INTERNAL_ROOT)/include/support

SYSTEM_LIBDIRS += $(WORK_ROOT)/lib $(INTERNAL_ROOT)/lib
endif #NO_STD_IMPORT

ifdef USE_SETUPSDK
SYSTEM_INC+=-I$(SETUPSDK_DIR)/include
LIBDIRS+=$(SETUPSDK_DIR)/lib
SYSTEM_LIB+=$(SETUPSDK_LIB)
endif

ifdef USE_VERITY
VERITY_COMMON=$(VERITY_DIR)/common
VERITY_DEFINES=-DVDK_CE_ANSI

LIBDIRS+=$(VERITY_DIR)/lib
SYSTEM_INC+=-I$(VERITY_DIR)/include
SYSTEM_LIB+=$(VERITY_LIB)
DEFINES+=$(VERITY_DEFINES)
endif

ifdef USE_MKS
SYSTEM_INC+=-I$(MKS_DIR)/include
LIBDIRS+=$(MKS_DIR)/$(OBJDIR)
SYSTEM_LIB+=$(MKS_LIB)
endif

ifdef USE_SMARTHEAP
DEFINES+=-DUSE_SMARTHEAP
endif

ifdef LINK_SMARTHEAP
LIBDIRS+=$(SMARTHEAP_DIR)/lib
EXE_LIBS+=smartheap_smp smartheapC_smp
endif

ifdef USE_CLIENTLIBS
# unlike other USE_X USE_CLIENTLIBS does not actually link any libraries.
INCLUDES+=-I$(THIRD_PARTY_ROOT)/clientlibs/include -I$(BUILD_ROOT)/../include
LIBDIRS+=$(THIRD_PARTY_ROOT)/clientlibs/lib
CLIENTLIBS=ssl3 smime3 nss3
CLIENTLIB_DIR=$(THIRD_PARTY_ROOT)/clientlibs
endif

ifdef USE_ACCESSLIBS
# unlike other USE_X USE_ACCESSLIBS does not actually link any libraries.
INCLUDES+=-I$(THIRD_PARTY_ROOT)/accesslibs/include \
-I$(THIRD_PARTY_ROOT)/accesslibs/include/libaccess
LIBDIRS+=$(THIRD_PARTY_ROOT)/accesslibs/lib
endif

# don't change that again
LDAP_LIB_VERSION = 50

ifdef USE_LDAPSDK
INCLUDES+=-I$(LDAPSDK_DIR)/include
LIBDIRS+=$(LDAPSDK_DIR)/lib
SYSTEM_LIB+=$(LDAP_LIBS)
endif

ifdef USE_XERCES
INCLUDES+=-I$(INTERNAL_ROOT)/include/xmlparser
INCLUDES+=-I$(INTERNAL_ROOT)/include/xmlparser/dom
SYSTEM_LIB+=xerces-c
LIBDIRS+=$(INTERNAL_ROOT)/lib
endif



ifdef USE_LIBICU
LIBDIRS+=$(ICU_DIR)/lib
DEFINES+=${LIBICU_DEFS}
SYSTEM_INC+=-I$(ICU_INCLUDE)
SYSTEM_LIB+=$(ICU_LIBS)
endif

ifdef USE_JDK
DEFINES += -DUSE_JDK=$(USE_JDK) 
endif

ifndef USE_AUTO_VERSION_INSERTION
SKIP_AUTO_VERSION_INSERTION=1
endif

ifndef SKIP_AUTO_VERSION_INSERTION
ifndef SKIP_VERSION_REGISTRY_LIB
SYSTEM_LIB+=nscpVer
SYSTEM_INC+=-I$(NSPR_INCLUDE)
DEFINES+=-DINCLUDE_AUTO_VERSION_REGISTRATION
endif
endif

ifdef USE_NSPR
LIBDIRS+=$(NSPR_DIR)/lib
SYSTEM_INC+=-I$(NSPR_INCLUDE) -I$(NSPR_INCLUDE_FULL)
SYSTEM_LIB+=$(NSPR_LIB)
endif

CC_INCL		= $(LOCAL_INC) $(PROJECT_INC) $(SUBSYS_INC) \
		  $(SYSTEM_INC) $(PLATFORM_INC) $(INCLUDES) $(LATE_INCLUDES)

CC_DEFS		= $(LOCAL_DEF) $(PROJECT_DEF) $(SUBSYS_DEF) \
		  $(SYSTEM_DEF) $(PLATFORM_DEF) $(DEFINES)

CC_OPTS		= $(LOCAL_CC_OPTS) $(PROJECT_CC_OPTS) $(SUBSYS_CC_OPTS) \
		  $(SYSTEM_CC_OPTS) $(PLATFORM_CC_OPTS)

C_OPTS          = $(LOCAL_C_OPTS) $(PROJECT_C_OPTS) $(SUBSYS_C_OPTS) \
                  $(SYSTEM_C_OPTS) $(PLATFORM_C_OPTS)

LD_LIBDIRS_RAW  = $(LOCAL_LIBDIRS) $(PROJECT_LIBDIRS) $(SUBSYS_LIBDIRS) \
                  $(SYSTEM_LIBDIRS) $(PLATFORM_LIBDIRS) $(LIBDIRS) \
		  $(LATE_LIBDIRS)

ifeq ($(OS_ARCH),WINNT) 
LD_LIBDIRS      = $(addprefix /LIBPATH:, $(LD_LIBDIRS_RAW))
else
LD_LIBDIRS      = $(addprefix -L, $(LD_LIBDIRS_RAW))
endif

LD_LIBS_RAW	= $(LOCAL_LIB) $(PROJECT_LIB) $(SUBSYS_LIB) \
		  $(LIBS) $(SYSTEM_LIB) $(PLATFORM_LIB)
ifeq ($(OS_ARCH),WINNT)
LD_LIBS      	= $(addsuffix .lib, $(LD_LIBS_RAW))
else
LD_LIBS        	= $(addprefix -l, $(LD_LIBS_RAW))
endif

ifneq ($(OS_ARCH),WINNT)
LD_RPATHS      	= $(addprefix $(RPATH_PREFIX), $(LD_RPATH))
endif

LD_OPTS		= $(LOCAL_LD_OPTS) $(PROJECT_LD_OPTS) $(SUBSYS_LD_OPTS) \
		  $(SYSTEM_LD_OPTS) $(PLATFORM_LD_OPTS)

LD_FLAGS	= $(LD_PREFLAGS) $(LD_OPTS) $(LD_LIBDIRS) \
		  $(LD_DEBUG) $(LD_PROFILE) $(LD_BROWSE) $(LD_POSTFLAGS)

CC_FLAGS	= $(CC_PREFLAGS) $(CC_OPTS) $(CC_DEFS) $(CC_INCL) \
		  $(CC_DEBUG) $(CC_PROFILE) $(CC_BROWSE) $(CC_POSTFLAGS)

C++FLAGS	= $(C++PREFLAGS) $(CC_OPTS) $(CC_DEFS) $(CC_INCL) \
		  $(CC_DEBUG) $(CC_PROFILE) $(CC_BROWSE) $(C++POSTFLAGS)

C_FLAGS         = $(C_PREFLAGS) $(C_OPTS) $(CC_DEFS) $(CC_INCL) \
                  $(C_DEBUG) $(C_PROFILE) $(C_BROWSE) $(C_POSTFLAGS)

NOSUCHFILE=/thisfilemustnotexist
