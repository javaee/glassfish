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

# X86_NT_4.0 rules

#
# AR[n]_TARGET, AR[n]_OBJS 
#
ZIPFLAGS = -r

ifdef AR_TARGET
AR_OBJ_INT=$(addsuffix .$(OBJ),$(AR_OBJS))
REAL_AR_OBJS=$(addprefix $(OBJDIR)/, $(AR_OBJ_INT))
$(OBJDIR)/$(LIBPREFIX)$(AR_TARGET).$(STATIC_LIB_SUFFIX): $(REAL_AR_OBJS)
	$(RM) -f $@
	$(MSVC_LIB) /OUT:$@ $(AR_NONPARSED_OBJS) \
		$^
endif

ifdef AR1_TARGET
AR1_OBJ_INT=$(addsuffix .$(OBJ),$(AR1_OBJS))
REAL_AR1_OBJS=$(addprefix $(OBJDIR)/, $(AR1_OBJ_INT))
$(OBJDIR)/$(LIBPREFIX)$(AR1_TARGET).$(STATIC_LIB_SUFFIX): $(REAL_AR1_OBJS)
	$(RM) -f $@
	$(MSVC_LIB) /OUT:$@ $(AR1_NONPARSED_OBJS) \
		$^
endif

ifdef AR2_TARGET
AR2_OBJ_INT=$(addsuffix .$(OBJ),$(AR2_OBJS))
REAL_AR2_OBJS=$(addprefix $(OBJDIR)/, $(AR2_OBJ_INT))
$(OBJDIR)/$(LIBPREFIX)$(AR2_TARGET).$(STATIC_LIB_SUFFIX): $(REAL_AR2_OBJS)
	$(RM) -f $@
	$(MSVC_LIB) /OUT:$@ $(AR2_NONPARSED_OBJS) \
		$^
endif

ifdef AR3_TARGET
AR3_OBJ_INT=$(addsuffix .$(OBJ),$(AR3_OBJS))
REAL_AR3_OBJS=$(addprefix $(OBJDIR)/, $(AR3_OBJ_INT))
$(OBJDIR)/$(LIBPREFIX)$(AR3_TARGET).$(STATIC_LIB_SUFFIX): $(REAL_AR3_OBJS)
	$(RM) -f $@
	$(MSVC_LIB) /OUT:$@ $(AR3_NONPARSED_OBJS) \
		$^
endif

ifdef AR4_TARGET
AR4_OBJ_INT=$(addsuffix .$(OBJ),$(AR4_OBJS))
REAL_AR4_OBJS=$(addprefix $(OBJDIR)/, $(AR4_OBJ_INT))
$(OBJDIR)/$(LIBPREFIX)$(AR4_TARGET).$(STATIC_LIB_SUFFIX): $(REAL_AR4_OBJS)
	$(RM) -f $@
	$(MSVC_LIB) /OUT:$@ $(AR4_NONPARSED_OBJS) \
		$^
endif

ifndef NO_STD_OBJDIR_RES_RULE
$(OBJDIR)/%.rc:%.mc
	@$(MAKE_OBJDIR) 
	$(MC) $(MC_FLAGS) $^
	
$(OBJDIR)/%.res:%.rc
	@$(MAKE_OBJDIR) 
	$(RC) $(RC_FLAGS) \
                \
                $(CC_DASH_O)$@ \
                \
                $^
endif #NO_STD_OBJDIR_RES_RULE

ifdef BROWSE
ifdef BSC_TARGET
_BSC_OBJS=$(addprefix $(OBJDIR)/, $(addsuffix .sbr, $(BSC_OBJS)))
$(OBJDIR)/$(BSC_TARGET): $(_BSC_OBJS)
	$(BSCMAKE) $(BSC_FLAGS) /o $@ \
		$(_BSC_OBJS)
endif
else
ifdef BSC_TARGET
$(OBJDIR)/$(BSC_TARGET):
	@$(TOUCH) $@
endif
endif

ifdef SB_INIT
$(OBJDIR)/$(SB_INIT): ; \
	@$(ECHO) The .sbinit file is for Solaris only. > $@
endif

EXPORT__LIBS=$(EXPORT_LIBRARIES) \
	$(EXPORT_DYNAMIC_LIBRARIES:$(DYNAMIC_LIB_SUFFIX)=$(STATIC_LIB_SUFFIX))
