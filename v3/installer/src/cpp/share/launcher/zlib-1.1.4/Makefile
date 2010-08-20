#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2008 Sun Microsystems, Inc. All rights reserved.
# 
# Use is subject to License Terms
#
ifndef COMPONENT_NAME
COMPONENT_NAME=installer
endif
BUILD_ROOT=../../../../..
include $(BUILD_ROOT)/make/defines.mk

# Get the name of the archive
include defines.mk

AR_TARGET = $(ZLIB)
AR_OBJS = compress deflate gzio infblock infcodes inffast inflate inftrees infutil trees uncompr zadler32 zcrc32 zutil

PCH_DEP += $(wildcard *.h)

include $(BUILD_ROOT)/make/rules.mk

# User customizable rules

libraries:: $(OBJDIR)/$(LIBPREFIX)$(AR_TARGET).$(STATIC_LIB_SUFFIX)

