
# Examples

# Connecting to a JMX server over the RMI connector
mx
mx> connect --port 8686 local-rmi

# show all MBeans in jmx domain 'amx'
mx> find amx:

# show all MBeans having property 'type' equal to network-listener 
mx> find type=network-listener

# target MBeans with property 'j2eeType=X-DomainRoot'
mx> target type=domain

# view all MBeans in domain 'amx' as a java interface
mx> java amx:

# view all MBeans in domain 'amx' as a fully doc'd java interface
mx> java --docs amx:

# get help
mx> help



