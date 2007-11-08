#
# NOTE: This file is used by Enterprise, Messaging AND Collabra servers.
#       Please do NOT change indiscriminately
#

include $(BUILD_ROOT)/make/defines_x86_Linux_2.4.mk

BASEFLAGS += -fPIC

ifdef DEBUG_BUILD
# easy on the warnings for now
CC_DEBUG = -g $(BASEFLAGS) -Wno-unknown-pragmas -Wno-non-virtual-dtor -Wno-unused
C_DEBUG  = -g $(BASEFLAGS)
LD_DEBUG =
else 
# optimized settings here
CC_DEBUG  = -O3 $(BASEFLAGS) -Wno-unknown-pragmas -Wno-non-virtual-dtor -Wno-unused
C_DEBUG   = -O3 $(BASEFLAGS)
LD_DEBUG  = -s
endif

PLATFORM_LD_OPTS = $(BASEFLAGS)
LD_DYNAMIC	= -shared
JNI_MD_LIBTYPE = server
JNI_MD_SYSNAME = amd64
SETUPSDK_JNIDIR = Unix/Linux/X86_64

IDS_NSPR_DIR    =$(NSPR_DIR)
IDS_LDAPSDK_DIR =$(LDAPSDK_DIR)

# XXX
# stuff that doesn't work on x86_64 yet
# XXX 
NO_SNMP=1

# Enables creation of Linux RPM packages
FEAT_OS_NATIVE_PKG=

# Enables creation of Linux patches
FEAT_OS_PATCH_PKG=

#PHP plugin unavailable for this platform
FEAT_PHP=
