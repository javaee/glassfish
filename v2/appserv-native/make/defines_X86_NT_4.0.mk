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

###############
#### TOOLS ####
###############

LOCAL_COPY=1

# Versions
# MSVC 6.0 is now the default
#MSVC_VER	=50
#MSVC_VER        =70
ifndef MSVC_VER
MSVC_VER        =60
endif

ifeq ($(MSVC_VER),70)
CRUNTIMESTREAMLIB_CHAR = p
else
CRUNTIMESTREAMLIB_CHAR = i
endif

ifndef OPTIMIZED_BUILD
LD_DYNAMIC		= -LDd
ifdef DEBUG_RUNTIME
MSCRTDEBUGBUILD = D
CRUNTIMELINK = /MDd
CRUNTIMELIB = msvcrtd
CRUNTIMESTREAMLIB = msvc${CRUNTIMESTREAMLIB_CHAR}rtd
else
CRUNTIMELINK = /MD
CRUNTIMELIB = msvcrt
CRUNTIMESTREAMLIB = msvc${CRUNTIMESTREAMLIB_CHAR}rt
endif
else
CRUNTIMELINK = /MD
LD_DYNAMIC		= -LD
CRUNTIMELIB = msvcrt
CRUNTIMESTREAMLIB = msvc${CRUNTIMESTREAMLIB_CHAR}rt
endif

# Directories:
# can be overriden in the environment:
ifndef TOOL_ROOT
TOOL_ROOT	=p:
endif

# can be overriden in the environment:
ifndef PERL_DIR
PERL_DIR	=$(TOOL_ROOT)/Perl/v5
endif

# can be overriden in the environment:
ifndef MSVC_DIR
MSVC_DIR	=$(TOOL_ROOT)/msvc$(MSVC_VER)
endif

# Binaries

DEFINES += -DUSE_TRANSMIT_FILE

# Visual C++ Binaries:
#CC is defined below
MSVC_CL		=$(MSVC_DIR)/bin/cl.exe
C++C		=$(CC)
C               =$(CC)
#AR is defined below
MSVC_LIB	=$(MSVC_DIR)/bin/lib.exe
RANLIB		=$(TOOL_ROOT)/bin/echo.exe
#LD is defined below
PROFILER	=$(MSVC_DIR)/bin/profile.exe
MSVC_LINK	=$(MSVC_DIR)/bin/link.exe
PREP		=$(MSVC_DIR)/bin/prep.exe
PLIST		=$(MSVC_DIR)/bin/plist.exe
BSCMAKE		=$(MSVC_DIR)/bin/bscmake.exe
NMAKE		=$(MSVC_DIR)/bin/nmake.exe /f
RC		=rc.exe
MC		=mc.exe

# Other Binaries
PERL5		=$(PERL_DIR)/bin/perl.exe
PERL		=$(PERL5)
RM		=$(TOOL_ROOT)/bin/rm.exe
CHMOD		=$(TOOL_ROOT)/bin/echo.exe
LS		=$(TOOL_ROOT)/bin/ls.exe
CP		=$(TOOL_ROOT)/bin/cp.exe
CP_R		=$(CP) -r
CMP		=$(TOOL_ROOT)/bin/cmp.exe
MV		=$(TOOL_ROOT)/bin/mv.exe
SED		=$(TOOL_ROOT)/bin/sed.exe
TAR		=$(TOOL_ROOT)/bin/tar.exe
ECHO		=$(TOOL_ROOT)/bin/echo.exe
DATE            =$(TOOL_ROOT)/bin/date.exe
MKDIR		=$(TOOL_ROOT)/bin/mkdir.exe
MKDIR_DASH_P    =$(MKDIR) -p
XARGS		=$(TOOL_ROOT)/bin/xargs.exe
DIRNAME		=$(TOOL_ROOT)/bin/dirname.exe
BASENAME	=$(TOOL_ROOT)/bin/basename.exe
SHELL		=$(TOOL_ROOT)/bin/sh.exe
PRINTF          =$(TOOL_ROOT)/bin/printf.exe
TR              =$(TOOL_ROOT)/bin/tr.exe
GZIP		=$(TOOL_ROOT)/bin/gzip.exe
GUNZIP		=$(GZIP) -d
ZIP		=$(TOOL_ROOT)/bin/zip.exe
UNZIP		=$(TOOL_ROOT)/bin/unzip.exe

