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
# Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
# Use is subject to license terms.
#

# This file determines the hardware and OS architecture we're running and the
# build variant that we want, then it includes the other files.

#Prune empty directories at checkout time
CVS_POST_CHECKOUT_CMD=-P

#
# BUILD_VARIANT can be either DEBUG or OPTIMIZED
#
ifndef BUILD_VARIANT
# default case
BUILD_VARIANT = DEBUG
endif

#
# SECURITY_POLICY is either DOMESTIC or EXPORT
#
ifndef SECURITY_POLICY
SECURITY_POLICY = DOMESTIC
endif

#
# BIT_VARIANT can be either 32 or 64
#
ifndef BIT_VARIANT
BIT_VARIANT=32
else
ifeq (${BIT_VARIANT},64)
BIT_VARIANT=64
else
$(error "Invalid BIT_VARIANT is specified here.")
endif
endif # BIT_VARIANT

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
#INSTALL_DIR=/opt/appserver

# This is your chance to lie to the makefile.  Change OS_ARCH and OS_RELEASE
# as needed for various exceptional cases here.

####################
#### WINDOWS NT ####
####################

# There are many uname's for NT.  We need to get a single OS_CONFIG from them:
# We're going to use "WINNT" as the NT OS_ARCH string.  so if you find
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
BUILD_OS=SOLARIS
ifeq ($(BIT_VARIANT),64)
BUILD_ARCH=SPARCV9
BUILD_VER=2.8
OS_RELEASE=5.8
OS_CPU=64
else
BUILD_ARCH=SPARC
endif
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
BUILD_VER=2.8
OS_RELEASE=5.8
ifeq ($(BIT_VARIANT),64)
BUILD_ARCH=AMD64
OS_CPU=64
else
BUILD_ARCH=i86pc
OS_CPU=i86pc
endif
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

#############
## MacOSX ###
#############

ifeq ($(OS_ARCH), Darwin)

BUILD_ARCH=ppc
BUILD_OS=$(OS_ARCH)
UNAME_MAJOR_VER=$(word 1, $(subst ., ,$(UNAME_OS_RELEASE)))
UNAME_MINOR_VER=$(word 2, $(subst ., ,$(UNAME_OS_RELEASE)))
OS_RELEASE=$(UNAME_MAJOR_VER).$(UNAME_MINOR_VER)
# for now, we do not bootstrap the jdk on Mac OS X. it is part
# of the installed components of the OS.
NO_JDK=true
endif

###############
## OS_CONFIG ##
###############
ifdef OS_CPU
OS_CONFIG = $(OS_ARCH)$(OS_RELEASE)_$(OS_CPU)
else
OS_CONFIG = $(OS_ARCH)$(OS_RELEASE)
endif

PLATFORM=${BUILD_ARCH}_${BUILD_OS}_${BUILD_VER}

ifeq ($(OS_ARCH),WINNT)
BOOTSTRAP_ERRORS=/dev/nul
else
BOOTSTRAP_ERRORS=/dev/null
endif

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

ifdef NO_ANT
OBJDIR_NAME:=$(HOST_OBJDIR)
OBJDIR:=$(OBJDIR_NAME)
endif

ifndef JAVA_VERSION
JAVA_VERSION = 5
endif

JAVA_OBJDIR=JDK1.$(JAVA_VERSION)$(OBJDIR_TAG).OBJ

ifndef NO_ANT
OBJDIR_NAME:=$(JAVA_OBJDIR)
OBJDIR:=$(OBJDIR_NAME)
endif

# comment the following line out when debugging the build system. It
# will cause all the commands to be echoed.
AT=@

# Commands

ifeq ($(OS_ARCH),SunOS)
TOOL_ROOT =/usr
CP        =$(TOOL_ROOT)/bin/cp
ifeq ($(BUILD_ARCH),i86pc)
#CVS       =/tools/ns-arch/i386_unknown_solaris2.6/bin/cvs
CVS       =/usr/sfw/bin/cvs
cvs_path  =$(shell ls $(CVS) 2> $BOOTSTRAP_ERRORS)
ifneq ($(cvs_path),$CVS)
# set cvs to this path if default path not accessible
CVS      =/tools/ns/bin/cvs
endif
cvs_path  =$(shell ls $(CVS) 2> $BOOTSTRAP_ERRORS)
ifneq ($(cvs_path),$CVS)
# set cvs to local path if any of above paths not accessible
CVS      =cvs
endif

