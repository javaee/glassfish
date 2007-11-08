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

# This is the first file included by all makefiles.  Usage should be as follows:
# BUILD_ROOT=./../../ (point to /ns/server)
# MODULE=mymodulename
# include $(BUILD_ROOT)/make/defines.mk
# // the rest of your makefile
# include $(BUILD_ROOT)/make/rules.mk


# This file determines the hardware and OS architecture we're running and the
# build variant that we want, then it includes the other files.

# First we collect some information from UNAME.  To allow speedier makefile
# execution, these can be set in the environment.

ifndef UNAME_REPORTS
UNAME_REPORTS:=$(shell uname)
endif # UNAME_REPORTS

ifndef UNAME_OS_RELEASE
UNAME_OS_RELEASE := $(shell uname -r)
endif #UNAME_OS_RELEASE

ifndef UNAME_OS_ARCH
UNAME_OS_ARCH:=$(subst /,_,$(shell uname -s))
endif #UNAME_OS_ARCH

OS_ARCH=$(UNAME_OS_ARCH)
OS_RELEASE=$(UNAME_OS_RELEASE)
OS_CPU=

# This is your chance to lie to the makefile.  Change OS_ARCH and OS_RELEASE
# as needed for various exceptional cases here.

####################
#### WINDOWS NT ####
####################

# There are many uname's for NT.  We need to get a single OS_CONFIG from them:
# We're going to use "WINNT" as the NT OS_ARCH string...  so if you find
# another, put in the aliasing here.  i.e.
ifeq ($(findstring CYGWIN32_NT, $(OS_ARCH)), CYGWIN32_NT)
OS_ARCH = WINNT
endif
ifeq ($(OS_ARCH),Windows_NT)
OS_ARCH = WINNT
endif

# Force OS release to 4.0 for now so it finds the components for win2k
ifeq ($(OS_ARCH),WINNT)
OS_RELEASE = 4.0
endif

ifeq ($(OS_ARCH),WINNT)
BUILD_ARCH=X86
BUILD_OS=NT
BUILD_VER=4.0
endif

#############
#### AIX ####
#############

ifeq ($(OS_ARCH),AIX)
UNAME_OS_RELEASE := $(shell uname -v).$(shell uname -r)
OS_RELEASE=$(UNAME_OS_RELEASE)

BUILD_ARCH=POWER
BUILD_OS=$(OS_ARCH)
BUILD_VER=$(OS_RELEASE)
endif

##############
#### IRIX ####
#########*####

# Force the IRIX64 machines to use IRIX.
ifeq ($(OS_ARCH),IRIX64)
OS_ARCH = IRIX
endif

ifeq ($(OS_ARCH), IRIX)
BUILD_ARCH=MIPS
BUILD_OS=IRIX
BUILD_VER=6.5
endif

###############
#### SunOS ####
##########*####

ifeq ($(OS_ARCH),SunOS)
BUILD_ARCH=SPARC
BUILD_OS=SOLARIS
# the following is a workaround for SunOS -> Solaris version translation
# only works for Solaris 2.5 and 2.6
ifeq ($(OS_RELEASE),5.6)
BUILD_VER=2.6
endif
ifeq ($(OS_RELEASE),5.7)
BUILD_VER=2.7
endif
ifeq ($(OS_RELEASE),5.8)
BUILD_VER=2.8
endif
ifeq ($(OS_RELEASE),5.9)
# Masquerade as 5.8 until we have 5.9 versions of all /s/b/c components
BUILD_VER=2.8
OS_RELEASE=5.8
endif
ifeq ($(OS_RELEASE),5.10)
BUILD_VER=2.8
OS_RELEASE=5.8
endif
ifeq ($(shell arch), i86pc)
BUILD_ARCH=i86pc
BUILD_VER=2.8
OS_CPU=i86pc
OS_RELEASE=5.8
endif
endif

##############
#### OSF1 ####
#########*####

ifeq ($(OS_ARCH),OSF1)
BUILD_ARCH=ALPHA
BUILD_OS=$(OS_ARCH)
BUILD_VER=$(OS_RELEASE)
endif

###############
#### Linux ####
##########*####

