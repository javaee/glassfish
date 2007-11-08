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

include $(BUILD_ROOT)/make/defines_UNIX.mk
LOCAL_COPY=0

# Compiler version
SUNWSPRO_VER = 6.2
SUNWSPRO_DIR_42 = /tools/ns/workshop
#SUNWSPRO_DIR_61 = /usr/suntools/internal/SUNWspro
#SUNWSPRO_DIR_62 = /usr/dist/share/forte_dev,v6.2/SUNWspro
#SUNWSPRO_DIR = $(SUNWSPRO_DIR_62)
SUNWSPRO_DIR = /usr/dist/share/sunstudio_sparc/SUNWspro
SYSTEM_LIBDIRS += $(SUNWSPRO_DIR)/lib $(SUNWSPRO_DIR)/lib/CC4

# -compat=4/-compat=5 selection
PLATFORM_CC_OPTS = -compat=$(SUNWSPRO_CC_COMPAT)
ifeq ($(SUNWSPRO_CC_COMPAT),5)
# We want the 6.1 C++ libraries, but we pull in 4.2 for any old so's
SUNWSPRO_CC_LIB = Crun Cstd
FORTE6 = forte6
else
# We want the 4.2 C++ library
SUNWSPRO_CC_LIB = C
FORTE6 =
endif

# Tool locations
C++C		=$(PRE_CC) $(SUNWSPRO_DIR)/bin/CC
CC		=$(PRE_CC) $(SUNWSPRO_DIR)/bin/CC
C               =$(PRE_C)  $(SUNWSPRO_DIR)/bin/cc
AR              =/usr/ccs/bin/ar
RANLIB          =/usr/ccs/bin/ranlib
YACC            =/usr/ccs/bin/yacc
LD		=$(PRE_LD) /usr/ccs/bin/ld
PROFILER	=$(SUNWSPRO_DIR)/bin/profile
FTP		=/usr/bin/ftp
PERL		=/usr/dist/exe/perl
PERL5		=/usr/dist/exe/perl5
LINT		=$(SUNWSPRO_DIR)/bin/lint
STRIP		=/usr/ccs/bin/strip -x
SPEC2MAP	=/usr/lib/abi/spec2map

# what is NLIST?
NLIST            = elf

# Verity defines
VERITY_ARCH	=_ssol26

BASEFLAGS = -xtarget=ultra 

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
JNI_MD_SYSNAME  = sparc

# Platform specific build options
LD_DYNAMIC	= -G
ARFLAGS		= -r
PLATFORM_DEF	= -DSVR4 -DSYSV -DSOLARIS -D_REENTRANT -DPLATFORM_SPECIFIC_STATS_ON
PLATFORM_LIB	= $(PRE_PLATFORM_LIB) pthread socket nsl dl posix4 kstat $(SUNWSPRO_CC_LIB)
PLATFORM_CC_OPTS += -mt -KPIC
PLATFORM_C_OPTS  += -mt -Kpic
PLATFORM_LD_OPTS += -mt -norunpath
RPATH_PREFIX = -R 
RPATH_ORIGIN = \$$ORIGIN

# Mapfile generation.  Set USE_MAPFILE=1 to require a mapfile for each so.
#USE_MAPFILE=1
MAPFILE_SUFFIX=mapfile
SPECFILE_SUFFIX=spec
VERSIONSFILE_SUFFIX=versions

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

#BUILD_ORACLE=1
#BUILD_SYBASE=1
#BUILD_INFORMIX=1
#BUILD_ODBC=1

################
#### XERCES ####
################
XERCES_DEFS += -features=rtti

##############
### PURIFY ###
##############

ifdef PURIFY
ifdef SUNWSPRO_CC_COMPAT4
PURIFY_VER= 4.1
else # SUNWSPRO_CC_COMPAT4
PURIFY_VER= 5.1
endif # SUNWSPRO_CC_COMPAT4
PURIFY_FLAGS	=-threads=yes -max_threads=100 -thread-stack-change=65536 
CC_PURIFY = /tools/ns/bin/purify-5.1 $(PURIFY_FLAGS)
C_PURIFY  = $(CC_PURIFY)
PRELINK	 += $(CC_PURIFY)

endif #PURIFY

ifdef PURECOV
PDIR=/h/iws-files/export/purecov/purecov-4.5.1-solaris2/
PURCOV_FLAGS	=-threads=yes -max_threads=100 -thread-stack-change=65536 -rtslave=yes -follow-child-processes=yes
CC_PURIFY = $(PDIR)purecov $(PURCOV_FLAGS)
C_PURIFY  = $(CC_PURIFY)
PRELINK	 += $(CC_PURIFY)

endif #PURECOV


################
### QUANTIFY ###
################

ifdef QUANTIFY
QUANTIFY_CACHEDIR = ./cache
QUANTIFY_OPTS   = -best-effort -collection-granularity=function \
		  -cache-dir=${QUANTIFY_CACHEDIR} \
		  -record-child-process-data=yes \
		  -max_threads=250 -record-data=yes -windows=yes \
		  -write-summary-file= -measure-timed-calls=user+system \
		  -avoid-recording-system-calls=210,87
QUANTIFY_FLAGS  = $(QUANTIFY_OPTS)
# Thanks, Ram!
QUANTIFY_PROG   = /u/rchinta/util/Quantify/quantify-3.0-solaris2/quantify
CC_QUANTIFY     = $(QUANTIFY_PROG) $(QUANTIFY_OPTS)
C_QUANTIFY      = $(QUANTIFY_PROG) $(QUANTIFY_OPTS)
PRELINK+= $(CC_QUANTIFY)

CC_FLAGS += -DQUANTIFY
C_FLAGS  += -DQUANTIFY

PRELINK+= $(CC_QUANTIFY)
endif # QUANTIFY

