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
# WARNING Don't use the -thread option, use -pthread option for PLATFORM_LD_OPTS
#

include $(BUILD_ROOT)/make/defines_UNIX.mk
LOCAL_COPY=0

CC		= $(PRE_CC) /usr/bin/CC -exceptions -mips3 -n32 -ptused -FE:template_in_elf_section
C               = $(PRE_CC) /usr/bin/cc -mips3 -n32 -ptused -FE:template_in_elf_section
C++C		= $(PRE_CC) /usr/bin/CC -exceptions -mips3 -n32 -ptused -FE:template_in_elf_section
LD		= $(PRE_LD) /usr/bin/ld -mips3 -n32 -ptused -FE:template_in_elf_section
AR              = /usr/bin/ar
RANLIB		= echo ranlib
TAR             = /sbin/tar
FTP		= /usr/bsd/ftp
RCP		= /usr/bsd/rcp
STRIP		= /usr/bin/strip -f
NLIST		= elf

# Verity defines
VERITY_ARCH = _irixn32


ifdef DEBUG_BUILD
CC_DEBUG	= -g
C_DEBUG         = -g
LD_DEBUG	=
else
CC_DEBUG	= -O2
C_DEBUG         = -O2
LD_DEBUG	= -s
endif 

ifndef JAVA_VERSION
JAVA_VERSION    = 2
endif

JNI_MD_LIBTYPE  = classic
JNI_MD_NAME	= irix
JNI_MD_SYSNAME	= sgi
JNI_MD_LIBDIR	= lib32

# Platform specific build options
LD_DYNAMIC	= -shared
ARFLAGS         = -cr
PLATFORM_DEF	= -DIRIX -DIRIX6_2 -DSVR4 -DSYSV -D_REENTRANT -D_VIS_UNICODE
RPATH_PREFIX = -rpath

PLATFORM_LIB	= $(PRE_PLATFORM_LIB)

#

PLATFORM_C_OPTS  = -n32 -DIRIX -DIRIXN32 -DPTHREADS
PLATFORM_CC_OPTS = -n32 -DIRIX -DIRIXN32 -DPTHREADS
PLATFORM_LD_OPTS = -n32 -lpthread


#BUILD_ORACLE=1
##BUILD_SYBASE=1
#BUILD_INFORMIX=1
##BUILD_ODBC=1
NO_SYBASE=1
NO_ODBC=1
