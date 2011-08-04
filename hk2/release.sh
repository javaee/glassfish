#!/bin/sh -ex
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
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

# Script to perform a new release with automatic version increment.
# This script is meant to be run from Hudson.
#
# Because of the tricky dependency inside HK2, where a part of the build (hk2/hk2 in particular)
# uses another part of the build (hk2-maven-plugin) as a build extension,
# normal "mvn -B release:prepare release:perform" fails.
#
# This script works around this issue, and that's why it's very non-obvious.

# To protect the release from interference with other builds in this system,
# use a private copy of the local repository. This avoids a JVM crash and
# other mysterious zip/jar related errors

# The first parameter is settings.xml.  If not set, then it assume that settings.xml is in ~/.m2/
    RELEASE_OPTIONS=""
if [ $# -eq 1 ]; then
    RELEASE_OPTS="$1"
fi
export RELEASE_OPTS

MAVEN_OPTS="-Xmx256m -Dmaven.repo.local=$PWD/repo"
export MAVEN_OPTS

uname -a

# don't let the crash logs fail the release
rm hs_err*.log || true

# First, get to the base line. This is probably not a requirement.
mvn -P release-phase1 install
mvn clean install

# clean previous release attempt
mvn release:clean

# This starts the release preparation, but it eventually fails because
# it's unable to resolve the maven-hk2-plugin that the release is going to build.
mvn -e -B -DuseEditMode=true -P release ${RELEASE_OPTS} release:prepare || true

# At this point local POM has the release version set,
# so we build it, in particular maven-hk2-plugin.
mvn -e -P release-phase1 install

# On one occasion I got the next release:prepare to fail, due to missing hk2:<RELEASE VER>:jar
# so just to be safe, fill the local repository with release versions first.
mvn -e install

# Now retry release:prepare and this shall run to the completion
mvn -e -B -DuseEditMode=true -P release ${RELEASE_OPTS} release:prepare

# At this point POM has the next SNAPSHOT version set,
# and unless I build maven-hk2-plugin again, the POM fails to load
# when we run "release:perform" later. So do the build again.
mvn -e -P release-phase1 install

# finally a release
mvn -e -B -P release-modules,release ${RELEASE_OPTS} release:perform


# Once the bits are pushed and made visible, you just need to change v3/pom.xml <hk2.version> property
# and GFv3 will pick up the new version of HK2.

# Boy, Maven sucks!
