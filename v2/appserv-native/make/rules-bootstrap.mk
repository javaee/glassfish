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

.PHONY: all tip rel

all:: build

tip:
	$(MAKE) MAKEFLAGS='$(MAKEFLAGS)' tip-all

tip-%:
	$(MAKE) MAKEFLAGS='$(MAKEFLAGS)' $*

rel:
	$(MAKE) MAKEFLAGS='$(MAKEFLAGS)' rel-all

rel-%:
	$(MAKE) USE_CVS_RELTAG=1 MAKEFLAGS='$(MAKEFLAGS)' $*

#-----------------------------------------------------------------------------
.PHONY: opt

opt:
	$(MAKE) BUILD_VARIANT=OPTIMIZED MAKEFLAGS='$(MAKEFLAGS)'

%-opt:
	$(MAKE) BUILD_VARIANT=OPTIMIZED MAKEFLAGS='$(MAKEFLAGS)' $*

#-----------------------------------------------------------------------------
.PHONY: build pre_build_check pre_build build_dependents build_component post_build

#
# Use a variable other than NO_RECURSION for suppressing dependency builds
# in a sub-component of one that has recursion disabled.
#
ifeq ($(DISABLE_RECURSION),)
build:: pre_build_check pre_build build_dependents build_component post_build
else
build:: pre_build_check pre_build build_component post_build
endif
	$(AT)$(BS_ECHO) "[$(COMPONENT_NAME)][source] build complete"

pre_build::
ifdef CALL_PREBUILD_TARGET
ifndef NO_ANT
	$(BS_MAKE) $(ANT_ENV) ANT_TARGETS=$@ antit
else
	$(AT)if test -f $(GMAKE_BUILD_FILE) ; then \
		$(STD_GMAKE_CMD) $@; \
	else \
		$(BS_ECHO) "WARNING: [$(COMPONENT)][source] $(GMAKE_BUILD_FILE) not found."; \
	fi 
endif
endif

# Check if $(COMPONENT_PUBLISH_DIR) contains a previously pulled
# binary form of the component. If so, then remove the entire directory
# before building. The Version file will contain the value "Source" 
# indicating that the $(COMPONENT_PUBLISH_DIR) was built from source.
pre_build_check:
	$(AT)$(BS_ECHO) "Building $(COMPONENT_NAME): $(BUILD_VARIANT) $(SECURITY_POLICY)"
	$(AT)if test -f "$(COMPONENT_PUBLISH_DIR)/$(VERSION_FILE)" ; then \
		$(BS_MAKE) check_if_built_from_source; \
	fi

# If the Version file does not contain the magic word "Source", then warn
# the user and blow away the contents of $(COMPONENT_PUBLISH_DIR)
check_if_built_from_source:
		$(AT)if test '$(shell cat $(COMPONENT_PUBLISH_DIR)/$(VERSION_FILE)| $(TR) -d "\r\n")' != "Source" ; then \
			$(BS_ECHO) "WARNING: Removing existing contents of $(COMPONENT_PUBLISH_DIR) as it contains a binary version [$(shell cat $(COMPONENT_PUBLISH_DIR)/$(VERSION_FILE))] of the component that is being built from source"; \
			$(RM) -rf $(COMPONENT_PUBLISH_DIR); \
		fi


build_dependents: $(addprefix build-,$(source_dependencies))
	$(AT)if test ! -z "$(strip $(source_dependencies))" ; then \
		$(BS_ECHO) "[$(COMPONENT_NAME)] source dependencies have been built"; \
	else \
		$(BS_ECHO) "[$(COMPONENT_NAME)] has no source dependencies"; \
	fi

build-%:
	$(AT)if test ! -z "$(filter $*, $(binary_dependencies))"; then \
		$(BS_ECHO) "Cannot build binary dependencies. [$*] has been specified as a binary dependency in $(DEPENDENCY_FILE)"; \
		exit 255; \
	fi
	$(AT)$(BS_MAKE) NAME=$* ALIAS=$(subst -,_,$*) build_dependent_component