EXPORT__DYN_LIBS= $(EXPORT_DYNAMIC_LIBRARIES)

EXPORT__EARLY_LIBS=$(EXPORT_EARLY_LIBRARIES) \
		   $(EXPORT_EARLY_DYNAMIC_LIBRARIES:.dll=.lib)
EXPORT__EARLY_DLLS=$(EXPORT_EARLY_DYNAMIC_LIBRARIES)

ifdef GENERATE_MAP_FILES
DLL_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(DLL_TARGET).map
DLL1_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(DLL1_TARGET).map
DLL2_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(DLL2_TARGET).map
DLL3_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(DLL3_TARGET).map
DLL4_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(DLL4_TARGET).map
EXE_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE_TARGET).map
EXE1_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE1_TARGET).map
EXE2_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE2_TARGET).map
EXE3_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE3_TARGET).map
EXE4_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE4_TARGET).map
EXE5_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE5_TARGET).map
EXE6_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE6_TARGET).map
EXE7_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE7_TARGET).map
EXE8_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE8_TARGET).map
EXE9_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE9_TARGET).map
EXE10_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE10_TARGET).map
EXE11_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE11_TARGET).map
EXE12_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE12_TARGET).map
EXE13_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE13_TARGET).map
EXE14_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE14_TARGET).map
EXE15_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE15_TARGET).map
EXE16_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE16_TARGET).map
EXE17_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE17_TARGET).map
EXE18_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE18_TARGET).map
EXE19_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE19_TARGET).map
EXE20_EXTRA+=$(CREATE_MSVC_MAP_FILES)$(OBJDIR)/$(EXE20_TARGET).map
endif # GENERATE_MAP_FILES

ifdef GENERATE_PDB_FILES
DLL_REAL_LIBS+=-pdb:$(OBJDIR)/$(DLL_TARGET).pdb
DLL1_REAL_LIBS+=-pdb:$(OBJDIR)/$(DLL1_TARGET).pdb
DLL2_REAL_LIBS+=-pdb:$(OBJDIR)/$(DLL2_TARGET).pdb
DLL3_REAL_LIBS+=-pdb:$(OBJDIR)/$(DLL3_TARGET).pdb
DLL4_REAL_LIBS+=-pdb:$(OBJDIR)/$(DLL4_TARGET).pdb
EXE_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE_TARGET).pdb
EXE1_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE1_TARGET).pdb
EXE2_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE2_TARGET).pdb
EXE3_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE3_TARGET).pdb
EXE4_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE4_TARGET).pdb
EXE5_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE5_TARGET).pdb
EXE6_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE6_TARGET).pdb
EXE7_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE7_TARGET).pdb
EXE8_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE8_TARGET).pdb
EXE9_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE9_TARGET).pdb
EXE10_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE10_TARGET).pdb
EXE11_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE11_TARGET).pdb
EXE12_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE12_TARGET).pdb
EXE13_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE13_TARGET).pdb
EXE14_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE14_TARGET).pdb
EXE15_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE15_TARGET).pdb
EXE16_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE16_TARGET).pdb
EXE17_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE17_TARGET).pdb
EXE18_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE18_TARGET).pdb
EXE19_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE19_TARGET).pdb
EXE20_REAL_LIBS+=-pdb:$(OBJDIR)/$(EXE20_TARGET).pdb
else # GENERATE_PDB_FILES
DLL_REAL_LIBS+=-pdb:none
DLL1_REAL_LIBS+=-pdb:none
DLL2_REAL_LIBS+=-pdb:none
DLL3_REAL_LIBS+=-pdb:none
DLL4_REAL_LIBS+=-pdb:none
EXE_REAL_LIBS+=-pdb:none
EXE1_REAL_LIBS+=-pdb:none
EXE2_REAL_LIBS+=-pdb:none
EXE3_REAL_LIBS+=-pdb:none
EXE4_REAL_LIBS+=-pdb:none
EXE5_REAL_LIBS+=-pdb:none
EXE6_REAL_LIBS+=-pdb:none
EXE7_REAL_LIBS+=-pdb:none
EXE8_REAL_LIBS+=-pdb:none
EXE9_REAL_LIBS+=-pdb:none
EXE10_REAL_LIBS+=-pdb:none
EXE11_REAL_LIBS+=-pdb:none
EXE12_REAL_LIBS+=-pdb:none
EXE13_REAL_LIBS+=-pdb:none
EXE14_REAL_LIBS+=-pdb:none
EXE15_REAL_LIBS+=-pdb:none
EXE16_REAL_LIBS+=-pdb:none
EXE17_REAL_LIBS+=-pdb:none
EXE18_REAL_LIBS+=-pdb:none
EXE19_REAL_LIBS+=-pdb:none
EXE20_REAL_LIBS+=-pdb:none
endif # GENERATE_PDB_FILES

