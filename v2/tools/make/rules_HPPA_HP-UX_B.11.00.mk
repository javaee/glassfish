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

# HPUX 11.00     rules

#
# AR[n]_TARGET, AR[n]_OBJS 
#

ifdef AR_TARGET
AR_OBJ_INT=$(addsuffix .$(OBJ),$(AR_OBJS))
REAL_AR_OBJS=$(addprefix $(OBJDIR)/,$(AR_OBJ_INT))
$(OBJDIR)/$(LIBPREFIX)$(AR_TARGET).$(STATIC_LIB_SUFFIX): $(REAL_AR_OBJS)
	$(RM) -f $@
	$(AR) -r $@ $(REAL_AR_OBJS) $(AR_NONPARSED_OBJS)
endif

ifdef AR1_TARGET
AR1_OBJ_INT=$(addsuffix .$(OBJ),$(AR1_OBJS))
REAL_AR1_OBJS=$(addprefix $(OBJDIR)/,$(AR1_OBJ_INT))
$(OBJDIR)/$(LIBPREFIX)$(AR1_TARGET).$(STATIC_LIB_SUFFIX): $(REAL_AR1_OBJS)
	$(RM) -f $@
	$(AR) -r $@ $(REAL_AR1_OBJS) $(AR1_NONPARSED_OBJS)
endif

ifdef AR2_TARGET
AR2_OBJ_INT=$(addsuffix .$(OBJ),$(AR2_OBJS))
REAL_AR2_OBJS=$(addprefix $(OBJDIR)/,$(AR2_OBJ_INT))
$(OBJDIR)/$(LIBPREFIX)$(AR2_TARGET).$(STATIC_LIB_SUFFIX): $(REAL_AR2_OBJS)
	$(RM) -f $@
	$(AR) -r $@ $(REAL_AR2_OBJS) $(AR2_NONPARSED_OBJS)
endif

ifdef AR3_TARGET
AR3_OBJ_INT=$(addsuffix .$(OBJ),$(AR3_OBJS))
REAL_AR3_OBJS=$(addprefix $(OBJDIR)/,$(AR3_OBJ_INT))
$(OBJDIR)/$(LIBPREFIX)$(AR3_TARGET).$(STATIC_LIB_SUFFIX): $(REAL_AR3_OBJS)
	$(RM) -f $@
	$(AR) -r $@ $(REAL_AR3_OBJS) $(AR3_NONPARSED_OBJS)
endif

ifdef BSC_TARGET
$(BSC_TARGET): ; \
	$(ECHO) The $@ file is for NT only. > $@
endif

#
# The following block is used for precompiled headers on HP-UX.
# To use precompiled headers we have to do two phases:
#    - first create a precompiled header using a .cpp file and 
#          +hdr_create compiler option.
#    - All files which need to use the precompiled header should
#          use +hdr_use option.
#
# For the Enterpries server this is implemented as follows:
#     A global precompiled header is created using just "#include corba.h"
#     This will be used as the default precompiled header. Each directory
#     can override this by introducing a precomphdr.cpp file in that directory.
#     This file will contain all the heavy-duty header files.
#     Since the support directory does not need corba.h and does not have
#     the knowhow to compile it, we use a special flag HP_NO_CORBA_PCH to
#     avoid using precompiled headers.
$(OBJDIR)/$(PCH_FILE).pch:$(PCH_FILE).cpp $(GLOBAL_PCH_FILE)
	@$(MAKE_OBJDIR)
	if test ! -f $(OBJDIR)/$(PCH_FILE).pch; then \
		if test -f $(PCH_FILE).cpp; then \
			echo "//" > $(OBJDIR)/tempfile.cpp; \
			$(CC) +DAportable +hdr_create $@  -c \
			-o $(OBJDIR)/tempfile.o $(OBJDIR)/tempfile.cpp; \
			rm -f $(OBJDIR)/tempfile.* ; \
			$(PRECC) $(CC) $(CC_FLAGS) $($<_CC_FLAGS) -c \
			\
			$(CC_DASH_O)$(OBJDIR)/$(PCH_FILE).o \
			+hdr_create $@ \
			\
			$<; \
		else \
			rm -f $@; \
			ln -s ../$(GLOBAL_PCH_FILE) $@; \
		fi; \
	fi

$(PCH_FILE).cpp:  

$(GLOBAL_PCH_FILE): 
	@$(MAKE_OBJDIR)
	echo "//" > $(OBJDIR)/tempfile.cpp
	$(CC) +DAportable +hdr_create $(OBJDIR)/$(PCH_FILE).pch  \
		-c -o $(OBJDIR)/tempfile.o \
		$(OBJDIR)/tempfile.cpp
	rm -f $(OBJDIR)/tempfile.*
	echo "#include \"corba.h\"" > $(OBJDIR)/tmpfile.cpp
	$(PRECC) $(CC) $(CC_FLAGS) -c \
		\
		$(CC_DASH_O)$(OBJDIR)/tmpfile.o \
		+hdr_create $@ \
		\
		$(OBJDIR)/tmpfile.cpp
	rm -f $(OBJDIR)/tmpfile.*
	rm -f $(OBJDIR)/$(PCH_FILE).pch


EXPORT__LIBS=$(EXPORT_LIBRARIES) $(EXPORT_DYNAMIC_LIBRARIES)


