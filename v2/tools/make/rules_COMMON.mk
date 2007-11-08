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

# common rules

# make sure all is first defined rule here
all::

ifndef GENERATE_PDB_FILES
SKIP_PDB=1
endif # GENERATE_PDB_FILES

ifdef SUPPRESS_PDB_FILES
SKIP_PDB=1
endif # SUPPRESS_PDB_FILES

ifndef GENERATE_MAP_FILES
SKIP_MAP=1
endif # GENERATE_MAP_FILES

ifdef SUPPRESS_MAP_FILES
SKIP_MAP=1
endif # SUPPRESS_MAP_FILES

ifndef BUILD_JAVA

#
# EXE[n]_TARGET,  EXE[n]_OBJS, [ EXE[n]_EXTRA ], [ EXE[n]_LIBS ]
#
_OBJS+=$(CSRCS:.c=.$(OBJ)) $(ASFILES:.s=$(OBJ)) $(CPPSRCS:.cpp=.$(OBJ))

ifdef _OBJS
OBJS+=$(addprefix $(OBJDIR)/,$(_OBJS))
endif #_OBJS

ifndef NO_STD_ALL_TARGET
all:: headers compile early_libraries libraries link
endif #NO_STD_ALL_TARGET

ifndef NO_STD_HEADERS_TARGET
headers::
	@$(MAKE_OBJDIR)
	$(LOOP_OVER_DIRS)
endif #NO_STD_HEADERS_TARGET

ifndef NO_STD_COMPILE_TARGET
compile:: $(GENERATED_FILES) $(OBJS)
	+$(LOOP_OVER_DIRS)
endif #NO_STD_COMPILE_TARGET

ifeq ($(OS_ARCH),WINNT)
ifdef BSC_TARGET
compile:: $(OBJDIR)/$(BSC_TARGET)
endif #BSC_TARGET
endif #OS_ARCH==WINNT

ifeq ($(OS_ARCH),SunOS)
ifdef SB_INIT
compile:: $(SB_INIT)
endif #SB_INIT
endif #OS_ARCH-SunOS

ifndef CC_DASH_O
CC_DASH_O=-o 
endif #CC_DASH_O

ifndef LD_DASH_O
LD_DASH_O=-o 
endif #LD_DASH_O

ifndef NO_STD_OBJDIR_O_RULE
$(OBJDIR)/%.$(OBJ):%.cpp $(PCH_DEP)
	@$(MAKE_OBJDIR)
	$(PRECC) $(CC) $(CC_FLAGS) $($<_CC_FLAGS) -c \
		\
		$(CC_DASH_O)$@ \
		\
		$<
endif #NO_STD_OBJDIR_O_RULE

ifndef NO_STD_OBJDIR_CPP_TO_OBJDIR_O_RULE
$(OBJDIR)/%.$(OBJ):$(OBJDIR)/%.cpp $(PCH_DEP)
	$(PRECC) $(CC) $(CC_FLAGS) $($<_CC_FLAGS) -c \
		\
		$(CC_DASH_O)$@ \
		\
		$<
endif #NO_STD_OBJDIR_CPP_TO_OBJDIR_O_RULE

ifndef NO_STD_OBJDIR_O_FROM_C_RULE
$(OBJDIR)/%.$(OBJ):%.c
	@$(MAKE_OBJDIR)
	$(PREC) $(C) $(C_FLAGS) -c \
		\
		$(CC_DASH_O)$@ \
		\
		$<
endif #NO_STD_OBJDIR_O_FROM_C_RULE
ifdef EXE_TARGET

ifndef SKIP_AUTO_VERSION_INSERTION
EXE_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE_TARGET).$(OBJ)
EXE1_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE1_TARGET).$(OBJ)
EXE2_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE2_TARGET).$(OBJ)
EXE3_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE3_TARGET).$(OBJ)
EXE4_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE4_TARGET).$(OBJ)
EXE5_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE5_TARGET).$(OBJ)
EXE6_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE6_TARGET).$(OBJ)
EXE7_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE7_TARGET).$(OBJ)
EXE8_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE8_TARGET).$(OBJ)
EXE9_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE9_TARGET).$(OBJ)
EXE10_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE10_TARGET).$(OBJ)
EXE11_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE11_TARGET).$(OBJ)
EXE12_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE12_TARGET).$(OBJ)
EXE13_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE13_TARGET).$(OBJ)
EXE14_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE14_TARGET).$(OBJ)
EXE15_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE15_TARGET).$(OBJ)
EXE16_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE16_TARGET).$(OBJ)
EXE17_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE17_TARGET).$(OBJ)
EXE18_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE18_TARGET).$(OBJ)
EXE19_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE19_TARGET).$(OBJ)
EXE20_NONPARSED_OBJS+=$(OBJDIR)/auto_$(EXE20_TARGET).$(OBJ)
endif #SKIP_AUTO_VERSION_INSERTION

ifdef EXE_RES
_EXE_RES=$(OBJDIR)/$(EXE_RES).res
endif
_EXE_OBJS:=$(addprefix $(OBJDIR)/,$(EXE_OBJS:=.$(OBJ))) $(EXE_NONPARSED_OBJS) $(_EXE_RES)
_EXE_OUTPUT_FILE:=$(OBJDIR)/$(EXE_TARGET)$(EXE)
$(_EXE_OUTPUT_FILE) : $(_EXE_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE_OUTPUT_FILE) \
		\
		$(_EXE_OBJS) $(EXE_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE_REAL_LIBDIRS) $(EXE_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS) $(PLATFORM_LD_OPTS_EXE)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE_OUTPUT_FILE)
endif
endif
endif #EXE_TARGET

ifdef EXE1_TARGET
_EXE1_OBJS:=$(addprefix $(OBJDIR)/,$(EXE1_OBJS:=.$(OBJ))) $(EXE1_NONPARSED_OBJS)
_EXE1_OUTPUT_FILE:=$(OBJDIR)/$(EXE1_TARGET)$(EXE)

$(_EXE1_OUTPUT_FILE): $(_EXE1_OBJS) ; \
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE1_OUTPUT_FILE) \
		\
		$(_EXE1_OBJS) $(EXE1_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE_REAL_LIBDIRS) $(EXE1_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE1_OUTPUT_FILE)
endif
endif
endif #EXE1_TARGET

ifdef EXE2_TARGET
_EXE2_OBJS:=$(addprefix $(OBJDIR)/,$(EXE2_OBJS:=.$(OBJ))) $(EXE2_NONPARSED_OBJS)
_EXE2_OUTPUT_FILE:=$(OBJDIR)/$(EXE2_TARGET)$(EXE)
$(_EXE2_OUTPUT_FILE): $(_EXE2_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE2_OUTPUT_FILE) \
		\
		$(_EXE2_OBJS) $(EXE2_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE2_REAL_LIBDIRS) $(EXE2_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS)  $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE2_OUTPUT_FILE)
endif
endif
endif #EXE2_TARGET

ifdef EXE3_TARGET
_EXE3_OBJS:=$(addprefix $(OBJDIR)/,$(EXE3_OBJS:=.$(OBJ))) $(EXE3_NONPARSED_OBJS)
_EXE3_OUTPUT_FILE:=$(OBJDIR)/$(EXE3_TARGET)$(EXE)
$(_EXE3_OUTPUT_FILE): $(_EXE3_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE3_OUTPUT_FILE) \
		\
		$(_EXE3_OBJS) $(EXE3_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE3_REAL_LIBDIRS) $(EXE3_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE3_OUTPUT_FILE)
endif
endif
endif #EXE3_TARGET

ifdef EXE4_TARGET
_EXE4_OBJS:=$(addprefix $(OBJDIR)/,$(EXE4_OBJS:=.$(OBJ))) $(EXE4_NONPARSED_OBJS)
_EXE4_OUTPUT_FILE:=$(OBJDIR)/$(EXE4_TARGET)$(EXE)
$(_EXE4_OUTPUT_FILE): $(_EXE4_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE4_OUTPUT_FILE) \
		\
		$(_EXE4_OBJS) $(EXE4_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE4_REAL_LIBDIRS) $(EXE4_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE4_OUTPUT_FILE)
endif
endif
endif #EXE4_TARGET

ifdef EXE5_TARGET
_EXE5_OBJS:=$(addprefix $(OBJDIR)/,$(EXE5_OBJS:=.$(OBJ))) $(EXE5_NONPARSED_OBJS)
_EXE5_OUTPUT_FILE:=$(OBJDIR)/$(EXE5_TARGET)$(EXE)
$(_EXE5_OUTPUT_FILE): $(_EXE5_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE5_OUTPUT_FILE) \
		\
		$(_EXE5_OBJS) $(EXE5_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE5_REAL_LIBDIRS) $(EXE5_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE5_OUTPUT_FILE)
endif
endif
endif #EXE5_TARGET

ifdef EXE6_TARGET
_EXE6_OBJS:=$(addprefix $(OBJDIR)/,$(EXE6_OBJS:=.$(OBJ))) $(EXE6_NONPARSED_OBJS)
_EXE6_OUTPUT_FILE:=$(OBJDIR)/$(EXE6_TARGET)$(EXE)
$(_EXE6_OUTPUT_FILE): $(_EXE6_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE6_OUTPUT_FILE) \
		\
		$(_EXE6_OBJS) $(EXE6_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE6_REAL_LIBDIRS) $(EXE6_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE6_OUTPUT_FILE)
endif
endif
endif #EXE6_TARGET

ifdef EXE7_TARGET
_EXE7_OBJS:=$(addprefix $(OBJDIR)/,$(EXE7_OBJS:=.$(OBJ))) $(EXE7_NONPARSED_OBJS)
_EXE7_OUTPUT_FILE:=$(OBJDIR)/$(EXE7_TARGET)$(EXE)
$(_EXE7_OUTPUT_FILE): $(_EXE7_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE7_OUTPUT_FILE) \
		\
		$(_EXE7_OBJS) $(EXE7_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE7_REAL_LIBDIRS) $(EXE7_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE7_OUTPUT_FILE)
endif
endif
endif #EXE7_TARGET

ifdef EXE8_TARGET
_EXE8_OBJS:=$(addprefix $(OBJDIR)/,$(EXE8_OBJS:=.$(OBJ))) $(EXE8_NONPARSED_OBJS)
_EXE8_OUTPUT_FILE:=$(OBJDIR)/$(EXE8_TARGET)$(EXE)
$(_EXE8_OUTPUT_FILE): $(_EXE8_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE8_OUTPUT_FILE) \
		\
		$(_EXE8_OBJS) $(EXE8_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE8_REAL_LIBDIRS) $(EXE8_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE8_OUTPUT_FILE)
endif
endif
endif #EXE8_TARGET

ifdef EXE9_TARGET
_EXE9_OBJS:=$(addprefix $(OBJDIR)/,$(EXE9_OBJS:=.$(OBJ))) $(EXE9_NONPARSED_OBJS)
_EXE9_OUTPUT_FILE:=$(OBJDIR)/$(EXE9_TARGET)$(EXE)
$(_EXE9_OUTPUT_FILE): $(_EXE9_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE9_OUTPUT_FILE) \
		\
		$(_EXE9_OBJS) $(EXE9_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE9_REAL_LIBDIRS) $(EXE9_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE9_OUTPUT_FILE)
endif
endif
endif #EXE9_TARGET

ifdef EXE10_TARGET
_EXE10_OBJS:=$(addprefix $(OBJDIR)/,$(EXE10_OBJS:=.$(OBJ))) $(EXE10_NONPARSED_OBJS)
_EXE10_OUTPUT_FILE:=$(OBJDIR)/$(EXE10_TARGET)$(EXE)
$(_EXE10_OUTPUT_FILE): $(_EXE10_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE10_OUTPUT_FILE) \
		\
		$(_EXE10_OBJS) $(EXE10_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE10_REAL_LIBDIRS) $(EXE10_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE10_OUTPUT_FILE)
endif
endif
endif #EXE10_TARGET

ifdef EXE11_TARGET
_EXE11_OBJS:=$(addprefix $(OBJDIR)/,$(EXE11_OBJS:=.$(OBJ))) $(EXE11_NONPARSED_OBJS)
_EXE11_OUTPUT_FILE:=$(OBJDIR)/$(EXE11_TARGET)$(EXE)
$(_EXE11_OUTPUT_FILE): $(_EXE11_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE11_OUTPUT_FILE) \
		\
		$(_EXE11_OBJS) $(EXE11_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE11_REAL_LIBDIRS) $(EXE11_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE11_OUTPUT_FILE)
endif
endif
endif #EXE11_TARGET

ifdef EXE12_TARGET
_EXE12_OBJS:=$(addprefix $(OBJDIR)/,$(EXE12_OBJS:=.$(OBJ))) $(EXE12_NONPARSED_OBJS)
_EXE12_OUTPUT_FILE:=$(OBJDIR)/$(EXE12_TARGET)$(EXE)
$(_EXE12_OUTPUT_FILE): $(_EXE12_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE12_OUTPUT_FILE) \
		\
		$(_EXE12_OBJS) $(EXE12_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE12_REAL_LIBDIRS) $(EXE12_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE12_OUTPUT_FILE)
