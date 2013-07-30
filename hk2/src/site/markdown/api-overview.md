## API Overview

### Compatibility

This page describes the HK2 2.0 API, which is based on JSR-330 standard annotations.
Also, Habitat has been replaced with a new interface called [ServiceLocator][servicelocator].

+ [Introduction](api-overview.html#Introduction)
+ [ServiceLocator](api-overview.html#ServiceLocator)
+ [Adding in your own services](api-overview.html#Adding_in_your_own_services)
+ [BuilderHelper Binding EDSL](api-overview.html#BuilderHelper_Binding_EDSL)
+ [DescriptorImpl](api-overview.html#DescriptorImpl)
+ [Binding a Descriptor into a ServiceLocator](api-overview.html#Binding_a_Descriptor_into_a_ServiceLocator)
+ [Looking up services](api-overview.html#Looking_up_services)
+ [Looking up services by name](api-overview.html#Looking_up_services_by_name)
+ [Looking up services with qualifiers](api-overview.html#Looking_up_services_with_qualifiers)
+ [Getting all services](api-overview.html#Getting_all_services)
+ [Getting service descriptors](api-overview.html#Getting_service_descriptors)
+ [Unmanaged Creation, Injection and Lifecycle](api-overview.html#Unmanaged_Creation,_Injection_and_Lifecycle)

## Introduction

HK2 is a declarative framework for services using annotations like [@Contract][contract] and [@Service][service].
However, it is possible to use programmatic APIs to precisely control the services and bindings available within the Services registry.

## ServiceLocator

 The most fundamental service in HK2 is the [ServiceLocator][servicelocator].
 The [ServiceLocator][servicelocator] represents the registry where services
 are looked up and where information about services (known as [Descriptors][descriptor]) are bound into the registry.
 The [ServiceLocator][servicelocator] itself is represented as a service in its
 own registry;  it is always the first service bound into its own registry.
 
[ServiceLocators][servicelocator] are named uniquely in a JVM and each has a unique locator ID.
It is possible to create or find [ServiceLocators][servicelocator] using the
[ServiceLocatorFactory][servicelocatorfactory].
The [ServiceLocatorFactory][servicelocatorfactory] will normally use a default
implementation of [ServiceLocatorGenerator][servicelocatorgenerator] specified in META-INF/services.
The default implementation can be changed by having a different META-INF/services 
specification of the implementation of [ServiceLocatorGenerator][servicelocatorgenerator]
earlier in the classpath than the provided implementation.
An implementation of [ServiceLocatorGenerator][servicelocatorgenerator] can also
be given directly to the [ServiceLocatorFactory][servicelocatorfactory] create method.

Once you have created a [ServiceLocator][servicelocator] with the
[ServiceLocatorFactory][servicelocatorfactory] it will contain at least three services:
 
- Itself (see [ServiceLocator][servicelocator])
- The default JSR-330 resolver (see [InjectionResolver][injectionresolver])
- A service for configuring further services (see [DynamicConfigurationService][dynamicconfigurationservice])

## Adding in your own services

While the three services in your [ServiceLocator][servicelocator] are nice, they hardly constitute
a useful system.  What is needed is all of your services, in order to make it useful.  Also please note that this section assumes that
you are not using the upper level system that automatically reads in the descriptions of your services and populate
[ServiceLocators][servicelocator] for you.  For information on how that system works see
TBD.

You add your own service by getting the [DynamicConfigurationService][dynamicconfigurationservice].
Since that is one of the original three services added to the service locator, you can get that service by simply looking
it up:
 
``` java
    public void initialize() {
        ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();
        
        ServiceLocator locator = factory.create("HelloWorld");
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        
        ...
    }
``` java

You use the [DynamicConfigurationService][dynamicconfigurationservice] to create [DynamicConfiguration][dynamicconfiguration] instances.
The [DynamicConfiguration][dynamicconfiguration] interface has a few methods for binding in descriptions of your services.
 
In order to bind in services you need to first create a description of your service.  A description of your service
gives information about the service, such as the name of the implementation class, and the name of the classes or
interfaces which the service should be available to be looked up as, and other information.  In general, any implementation
of [Descriptor][descriptor] can be used, but we have provided at least two mechanisms
for creating [Descriptors][descriptor] that you might want to use.  We will go through
those mechanisms in the next two sections, and then come back to adding in your own descriptor to your newly created [ServiceLocator][servicelocator].
 
### BuilderHelper Binding EDSL

An EDSL is an Embedded Domain Specific Language that allows you to build up objects specific to your particular domain.  In this
case we provide an EDSL for building Descriptors.

Lets take an example.  Suppose I wanted to tell the system about a service of mine that has implementation class
com.acme.internal.WidgetImpl which implements the com.acme.Widget contract (interface) and which is in the
PerLookup scope (which means a new instance of WidgetImpl will be provided for every injection point).  Here is how a descriptor
that contains all of that information can be built up using our EDSL:
 
``` java
    public Descriptor createWidgetDescriptor() {
        return BuilderHelper.link("com.acme.internal.WidgetImpl").
                         to("com.acme.Widget").
                         in("org.glassfish.api.PerLookup").
                         build();
    }
``` java

The [BuildHelper][buildhelper] link method creates a [DescriptorBuilder][descriptorbuilder].
The [DescriptorBuilder][descriptorbuilder] then creates more and more specific versions of itself
as you fill in the data with calls to "to" or "in" or "qualifiedBy".

Finally, when you are finished filling in all the details of your service, you call build in order to
produce a [Descriptor][descriptor] that can be used in a bind call of [DynamicConfiguration][dynamicconfiguration].

It is interesting to note that the build call of [DescriptorBuilder][descriptorbuilder] produces a [DescriptorImpl][descriptorimpl].
A [DescriptorImpl][descriptorimpl] is nothing more than a convenience implementation of [Descriptor][descriptor] that has settable fields.
Hence, if your code wanted to use the EDSL to produce a basic [Descriptor][descriptor] and then further customize it with the
added methods of [DescriptorImpl][descriptorimpl] it could do so.
 
### DescriptorImpl

Rather than create your own implementation of [Descriptor][descriptor] we have provided
an implementation of [Descriptor][descriptor] called [DescriptorImpl][descriptorimpl].
This implementation has convenient methods for setting all of the fields of [Descriptor][descriptor].
It should be noted that the bind API of [DynamicConfiguration][dynamicconfiguration]
will make a deep copy of whatever [Descriptor][descriptor] is passed to it, and
that the underlying implementation of the HK2 API never uses the [DescriptorImpl][descriptorimpl] class directly.
It is purely there as a convenience class for those who wish to provide their own [Descriptors][descriptor].

Here is an example that achieves the same [Descriptor][descriptor] as the example
in the previous section but uses the [DescriptorImpl][descriptorimpl] to do it:
 
``` java
    public Descriptor createWidgetDescriptor() {
        DescriptorImpl retVal = new DescriptorImpl();
        
        retVal.setImplementation("com.acme.internal.WidgetImpl");
        retVal.addAdvertisedContract("com.acme.internal.WidgetImpl");
        retVal.addAdvertisedContract("com.acme.Widget");
        retVal.setScope("org.glassfish.api.PerLookup");
        
        return retVal;
    }
``` java

One interesting thing to notice in the above code is that we added the implementation class as an advertisedContract.
This was done automatically for us in the [BuilderHelper][buildhelper] case, but needed to be explicitly done in this case.
 
### Binding a Descriptor into a ServiceLocator

Now that we have seen two simple ways to create a [Descriptor][descriptor] lets take a
look at how we bind that descriptor into our [ServiceLocator][servicelocator].
Here is an example:
 
``` java
    public void initialize() {
        ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();
        
        ServiceLocator locator = factory.create("HelloWorld");
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.bind(createWidgetDescriptor());
        
        config.commit();
    }
``` java

The method createWidgetDescriptor is from the preceeding examples.
In the above code we call the createDynamicConfiguration method of [DynamicConfigurationService][dynamicconfigurationservice].
This creates an instance of [DynamicConfiguration][dynamicconfiguration].
To use a [DynamicConfiguration][dynamiccondfiguration] you call the bind or unbind methods until you
are happy with the change and then you call commit to make the changes occur for real in the system.  If you do not call
commit none of the changes you added to the [DynamicConfiguration][dynamicconfiguration] instance will be made to the system.
 
That is all there is to it!  The services you add in this manner can now be looked up or injected into other services or
generally manipulated through all of the other methods in [ServiceLocator][servicelocator].
 
### Looking up services

There are several mechanisms for looking up services in HK2.  The simplest is to just call getService method of
[ServiceLocator][servicelocator] with the class of the service you are interested in:
 
``` java
  Widget widget = locator.getService(Widget.class);
``` java

The type passed in can be any implementation class or interface with which the service was bound with as an advertisable
contract.  If there is no Widget that can be found in the system then the getService method will return null.  If there
are more than one Widge (e.g. Widget is an interface that can have many implementations) then the best Widget will
be returned from the getService method.
 
The best instance of a service is a service with the highest ranking or the lowest service id.
The ranking of a service is found in its [Descriptor][descriptor] and can be changed at any time at run time.
The service id of a service is a system assigned value for the [Descriptor][descriptor] when it is bound into the [ServiceLocator][servicelocator].
The system assigned value is a monotonically increasing value.
Thus if two services have the same ranking the best service will be associated with the oldest [Descriptor][descriptor] bound into the system.
 
### Looking up services by name

Services can be qualified in many ways, but the most common is to have a name associated with the service.  Hence, in
our Widget example if there are several Widgets in the system but each has a different name we can find our particular
Widget like this:

``` java
    public Widget getNamedWidget(String name) {
        return locator.getService(Widget.class, name);
    }
``` java

The given name is used to further qualify the specific Widget that was bound into the system.
 
### Looking up services with qualifiers

If your services have qualifiers you can look them up via the qualifiers.  In order to do this you can use 
the [AnnotationLiteral][annotationliteral] in order to create concrete implementations of your annotations. 
Lets see how this would be done.  Suppose you have a qualifer called Blue, defined like this:

``` java
@Qualifier
@Retention(RUNTIME)
@Target( { TYPE, METHOD, FIELD, PARAMETER })
public @interface Blue {
}
``` java

Normally you wouldn't implement Blue, but in this case you do need an implementation in order to be able to
look it up.  You do that by providing an implement of Blue that extends [AnnotationLiteral][annotationliteral]:

``` java
public class BlueImpl extends AnnotationLiteral<Blue> implements Blue {
}
``` java

You can now use this BlueImpl to look up your qualified Widget in a [ServiceLocator][servicelocator] like this:
 
``` java
    Widget widget = locator.getService(Widget.class, new BlueImpl());
``` java

This will get the Widget that has been qualified with @Blue.
 
### Getting all services

You may also want to get all of the services that have advertised a certain contract.  You can do this like this:
 
``` java
    List<Widget> widgetList = locator.getAllServices(Widget.class);
``` java

The list returned will have as many Widgets that could be found in the system.  It is important to note in this case that all
of the Widges will have been classloaded when you use this call, so if classloading performance is important to you be careful
of using the getAllServices method.  Instead, consider using the getAllServiceHandles or getDescriptors method.
 
### Getting service descriptors

If you want to look up service descriptors rather than the services themselves you can use the getDescriptor or
getBestDescriptor methods on [ServiceLocator][servicelocator].  The getDescriptor
and getBestDescriptor methods will never cause classloading to occur, so it is safe to use in environments where
classloading can be an issue.
 
The getDescriptor methods on [ServiceLocator][servicelocator] use a [Filer][filter] to determine which [Descriptors][descriptor] to return.
You can implement your own [Filter][filter] or you can use one of the [Filter][filter] implementations provided by [BuilderHelper][buildhelper].
The most common case is to use an [IndexedFilter][indexedfilter] provided by [BuildHelper][buildhelper], like this:
 
```java
  IndexedFilter widgetFilter = BuilderHelper.createContractFilter(Widget.class.getName());
  
  List<ActiveDescriptor<?>> widgetDescriptors = locator.getDescriptors(widgetFilter);
```java

Using an [IndexedFilter][indexedfilter] can greatly improve the search time for your [Descriptors][descriptor].
 
### Unmanaged Creation, Injection and Lifecycle

There are times when you would like to have an object created, injected or have its lifecycle methods called by HK2, but
not have that Object be explicitly managed by HK2.  The [ServiceLocator][serviceLocator] has methods that suit this case.
These methods will inspect the class or object given and will attempt to perform the requested operations, without keeping track or managaging
those objects in any way.
 
The first method is the create method, which will attempt to create an instance of the given class using the dependency injection
rules of HK2:

```java
  Widget widget = locator.create(WidgetImpl.class);
```java

It is important to note that the only references to other beans that will have been initialized when this returns are those necessary
to perform constructor injection.  Hence any @Inject fields or @Inject initializer methods will NOT have been initialized when this
method returns.
 
If you already have an object, and would like for its @Inject fields and @Inject initializer methods to get filled in, you can
use the inject method:

```java
  locator.inject(widget);
```java

The object given will be analyzed and all of the fields and methods will be injected upon return.  However, any postConstruct
method on the object will not have been called yet.  That can be done with the postConstruct method:
 
```java
  locator.postConstruct(widget);
```java

This method call will find the postConstruct method on widget and call it.  Once the user is finished with the object, they can force
the preDestroy to be called on it by using the preDestroy method:
 
```java
  locator.preDestroy(widget);
```java

This sequence can be very useful when there is some special processing that needs to happen and the user does not want to have HK2 manage
the objects themselves.
 
[buildhelper]: apidocs/org/glassfish/hk2/utilities/BuilderHelper.html
[servicelocator]: apidocs/org/glassfish/hk2/api/ServiceLocator.html
[filter]: apidocs/org/glassfish/hk2/api/Filter.html
[descriptor]: apidocs/org/glassfish/hk2/api/Descriptor.html
[indexedfilter]: apidocs/org/glassfish/hk2/api/IndexedFilter.html
[annotationlitteral]: apidocs/org/glassfish/hk2/AnnotationLiteral.html
[dynamicconfigurationservice]: apidocs/org/glassfish/hk2/api/DynamicConfigurationService.html
[dynamicconfiguration]: apidocs/org/glassfish/hk2/api/DynamicConfiguration.html
[descriptorimpl]: apidocs/org/glassfish/hk2/utilities/DescriptorImpl.html
[injectionresolver]: apidocs/org/glassfish/hk2/api/InjectionResolver.html
[servicelocatorfactory]: apidocs/org/glassfish/hk2/api/ServiceLocatorFactory.html
[servicelocatorgenerator]: apidocs/org/glassfish/extension/api/ServiceLocatorGenerator.html
[descriptorbuilder]: apidocs/org/glassfish/hk2/utilities/DescriptorBuilder.html
[contract]: apidocs/org/jvnet/hk2/annotations/Contract.html
[service]: apidocs/org/jvnet/hk2/annotations/Service.html