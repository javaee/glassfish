## Run Level Service

The HK2 run level service allows you to automatically bring your system up and down in
an orderly fashion.  Run level services are annotated to come up or down at specific
levels.  The user then sets the current level of the system and the run level service
ensures that all services at that level and below are started.

+ [Creating a RunLevel service](runlevel.html#Creating_a_RunLevel_service)
+ [The RunLevelController](runlevel.html#The_RunLevelController)
+ [The RunLevelListener](runlevel.html#The_RunLevelListener)
+ [Multi Threading](runlevel.html#Multi_Threading)
+ [Ordering](runlevel.html#Ordering)
+ [Other Goodies](runlevel.html#Other_Goodies)

### Creating a RunLevel service

A RunLevel service is simply a service that is in the [RunLevel][runlevel] scope.  RunLevel services
normally have a @PostConstruct or @PreDestroy method to be called when the service is started or
stopped (but this is not required).Here is an example of a RunLevel service:
 
```java
  @Service @RunLevel(value=5)
  public class LoggingRunLevelService {
    @PostConstruct
    private void startMe() {}
    
    @PreDestroy
    private void stopMe() {}
  }
```java

The LoggingRunLevelService will get started when the run level is 5 or higher, and will be shutdown
if the run level is dropped to 4 or lower.  

The following service is started at level 10:

```java
  @Service @RunLevel(value=10)
  public class SecurityRunLevelService {
    @PostConstruct
    private void startMe() {}
    
    @PreDestroy
    private void stopMe() {}
  }
```java

All the services in one level are started prior to any service in another level being started.
If the system is going down rather than up, all services in a level will be stopped before
services in a lower level are stopped.

The implementation of [Context][context] that goes along with the [RunLevel][runlevel] scope
is provided by the RunLevel feature.

### The RunLevelController

The [RunLevelController][runlevelcontroller] is a service implemented by the HK2 RunLevel
feature.  It can tell you the current run level of the system, and it can be used to
change the current run level.  You can use the asynchronous or synchronous methods
to change the level.  The only difference is whether or not you want to block the current
thread waiting for the services to finish. 

The synchronous API to change levels is proceedTo.  You can use it like this:

```java
  public class MyClass {
    @Inject
    private RunLevelController controller;
    
    public void changeRunLevel(int level) {
        controller.proceedTo(level);
    }
  }
```java

This code will either bring the system up or down to the specified level while blocking
the thread.  If you do not want to block the thread, you can use the asynchronous version
of the API, which returns a [RunLevelFuture][runlevelfuture] that can either be polled
or waited on at a later time.

```java
  public class MyClass {
    @Inject
    private RunLevelController controller;
    
    public RunLevelFuture changeRunLevelAsync(int level) {
        return controller.proceedToAsync(level);
    }
  }
```java

There are also methods on the [RunLevelFuture][runlevelfuture] which allows the caller
to determine what level the system is proceeding to, and whether or not the current
job is going up in level or going down.

In general if there is an existing RunLevel job in progress another one cannot be
started and existing job cannot be changed.  A running job can be cancelled at any time.
A [RunLevelListener][runlevellistener] can be used to change the proceedTo value of a
RunLevel job.

### The RunLevelListener

The [RunLevelListener][runlevellistener] is an interface implemented by users of the RunLevel
service if they want to perform actions in error situations or at the end of a level.  The
implementations must be put into hk2 as services.  All implementations of
[RunLevelListener][runlevellistener] registered with HK2 will be called.

In the following example the [RunLevelListener][runlevellistener] logs all
events that happen.

```java
  @Service
  public class RunLevelListenerLogger implements RunLevelListener {
  
    public void onProgress(ChangeableRunLevelFuture currentJob,
            int levelAchieved) {
        Logger.log("Achieved level: " + levelAchieved);
    }
    
    public void onCancelled(RunLevelFuture currentJob,
            int levelAchieved) {
        Logger.log("Cancelled at level: " + levelAchieved);

    }

    public void onError(RunLevelFuture currentJob, ErrorInformation info) {
        Logger.log("Error while progressing to level: " + currentJob.getProposedLevel(),
            info.getError());
    }
  }
```java

The onProgress callback can be used to change the level the current job is proceeding to
using the changeProposedLevel method of the passed in [ChangeableRunLevelFuture][changeablerunlevelfuture].

In the onError callback the [ErrorInformation][errorinformation] parameter can be
used to get the error that was thrown by the service.  It can also be used
to tell the RunLevel service what to do about the error.  The choices are to
go down to the last fully achieved level and end the job there
(ErrorInformation.ErrorAction.GO_TO_NEXT_LOWER_LEVEL_AND_STOP), or to simply
ignore the error (ErrorInformation.ErrorAction.IGNORE).  When
the system is going up in level the default action is to go down to the
last fully achieved level and stop.  When the system is going down in
level the default action is to ignore the error.

If the onCancelled callback has been called then the job has been cancelled.  A new job
can be put into place, but the current job is considered to be complete.

### Multi Threading

The [RunLevelController][runlevelcontroller] will attempt to run all of the services
in a single run-level on parallel threads.  All dependencies will be honored as
always (if run-level service B depends on run-level service A then run-level service
A is guaranteed to be fully up before run-level service B is started).  By default
the [RunLevelController][runlevelcontroller] will use as many threads as there are services
in the level.  This can be controlled with the setMaximumUseableThreads method of
[RunLevelController][runlevelcontroller].

Multi-threading can be turned off by using the setThreadingPolicy method to set
the policy to RunLevelController.ThreadingPolicy.USE_NO_THREADS.
If this is done then an IllegalStateException will be thrown from the proceedToAsync
method, since in order to do asynchronous processing at least one thread must be used.  When
the policy is USE_NO_THREADS all services will be started on the thread
that calls the proceedTo method.  However, even in the USE_NO_THREADS mode a thread will
be used when bringing services down in order to ensure that they can be canceled in
the case of a hung service.

In some cases it is necessary to use specialized threads on which to run the run-level
services.  The [Executor][executor] that is used by the run level feature can be replaced
with the setExecutor method of the [RunLevelController][runlevelcontroller].  The
[Executor][executor] that is currently being used can be discovered with the getExecutor
method.

### Ordering

In general the run level feature feeds all the services in a certain level to the
set of threads based on the natural ordering given by hk2.  If only a single thread
is being used then that is the order services will run.  If multiple threads are
used then that is the order those services will be given to the threads.  Thus when
more than one thread is used the ordering in which services are started may be
indeterminate.

A user can affect the ordering with which the services are given to the threads by
implementing the [Sorter][sorter] service.  All implementations of [Sorter][sorter]
will be called with the results from the previous sort method of
[Sorter][sorter].  The final result will be the list of services given to the
set of threads executing run-level services.

### Other Goodies

When you give a service a RunLevel value like 10, that normally means that the service
should start at level 10, no sooner and no later.  In fact, by default if a service
at level 10 is started when the system level is less than 10 an error will be raised.  But
there are other times when what you want is for the RunLevel value to mean that the service
should be started at that level or lower.  This is useful in cases where it may be
necessary to start the service sooner than it would normally be started based on some
runtime characteristic of the system.  In that case you can set the mode of
the [RunLevel][runlevel] to RUNLEVEL_MODE_NON_VALIDATING.  Here is an example:

```java
  @RunLevel(value=10, mode=RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
  public class CanRunBeforeTenService {
  }
```java

HK2 is normally a demand driven system, meaning that services are not started until there
is an explicit lookup for a service (either via API or via injection).  The HK2 Run Level
Service allows [RunLevel][runlevel] services to be satisfaction driven, where the condition
for creating is the current run level of the system.  This is useful when building
processes that have a specific set of services that need to run on booting or shutting down.
The obvious application is for server-style processes, but it is also useful for setting
up things like connections or security in clients.

[runlevel]: apidocs/org/glassfish/hk2/runlevel/RunLevel.html
[runlevelcontroller]: apidocs/org/glassfish/hk2/runlevel/RunLevelController.html
[context]: apidocs/org/glassfish/hk2/api/Context.html
[runlevelfuture]: apidocs/org/glassfish/hk2/runlevel/RunLevelFuture.html
[runlevellistener]: apidocs/org/glassfish/hk2/runlevel/RunLevelListener.html
[changeablerunlevelfuture]: apidocs/org/glassfish/hk2/runlevel/ChangeableRunLevelFuture.html
[errorinformation]: apidocs/org/glassfish/hk2/runlevel/ErrorInformation.html
[executor]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executor.html
[sorter]: apidocs/org/glassfish/hk2/runlevel/Sorter.html