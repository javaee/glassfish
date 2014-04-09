## Custom Injection Resolver Example

### Custom Injection Resolution

This directory contains an example that illustrates how to write a custom injection resolver.

A custom injector allow users to define their own injection annotation, or to customize in some way the system
injection resolver that does the JSR-330 standard resolution.  In this example, we will define our own
injection resolver which customizes the JSR-300 standard one, but supplies the ability to get more information
out of annotations on the parameter of a method.

In this use case, we want to have a method that can be injected, and which can annotate parameters of the method
to determines the value that parameter should take.  The real value will end up coming from an index
into the data of an HttpRequest.  Here is an example of a method that uses this custom injector, from the
HttpEventReceiver class:

```java
    @AlternateInject
    public void receiveRequest(
            @HttpParameter int rank,
            @HttpParameter(1) long id,
            @HttpParameter(2) String action,
            Logger logger) {
       //...
    }
```java

The method receiveRequest takes the parameters rank, id, action and logger.  But the determination of what value rank, id and
action should take will be determined by the index in the HttpParameter annotation.  Here is the definition of HttpParameter:

```java
@Retention(RUNTIME)
@Target( { PARAMETER })
public @interface HttpParameter {
    /** The index  number of the parameter to retrieve */

   public int value() default 0;
}
```java

The logger parameter of the receiveRequest method is just another service.  This service will come from the normal JSR-330
resolver, but the other parameters will be determined from the HttpParameter annotation.  The determination of
what the values should take comes from an object called the HttpRequest, which does nothing but store strings in certain indexes.
The HttpRequest object itself is in the RequestScope context, which means its values will change depending on what request
is currently active.  In order to do that, the RequestScope context is a Proxiable context.  We will see more about creating
the RequestScoped context later in this document.

For now, lets look at how we define the **@AlternateInject** annotation.  An injection annotation is valid on fields, methods and
constructors.  However, in this case the **@AlternateInject** is only supported for methods, so the definition of
**@AlternateInject** looks like this:

```java
@Retention(RUNTIME)
@Target( { METHOD })
public @interface AlternateInject {
}
```java

When providing a custom injection annotation, you must also provide an implementation of the
[InjectionResolver][injectionresolver] interface.  It is this
implementation that will be called whenever HK2 wants to inject into a constructor or field or method
that is annotated with the custom injection annotation.  The actual type of the parameterized type of
the [InjectionResolver][injectionresolver] implementation must be
the custom injection annotation.  Here is how the AlternateInjectionResolver is defined:

```java
@Singleton
public class AlternateInjectResolver implements InjectionResolver<AlternateInject> {
    //...
}
```java

Implementations of [InjectionResolver][injectionresolver] are registered
with HK2 like any other service, and like any other service they may be injected with other services in the system.
The AlternateInjectResolver is in the @Singleton context, which is the usual context for implementations of
[InjectionResolver][injectionresolver].  In general however implementations of [InjectionResolver][injectionresolver] may be in any context.
Implementations of [InjectionResolver][injectionresolver] may not use the custom injection
annotations that they themselves are defining to inject things into themselves.

Implementations of [InjectionResolver][injectionresolver] that want
to customize the default JSR-330 system provided injector can do so by injecting the default JSR-330 system provided
injector.  The AlternateInjectionResolver does just that:

```java
public class AlternateInjectResolver implements InjectionResolver<AlternateInject> {
    @Inject @Named(InjectionResolver.SYSTEM_RESOLVER_NAME)
    private InjectionResolver<Inject> systemResolver;
}
```java

The system JSR-330 injection resolver is put into the registry with a specific name so that other injection resolvers
can easily inject it using the @Named annotation.

Now we need to write the resolve method from [InjectionResolver][injectionresolver].
We can get the current method we are injecting into from the passed in [Injectee][injectee], and from that we can tell whether or not any particular
parameter of the method has the @HttpParameter annotation.  But what happens when a parameter does have the
@HttpParameter annotation?

In that case, the real data should come from the underlying HttpRequest object.  The HttpRequest object is a very simple
object that stores strings at certain indexes:

```java
@RequestScope
public class HttpRequest {
    public String getPathElement(int index) {...}

    public void addElement(String element) {...}
}
```java

Because this is a request scoped object, the underlying values will change whenever the request has changed.  So our
AlternateInjectResolver can inject an HttpRequest object and use it to get values whenever it detects an
@HttpParameter annotation on a parameter of the method.  This is a code snippet from AlternateInjectResolver:

