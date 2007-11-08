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

###########################################################################
# defines.HPPA_HP-UX_B.11.00.mk for Enterprise and Messaging Servers
#
# 05/02/98 (J. Salter)
#         2         3         4         5         6         7         8
#12345678901234567890123456789012345678901234567890123456789012345678901234
###########################################################################

include $(BUILD_ROOT)/make/defines_UNIX.mk
LOCAL_COPY=1

CC		 = $(PRE_CC) /opt/aCC/bin/aCC 
C++C		 = $(PRE_CC) /opt/aCC/bin/aCC
LD               = $(PRE_LD) /opt/aCC/bin/aCC
C		 = $(PRE_CC) /usr/bin/cc

# Tool locations
AR              =/usr/bin/ar
RANLIB          =/usr/bin/ranlib
FTP		=/usr/bin/ftp
# /usr/bin/cp -r does not follow symbolic links on HP
CP_R	=/usr/local/bin/cp -r
STRIP		=/usr/bin/strip -x

NMAKE		=$(TOOL_ROOT)/local/bin/gmake -f

# what is NLIST?
NLIST		= elf

# Verity defines
VERITY_ARCH =_hpux11

# CC_DEBUG defined in defines_UNIX.mk
# -Wall will fail on native (non-GNU) compilers so don't reference here
#
ifdef DEBUG_BUILD
CC_DEBUG	= -g
C_DEBUG		= -g
LD_DEBUG	= -g
else
CC_DEBUG	= -O
C_DEBUG		= -O
LD_DEBUG	= -s 
endif

# We need to investigate what path we should give after +b option before
# we include the +b option. For now just using +s
#
# jsalter: please don't define the path here as it will probably
#          be different on a build-by-build basis
#
LD_DYNAMIC	= +Z -b -Wl,+s -Wl,-B,symbolic

ifndef JAVA_VERSION
JAVA_VERSION	= 2
endif

JNI_MD_LIBTYPE  = classic
JNI_MD_NAME	= hp-ux
JNI_MD_SYSNAME  = PA_RISC2.0

##   replace -D_REENTRANT with -D_POSIX_C_SOURCE=199506L
##   option being phased out 
##   add _HPUX_SOURCE for "strings" support
##   jaimed@migration.com
##

# Per message from jsalter, per message from saleem
# remove +DS1.0 +DA1.0, replace with +DS1.1 +DAportable
# the claim is that HPUX 11 is not supported on
# architectures on which these flags are not safe.
# actual replacement was +DA1.1 -> +DAportable. XXXJBS
#
#   add +W503   to ignore future errors for tinderbox
#         641,740,749  roguewave warnings...
#
# jsalter: XP_UNIX removed - properly defined in defines_UNIX.mk
#

PLATFORM_DEF   = +Z +DAportable +DS2.0 -DSVR4 \
	-DSYSV -D_POSIX_C_SOURCE=199506L -DHPUX  \
	-DRW_MULTI_THREAD -D_VIS_UNICODE -D_HPUX_SOURCE 
RPATH_PREFIX = -Wl,+b,

# jsalter: PLATFORM_{C,CC}_OPTS is in addition to the PLATFORM_DEF macro
#          Do not include PLATFORM_DEF in PLATFORM_{C,CC}_OPTS
#
PLATFORM_CC_OPTS = +W503,251,740,749,641 
# The following are for the JNI c++ interface code
PLATFORM_CC_OPTS += -DNATIVE -D_HPUX -ext +u4
PLATFORM_C_OPTS =   -DNATIVE -D_HPUX -Ae +u4

PLATFORM_INC	= -I/opt/aCC/include/iostream

PLATFORM_LD_OPTS = -Wl,+s

# do not include c, C libraries; aCC takes care of it: mohideen@cup.hp.com
# Add PRE_PLATFORM_LIB - jsalter
PLATFORM_LIB    = $(PRE_PLATFORM_LIB) nsl pthread lwp rt

ifdef USE_PCH
ifndef HP_NO_CORBA_PCH
PCH_FILE = precomphdr
PLATFORM_DEF += -DHPUX_PCH
PLATFORM_CC_OPTS += +hdr_use $(OBJDIR)/$(PCH_FILE).pch
PCH_DEP = $(OBJDIR)/$(PCH_FILE).pch
GLOBAL_PCH_FILE=$(WORK_ROOT)/include/$(PCH_FILE).pch
endif
endif

DYNAMIC_LIB_SUFFIX=sl
LD_LIB_VAR      =SHLIB_PATH

#BUILD_ORACLE=1
#BUILD_SYBASE=1
#BUILD_INFORMIX=1
#BUILD_ODBC=1

# No support for mapfiles
USE_MAPFILE=

##############
### PURIFY ###
##############

ifdef PURIFY
# Worked on HP-UX 11.0 April 15, 1999
PURIFY_FLAGS	=-threads=yes -max_threads=100 -cache-dir=/u/swift/purifyes40cache -always-use-cache-dir
CC_PURIFY = /u/marcel/purify-4.2-hpux/purify $(PURIFY_FLAGS)
C_PURIFY  = $(CC_PURIFY)
PRELINK	 += $(CC_PURIFY)
endif #PURIFY

################
### QUANTIFY ###
################

ifdef QUANTIFY
endif # QUANTIFY