endif
endif
endif #EXE12_TARGET

ifdef EXE13_TARGET
_EXE13_OBJS:=$(addprefix $(OBJDIR)/,$(EXE13_OBJS:=.$(OBJ))) $(EXE13_NONPARSED_OBJS)
_EXE13_OUTPUT_FILE:=$(OBJDIR)/$(EXE13_TARGET)$(EXE)
$(_EXE13_OUTPUT_FILE): $(_EXE13_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE13_OUTPUT_FILE) \
		\
		$(_EXE13_OBJS) $(EXE13_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE13_REAL_LIBDIRS) $(EXE13_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE13_OUTPUT_FILE)
endif
endif
endif #EXE13_TARGET

ifdef EXE14_TARGET
_EXE14_OBJS:=$(addprefix $(OBJDIR)/,$(EXE14_OBJS:=.$(OBJ))) $(EXE14_NONPARSED_OBJS)
_EXE14_OUTPUT_FILE:=$(OBJDIR)/$(EXE14_TARGET)$(EXE)
$(_EXE14_OUTPUT_FILE): $(_EXE14_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE14_OUTPUT_FILE) \
		\
		$(_EXE14_OBJS) $(EXE14_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE14_REAL_LIBDIRS) $(EXE14_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE14_OUTPUT_FILE)
endif
endif
endif #EXE14_TARGET

ifdef EXE15_TARGET
_EXE15_OBJS:=$(addprefix $(OBJDIR)/,$(EXE15_OBJS:=.$(OBJ))) $(EXE15_NONPARSED_OBJS)
_EXE15_OUTPUT_FILE:=$(OBJDIR)/$(EXE15_TARGET)$(EXE)
$(_EXE15_OUTPUT_FILE): $(_EXE15_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE15_OUTPUT_FILE) \
		\
		$(_EXE15_OBJS) $(EXE15_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE15_REAL_LIBDIRS) $(EXE15_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE15_OUTPUT_FILE)
endif
endif
endif #EXE15_TARGET

ifdef EXE16_TARGET
_EXE16_OBJS:=$(addprefix $(OBJDIR)/,$(EXE16_OBJS:=.$(OBJ))) $(EXE16_NONPARSED_OBJS)
_EXE16_OUTPUT_FILE:=$(OBJDIR)/$(EXE16_TARGET)$(EXE)
$(_EXE16_OUTPUT_FILE): $(_EXE16_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE16_OUTPUT_FILE) \
		\
		$(_EXE16_OBJS) $(EXE16_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE16_REAL_LIBDIRS) $(EXE16_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE16_OUTPUT_FILE)
endif
endif
endif #EXE16_TARGET

ifdef EXE17_TARGET
_EXE17_OBJS:=$(addprefix $(OBJDIR)/,$(EXE17_OBJS:=.$(OBJ))) $(EXE17_NONPARSED_OBJS)
_EXE17_OUTPUT_FILE:=$(OBJDIR)/$(EXE17_TARGET)$(EXE)
$(_EXE17_OUTPUT_FILE): $(_EXE17_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE17_OUTPUT_FILE) \
		\
		$(_EXE17_OBJS) $(EXE17_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE17_REAL_LIBDIRS) $(EXE17_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE17_OUTPUT_FILE)
endif
endif
endif #EXE17_TARGET

ifdef EXE18_TARGET
_EXE18_OBJS:=$(addprefix $(OBJDIR)/,$(EXE18_OBJS:=.$(OBJ))) $(EXE18_NONPARSED_OBJS)
_EXE18_OUTPUT_FILE:=$(OBJDIR)/$(EXE18_TARGET)$(EXE)
$(_EXE18_OUTPUT_FILE): $(_EXE18_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE18_OUTPUT_FILE) \
		\
		$(_EXE18_OBJS) $(EXE18_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE18_REAL_LIBDIRS) $(EXE18_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE18_OUTPUT_FILE)
endif
endif
endif #EXE18_TARGET

ifdef EXE19_TARGET
_EXE19_OBJS:=$(addprefix $(OBJDIR)/,$(EXE19_OBJS:=.$(OBJ))) $(EXE19_NONPARSED_OBJS)
_EXE19_OUTPUT_FILE:=$(OBJDIR)/$(EXE19_TARGET)$(EXE)
$(_EXE19_OUTPUT_FILE): $(_EXE19_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE19_OUTPUT_FILE) \
		\
		$(_EXE19_OBJS) $(EXE19_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE19_REAL_LIBDIRS) $(EXE19_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE19_OUTPUT_FILE)
endif
endif
endif #EXE19_TARGET

