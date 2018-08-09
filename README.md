#### :warning:This project is now part of the EE4J initiative. The activity on this project has been paused while it is being migrated to the Eclipse Foundation. See [here](https://www.eclipse.org/ee4j/status.php) for the overall EE4J transition status.
 
---
GlassFish Server
=================
https://javaee.github.io/glassfish

GlassFish is the reference implementation of Java EE.

Building
--------

Prerequisites:

* JDK8+
* Maven 3.0.3+

Run the full build:

`mvn install`

Locate the Zip distributions:
- appserver/distributions/glassfish/target/glassfish.zip
- appserver/distributions/web/target/web.zip

Locate staged distributions:
- appserver/distributions/glassfish/target/stage
- appserver/distributions/web/target/stage

Starting GlassFish
------------------

`glassfish5/bin/asadmin start-domain`

Stopping GlassFish
------------------

`glassfish5/bin/asadmin stop-domain`