FTP		=$(SystemRoot)/system32/ftp.exe
RCP		=$(SystemRoot)/system32/rcp.exe
RCP_CMD		=$(SystemRoot)/system32/rcp.exe -b -r $(RCPSERVER).$(RCPUSER)

SPLITSYM        =$(TOOL_ROOT)/bin/splitsym.exe

TOUCH           =$(TOOL_ROOT)/bin/touch.exe
CVS             =$(TOOL_ROOT)/bin/cvs.exe
SLEEP           =$(TOOL_ROOT)/bin/sleep.exe
WC              =$(TOOL_ROOT)/bin/wc.exe
GREP            =$(TOOL_ROOT)/bin/grep.exe
FIND            =$(TOOL_ROOT)/bin/find.exe
PWD             =$(TOOL_ROOT)/bin/pwd.exe

# UNIX to NT argument converters
# see $(PL_DIR)/readme.pl for details
# For debugging purposes you can set U2N on the make command line.
# U2N undefined - silently executes the converted command [default]
# U2N=-expand   - prints and then executes the converted command
# U2N=-noexec	- only prints the converted command
ifdef USE_PERL_PREPROCESSORS
PL_DIR		=$(BUILD_ROOT)/bin
CC		=$(PERL) $(PL_DIR)/cc2cl.pl $(MSVC_CL) $(U2N)
LD		=$(PERL) $(PL_DIR)/ld2link.pl $(MSVC_LINK) $(U2N)
AR		=$(PERL) $(PL_DIR)/ar2lib.pl $(MSVC_LIB) $(U2N)
else
CC              =$(MSVC_CL)
LD              =$(MSVC_LINK)
AR              =$(MSVC_LIB)
endif

#######################
#### GLOBAL MACROS ####
#######################

#SUFFIXES
# DLL is deprecated.  Use DYNAMIC_LIB_SUFFIX
#DLL			=dll
OBJ			=obj
SBR			=sbr
CPP			=cpp
EXE			=.exe
LIBEXT			=.lib
STATIC_LIB_SUFFIX	=lib
DYNAMIC_LIB_SUFFIX 	=dll

#PREFIXES
LIBPREFIX=

#COMMENT CHARACTER
COMMENT=^#


######################
#### XERCES Stuff ####
######################

ifdef XERCES
XERCES_DEFS += -D_CRTDBG_MAP_ALLOC -DPROJ_XMLPARSER -DPROJ_XMLUTIL -DPROJ_PARSERS -DPROJ_SAX4C -DPROJ_DOM -DPROJ_VALIDATORS -DXML_SINGLEDLL -DXML_USE_WIN32_TRANSCODER -DXML_USE_INMEMORY_MSGLOADER -DXML_USE_NETACCESSOR_WINSOCK
DEFINES += $(XERCES_DEFS)
INCLUDES+=-I$(INTERNAL_ROOT)/include/xmlparser
INCLUDES+=-I$(INTERNAL_ROOT)/include/xmlparser/dom
endif


# Libraries
SEC_LIB+= ssl cert secmod key crypto hash secutil nss dbm
NSPR_LIB	=libplc4 libplds4 libnspr4
LDAP_LIBS	=nsldap32v50 nsldappr32v50
SSLDAP_LIB	=nssldap32v50
ICU_LIBS	= icuuc icudata icuin
SETUPSDK_LIB    =nssetup32

# Verity defines
VERITY_ARCH	=_nti40
VERITY_LIB	=vdk200

#
#C++ Compilation rules
#
CREATE_MSVC_MAP_FILES	= -Fm

STD_LIB_OPTS		= $(CRUNTIMELINK)

ifndef PCH_DIR
PCH_DIR=$(OBJDIR)
endif

PRECOMPILED_HEADERS	= -YX -Fp$(PCH_DIR)/vc$(MSVC_VER).pch
C++EXCEPTION_HANDLER	= -GX
FIBER_SAFE_TLS		= -GT
PDB_LOCATION            = -Fd$(OBJDIR)/vc$(MSVC_VER).pdb

PRELIB                  = $(CRUNTIMELINK) /link /FIXED:NO $(DLL_DEF_EXPORT_FLAG) /NODEFAULTLIB

ifdef DEBUG_BUILD
# ********** DEBUG ************

# DEBUG
GENERATE_PDB_FILES=1

CC_DEBUG		= -Zi -D_DEBUG -Od -W3 $(PDB_LOCATION)
C_DEBUG                 = $(CC_DEBUG)
LD_DEBUG		= -debug -debugtype:both

