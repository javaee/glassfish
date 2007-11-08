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

# xlC_r will compile both C and C++ code
#
# Do NOT add compiler options to any of these lines
# These macros define the compiler, not compiler options/flags/etc.
#
#CC		= $(PRE_CC) /usr/bin/xlC_r
#CC		= $(PRE_CC) /usr/local/bin/gcc
CC		= $(PRE_CC) /usr/bin/gcc
CPPCMD	=/usr/ccs/lib/cpp -P
#C 		= $(PRE_CC) /usr/bin/xlC_r
#C		= $(PRE_CC) /usr/local/bin/gcc
C		= $(PRE_CC) /usr/bin/gcc
#C++C		= $(PRE_CC) /usr/bin/xlC_r
#C++C		= $(PRE_CC) /usr/local/bin/gcc
C++C		= $(PRE_CC) /usr/bin/gcc
LD		= $(PRE_LD) /usr/bin/ld
AR		= /usr/bin/ar cr $@
# fix linkage problems on NFS
#RANLIB		= /usr/bin/ranlib
RANLIB		= sleep 10; /bin/ranlib
FTP		= /usr/bin/ftp
#STRIP		= /bin/strip
# on AIX strip is broken
STRIP		= /bin/touch

# what is NLIST?
NLIST		 = xcoff

# Verity defines
VERITY_ARCH =_rs6k41

ifdef DEBUG_BUILD
#CC_DEBUG	= -g -qfullpath -qsrcmsg # -qsrcmsg outputs source on error
CC_DEBUG	=   
#C_DEBUG         = -g -qfullpath -qsrcmsg # -qfullpath for debug dbx path
C_DEBUG         = -g 
LD_DEBUG	=
else
# optimized flags here
CC_DEBUG	= -O2
C_DEBUG         = -O2
LD_DEBUG	= -s
endif

#PRELIB		= -brtl

ifndef JAVA_VERSION
JAVA_VERSION    = 2
endif

JNI_MD_LIBTYPE  = classic
JNI_MD_LIBDIR   = bin

JAVA            = $(EXTERNAL_JDK_DIR)/jre/sh/java
JAVAC           = $(EXTERNAL_JDK_DIR)/sh/javac
JAVAH           = $(EXTERNAL_JDK_DIR)/sh/javah
JAR             = $(EXTERNAL_JDK_DIR)/sh/jar
JAVADOC         = $(EXTERNAL_JDK_DIR)/sh/javadoc

# Shared library build
DLL_PRESUF        = 
#ES_RPATH          = .:../lib:../../lib
ES_RPATH          = 
#DEF_LIBPATH       = /usr/lib/threads:/usr/ibmcxx/lib:/usr/lib:/lib
#DEF_LIBPATH       = /usr/lib/threads:/usr/lib:/lib
DEF_LIBPATH       = 
LD_RPATH          = $(DEF_LIBPATH):$(ES_RPATH)
#LD_DYNAMIC        = -p 0 -berok -blibpath:$(LD_RPATH)
LD_DYNAMIC        = -p 0 -berok
#MKSHLIB           = /usr/ibmcxx/bin/makeC++SharedLib_r $(LD_DYNAMIC)
MKSHLIB		= $(PRE_LD) /usr/bin/ld
#PLATFORM_CC_OPTS += -qansithrow

PLATFORM_DEF	= -DSVR4 -DSYSV -DAIX -DAIXV4 -D_AIX42 -DAIX4_3 -D_REENTRANT -DCOMPAT_43
#RPATH_PREFIX = -blibpath:
RPATH_PREFIX = 

################
#### Xerces ####
################
XERCES_DEFS += -qnotempinc -D_THREAD_SAFE

# xlC_r already calls libpthreads, libC_r and libc_r in the correct order
# (see /etc/xlC.cfg) - 98/06/23 jsalter
#
# Add PRE_PLATFORM_LIB on AIX to support defining libpthreads before libc_r
# when necessary - 98/06 jsalter (this could probably be useful on other
# platforms as well)
#
PLATFORM_LIB	 += $(PRE_PLATFORM_LIB) dl

LD_LIB_VAR        = LIBPATH

#BUILD_ORACLE=1
#BUILD_SYBASE=1
#BUILD_INFORMIX=1
#BUILD_ODBC=1

# No support for mapfiles
USE_MAPFILE=

