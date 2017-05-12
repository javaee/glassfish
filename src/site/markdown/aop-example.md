[//]: # " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. "
[//]: # "  "
[//]: # " Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " The contents of this file are subject to the terms of either the GNU "
[//]: # " General Public License Version 2 only (''GPL'') or the Common Development "
[//]: # " and Distribution License(''CDDL'') (collectively, the ''License'').  You "
[//]: # " may not use this file except in compliance with the License.  You can "
[//]: # " obtain a copy of the License at "
[//]: # " https://oss.oracle.com/licenses/CDDL+GPL-1.1 "
[//]: # " or LICENSE.txt.  See the License for the specific "
[//]: # " language governing permissions and limitations under the License. "
[//]: # "  "
[//]: # " When distributing the software, include this License Header Notice in each "
[//]: # " file and include the License file at LICENSE.txt. "
[//]: # "  "
[//]: # " GPL Classpath Exception: "
[//]: # " Oracle designates this particular file as subject to the ''Classpath'' "
[//]: # " exception as provided by Oracle in the GPL Version 2 section of the License "
[//]: # " file that accompanied this code. "
[//]: # "  "
[//]: # " Modifications: "
[//]: # " If applicable, add the following below the License Header, with the fields "
[//]: # " enclosed by brackets [] replaced by your own identifying information: "
[//]: # " ''Portions Copyright [year] [name of copyright owner]'' "
[//]: # "  "
[//]: # " Contributor(s): "
[//]: # " If you wish your version of this file to be governed by only the CDDL or "
[//]: # " only the GPL Version 2, indicate your decision by adding ''[Contributor] "
[//]: # " elects to include this software in this distribution under the [CDDL or GPL "
[//]: # " Version 2] license.''  If you don't indicate a single choice of license, a "
[//]: # " recipient has the option to distribute your version of this file under "
[//]: # " either the CDDL, the GPL Version 2 or to extend the choice of license to "
[//]: # " its licensees as provided above.  However, if you add GPL Version 2 code "
[//]: # " and therefore, elected the GPL Version 2 license, then the option applies "
[//]: # " only if the new code is made subject to such option by the copyright "
[//]: # " holder. "

## Aspect Oriented Programming (AOP) Example

This example illustrates how to use Aspect Oriented Programming (AOP) with HK2
services.

AOP is generally used for cross-cutting concerns such as security or transactions.
This example will illustrate how you can use
AOP in HK2 in order to elegantly solve these sorts of issues.

HK2 AOP allows you to use [AOP Alliance][aopalliance] [method][methodinterceptors]
and [constructor][constructorinterceptors] interceptors with most
services that HK2 constructs.  This example will present simple caching
interceptors to illustrate how to write interceptors and then use those
interceptors with HK2 services.

## Caching Interceptors

The method interceptor shown here assumes that the methods it will be used with
take one input parameter and return some sort of result.  When the method is called
with an input parameter it has already seen the interceptor will not call the underlying
method, but will instead find the previous result in the cache and return it.  This saves
the method from performing the same calculation over again.  Here is the code for the
method interceptor:

```java
public class CachingMethodInterceptor implements MethodInterceptor {
    private final HashMap<CacheKey, Object> cache = new HashMap<CacheKey, Object>();

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object args[] = invocation.getArguments();
        if (args.length != 1) {
            return invocation.proceed();
        }
        
        Object arg = args[0];
        if (arg == null) {
            return invocation.proceed();
        }
        
        Method m = invocation.getMethod();
        CacheKey key = new CacheKey(m.getDeclaringClass().getName(), m.getName(), arg);
        
        if (!cache.containsKey(key)) {
            Object retVal = invocation.proceed();
            
            cache.put(key, retVal);
            
            return retVal;
        }
        
        // Found it in the cache!  Do not call the method
        return cache.get(key);
    }
}
```java

The cache is the HashMap field named cache.  After some defensive checking
the interceptor creates a CacheKey from the input parameters.  The interceptor then
checks to see if the cache contains the corresponding key.  If the cache does not contain the corresponding
key then it goes ahead and calls the underlying method with the call to proceed, saving the output.  
The method interceptor then puts that key and the result into the cache.  However if it does find the
CacheKey in the cache then it just returns the result *without* calling the proceed method, and thus the
underlying method on the service will NOT get called.

Here is the code for the CacheKey, which is an immutable object composed of the name of the class,
the name of the method called, and the argument sent to the method:

```java
public class CacheKey {
    private final String className;
    private final String methodName;
    private final Object input;
    
    public CacheKey(String className, String methodName, Object input) {
        this.className = className;
        this.methodName = methodName;
        this.input = input;
    }
    
    public int hashCode() {
        return className.hashCode() ^ methodName.hashCode() ^ input.hashCode();
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof CacheKey)) return false;
        CacheKey other = (CacheKey) o;
        
        if (!other.className.equals(className)) return false;
        if (!other.methodName.equals(methodName)) return false;
        return other.input.equals(input);
    }
}
```java

The CacheKey has an appropriate hashCode and equals method for objects that will be used as
a key in a HashMap.  The CacheKey will also bin be used in the constructor interceptor.

The constructor interceptor works exactly the same as the method interceptor, although the effect
it has on the service is somewhat different than the effect it has on the method, since
a constructor controls creation of the instance of the class.  Here is the constructor interceptor:

```java
public class CachingConstructorInterceptor implements ConstructorInterceptor {
    private final HashMap<CacheKey, Object> cache = new HashMap<CacheKey, Object>();

    public Object construct(ConstructorInvocation invocation) throws Throwable {
        Object args[] = invocation.getArguments();
        if (args.length != 1) {
            return invocation.proceed();
        }
        
        Object arg = args[0];
        if (arg == null) {
            return invocation.proceed();
        }
        
        Constructor<?> c = invocation.getConstructor();
        CacheKey key = new CacheKey(c.getDeclaringClass().getName(), "<init>", arg);
        
        if (!cache.containsKey(key)) {
            Object retVal = invocation.proceed();
            
            cache.put(key, retVal);
            
            return retVal;
        }
        
        // Found it in the cache!  Do not call the method
        return cache.get(key);
    }
```java

This code should look familiar, as it follows exactly the same pattern as the method interceptor.
This interceptor will only work on constructors with one input parameter and will short-circuit
the creation of a new object by returning the value in the cache (assuming one is found).  If
no corresponding object is found in the cache then a new object will be created with the call
to proceed.

Both the method and constructor interceptors use no HK2 API and can therefore be used in any
AOP alliance system.  However there is nothing preventing writers of AOP alliance interceptors
from using HK2 services as AOP alliance interceptors, in which case they could do things like inject
transaction or security managers.

### Using Interceptors in HK2

In order to use AOP Alliance interceptors in HK2 an instance of the [InterceptionService][interceptionservice]
must be added to the HK2 registry.  An implementation of [InterceptionService][interceptionservice] must be in
the Singleton scope.  It has the job of determining which HK2 services ([ActiveDescriptors][activedescriptor])
are candidates to use interception and then to specify exactly which methods and constructors of those
services should be intercepted.

This example uses an annotation called Cache that when placed on a method or constructor indicates
that that method or constructor should be intercepted with the caching interceptor.  This is the definition
of the Cache annotation:

```java
@Retention(RUNTIME)
@Target( { METHOD, CONSTRUCTOR })
public @interface Cache {
}
```java

The example [InterceptionService][interceptionservice] is named HK2InterceptionService and is in
the Singleton scope, as must all implementations of [InterceptionService][interceptionservice].  When
HK2 creates a new service it will look through all of the implementations of
[InterceptionService][interceptionservice] looking for ones that are appropriate for the services'
[ActiveDescriptor][activedescriptor].  If the [ActiveDescriptor][activedescriptor] passes through the filter
returned by the [InterceptionService][interceptionservice] then all the methods of that service
will be given to the [InterceptionService][interceptionservice] in order to determine what method
interceptors should be called for that method.  The single constructor that HK2 would normally use
will also be given to the [InterceptionService][interceptionservice] in order to determine what
constructor interceptors should be called for that constructor.

The example [InterceptionService][interceptionservice] inspects the input methods and constructors to see if
they are annotated with @Cache, and if they are it returns the caching interceptors described in the
previous section.  The filter that the example [InterceptionService][interceptionservice] uses to select HK2
services is the [BuilderHelper][builderhelper] allFilter, since any method or constructor might
be a candidate for caching.  Here is the code for the example caching HK2
[InterceptionService][interceptionservice]:

```java
@Service
public class HK2InterceptionService implements InterceptionService {
    private final static MethodInterceptor METHOD_INTERCEPTOR = new CachingMethodInterceptor();
    private final static ConstructorInterceptor CONSTRUCTOR_INTERCEPTOR = new CachingConstructorInterceptor();
    
    private final static List<MethodInterceptor> METHOD_LIST = Collections.singletonList(METHOD_INTERCEPTOR);
    private final static List<ConstructorInterceptor> CONSTRUCTOR_LIST = Collections.singletonList(CONSTRUCTOR_INTERCEPTOR);

    public Filter getDescriptorFilter() {
        return BuilderHelper.allFilter();
    }

    public List<MethodInterceptor> getMethodInterceptors(Method method) {
        if (method.isAnnotationPresent(Cache.class)) {
            return METHOD_LIST;
        }
        
        return null;
    }

    public List<ConstructorInterceptor> getConstructorInterceptors(
            Constructor<?> constructor) {
        if (constructor.isAnnotationPresent(Cache.class)) {
            return CONSTRUCTOR_LIST;
        }
        
        return null;
    }
}
```java

In the next section we will use our caching annotation and the caching interceptors on an example
set of services.

### Using @Cache on Methods

The runner project under examples/caching in the HK2 source tree contains an example of method and
constructor injection using the system described in the above secion (which is under examples/caching/system).
It also contains unit tests to ensure that everything in the example is working as expected.

The ExpensiveMethods class has a method on it that performs an expensive operation.  The expensive method
counts the number of times it has been called so that the class can easily demonstrate that the caching code
has worked.

```java
@Service @Singleton
public class ExpensiveMethods {
    private int timesCalled = 0;
    
    @Cache
    public int veryExpensiveCalculation(int input) {
        timesCalled++;
        return input + 1;
    }
    
    public int getNumTimesCalled() {
        return timesCalled;
    }
    
    public void clear() {
        timesCalled = 0;
    }
}
```java

The ExpensiveMethods service is in the Singleton scope and hence will get created once.  However, the
method named veryExpensiveCalculation will only get called once per input integer.  So if the method
is called ten times with an input parameter of 1 the method on ExpensiveMethods will only get called once.  The
output from that call will be saved in the cache and every other time the method is called by the application
the returned value will come from the cache rather than calling the method again.  This can be seen
with the test code:

```java
    @Test
    public void testMethodsAreIntercepted() {
        ExpensiveMethods expensiveMethods = testLocator.getService(ExpensiveMethods.class);
        
        // Now call the expensive method
        int result = expensiveMethods.veryExpensiveCalculation(1);
        Assert.assertEquals(2, result);
        
        // The expensive method should have been called
        Assert.assertEquals(1, expensiveMethods.getNumTimesCalled());
        
        // Now call the expensive method ten more times
        for (int i = 0; i < 10; i++) {
            result = expensiveMethods.veryExpensiveCalculation(1);
            Assert.assertEquals(2, result);
        }
        
        // But the expensive call was never made again, since the result was cached!
        Assert.assertEquals(1, expensiveMethods.getNumTimesCalled());
    }
```java

### Using @Cache on a Constructor

Caching on a constructor can limit the number of times a service is created.  The example
class called ExpensiveConstructor is in the PerLookup scope.  However, since @Cache has been
place on its constructor, it will in fact only get created when different input parameters are given
to the constructor.  Static fields are used in ExpensiveConstructor to keep track of how many times
the service has been created.  Here is the ExpensiveConstructor service:

```java
@Service @PerLookup
public class ExpensiveConstructor {
    private static int numTimesConstructed;
    private final int multiplier;
    
    @Inject @Cache
    public ExpensiveConstructor(int multiplier) {
        // Very expensive operation
        this.multiplier = multiplier * 2;
        numTimesConstructed++;
    }
    
    public int getComputation() {
        return multiplier;
    }
    
    public static void clear() {
        numTimesConstructed = 0;
    }
    
    public static int getNumTimesConstructed() {
        return numTimesConstructed;
    }
}
```java

The ExpensiveConstructor class takes an integer input parameter.  In order to create an integer input
that can change values there is an implementation of [Factory][factory] called InputFactory which produces
integers.  The provide method of InputFactory changes the value it returns based on how it
is currently configured.  Here is the code for InputFactory:

```java
@Service
public class InputFactory implements Factory<Integer> {
    private int input;

    @PerLookup
    public Integer provide() {
        return input;
    }

    public void dispose(Integer instance) {
    }
    
    public void setInput(int input) {
        this.input = input;
    }
}
```java

The test code for this demonstrates how to change the input parameter for the
ExpensiveConstructor service.  The test then uses the static methods to show that
the service is only created when the input parameter changes, even though
the service is in the PerLookup scope.

```java
    @Test
    public void testConstructorsAreIntercepted() {
        InputFactory inputFactory = testLocator.getService(InputFactory.class);
        inputFactory.setInput(2);
        
        ExpensiveConstructor instanceOne = testLocator.getService(ExpensiveConstructor.class);
        
        int computation = instanceOne.getComputation();
        Assert.assertEquals(4, computation);
        
        Assert.assertEquals(1, ExpensiveConstructor.getNumTimesConstructed());
        
        ExpensiveConstructor instanceTwo = testLocator.getService(ExpensiveConstructor.class);
        
        computation = instanceTwo.getComputation();
        Assert.assertEquals(4, computation);
        
        // Amazingly, the object was NOT recreated:
        Assert.assertEquals(1, ExpensiveConstructor.getNumTimesConstructed());
        
        // Further proof that it was not recreated:
        Assert.assertTrue(instanceOne == instanceTwo);
        
        // Now change the input parameter
        inputFactory.setInput(8);
        
        ExpensiveConstructor instanceThree = testLocator.getService(ExpensiveConstructor.class);
        
        computation = instanceThree.getComputation();
        Assert.assertEquals(16, computation);
        
        Assert.assertFalse(instanceOne.equals(instanceThree));
    }
```java

### AOP Requirements

In order to use method interceptors proxies are used.  Therefore services that use method interceptors
must not be final nor have any final methods.  Proxies must be supported on the platform on which
hk2 is running.  Constructor interception does NOT use proxies so these limitations do not extend to
constructor injection.  Any method to be intercepted must be public, protected or package visibility.
Private methods will not be intercepted.  Constructors to be intercepted can have any visibility.

The proxies created for method interception (as opposed to those created for
proxiable scopes) do not implement the [ProxyCtl][proxyctl] interface.  Instead they implement the
[AOPProxyCtl][aopproxyctl] interface, which allows access to the underlying descriptor for the service
whose instance was proxied.

Interception in general is only supported when HK2 is constructing the services itself.  In particular
services created via a [Factory][factory] can not use AOP nor can services that come from third-parties.
Any constant service can not use interception as HK2 did not create the service.

### Conclusion

HK2 AOP can be used to solve many cross-cutting concerns.  Because it uses AOP Alliance interceptors
the interceptors developed for other systems may be appropriate in HK2 as well.

[aopalliance]: http://aopalliance.sourceforge.net/
[methodinterceptors]: http://aopalliance.sourceforge.net/doc/org/aopalliance/intercept/MethodInterceptor.html
[constructorinterceptors]: http://aopalliance.sourceforge.net/doc/org/aopalliance/intercept/ConstructorInterceptor.html
[interceptionservice]: apidocs/org/glassfish/hk2/api/InterceptionService.html
[factory]: apidocs/org/glassfish/hk2/api/Factory.html
[activedescriptor]: apidocs/org/glassfish/hk2/api/ActiveDescriptor.html
[builderhelper]: apidocs/org/glassfish/hk2/utilities/BuilderHelper.html
[proxyctl]: apidocs/org/glassfish/hk2/api/ProxyCtl.html
[aopproxyctl]: apidocs/org/glassfish/hk2/api/AOPProxyCtl.html
