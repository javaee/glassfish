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
BUILD_ROOT=.
# include bootstrap variable definitions
include $(GLASSFISH_MAVEN)/make/defines.mk
include $(BUILD_ROOT)/make/defines.mk
include $(BUILD_ROOT)/project.properties

# User customizable rules
ifeq ($(OS_ARCH), SunOS)

SOLARIS_DIR=src/cpp/solaris
SYSLOG_DIR=src/cpp/as8syslog
CLI_DIR=src/cpp/cli
LAUNCHER_DIR=src/cpp/launcher
JVMINFO_DIR=src/cpp/jvminfo

post_build:: build_all publish_solaris publish_syslog publish_jvminfo publish_cli publish_cli_glassfish publish_launcher publish_launcher_glassfish

build_all::
	$(JAVA_HOME)/bin/java -d64 -version 1>&2 2> /dev/null; \
	if [ $$? = 0 ] ; \
	then \
	$(STD_GMAKE_CMD) COMPILE64=true ; \
	fi ;
	$(STD_GMAKE_CMD)

publish_solaris::
	$(AT)if test -d $(BUILD_ROOT)/$(SOLARIS_DIR)/build; then \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
			$(CP) -R $(BUILD_ROOT)/$(SOLARIS_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		else \
			$(ECHO) "Creating ... $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)"; \
			$(MKDIR) -p $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
			$(ECHO) "Copy ... $(BUILD_ROOT)/$(SOLARIS_DIR)/build/*"; \
			$(CP) -R $(BUILD_ROOT)/$(SOLARIS_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		fi; \
		$(ECHO) "Copy *.so to $(GLASSFISH_HOME)/lib"; \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.so $(GLASSFISH_HOME)/lib; \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64); then \
			$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64)/*.so $(GLASSFISH_HOME)/lib/$(OBJDIR64); \
		fi ; \
	fi
	echo OBJDIR64 : $(OBJDIR64)


publish_syslog::
	$(AT)if test -d $(BUILD_ROOT)/$(SYSLOG_DIR)/build; then \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
			$(CP) -R $(BUILD_ROOT)/$(SYSLOG_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		else \
			$(ECHO) "Creating ... $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)"; \
			$(MKDIR) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
			$(CP) -R $(BUILD_ROOT)/$(SYSLOG_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		fi; \
		$(ECHO) "Copy *.so to $(GLASSFISH_HOME)/lib"; \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.so $(GLASSFISH_HOME)/lib; \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64); then \
			$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64)/*.so $(GLASSFISH_HOME)/lib/$(OBJDIR64); \
		fi ; \
	fi

publish_jvminfo::
	$(AT)if test -d $(BUILD_ROOT)/$(JVMINFO_DIR)/build; then \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
			$(CP) -R $(BUILD_ROOT)/$(JVMINFO_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		else \
			$(ECHO) "Creating ... $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)"; \
			$(MKDIR) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
			$(CP) -R $(BUILD_ROOT)/$(JVMINFO_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		fi; \
		$(ECHO) "Copy *.so to $(GLASSFISH_HOME)/lib"; \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.so $(GLASSFISH_HOME)/lib; \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64); then \
			$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64)/*.so $(GLASSFISH_HOME)/lib/$(OBJDIR64); \
		fi ; \
	fi
endif # ifeq SunOS

ifeq ($(OS_ARCH), WINNT)
CLI_DIR=src/cpp/cli
LAUNCHER_DIR=src/cpp/launcher
JVMINFO_DIR=src/cpp/jvminfo

post_build:: build_cli publish_jvminfo publish_cli publish_cli_win_glassfish publish_launcher publish_launcher_win_glassfish
build_cli::
	$(STD_GMAKE_CMD)
endif #ifeq Windows_NT

ifeq ($(OS_ARCH), Darwin)
CLI_DIR=src/cpp/cli
LAUNCHER_DIR=src/cpp/launcher
JVMINFO_DIR=src/cpp/jvminfo

post_build:: build_cli publish_jvminfo publish_cli publish_cli_darwin_glassfish publish_launcher publish_launcher_glassfish
build_cli::
	$(STD_GMAKE_CMD)
endif #ifeq Darwin

ifeq ($(OS_ARCH), Linux)
SYSLOG_DIR=src/cpp/as8syslog
CLI_DIR=src/cpp/cli
LAUNCHER_DIR=src/cpp/launcher
JVMINFO_DIR=src/cpp/jvminfo

post_build:: build_syslog_cli publish_syslog publish_jvminfo publish_cli publish_cli_glassfish publish_launcher publish_launcher_glassfish
build_syslog_cli::
	$(STD_GMAKE_CMD)
publish_syslog::
	$(AT)if test -d $(BUILD_ROOT)/$(SYSLOG_DIR)/build; then \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
		$(CP) -R $(BUILD_ROOT)/$(SYSLOG_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
	else \
		$(ECHO) "Creating ... $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)"; \
		$(MKDIR) -p $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		$(CP) -R $(BUILD_ROOT)/$(SYSLOG_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		fi; \
	fi
endif #ifeq Linux


publish_jvminfo::
	$(AT)if test -d $(BUILD_ROOT)/$(JVMINFO_DIR)/build; then \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
		$(CP) -R $(BUILD_ROOT)/$(JVMINFO_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
	else \
		$(ECHO) "Creating ... $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)"; \
		$(MKDIR) -p $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		$(CP) -R $(BUILD_ROOT)/$(JVMINFO_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		fi; \
	fi

publish_cli::
	$(AT)if test -d $(BUILD_ROOT)/$(CLI_DIR)/build; then \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
			$(CP) -R $(BUILD_ROOT)/$(CLI_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		else \
			$(ECHO) "Creating ... $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)"; \
			$(MKDIR) -p $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
			$(CP) -R $(BUILD_ROOT)/$(CLI_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		fi; \
	fi

publish_cli_win_glassfish::
	$(ECHO) "Copy  *.dll to $(GLASSFISH_HOME)/lib"; \
	$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.dll $(GLASSFISH_HOME)/lib;

publish_cli_darwin_glassfish::
	$(ECHO) "Copy  *.jnilib to $(GLASSFISH_HOME)/lib"; \
	$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.jnilib $(GLASSFISH_HOME)/lib; 

publish_cli_glassfish::
	$(ECHO) "Copy  *.so to $(GLASSFISH_HOME)/lib"; \
	$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/*.so $(GLASSFISH_HOME)/lib;
	if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64); then \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64)/*.so $(GLASSFISH_HOME)/lib/$(OBJDIR64); \
	fi ; \


publish_launcher::
	$(AT)if test -d $(BUILD_ROOT)/$(LAUNCHER_DIR)/build; then \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
			$(CP) -R $(BUILD_ROOT)/$(LAUNCHER_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		else \
			$(ECHO) "Creating ... $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)"; \
			$(MKDIR) -p $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
			$(CP) -R $(BUILD_ROOT)/$(LAUNCHER_DIR)/build/* $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); \
		fi; \
	fi

publish_launcher_glassfish:
	$(AT)if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
		$(ECHO) "Copy appserv* to $(GLASSFISH_HOME)/lib"; \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/appserv $(GLASSFISH_HOME)/lib; \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/appservLauncher $(GLASSFISH_HOME)/lib; \
		if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64); then \
			$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64)/appserv $(GLASSFISH_HOME)/lib/$(OBJDIR64); \
			$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/$(OBJDIR64)/appservLauncher $(GLASSFISH_HOME)/lib/$(OBJDIR64); \
		fi ; \
	fi

publish_launcher_win_glassfish:
	$(AT)if test -d $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME); then \
		$(ECHO) "Copy appserv* to $(GLASSFISH_HOME)/lib"; \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/appserv.exe $(GLASSFISH_HOME)/lib; \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/appservLauncher.exe $(GLASSFISH_HOME)/lib; \
		$(CP) $(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(COMPONENT_NAME)/appservService.exe $(GLASSFISH_HOME)/lib; \
	fi
