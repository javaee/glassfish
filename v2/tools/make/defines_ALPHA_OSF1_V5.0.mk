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

CC		=$(PRE_CC) /usr/bin/cxx
C               =$(PRE_C)  /usr/bin/cc
C++C		=$(CC)
AR              =/usr/bin/ar
RANLIB          =/usr/bin/ranlib
LD		=$(PRE_LD) /usr/bin/ld
FTP		=/usr/bin/ftp
STRIP		=/usr/bin/strip

# Verity defines
VERITY_ARCH =_aosf40

ifdef DEBUG_BUILD
CC_DEBUG	= -g -O0
C_DEBUG         = -g
LD_DEBUG	=
else 
# optimized settings here
CC_DEBUG	= -O4
C_DEBUG         = -O4
LD_DEBUG	= -s
endif

# Used for shared libraries - don't put -pthread here, use PLATFORM_LD_OPTS
#
LD_DYNAMIC	= -shared 

ifndef JAVA_VERSION
JAVA_VERSION    = 2
endif

JNI_MD_LIBTYPE  = classic
JNI_MD_NAME	= alpha
JNI_MD_SYSNAME  = alpha

PLATFORM_DEF	= -DSVR4 -DSYSV -D_REENTRANT
RPATH_PREFIX = -rpath 

# -verbose for printing all informational messages (LOTS of 'em)
# -w0 for stricter than ANSI-C prototype warnings
#
# WARNING Don't use the -thread option, use -pthread option for PLATFORM_LD_OPTS
#
# The following is needed until 19980608 version of clientlibs (1.53) is
# straightened out
FOR_HCL153=-DOSF1V4D
PLATFORM_CC_OPTS = -DOSF1 -DOSF1V4_0 -DPTHREADS $(FOR_HCL153)
PLATFORM_CC_OPTS+= -D__V40_OBJ_COMPAT
#PLATFORM_CC_OPTS+= -w0
PLATFORM_CC_OPTS+= -verbose
PLATFORM_CC_OPTS+= -error_limit 10
# The following is needed by V6.0 cxx to suppress many spurious messages; it
# is claimed to be fixed in the next release, so this option will not be needed.
# PLATFORM_CC_OPTS+= -msg_disable no_access_to_constructors
# The above was disabled June 25: T6.1 cxx compiler complains about it.
ifdef SKIP_ORB_WARNINGS
PLATFORM_CC_OPTS+= -msg_disable code_is_unreachable
PLATFORM_CC_OPTS+= -msg_disable useless_type_qualifier_on_return_type
PLATFORM_CC_OPTS+= -msg_disable integer_truncated
PLATFORM_CC_OPTS+= -msg_disable cast_to_qualified_type
PLATFORM_CC_OPTS+= -msg_disable integer_sign_change

endif
PLATFORM_C_OPTS  = -warnprotos -DOSF1 -DOSF1V4_0 -DPTHREADS $(FOR_HCL153)
PLATFORM_C_OPTS+= -D__V40_OBJ_COMPAT
#PLATFORM_C_OPTS+=-w0 
PLATFORM_C_OPTS+= -verbose 
PLATFORM_C_OPTS+= -std 
PLATFORM_LD_OPTS = -pthread

# template database location.
ifndef NO_STD_DATABASE_DEFINE
TEMPLATE_DATABASE_DIR=$(OBJDIR)
endif

ifdef TEMPLATE_DATABASE_DIR
PLATFORM_CC_OPTS += -ptr $(TEMPLATE_DATABASE_DIR)
LD_DYNAMIC       += -ptr $(TEMPLATE_DATABASE_DIR)
PLATFORM_LD_OPTS += -ptr $(TEMPLATE_DATABASE_DIR)
endif

# These libraries are platform-specific, not system-specific
#  add the math library to the link line for floor and sqrt
#  Jaime Delgadillo
PLATFORM_LIB	 += $(PRE_PLATFORM_LIB) cxxstd cxx mach exc c_r m

#BUILD_ORACLE=1
#BUILD_SYBASE=1
#BUILD_INFORMIX=1
##BUILD_ODBC=1
NO_ODBC=1

# No support for mapfiles
USE_MAPFILE=

