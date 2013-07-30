## Run Level Service

The HK2 run level service allows you to automatically bring your system up and down in
an orderly fashion.  Run level services are annotated to come up or down at a specific
level.  The user then sets the current level of the system and the run level service
ensures that all services at that level and below are started.

+ [Definitions](guice-bridge.html#Definitions)
+ [Injecting Guice services into HK2 services](guice-bridge.html#Injecting_Guice_services_into_HK2_services)
+ [Injecting HK2 services into Guice services](guice-bridge.html#Injecting_HK2_services_into_Guice_services)
+ [Bi-Directional HK2 Guice Bridge](guice-bridge.html#Bi-Directional_HK2_Guice_Bridge)

### Creating a RunLevel service

A RunLevel service is simply a service that is in the [RunLevel][runlevel] scope.  Here is an
example of a RunLevel service:
 
```java
  @RunLevel(value=5)
  public class LoggingRunLevelService {
  }
```java

The LoggingRunLevelService will get started when the run level is 5 or higher, and will be shutdown
if the run level is dropped to 4 or lower.  The following service is started at level 10:

```java
  @RunLevel(value=10)
  public class SecurityRunLevelService {
  }
```java

All the services in one level are started prior to any service in another level being started.
If the system is going down rather than up, all services in a level will be stopped before
services in a lower level are stopped.

### The RunLevelController

The [RunLevelController][runlevelcontroller] is a service implemented by the HK2 RunLevel
feature.  It can tell you the current run level of the system, and it can be used to
change the current run level.  You can either use the asynchronous or synchronous version
to change the level.  The only difference is whether or not you want to block the current
thread waiting for the services to finish. 



[runlevel]: apidocs/org/glassfish/hk2/runlevel/RunLevel.html
[runlevelcontroller]: apidocs/org/glassfish/hk2/runlevel/RunLevelController.html