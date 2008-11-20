#--- Building ---

# build it
mvn install

# create the publish directory with everything in it
# this should be added to pom.xml, build help anyone?
./make-jmxcmd.sh

#--- Dependency requirements (post-build) ---

# Requires amx-api.jar from GlassFish V3, other jars optional
export V3=/v3/glassfish
cp $V3/modules/amx-api.jar ./publish/jars


# Connecting to a JMX server over the RMI connector
./publish/jmxcmd   # start in interactive mode
connect --port 8686 --jndi-name /jmxrmi --protocol rmi local-rmi

# show all MBeans in jmx domain 'amx'
jmxcmd> find amx:

# show all MBeans having property 'j2eeType' equal to X-HTTPListenerConfig 
jmxcmd> find j2eeType=X-HTTPListenerConfig

# target MBeans with property 'j2eeType=X-DomainRoot'
jmxcmd> target j2eeType=X-DomainRoot

# view MBeanInfo
jmxcmd> inspect

# get help
jmxcmd> help