# ********** DEBUG ************

else # DEBUG_BUILD

# ********** OPTIMIZED ************

# by setting DEBUGTYPE to BOTH we make sure that debug symbols get into
# the executables (old way), not only into .PDB files. Splitsym expects
# the symbols to be in the executable to be able to generate the DBG files.

ifndef OPTIMIZATION_FLAGS
OPTIMIZATION_FLAGS=/O2 /Ox
endif # OPTIMIZATION_FLAGS

CC_DEBUG                = -GF -G5 $(OPTIMIZATION_FLAGS) $(PDB_LOCATION)
C_DEBUG                 = $(CC_DEBUG)
LD_DEBUG                = 

# ********** OPTIMIZED ************

endif # DEBUG_BUILD

ifndef JAVA_VERSION
JAVA_VERSION		= 5
endif

JNI_MD_NAME	= win32
JNI_MD_SYSNAME	=
JNI_MD_LIBTYPE	=
JVM_LIBDIR	= $(EXTERNAL_JDK_DIR)/lib

ifdef PROFILE
CC_PROFILE		= -Gh 
C_PROFILE               = $(CC_PROFILE)
LD_PROFILE		= -debugtype:coff -debug:mapped,partial
endif # PROFILE

ifdef BROWSE
CC_BROWSE		= -Fr$(OBJDIR)/
C_BROWSE                = $(CC_BROWSE)
LD_BROWSE		=
endif # BROWSE

ARFLAGS			= -r
PLATFORM_INC		= -X -I$(MSVC_DIR)/include
PLATFORM_LIBDIRS	= $(MSVC_DIR)/Lib
ifeq ($(MSVC_VER),70)
PLATFORM_INC		+= -I$(MSVC_DIR)/PlatformSDK/Include 
endif
LAMEASS_XP_DEFINES      = -DXP_WIN -D_WINDOWS -DXP_PC
PLATFORM_DEF		= -D_X86_ -DXP_WIN32 -DWIN32 -D_MT -D_DLL $(LAMEASS_XP_DEFINES)
BSC_FLAGS		= -nologo 

RC_INCLUDES		= -I$(INTERNAL_ROOT)/include
RC_INCLUDES		+= -I$(MSVC_DIR)/include -I$(MSVC_DIR)/mfc/include
RC_INCL			= $(LOCAL_RC_INC) $(RC_INCLUDES)
RC_FLAGS		= $(DEFINES) $(RC_OPTS) $(RC_INCL)

MC_FLAGS		= -r $(OBJDIR) -h $(OBJDIR)

#ignore the environment variables path and include when searching for 
#include files, and add the standard search path

PLATFORM_LD_OPTS	= -machine:IX86

PLATFORM_CC_OPTS	= -nologo $(STD_LIB_OPTS) \
			  $(C++EXCEPTION_HANDLER) $(FIBER_SAFE_TLS) \
                          $(SOURCE_BROWSER_OPTIONS)
ifndef SKIP_PCH
PLATFORM_CC_OPTS+=$(PRECOMPILED_HEADERS)
endif

PLATFORM_LD_OPTS        = -nologo
PLATFORM_C_OPTS         = $(PLATFORM_CC_OPTS)
CC_DASH_O               =/Fo
LD_DASH_O               =/Fe

PLATFORM_LIB += kernel32 $(CRUNTIMELIB) $(CRUNTIMESTREAMLIB) oldnames ws2_32 mswsock advapi32