ifdef EXE20_TARGET
_EXE20_OBJS:=$(addprefix $(OBJDIR)/,$(EXE20_OBJS:=.$(OBJ))) $(EXE20_NONPARSED_OBJS)
_EXE20_OUTPUT_FILE:=$(OBJDIR)/$(EXE20_TARGET)$(EXE)
$(_EXE20_OUTPUT_FILE): $(_EXE20_OBJS)
	$(PRELINK) $(CC) \
		\
		$(LD_DASH_O)$(_EXE20_OUTPUT_FILE) \
		\
		$(_EXE20_OBJS) $(EXE20_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(EXE20_REAL_LIBDIRS) $(EXE20_REAL_LIBS) $(LD_LIBS) $(LD_RPATHS) $(SYSTEM_LINK_LIBS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(_EXE20_OUTPUT_FILE)
endif
endif
endif #EXE20_TARGET


#
# DLL[n]_TARGET, DLL[n]_OBJS, [ DLL[n]_EXTRA ], [ DLL[n]_LIBS ]
#

ifdef DLL_DEF_EXPORT
DLL_DEF_EXPORT_FLAG=/DEF:$(DLL_DEF_EXPORT)
endif

ifdef DLL_TARGET
DLL_REAL_OBJS:=$(addprefix $(OBJDIR)/, $(DLL_OBJS:=.$(OBJ)))
DLL_OUTPUT_FILE:=$(OBJDIR)/$(LIBPREFIX)$(DLL_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef USE_MAPFILE
DLL_MAPFILE=$(OBJDIR)/$(LIBPREFIX)$(DLL_TARGET).$(MAPFILE_SUFFIX)
DLL_ADDITIONAL_DEPENDENCIES+=$(DLL_MAPFILE)
endif
$(DLL_OUTPUT_FILE): $(DLL_REAL_OBJS) $(DLL_NONPARSED_OBJS) $(DLL_ADDITIONAL_DEPENDENCIES)
	$(CC) $(CC_SHARED_LIB_FLAGS) $(LD_DYNAMIC) \
		\
		$(LD_DASH_O)$(DLL_OUTPUT_FILE) \
		\
		$(DLL_REAL_OBJS) $(DLL_NONPARSED_OBJS) \
                $(DLL_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(DLL_REAL_LIBS) $(DLL_NONPARSED_LIBS) $(LD_LIBS) $(LD_RPATHS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(DLL_OUTPUT_FILE)
endif
endif
endif # DLL_TARGET

ifdef DLL1_TARGET
DLL1_REAL_OBJS = $(addprefix $(OBJDIR)/,$(DLL1_OBJS:=.$(OBJ)))
DLL1_OUTPUT_FILE= $(OBJDIR)/$(LIBPREFIX)$(DLL1_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef USE_MAPFILE
DLL1_MAPFILE=$(OBJDIR)/$(LIBPREFIX)$(DLL1_TARGET).$(MAPFILE_SUFFIX)
DLL1_ADDITIONAL_DEPENDENCIES+=$(DLL1_MAPFILE)
endif
$(DLL1_OUTPUT_FILE): $(DLL1_REAL_OBJS)  $(DLL1_NONPARSED_OBJS) $(DLL1_ADDITIONAL_DEPENDENCIES)
	$(CC) $(CC_SHARED_LIB_FLAGS) $(LD_DYNAMIC) \
		\
		$(LD_DASH_O)$(DLL1_OUTPUT_FILE) \
		\
		$(DLL1_REAL_OBJS) $(DLL1_NONPARSED_OBJS) \
                $(DLL1_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(DLL1_REAL_LIBS) $(DLL1_NONPARSED_LIBS) $(LD_LIBS) $(LD_RPATHS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(DLL1_OUTPUT_FILE)
endif
endif
endif #DLL1_TARGET

ifdef DLL2_TARGET
DLL2_REAL_OBJS = $(addprefix $(OBJDIR)/,$(DLL2_OBJS:=.$(OBJ)))
DLL2_OUTPUT_FILE= $(OBJDIR)/$(LIBPREFIX)$(DLL2_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef USE_MAPFILE
DLL2_MAPFILE=$(OBJDIR)/$(LIBPREFIX)$(DLL2_TARGET).$(MAPFILE_SUFFIX)
DLL2_ADDITIONAL_DEPENDENCIES+=$(DLL2_MAPFILE)
endif
$(DLL2_OUTPUT_FILE): $(DLL2_REAL_OBJS)  $(DLL2_NONPARSED_OBJS) $(DLL2_ADDITIONAL_DEPENDENCIES)
	$(CC) $(CC_SHARED_LIB_FLAGS) $(LD_DYNAMIC) \
		\
		$(LD_DASH_O)$(DLL2_OUTPUT_FILE) \
		\
		$(DLL2_REAL_OBJS) $(DLL2_NONPARSED_OBJS) \
                $(DLL2_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(DLL2_REAL_LIBS) $(DLL2_NONPARSED_LIBS) $(LD_LIBS) $(LD_RPATHS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(DLL2_OUTPUT_FILE)
endif
endif
endif #DLL2_TARGET

ifdef DLL3_TARGET
DLL3_REAL_OBJS = $(addprefix $(OBJDIR)/,$(DLL3_OBJS:=.$(OBJ)))
DLL3_OUTPUT_FILE= $(OBJDIR)/$(LIBPREFIX)$(DLL3_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef USE_MAPFILE
DLL3_MAPFILE=$(OBJDIR)/$(LIBPREFIX)$(DLL3_TARGET).$(MAPFILE_SUFFIX)
DLL3_ADDITIONAL_DEPENDENCIES+=$(DLL3_MAPFILE)
endif
$(DLL3_OUTPUT_FILE): $(DLL3_REAL_OBJS)  $(DLL3_NONPARSED_OBJS) $(DLL3_ADDITIONAL_DEPENDENCIES)
	$(CC) $(CC_SHARED_LIB_FLAGS) $(LD_DYNAMIC) \
		\
		$(LD_DASH_O)$(DLL3_OUTPUT_FILE) \
		\
		$(DLL3_REAL_OBJS) $(DLL3_NONPARSED_OBJS) \
                $(DLL3_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(DLL3_REAL_LIBS) $(DLL3_NONPARSED_LIBS) $(LD_LIBS) $(LD_RPATHS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(DLL3_OUTPUT_FILE)
endif
endif
endif #DLL3_TARGET

ifdef DLL4_TARGET
DLL4_REAL_OBJS = $(addprefix $(OBJDIR)/,$(DLL4_OBJS:=.$(OBJ)))
DLL4_OUTPUT_FILE= $(OBJDIR)/$(LIBPREFIX)$(DLL4_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef USE_MAPFILE
DLL4_MAPFILE=$(OBJDIR)/$(LIBPREFIX)$(DLL4_TARGET).$(MAPFILE_SUFFIX)
DLL4_ADDITIONAL_DEPENDENCIES+=$(DLL4_MAPFILE)
endif
$(DLL4_OUTPUT_FILE): $(DLL4_REAL_OBJS)  $(DLL4_NONPARSED_OBJS) $(DLL4_ADDITIONAL_DEPENDENCIES)
	$(CC) $(CC_SHARED_LIB_FLAGS) $(LD_DYNAMIC) \
		\
		$(LD_DASH_O)$(DLL4_OUTPUT_FILE) \
		\
		$(DLL4_REAL_OBJS) $(DLL4_NONPARSED_OBJS) \
                $(DLL4_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(DLL4_REAL_LIBS) $(DLL4_NONPARSED_LIBS) $(LD_LIBS) $(LD_RPATHS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(DLL4_OUTPUT_FILE)
endif
endif
endif #DLL4_TARGET


ifdef DLL5_TARGET
DLL5_REAL_OBJS = $(addprefix $(OBJDIR)/,$(DLL5_OBJS:=.$(OBJ)))
DLL5_OUTPUT_FILE= $(OBJDIR)/$(LIBPREFIX)$(DLL5_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef USE_MAPFILE
DLL5_MAPFILE=$(OBJDIR)/$(LIBPREFIX)$(DLL5_TARGET).$(MAPFILE_SUFFIX)
DLL5_ADDITIONAL_DEPENDENCIES+=$(DLL5_MAPFILE)
endif
$(DLL5_OUTPUT_FILE): $(DLL5_REAL_OBJS)  $(DLL5_NONPARSED_OBJS) $(DLL5_ADDITIONAL_DEPENDENCIES)
	$(CC) $(CC_SHARED_LIB_FLAGS) $(LD_DYNAMIC) \
		\
		$(LD_DASH_O)$(DLL5_OUTPUT_FILE) \
		\
		$(DLL5_REAL_OBJS) $(DLL5_NONPARSED_OBJS) \
                $(DLL5_EXTRA) $(PRELIB) $(LD_FLAGS) \
		$(DLL5_REAL_LIBS) $(DLL5_NONPARSED_LIBS) $(LD_LIBS) $(LD_RPATHS)
ifeq ($(BUILD_VARIANT), OPTIMIZED)
ifdef STRIP
	$(STRIP) $(DLL5_OUTPUT_FILE)
endif
endif
endif #DLL5_TARGET


ifndef OBJDIR_BIN
OBJDIR_BIN=$(OBJDIR)
endif

ifndef OBJDIR_LIB
OBJDIR_LIB=$(OBJDIR)
endif

ifndef OBJDIR_DYNLIB
OBJDIR_DYNLIB=$(OBJDIR)
endif

ifndef OBJDIR_HDR
OBJDIR_HDR=.
endif

ifdef PUBLIC_RESOURCEFILES
SOMETHING_EXPORTED=1
endif

ifdef PUBLIC_MIBFILES
SOMETHING_EXPORTED=1
endif

ifdef PUBLIC_HEADERS
#EXPORT_HEADERS+=$(PUBLIC_HEADERS)
SOMETHING_EXPORTED=1
_PUBLIC_HEADERS+=$(addprefix $(OBJDIR_HDR)/, $(PUBLIC_HEADERS))
endif

ifdef EXPORT_HEADERS
SOMETHING_EXPORTED=1
_EXPORT_HEADERS+=$(addprefix $(OBJDIR_HDR)/, $(EXPORT_HEADERS))
endif

ifdef PUBLIC_INSTALLER_FILES
SOMETHING_EXPORTED=1
ADMIN_CPP_RULES=1
ifndef PUBLIC_INSTALLER_FILES_OBJDIR
PUBLIC_INSTALLER_FILES_OBJDIR=.
endif
_PUBLIC_INSTALLER_FILES+=$(addprefix $(PUBLIC_INSTALLER_FILES_OBJDIR)/, $(PUBLIC_INSTALLER_FILES))
endif

ifdef PUBLIC_ADMIN_FILES
SOMETHING_EXPORTED=1
ADMIN_CPP_RULES=1
ifndef PUBLIC_ADMIN_FILES_OBJDIR
PUBLIC_ADMIN_FILES_OBJDIR=.
endif
_PUBLIC_ADMIN_FILES+=$(addprefix $(PUBLIC_ADMIN_FILES_OBJDIR)/, $(PUBLIC_ADMIN_FILES))
endif #PUBLIC_ADMIN_FILES

ifdef PUBLIC_HTTPADMIN_FILES
SOMETHING_EXPORTED=1
ADMIN_CPP_RULES=1
ifndef PUBLIC_HTTPADMIN_FILES_OBJDIR
PUBLIC_HTTPADMIN_FILES_OBJDIR=.
endif
_PUBLIC_HTTPADMIN_FILES+=$(addprefix $(PUBLIC_HTTPADMIN_FILES_OBJDIR)/, $(PUBLIC_HTTPADMIN_FILES))
endif #PUBLIC_HTTPADMIN_FILES

ifdef ADMIN_CPP_RULES

ifeq ($(OS_ARCH),WINNT)
$(OBJDIR)/%.html: %.h
	$(CC) /nologo /P /EP -I. -I$(INTERNAL_ROOT)/include $(HTMLDEFS) -Fo$@  $*.h
	cp $*.i $@
	rm $*.i
else
$(OBJDIR)/%.html: %.h
	@echo "$< -> $(OBJDIR)/$*.html..."
	@sed -e s/\'/::NETSCAPE_QUOTE_CHAR::/g \
            -e s/\"/::NETSCAPE_DBL_QUOTE::/g \
            -e 's^/\*^::NETSCAPE_SLASHSTAR::^g' \
            -e 's^\*/^::NETSCAPE_STARSLASH::^g' \
            -e 's^//^::NETSCAPE_DBLSLASH::^g' \
            -e 's^\.^::NETSCAPE_PERIOD::^g' \
            -e 's/^# /::NETSCAPE_HASH_BEGIN::/g' \
            -e 's/^\(#include.*\)::NETSCAPE_PERIOD::\(.*\)$$/\1.\2/g' \
	    $< > $(OBJDIR)/$*.$(OBJDIR_NAME).c

# For #DEFINE X "N", the Solaris preprocessor substitutes " N " for X, so
# we need to get rid of the spaces before we resubstitute. This only breaks
# ciphers.h #DEFINE DOMESTIC_FLAG "1" as far as I know, so we only do this
# for DBL_QUOTE  --mlarson 09/08/99

	@$(CPPCMD) -I. -I$(INTERNAL_ROOT)/include $(HTMLDEFS) $(OBJDIR)/$*.$(OBJDIR_NAME).c | \
            sed -e s/::NETSCAPE_QUOTE_CHAR::/\'/g \
                -e 's/: : NETSCAPE_DBL_QUOTE : : /::NETSCAPE_DBL_QUOTE::/g' \
                -e s/::NETSCAPE_DBL_QUOTE::/\"/g \
                -e 's^::NETSCAPE_SLASHSTAR::^/\*^g' \
                -e 's^::NETSCAPE_DBLSLASH::^//^g' \
                -e 's^::NETSCAPE_PERIOD::^.^g' \
                -e 's^::NETSCAPE_STARSLASH::^\*/^g' | \
            egrep -v '^# .*' | grep -v '#ident ' | \
	    egrep -v '^# .*' | grep -v '#line ' | \
            sed -e 's/::NETSCAPE_HASH_BEGIN::/# /g' > $(OBJDIR)/$*.html
	@rm $(OBJDIR)/$*.$(OBJDIR_NAME).c
endif
ifeq ($(OS_ARCH),WINNT)
$(OBJDIR)/%.lst: %.lst
	$(CC) /nologo /P /EP -I. -I$(INTERNAL_ROOT)/include $(HTMLDEFS) -Fo$@  $*.lst
	cp $*.i $@
	rm $*.i
else
$(OBJDIR)/%.lst: %.lst
	@echo "$< -> $(OBJDIR)/$*.lst..."
	@sed -e s/\'/::NETSCAPE_QUOTE_CHAR::/g \
            -e s/\"/::NETSCAPE_DBL_QUOTE::/g \
            -e 's^/\*^::NETSCAPE_SLASHSTAR::^g' \
            -e 's^\*/^::NETSCAPE_STARSLASH::^g' \
            -e 's^//^::NETSCAPE_DBLSLASH::^g' \
            -e 's^\.^::NETSCAPE_PERIOD::^g' \
            -e 's/^# /::NETSCAPE_HASH_BEGIN::/g' \
            -e 's/^\(#include.*\)::NETSCAPE_PERIOD::\(.*\)$$/\1.\2/g' \
	    $< > $(OBJDIR)/$*.$(OBJDIR_NAME).c
	@$(CPPCMD) -I. -I$(INTERNAL_ROOT)/include $(HTMLDEFS) $(OBJDIR)/$*.$(OBJDIR_NAME).c | \
            sed -e s/::NETSCAPE_QUOTE_CHAR::/\'/g \
                -e s/::NETSCAPE_DBL_QUOTE::/\"/g \
                -e 's^::NETSCAPE_SLASHSTAR::^/\*^g' \
                -e 's^::NETSCAPE_DBLSLASH::^//^g' \
                -e 's^::NETSCAPE_PERIOD::^.^g' \
                -e 's^::NETSCAPE_STARSLASH::^\*/^g' | \
            egrep -v '^# .*' | grep -v '#ident ' | \
            sed -e 's/::NETSCAPE_HASH_BEGIN::/# /g' > $(OBJDIR)/$*.lst
	@rm $(OBJDIR)/$*.$(OBJDIR_NAME).c
endif
ifeq ($(OS_ARCH),WINNT)
$(OBJDIR)/%.apm: %.apm
	$(CC) /nologo /P /EP -I. -I$(INTERNAL_ROOT)/include $(HTMLDEFS) -Fo$@  $*.apm
	cp $*.i $@
	rm $*.i
else
$(OBJDIR)/%.apm: %.apm
	@echo "$< -> $(OBJDIR)/$*.apm..."
	@sed -e s/\'/::NETSCAPE_QUOTE_CHAR::/g \
            -e s/\"/::NETSCAPE_DBL_QUOTE::/g \
            -e 's^/\*^::NETSCAPE_SLASHSTAR::^g' \
            -e 's^\*/^::NETSCAPE_STARSLASH::^g' \
            -e 's^//^::NETSCAPE_DBLSLASH::^g' \
            -e 's^\.^::NETSCAPE_PERIOD::^g' \
            -e 's/^# /::NETSCAPE_HASH_BEGIN::/g' \
            -e 's/^\(#include.*\)::NETSCAPE_PERIOD::\(.*\)$$/\1.\2/g' \
	    $< > $(OBJDIR)/$*.$(OBJDIR_NAME).c
	@$(CPPCMD) -I. -I$(INTERNAL_ROOT)/include $(HTMLDEFS) $(OBJDIR)/$*.$(OBJDIR_NAME).c | \
            sed -e s/::NETSCAPE_QUOTE_CHAR::/\'/g \
                -e s/::NETSCAPE_DBL_QUOTE::/\"/g \
                -e 's^::NETSCAPE_SLASHSTAR::^/\*^g' \
                -e 's^::NETSCAPE_DBLSLASH::^//^g' \
                -e 's^::NETSCAPE_PERIOD::^.^g' \
                -e 's^::NETSCAPE_STARSLASH::^\*/^g' | \
            egrep -v '^# .*' | grep -v '#ident ' | \
            sed -e 's/::NETSCAPE_HASH_BEGIN::/# /g' > $(OBJDIR)/$*.apm
	@rm $(OBJDIR)/$*.$(OBJDIR_NAME).c
endif
ifeq ($(OS_ARCH),WINNT)
$(OBJDIR)/%.spm: %.spm
	$(CC) /nologo /P /EP -I. -I$(INTERNAL_ROOT)/include $(HTMLDEFS) -Fo$@  $*.spm
	cp $*.i $@
	rm $*.i
else
$(OBJDIR)/%.spm: %.spm
	@echo "$< -> $(OBJDIR)/$*.spm..."
	@sed -e s/\'/::NETSCAPE_QUOTE_CHAR::/g \
            -e s/\"/::NETSCAPE_DBL_QUOTE::/g \
            -e 's^/\*^::NETSCAPE_SLASHSTAR::^g' \
            -e 's^\*/^::NETSCAPE_STARSLASH::^g' \
            -e 's^//^::NETSCAPE_DBLSLASH::^g' \
            -e 's^\.^::NETSCAPE_PERIOD::^g' \
            -e 's/^# /::NETSCAPE_HASH_BEGIN::/g' \
            -e 's/^\(#include.*\)::NETSCAPE_PERIOD::\(.*\)$$/\1.\2/g' \
	    $< > $(OBJDIR)/$*.$(OBJDIR_NAME).c
	@$(CPPCMD) -I. -I$(INTERNAL_ROOT)/include $(HTMLDEFS) $(OBJDIR)/$*.$(OBJDIR_NAME).c | \
            sed -e s/::NETSCAPE_QUOTE_CHAR::/\'/g \
                -e s/::NETSCAPE_DBL_QUOTE::/\"/g \
                -e 's^::NETSCAPE_SLASHSTAR::^/\*^g' \
                -e 's^::NETSCAPE_DBLSLASH::^//^g' \
                -e 's^::NETSCAPE_PERIOD::^.^g' \
                -e 's^::NETSCAPE_STARSLASH::^\*/^g' | \
            egrep -v '^# .*' | grep -v '#ident ' | \
            sed -e 's/::NETSCAPE_HASH_BEGIN::/# /g' > $(OBJDIR)/$*.spm
	@rm $(OBJDIR)/$*.$(OBJDIR_NAME).c
endif

endif #ADMIN_CPP_RULES

ifdef PUBLIC_SDK_FILES
SOMETHING_EXPORTED=1
endif

ifdef PUBLIC_DOC_FILES
SOMETHING_EXPORTED=1
endif

ifdef PRIVATE_BINARIES
SOMETHING_EXPORTED=1
ifdef EXE
ifdef NO_EXE_SUFFIX
_PRIVATE_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(PRIVATE_BINARIES))
else
_PRIVATE_BINARIES+=$(addprefix $(OBJDIR_BIN)/, \
                   $(addsuffix $(EXE), $(PRIVATE_BINARIES)))
ifeq ($(OS_ARCH),WINNT) 
ifndef SKIP_PDB
_PRIVATE_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(PRIVATE_BINARIES)))
endif # SKIP_PDB
ifndef SKIP_MAP
_PRIVATE_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(PRIVATE_BINARIES)))
endif # SKIP_MAP

endif # OS==WINNT
endif # EXE && !NO_EXE_SUFFIX
else
_PRIVATE_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(PRIVATE_BINARIES))
endif #ifndef EXE

ifeq ($(OS_ARCH),WINNT) 
ifndef SKIP_PDB
_PRIVATE_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(PRIVATE_BINARIES)))
endif # SKIP_PDB
ifndef SKIP_MAP
_PRIVATE_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(PRIVATE_BINARIES)))
endif # SKIP_MAP
endif # OS==WINNT

endif #PRIVATE_BINARIES

ifdef PUBLIC_BINARIES
SOMETHING_EXPORTED=1
ifdef EXE
ifdef NO_EXE_SUFFIX
_PUBLIC_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(PUBLIC_BINARIES))
else
_PUBLIC_BINARIES+=$(addprefix $(OBJDIR_BIN)/, \
                   $(addsuffix $(EXE), $(PUBLIC_BINARIES)))
ifeq ($(OS_ARCH),WINNT) 
ifndef SKIP_PDB
_PUBLIC_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(PUBLIC_BINARIES)))
endif # SKIP_PDB
ifndef SKIP_MAP
_PUBLIC_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(PUBLIC_BINARIES)))
endif # SKIP_MAP

endif # OS==WINNT
endif # EXE && !NO_EXE_SUFFIX
else
_PUBLIC_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(PUBLIC_BINARIES))
endif #ifndef EXE

ifeq ($(OS_ARCH),WINNT) 
ifndef SKIP_PDB
_PUBLIC_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(PUBLIC_BINARIES)))
endif # SKIP_PDB
ifndef SKIP_MAP
_PUBLIC_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(PUBLIC_BINARIES)))
endif # SKIP_MAP
endif # OS==WINNT

