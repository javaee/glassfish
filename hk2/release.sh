#!/bin/sh -ex
# Script to perform a new release with automatic version increment.
# This script is meant to be run from Hudson.
#
# Because of the tricky dependency inside HK2, where a part of the build (hk2/hk2 in particular)
# uses another part of the build (hk2-maven-plugin) as a build extension,
# normal "mvn -B release:prepare release:perform" fails.
#
# This script works around this issue, and that's why it's very non-obvious.

# First, get to the base line. This is probably not a requirement.
mvn clean install

# This starts the release preparation, but it eventually fails because
# it's unable to resolve the maven-hk2-plugin that the release is going to build.
mvn -B -P release release:prepare || true

# At this point local POM has the release version set,
# so we build it, in particular maven-hk2-plugin.
mvn -P release-phase1 install

# On one occasion I got the next release:prepare to fail, due to missing hk2:<RELEASE VER>:jar
# so just to be safe, fill the local repository with release versions first.
mvn install

# Now retry release:prepare and this shall run to the completion
mvn -B -P release release:prepare

# At this point POM has the next SNAPSHOT version set,
# and unless I build maven-hk2-plugin again, the POM fails to load
# when we run "release:perform" later. So do the build again.
mvn -P release-phase1 install

# finally a release
mvn -B release:perform

# when other people get the new workspace, they'll fail to resolve maven-hk2-plugin,
# so we need some seed version to be out there.
mvn deploy

# Once the bits are pushed and made visible, you just need to change v3/pom.xml <hk2.version> property
# and GFv3 will pick up the new version of HK2.

# Boy, Maven sucks!