build_dependent_component:
	$(AT)if test -z "$(NAME)" -o -z "$(ALIAS)" ; then \
		$(BS_ECHO) "The [$@] target should not be invoked directly"; \
		exit 255; \
	fi
	$(AT)if test ! -d "$(WORKSPACE_DIR)/$(NAME)"; then \
		$(BS_ECHO) "WARNING: [$(NAME)][source] has not been checked out in $(WORKSPACE_DIR)/$(NAME) - $(NAME) cannot be built" ; \
		if test -f "$(PUBLISH_HOME)/$(NAME)/$(DEPENDENCY_FILE)" -a -z "$(NO_RECURSION)"; then \
			$(BS_ECHO) "Building [$(NAME)] dependencies using $(PUBLISH_HOME)/$(NAME)/$(DEPENDENCY_FILE)"; \
			$(BS_MAKE) COMPONENT_NAME=$(NAME) EXTERNAL_DEPENDENCY_FILE=$(PUBLISH_HOME)/$(NAME)/$(DEPENDENCY_FILE) build_dependents ; \
		fi; \
	else \
		if test ! -z "$($(ALIAS)_custom_ant_target)"; then \
			$(BS_ECHO) "Building [$(NAME)][source] in $(WORKSPACE_DIR)/$(NAME) using a custom Ant target [$($(ALIAS)_custom_ant_target)] in $(COMPONENT_NAME)'s $(ANT_BUILD_FILE)"; \
			$(BS_MAKE) COMPONENT_NAME=$(NAME) $(ANT_ENV) ANT_TARGETS="$($(ALIAS)_custom_ant_target)" antit; \
		else \
			if test -f "$(WORKSPACE_DIR)/$(NAME)/Makefile"; then \
				$(BS_ECHO) "Building [$(NAME)][source] in $(WORKSPACE_DIR)/$(NAME)"; \
				( cd $(WORKSPACE_DIR)/$(NAME) && $(MAKE_CMD) COMPONENT_NAME=$(NAME) DISABLE_RECURSION=$(NO_RECURSION) EXTERNAL_DEPENDENCY_FILE= build) || exit 255; \
			else \
				if test -z "$(ALLOW_CROSS_COMPONENT_BUILDS)"; then \
					$(BS_ECHO) "WARNING: [$(NAME)][source] in $(WORKSPACE_DIR)/$(NAME) is not bootstrap-enabled - ignoring it"; \
				else \
						$(BS_ECHO) "Cross-component builds enabled. Applying ../$(COMPONENT_NAME)/Makefile to build [$(NAME)][source]"; \
						( cd $(WORKSPACE_DIR)/$(NAME) && $(MAKE_CMD) COMPONENT_NAME=$(NAME) EXTERNAL_DEPENDENCY_FILE= EXTERNAL_MAKEFILE=$(WORKSPACE_DIR)/$(COMPONENT_NAME)/Makefile -f $(WORKSPACE_DIR)/$(COMPONENT_NAME)/Makefile build_component ) || exit 255; \
				fi; \
			fi; \
		fi; \
	fi


ifndef NO_ANT
build_component::
	$(AT)if test -f $(ANT_BUILD_FILE) ; then \
		$(RUN_ANT); \
	else \
		$(BS_ECHO) "WARNING: [$(COMPONENT_NAME)][source] $(ANT_BUILD_FILE) not found. Nothing to build"; \
	fi 
else
build_component::
	$(AT)if test -f $(GMAKE_BUILD_FILE) ; then \
		$(STD_GMAKE_CMD); \
	else \
		$(BS_ECHO) "WARNING: [$(COMPONENT_NAME)][source] $(GMAKE_BUILD_FILE) not found. Nothing to build"; \
	fi 
endif

post_build::
ifdef CALL_POSTBUILD_TARGET
ifndef NO_ANT
	$(BS_MAKE) $(ANT_ENV) ANT_TARGETS=$@ antit
else
	$(AT)if test -f $(GMAKE_BUILD_FILE) ; then \
		$(STD_GMAKE_CMD) $@; \
	else \
		$(BS_ECHO) "WARNING: [$(COMPONENT)][source] $(GMAKE_BUILD_FILE) not found."; \
	fi 
endif
endif
	$(AT)$(PUBLISH_DEPENDENCIES)
	$(AT)$(SET_VERSION_AS_SOURCE)
#-----------------------------------------------------------------------------

ifndef NO_SET_VERSION
SET_VERSION_AS_SOURCE=$(BS_ECHO) "Marking $(COMPONENT_PUBLISH_DIR) as built-from-source" ; $(ECHO) Source > $(COMPONENT_PUBLISH_DIR)/$(VERSION_FILE)
else
SET_VERSION_AS_SOURCE= $(BS_ECHO) Version setting skipped
endif