```java
public class Foo {
    @Inject
    private HttpRequest request;

    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        //...

        Annotation annotations[] = method.getParameterAnnotations()[injectee.getPosition()];
        HttpParameter httpParam = getHttpParameter(annotations);
        if (httpParam == null) {
            return systemResolver.resolve(injectee, root);
        }

        int index = httpParam.value();
        String fromRequest = request.getPathElement(index);

        //...
    }
}
```java

In the above code snippet the resolve method looks for an HttpParameter annotation on the particular parameter being
injected.  If it does not find such an annotation it simply lets the system injection resolver do the resolution.
Otherwise, it gets the value from the injected HttpRequest.

But that is not the end of the story.  The values that get injected into can be of type int, long or String.  The
resolve method can determine the type that is required to be returned, and ensure that it does the correct conversion
before returning the object.  Here is how that code works:

```java
    Class<?> injecteeType = method.getParameterTypes()[injectee.getPosition()];
    if (int.class.equals(injecteeType)) {
        return Integer.parseInt(fromRequest);
    }
    if (long.class.equals(injecteeType)) {
        return Long.parseLong(fromRequest);
    }
    if (String.class.equals(injecteeType)) {
        return fromRequest;
    }
```java

That is it for the implementation of our custom injection resolver!  Every time the HttpEventReceiver
class is instantiated its receiveRequest method will be called with the values from the current
HttpRequest.  The custom injection resolver was used to find the proper values in the HttpRequest and
to convert them to the proper types.  The logger would come from the default JSR-330 resolver, since
it is not annotated with the HttpParameter annotation.

### The RequestScope Context

While the above is enough to demonstrate the custom injection resolver, it is instructive to also go through
how the RequestScope context works.

The RequestScope context is a proxiable context that changes every time a new request has come in.  The HttpRequest
in the above example is in the RequestScope, and hence its underlying values will change whenever the request
has been deemed to change.

In order to create such a scope/context, we first define the scope annotation, RequestScope:

```java
@Scope
@Proxiable
@Retention(RUNTIME)
@Target( { TYPE, METHOD })
public @interface RequestScope {
}
```java

 The context that goes along with that request scope must implement the
 [Context][context] interface.  The actual type of the [Context][context] parameterized type must be the
 annotation of the scope.  The [Context][context] implementation
 for our RequestScope is called RequestContext and is defined like this:

```java
@Singleton
public class RequestContext implements Context<RequestScope> {
    //...
}
```java

Most implementations of [Context][context] are put into the Singleton
scope, though this is not required.  An implementation of [Context][context]
are just like regular HK2 services, and so can be injected with other HK2 services.

The job of an implementation of [Context][context] is to keep a mapping
of objects created for that particular context while that context is active.  The code that looks up and finds
objects for a particular request is straight-forward:


```java
public class Bar {
    private final HashMap<ActiveDescriptor<?>, Object> requestScopedEntities = new HashMap<ActiveDescriptor<?>, Object>();

    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor, ServiceHandle<?> root) {
        U retVal = (U) requestScopedEntities.get(activeDescriptor);
        if (retVal != null) {
            return retVal;
        }

        retVal = activeDescriptor.create(root);
        requestScopedEntities.put(activeDescriptor, retVal);

        return retVal;
    }

    public <U> U find(ActiveDescriptor<U> descriptor) {
        return (U) requestScopedEntities.get(descriptor);
    }
}
```java

Since an implementation of [Context][context] is a service, it can be
looked up by other services.  RequestContext has methods on it that allow some controller to tell it when
a request has started, and when it ends.  When a request ends its objects are no longer needed and should
be destroyed.  Here are the methods on RequestContext that begin and end a request:

```java
    private boolean inRequest = false;

    /**
     * Starts a request
     */
    public void startRequest() {
        inRequest = true;
    }

    public void stopRequest() {
        inRequest = false;

        for (Map.Entry<ActiveDescriptor<?>, Object> entry : requestScopedEntities.entrySet()) {
            ActiveDescriptor<Object> ad = (ActiveDescriptor<Object>) entry.getKey();
            Object value = entry.getValue();

            ad.dispose(value);
        }

        requestScopedEntities.clear();
    }

    public boolean isActive() {
        return inRequest;
    }
```java

