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

# User customizable variable definitions only

# The BUILD_ROOT variable must be defined
BUILD_ROOT=$(GLASSFISH_MAVEN)/../appserv-native
# include bootstrap variable definitions
include $(GLASSFISH_MAVEN)/make/defines.mk
include $(GLASSFISH_MAVEN)/make/defines-ee.mk
include $(BUILD_ROOT)/make/defines.mk

# The PUBLISH_ROOT variable must be defined
PUBLISH_ROOT=$(GLASSFISH_HOME)/..

# User customizable rules

NSSUTIL_DIR=src/cpp/nssutil
LOADMETRICS_DIR=src/cpp/loadmetrics

ifeq ($(OS_ARCH), WINNT)

post_build:: build_all publish_nssutil publish_loadmetrics publish_nssutil_win_glassfish publish_loadmetrics_win_glassfish publish_ee_launcher_win_glassfish

build_all::
	$(STD_GMAKE_CMD)
endif
ifeq ($(OS_ARCH), SunOS)
#for Solaris
post_build:: build_all publish_nssutil publish_loadmetrics publish_nssutil_glassfish publish_loadmetrics_glassfish publish_ee_launcher_glassfish

build_all::
	$(JAVA_HOME)/bin/java -d64 -version 1>&2 2> /dev/null; \
	if [ $$? = 0 ] ; \
	then \
		$(STD_GMAKE_CMD) COMPILE64=true ; \
	fi ;
	$(STD_GMAKE_CMD)
endif
ifeq ($(OS_ARCH), Linux)
#for Linux

post_build:: build_all publish_nssutil publish_loadmetrics publish_nssutil_glassfish publish_loadmetrics_glassfish publish_ee_launcher_glassfish

build_all::
	$(STD_GMAKE_CMD)

endif 

publish_nssutil::
	$(AT)if test -d ./$(NSSUTIL_DIR)/build; then \
		if test ! -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
			$(ECHO) "Creating ... $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)"; \
			$(MKDIR) -p $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		fi; \
		$(CP) -r ./$(NSSUTIL_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
	fi

publish_loadmetrics::
	$(AT)if test -d ./$(LOADMETRICS_DIR)/build; then \
                if test ! -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
                        $(ECHO) "Creating ... $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)"; \
                        $(MKDIR) -p $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
                fi; \
                $(CP) -r ./$(LOADMETRICS_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
	fi
                                                                                                                                               
publish_nssutil_win_glassfish::
	$(ECHO) "Copy  *.dll to $(GLASSFISH_HOME)/lib"; \
	$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.dll $(GLASSFISH_HOME)/lib;

publish_loadmetrics_win_glassfish::
	$(ECHO) "Copy  *.dll to $(GLASSFISH_HOME)/lib"; \
        $(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.dll $(GLASSFISH_HOME)/lib;

publish_nssutil_glassfish::
	$(ECHO) "Copy  *.so to $(GLASSFISH_HOME)/lib"; \
	$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.so $(GLASSFISH_HOME)/lib;
	if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64); then \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64)/*.so $(GLASSFISH_HOME)/lib/$(OBJDIR64); \
	fi ; 

publish_loadmetrics_glassfish::
	$(ECHO) "Copy  *.so to $(GLASSFISH_HOME)/lib"; \
        $(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.so $(GLASSFISH_HOME)/lib;
	if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64); then \
                $(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64)/*.so $(GLASSFISH_HOME)/lib/$(OBJDIR64); \
        fi ;

publish_ee_launcher_glassfish:
	$(AT)if test -f $(GLASSFISH_HOME)/lib/appserv; then \
		$(ECHO) "Making EE Launcher copies from appserv to appservAgent appservDAS"; \
		$(CP) $(GLASSFISH_HOME)/lib/appserv $(GLASSFISH_HOME)/lib/appservAgent; \
		$(CP) $(GLASSFISH_HOME)/lib/appserv $(GLASSFISH_HOME)/lib/appservDAS; \
	fi

publish_ee_launcher_win_glassfish:
	$(AT)if test -f $(GLASSFISH_HOME)/lib/appserv.exe; then \
		$(ECHO) "Making EE Launcher copies from appserv.exe to appservAgent.exe appservDAS.exe"; \
		$(CP) $(GLASSFISH_HOME)/lib/appserv.exe $(GLASSFISH_HOME)/lib/appservAgent.exe; \
		$(CP) $(GLASSFISH_HOME)/lib/appserv.exe $(GLASSFISH_HOME)/lib/appservDAS.exe; \
	fi

	$(AT)if test -f $(GLASSFISH_HOME)/lib/appservService.exe; then \
		$(ECHO) "Making EE Launcher copies from appservService.exe to appservAgentService.exe"; \
		$(CP) $(GLASSFISH_HOME)/lib/appservService.exe $(GLASSFISH_HOME)/lib/appservAgentService.exe; \
	fi

clean::
	$(STD_GMAKE_CMD) clobber