MAKE_RC = $(BUILD_ROOT)/tools/MakeRC/MakeRC.exe

ifdef EXE_TARGET
ifdef EXE_RES
_EXE_RES=$(OBJDIR)/$(EXE_RES).res
headers:: $(EXE_RES).rc
compile:: $(OBJDIR)/$(EXE_RES).res
endif # EXE_RES
endif # EXE_TARGET

ifndef SKIP_AUTO_RESOURCE


ifdef DLL_RES
DLL_RC_VERGEN_FLAGS+=-rcInclude ../$(DLL_RES).rc
$(OBJDIR)/auto_$(DLL_TARGET).res : $(DLL_RES).rc
endif

ifdef DLL1_RES
DLL1_RC_VERGEN_FLAGS+=-rcInclude ../$(DLL1_RES).rc
$(OBJDIR)/auto_$(DLL1_TARGET).res : $(DLL1_RES).rc
endif

ifdef DLL2_RES
DLL2RC_VERGEN_FLAGS+=-rcInclude ../$(DLL2_RES).rc
$(OBJDIR)/auto_$(DLL2_TARGET).res : $(DLL2_RES).rc
endif

ifdef DLL3_RES
DLL3RC_VERGEN_FLAGS+=-rcInclude ../$(DLL3_RES).rc
$(OBJDIR)/auto_$(DLL3_TARGET).res : $(DLL3_RES).rc
endif

ifdef DLL4_RES
DLL4RC_VERGEN_FLAGS+=-rcInclude ../$(DLL4_RES).rc
$(OBJDIR)/auto_$(DLL4_TARGET).res : $(DLL4_RES).rc
endif

