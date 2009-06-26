To run connector devtests in GlassFish v3

- set S1AS_HOME, APS_HOME as appropriate
- Set CLASSPATH to contain javax.resource.jar. export CLASSPATH=$S1AS_HOME/modules/javax.resource.jar:$CLASSPATH
- ant all