PUBLISH_DEPENDENCIES = if test -f "$(BUILD_ROOT)/$(DEPENDENCY_FILE)" ; then $(BS_ECHO) "Publishing $(BUILD_ROOT)/$(DEPENDENCY_FILE) to $(COMPONENT_PUBLISH_DIR)"; $(MKDIR_DASH_P) $(COMPONENT_PUBLISH_DIR); $(CP) $(BUILD_ROOT)/$(DEPENDENCY_FILE) $(COMPONENT_PUBLISH_DIR); fi

#-----------------------------------------------------------------------------
.PHONY: bootstrap get_components post_bootstrap src_bootstrap
.PHONY: source_bootstrap

bootstrap:: get_components $(external_checkout_targets) post_bootstrap

source_bootstrap src_bootstrap:: checkout bootstrap

get_components:
	$(AT)if test -z "$(COMPONENT_NAME)"; then \
		$(BS_ECHO) "COMPONENT value must be set"; \
		exit 255; \
	fi
	$(AT)if test ! -z "$(strip $(source_binary_dependencies))"; then \
		$(BS_ECHO) "[$(COMPONENT_NAME)] depends on '$(source_binary_dependencies)'"; \
		$(BS_MAKE) --no-print-directory $(bootstrap_targets); \
	else \
		$(BS_ECHO) "[$(COMPONENT_NAME)] has no dependencies"; \
	fi

post_bootstrap::

get-%:
	$(AT)if test -z "$(filter $*, $(source_binary_dependencies))"; then \
		$(BS_ECHO) "Error: [$*] has not been specified as a source or binary dependency in $(DEPENDENCY_FILE)"; \
		exit 255; \
	fi;
	$(AT)$(BS_MAKE) PARENT=$(PARENT):$(COMPONENT_NAME) NAME=$* ALIAS=$(subst -,_,$*) get_component

ifndef NO_RECURSION
rget-%:
	$(AT)if test -z "$(filter $*, $(source_binary_dependencies))"; then \
		$(BS_ECHO) "Error: [$*] has not been specified as a source or binary dependency in $(DEPENDENCY_FILE)"; \
		exit 255; \
	fi;
	$(AT)$(BS_MAKE) PARENT=$(PARENT):$(COMPONENT_NAME) NAME=$* ALIAS=$(subst -,_,$*) rget_component

rget_component:
	$(AT)if test -z "$(NAME)" -o -z "$(ALIAS)" ; then \
		$(BS_ECHO) "The [$@] target should not be invoked directly"; \
		exit 255; \
	fi
	$(AT)if test -d "$(WORKSPACE_DIR)/$(NAME)" -a ! -z "$(filter $(NAME), $(source_dependencies))"; then \
		if test -f "$(WORKSPACE_DIR)/$(NAME)/$(DEPENDENCY_FILE)"; then \
			$(BS_ECHO) "[$(NAME)][source] has additional dependencies"; \
			$(BS_MAKE) COMPONENT_NAME=$(NAME) EXTERNAL_DEPENDENCY_FILE=$(WORKSPACE_DIR)/$(NAME)/$(DEPENDENCY_FILE) bootstrap ; \
		else \
			$(BS_ECHO) "[$(NAME)][source] has no dependencies"; \
		fi; \
	else \
		if test -f "$(PUBLISH_ROOT)/$($(ALIAS)_destdir)/$(DEPENDENCY_FILE)"; then \
			$(BS_ECHO) "[$(NAME)][$($(ALIAS)_version)][binary]' has additional dependencies"; \
			$(BS_MAKE) COMPONENT_NAME=$(NAME) EXTERNAL_DEPENDENCY_FILE=$(PUBLISH_ROOT)/$($(ALIAS)_destdir)/$(DEPENDENCY_FILE) bootstrap ; \
		else \
			if test -f "$(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(NAME)/$(DEPENDENCY_FILE)"; then \
				$(BS_ECHO) "[$(NAME)][$($(ALIAS)_version)][binary]' has additional dependencies"; \
				$(BS_MAKE) COMPONENT_NAME=$(NAME) EXTERNAL_DEPENDENCY_FILE=$(PUBLISH_ROOT)/$(HOST_OBJDIR)/$(NAME)/$(DEPENDENCY_FILE) bootstrap ; \
			else \
				if test -f "$(PUBLISH_ROOT)/$(JAVA_OBJDIR)/$(NAME)/$(DEPENDENCY_FILE)"; then \
					$(BS_ECHO) "[$(NAME)][$($(ALIAS)_version)][binary]' has additional dependencies"; \
					$(BS_MAKE) COMPONENT_NAME=$(NAME) EXTERNAL_DEPENDENCY_FILE=$(PUBLISH_ROOT)/$(JAVA_OBJDIR)/$(NAME)/$(DEPENDENCY_FILE) bootstrap ; \
				else \
					$(BS_ECHO) "[$(NAME)][$($(ALIAS)_version)][binary] has no dependencies"; \
				fi; \
			fi; \
		fi; \
	fi