The startRequest method above sets the flag saying that the Request has begun.  Any new requests to
find or create request scoped objects will be adding those objects to the requestScopedEntities map.  The
stopRequest method above will set the flag saying that the request is over and destroy any objects that were
created for this request.  It then also clears the requestScopedEntities map so that this RequestScoped context
is now clean and ready for the next request to come along.
The isActive method of [Context][context] will tell the system whether or not there is a request that is active.

That is it for the implementation of our RequestScope scope/context pair.  In this example the HttpRequest object
is in the RequestScope, but this implementation does not preclude other services from also being in this scope.
The scope is Proxiable, so that it can be injected into other objects with a different lifecycle (like the
AlternateInjectResolver itself).  Further, it properly disposes all request scoped objects that were created
when the request has terminated.

### Putting it all together

We now have a custom injection resolver and a custom scope.  Lets look at the other classes in the example, to see
how they tie everything together.

First we have a class called the HttpServer.  The HttpServer is a mock HttpServer that takes requests from the faux
network and does the following things:

+ Tells the RequestScope that a Request has begun
+ Fills in the HttpRequest with information from the wire

The request processing then continues from there, until the faux network decides that the request has finished.  The
HttpServer will then tell the RequestContext that the request has terminated.

Here is the implementation of our mocked HttpServer:

```java
@Singleton
public class HttpServer {
    @Inject
    private HttpRequest httpRequest;

    @Inject
    private RequestContext requestContext;

    public void startRequest(String lastRank, String id, String action) {
        requestContext.startRequest();

        httpRequest.addElement(lastRank);
        httpRequest.addElement(id);
        httpRequest.addElement(action);
    }

    public void finishRequest() {
        requestContext.stopRequest();
    }
}
```java

This mock HttpServer will be used by the test code to give the server requests from the network and to then end those requests.  The
injected HttpRequest will be created anew in the HttpServer.startRequest  method when the RequestContext.startRequest() method is called.

We then have another class called RequestProcessor which is in the PerLookup scope and which is responsible for further handling the
request.  In our example it doesn't have much to do other than injecting an instance of the HttpEventReceiver.  Since the
HttpEventReceiver is also in the PerLookup scope, it will be created whenever the instance of RequestProcessor is created.  Here is
the code for RequestProcessor:

```java
@PerLookup
public class RequestProcessor {
    @Inject
    private HttpEventReceiver eventReciever;

    public HttpEventReceiver processHttpRequest() {
        return eventReciever;
    }
}
```java

We can now look at how the test code work.
The test has a helper method that does the following:

+ Gets the HttpServer service
+ starts a request by giving it passed in strings that came from the faux network
+ Gets a RequestProcessor service
+ Gets the HttpEventReceiver from the RequestProcessor
+ ends the request with the HttpServer
+ Checks that the values from the HttpEventReceiver were passed into it properly

Here is the test utility method:

```java
    private void doRequest(int rank, long id, String event) {
        HttpServer httpServer = locator.getService(HttpServer.class);

        httpServer.startRequest("" + rank, "" + id, event);

        RequestProcessor processor = locator.getService(RequestProcessor.class);

        HttpEventReceiver receiver = processor.processHttpRequest();

        httpServer.finishRequest();

        // And now test that we got what we should have
        Assert.assertEquals(rank, receiver.getLastRank());
        Assert.assertEquals(id, receiver.getLastId());
        Assert.assertEquals(event, receiver.getLastAction());
    }
```java

After having this utility method, the test itself is very simple, and just ensures that the whole thing fits together nicely:

```java
    @Test
    public void testSomeRequests() {
        doRequest(50, 1, "FirstRequest");
        doRequest(100, 2, "SecondRequest");
        doRequest(1000, 3, "ThirdRequest");
    }
```java

### Conclusion

 In this example we have learned how to create and use a custom injection resolver and a request scoped context.  We have done so
 with a fake HttpServer example, that takes requests from a fake network and passes values to services based on fields in the
 HttpRequest.  We have seen how the custom resolver can use data from annotations to further discover the values that should be
 given to the parameter.  We have seen how the proxiable request context is used to ensure that the underlying request can
 change from request to request.  We have shown how a custom resolver can customize the default JSR-330 provider.

[context]: apidocs/org/glassfish/hk2/api/Context.html
[injectionresolver]: apidocs/org/glassfish/hk2/api/InjectionResolver.html
[injectee]: apidocs/org/glassfish/hk2/api/Injectee.html