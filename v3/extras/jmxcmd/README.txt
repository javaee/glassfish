#--- Building 'mx' ---

# build it
mvn install

# create the bin directory with everything in it
# this should be added to pom.xml, build help anyone?
# note that the script assumes that V3 modules are in /v3/glassfishv3/glassfish/modules
./mk

#--- Dependency requirements (post-build) ---

# Requires amx-api.jar from GlassFish V3, other jars optional
export V3=/v3/glassfishv3/glassfish
cp $V3/modules/amx-core.jar ./publish/jars
# and others

########################### Examples

# Connecting to a JMX server over the RMI connector
./publish/mx   # start in interactive mode
connect --port 8686 --jndi-name /jmxrmi --protocol rmi local-rmi

# show all MBeans in jmx domain 'amx'
mx> find amx:

# show all MBeans having property 'j2eeType' equal to X-HTTPListenerConfig 
mx> find j2eeType=X-HTTPListenerConfig

# target MBeans with property 'j2eeType=X-DomainRoot'
mx> target j2eeType=X-DomainRoot

# view MBeanInfo
mx> inspect

# get help
mx> help