endif

# Create symbolic links for a binary component into the publish directory
LOCAL_COPY=0

# rcp the binary component into the publish directory
REMOTE_COPY=1

#
# Generic rule for getting a component and its dependencies
# 1. Check if the component's source has been checked out
# 2. If yes, then don't pull the binary form of the component and instead
#    use the checked out source
# 3. If no, then pull the binary form
#
# NAME = real name of component (with hyphens)
# ALIAS = $(NAME) with hyphens substituted with underscores 
#
get_component:
	$(AT)if test -z "$(NAME)" -o -z "$(ALIAS)" ; then \
		$(BS_ECHO) "The '$@' target should not be invoked directly"; \
		exit 255; \
	fi
	$(AT)if test -d "$(WORKSPACE_DIR)/$(NAME)" -a ! -z "$(filter $(NAME), $(source_dependencies))"; then \
		$(BS_ECHO) "[$(NAME)][source] detected in $(WORKSPACE_DIR)/$(NAME); [$(NAME)][binary] will not be installed"; \
	else \
		$(MKDIR_DASH_P) $(PUBLISH_ROOT); \
		if test -z "$(filter $(NAME), $(source_dependencies))"; then \
			if test -z "$($(ALIAS)_rootdir)"; then \
				$(BS_ECHO) "$(ALIAS)_rootdir must be specified in $(DEPENDENCY_FILE) as $(NAME) is defined as a binary dependency."; \
				exit 255; \
			fi; \
		fi; \
		if test ! -z "$($(ALIAS)_rootdir)"; then \
			$(BS_ECHO) "Getting [$(NAME)][$($(ALIAS)_version)][binary]"; \
			if test -d "$($(ALIAS)_rootdir)"; then \
				$(SHELL) $(BUILD_ROOT)/../bootstrap/compver.sh  \
					"$(NAME)" \
					"$($(ALIAS)_rootdir)" \
					"$($(ALIAS)_dir)" \
					"$($(ALIAS)_host_dir)" \
					"$($(ALIAS)_java_dir)" \
					"$($(ALIAS)_version)" \
					"$($(ALIAS)_subdir)" \
					"$(PUBLISH_ROOT)" \
					"$(HOST_OBJDIR)" \
					"$(JAVA_OBJDIR)" \
					"$($(ALIAS)_destdir)" \
					"$(LOCAL_COPY)" \
					"$(PARENT)" \
					"$(PARENT_FILE)" \
					"$(VERSION_FILE)" \
					"$(BOOTSTRAP_ERRORS)" ; \
			else \
				$(SHELL) $(BUILD_ROOT)/../bootstrap/compver.sh  \
					"$(NAME)" \
					"$($(ALIAS)_rootdir)" \
					"$($(ALIAS)_dir)" \
					"$($(ALIAS)_host_dir)" \
					"$($(ALIAS)_java_dir)" \
					"$($(ALIAS)_version)" \
					"$($(ALIAS)_subdir)" \
					"$(PUBLISH_ROOT)" \
					"$(HOST_OBJDIR)" \
					"$(JAVA_OBJDIR)" \
					"$($(ALIAS)_destdir)" \
					"$(REMOTE_COPY)" \
					"$(PARENT)" \
					"$(PARENT_FILE)" \
					"$(VERSION_FILE)" \
					"$(BOOTSTRAP_ERRORS)" ; \
			fi; \
			if test "$$?" = "1"; then \
				if test ! -z "$(filter $(NAME), $(binary_dependencies))"; then \
					exit 255; \
				else \
					$(BS_MAKE) checkout-$(NAME); \
				fi; \
			fi; \
		else \
			$(BS_MAKE) checkout-$(NAME); \
		fi; \
	fi

