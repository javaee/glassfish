[//]: # ( DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. )
[//]: # (  )
[//]: # ( Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved. )
[//]: # (  )
[//]: # ( The contents of this file are subject to the terms of either the GNU )
[//]: # ( General Public License Version 2 only ("GPL") or the Common Development )
[//]: # ( and Distribution License("CDDL") (collectively, the "License").  You )
[//]: # ( may not use this file except in compliance with the License.  You can )
[//]: # ( obtain a copy of the License at )
[//]: # ( https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html )
[//]: # ( or packager/legal/LICENSE.txt.  See the License for the specific )
[//]: # ( language governing permissions and limitations under the License. )
[//]: # (  )
[//]: # ( When distributing the software, include this License Header Notice in each )
[//]: # ( file and include the License file at packager/legal/LICENSE.txt. )
[//]: # (  )
[//]: # ( GPL Classpath Exception: )
[//]: # ( Oracle designates this particular file as subject to the "Classpath" )
[//]: # ( exception as provided by Oracle in the GPL Version 2 section of the License )
[//]: # ( file that accompanied this code. )
[//]: # (   )
[//]: # ( Modifications: )
[//]: # ( If applicable, add the following below the License Header, with the fields )
[//]: # ( enclosed by brackets [] replaced by your own identifying information: )
[//]: # ( "Portions Copyright [year] [name of copyright owner]" )
[//]: # (  )
[//]: # ( Contributor(s): )
[//]: # ( If you wish your version of this file to be governed by only the CDDL or )
[//]: # ( only the GPL Version 2, indicate your decision by adding "[Contributor] )
[//]: # ( elects to include this software in this distribution under the [CDDL or GPL )
[//]: # ( Version 2] license."  If you don't indicate a single choice of license, a )
[//]: # ( recipient has the option to distribute your version of this file under )
[//]: # ( either the CDDL, the GPL Version 2 or to extend the choice of license to )
[//]: # ( its licensees as provided above.  However, if you add GPL Version 2 code )
[//]: # ( and therefore, elected the GPL Version 2 license, then the option applies )
[//]: # ( only if the new code is made subject to such option by the copyright )
[//]: # ( holder. )

## Extensibility


### Compatibility

This page describes the features of the HK2 2.0 API.  The Habitat API
from version 1.x of HK2 has been replaced with a new interface called
[ServiceLocator][servicelocator]. More information can be found [here][apioverview].

### Features of HK2

HK2 has many features for customizing the system.  This page is intended to give an
overview of each feature.  Among the set of HK2 features are:

+ [Adding a Scope and Context to the system](extensibility.html#Adding_a_Scope_and_Context_to_the_system)
+ [PerThread Scope](extensibility.html#PerThread_Scope)
+ [InheritableThread Scope](extensibility.html#InheritableThread_Scope)
+ [Security](extensibility.html#Security)
+ [Proxies](extensibility.html#Proxies)
+ [ClassLoading](extensibility.html#ClassLoading)
+ [Custom Injection Resolvers](extensibility.html#Custom_Injection_Resolvers)
+ [Just in Time Injection Resolver](extensibility.html#Just_in_Time_Injection_Resolver)
+ [Events](extensibility.html#Events)
+ [Instance Lifecycle](extensibility.html#Instance_Lifecycle)
+ [Interception](extensibility.html#Interception)
+ [Dynamic Configuration Listeners](extensibility.html#Dynamic_Configuration_Listeners)
+ [Class Analysis](extensibility.html#Class_Analysis)
+ [Run Level Services](extensibility.html#Run_Level_Services)
+ [Self Descriptor Injection](extensibility.html#Self_Descriptor_Injection)
+ [ServiceLocator to ServiceLocator Bridge](extensibility.html#ServiceLocator_to_ServiceLocator_Bridge)
+ [Error Handling](extensibility.html#Error_Handling)

### Events

It is possible to send messages from one service to another using the HK2 event feature.  The event feature is allows for unrelated
services to message each other without prior coordination (other than on the Type of event).  It is a pluggable event service,
which allows for user defined qualities of service between the publishers and subscribers.

The HK2 event service is described fully [here][events].

An example of plugging in a different event distributor can be found [here][threaded-events-example].

### Adding a Scope and Context to the system

In HK2 a [Context][context] is a class that is used to control the lifecycle of service instances.  A [Scope][scope] is an annotation that is put onto another
annotation that is used to associate any service with a particular [Context][context].  All services
in HK2 are associated with a single scope.

There are two system provided scope/context pairs.  The default [Scope][scope] for services annotated with [@Service][service] is the
[Singleton][singleton] scope.  Service instances in the [Singleton][singleton] scope are created once and are never destroyed.

The default [Scope][scope] for services bound with the [DynamicConfiguration][dynamicconfiguration] bind call is [PerLookup][perlookup].
Service instances in the [PerLookup][perlookup] scope are created every time that the service is injected or looked up via the API.
These instances are destroyed when the [ServiceHandle][servicehandle] destroy method is called on any service that has injected a [PerLookup][perlookup] object.

Any number of other scope/context pairs can be added to the system.  In order to do so, the user must write
an implementation of [Context][context] where the parameterized type of the [Context][context] is the annotation annotated with [Scope][scope] that the [Context][context] is handling.
This implementation of [Context][context] is then bound into the [ServiceLocator][servicelocator] like any other service.

To make this more clear, we have two examples of user scope/context pairs:

- This [example][ctm-example] adds a context that is based on the current running tenant.
- This [example][custom-resolver-example] adds a request scoped context.

### PerThread Scope

There is a per-thread scope/context pair optionally supported in HK2.
Services marked with [PerThread][perthread] have their life cycle defined by the thread they are on.
Two different threads injecting a service from the [PerThread][perthread] scope will get different objects.
Two objects on the same thread injecting a [PerThread][perthread] scope service will get the same object.

The [PerThread][perthread] scope can be added to any [ServiceLocator][servicelocator] by using the method [enablePerThreadScope][enableperthreadscope]

### InheritableThread Scope

There is a inheritable thread scope/context pair optionally supported in HK2.
Services marked with [InheritableThread][inheritablethread] are similar to PerThread scoped services with one caveat,
their life cycle defined by the thread they are on and are inherited by its child threads.
Two different threads injecting a service from the [InheritableThread][inheritablethread] scope will get different objects.
Two objects on the same thread injecting a [InheritableThread][inheritablethread] scope service will get the same object.
Two objects on a parent and child thread injecting a [InheritableThread][inheritablethread] scope service will get the same object.

The [InheritableThread][inheritablethread] scope can be added to any [ServiceLocator][servicelocator] by using the method [enableInheritableThreadScope][enableInheritableThreadScope]

### Immediate Scope

There is an Immediate scope/context pair optionally supported in HK2.
Services marked with [Immediate][immediate] will be started as soon as their
[ActiveDescriptors][activedescriptor] are added to the [ServiceLocator][servicelocator].  They are destroyed when
their [ActiveDescriptors][activedescriptor] are removed from the [ServiceLocator][servicelocator].
[Immediate][immediate] services are started and stopped on an independent thread.  Users of this
feature can also register implementations of [ImmediateErrorHandler][immediateerrorhandler] in order
to catch errors thrown by [Immediate][immediate] services.

Care should be taken with the Injection points of an [Immediate][immediate] service, as they will implicitly
get created immediately in order to satisfy the dependencies.  Since [Immediate][immediate] services
are created using an independent thread there is no guarantee that [Immediate][immediate] services
will be started before or after any other service.  The only guarantee is that [Immediate][immediate]
services will eventually get started.  Normally they get started very quickly after being added to
the [ServiceLocator][servicelocator].

The [Immediate][immediate] scope can be added to any [ServiceLocator][servicelocator] by using
the method [enableImmediateScope][enableimmediatescope].  It is important to notice that
[enableImmediateScope][enableimmediatescope] must be called on all [ServiceLocators][servicelocator]
who will have [Immediate][immediate] services bound in them.  In particular it is NOT sufficient to
call [enableImmediateScope][enableimmediatescope] on the parent of a [ServiceLocators][servicelocator],
since this implementation will only automatically detect [Immediate][immediate] services directly added
to the [ServiceLocators][servicelocator] given to the [enableImmediateScope][enableimmediatescope]
method.

### Proxies

Rather than injecting an instance of a service itself, HK2 can also inject a proxy to that service.  There are a few
reasons that you might want to use proxies.  One reason is because the lifeycle of two different scopes may be
different.  For example, you might have something like a RequestScoped scope, and you would like to inject it
into a Singleton scoped object.  But the Singleton scoped object is only injected once, and the RequestScoped service
will be changing every time the Request has changed.  This can be solved by injecting a proxy into the
Singleton scoped object.  Then every time the Singleton scoped service uses the RequestScoped service the proxy
will make sure to use the real RequestScoped service that is appropriate for the current request.

Another reason you might want to use a proxy for a service is if the service is extremely expensive to create, and
if possible you want to delay the creation until the service is actually used by the caller.  In fact, if the caller
never invokes on the proxy, it is possible the service will never get started!  This can be done by injecting a
proxy into a service rather than the real service.  The proxy will not attempt to create the service until some method
of that proxy is invoked.

All proxies created by HK2 will also implement [ProxyCtl][proxyctl].
[ProxyCtl][proxyctl] can be used to force the creation of the underlying service without calling any of the methods of that service.
Of course every service that is to be proxied must be proxiable, so the service to be proxied must either be an interface or a class that is not declared final,
has no final fields or methods and has a public zero-argument constructor.  In general it is better to proxy interfaces
rather than classes.

In order to have HK2 create a proxy for your service rather than the service itself you can create a proxiable scope.
A proxiable scope is just like a normal scope, except that the scope annotation is also annotated with [Proxiable][proxiable].
All services injected or looked up from this scope will be given a proxy rather than the real service.

This is an example of a proxiable scope:

```java
@Scope
@Proxiable
@Retention(RUNTIME)
@Target( { TYPE, METHOD })
public @interface ProxiableSingleton {
}
```java

While normally every service in a proxiable scope is proxiable, you can override the default proxying behavior
on a per-service basis.  This is also true for services in non-proxiable scopes.  For example you can make
a service that is in Singleton scope (which is not proxiable) be proxied.
You do this by setting the field [isProxiable][isproxiable].
If that method returns null then that service will use the scopes mode when it comes to proxying.
If that method returns non-null then the system will either proxy or not proxy based on the returned value.
Classes that are automatically analyzed can also use the [UseProxy][useproxy] annotation to indicate explicitly
whether or not they should be proxied.  This is a service in Singleton scope that will be proxied:

```java
@Singleton @UseProxy
public class SingletonService {
}
```java

This is a service in the ProxiableSingleton scope that will NOT be proxied (even though ProxiableSingleton is
a Proxiable scope):

```java
@ProxiableSingleton @UseProxy(false)
public class AnotherService {
}
```java

#### Proxying within the same scope

By default if a service is proxiable then it will be proxied even when being injected into other services within the same scope.
This allows for the lazy use case.  However, it is sometimes the case that it is counter-productive to proxy services when
they are injected into other services of the same scope.  HK2 supports Proxiable scopes that do NOT proxy services when they
are being injected into the same scope.  The [Proxiable][proxiable] annotation has a field called proxyForSameScope that by default is true but which can be set to false.
The following scope is a proxiable scope where services injected into other services in the same scope will not be proxied:

```java
@Scope
@Proxiable(proxyForSameScope=false)
@Retention(RUNTIME)
@Target( { TYPE, METHOD })
public @interface RequestScope {
}
```java

 Individual descriptors can also explicity set whether or not they should be proxied for other services in the same
 scope by setting the [isProxyForSameScope][isproxyforsamescope] value.
 This value can also be set when using automatic class analysis by using the [ProxyForSameScope][proxyforsamescope].  The following
 service is in the ProxiableSingelton scope which would normally not proxy when being injected into the same scope, but
 which in this case WILL be proxied even when injected into another service in the same scope:

```java
@RequestScope @ProxyForSameScope
public class ExpensiveRequestService {
}
```java

### ClassLoading

Classloading is an interesting challenge in any Java environment.  HK2 defers classloading as long as possible, but at some
point, it must get access to the true class in order to create and inject instances.  At that moment, HK2 will attempt
to reify the descriptor, using the [ServiceLocator][servicelocator] reify method.

Every [Descriptor][descriptor] bound into the system has an associated [HK2Loader][hk2loader].
If the getLoader method of [Descriptor][descriptor] returns null, then the system defined algorithm
for loading classes will be used.  Otherwise, the given [HK2Loader][hk2loader] will be used to load the class described by this [Descriptor][descriptor].

The system algorithm used when the getLoader method of [Descriptor][descriptor] returns null is to first consult the classloader of the class being injected into, if available.
If not available, HK2 will use the classloader that loaded HK2 itself.
Failing this, the class will fail to be loaded and an exception will be thrown.

Note that since the user is providing an implementation of [HK2Loader][hk2loader]
rather than a java.lang.ClassLoader that it is possible to delay the instantiation of the underlying ClassLoader until
the [Descriptor][descriptor] is being reified.  It might also be possible to have the implementation of [HK2Loader][hk2loader] consult several underlying ClassLoaders,
or construct the class dynamically using weaving or some other class building technology.
The mind boggles at all the ways [HK2Loader][hk2loader] can be implemented.

### Custom Injection Resolvers

By default the system provides JSR-330 standard injection.
That means honoring [@Inject][javaxinject] and all other parts of the JSR-330 specification.
However, it is sometimes the case that a user would like to customize the JSR-330 resolution in some manner,
or provide their own injection points based on a different annotation.

In order to do so, the user implements [InjectionResolver][injectionresolver].
The parameterized type of the [InjectionResolver][injectionresolver] must be the injection annotation that they will resolve.
The user implementation of [InjectionResolver][injectionresolver] is then bound into a [ServiceLocator][servicelocator] like any other service.

Annotations to be used as injection points can optionally be annotated
with [InjectionPointIndicator][injectionpointindicator].  This annotation
allows automatic analysis of classes using the custom [InjectionResolver][injectionresolver].

This [example][custom-resolver-example] adds a custom injection resolver that customizes the default JSR-330 injection resolver.

### Just in Time Injection Resolver

There are times when the set of services to be injected is not completely known before the system is booted.  In
these cases it may be useful to use a [Just In Time Injection Resolver][justintimeinjectionresolver].  The
[Just In Time Injection Resolver][justintimeinjectionresolver] is an hk2 service that is called whenever the
system cannot find a suitable service for a given [Injection Point][injectee].  If the
[Just In Time Injection Resolver][justintimeinjectionresolver] service knows how to find the service then
it can add the service to the ServiceLocator and tell hk2 to look again for the service.  A good example
of using this would be when retrieving services from a remote system, or from some other service oriented
system such as OSGi, Spring or Guice.

### Security

Certain operations that are performed by the users of HK2 can be validated.  Validation can either
allow or deny the operation in question.  The operations that can be validated are adding a
service to the [ServiceLocator][servicelocator], removing a service from the [ServiceLocator][servicelocator],
injecting a service into another service or looking up a service from the [ServiceLocator][servicelocator].
This feature is most often used in secure use-cases, but has applicability for other use-cases
as well.  To use validation the user registers an implementation of [ValidationService][validationservice]
with the [ServiceLocator][servicelocator] whose operations are to be validated.

There is an example example of how the [ValidationService][validationservice] can be used to do a complete
security lockdown of the system.  This example runs with the J2SE security manager turned on and
grants some privileges to some projects and other privileges to other projects to ensure that
the [ValidationService][ValidationService] can be used to define the security of the system.

The example can be seen [here][security-lockdown-example-runner].

### Instance Lifecycle

A user may register an implementation of [InstanceLifecycleListener][instancelifecyclelistener] to be notified whenever an instance of a service is created.
Unlike the [ValidationService][validationservice], which deals only with the metadata of a service,
the [InstanceLifecycleListener][instancelifecyclelistener] is notified whenever an instance
of a service is created or destroyed.  This is a useful facility for tracing or for scenarios where a service wishes to become
an automatic listener for anything that it is injected into.

### Interception

[AOP Alliance][aopalliance] method and constructor interception is supported by HK2.  Methods and constructors that are to be
intercepted are identified using instances of the HK2 [InterceptionService][interceptionservice].  An example of
how to use the [InterceptionService][interceptionservice] can be found [here][aopexample].

There is an HK2 provided default implementation of the [InterceptionService][interceptionservice] which uses
annotations to indicate services that should be intercepted, those that should do the intercepting and to
bind intercepted methods and constructors to their interceptors.  Information about the HK2 provided
default implementation of the [InterceptionService][interceptionservice] can be found [here][aopdefault].

### Dynamic Configuration Listeners

A user may register an implementation of [DynamicConfigurationListener][dynamicconfigurationlistener] to be notified  whenever
the set of [ActiveDescriptors][activedescriptor] in a [ServiceLocator][servicelocator] has changed.  The
[DynamicConfigurationListener][dynamicconfigurationlistener] must be in the [Singleton][singleton] scope.

### Class Analysis

HK2 often needs to look at a java class in order to find things about that class such as its set
of constructors, methods or fields.  The choices HK2 makes is usually determined by specifications
such as JSR-330 or JSR-299.  However, in some cases different specifications make different choices,
or the user of the HK2 system may have some other scheme it would like to use in order to
select the parts of class which HK2 should manipulate.  For example, the JAX-RS specification
requires the system to choose the constructor with the largest number of parameters (by default)
while the JSR-299 specification requires the system to choose the zero-argument constructor
or else fail.

The HK2 system allows the user to register named implementation of the [ClassAnalyzer][classanalyzer]
in order to modify or completely replace the constructors, fields and methods HK2 would choose.
Individual HK2 [Descriptors][descriptor] can set the name of the [ClassAnalyzer][classanalyzer]
that should be used to analyze the implementation class.

HK2 always adds an implementation of [ClassAnalyzer][classanalyzer] with the name "default" that implements
the JSR-299 style of selection.

### Run Level Services

If your system has sets of services that need to come up and down in an orderly fashion consider
using the HK2 Run Level Services.  The Run Level Service allows one to specify levels at
which services come up and down and will bring these services up and down when the system run level
has changed.

Learn more about Run Level Services [here][runlevelservices].

### Self Descriptor Injection

Any service can have its own [ActiveDescriptor][activedescriptor] injected into itself.  One use case for
this is when you have a common set of services that all share the same super class.  The super class can
self inject the [ActiveDescriptor][activedescriptor] and then use that to do further generic processing
of the service.  To self inject the [ActiveDescriptor][activedescriptor] for your service use the
[Self][self] annotation on a field or on a parameter of your constructor or initializer method.  Here is an example:

```java
public abstract class GenericService {
  @Inject @Self
  private ActiveDescriptor<?> myOwnDescriptor;
}
```java

### ServiceLocator to ServiceLocator Bridge

It is possible to import all the [NORMAL] services from one ServiceLocator into
another ServiceLocator as long as the locators do not have a parent/child relationship.  You do
this be creating a ServiceLocator bridge between the two ServiceLocators.  This is done using
the [bridgeServiceLocator][bridgeservicelocator] method in the optional hk2-extras module.

Bridges are one-way and dynamic.  Suppose you have a ServiceLocator named Foo and have bridged
its services into the Bar ServiceLocator.  If you add a service to the Foo ServiceLocator that service
will be locatable in the Bar ServiceLocator, even if the service is added after the bridge was
created.

Cycles between ServiceLocators are supported.  It will appear that all services from all ServiceLocators
are available in all of the ServiceLocators involved in the cycle.  For example, if Foo and Bar
service locators have bridges going in both directions then it will appear that Foo and Bar
have the same set of [NORMAL] services.

A bridge between two service locators is torn down by using the method
[unbridgeServiceLocator][unbridgeservicelocator] or by shutting down the ServiceLocator
supplying the services.

### Error Handling

Errors can pop up in various phases of the HK2 service lifecycle.  Users can register implementations of the
[ErrorService][errorservice] in order to be notified when errors occur.  There are currently four types of
errors that the system sends to the [ErrorService][errorservice]:

+ FAILURE_TO_REIFY:  When there has been a problem classloading a service
+ DYNAMIC_CONFIGURATION_FALURE: When a dynamic update to the set of services fails
+ SERVICE_CREATION_FALURE: When a service fails during creation
+ SERVICE_DESTRUCTION_FAILURE: When a service fails during destruction

Using the [ErrorService][errorservice] can be a convenient place to standardize on logging of service failures.

[apioverview]: api-overview.html
[servicelocator]: apidocs/org/glassfish/hk2/api/ServiceLocator.html
[context]: apidocs/org/glassfish/hk2/api/Context.html
[servicehandle]: apidocs/org/glassfish/hk2/api/ServiceHandle.html
[perlookup]: apidocs/org/glassfish/hk2/api/PerLookup.html
[perthread]: apidocs/org/glassfish/hk2/api/PerThread.html
[enableperthreadscope]: apidocs/org/glassfish/hk2/utilities/ServiceLocatorUtilities.html#enablePerThreadScope(org.glassfish.hk2.api.ServiceLocator)
[inheritablethread]: apidocs/org/glassfish/hk2/api/InheritableThread.html
[enableinheritablethreadscope]: apidocs/org/glassfish/hk2/utilities/ServiceLocatorUtilities.html#enableInheritableThreadScope(org.glassfish.hk2.api.ServiceLocator)
[enableimmediatescope]: apidocs/org/glassfish/hk2/utilities/ServiceLocatorUtilities.html#enableImmediateScope(org.glassfish.hk2.api.ServiceLocator)
[immediateerrorhandler]: apidocs/org/glassfish/hk2/utilities/ImmediateErrorHandler.html
[service]: apidocs/org/jvnet/hk2/annotations/Service.html
[dynamicconfiguration]: apidocs/org/glassfish/hk2/api/DynamicConfiguration.html
[scope]: http://docs.oracle.com/javaee/6/api/javax/inject/Scope.html
[singleton]: http://docs.oracle.com/javaee/6/api/javax/inject/Singleton.html
[ctm-example]: ctm-example.html
[proxyctl]: apidocs/org/glassfish/hk2/api/ProxyCtl.html
[proxiable]: apidocs/org/glassfish/hk2/api/Proxiable.html
[isproxiable]: apidocs/org/glassfish/hk2/api/Descriptor.html#isProxiable()
[useproxy]: apidocs/org/glassfish/hk2/api/UseProxy.html
[isproxyforsamescope]: apidocs/org/glassfish/hk2/api/Descriptor.html#isProxyForSameScope()
[proxyforsamescope]: apidocs/org/glassfish/hk2/api/ProxyForSameScope.html
[descriptor]: apidocs/org/glassfish/hk2/api/Descriptor.html
[hk2loader]: apidocs/org/glassfish/hk2/api/HK2Loader.html
[javaxinject]: http://docs.oracle.com/javaee/6/api/javax/inject/Inject.html
[injectionresolver]: apidocs/org/glassfish/hk2/api/InjectionResolver.html
[injectionpointindicator]: apidocs/org/glassfish/hk2/api/InjectionPointIndicator.html
[validationservice]: apidocs/org/glassfish/hk2/api/ValidationService.html
[security-lockdown-example-runner]: security-lockdown-example-runner.html
[instancelifecyclelistener]: apidocs/org/glassfish/hk2/api/InstanceLifecycleListener.html
[classanalyzer]: apidocs/org/glassfish/hk2/api/ClassAnalyzer.html
[custom-resolver-example]: custom-resolver-example.html
[runlevelservices]: runlevel.html
[activedescriptor]: apidocs/org/glassfish/hk2/api/ActiveDescriptor.html
[dynamicconfigurationlistener]: apidocs/org/glassfish/hk2/api/DynamicConfigurationListener.html
[immediate]: apidocs/org/glassfish/hk2/api/Immediate.html
[self]: apidocs/org/glassfish/hk2/api/Self.html
[aopalliance]: http://aopalliance.sourceforge.net/
[interceptionservice]: apidocs/org/glassfish/hk2/api/InterceptionService.html
[aopexample]: aop-example.html
[aopdefault]: aop-default.html
[events]: events.html
[threaded-events-example]: threaded-events-example.html
[bridgeservicelocator]: apidocs/org/glassfish/hk2/extras/ExtrasUtilities.html#bridgeServiceLocator(org.glassfish.hk2.api.ServiceLocator,org.glassfish.hk2.api.ServiceLocator)
[errorservice]: apidocs/org/glassfish/hk2/api/ErrorService.html
[unbridgeservicelocator]: apidocs/org/glassfish/hk2/extras/ExtrasUtilities.html#unbridgeServiceLocator(org.glassfish.hk2.api.ServiceLocator,org.glassfish.hk2.api.ServiceLocator)
[NORMAL]: apidocs/org/glassfish/hk2/api/DescriptorVisibility.html#NORMAL
[justintimeinjectionresolver]: apidocs/org/glassfish/hk2/api/JustInTimeInjectionResolver.html
[injectee]: apidocs/org/glassfish/hk2/api/Injectee.html
