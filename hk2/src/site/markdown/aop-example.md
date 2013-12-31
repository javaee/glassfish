## Aspect Oriented Programming (AOP) Example

This example illustrates how to use Aspect Oriented Programming (AOP) with HK2
services.

AOP is generally used for cross-cutting concerns such as security, transactions
or other global concerns.  This example will illustrate how you can use
HK2 AOP in order to do caching of expensive method and constructor calls.

HK2 allows you to use [AOP Alliance][aopalliance] [method][methodinterceptors]
interceptors and [constructor][[constructorinterceptors] interceptors with most
objects that HK2 constructs.  In this example we will write a simple caching
interceptors that use no HK2 API and thus could be used in any framework supporting
AOP interceptors.

## The Caching Interceptors

The method interceptor we write here assumes that the methods it will be caching the results
for take one input parameter and return some sort of result.  When the method is called
with the same input parameter the interceptor will not call the underlying method, but will
instead find the previous answer in the cache and return it.  This saves the method from
performing the same calculation over again.  Here is the code for the method interceptor:

```java
public class CachingMethodInterceptor implements MethodInterceptor {
    private final HashMap<CacheKey, Object> cache = new HashMap<CacheKey, Object>();

    @Override
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

The cache is the HashMap field named cache.  After some defensive checking (and a check for null, as we
don't cache the null input parameter) we create a CacheKey from the input invocation parameters.  We then
check to see if the cache contains the corresponding key.  If the cache does not contain the corresponding
key then we go ahead and call the underlying method with the call to proceed, saving the output.  We
then put that key and the result into the cache.  However if we do find the CacheKey in the cache then
we just return the result *without* calling the proceed method, implying that the underlying method on
the object will NOT get called.

Here is the code for the CacheKey, which is just an immutable object composed of the name of the class,
the name of the method called and the argument sent to the method:

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

The above CacheKey has an appropriate hashCode and equals method for objects that will be used as
a key in a HashMap.  We will also use the CacheKey in our constructor interceptor, except that the
methodName in that case will always be &lt;init&rt;.

The constructor interceptor works exactly the same as the method interceptor, although the effect
it has on the service is somewhat different than the effect it would have on the method, since
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

Both of the above interceptors use no HK2 API and can therefore be used in any AOP alliance system.
However there is nothing preventing writers of AOP alliance interceptors from using HK2 services
as AOP alliance interceptors, in which case they could do things like inject transaction or
security managers.

### Using Interceptors in HK2

In order to use AOP Alliance interceptors in HK2 an instance of the [InterceptionService][interceptionservice]
must be added to the HK2 registry.  An implementation of [InterceptionService][interceptionservice] must be in
the Singleton scope.  It has the job of determining which HK2 services (ActiveDescriptors) are candidates to
use interception and then to specify exactly which methods and constructors of those services should be
intercepted.

In our example we create an annotation called Cache that when placed on a method or constructor indicates
that that method or constructor should be intercepted with the caching interceptor.  This is the definition
of our annotation:

```java
@Retention(RUNTIME)
@Target( { METHOD, CONSTRUCTOR })
public @interface Cache {
}
```java

Since that annotation can be placed on any method or constructor, all services in HK2 are candidates to have
interception performed on them.  So the filter that we used to select HK2 services can just be the allFilter.
We then inspect the Method or Constructor to see if they contain the annotation, and if they do we return
the Caching interceptors described in the previous section.  Here is the code for the HK2
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

If that service is placed into an HK2 ServiceLocator registry then all methods marked with @Cache on all HK2 services
will be intercepted with the CachingMethodInterceptor and all constructors marked with @Cache will be intercepted
with CachingConstructorInterceptor.

Lets look at an example of how to use @Cache.

### Using @Cache on Methods

The runner project under examples/caching in the HK2 source tree contains an example of method and constructor injection
using the above system (which is under examples/caching/system).  It also contains unit tests to ensure that everything
in the example is working as expected.

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
is called ten times with an input parameter of 1 the method will only get called once.  The output from that
call will be saved in the cache and every other time the method is called by the application
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

Caching on a constructor can limit the number of times a service is created.  In our example
we have a class called ExpensiveConstructor that is in the PerLookup scope, but which in
fact only get created when different input parameters are given to the constructor.  Static
fields are used in ExpensiveConstructor to keep track of how many times the service has
been created.  Here is the ExpensiveConstructor service:

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

In order to create an integer input that can change values we use an implementation of
[Factory][factory] called InputFactory.  The provide method changes the value it returns
based on how the InputFactory is currently configured.  Here is the code for InputFactory:

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
must not be final nor have any final methods or fields.  Proxies must be supported on the platform on which
hk2 is running.  Constructor interception does NOT use proxies so these limitations do not extend to
constructor injection.

Interception in general is only supported when HK2 is constructing the services itself.  In particular
services created via [Factories][factory] can not use AOP nor can services that come from third-parties.
Any constant service can not use interception as HK2 did not create the service.

[aopalliance]: http://aopalliance.sourceforge.net/
[methodinterceptors]: http://aopalliance.sourceforge.net/doc/org/aopalliance/intercept/MethodInterceptor.html
[constructorinterceptors]: http://aopalliance.sourceforge.net/doc/org/aopalliance/intercept/ConstructorInterceptor.html
[interceptionservice]: apidocs/org/glassfish/hk2/api/InterceptionService.html
[factory]: apidocs/org/glassfish/hk2/api/Factory.html