#-----------------------------------------------------------------------------

.PHONY: clobber_publish

# Clobbering the entire publish directory
remove_publish:
	$(RM) -rf $(PUBLISH_ROOT)

#-----------------------------------------------------------------------------

.PHONY: checkout print_source_dependencies checkout_source_dependencies
.PHONY: rcheckout_source_dependencies post_checkout

checkout: print_source_dependencies $(checkout_targets) post_checkout

print_source_dependencies:
	$(AT)if test ! -z "$(strip $(source_dependencies))" ; then \
		$(BS_ECHO) "[$(COMPONENT_NAME)][source] depends on '$(addsuffix [source],$(source_dependencies))'"; \
	else \
		$(BS_ECHO) "[$(COMPONENT_NAME)][source] has no source dependencies"; \
	fi
	
print_binary_dependencies:
	$(AT)if test ! -z "$(strip $(binary_dependencies))" ; then \
		$(BS_ECHO) "[$(COMPONENT_NAME)][source] depends on '$(addsuffix [binary],$(binary_dependencies))'"; \
	else \
		$(BS_ECHO) "[$(COMPONENT_NAME)][source] has no binary dependencies"; \
	fi
	
post_checkout::
	$(AT)if test ! -z "$(strip $(source_dependencies))" ; then \
		$(BS_ECHO) "[$(COMPONENT_NAME)][source] checkout complete"; \
	fi

#
# Checkout the sources for the specified component and if recursive bootstrap
# is enabled then checkout the sources of the component's dependencies as
# well
#
checkout-%:
ifndef NO_RECURSION
	$(BS_MAKE) MAKEFLAGS='$(MAKEFLAGS)' checkoutsrc-$* rcheckoutsrc-$*
else
	$(BS_MAKE) MAKEFLAGS='$(MAKEFLAGS)' checkoutsrc-$*
endif

#
# Checkout the source of a specified component
#
checkoutsrc-%:
	$(AT)if test -z "$(filter $*, $(source_dependencies))"; then \
		$(BS_ECHO) "Checkout error: [$*] has not been specified as an internal dependency in $(DEPENDENCY_FILE)"; \
		exit 255; \
	fi;
	$(AT)if test -d "$(WORKSPACE_DIR)/$*"; then \
		$(BS_ECHO) "WARNING: Checking out [$*][source] over existing sources in $(WORKSPACE_DIR)/$*"; \
	else \
		$(BS_ECHO) "Checking out [$*][source] in $(WORKSPACE_DIR)/$*"; \
	fi
	$(AT)if test -z "$($(subst -,_,$*)_cvs_module)"; then \
		$(BS_MAKE) COMPONENT_CVS_ROOT=$($(subst -,_,$*)_cvs_root) \
					COMPONENT_CVS_MODULE=$* \
					COMPONENT_CVS_RELTAG=$($(subst -,_,$*)_cvs_reltag) \
					COMPONENT_CVS_DEVBRANCH=$($(subst -,_,$*)_cvs_devbranch) \
					COMPONENT_CVS_DEST=$($(subst -,_,$*)_cvs_dest) \
					checkout_component ; \
	else \
		$(BS_MAKE) COMPONENT_CVS_ROOT=$($(subst -,_,$*)_cvs_root) \
					COMPONENT_CVS_MODULE=$($(subst -,_,$*)_cvs_module) \
					COMPONENT_CVS_RELTAG=$($(subst -,_,$*)_cvs_reltag) \
					COMPONENT_CVS_DEVBRANCH=$($(subst -,_,$*)_cvs_devbranch) \
					COMPONENT_CVS_DEST=$($(subst -,_,$*)_cvs_dest) \
					checkout_component ; \
	fi
	
#
# Internal target that actually runs the CVS command to checkout source
#
checkout_component::
	$(AT)if test -z "$(COMPONENT_CVS_MODULE)"; then \
		$(BS_ECHO) "The '$@' target should not be invoked directly"; \
		exit 255; \
	fi
	$(AT)$(MKDIR_DASH_P) $(CVS_CHECKOUT_DIR)
	( cd $(CVS_CHECKOUT_DIR) && $(CVS_CHECKOUT_CMD) ) || exit 255