else
#CVS       =/usr/dist/exe/cvs
CVS       =/usr/dist/share/cvs,v1.11.5/bin/cvs
endif
ECHO      =$(TOOL_ROOT)/bin/echo
LN        =$(TOOL_ROOT)/bin/ln
MKDIR     =$(TOOL_ROOT)/bin/mkdir
RM        =$(TOOL_ROOT)/bin/rm
SED       =$(TOOL_ROOT)/bin/sed
SHELL     =$(TOOL_ROOT)/bin/sh
TR        =$(TOOL_ROOT)/bin/tr
UNZIP     =$(TOOL_ROOT)/bin/unzip
ZIP       =$(TOOL_ROOT)/bin/zip
CHMOD     =$(TOOL_ROOT)/bin/chmod
endif

ifeq ($(OS_ARCH), Linux)
CP      =/bin/cp
CVS     =/usr/bin/cvs
ECHO    =/bin/echo -e
LN      =where_is_ln_on_linux
MKDIR   =/bin/mkdir
RM      =/bin/rm
SED     =/bin/sed
SHELL   =/bin/sh
TR      =/usr/bin/tr
UNZIP   =/usr/bin/unzip
ZIP     =/usr/bin/zip
CHMOD     =/bin/chmod
endif

ifeq ($(OS_ARCH), Darwin)
CP      =/bin/cp
CVS     =/usr/bin/cvs
ECHO    =/bin/echo
LN      =where_is_ln_on_linux
MKDIR   =/bin/mkdir
RM      =/bin/rm
SED     =/bin/sed
SHELL   =/bin/sh
TR      =/usr/bin/tr
UNZIP   =/usr/bin/unzip
ZIP     =/usr/bin/zip
CHMOD     =/bin/chmod
endif


ifeq ($(OS_ARCH),WINNT) 
ifndef TOOL_ROOT
TOOL_ROOT =p:
endif
CP        =$(TOOL_ROOT)/bin/cp.exe
CVS       =$(TOOL_ROOT)/bin/cvs.exe
ECHO      =$(TOOL_ROOT)/bin/echo.exe
MKDIR     =$(TOOL_ROOT)/bin/mkdir.exe
RM        =$(TOOL_ROOT)/bin/rm.exe
SED       =$(TOOL_ROOT)/bin/sed.exe
SHELL     =$(TOOL_ROOT)/bin/sh.exe
TR        =$(TOOL_ROOT)/bin/tr.exe
UNZIP     =$(TOOL_ROOT)/bin/unzip.exe
ZIP       =$(TOOL_ROOT)/bin/zip.exe
CHMOD     =$(TOOL_ROOT)/bin/chmod.exe
endif

MKDIR_DASH_P  =$(MKDIR) -p
BS_ECHO = $(ECHO) "[bootstrap]"

ifneq ($(OS_ARCH),WINNT)
CUR_DIR=$(shell pwd)
else
# PWD on NT appends CRLF which we must strip:
CUR_DIR=$(shell pwd | $(TR) -d "\r\n")
endif

ifndef COMPONENT_NAME
COMPONENT_NAME=$(shell basename $(CUR_DIR))
endif

MAKE_CMD=$(MAKE) MAKEFLAGS='$(MAKEFLAGS)' BUILD_VARIANT=$(BUILD_VARIANT) SECURITY_POLICY=$(SECURITY_POLICY)

ifdef ALLOW_CROSS_COMPONENT_BUILDS
ifdef EXTERNAL_MAKEFILE
MAKE_CMD+=-f $(EXTERNAL_MAKEFILE)
endif
endif

ifdef NO_RECURSION
MAKE_CMD+=NO_RECURSION=$(NO_RECURSION)
endif

ifdef ALLOW_CROSS_COMPONENT_BUILDS
MAKE_CMD+=ALLOW_CROSS_COMPONENT_BUILDS=$(ALLOW_CROSS_COMPONENT_BUILDS)
endif

BS_MAKE=$(MAKE_CMD) --no-print-directory