endif #PUBLIC_BINARIES

ifdef PUBLIC_ADMIN_BINARIES
SOMETHING_EXPORTED=1
ifdef EXE
ifdef NO_EXE_SUFFIX
_PUBLIC_ADMIN_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(PUBLIC_ADMIN_BINARIES))
else
_PUBLIC_ADMIN_BINARIES+=$(addprefix $(OBJDIR_BIN)/, \
                   $(addsuffix $(EXE), $(PUBLIC_ADMIN_BINARIES)))
ifeq ($(OS_ARCH),WINNT) 
ifndef SKIP_PDB
_PUBLIC_ADMIN_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(PUBLIC_ADMIN_BINARIES)))
endif # SKIP_PDB
ifndef SKIP_MAP
_PUBLIC_ADMIN_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(PUBLIC_ADMIN_BINARIES)))
endif # SKIP_MAP

endif # OS==WINNT
endif # EXE && !NO_EXE_SUFFIX
else
_PUBLIC_ADMIN_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(PUBLIC_ADMIN_BINARIES))
endif #ifndef EXE

ifeq ($(OS_ARCH),WINNT) 
ifndef SKIP_PDB
_PUBLIC_ADMIN_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(PUBLIC_ADMIN_BINARIES)))
endif # SKIP_PDB
ifndef SKIP_MAP
_PUBLIC_ADMIN_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(PUBLIC_ADMIN_BINARIES)))
endif # SKIP_MAP
endif # OS==WINNT

endif #PUBLIC_ADMIN_BINARIES

ifdef PUBLIC_HTTPADMIN_BINARIES
SOMETHING_EXPORTED=1
ifdef EXE
ifdef NO_EXE_SUFFIX
_PUBLIC_HTTPADMIN_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(PUBLIC_HTTPADMIN_BINARIES))
else
_PUBLIC_HTTPADMIN_BINARIES+=$(addprefix $(OBJDIR_BIN)/, \
                   $(addsuffix $(EXE), $(PUBLIC_HTTPADMIN_BINARIES)))
ifeq ($(OS_ARCH),WINNT) 
ifndef SKIP_PDB
_PUBLIC_HTTPADMIN_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(PUBLIC_HTTPADMIN_BINARIES)))
endif # SKIP_PDB
ifndef SKIP_MAP
_PUBLIC_HTTPADMIN_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(PUBLIC_HTTPADMIN_BINARIES)))
endif # SKIP_MAP

endif # OS==WINNT
endif # EXE && !NO_EXE_SUFFIX
else
_PUBLIC_HTTPADMIN_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(PUBLIC_HTTPADMIN_BINARIES))
endif #ifndef EXE

ifeq ($(OS_ARCH),WINNT) 
ifndef SKIP_PDB
_PUBLIC_HTTPADMIN_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(PUBLIC_HTTPADMIN_BINARIES)))
endif # SKIP_PDB
ifndef SKIP_MAP
_PUBLIC_HTTPADMIN_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(PUBLIC_HTTPADMIN_BINARIES)))
endif # SKIP_MAP
endif # OS==WINNT

endif #PUBLIC_HTTPADMIN_BINARIES

ifdef EXPORT_BINARIES
SOMETHING_EXPORTED=1
ifdef EXE
ifdef NO_EXE_SUFFIX
_EXPORT_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(EXPORT_BINARIES))
else
_EXPORT_BINARIES+=$(addprefix $(OBJDIR_BIN)/, \
                   $(addsuffix $(EXE), $(EXPORT_BINARIES)))
ifeq ($(OS_ARCH),WINNT) 
ifndef SKIP_PDB
_EXPORT_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(EXPORT_BINARIES)))
endif # SKIP_PDB

ifndef SKIP_MAP
_EXPORT_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(EXPORT_BINARIES)))
endif # SKIP_MAP

endif # OS==WINNT
endif # EXE && !NO_EXE_SUFFIX
else
_EXPORT_BINARIES+=$(addprefix $(OBJDIR_BIN)/, $(EXPORT_BINARIES))
endif #ifndef EXE

ifeq ($(OS_ARCH),WINNT) 

ifndef SKIP_PDB
_EXPORT_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .pdb, $(EXPORT_BINARIES)))
endif # SKIP_PDB

ifndef SKIP_MAP
_EXPORT_BINARIES+= $(addprefix $(OBJDIR_BIN)/, $(addsuffix .map, $(EXPORT_BINARIES)))
endif # SKIP_MAP

endif # OS==WINNT

endif #EXPORT_BINARIES

ifdef LOCAL_BINARIES
_LOCAL_BINARIES=$(addprefix $(OBJDIR_BIN)/, \
		   $(addsuffix $(EXE), $(LOCAL_BINARIES)))
endif

ifdef EXPORT_TESTS_BINARIES
SOMETHING_EXPORTED=1
_EXPORT_TESTS_BINARIES=$(addprefix $(OBJDIR_BIN)/, \
		   $(addsuffix $(EXE), $(EXPORT_TESTS_BINARIES)))
EXPORT_TESTS_FILES+=$(_EXPORT_TESTS_BINARIES)
endif

ifdef EXPORT_TESTS_DYNAMIC_LIBRARIES
SOMETHING_EXPORTED=1
_EXPORT_TESTS_DYNAMIC_LIBRARIES=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
		   $(addsuffix .$(DYNAMIC_LIB_SUFFIX), $(EXPORT_TESTS_DYNAMIC_LIBRARIES)))
EXPORT_TESTS_FILES+=$(_EXPORT_TESTS_DYNAMIC_LIBRARIES)
endif

ifdef EXPORT_TESTS
SOMETHING_EXPORTED=1
EXPORT_TESTS_FILES+=$(EXPORT_TESTS)
endif

ifdef PUBLIC_EARLY_LIBRARIES
SOMETHING_EXPORTED=1
_PUBLIC_EARLY_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                      $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                         $(PUBLIC_EARLY_LIBRARIES)))
endif

ifdef EXPORT_EARLY_LIBRARIES
SOMETHING_EXPORTED=1
_EXPORT_EARLY_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                      $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                         $(EXPORT_EARLY_LIBRARIES)))
endif

ifdef PUBLIC_LIBRARIES
SOMETHING_EXPORTED=1
_PUBLIC_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                      $(addsuffix .$(STATIC_LIB_SUFFIX), $(PUBLIC_LIBRARIES)))
endif

ifdef EXPORT_LIBRARIES
SOMETHING_EXPORTED=1
_EXPORT_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                      $(addsuffix .$(STATIC_LIB_SUFFIX), $(EXPORT_LIBRARIES)))
endif

ifdef LOCAL_EARLY_LIBRARIES
_LOCAL_EARLY_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                      $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                         $(LOCAL_EARLY_LIBRARIES)))
endif

ifdef LOCAL_LIBRARIES
_LOCAL_LIBRARIES=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX),\
			$(addsuffix .$(STATIC_LIB_SUFFIX), $(LOCAL_LIBRARIES)))
endif

ifdef PUBLIC_EARLY_DYNAMIC_LIBRARIES
SOMETHING_EXPORTED=1
_PUBLIC_EARLY_DYNAMIC_LIBRARIES=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
	                    $(addsuffix .$(DYNAMIC_LIB_SUFFIX), \
                                         $(PUBLIC_EARLY_DYNAMIC_LIBRARIES)))
ifeq ($(OS_ARCH),WINNT) 
_PUBLIC_EARLY_STAT_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                        $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                     $(PUBLIC_EARLY_DYNAMIC_LIBRARIES)))
ifndef SKIP_PDB
_PUBLIC_EARLY_DYNAMIC_LIBRARIES+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
                        $(addsuffix .pdb, $(PUBLIC_EARLY_DYNAMIC_LIBRARIES)))
endif # SKIP_PDB

ifndef SKIP_MAP
_PUBLIC_EARLY_DYNAMIC_LIBRARIES+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
			$(addsuffix .map, $(PUBLIC_EARLY_DYNAMIC_LIBRARIES)))
endif # SKIP_MAP

_PUBLIC_EARLY_LIBRARIES+=$(_PUBLIC_EARLY_STAT_LIBRARIES)
$(_PUBLIC_EARLY_STAT_LIBRARIES): $(_PUBLIC_EARLY_DYNAMIC_LIBRARIES)
endif # OS==WINNT
endif # PUBLIC_EARLY_DYNAMIC_LIBRARIES

ifdef EXPORT_EARLY_DYNAMIC_LIBRARIES
SOMETHING_EXPORTED=1
_EXPORT_EARLY_DYNAMIC_LIBRARIES=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
	                    $(addsuffix .$(DYNAMIC_LIB_SUFFIX), \
                                         $(EXPORT_EARLY_DYNAMIC_LIBRARIES)))
ifeq ($(OS_ARCH),WINNT) 
_EXPORT_EARLY_STAT_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                        $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                     $(EXPORT_EARLY_DYNAMIC_LIBRARIES)))
ifndef SKIP_PDB
_EXPORT_EARLY_DYNAMIC_LIBRARIES+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
                        $(addsuffix .pdb, $(EXPORT_EARLY_DYNAMIC_LIBRARIES)))
endif # SKIP_PDB

ifndef SKIP_MAP
_EXPORT_EARLY_DYNAMIC_LIBRARIES+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
			$(addsuffix .map, $(EXPORT_EARLY_DYNAMIC_LIBRARIES)))
endif # SKIP_MAP

_EXPORT_EARLY_LIBRARIES+=$(_EXPORT_EARLY_STAT_LIBRARIES)
$(_EXPORT_EARLY_STAT_LIBRARIES): $(_EXPORT_EARLY_DYNAMIC_LIBRARIES)
endif # OS==WINNT
endif # EXPORT_EARLY_DYNAMIC_LIBRARIES

ifdef PUBLIC_DYNAMIC_LIBRARIES
SOMETHING_EXPORTED=1
_PUBLIC_DYNAMIC_LIBRARIES=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
	                    $(addsuffix .$(DYNAMIC_LIB_SUFFIX), \
                                         $(PUBLIC_DYNAMIC_LIBRARIES)))
ifeq ($(OS_ARCH),WINNT) 
#ruslan: don't copy .lib on NT when there's no need to
ifdef DLL_LIB_INTERNAL
_EXPORT_STAT_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                        $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                     $(PUBLIC_DYNAMIC_LIBRARIES)))
_EXPORT_LIBRARIES+=$(_EXPORT_STAT_LIBRARIES)
$(_EXPORT_STAT_LIBRARIES): $(_EXPORT_DYNAMIC_LIBRARIES)
else
_PUBLIC_STAT_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                        $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                     $(PUBLIC_DYNAMIC_LIBRARIES)))
endif

ifndef SKIP_PDB
_PUBLIC_DYNAMIC_LIBRARIES+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
                        $(addsuffix .pdb, $(PUBLIC_DYNAMIC_LIBRARIES)))
