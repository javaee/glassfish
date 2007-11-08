#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

BUILD_ROOT = ../..

include $(BUILD_ROOT)/make/defines.mk

PUBLISH_ROOT = $(BUILD_ROOT)/work/B$(BASELINE)/$(HOST_OBJDIR)
BUNDLED_ROOT = $(PUBLISH_ROOT)/BROOT

ETCDIRS = \
	$(BUNDLED_ROOT)/etc/security

RBACFILES = \
	$(BUNDLED_ROOT)/etc/security/exec_attr.template \
	$(BUNDLED_ROOT)/etc/security/prof_attr.template

all :: ROOT rbac
rbac: ROOT
	rm -f $(RBACFILES)
	cp exec_attr.template $(BUNDLED_ROOT)/etc/security
	cp prof_attr.template $(BUNDLED_ROOT)/etc/security
	touch $(PUBLISH_ROOT)/lib/install/templates/domains.bin
	touch $(PUBLISH_ROOT)/lib/install/templates/domains.lck

clean clobber:: clean-rbac
clean-rbac::
	$(RM) -rf $(ETCDIRS) 

ROOT: \
	$(BUNDLED_ROOT) \
	$(BUNDLED_ROOT)/etc \
	$(ETCDIRS)

$(BUNDLED_ROOT) $(BUNDLED_ROOT)/%:
	mkdir -p $@

.PHONY: ROOT

include $(BUILD_ROOT)/make/rules.mk
