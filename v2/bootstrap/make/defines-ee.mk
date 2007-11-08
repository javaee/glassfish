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

#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#
ifneq ($(DO_NOT_BUILD_PE_SRC), 1)
JWS_COMPONENTS_DIR=/net/koori.sfbay/onestop/sjsas_ee/9.0
endif
# All the external components come from single location 

ifndef CURRENT_RELEASE_TAG
CURRENT_RELEASE_TAG=S1AS8PE_Base
endif


ifndef DO_NOT_BUILD_PE_SRC
DO_NOT_BUILD_PE_SRC=0
endif

ifndef BUILD_ALL_SRC
BUILD_ALL_SRC=0
endif
# We are in an SE/EE build. Bootstrap the PE installer and packager, if desired.
ifndef BS_PE_INSTALLER
BS_PE_INSTALLER=0
endif
#################################################
# Bundled JDK
#################################################
# For EE we bundle JDK 1.5.0 for ALL platforms (MAC does not have JDK 1.5.0 -- we do not have MAC EE build either).
PRINCIPAL_PLATFORMS_BUNDLED_JDK_VERSION=1.5.0_01
MAC_BUNDLED_JDK_VERSION=NOT_AVAILABLE_YET