endif # SKIP_PDB

ifndef SKIP_MAP
_PUBLIC_DYNAMIC_LIBRARIES+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
			$(addsuffix .map, $(PUBLIC_DYNAMIC_LIBRARIES)))
endif # SKIP_MAP

ifdef _PUBLIC_STAT_LIBRARIES
_PUBLIC_LIBRARIES+=$(_PUBLIC_STAT_LIBRARIES)
$(_PUBLIC_STAT_LIBRARIES): $(_PUBLIC_DYNAMIC_LIBRARIES)
endif
endif # OS==WINNT
endif # PUBLIC_DYNAMIC_LIBRARIES

ifdef EXPORT_DYNAMIC_LIBRARIES
SOMETHING_EXPORTED=1
_EXPORT_DYNAMIC_LIBRARIES=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
	                    $(addsuffix .$(DYNAMIC_LIB_SUFFIX), \
                                         $(EXPORT_DYNAMIC_LIBRARIES)))
ifeq ($(OS_ARCH),WINNT) 
_EXPORT_STAT_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                        $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                     $(EXPORT_DYNAMIC_LIBRARIES)))
ifndef SKIP_PDB
_EXPORT_DYNAMIC_LIBRARIES+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
                        $(addsuffix .pdb, $(EXPORT_DYNAMIC_LIBRARIES)))
endif # SKIP_PDB

ifndef SKIP_MAP
_EXPORT_DYNAMIC_LIBRARIES+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
			$(addsuffix .map, $(EXPORT_DYNAMIC_LIBRARIES)))
endif # SKIP_MAP

_EXPORT_LIBRARIES+=$(_EXPORT_STAT_LIBRARIES)
$(_EXPORT_STAT_LIBRARIES): $(_EXPORT_DYNAMIC_LIBRARIES)
endif # OS==WINNT
endif # EXPORT_DYNAMIC_LIBRARIES

ifdef PUBLIC_DYNAMIC_LIBRARIES_SPECIAL
SOMETHING_EXPORTED=1
_PUBLIC_DYNAMIC_LIBRARIES_SPECIAL=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
	                    $(addsuffix .$(DYNAMIC_LIB_SUFFIX), \
                                         $(PUBLIC_DYNAMIC_LIBRARIES_SPECIAL)))
ifeq ($(OS_ARCH),WINNT) 
_PUBLIC_STAT_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                        $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                     $(PUBLIC_DYNAMIC_LIBRARIES_SPECIAL)))
ifndef SKIP_PDB
_PUBLIC_DYNAMIC_LIBRARIES_SPECIAL+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
                        $(addsuffix .pdb, $(PUBLIC_DYNAMIC_LIBRARIES_SPECIAL)))
endif # SKIP_PDB

ifndef SKIP_MAP
_PUBLIC_DYNAMIC_LIBRARIES_SPECIAL+=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
			$(addsuffix .map, $(PUBLIC_DYNAMIC_LIBRARIES_SPECIAL)))
endif # SKIP_MAP

_PUBLIC_LIBRARIES+=$(_PUBLIC_STAT_LIBRARIES)
$(_PUBLIC_STAT_LIBRARIES): $(_PUBLIC_DYNAMIC_LIBRARIES_SPECIAL)
endif # OS==WINNT
endif # PUBLIC_DYNAMIC_LIBRARIES_SPECIAL


ifdef LOCAL_DYNAMIC_LIBRARIES
_LOCAL_DYNAMIC_LIBRARIES=$(addprefix $(OBJDIR_DYNLIB)/$(LIBPREFIX), \
                         $(addsuffix .$(DYNAMIC_LIB_SUFFIX), \
                                        $(LOCAL_DYNAMIC_LIBRARIES)))
ifeq ($(OS_ARCH),WINNT)
_LOCAL_STAT_LIBRARIES+=$(addprefix $(OBJDIR_LIB)/$(LIBPREFIX), \
                       $(addsuffix .$(STATIC_LIB_SUFFIX), \
                                    $(LOCAL_DYNAMIC_LIBRARIES)))
_LOCAL_LIBRARIES+=$(_LOCAL_STAT_LIBRARIES)
$(_LOCAL_STAT_LIBRARIES) : $(_LOCAL_DYNAMIC_LIBRARIES)
endif # OS==WINNT
endif # LOCAL_DYNAMIC_LIBRARIES


ifndef NO_STD_EARLY_LIBRARIES_TARGET
early_libraries:: $(_EXPORT_EARLY_DYNAMIC_LIBRARIES) \
                  $(_EXPORT_EARLY_LIBRARIES) \
				  $(_PUBLIC_EARLY_DYNAMIC_LIBRARIES) \
                  $(_PUBLIC_EARLY_LIBRARIES) \
				  $(_LOCAL_EARLY_LIBRARIES)
	$(LOOP_OVER_DIRS)
endif #NO_STD_EARLY_LIBRARIES_TARGET

ifndef NO_STD_LIBRARIES_TARGET
libraries::$(_LOCAL_LIBRARIES) $(_EXPORT_LIBRARIES) $(_PUBLIC_LIBRARIES) $(_EXPORT_DYNAMIC_LIBRARIES) $(_PUBLIC_DYNAMIC_LIBRARIES) $(_PUBLIC_DYNAMIC_LIBRARIES_SPECIAL) $(_LOCAL_DYNAMIC_LIBRARIES) $(_EXPORT_TESTS_DYNAMIC_LIBRARIES)
	$(LOOP_OVER_DIRS)
endif #NO_STD_LIBRARIES_TARGET

ifndef NO_STD_LINK_TARGET
link:: $(_LOCAL_BINARIES) $(_EXPORT_BINARIES) $(_PRIVATE_BINARIES) $(_PUBLIC_BINARIES) $(_PUBLIC_ADMIN_BINARIES)$(_PUBLIC_HTTPADMIN_BINARIES) $(_EXPORT_TESTS_BINARIES)
	$(LOOP_OVER_DIRS)
endif #NO_STD_LINK_TARGET

else #BUILD_JAVA

ifdef PUBLIC_JAR
SOMETHING_EXPORTED=1
endif

ifdef PUBLIC_WAR
SOMETHING_EXPORTED=1
endif

ifdef PUBLIC_JAVA_SAMPLES
SOMETHING_EXPORTED=1
endif

ifdef EXPORT_JAR
SOMETHING_EXPORTED=1
endif

_OBJS+=$(JAVA_SRCS:=.class)

ifdef _OBJS
OBJS+=$(addprefix $(OBJDIR)/,$(_OBJS))
endif #_OBJS

endif #BUILD_JAVA

ifdef DIRS
ifndef NO_LOOP_OVER_DIRS
LOOP_OVER_DIRS=	for dir in $(DIRS) ; do \
			( cd $${dir} &&  \
			$(MAKE) MAKEFLAGS='$(MAKEFLAGS) $(EXTRA_MAKEFLAGS)' $@ ) || exit 255 ; \
	 	done
ALLPUBLISH_LOOP_OVER_DIRS=	for dir in $(DIRS) ; do \
			( cd $${dir} &&  \
			$(MAKE) MAKEFLAGS='$(MAKEFLAGS) $(EXTRA_MAKEFLAGS)' all publish ) || exit 255 ; \
	 	done
else #NO_LOOP_OVER_DIRS
LOOP_OVER_DIRS= $(ECHO) directory traversal skipped
ALLPUBLISH_LOOP_OVER_DIRS= $(ECHO) directory traversal skipped
endif #NO_LOOP_OVER_DIRS
endif #DIRS

ifndef MAKE_OBJDIR
define MAKE_OBJDIR
if test ! -d $(OBJDIR); then rm -rf $(OBJDIR); $(MKDIR) -p $(OBJDIR); fi
endef
endif #MAKE_OBJDIR


####################################
###### PUBLICATION CODE ############
####################################

print_exports::
	$(LOOP_OVER_DIRS)

print_exports::
	@$(ECHO) files to be exported for module $(MODULE):

ifdef SOMETHING_EXPORTED
ifndef NO_STD_PUBLISH_RULE
publish: local_pre_publish pre_publish publish_copy local_post_publish
ifndef NO_PUBLISH_RECURSE
	$(LOOP_OVER_DIRS)
endif #NO_PUBLISH_RECURSE
endif #NO_STD_PUBLISH_RULE

# can be used by local makefiles to prepare for publish:
local_pre_publish::

# can unpack tarfiles, etc.
local_post_publish::
else # n SOMETHING_EXPORTED

publish: pre_publish
	@$(ECHO) Nothing to publish. Traversing subdirs
ifndef NO_PUBLISH_RECURSE
	$(LOOP_OVER_DIRS)
endif #NO_PUBLISH_RECURSE
endif # SOMETHING_EXPORTED

.PHONY: pre_publish publish_copy

ifdef _PUBLIC_INSTALLER_FILES
print_exports::
	@$(ECHO) "INSTALLER FILES: $(PUBLIC_INSTALLER_FILES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/lib/install/templates
	$(CP) -f $(_PUBLIC_INSTALLER_FILES) $(WORK_ROOT)/lib/install/templates
endif #_PUBLIC_INSTALLER_FILES

# rules for admin admin icons, html 
ifdef _PUBLIC_ADMIN_FILES
print_exports::
	@$(ECHO) "ADMIN FILES: $(PUBLIC_ADMIN_FILES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/lib/admincgi/$(MODULE)
	$(CP) -f $(_PUBLIC_ADMIN_FILES) $(WORK_ROOT)/lib/admincgi/$(MODULE)
endif #_PUBLIC_ADMIN_FILES


# rules for admin admin cgi
ifdef _PUBLIC_ADMIN_BINARIES
print_exports::
	@$(ECHO) "ADMIN BINARIES FILES: $(PUBLIC_ADMIN_BINARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/lib/admincgi
	$(CP) -f $(_PUBLIC_ADMIN_BINARIES) $(WORK_ROOT)/lib/admincgi
endif #_PUBLIC_ADMIN_BINARIES

# rules for httpadmin icons, html 
ifdef _PUBLIC_HTTPADMIN_FILES
print_exports::
	@$(ECHO) "ADMIN FILES: $(PUBLIC_HTTPADMIN_FILES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/lib/instancecgi/$(MODULE)
	$(CP) -f $(_PUBLIC_HTTPADMIN_FILES) $(WORK_ROOT)/lib/instancecgi/$(MODULE)
endif #_PUBLIC_HTTPADMIN_FILES

# Rules for http cgi
ifdef _PUBLIC_HTTPADMIN_BINARIES
print_exports::
	@$(ECHO) "ADMIN BINARIES FILES: $(PUBLIC_HTTPADMIN_BINARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/lib/instancecgi
	$(CP) -f $(_PUBLIC_HTTPADMIN_BINARIES) $(WORK_ROOT)/lib/instancecgi
endif #_PUBLIC_HTTPADMIN_BINARIES

ifdef PUBLIC_DOC_FILES
print_exports::
	@$(ECHO) "ERR_MSGS: $(PUBLIC_DOC_FILES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/samples/nsapi/$(MODULE)
	$(CP) -f $(PUBLIC_DOC_FILES) $(WORK_ROOT)/samples/nsapi/$(MODULE)
endif

ifdef PUBLIC_SDK_FILES
print_exports::
	@$(ECHO) "ERR_MSGS: $(PUBLIC_SDK_FILES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/samples/nsapi/$(MODULE)
	$(CP) -f $(PUBLIC_SDK_FILES) $(WORK_ROOT)/samples/nsapi/$(MODULE)
endif # PUBLIC_SDK_FILES


ifndef BUILD_JAVA

.PHONY: publish pre_publish 

#
# _HEADERS
# 
ifdef EXPORT_HEADERS
print_exports::
	@$(ECHO) "HEADERS: $(_EXPORT_HEADERS)"
publish_copy::
	$(MKDIR_DASH_P) $(INTERNAL_ROOT)/include/$(MODULE)
	$(CP) -f $(_EXPORT_HEADERS) $(INTERNAL_ROOT)/include/$(MODULE)
endif #EXPORT_HEADERS

ifdef PUBLIC_HEADERS
print_exports::
	@$(ECHO) "HEADERS: $(_PUBLIC_HEADERS)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/include/$(MODULE)
	$(CP) -f $(_PUBLIC_HEADERS) $(WORK_ROOT)/include/$(MODULE)
endif #PUBLIC_HEADERS

#
# _TESTS_FILES
#
ifdef EXPORT_TESTS_FILES
ifndef EXPORT_TESTS_DIR
EXPORT_TESTS_DIR=$(MODULE)
endif
print_exports::
	@$(ECHO) "TESTS: $(EXPORT_TESTS_FILES)"
publish_copy::
	$(MKDIR_DASH_P) $(INTERNAL_ROOT)/tests/$(EXPORT_TESTS_DIR)
	$(CP) -f $(EXPORT_TESTS_FILES) $(INTERNAL_ROOT)/tests/$(EXPORT_TESTS_DIR)