ifeq ($(OS_ARCH), Linux)
ARCH_REPORTS=$(shell arch)
ifeq ($(ARCH_REPORTS), i686)
BUILD_ARCH=x86
else
BUILD_ARCH=UNKNOWN
endif
BUILD_OS=Linux

UNAME_MAJOR_VER=$(word 1, $(subst ., ,$(UNAME_OS_RELEASE)))
UNAME_MINOR_VER=$(word 2, $(subst ., ,$(UNAME_OS_RELEASE)))
OS_RELEASE=$(UNAME_MAJOR_VER).$(UNAME_MINOR_VER)
BUILD_VER=$(OS_RELEASE)
endif

###############
### Mac OS X ##
##########*####

ifeq ($(OS_ARCH), Darwin)
ARCH_REPORTS=$(shell arch)
ifeq ($(ARCH_REPORTS), ppc)
BUILD_ARCH=ppc
else
BUILD_ARCH=UNKNOWN
endif
BUILD_OS=Darwin

UNAME_MAJOR_VER=$(word 1, $(subst ., ,$(UNAME_OS_RELEASE)))
UNAME_MINOR_VER=$(word 2, $(subst ., ,$(UNAME_OS_RELEASE)))
OS_RELEASE=$(UNAME_MAJOR_VER).$(UNAME_MINOR_VER)
BUILD_VER=$(UNAME_MAJOR_VER)
endif

###############
#### HP-UX ####
##########*####

ifeq ($(OS_ARCH), HP-UX)
BUILD_ARCH=HPPA
BUILD_OS=HP-UX
BUILD_VER=B.11.00
endif

ifndef OSVERSION
OSVERSION = $(subst .,0,$(UNAME_OS_RELEASE))
endif #OSVERSION

###############
## OS_CONFIG ##
###############
ifdef OS_CPU
OS_CONFIG = $(OS_ARCH)$(OS_RELEASE)_$(OS_CPU)
else
OS_CONFIG = $(OS_ARCH)$(OS_RELEASE)
endif

PLATFORM=${BUILD_ARCH}_${BUILD_OS}_${BUILD_VER}

###################
## BUILD VARIANT ##
###################

ifndef BUILD_VARIANT
BUILD_VARIANT=DEBUG
endif # BUILD_VARIANT

ifeq ($(BUILD_VARIANT),DEBUG)
DEBUG=1
DEBUG_BUILD=1
endif

ifeq ($(BUILD_VARIANT),OPTIMIZED)
OPTIMIZED_BUILD=1
NPROBE=1
endif

ifeq ($(BUILD_VARIANT),RELEASE)
OPTIMIZED_BUILD=1
NPROBE=1
endif

# ADD NEW BUILD_VARIANT TAGS HERE

ifdef OPTIMIZED_BUILD
OBJDIR_TAG = _OPT
else # OPTIMIZED_BUILD
ifdef DEBUG_BUILD
OBJDIR_TAG = _DBG
else # DEBUG_BUILD
OBJDIR_TAG = _UNKNOWN
endif # DEBUG_BUILD
endif # OPTIMIZED_BUILD

# if we're building java or cross compiling, we don't build the local platform.
# HOST_OBJDIR will point to the  local platform while OBJDIR will point
# to the target platform

HOST_OBJDIR:=$(OS_CONFIG)$(OBJDIR_TAG).OBJ

ifndef BUILD_JAVA
OBJDIR_NAME:=$(HOST_OBJDIR)
OBJDIR:=$(OBJDIR_NAME)
endif

#USE_JDK=1

include $(BUILD_ROOT)/make/defines_COMMON.mk
include $(BUILD_ROOT)/make/defines_${PLATFORM}.mk

# we assume PROJECT is iASSE if it's not already defined
ifndef PROJECT
PROJECT = iASSE
endif

ifdef PROJECT
include $(BUILD_ROOT)/make/defines_$(PROJECT).mk
endif

JAVA_OBJDIR=JDK1.$(JAVA_VERSION)$(OBJDIR_TAG).OBJ

ifdef BUILD_JAVA
OBJDIR_NAME:=$(JAVA_OBJDIR)
OBJDIR:=$(OBJDIR_NAME)
endif

ifdef BUILD_JAVA
include $(BUILD_ROOT)/make/defines_JAVA_JDK_1.$(JAVA_VERSION).mk
endif