EXE_REAL_LIBS=$(addsuffix .lib, $(EXE_LIBS))
EXE1_REAL_LIBS=$(addsuffix .lib, $(EXE1_LIBS))
EXE2_REAL_LIBS=$(addsuffix .lib, $(EXE2_LIBS))
EXE3_REAL_LIBS=$(addsuffix .lib, $(EXE3_LIBS))
EXE4_REAL_LIBS=$(addsuffix .lib, $(EXE4_LIBS))
EXE5_REAL_LIBS=$(addsuffix .lib, $(EXE5_LIBS))
EXE6_REAL_LIBS=$(addsuffix .lib, $(EXE6_LIBS))
EXE7_REAL_LIBS=$(addsuffix .lib, $(EXE7_LIBS))
EXE8_REAL_LIBS=$(addsuffix .lib, $(EXE8_LIBS))
EXE9_REAL_LIBS=$(addsuffix .lib, $(EXE9_LIBS))
EXE10_REAL_LIBS=$(addsuffix .lib, $(EXE10_LIBS))
EXE11_REAL_LIBS=$(addsuffix .lib, $(EXE11_LIBS))
EXE12_REAL_LIBS=$(addsuffix .lib, $(EXE12_LIBS))
EXE13_REAL_LIBS=$(addsuffix .lib, $(EXE13_LIBS))
EXE14_REAL_LIBS=$(addsuffix .lib, $(EXE14_LIBS))
EXE15_REAL_LIBS=$(addsuffix .lib, $(EXE15_LIBS))
EXE16_REAL_LIBS=$(addsuffix .lib, $(EXE16_LIBS))
EXE17_REAL_LIBS=$(addsuffix .lib, $(EXE17_LIBS))
EXE18_REAL_LIBS=$(addsuffix .lib, $(EXE18_LIBS))
EXE19_REAL_LIBS=$(addsuffix .lib, $(EXE19_LIBS))
EXE20_REAL_LIBS=$(addsuffix .lib, $(EXE20_LIBS))

DLL_REAL_LIBS=$(addsuffix .lib, $(DLL_LIBS))
DLL1_REAL_LIBS=$(addsuffix .lib, $(DLL1_LIBS))
DLL2_REAL_LIBS=$(addsuffix .lib, $(DLL2_LIBS))
DLL3_REAL_LIBS=$(addsuffix .lib, $(DLL3_LIBS))
DLL4_REAL_LIBS=$(addsuffix .lib, $(DLL4_LIBS))
DLL5_REAL_LIBS=$(addsuffix .lib, $(DLL5_LIBS))

ifdef SKIP_AUTO_VERSION_INSERTION
SKIP_AUTO_RESOURCE=1
endif

ifndef SKIP_AUTO_RESOURCE

DLL_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(DLL_TARGET)))
DLL1_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(DLL1_TARGET)))
DLL2_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(DLL2_TARGET)))

EXE_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE_TARGET)))
EXE1_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE1_TARGET)))
EXE2_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE2_TARGET)))
EXE3_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE3_TARGET)))
EXE4_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE4_TARGET)))
EXE5_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE5_TARGET)))
EXE6_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE6_TARGET)))
EXE7_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE7_TARGET)))
EXE8_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE8_TARGET)))
EXE9_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE9_TARGET)))
EXE10_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE10_TARGET)))
EXE11_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE11_TARGET)))
EXE12_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE12_TARGET)))
EXE13_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE13_TARGET)))
EXE14_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE14_TARGET)))
EXE15_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE15_TARGET)))
EXE16_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE16_TARGET)))
EXE17_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE17_TARGET)))
EXE18_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE18_TARGET)))
EXE19_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE19_TARGET)))
EXE20_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/auto_, $(addsuffix .res, $(EXE20_TARGET)))

else # SKIP_AUTO_RESPONSE

DLL_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/, $(DLL_RES:=.res))
DLL1_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/, $(DLL1_RES:=.res))
DLL2_NONPARSED_OBJS+=$(addprefix $(OBJDIR)/, $(DLL2_RES:=.res))

endif # SKIP_AUTO_RESOURCE

EXE_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE_LIBDIRS))
EXE1_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE1_LIBDIRS))
EXE2_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE2_LIBDIRS))
EXE3_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE3_LIBDIRS))
EXE4_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE4_LIBDIRS))
EXE5_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE5_LIBDIRS))
EXE6_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE6_LIBDIRS))
EXE7_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE7_LIBDIRS))
EXE8_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE8_LIBDIRS))
EXE9_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE9_LIBDIRS))
EXE10_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE10_LIBDIRS))
EXE11_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE11_LIBDIRS))
EXE12_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE12_LIBDIRS))
EXE13_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE13_LIBDIRS))
EXE14_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE14_LIBDIRS))
EXE15_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE15_LIBDIRS))
EXE16_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE16_LIBDIRS))
EXE17_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE17_LIBDIRS))
EXE18_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE18_LIBDIRS))
EXE19_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE19_LIBDIRS))
EXE20_REAL_LIBDIRS=$(addprefix /LIBPATH:, $(EXE20_LIBDIRS))


#BUILD_ORACLE=1
#BUILD_SYBASE=1
#BUILD_INFORMIX=1
#BUILD_ODBC=1

# No support for mapfiles
USE_MAPFILE=

#so that for JS samples we can differentiate unix and NT
BATCH_FILE_SUFFIX=.bat
