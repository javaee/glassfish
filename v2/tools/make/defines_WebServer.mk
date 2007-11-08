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

# defines for Web Server

COMPANY_NAME = "Sun Netscape Alliance"
COMPANY_SHORT_NAME = SunNetscapeAlliance
BRAND_NAME = iPlanet
BRAND_NAME_OLD = Netscape
PRODUCT_COPYRIGHT="Copyright (c) 2001 Sun Microsystems, Inc."

#build Numbers

# major, minor are numeric
VER_MAJOR=6
VER_MINOR=0

# PRODUCT_LICENSE_SUB_NAME is set to Beta for a beta so the current
# license file is picked up
PRODUCT_LICENSE_SUB_NAME = 

# SERVICE_PACK_ID needs to be SP for Service Packs
# VER_SERVICE_PACK needs to be the SP version. This is numeric (1,2 etc)
# VER_HOT_PATCH is used to indicate a hot patch between SPs as in SP2a
# hot patch is alpha, lowercase (a,b etc)
VER_SERVICE_PACK=2
SERVICE_PACK_ID=SP
VER_HOT_PATCH=

MAJOR_VERSION="$(VER_MAJOR)"
MINOR_VERSION="$(VER_MINOR)"

DAEMON_DLL_VER=40

#######################################
# Product name and feature definition #
#######################################
ifeq ($(PROJECT),Enterprise) # this should be the ONLY use of $(PROJECT)

# the long name (incl. quotes): e.g. "Web Server, Enterprise Edition"
PRODUCT_FULL_NAME = "Web Server, Enterprise Edition"
# the name (incl. quotes): e.g. "Web Server"
PRODUCT_NAME = "Web Server"
# the short name: must be one word, used as name of install dir, for example
PRODUCT_SHORT_NAME = WebServer
PRODUCT_SUB_NAME = Enterprise
NS_PRODUCT = NS_ENTERPRISE
PRODUCT_ABBREVIATION = iWS

# you can define NO_xxxx in defines_$(PLATFORM).mk to get rid of some of these
BUILD_IWSJAVA=1
ifndef NO_SEARCH
BUILD_SEARCH=1
endif
ifndef NO_WAI
BUILD_WAI=1
endif
ifndef NO_SNMP
BUILD_SNMP=1
endif

# With iWS50 there is no SSJS and there is no SSJSDB
NO_SSJS=1
NO_SSJSDB=1

ifndef NO_SSJS
BUILD_SSJS=1
endif
ifndef NO_SSJSDB
BUILD_SSJSDB=1
endif

#
# optional features. do not define to turn off.

# multiprocess mode
FEAT_MULTIPROCESS=1
# software virtual servers
FEAT_SW_VIRTUAL_SERVERS=1
# internal log rotation
FEAT_INTERNAL_LOG_ROTATION=1
# unlimited operation (if not defined, some restrictions apply)
FEAT_NOLIMITS=1
# can be tuned
FEAT_TUNEABLE=1
# daemonstats subsystem (used for tuning)
FEAT_DAEMONSTATS=1
# cluster administration
FEAT_CLUSTER=1
# delegated/distributed admin
FEAT_DELEGATED_ADMIN=1
# dynamic groups
FEAT_DYNAMIC_GROUPS=1
# password policies (expiration)
FEAT_PASSWORD_POLICIES=1
# upgrade/migration capability
FEAT_UPGRADE=1
# PKCS 11 modules
FEAT_PKCS_MODULES=1

else
ifeq ($(PROJECT),FastTrack) # this should be the ONLY use of $(PROJECT)

PRODUCT_FULL_NAME = "Web Server, FastTrack Edition"
PRODUCT_NAME = "Web Server"
PRODUCT_SHORT_NAME = WebServer
PRODUCT_SUB_NAME = FastTrack
NS_PRODUCT = NS_PERSONAL
PRODUCT_ABBREVIATION = iWS
VER_SERVICE_PACK =
SERVICE_PACK_ID =

# no WAI, WEPUB, SNMP, SSJSDB, CLUSTER for FastTrack Edition SSJS
BUILD_IWSJAVA=1

else

# placeholder for new products
PRODUCT_FULL_NAME = "Web Server, Undefined Edition"
PRODUCT_NAME = "Web Server"
PRODUCT_SHORT_NAME = WebServer
PRODUCT_SUB_NAME = Undefined
NS_PRODUCT = NS_UNDEFINED

endif
endif

# database definitions
# build those if not explicitely disabled
ifdef BUILD_SSJSDB

ifndef NO_INFORMIX
BUILD_INFORMIX=1
endif
ifndef NO_ORACLE
BUILD_ORACLE=1
endif
ifndef NO_SYBASE
BUILD_SYBASE=1
endif
ifndef NO_ODBC
BUILD_ODBC=1
endif

endif

PRODUCT_ID = "$(BRAND_NAME)-$(PRODUCT_SHORT_NAME)-$(PRODUCT_SUB_NAME)"
PRODUCT_HEADER_ID = "$(BRAND_NAME_OLD)-$(PRODUCT_SUB_NAME)"

# All variants are called https as well to make life easier
SERVER_DIR_NAME=https

ES_RPATH=.:../lib:../../lib

# see also ns/netsite/include/version.h
PRODUCT_VERSION = $(VER_MAJOR).$(VER_MINOR)

ifneq ($(VER_SERVICE_PACK),)

# SPx
ifneq ($(VER_HOT_PATCH),)
# have hot patch in there -> 4.1SP2a
PRODUCT_FULL_VERSION = "$(VER_MAJOR).$(VER_MINOR)$(SERVICE_PACK_ID)$(VER_SERVICE_PACK)$(VER_HOT_PATCH)"
else
# no hot patch -> 4.1SP2
PRODUCT_FULL_VERSION = "$(VER_MAJOR).$(VER_MINOR)$(SERVICE_PACK_ID)$(VER_SERVICE_PACK)"
endif

else

# no SP yet
ifneq ($(VER_HOT_PATCH),)
# have hot patch in there -> 4.1SP0a
PRODUCT_FULL_VERSION = "$(VER_MAJOR).$(VER_MINOR)SP0$(VER_HOT_PATCH)"
else
# no hot patch, no SP -> 4.1
PRODUCT_FULL_VERSION = "$(VER_MAJOR).$(VER_MINOR)"
endif

endif

ifndef SECURITY_POLICY
SECURITY_POLICY=DOMESTIC
NS_DOMESTIC=1
DEFINES+= -DNS_DOMESTIC
else
ifeq ($(SECURITY_POLICY),DOMESTIC)
NS_DOMESTIC=1
DEFINES+= -DNS_DOMESTIC
else
NS_EXPORT=1
DEFINES+= -DNS_EXPORT
endif
endif

ifndef LD_RPATH
LD_RPATH=$(ES_RPATH)
endif

DEFINES+= -D$(NS_PRODUCT)
DEFINES+= -DMCC_HTTPD
DEFINES+= -DNET_SSL
DEFINES+= -DSERVER_BUILD
DEFINES+= -DENCRYPT_PASSWORDS
DEFINES+= -DNSPR20
DEFINES+= -DSPAPI20
DEFINES+= -DPEER_SNMP
DEFINES+= -DOSVERSION=$(OSVERSION)

DAEMON_DLL=ns-httpd$(DAEMON_DLL_VER)