endif #EXPORT_TESTS

#
# _EARLY_LIBRARIES
#
ifdef _EXPORT_EARLY_LIBRARIES
print_exports::
	@$(ECHO) "LIBRARIES: $(_EXPORT_EARLY_LIBRARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(INTERNAL_ROOT)/lib
	$(CP) -f $(_EXPORT_EARLY_LIBRARIES) $(INTERNAL_ROOT)/lib
endif #_EXPORT_EARLY_LIBRARIES

ifdef _PUBLIC_EARLY_LIBRARIES
print_exports::
	@$(ECHO) "LIBRARIES: $(_PUBLIC_EARLY_LIBRARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/lib
	$(CP) -f $(_PUBLIC_EARLY_LIBRARIES) $(WORK_ROOT)/lib
endif #_PUBLIC_EARLY_LIBRARIES

#
# _LIBRARIES
#
ifdef _EXPORT_LIBRARIES
print_exports::
	@$(ECHO) "LIBRARIES: $(_EXPORT_LIBRARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(INTERNAL_ROOT)/lib
	$(CP) -f $(_EXPORT_LIBRARIES) $(INTERNAL_ROOT)/lib
endif #_EXPORT_LIBRARIES

ifdef _PUBLIC_LIBRARIES
print_exports::
	@$(ECHO) "LIBRARIES: $(_PUBLIC_LIBRARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/lib
	$(CP) -f $(_PUBLIC_LIBRARIES) $(WORK_ROOT)/lib
endif #_PUBLIC_LIBRARIES

#
# _EARLY_DYNAMIC_LIBRARIES
#
ifdef _EXPORT_EARLY_DYNAMIC_LIBRARIES
ifeq ($(OS_ARCH),WINNT) 
DLL_PUBLISH_DIR=$(INTERNAL_ROOT)/bin
else #OS=NT
DLL_PUBLISH_DIR=$(INTERNAL_ROOT)/lib
endif #OS=NT
print_exports::
	@$(ECHO) "DLLS: $(_EXPORT_EARLY_DYNAMIC_LIBRARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(DLL_PUBLISH_DIR)
	$(CP) -f $(_EXPORT_EARLY_DYNAMIC_LIBRARIES) $(DLL_PUBLISH_DIR)
endif #_EXPORT_EARLY_DYNAMIC_LIBRARIES

ifdef _PUBLIC_EARLY_DYNAMIC_LIBRARIES
ifeq ($(OS_ARCH),WINNT) 
DLL_PUBLISH_DIR=$(WORK_ROOT)/bin
else #OS=NT
DLL_PUBLISH_DIR=$(WORK_ROOT)/lib
endif #OS=NT

print_exports::
	@$(ECHO) "DLLS: $(_PUBLIC_EARLY_DYNAMIC_LIBRARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(DLL_PUBLISH_DIR)
	$(CP) -f $(_PUBLIC_EARLY_DYNAMIC_LIBRARIES) $(DLL_PUBLISH_DIR)
endif #_PUBLIC_EARLY_DYNAMIC_LIBRARIES

#
# _DYNAMIC_LIBRARIES
#

ifdef _EXPORT_DYNAMIC_LIBRARIES
ifeq ($(OS_ARCH),WINNT) 
DLL_PUBLISH_DIR=$(INTERNAL_ROOT)/bin
else #OS=NT
DLL_PUBLISH_DIR=$(INTERNAL_ROOT)/lib
endif #OS=NT

print_exports::
	@$(ECHO) "DLLS: $(_EXPORT_DYNAMIC_LIBRARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(DLL_PUBLISH_DIR)
	$(CP) -f $(_EXPORT_DYNAMIC_LIBRARIES) $(DLL_PUBLISH_DIR)
endif #_EXPORT_DYNAMIC_LIBRARIES

ifdef _PUBLIC_DYNAMIC_LIBRARIES
ifeq ($(OS_ARCH),WINNT) 
DLL_PUBLISH_DIR=$(WORK_ROOT)/bin
else #OS=NT
DLL_PUBLISH_DIR=$(WORK_ROOT)/lib
endif #OS=NT

print_exports::
	@$(ECHO) "DLLS: $(_PUBLIC_DYNAMIC_LIBRARIES)"
publish_copy::
	$(MKDIR_DASH_P) $(DLL_PUBLISH_DIR)
	$(CP) -f $(_PUBLIC_DYNAMIC_LIBRARIES) $(DLL_PUBLISH_DIR)
endif #_PUBLIC_DYNAMIC_LIBRARIES

ifdef _PUBLIC_DYNAMIC_LIBRARIES_SPECIAL
####DLL_PUBLISH_DIR=$(WORK_ROOT)/lib/$(MODULE)
DLL_PUBLISH_DIR=$(WORK_ROOT)/lib

print_exports::
	@$(ECHO) "DLLS: $(_PUBLIC_DYNAMIC_LIBRARIES_SPECIAL)"
publish_copy::
	$(MKDIR_DASH_P) $(DLL_PUBLISH_DIR)
	$(CP) -f $(_PUBLIC_DYNAMIC_LIBRARIES_SPECIAL) $(DLL_PUBLISH_DIR)
endif #_PUBLIC_DYNAMIC_LIBRARIES_SPECIAL

#
# _BINARIES
#
ifdef _EXPORT_BINARIES
print_exports::
	@$(ECHO) "BINARIES: $(_EXPORT_BINARIES)"	
publish_copy::
	$(MKDIR_DASH_P) $(INTERNAL_ROOT)/bin
	$(CP) -f $(_EXPORT_BINARIES) $(INTERNAL_ROOT)/bin
endif # _EXPORT_BINARIES

ifdef _PUBLIC_BINARIES
print_exports::
	@$(ECHO) "BINARIES: $(_PUBLIC_BINARIES)"	
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/bin
	$(CP) -f $(_PUBLIC_BINARIES) $(WORK_ROOT)/bin
endif # _PUBLIC_BINARIES

ifdef _PRIVATE_BINARIES
ifeq ($(OS_ARCH),WINNT) 
EXE_PUBLISH_DIR=$(WORK_ROOT)/bin
else #OS=NT
EXE_PUBLISH_DIR=$(WORK_ROOT)/lib
endif #OS=NT
print_exports::
	@$(ECHO) "BINARIES: $(_PRIVATE_BINARIES)"	
publish_copy::
	@$(ECHO) "PRIVATE BINARIES: $(_PRIVATE_BINARIES)"	
	$(MKDIR_DASH_P) $(EXE_PUBLISH_DIR)
	$(CP) -f $(_PRIVATE_BINARIES) $(EXE_PUBLISH_DIR)
endif # _PRIVATE_BINARIES

.PHONY: all clean clobber headers compile early_libraries libraries link

endif #BUILD_JAVA

ifndef NO_STD_CLEAN_TARGET
clean::
	$(RM) -f $(OBJS) $(NOSUCHFILE) 
ifeq ($(OS_ARCH),SunOS)
	- $(RM) -rf Templates.DB
endif #OS==SunOS
	$(LOOP_OVER_DIRS)
endif #NO_STD_CLEAN_TARGET

ifndef NO_STD_CLOBBER_TARGET
clobber::
ifdef OBJDIR
	$(RM) -rf $(OBJDIR) $(NOSUCHFILE)
endif #OBJDIR
ifeq ($(OS_ARCH),SunOS)
	$(RM) -rf .sb
	$(RM) -rf SunWS_cache        
endif #OS_ARCH=SunOS
ifeq ($(OS_ARCH),WINNT)
	$(RM) -f pmc*.log
endif #OS_ARCH=WINNT
	$(LOOP_OVER_DIRS)
ifdef CLOBBER_COMPONENT
	$(RM) -rf $(INTERNAL_ROOT) $(WORK_ROOT)
endif
endif #NO_STD_CLOBBER_TARGET

cleanup ::
	$(FIND) $(WORK_ROOT) -name CVS | $(XARGS) -n 1 $(RM) -rf $(NOSUCHFILE)

ifdef PUBLIC_RESOURCEFILES
print_exports::
	@$(ECHO) "Resource Files: $(PUBLIC_RESOURCEFILES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/lib/messages
	$(CP) -f $(PUBLIC_RESOURCEFILES) $(WORK_ROOT)/lib/messages
endif #PUBLIC_RESOURCEFILES

ifdef PUBLIC_MIBFILES
print_exports::
	@$(ECHO) "Helper Files: $(PUBLIC_MIBFILES)"
publish_copy::
	$(MKDIR_DASH_P) $(WORK_ROOT)/lib/mib
	$(CP) -f $(PUBLIC_MIBFILES) $(WORK_ROOT)/lib/mib
endif #PUBLIC_MIBFILES

ifndef SKIP_AUTO_VERSION_INSERTION

