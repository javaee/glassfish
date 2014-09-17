## Default Interception Service Implementation

In the hk2-extras module there is a default implementation of the [InterceptionService][interceptionserivice] which
uses annotations to determine what services are to be intercepted and which interceptors should
be used.  This page describes this default implementation.

### Indicating Services to be Intercepted

All services to be intercepted by this implementation must be annotated with the
[Intercepted][intercepted] qualifier.  The [Intercepted][intercepted] qualifier may
only be placed on classes.  Here is an example of a service that can used method or
constructor interception when using the default [InterceptionService][interceptionserivice]
implementation:

```java
@Service
@Intercepted
public class MyService {
  // Can be intercepted
  @Inject
  public MyService(ServiceLocator locator) {
  }
  
  // Can be intercepted
  public boolean goEagles() {
    return true;
  }
}
```java

### Indicating Interceptors

A class that wishes to be used as a method or constructor interceptor must adhere to the
following rules:

+ must be an hk2 service
+ must implement either [MethodInterceptor][methodinterceptor] or [ConstructorInterceptor][constructorinterceptor]
+ must advertise [MethodInterceptor][methodinterceptor] or [ConstructorInterceptor][constructorinterceptor] as one of its contracts
+ must be annotated with the [Interceptor][interceptor] qualifier

Since [MethodInterceptor][methodinterceptor] and [ConstructorInterceptor][constructorinterceptor] are API from
the AopAlliance, they are not naturally marked with [Contract][contract].  Therefore it is often the case
when doing automatic analysis of contracts that the [ContractsProvided][contractsprovided] annotation
must be used to ensure that the standard AopAlliance interface is included in the set of
contracts.  Here is an example of a method interceptor:

```java
@Service
@Interceptor
@ContractsProvided({MyMethodInterceptor.class, MethodInterceptor.class})
public class MyMethodInterceptor implements MethodInterceptor {
  // ...
}
```java

Here is an example of a constructor interceptor:

```java
@Service
@Interceptor
@ContractsProvided({MyConstructorInterceptor.class, ConstructorInterceptor.class})
public class MyConstructorInterceptor implements ConstructorInterceptor {
  // ...
}
```java

### Interception Bindings

We must still be able to associate intercepted things such as constructors and methods of intercepted services
with the specific interceptors.  This is done by using the [InterceptionBinder][interceptionbinder]
annotation.  The [InterceptionBinder][interceptionbinder] is placed on user defined annotations to indicate
that the annotation is meant to associate an interceptor and the thing to be intercepted.

For example, if the user has a security interceptor, they might define their annotation like this:

```java
@Inherited
@InterceptionBinder
@Target({TYPE, METHOD, CONSTRUCTOR})
@Retention(RUNTIME)
@Documented
public @interface Secure {
}
```java

They would then put that annotation both on the security interceptor like so:

```java
@Service
@Interceptor
@ContractsProvided({ConstructorInterceptor.class, MethodInterceptor.class})
@Secure
public class SecurityInterceptor implements MethodInterceptor, ConstructorInterceptor {
  // ...
}
```java

Then any method or constructor on an intercepted service that needs to be secure
would add the @Secure annotation as well:

```java
@Service
@Intercepted
public class KernelSanders {

  @Secure
  public KernelSanders() {
    // Lets make some chicken!
  }

  @Secure
  public String getSecretFormula() {
    return secretFormula;
  }
}
```java

The user annotation that is marked with [InterceptionBinder][interceptionbinder] can
also be placed on the service that is intercepted in order to indicate that all
methods (and the construtor which hk2 will call) should be intercepted by interceptors
with matching annotations.  For example if the user annotation is this:

```java
@Inherited
@InterceptionBinder
@Target({TYPE, METHOD, CONSTRUCTOR})
@Retention(RUNTIME)
@Documented
public @interface Trace {
}
```java

Then the intercepted service can put Trace at the class level to indicate that all
methods should be traced:

```java
@Service
@Intercepted
@Trace
public class LoudService {
}
```java

If there is a corresponding interceptor that is marked with the Trace annotation then all
methods and/or constructors of the LoudService will be traced.

Also, annotations marked with [InterceptionBinder][interceptionbinder] are transitive.
So services marked with the following annotation will invoke interceptors with both the
Secure and Trace annotations:

```java
@Inherited
@InterceptionBinder
@Trace @Secure
@Target({TYPE, METHOD, CONSTRUCTOR})
@Retention(RUNTIME)
@Documented
public @interface SecurelyTraceable {
}
```java

### Interceptor Ordering and Customization

Normally interceptors will run in their natural HK2 ordering (based on Rank and serivce/locator id)
but sometimes it is convenient to re-order the interceptors based on some external configuration. 
It may also be convenient to add or remove interceptors that will run with some method or
constructor.  The default implementation of the interception service has defined its own plugin service
named the [InterceptorOrderingService][interceptororderingservice] that allows users to add, modify
or remove the set of intereceptors that would normally be run on a method or constructor.

The methods of the [InterceptorOrderingService][interceptororderingservice] are supplied with
the list of interceptors that would be run on the given Method or Constructor.  The
implementations of those methods can either return the list as-is (or return null) or they can
return their own list with a set of interceptors that should be run instead (and in what order).
If there are more than one implementation of the [InterceptorOrderingService][interceptororderingservice]
then all implementations are run in hk2 ranking order with the results of the previous service
being given to the next service.  In this way chains of interceptor ordering modifiers can be used together.

### Getting it all started

In order to enable the default implementation of the [InterceptionService][interceptionservice]
you will need to use the enableDefaultInterceptorServiceImplementation method of
the [ExtrasUtilities][extrasutilities] class.  You will also need to include the hk2-extras module
on your classpath.

[interceptionserivice]: apidocs/org/glassfish/hk2/api/InterceptionService.html
[intercepted]: apidocs/org/glassfish/hk2/extras/interception/Intercepted.html
[interceptor]: apidocs/org/glassfish/hk2/extras/interception/Interceptor.html
[methodinterceptor]: http://aopalliance.sourceforge.net/doc/org/aopalliance/intercept/MethodInterceptor.html
[constructorinterceptor]: http://aopalliance.sourceforge.net/doc/org/aopalliance/intercept/ConstructorInterceptor.html
[contract]: apidocs/org/jvnet/hk2/annotations/Contract.html
[contractsprovided]: apidocs/org/glassfish/hk2/api/ContractsProvided.html
[extrasutilities]: apidocs/org/glassfish/hk2/extras/ExtrasUtilities.html
[interceptionbinder]: apidocs/org/glassfish/hk2/extras/interception/InterceptionBinder.html