ifndef GMAKE_BUILD_FILE
GMAKE_BUILD_FILE = Makefile.$(COMPONENT_NAME)
endif
STD_GMAKE_OPTIONS = COMPONENT_NAME=$(COMPONENT_NAME)
STD_GMAKE_OPTIONS += PUBLISH_ROOT=$(PUBLISH_ROOT) HOST_OBJDIR=$(HOST_OBJDIR)
STD_GMAKE_OPTIONS += JAVA_OBJDIR=$(JAVA_OBJDIR) PUBLISH_HOME=$(PUBLISH_HOME)

STD_GMAKE_CMD = $(MAKE_CMD) $(STD_GMAKE_OPTIONS) -f $(GMAKE_BUILD_FILE)

ifndef NO_ANT

#
# defs for ant
#
ifeq ($(OS_ARCH), Darwin)
ANT_JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
else
ANT_JAVA_HOME=$(CUR_DIR)/$(PUBLISH_ROOT)/$(HOST_OBJDIR)/jdk
endif
ANT_HOME=$(CUR_DIR)/$(PUBLISH_HOME)/ant

ifeq ($(OS_ARCH),WINNT)
ANT_BIN=ant.bat
else
ANT_BIN=ant
endif

ifndef ANT_BUILD_FILE
ANT_BUILD_FILE = build.xml
endif

STD_ANT_OPTIONS  = -buildfile $(ANT_BUILD_FILE)
STD_ANT_OPTIONS += -Djava.obj.dir=$(JAVA_OBJDIR)
STD_ANT_OPTIONS += -Dhost.obj.dir=$(HOST_OBJDIR)
#STD_ANT_OPTIONS += -Dcomponent.name=$(COMPONENT_NAME)
STD_ANT_OPTIONS += -Dpublish.home=$(PUBLISH_HOME)
STD_ANT_OPTIONS += -Dpublish.root=$(PUBLISH_ROOT)

#ifdef INSTALL_EE_DIR
#STD_ANT_OPTIONS += -Dinstall.dir=$(INSTALL_EE_DIR)
#STD_ANT_OPTIONS += -Dee.assembled=true
#else
#STD_ANT_OPTIONS += -Dinstall.dir=$(INSTALL_DIR)
#endif


ifdef ANT_DEBUG
STD_ANT_OPTIONS += -debug
endif

ifeq ($(BUILD_NATIVE_PACKAGES),true)
#STD_ANT_OPTIONS += -Dsvr4.build=true
#STD_ANT_OPTIONS += -Drpm.build=true
STD_ANT_OPTIONS += -Dnative.build=true
else
#STD_ANT_OPTIONS += -Dsvr4.build=false
#STD_ANT_OPTIONS += -Drpm.build=false
STD_ANT_OPTIONS += -Dnative.build=false
endif

ifeq ($(BUILD_VARIANT), OPTIMIZED)
STD_ANT_OPTIONS += -Djavac.debug=on -Djavac.optimize=off
#even if the top-level build is "optimized", java-build is "debug" for now -- decided on 10/27/04
else
STD_ANT_OPTIONS += -Djavac.debug=on -Djavac.optimize=off
endif

ifdef ANT_DEBUG
STD_ANT_OPTIONS += -debug
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

# Ant build command
RUN_ANT=$(BS_MAKE) $(ANT_ENV) antit

# Ant clobber command
RUN_ANT_CLOBBER=$(BS_MAKE) $(ANT_ENV) ANT_TARGETS=clobber antit
endif

#-----------------------------------------------------------------------------
# CVS checkout 
#-----------------------------------------------------------------------------

JWS_CVS_ROOT_FILE := $(BUILD_ROOT)/../bootstrap/CVS/Root
JWS_CVS_ROOT := $(shell cat $(JWS_CVS_ROOT_FILE) | $(TR) -d "\r\n")

## The following is the right way to do it.  However, our current
## iastools.zip has a really old version of sed that doesn't
## seem to be handle .* patterns.
#JWS_CVS_USER := $(shell cat $(JWS_CVS_ROOT_FILE) | $(SED) -e 's/:pserver://' | $(SED) -e 's/@.*//g')

## Workaround for the problem explained above.  Using hard coded CVSROOT
## QQ FIXME: I am taking out the SED processing since it does not work well
## on Windows with our current version of iastools/sed.exe.  The user now
## is required to specify their own username if they want to do BUILD_ALL_SRC=1
#JWS_CVS_USER := $(shell cat $(JWS_CVS_ROOT_FILE) | $(SED) -e 's/:pserver://g' | $(SED) -e 's!@redcvs.red.iplanet.com:/m/jws!!g')

