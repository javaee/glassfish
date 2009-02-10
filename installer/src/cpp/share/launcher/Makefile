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
BUILD_ROOT=../../../..
NO_LINK_CPLUSPLUS=TRUE
include $(BUILD_ROOT)/make/defines.mk

ZLIBDIR = zlib-1.1.4
ZLIB_INCLUDES = -I$(ZLIBDIR)
RES_BUNDLE_NAME = messages

# Get the name of the archive
include $(ZLIBDIR)/defines.mk

# Compile zlib first
DIRS = $(ZLIBDIR)

COMMON_EXE_OBJS = $(RES_BUNDLE_NAME) java zip_util
ifeq ($(OS_ARCH), WINNT)
COMMON_EXE_OBJS += java_md.winnt
else
COMMON_EXE_OBJS += java_md.unix
endif

EXE_TARGET = setup
EXE1_TARGET = uninstall
EXE_OBJS = $(COMMON_EXE_OBJS) setup
EXE1_OBJS = $(COMMON_EXE_OBJS) uninstall
EXE_LIBDIRS += $(ZLIBDIR)/$(OBJDIR)
EXE1_LIBDIRS += $(ZLIBDIR)/$(OBJDIR)
ifeq ($(OS_ARCH), WINNT)
EXE_RES=java
EXE1_RES=java
EXE2_TARGET = setup.dos
EXE3_TARGET = uninstall.dos
EXE2_OBJS = $(COMMON_EXE_OBJS) setup.dos
EXE3_OBJS = $(COMMON_EXE_OBJS) uninstall.dos
EXE2_LIBDIRS += $(ZLIBDIR)/$(OBJDIR)
EXE3_LIBDIRS += $(ZLIBDIR)/$(OBJDIR)
endif

LOCAL_INC += $(JNI_INCLUDES) $(ZLIB_INCLUDES)
ifeq ($(OS_ARCH), Linux)
# Add X11 include path explicitly since some gcc versions may not automatically
# include them
LOCAL_INC += -I/usr/X11R6/include
endif
ifeq ($(OS_ARCH), SunOS)
# To be absolutely safe, explicitly add the X11 include paths even though
# Forte automatically includes them
LOCAL_INC += -I/usr/dt/include -I/usr/openwin/include
endif
LOCAL_BINARIES = $(EXE_TARGET) $(EXE1_TARGET)
ifeq ($(OS_ARCH), WINNT)
LOCAL_BINARIES += $(EXE2_TARGET) $(EXE3_TARGET)
LOCAL_LIB += user32 gdi32
endif
ifeq ($(OS_ARCH), Linux)
# Needed to make the executable's resource symbols available via dlsym()
LD_FLAGS += -Wl,-export-dynamic
endif

# Link in zlib archive
LOCAL_LIB += $(ZLIB)

PCH_DEP += $(OBJDIR)/$(RES_BUNDLE_NAME).c $(wildcard *.h) $(wildcard $(ZLIBDIR)/*.h)

# Needed for finding the symbol names in the generated
# $(OBJDIR)/$(RES_BUNDLE_NAME).c file
DEFINES += -DRES_BUNDLE_NAME=\"$(RES_BUNDLE_NAME)\"
ifeq ($(OS_ARCH), Linux)
# Needed when including dlfcn.h
DEFINES += -D_GNU_SOURCE
endif

include $(BUILD_ROOT)/make/rules.mk

# User customizable rules

JAVA_PROPS_TO_C_CLASS = JavaPropertiesToC

$(OBJDIR)/%.c: $(OBJDIR)/$(JAVA_PROPS_TO_C_CLASS).class $(wildcard $(RES_BUNDLE_NAME)*.properties)
	@$(RM) -f $@ $@.out
	$(EXTERNAL_JDK_DIR)/bin/java -classpath $(OBJDIR) $(JAVA_PROPS_TO_C_CLASS) -srcdir . -bundle $(RES_BUNDLE_NAME) > $@.out
	$(CP) $@.out $@

$(OBJDIR)/%.class: %.java
	$(EXTERNAL_JDK_DIR)/bin/javac -d $(OBJDIR) -classpath . $?

# Don't let make delete generated source files
.PHONY: $(OBJDIR)/%.c
