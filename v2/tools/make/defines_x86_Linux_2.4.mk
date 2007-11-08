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

PERL_DIR        =/usr
PERL5         	=$(PERL_DIR)/bin/perl
PERL		=$(PERL5)
CC		=$(PRE_CC) /usr/bin/g++
C               =$(PRE_C)  /usr/bin/gcc
C++C		=$(CC)
AR              =/usr/bin/ar
TAR             =/bin/tar
RANLIB          =/usr/bin/ranlib
LD		=$(PRE_LD) /usr/bin/g++
ZIP		=/usr/bin/zip
UNZIP		=/usr/bin/unzip
GZIP		=/bin/gzip
GUNZIP		=/bin/gunzip
SHELL		=/bin/sh
DATE		=/bin/date
MKDIR		=/bin/mkdir
TOUCH		=/bin/touch
CHMOD		=/bin/chmod
CP		=/bin/cp
MV		=/bin/mv
ECHO		=/bin/echo -e
SED		=/bin/sed
MKDIR		=/bin/mkdir
LN		=/bin/ln -f
NMAKE		=/usr/bin/make -f
STRIP		=/usr/bin/strip -x
RM		=/bin/rm

# we don't have that stuff
NO_SEARCH=1
NO_WAI=1

ifdef DEBUG_BUILD
# optimize to catch more warnings
#CC_DEBUG	= -g2 -Wall -O1
#C_DEBUG         = -g2 -Wall -O1
# easy on the warnings for now
CC_DEBUG	= -g
C_DEBUG         = -g
LD_DEBUG	=
else 
# optimized settings here
CC_DEBUG	= -W -O2
C_DEBUG         = -W -O2
LD_DEBUG	= -s
endif

LD_DYNAMIC	= -shared
RPATH_PREFIX	= -Wl,-rpath,
RPATH_ORIGIN	= \$$ORIGIN

PLATFORM_DEF	= -DLinux -DLINUX -D__LINUX__=2 -D__LINUX_MINOR=4 -D__LINUX_SUBMINOR=7 -D__GLIBC_MAJOR=2 -D__GLIBC_MINOR=2 -D_REENTRANT

# -verbose for printing all informational messags
# -w0 for stricter than ANSI-C prototype warnings
# -fPIC is needed for any code that ends up in a shared library
PLATFORM_CC_OPTS = -fPIC
PLATFORM_C_OPTS  = -fPIC
PLATFORM_LD_OPTS =

# These libraries are platform-specific, not system-specific
# WARNING Don't use the -thread option, use -pthread option
PLATFORM_LIB	 += $(PRE_PLATFORM_LIB) pthread dl crypt resolv

ifndef JAVA_VERSION
JAVA_VERSION	= 5
endif

JNI_MD_LIBTYPE  = client
JNI_MD_NAME	= linux
JNI_MD_SYSNAME	= i386

# force native threads to be used at build runtime
export THREADS_FLAG=native

# No support for mapfiles
USE_MAPFILE=