APACHE_CVS_ROOT := :pserver:anoncvs@cvs.apache.org:/home/cvspublic
#JEEVES_CVS_ROOT := :pserver:$(JWS_CVS_USER)@jeeves.sfbay.sun.com:/jeeves
#JWS_MIRROR_CVS_ROOT := :pserver:$(JWS_CVS_USER)@redcvs.red.iplanet.com:/m/jws-mirror

## The following is the workaround for the SED problem
JEEVES_CVS_ROOT := :pserver:$(USER)@jeeves.sfbay.sun.com:/jeeves
JWS_MIRROR_CVS_ROOT := :pserver:$(USER)@redcvs.red.iplanet.com:/m/jws-mirror
JSE_CVS_ROOT := :pserver:$(USER)@jse.east.sun.com:/jse
SUNSW_CVS_ROOT := :pserver:$(USER)@sunsw.sfbay.sun.com:/sw/wpts

ifdef COMPONENT_CVS_MODULE

ifndef EXTERNAL_DEPENDENCY
CVS_CHECKOUT_DIR=$(WORKSPACE_DIR)
else
CVS_CHECKOUT_DIR=./src
endif

ifneq ($(COMPONENT_CVS_DEST),)
CVS_CHECKOUT_DIR=$(COMPONENT_CVS_DEST)
endif

CVS_CHECKOUT_CMD = $(CVS)
ifneq ($(COMPONENT_CVS_ROOT),)
CVS_CHECKOUT_CMD += -d $(COMPONENT_CVS_ROOT)
else
CVS_CHECKOUT_CMD += -d $(JWS_CVS_ROOT)
endif

ifdef CVS_EXTRA
CVS_CHECKOUT_CMD += $(CVS_EXTRA)
endif

CVS_CHECKOUT_CMD += checkout

ifdef CVS_POST_CHECKOUT_CMD
CVS_CHECKOUT_CMD += $(CVS_POST_CHECKOUT_CMD)
endif

#
# Choose the tag to checkout based on whether this is checkout or rel-checkout
#
CVS_CHECKOUT_TAG =
ifdef USE_CVS_RELTAG
CVS_CHECKOUT_TAG = $(COMPONENT_CVS_RELTAG)
else
CVS_CHECKOUT_TAG = $(COMPONENT_CVS_DEVBRANCH)
endif

ifneq ($(CVS_CHECKOUT_TAG),)
CVS_CHECKOUT_CMD += -r $(CVS_CHECKOUT_TAG)
endif

CVS_CHECKOUT_CMD += $(COMPONENT_CVS_MODULE)

endif


#=============================================================================
# Global/common definitions
#=============================================================================

WORKSPACE_DIR = ..

PUBLISH_ROOT = $(BUILD_ROOT)/$(WORKSPACE_DIR)/publish
PUBLISH_HOME = $(PUBLISH_ROOT)/$(OBJDIR_NAME)
COMPONENT_PUBLISH_DIR = $(PUBLISH_HOME)/$(COMPONENT_NAME)
CHECKOUT_INFO_ROOT = $(PUBLISH_ROOT)/checkout-info
VERSION_FILE = bootstrap.version
PARENT_FILE = bootstrap.parent
DEPENDENCY_FILE = dependencies.mk

# truly external components, such as jdk, ant, nspr etc
EXTERNAL_COMPONENTS_DIR=/net/redbuild.red.iplanet.com/export/builds/components

# another location for the external components
EXTERNAL_INTEGRATION_DIR=/net/redbuild.red.iplanet.com/export/builds/integration

## JWS Official Build Area
ifndef JWS_COMPONENTS_DIR 
JWS_COMPONENTS_DIR=/net/koori.sfbay/onestop/sjsas_pe/9.0
endif

# nightly builds location.
# XXX FIXME The following location should be modified once we start build PE
# on the main branch
ifndef NIGHTLY_ROOT
NIGHTLY_ROOT=$(JWS_COMPONENTS_DIR)/nightly/binaries
endif

ifndef NIGHTLY_DIR