ifndef EXTERNAL_DEPENDENCY
	$(AT)$(MKDIR_DASH_P) $(CHECKOUT_INFO_ROOT)/$(COMPONENT_CVS_MODULE)
	$(AT)$(ECHO) $(CVS_CHECKOUT_TAG) > $(CHECKOUT_INFO_ROOT)/$(COMPONENT_CVS_MODULE)/$(VERSION_FILE)
	$(AT)$(ECHO) $(PARENT) > $(CHECKOUT_INFO_ROOT)/$(COMPONENT_CVS_MODULE)/$(PARENT_FILE)
endif

ifndef NO_RECURSION
#
# Recursively checkout the sources of the specified component's dependencies
#
rcheckoutsrc-%:
	$(AT)if test -z "$(filter $*, $(source_dependencies))"; then \
		$(BS_ECHO) "Checkout error: [$*] has not been specified as an internal dependency in $(DEPENDENCY_FILE)"; \
		exit 255; \
	fi;
	$(AT)if test -z "$($(subst -,_,$*)_cvs_module)"; then \
		$(BS_MAKE) COMPONENT_CVS_ROOT=$($(subst -,_,$*)_cvs_root) \
					COMPONENT_CVS_MODULE=$* \
					COMPONENT_CVS_RELTAG=$($(subst -,_,$*)_cvs_reltag) \
					COMPONENT_CVS_DEVBRANCH=$($(subst -,_,$*)_cvs_devbranch) \
					COMPONENT_CVS_DEST=$($(subst -,_,$*)_cvs_dest) \
					rcheckout_component ; \
	else \
		$(BS_MAKE) COMPONENT_CVS_ROOT=$($(subst -,_,$*)_cvs_root) \
					COMPONENT_CVS_MODULE=$($(subst -,_,$*)_cvs_module) \
					COMPONENT_CVS_RELTAG=$($(subst -,_,$*)_cvs_reltag) \
					COMPONENT_CVS_DEVBRANCH=$($(subst -,_,$*)_cvs_devbranch) \
					COMPONENT_CVS_DEST=$($(subst -,_,$*)_cvs_dest) \
					rcheckout_component ; \
	fi

rcheckout_component::
	$(AT)if test -z "$(COMPONENT_CVS_MODULE)"; then \
		$(BS_ECHO) "The '$@' target should not be invoked directly"; \
		exit 255; \
	fi
	$(AT)if test -d "$(WORKSPACE_DIR)/$(COMPONENT_CVS_MODULE)"; then \
		if test -f "$(WORKSPACE_DIR)/$(COMPONENT_CVS_MODULE)/$(DEPENDENCY_FILE)"; then \
			( cd $(WORKSPACE_DIR)/$(COMPONENT_CVS_MODULE) && $(BS_MAKE) checkout ) || exit 255; \
		else \
			$(BS_ECHO) "[$@][source] has no source dependencies"; \
		fi; \
	fi

endif

external-%:
	$(AT)if test -z "$($(subst -,_,$*)_cvs_root)"; then \
		$(BS_ECHO) "Error: External dependencies must specify cvs_root"; \
		exit 255; \
	fi
	$(AT)if test -z "$($(subst -,_,$*)_cvs_module)"; then \
		$(BS_ECHO) "Error:$(subst -,_,$*)_cvs_module has not been defined"; \
		exit 255; \
	fi
	$(BS_MAKE) COMPONENT_CVS_ROOT=$($(subst -,_,$*)_cvs_root) \
				COMPONENT_CVS_MODULE=$($(subst -,_,$*)_cvs_module) \
				COMPONENT_CVS_RELTAG=$($(subst -,_,$*)_cvs_reltag) \
				COMPONENT_CVS_DEVBRANCH=$($(subst -,_,$*)_cvs_devbranch) \
				COMPONENT_CVS_DEST=$($(subst -,_,$*)_cvs_dest) \
				EXTERNAL_DEPENDENCY=1 checkout_component ;

#-----------------------------------------------------------------------------
ifndef NO_ANT
.PHONY: antit
#
# Rule to invoke ant
#
# This target should not be invoked directly from the command line
antit::
	$(ANT)
endif

#-----------------------------------------------------------------------------
.PHONY: package package_dependents package_component

