#!/bin/sh
#------------------------------------------------------   
#-- BE SURE TO HAVE THE FOLLOWING IN YOUR SETTINGS.XML
#------------------------------------------------------
#
#    <servers>
#        <server>
#            <id>jvnet-nexus-staging</id>
#            <username>jvnet_id</username>
#            <password>password</password>
#        </server>
#    </servers>
#    <profiles>
#      <profile>
#        <id>release</id>
#        <properties>
#          <release.arguments>-Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80 -Dgpg.passphrase=glassfish -Pjvnet-release</release.arguments>
#        </properties>
#        <activation>
#          <activeByDefault>false</activeByDefault>
#        </activation>
#      </profile>
#    </profiles>

# see the following URL for gpg issues
# https://docs.sonatype.org/display/Repository/How+To+Generate+PGP+Signatures+With+Maven#HowToGeneratePGPSignaturesWithMaven-GenerateaKeyPair

# login to nexus at maven.java.net and release (Close) the artifact
# https://maven.java.net/index.html#stagingRepositories

# More information:
# https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-8.ReleaseIt
# http://aseng-wiki.us.oracle.com/asengwiki/display/GlassFish/Migrating+Maven+deployment+to+maven.java.net

mvn -B release:prepare -Prelease
mvn -B release:perform -Prelease