######################## DLL_AUTO_VERSION ############################
DLL_VERGEN_FLAGS+=-filename $(DLL_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef DLL_DESC
DLL_VERGEN_FLAGS+=-description $(DLL_DESC)
endif
ifdef DLL_COPYRIGHT
DLL_VERGEN_FLAGS+=-copyright $(DLL_COPYRIGHT)
endif
ifdef DLL_VER
DLL_VERGEN_FLAGS+=-v $(DLL_VER)
endif
ifdef DLL_COMMENT
DLL_VERGEN_FLAGS+= -comment $(DLL_COMMENT)
endif
ifdef DLL_SPECIAL
DLL_VERGEN_FLAGS+= -special $(DLL_SPECIAL)
endif

DLL_RC_VERGEN_FLAGS+=$(DLL_VERGEN_FLAGS) -rc


ifdef DLL_TARGET
$(OBJDIR)/auto_$(DLL_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(DLL_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(DLL_TARGET).cpp
compile::$(OBJDIR)/auto_$(DLL_TARGET).$(OBJ)
endif
endif
DLL_NONPARSED_OBJS+=$(OBJDIR)/auto_$(DLL_TARGET).$(OBJ)
$(OBJDIR)/$(DLL_TARGET).$(DYNAMIC_LIB_SUFFIX): $(OBJDIR)/auto_$(DLL_TARGET).$(OBJ)

######################## DLL1_AUTO_VERSION ############################
DLL1_VERGEN_FLAGS+=-filename $(DLL1_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef DLL1_DESC
DLL1_VERGEN_FLAGS+=-description $(DLL1_DESC)
endif
ifdef DLL1_COPYRIGHT
DLL1_VERGEN_FLAGS+=-copyright $(DLL1_COPYRIGHT)
endif
ifdef DLL1_VER
DLL1_VERGEN_FLAGS+=-v $(DLL1_VER)
endif
ifdef DLL1_COMMENT
DLL1_VERGEN_FLAGS+= -comment $(DLL1_COMMENT)
endif
ifdef DLL1_SPECIAL
DLL1_VERGEN_FLAGS+= -special $(DLL1_SPECIAL)
endif

DLL1_RC_VERGEN_FLAGS+=$(DLL1_VERGEN_FLAGS) -rc


ifdef DLL1_TARGET
$(OBJDIR)/auto_$(DLL1_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(DLL1_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(DLL1_TARGET).cpp
compile::$(OBJDIR)/auto_$(DLL1_TARGET).$(OBJ)
endif
endif
DLL1_NONPARSED_OBJS+=$(OBJDIR)/auto_$(DLL1_TARGET).$(OBJ)
$(OBJDIR)/$(DLL1_TARGET).$(DYNAMIC_LIB_SUFFIX): $(OBJDIR)/auto_$(DLL1_TARGET).$(OBJ)

######################## DLL2_AUTO_VERSION ############################
DLL2_VERGEN_FLAGS+=-filename $(DLL2_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef DLL2_DESC
DLL2_VERGEN_FLAGS+=-description $(DLL2_DESC)
endif
ifdef DLL2_COPYRIGHT
DLL2_VERGEN_FLAGS+=-copyright $(DLL2_COPYRIGHT)
endif
ifdef DLL2_VER
DLL2_VERGEN_FLAGS+=-v $(DLL2_VER)
endif
ifdef DLL2_COMMENT
DLL2_VERGEN_FLAGS+= -comment $(DLL2_COMMENT)
endif
ifdef DLL2_SPECIAL
DLL2_VERGEN_FLAGS+= -special $(DLL2_SPECIAL)
endif

DLL2_RC_VERGEN_FLAGS+=$(DLL2_VERGEN_FLAGS) -rc

ifdef DLL2_TARGET
$(OBJDIR)/auto_$(DLL2_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(DLL2_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(DLL2_TARGET).cpp
compile::$(OBJDIR)/auto_$(DLL2_TARGET).$(OBJ)
endif
endif

DLL2_NONPARSED_OBJS+=$(OBJDIR)/auto_$(DLL2_TARGET).$(OBJ)
$(OBJDIR)/$(DLL2_TARGET).$(DYNAMIC_LIB_SUFFIX): $(OBJDIR)/auto_$(DLL2_TARGET).$(OBJ)

######################## DLL3_AUTO_VERSION ############################
DLL3_VERGEN_FLAGS+=-filename $(DLL3_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef DLL3_DESC
DLL3_VERGEN_FLAGS+=-description $(DLL3_DESC)
endif
ifdef DLL3_COPYRIGHT
DLL3_VERGEN_FLAGS+=-copyright $(DLL3_COPYRIGHT)
endif
ifdef DLL3_VER
DLL3_VERGEN_FLAGS+=-v $(DLL3_VER)
endif
ifdef DLL3_COMMENT
DLL3_VERGEN_FLAGS+= -comment $(DLL3_COMMENT)
endif
ifdef DLL3_SPECIAL
DLL3_VERGEN_FLAGS+= -special $(DLL3_SPECIAL)
endif

DLL3_RC_VERGEN_FLAGS+=$(DLL3_VERGEN_FLAGS) -rc

ifdef DLL3_TARGET
$(OBJDIR)/auto_$(DLL3_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(DLL3_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(DLL3_TARGET).cpp
compile::$(OBJDIR)/auto_$(DLL3_TARGET).$(OBJ)
endif
endif

DLL3_NONPARSED_OBJS+=$(OBJDIR)/auto_$(DLL3_TARGET).$(OBJ)
$(OBJDIR)/$(DLL3_TARGET).$(DYNAMIC_LIB_SUFFIX): $(OBJDIR)/auto_$(DLL3_TARGET).$(OBJ)

######################## DLL4_AUTO_VERSION ############################
DLL4_VERGEN_FLAGS+=-filename $(DLL4_TARGET).$(DYNAMIC_LIB_SUFFIX)
ifdef DLL4_DESC
DLL4_VERGEN_FLAGS+=-description $(DLL4_DESC)
endif
ifdef DLL4_COPYRIGHT
DLL4_VERGEN_FLAGS+=-copyright $(DLL4_COPYRIGHT)
endif
ifdef DLL4_VER
DLL4_VERGEN_FLAGS+=-v $(DLL4_VER)
endif
ifdef DLL4_COMMENT
DLL4_VERGEN_FLAGS+= -comment $(DLL4_COMMENT)
endif
ifdef DLL4_SPECIAL
DLL4_VERGEN_FLAGS+= -special $(DLL4_SPECIAL)
endif

DLL4_RC_VERGEN_FLAGS+=$(DLL4_VERGEN_FLAGS) -rc

ifdef DLL4_TARGET
$(OBJDIR)/auto_$(DLL4_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(DLL4_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(DLL4_TARGET).cpp
compile::$(OBJDIR)/auto_$(DLL4_TARGET).$(OBJ)
endif
endif

DLL4_NONPARSED_OBJS+=$(OBJDIR)/auto_$(DLL4_TARGET).$(OBJ)
$(OBJDIR)/$(DLL4_TARGET).$(DYNAMIC_LIB_SUFFIX): $(OBJDIR)/auto_$(DLL4_TARGET).$(OBJ)

######################## EXE_AUTO_VERSION ############################
EXE_VERGEN_FLAGS+=-filename $(EXE_TARGET)$(EXE)
ifdef EXE_DESC
EXE_VERGEN_FLAGS+=-description $(EXE_DESC)
endif
ifdef EXE_COPYRIGHT
EXE_VERGEN_FLAGS+=-copyright $(EXE_COPYRIGHT)
endif
ifdef EXE_VER
EXE_VERGEN_FLAGS+=-v $(EXE_VER)
endif
ifdef EXE_COMMENT
EXE_VERGEN_FLAGS+= -comment $(EXE_COMMENT)
endif
ifdef EXE_SPECIAL
EXE_VERGEN_FLAGS+= -special $(EXE_SPECIAL)
endif

EXE_RC_VERGEN_FLAGS+=$(EXE_VERGEN_FLAGS) -rc

ifdef EXE_TARGET
$(OBJDIR)/auto_$(EXE_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE_TARGET).$(OBJ)


######################## EXE1_AUTO_VERSION ############################
EXE1_VERGEN_FLAGS+=-filename $(EXE1_TARGET)$(EXE)
ifdef EXE1_DESC
EXE1_VERGEN_FLAGS+=-description $(EXE1_DESC)
endif
ifdef EXE1_COPYRIGHT
EXE1_VERGEN_FLAGS+=-copyright $(EXE1_COPYRIGHT)
endif
ifdef EXE1_VER
EXE1_VERGEN_FLAGS+=-v $(EXE1_VER)
endif
ifdef EXE1_COMMENT
EXE1_VERGEN_FLAGS+= -comment $(EXE1_COMMENT)
endif
ifdef EXE1_SPECIAL
EXE1_VERGEN_FLAGS+= -special $(EXE1_SPECIAL)
endif

EXE1_RC_VERGEN_FLAGS+=$(EXE1_VERGEN_FLAGS) -rc

ifdef EXE1_TARGET
$(OBJDIR)/auto_$(EXE1_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE1_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE1_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE1_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE1_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE1_TARGET).$(OBJ)

######################## EXE2_AUTO_VERSION ############################
EXE2_VERGEN_FLAGS+=-filename $(EXE2_TARGET)$(EXE)
ifdef EXE2_DESC
EXE2_VERGEN_FLAGS+=-description $(EXE2_DESC)
endif
ifdef EXE2_COPYRIGHT
EXE2_VERGEN_FLAGS+=-copyright $(EXE2_COPYRIGHT)
endif
ifdef EXE2_VER
EXE2_VERGEN_FLAGS+=-v $(EXE2_VER)
endif
ifdef EXE2_COMMENT
EXE2_VERGEN_FLAGS+= -comment $(EXE2_COMMENT)
endif
ifdef EXE2_SPECIAL
EXE2_VERGEN_FLAGS+= -special $(EXE2_SPECIAL)
endif

EXE2_RC_VERGEN_FLAGS+=$(EXE2_VERGEN_FLAGS) -rc

ifdef EXE2_TARGET
$(OBJDIR)/auto_$(EXE2_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE2_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE2_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE2_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE2_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE2_TARGET).$(OBJ)


######################## EXE3_AUTO_VERSION ############################
EXE3_VERGEN_FLAGS+=-filename $(EXE3_TARGET)$(EXE)
ifdef EXE3_DESC
EXE3_VERGEN_FLAGS+=-description $(EXE3_DESC)
endif
ifdef EXE3_COPYRIGHT
EXE3_VERGEN_FLAGS+=-copyright $(EXE3_COPYRIGHT)
endif
ifdef EXE3_VER
EXE3_VERGEN_FLAGS+=-v $(EXE3_VER)
endif
ifdef EXE3_COMMENT
EXE3_VERGEN_FLAGS+= -comment $(EXE3_COMMENT)
endif
ifdef EXE3_SPECIAL
EXE3_VERGEN_FLAGS+= -special $(EXE3_SPECIAL)
endif

EXE3_RC_VERGEN_FLAGS+=$(EXE3_VERGEN_FLAGS) -rc

ifdef EXE3_TARGET
$(OBJDIR)/auto_$(EXE3_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE3_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE3_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE3_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE3_TARGET).$(DYNAMIC_LIB_SUFIX): $(OBJDIR)/auto_$(EXE3_TARGET).$(OBJ)

######################## EXE4_AUTO_VERSION ############################
EXE4_VERGEN_FLAGS+=-filename $(EXE4_TARGET)$(EXE)
ifdef EXE4_DESC
EXE4_VERGEN_FLAGS+=-description $(EXE4_DESC)
endif
ifdef EXE4_COPYRIGHT
EXE4_VERGEN_FLAGS+=-copyright $(EXE4_COPYRIGHT)
endif
ifdef EXE4_VER
EXE4_VERGEN_FLAGS+=-v $(EXE4_VER)
endif
ifdef EXE4_COMMENT
EXE4_VERGEN_FLAGS+= -comment $(EXE4_COMMENT)
endif
ifdef EXE4_SPECIAL
EXE4_VERGEN_FLAGS+= -special $(EXE4_SPECIAL)
endif

EXE4_RC_VERGEN_FLAGS+=$(EXE4_VERGEN_FLAGS) -rc

ifdef EXE4_TARGET
$(OBJDIR)/auto_$(EXE4_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE4_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE4_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE4_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE4_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE4_TARGET).$(OBJ)

######################## EXE5_AUTO_VERSION ############################
EXE5_VERGEN_FLAGS+=-filename $(EXE5_TARGET)$(EXE)
ifdef EXE5_DESC
EXE5_VERGEN_FLAGS+=-description $(EXE5_DESC)
endif
ifdef EXE5_COPYRIGHT
EXE5_VERGEN_FLAGS+=-copyright $(EXE5_COPYRIGHT)
endif
ifdef EXE5_VER
EXE5_VERGEN_FLAGS+=-v $(EXE5_VER)
endif
ifdef EXE5_COMMENT
EXE5_VERGEN_FLAGS+= -comment $(EXE5_COMMENT)
endif
ifdef EXE5_SPECIAL
EXE5_VERGEN_FLAGS+= -special $(EXE5_SPECIAL)
endif

EXE5_RC_VERGEN_FLAGS+=$(EXE5_VERGEN_FLAGS) -rc

ifdef EXE5_TARGET
$(OBJDIR)/auto_$(EXE5_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE5_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE5_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE5_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE5_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE5_TARGET).$(OBJ)

######################## EXE6_AUTO_VERSION ############################
EXE6_VERGEN_FLAGS+=-filename $(EXE6_TARGET)$(EXE)
ifdef EXE6_DESC
EXE6_VERGEN_FLAGS+=-description $(EXE6_DESC)
endif
ifdef EXE6_COPYRIGHT
EXE6_VERGEN_FLAGS+=-copyright $(EXE6_COPYRIGHT)
endif
ifdef EXE6_VER
EXE6_VERGEN_FLAGS+=-v $(EXE6_VER)
endif
ifdef EXE6_COMMENT
EXE6_VERGEN_FLAGS+= -comment $(EXE6_COMMENT)
endif
ifdef EXE6_SPECIAL
EXE6_VERGEN_FLAGS+= -special $(EXE6_SPECIAL)
endif

EXE6_RC_VERGEN_FLAGS+=$(EXE6_VERGEN_FLAGS) -rc

ifdef EXE6_TARGET
$(OBJDIR)/auto_$(EXE6_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE6_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE6_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE6_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE6_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE6_TARGET).$(OBJ)

######################## EXE7_AUTO_VERSION ############################
EXE7_VERGEN_FLAGS+=-filename $(EXE7_TARGET)$(EXE)
ifdef EXE7_DESC
EXE7_VERGEN_FLAGS+=-description $(EXE7_DESC)
endif
ifdef EXE7_COPYRIGHT
EXE7_VERGEN_FLAGS+=-copyright $(EXE7_COPYRIGHT)
endif
ifdef EXE7_VER
EXE7_VERGEN_FLAGS+=-v $(EXE7_VER)
endif
ifdef EXE7_COMMENT
EXE7_VERGEN_FLAGS+= -comment $(EXE7_COMMENT)
endif
ifdef EXE7_SPECIAL
EXE7_VERGEN_FLAGS+= -special $(EXE7_SPECIAL)
endif

EXE7_RC_VERGEN_FLAGS+=$(EXE7_VERGEN_FLAGS) -rc

ifdef EXE7_TARGET
$(OBJDIR)/auto_$(EXE7_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE7_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE7_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE7_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE7_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE7_TARGET).$(OBJ)

######################## EXE8_AUTO_VERSION ############################
EXE8_VERGEN_FLAGS+=-filename $(EXE8_TARGET)$(EXE)
ifdef EXE8_DESC
EXE8_VERGEN_FLAGS+=-description $(EXE8_DESC)
endif
ifdef EXE8_COPYRIGHT
EXE8_VERGEN_FLAGS+=-copyright $(EXE8_COPYRIGHT)
endif
ifdef EXE8_VER
EXE8_VERGEN_FLAGS+=-v $(EXE8_VER)
endif
ifdef EXE8_COMMENT
EXE8_VERGEN_FLAGS+= -comment $(EXE8_COMMENT)
endif
ifdef EXE8_SPECIAL
EXE8_VERGEN_FLAGS+= -special $(EXE8_SPECIAL)
endif

EXE8_RC_VERGEN_FLAGS+=$(EXE8_VERGEN_FLAGS) -rc

ifdef EXE8_TARGET
$(OBJDIR)/auto_$(EXE8_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE8_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE8_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE8_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE8_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE8_TARGET).$(OBJ)

######################## EXE9_AUTO_VERSION ############################
EXE9_VERGEN_FLAGS+=-filename $(EXE9_TARGET)$(EXE)
ifdef EXE9_DESC
EXE9_VERGEN_FLAGS+=-description $(EXE9_DESC)
endif
ifdef EXE9_COPYRIGHT
EXE9_VERGEN_FLAGS+=-copyright $(EXE9_COPYRIGHT)
endif
ifdef EXE9_VER
EXE9_VERGEN_FLAGS+=-v $(EXE9_VER)
endif
ifdef EXE9_COMMENT
EXE9_VERGEN_FLAGS+= -comment $(EXE9_COMMENT)
endif
ifdef EXE9_SPECIAL
EXE9_VERGEN_FLAGS+= -special $(EXE9_SPECIAL)
endif

EXE9_RC_VERGEN_FLAGS+=$(EXE9_VERGEN_FLAGS) -rc

ifdef EXE9_TARGET
$(OBJDIR)/auto_$(EXE9_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE9_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE9_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE9_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE9_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE9_TARGET).$(OBJ)

######################## EXE10_AUTO_VERSION ############################
EXE10_VERGEN_FLAGS+=-filename $(EXE10_TARGET)$(EXE)
ifdef EXE10_DESC
EXE10_VERGEN_FLAGS+=-description $(EXE10_DESC)
endif
ifdef EXE10_COPYRIGHT
EXE10_VERGEN_FLAGS+=-copyright $(EXE10_COPYRIGHT)
endif
ifdef EXE10_VER
EXE10_VERGEN_FLAGS+=-v $(EXE10_VER)
endif
ifdef EXE10_COMMENT
EXE10_VERGEN_FLAGS+= -comment $(EXE10_COMMENT)
endif
ifdef EXE10_SPECIAL
EXE10_VERGEN_FLAGS+= -special $(EXE10_SPECIAL)
endif

EXE10_RC_VERGEN_FLAGS+=$(EXE10_VERGEN_FLAGS) -rc

ifdef EXE10_TARGET
$(OBJDIR)/auto_$(EXE10_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE10_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE10_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE10_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE10_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE10_TARGET).$(OBJ)

######################## EXE11_AUTO_VERSION ############################
EXE11_VERGEN_FLAGS+=-filename $(EXE11_TARGET)$(EXE)
ifdef EXE11_DESC
EXE11_VERGEN_FLAGS+=-description $(EXE11_DESC)
endif
ifdef EXE11_COPYRIGHT
EXE11_VERGEN_FLAGS+=-copyright $(EXE11_COPYRIGHT)
endif
ifdef EXE11_VER
EXE11_VERGEN_FLAGS+=-v $(EXE11_VER)
endif
ifdef EXE11_COMMENT
EXE11_VERGEN_FLAGS+= -comment $(EXE11_COMMENT)
endif
ifdef EXE11_SPECIAL
EXE11_VERGEN_FLAGS+= -special $(EXE11_SPECIAL)
endif

EXE11_RC_VERGEN_FLAGS+=$(EXE11_VERGEN_FLAGS) -rc

ifdef EXE11_TARGET
$(OBJDIR)/auto_$(EXE11_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE11_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE11_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE11_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE11_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE11_TARGET).$(OBJ)

######################## EXE12_AUTO_VERSION ############################
EXE12_VERGEN_FLAGS+=-filename $(EXE12_TARGET)$(EXE)
ifdef EXE12_DESC
EXE12_VERGEN_FLAGS+=-description $(EXE12_DESC)
endif
ifdef EXE12_COPYRIGHT
EXE12_VERGEN_FLAGS+=-copyright $(EXE12_COPYRIGHT)
endif
ifdef EXE12_VER
EXE12_VERGEN_FLAGS+=-v $(EXE12_VER)
endif
ifdef EXE12_COMMENT
EXE12_VERGEN_FLAGS+= -comment $(EXE12_COMMENT)
endif
ifdef EXE12_SPECIAL
EXE12_VERGEN_FLAGS+= -special $(EXE12_SPECIAL)
endif

EXE12_RC_VERGEN_FLAGS+=$(EXE12_VERGEN_FLAGS) -rc

ifdef EXE12_TARGET
$(OBJDIR)/auto_$(EXE12_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE12_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE12_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE12_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE12_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE12_TARGET).$(OBJ)

######################## EXE13_AUTO_VERSION ############################
EXE13_VERGEN_FLAGS+=-filename $(EXE13_TARGET)$(EXE)
ifdef EXE13_DESC
EXE13_VERGEN_FLAGS+=-description $(EXE13_DESC)
endif
ifdef EXE13_COPYRIGHT
EXE13_VERGEN_FLAGS+=-copyright $(EXE13_COPYRIGHT)
endif
ifdef EXE13_VER
EXE13_VERGEN_FLAGS+=-v $(EXE13_VER)
endif
ifdef EXE13_COMMENT
EXE13_VERGEN_FLAGS+= -comment $(EXE13_COMMENT)
endif
ifdef EXE13_SPECIAL
EXE13_VERGEN_FLAGS+= -special $(EXE13_SPECIAL)
endif

EXE13_RC_VERGEN_FLAGS+=$(EXE13_VERGEN_FLAGS) -rc

ifdef EXE13_TARGET
$(OBJDIR)/auto_$(EXE13_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE13_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE13_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE13_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE13_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE13_TARGET).$(OBJ)

######################## EXE14_AUTO_VERSION ############################
EXE14_VERGEN_FLAGS+=-filename $(EXE14_TARGET)$(EXE)
ifdef EXE14_DESC
EXE14_VERGEN_FLAGS+=-description $(EXE14_DESC)
endif
ifdef EXE14_COPYRIGHT
EXE14_VERGEN_FLAGS+=-copyright $(EXE14_COPYRIGHT)
endif
ifdef EXE14_VER
EXE14_VERGEN_FLAGS+=-v $(EXE14_VER)
endif
ifdef EXE14_COMMENT
EXE14_VERGEN_FLAGS+= -comment $(EXE14_COMMENT)
endif
ifdef EXE14_SPECIAL
EXE14_VERGEN_FLAGS+= -special $(EXE14_SPECIAL)
endif

EXE14_RC_VERGEN_FLAGS+=$(EXE14_VERGEN_FLAGS) -rc

ifdef EXE14_TARGET
$(OBJDIR)/auto_$(EXE14_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE14_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE14_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE14_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE14_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE14_TARGET).$(OBJ)

######################## EXE15_AUTO_VERSION ############################
EXE15_VERGEN_FLAGS+=-filename $(EXE15_TARGET)$(EXE)
ifdef EXE15_DESC
EXE15_VERGEN_FLAGS+=-description $(EXE15_DESC)
endif
ifdef EXE15_COPYRIGHT
EXE15_VERGEN_FLAGS+=-copyright $(EXE15_COPYRIGHT)
endif
ifdef EXE15_VER
EXE15_VERGEN_FLAGS+=-v $(EXE15_VER)
endif
ifdef EXE15_COMMENT
EXE15_VERGEN_FLAGS+= -comment $(EXE15_COMMENT)
endif
ifdef EXE15_SPECIAL
EXE15_VERGEN_FLAGS+= -special $(EXE15_SPECIAL)
endif

EXE15_RC_VERGEN_FLAGS+=$(EXE15_VERGEN_FLAGS) -rc

ifdef EXE15_TARGET
$(OBJDIR)/auto_$(EXE15_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE15_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE15_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE15_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE15_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE15_TARGET).$(OBJ)

######################## EXE16_AUTO_VERSION ############################
EXE16_VERGEN_FLAGS+=-filename $(EXE16_TARGET)$(EXE)
ifdef EXE16_DESC
EXE16_VERGEN_FLAGS+=-description $(EXE16_DESC)
endif
ifdef EXE16_COPYRIGHT
EXE16_VERGEN_FLAGS+=-copyright $(EXE16_COPYRIGHT)
endif
ifdef EXE16_VER
EXE16_VERGEN_FLAGS+=-v $(EXE16_VER)
endif
ifdef EXE16_COMMENT
EXE16_VERGEN_FLAGS+= -comment $(EXE16_COMMENT)
endif
ifdef EXE16_SPECIAL
EXE16_VERGEN_FLAGS+= -special $(EXE16_SPECIAL)
endif

EXE16_RC_VERGEN_FLAGS+=$(EXE16_VERGEN_FLAGS) -rc

ifdef EXE16_TARGET
$(OBJDIR)/auto_$(EXE16_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE16_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE16_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE16_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE16_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE16_TARGET).$(OBJ)

######################## EXE17_AUTO_VERSION ############################
EXE17_VERGEN_FLAGS+=-filename $(EXE17_TARGET)$(EXE)
ifdef EXE17_DESC
EXE17_VERGEN_FLAGS+=-description $(EXE17_DESC)
endif
ifdef EXE17_COPYRIGHT
EXE17_VERGEN_FLAGS+=-copyright $(EXE17_COPYRIGHT)
endif
ifdef EXE17_VER
EXE17_VERGEN_FLAGS+=-v $(EXE17_VER)
endif
ifdef EXE17_COMMENT
EXE17_VERGEN_FLAGS+= -comment $(EXE17_COMMENT)
endif
ifdef EXE17_SPECIAL
EXE17_VERGEN_FLAGS+= -special $(EXE17_SPECIAL)
endif

EXE17_RC_VERGEN_FLAGS+=$(EXE17_VERGEN_FLAGS) -rc

ifdef EXE17_TARGET
$(OBJDIR)/auto_$(EXE17_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE17_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE17_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE17_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE17_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE17_TARGET).$(OBJ)

######################## EXE18_AUTO_VERSION ############################
EXE18_VERGEN_FLAGS+=-filename $(EXE18_TARGET)$(EXE)
ifdef EXE18_DESC
EXE18_VERGEN_FLAGS+=-description $(EXE18_DESC)
endif
ifdef EXE18_COPYRIGHT
EXE18_VERGEN_FLAGS+=-copyright $(EXE18_COPYRIGHT)
endif
ifdef EXE18_VER
EXE18_VERGEN_FLAGS+=-v $(EXE18_VER)
endif
ifdef EXE18_COMMENT
EXE18_VERGEN_FLAGS+= -comment $(EXE18_COMMENT)
endif
ifdef EXE18_SPECIAL
EXE18_VERGEN_FLAGS+= -special $(EXE18_SPECIAL)
endif

EXE18_RC_VERGEN_FLAGS+=$(EXE18_VERGEN_FLAGS) -rc

ifdef EXE18_TARGET
$(OBJDIR)/auto_$(EXE18_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE18_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE18_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE18_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE18_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE18_TARGET).$(OBJ)

######################## EXE19_AUTO_VERSION ############################
EXE19_VERGEN_FLAGS+=-filename $(EXE19_TARGET)$(EXE)
ifdef EXE19_DESC
EXE19_VERGEN_FLAGS+=-description $(EXE19_DESC)
endif
ifdef EXE19_COPYRIGHT
EXE19_VERGEN_FLAGS+=-copyright $(EXE19_COPYRIGHT)
endif
ifdef EXE19_VER
EXE19_VERGEN_FLAGS+=-v $(EXE19_VER)
endif
ifdef EXE19_COMMENT
EXE19_VERGEN_FLAGS+= -comment $(EXE19_COMMENT)
endif
ifdef EXE19_SPECIAL
EXE19_VERGEN_FLAGS+= -special $(EXE19_SPECIAL)
endif

EXE19_RC_VERGEN_FLAGS+=$(EXE19_VERGEN_FLAGS) -rc

ifdef EXE19_TARGET
$(OBJDIR)/auto_$(EXE19_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE19_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE19_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE19_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE19_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE19_TARGET).$(OBJ)

######################## EXE20_AUTO_VERSION ############################
EXE20_VERGEN_FLAGS+=-filename $(EXE20_TARGET)$(EXE)
ifdef EXE20_DESC
EXE20_VERGEN_FLAGS+=-description $(EXE20_DESC)
endif
ifdef EXE20_COPYRIGHT
EXE20_VERGEN_FLAGS+=-copyright $(EXE20_COPYRIGHT)
endif
ifdef EXE20_VER
EXE20_VERGEN_FLAGS+=-v $(EXE20_VER)
endif
ifdef EXE20_COMMENT
EXE20_VERGEN_FLAGS+= -comment $(EXE20_COMMENT)
endif
ifdef EXE20_SPECIAL
EXE20_VERGEN_FLAGS+= -special $(EXE20_SPECIAL)
endif

EXE20_RC_VERGEN_FLAGS+=$(EXE20_VERGEN_FLAGS) -rc

ifdef EXE20_TARGET
$(OBJDIR)/auto_$(EXE20_TARGET).cpp:
	$(VERGEN) $(VERGEN_FLAGS) $(EXE20_VERGEN_FLAGS) -o $@
ifndef BUILD_JAVA
headers:: $(OBJDIR)/auto_$(EXE20_TARGET).cpp
compile::$(OBJDIR)/auto_$(EXE20_TARGET).$(OBJ)
endif
endif

$(OBJDIR)/$(EXE20_TARGET)$(EXE): $(OBJDIR)/auto_$(EXE20_TARGET).$(OBJ)

endif #SKIP_AUTO_VERSION_INSERTION