#
# Use a variable other than NO_RECURSION for suppressing dependency packaging
# in a sub-component of one that has recursion disabled.
#
ifeq ($(DISABLE_RECURSION),)
package:: package_dependents package_component
else
package:: package_component
endif
ifndef NO_PACKAGE
	$(AT)$(BS_ECHO) "[$(COMPONENT_NAME)][source] package complete"
endif


package_dependents: $(addprefix package-,$(source_dependencies))
	$(AT)if test ! -z "$(strip $(source_dependencies))" ; then \
		$(BS_ECHO) "[$(COMPONENT_NAME)] source dependencies have been packaged"; \
	else \
		$(BS_ECHO) "[$(COMPONENT_NAME)] has no source dependencies"; \
	fi

package-%:
	$(AT)if test ! -z "$(filter $*, $(binary_dependencies))"; then \
		$(BS_ECHO) "Cannot package binary dependencies. [$*] has been specified as a binary dependency in $(DEPENDENCY_FILE)"; \
		exit 255; \
	fi
	$(AT)if test ! -d "$(WORKSPACE_DIR)/$*"; then \
		$(BS_ECHO) "WARNING: [$*][source] has not been checked out in $(WORKSPACE_DIR)/$* - $* cannot be packaged" ; \
		if test -f "$(PUBLISH_HOME)/$*/$(DEPENDENCY_FILE)" -a -z "$(NO_RECURSION)"; then \
			$(BS_ECHO) "Packaging [$*] dependencies using $(PUBLISH_HOME)/$*/$(DEPENDENCY_FILE)"; \
			$(BS_MAKE) COMPONENT_NAME=$* EXTERNAL_DEPENDENCY_FILE=$(PUBLISH_HOME)/$*/$(DEPENDENCY_FILE) package_dependents ; \
		fi; \
	else \
		if test -f "$(WORKSPACE_DIR)/$*/Makefile"; then \
			$(BS_ECHO) "Packaging [$*][source] in $(WORKSPACE_DIR)/$*"; \
			( cd $(WORKSPACE_DIR)/$* && $(MAKE_CMD) COMPONENT_NAME=$* DISABLE_RECURSION=$(NO_RECURSION) EXTERNAL_DEPENDENCY_FILE= package) || exit 255; \
		else \
			if test -z "$(ALLOW_CROSS_COMPONENT_BUILDS)"; then \
				$(BS_ECHO) "WARNING: [$*][source] in $(WORKSPACE_DIR)/$* is not bootstrap-enabled - ignoring it"; \
			else \
				$(BS_ECHO) "Cross-component builds enabled. Applying ../$(COMPONENT_NAME)/Makefile to package [$*][source]"; \
				( cd $(WORKSPACE_DIR)/$* && $(MAKE_CMD) COMPONENT_NAME=$* EXTERNAL_DEPENDENCY_FILE= EXTERNAL_MAKEFILE=$(WORKSPACE_DIR)/$(COMPONENT_NAME)/Makefile -f $(WORKSPACE_DIR)/$(COMPONENT_NAME)/Makefile package_component ) || exit 255; \
			fi; \
		fi; \
	fi


ifdef NO_PACKAGE
package_component::
		$(AT)$(BS_ECHO) "[$(COMPONENT_NAME)][source] has nothing to package";
else
ifndef NO_ANT
package_component::
	$(AT)if test -f $(ANT_BUILD_FILE) ; then \
		$(BS_MAKE) $(ANT_ENV) ANT_TARGETS=package antit; \
	else \
		$(BS_ECHO) "WARNING: [$(COMPONENT_NAME)][source] $(ANT_BUILD_FILE) not found. Nothing to package"; \
	fi 
else
package_component::
	$(AT)if test -f $(GMAKE_BUILD_FILE) ; then \
		$(STD_GMAKE_CMD) package; \
	else \
		$(BS_ECHO) "WARNING: [$(COMPONENT_NAME)][source] $(GMAKE_BUILD_FILE) not found. Nothing to package"; \
	fi 
endif
endif

#-----------------------------------------------------------------------------
.PHONY: clean clean_dependents clean_component

#
# Use a variable other than NO_RECURSION for suppressing dependency cleanup
# in a sub-component of one that has recursion disabled.
#
ifeq ($(DISABLE_RECURSION),)
clean:: clean_dependents clean_component
else
clean:: clean_component
endif
ifndef NO_CLEAN
	$(AT)$(BS_ECHO) "[$(COMPONENT_NAME)][source] clean complete"