ifeq ($(OS_ARCH),Linux)
NIGHTLY_DIR=$(NIGHTLY_ROOT)/linux/latest
endif

ifeq ($(OS_ARCH),SunOS)
ifeq ($(BUILD_ARCH),i86pc)
NIGHTLY_DIR=$(NIGHTLY_ROOT)/solaris-i586/latest
endif
endif

ifeq ($(OS_ARCH),SunOS)
ifeq ($(BUILD_ARCH),SPARC)
NIGHTLY_DIR=$(NIGHTLY_ROOT)/solaris/latest
endif
ifeq ($(BUILD_ARCH),SPARCV9)
NIGHTLY_DIR=$(NIGHTLY_ROOT)/solaris/latest
endif
endif

ifeq  ($(OS_ARCH),WINNT)
NIGHTLY_DIR=$(NIGHTLY_ROOT)/windows/latest
endif

ifeq  ($(OS_ARCH),Darwin)
NIGHTLY_DIR=$(NIGHTLY_ROOT)/linux/latest
endif

endif

# CANON PE external components location
ifndef JWS_EXTERNAL_COMPONENTS_DIR 
JWS_EXTERNAL_COMPONENTS_DIR=/net/koori.sfbay/onestop/s1aspe/8.0/external
endif

#
# Temporary location (where S1AS picks up the jars from)
# All these should move to either EXTERNAL_COMPONENTS_DIR or 
# INTERNAL_COMPONENTS_DIR
#
TMP_EXTERNAL_DIR:=/net/redbuild.red.iplanet.com/export/builds/aas/iAS70SEBuild/external

source_binary_dependencies = $(binary_dependencies) $(source_dependencies)

bootstrap_targets = $(addprefix get-,$(source_binary_dependencies))
ifndef NO_RECURSION
bootstrap_targets += $(addprefix rget-,$(source_binary_dependencies))
endif

external_checkout_targets  = $(addprefix external-, $(external_dependencies))
checkout_targets  = $(external_checkout_targets)
checkout_targets += $(addprefix checkoutsrc-,$(source_dependencies))
ifndef NO_RECURSION
checkout_targets += $(addprefix rcheckoutsrc-,$(source_dependencies))
endif

ifeq ($(SECURITY_POLICY),DOMESTIC)
EXPORT_DOMESTIC_TAG=D
else
EXPORT_DOMESTIC_TAG=E
endif

#=============================================================================
# Dependency definitions
#=============================================================================

ifndef EXTERNAL_DEPENDENCY_FILE
-include $(BUILD_ROOT)/$(DEPENDENCY_FILE)
else
ifeq ($(EXTERNAL_DEPENDENCY_FILE),)
-include $(BUILD_ROOT)/$(DEPENDENCY_FILE)
else
include $(EXTERNAL_DEPENDENCY_FILE)
endif
endif

##############################################################################
#  jdk
##############################################################################

ifndef USING_ANT
ifndef NO_JDK
binary_dependencies += jdk
jdk_rootdir=$(JWS_EXTERNAL_COMPONENTS_DIR)
jdk_dir=jdk
ifndef jdk_version
jdk_version=1.5.0_01
endif
jdk_subdir=$(OS_ARCH)
ifeq  ($(OS_ARCH),SunOS)
ifeq ($(BUILD_ARCH),i86pc)
jdk_subdir=$(OS_ARCH)_X86
endif
endif
jdk_destdir=$(HOST_OBJDIR)/$(jdk_dir)
endif
endif

##############################################################################
# ant
##############################################################################

ifndef USING_ANT
ifndef NO_ANT
binary_dependencies += ant
ant_rootdir = $(JWS_EXTERNAL_COMPONENTS_DIR)
ant_dir = ant
ifndef ant_version
ant_version=1.5.4
endif
ant_subdir=.
ant_destdir=$(JAVA_OBJDIR)/ant
endif
endif
#################################################
# Bundled JDK
#################################################
# For PE we bundle JDK 1.4.2_x for ALL platforms
ifndef PRINCIPAL_PLATFORMS_BUNDLED_JDK_VERSION
PRINCIPAL_PLATFORMS_BUNDLED_JDK_VERSION=1.5.0_01
endif
ifndef MAC_BUNDLED_JDK_VERSION
MAC_BUNDLED_JDK_VERSION=1.4.2_06
endif