ifdef DLL_TARGET
$(OBJDIR)/auto_$(DLL_TARGET).rc:
	$(VERGEN) $(DLL_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(DLL_TARGET).rc
compile:: $(OBJDIR)/auto_$(DLL_TARGET).res
endif # DLL_TARGET

ifdef DLL1_TARGET
$(OBJDIR)/auto_$(DLL1_TARGET).rc:
	$(VERGEN) $(DLL1_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(DLL1_TARGET).rc
compile:: $(OBJDIR)/auto_$(DLL1_TARGET).res
endif # DLL1_TARGET

ifdef DLL2_TARGET
$(OBJDIR)/auto_$(DLL2_TARGET).rc:
	$(VERGEN) $(DLL2_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(DLL2_TARGET).rc
compile:: $(OBJDIR)/auto_$(DLL2_TARGET).res
endif # DLL2_TARGET

ifdef DLL3_TARGET
$(OBJDIR)/auto_$(DLL3_TARGET).rc:
	$(VERGEN) $(DLL3_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(DLL3_TARGET).rc
compile:: $(OBJDIR)/auto_$(DLL3_TARGET).res
endif # DLL3_TARGET

ifdef DLL4_TARGET
$(OBJDIR)/auto_$(DLL4_TARGET).rc:
	$(VERGEN) $(DLL4_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(DLL4_TARGET).rc
compile:: $(OBJDIR)/auto_$(DLL4_TARGET).res
endif # DLL4_TARGET

ifdef EXE_TARGET
$(OBJDIR)/auto_$(EXE_TARGET).rc:
	$(VERGEN) $(EXE_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE_TARGET).res
endif # EXE_TARGET

ifdef EXE1_TARGET
$(OBJDIR)/auto_$(EXE1_TARGET).rc:
	$(VERGEN) $(EXE1_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE1_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE1_TARGET).res
endif # EXE1_TARGET

ifdef EXE2_TARGET
$(OBJDIR)/auto_$(EXE2_TARGET).rc:
	$(VERGEN) $(EXE2_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE2_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE2_TARGET).res
endif # EXE2_TARGET

ifdef EXE3_TARGET
$(OBJDIR)/auto_$(EXE3_TARGET).rc:
	$(VERGEN) $(EXE3_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE3_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE3_TARGET).res
endif # EXE3_TARGTE

ifdef EXE4_TARGET
$(OBJDIR)/auto_$(EXE4_TARGET).rc:
	$(VERGEN) $(EXE4_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE4_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE4_TARGET).res
endif # EXE4_TARGET

ifdef EXE5_TARGET
$(OBJDIR)/auto_$(EXE5_TARGET).rc:
	$(VERGEN) $(EXE5_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE5_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE5_TARGET).res
endif # EXE5_TARGET

ifdef EXE6_TARGET
$(OBJDIR)/auto_$(EXE6_TARGET).rc:
	$(VERGEN) $(EXE6_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE6_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE6_TARGET).res
endif # EXE6_TARGET

ifdef EXE7_TARGET
$(OBJDIR)/auto_$(EXE7_TARGET).rc:
	$(VERGEN) $(EXE7_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE7_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE7_TARGET).res
endif # EXE7_TARGET

ifdef EXE8_TARGET
$(OBJDIR)/auto_$(EXE8_TARGET).rc:
	$(VERGEN) $(EXE8_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE8_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE8_TARGET).res
endif # EXE8_TARGET

ifdef EXE9_TARGET
$(OBJDIR)/auto_$(EXE9_TARGET).rc:
	$(VERGEN) $(EXE9_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE9_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE9_TARGET).res
endif # EXE9_TARGET

ifdef EXE10_TARGET
$(OBJDIR)/auto_$(EXE10_TARGET).rc:
	$(VERGEN) $(EXE10_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE10_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE10_TARGET).res
endif # EXE10_TARGET

ifdef EXE11_TARGET
$(OBJDIR)/auto_$(EXE11_TARGET).rc:
	$(VERGEN) $(EXE11_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE11_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE11_TARGET).res
endif # EXE11_TARGET

ifdef EXE12_TARGET
$(OBJDIR)/auto_$(EXE12_TARGET).rc:
	$(VERGEN) $(EXE12_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE12_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE12_TARGET).res
endif # EXE12_TARGET

ifdef EXE13_TARGET
$(OBJDIR)/auto_$(EXE13_TARGET).rc:
	$(VERGEN) $(EXE13_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE13_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE13_TARGET).res
endif # EXE13_TARGET

ifdef EXE14_TARGET
$(OBJDIR)/auto_$(EXE14_TARGET).rc:
	$(VERGEN) $(EXE14_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE14_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE14_TARGET).res
endif # EXE14_TARGET

ifdef EXE15_TARGET
$(OBJDIR)/auto_$(EXE15_TARGET).rc:
	$(VERGEN) $(EXE15_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE15_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE15_TARGET).res
endif # EXE15_TARGET

ifdef EXE16_TARGET
$(OBJDIR)/auto_$(EXE16_TARGET).rc:
	$(VERGEN) $(EXE16_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE16_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE16_TARGET).res
endif # EXE16_TARGET

ifdef EXE17_TARGET
$(OBJDIR)/auto_$(EXE17_TARGET).rc:
	$(VERGEN) $(EXE17_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE17_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE17_TARGET).res
endif # EXE17_TARGET

ifdef EXE18_TARGET
$(OBJDIR)/auto_$(EXE18_TARGET).rc:
	$(VERGEN) $(EXE18_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE18_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE18_TARGET).res
endif # EXE18_TARGET

ifdef EXE19_TARGET
$(OBJDIR)/auto_$(EXE19_TARGET).rc:
	$(VERGEN) $(EXE19_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE19_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE19_TARGET).res
endif # EXE19_TARGET

ifdef EXE20_TARGET
$(OBJDIR)/auto_$(EXE20_TARGET).rc:
	$(VERGEN) $(EXE20_RC_VERGEN_FLAGS) -o $@
headers:: $(OBJDIR)/auto_$(EXE20_TARGET).rc
compile:: $(OBJDIR)/auto_$(EXE20_TARGET).res
endif # EXE20_TARGET

endif # SKIP_AUTO_RESOURCE

$(OBJDIR)/%.res : $(OBJDIR)/%.rc
	$(RC) $(RC_FLAGS) $($*_RC_FLAGS) /fo$@ $<

$(OBJDIR)/%.sbr : $(OBJDIR)/%.$(OBJ)
