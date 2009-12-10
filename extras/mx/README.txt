
# Examples

# Connecting to a JMX server over the RMI connector
mx
mx> connect --port 8686 local-rmi

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



