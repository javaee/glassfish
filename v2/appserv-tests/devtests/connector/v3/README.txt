To run connector devtests in GlassFish v3

- set S1AS_HOME, APS_HOME as appropriate
- Set CLASSPATH to contain javax.resource.jar. export CLASSPATH=$S1AS_HOME/modules/javax.resource.jar:$CLASSPATH
- start GlassFish
- use "ant startDerby" to start derby via appserv-tests (APS_HOME) target so that a stored procedure needed by connector test (cci, cci-embedded)
  is available
- ant all
- results can be found at APS_HOME/test_results.html
- stop GlassFish
- ant stopDerby