endif


clean_dependents: $(addprefix clean-,$(source_dependencies))
	$(AT)if test ! -z "$(strip $(source_dependencies))" ; then \
		$(BS_ECHO) "[$(COMPONENT_NAME)] source dependencies have been cleaned"; \
	else \
		$(BS_ECHO) "[$(COMPONENT_NAME)] has no source dependencies"; \
	fi

clean-%:
	$(AT)if test ! -z "$(filter $*, $(binary_dependencies))"; then \
		$(BS_ECHO) "Cannot clean binary dependencies. [$*] has been specified as a binary dependency in $(DEPENDENCY_FILE)"; \
		exit 255; \
	fi
	$(AT)if test ! -d "$(WORKSPACE_DIR)/$*"; then \
		$(BS_ECHO) "WARNING: [$*][source] has not been checked out in $(WORKSPACE_DIR)/$* - $* cannot be cleaned" ; \
		if test -f "$(PUBLISH_HOME)/$*/$(DEPENDENCY_FILE)" -a -z "$(NO_RECURSION)"; then \
			$(BS_ECHO) "Cleaning [$*] dependencies using $(PUBLISH_HOME)/$*/$(DEPENDENCY_FILE)"; \
			$(BS_MAKE) COMPONENT_NAME=$* EXTERNAL_DEPENDENCY_FILE=$(PUBLISH_HOME)/$*/$(DEPENDENCY_FILE) clean_dependents ; \
		fi; \
	else \
		if test -f "$(WORKSPACE_DIR)/$*/Makefile"; then \
			$(BS_ECHO) "Cleaning [$*][source] in $(WORKSPACE_DIR)/$*"; \
			( cd $(WORKSPACE_DIR)/$* && $(MAKE_CMD) COMPONENT_NAME=$* DISABLE_RECURSION=$(NO_RECURSION) EXTERNAL_DEPENDENCY_FILE= clean) || exit 255; \
		else \
			if test -z "$(ALLOW_CROSS_COMPONENT_BUILDS)"; then \
				$(BS_ECHO) "WARNING: [$*][source] in $(WORKSPACE_DIR)/$* is not bootstrap-enabled - ignoring it"; \
			else \
				$(BS_ECHO) "Cross-component builds enabled. Applying ../$(COMPONENT_NAME)/Makefile to clean [$*][source]"; \
				( cd $(WORKSPACE_DIR)/$* && $(MAKE_CMD) COMPONENT_NAME=$* EXTERNAL_DEPENDENCY_FILE= EXTERNAL_MAKEFILE=$(WORKSPACE_DIR)/$(COMPONENT_NAME)/Makefile -f $(WORKSPACE_DIR)/$(COMPONENT_NAME)/Makefile clean_component ) || exit 255; \
			fi; \
		fi; \
	fi


ifdef NO_CLEAN
clean_component::
		$(AT)$(BS_ECHO) "[$(COMPONENT_NAME)][source] has nothing to clean";
else
ifndef NO_ANT
clean_component::
	$(AT)if test -f $(ANT_BUILD_FILE) ; then \
		$(BS_MAKE) $(ANT_ENV) ANT_TARGETS=clean antit; \
	else \
		$(BS_ECHO) "WARNING: [$(COMPONENT_NAME)][source] $(ANT_BUILD_FILE) not found. Nothing to clean"; \
	fi 
else
clean_component::
	$(AT)if test -f $(GMAKE_BUILD_FILE) ; then \
		$(STD_GMAKE_CMD) clean; \
	else \
		$(BS_ECHO) "WARNING: [$(COMPONENT_NAME)][source] $(GMAKE_BUILD_FILE) not found. Nothing to clean"; \
	fi 
endif
endif

#-----------------------------------------------------------------------------
.PHONY: checkstyle

checkstyle:
	$(BS_MAKE) $(ANT_ENV) ANT_TARGETS="-f $(BUILD_ROOT)/$(WORKSPACE_DIR)/bootstrap/checkstyle.xml checkstyle -Dbasedir=. -DJWS_EXTERNAL_COMPONENTS_DIR=$(JWS_EXTERNAL_COMPONENTS_DIR)" antit

