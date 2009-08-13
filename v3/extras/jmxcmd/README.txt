#--- Building ---

# build it
mvn install

# create the publish directory with everything in it
# this should be added to pom.xml, build help anyone?
# note that the script assumes that V3 modules are in /v3/glassfishv3/glassfish/modules
./publish.sh

#--- Dependency requirements (post-build) ---

# Requires amx-api.jar from GlassFish V3, other jars optional
export V3=/v3/glassfish
cp $V3/modules/amx-api.jar ./publish/jars

########################### Examples
# See  docs/index.html

# Connecting to a JMX server over the RMI connector
./publish/mc   # start in interactive mode
connect --port 8686 --jndi-name /jmxrmi --protocol rmi local-rmi

# show all MBeans in jmx domain 'amx'
mx> find amx:

# show all MBeans having property 'j2eeType' equal to X-HTTPListenerConfig 
mc> find j2eeType=X-HTTPListenerConfig

# target MBeans with property 'j2eeType=X-DomainRoot'
mc> target j2eeType=X-DomainRoot

# view MBeanInfo
mc> inspect

# get help
mc> help



