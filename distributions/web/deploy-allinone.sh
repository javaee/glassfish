#!/bin/sh
# I first did this by making an attached artifact, but the problem is that an attached artifact shares POM with the main artifact,
# and therefore ends up pulling in all the dependencies back again.
# So I'm still thinking about what the proper delivery mechanism for this is.
#
# in the mean time, separate deploy:deploy-file allows a jar without any non-SNAPSHOT dependency,
# which enables releases of other artifacts that depend on this.
mvn deploy:deploy-file -Durl=dav:http://rator.sfbay/maven/repositories/glassfish/ -DrepositoryId=rator.sfbay -Dfile=target/all.jar -DgroupId=org.glassfish.distributions -DartifactId=web-all -Dversion=10.0-build-$(date +%Y%m%d) -Dpackaging=jar
