## Configuration

HK2 has an optional feature which allows configuration values specified in some format
such as (but not limited to) XML or Properties files to be injected into HK2 services.

### The parts

The architecture of the HK2 configuration system consists of three parts.  These three parts are
then combined to make the user experience simple and intuitive.  The three basic parts of the
HK2 configuration system are:

1.  Persistence (some external configuration format such as XML or properties)
2.  Registry (a single JVM in-memory representation of Java Beans or bean-like maps)
3.  HK2 service integration (injecting values into HK2 services)

Each of these parts play a role in bringing configuration information into HK2 services.  The basic
idea is that the persistence layer translates whatever external format the configuration has into
JavaBeans and updates the registry.  The registry then notifies the HK2 service integration piece
which either adds HK2 services or updates existing HK2 services with new values from the existing
JavaBeans.

Since it is the second part of the HK2 configuration system that serves as the basic abstraction
layer it shall be described first.

### The HK2 Configuration Registry

The HK2 Configuration Registry is an in-memory registry of JavaBeans (or bean-like maps).  Instances
are organized by a type, and then further by instance names.  It is usually the case that all instances
of the same type have the same java type, and the same set of parameters.  Instance names can either
be a field from the bean or can be calculated some other way.  The combination of type name and
instance name will always uniquely identify a particular instance.

The main API for working with the Registry is the [Hub][hub].  The [Hub][hub] is an HK2 service that
can be added to a [ServiceLocator][servicelocator] with the enableConfigurationHub method of the
[ManagerUtilities][managerutilities] class.

The current [BeanDatabase][beandatabase] can be gotten from the [Hub][hub]. A 
[WriteableBeanDatabase][writeablebeandatabase] which allows changes to the [BeanDatabase][beandatabase]
is available from the [Hub][hub].  [BeanDatabaseUpdateListener][beandatabaseupdatelistener] implementations
can be registered with the [Hub][hub] to keep track of the current state of the configuration registry.

Those creating a module that is providing configuration from some backing store into the registry will
most often be working with the [BeanDatabase][beandatabase] and [WriteableBeanDatabase][writeablebeandatabase]
API.  The HK2 service integration part of the HK2 configuration feature sets itself up as a
[BeanDatabaseUpdateListener][beandatabaseupdatelistener] in order to provision HK2 services with configuration
data.  However there may be other uses for a [BeanDatabaseUpdateListener][beandatabaseupdatelistener].

### HK2 Service Integration

The HK2 service integration layer takes JavaBean instances from the Hub and creates an HK2 service per instance
of the type.  It does this by setting itself up as a [BeanDatabaseUpdateListener][beandatabaseupdatelistener]
on the [Hub][hub].  HK2 mark themselves as being configured by a specific type by putting themselves into
the [ConfiguredBy][configuredby] scope.  This is the definition of the [ConfiguredBy][configuredby] scope
annotation:

```java
@Scope
@Retention(RUNTIME)
@Target(TYPE)
public @interface ConfiguredBy {
    /** The type to be associated with this service */
    public String value();
    
    /** * Specifies the creation policy for configured services */
    public CreationPolicy creationPolicy() default CreationPolicy.ON_DEMAND;
    
    public enum CreationPolicy {
        /**
         * Instances of services with this policy will
         * be created when some user code creates explicit
         * demand for the service.  This is similar to most
         * other hk2 services
         */
        ON_DEMAND,
        
        /**
         * Instances of services with this policy will
         * be created as soon as their backing instances
         * become available
         */
        EAGER
    }
}
```java

HK2 services in the [ConfiguredBy][configuredby] scope are normal HK2 services in that they can have normal
injection points and have all the other features of an HK2 service.  They are also able to inject configuration
data into fields, constructors or methods that are annotated with [Configured][configured].  There will be one
instance of the HK2 service created for each instance of the type in the [BeanDatabase][beandatabase].  Each
instance of the HK2 service will have a name equal to the name of the instance in the [BeanDatabase][beandatabase]
upon which it is based.

Any field or parameter annotated with [Configured][configured] will be injected with a value from the JavaBean instance
in the [BeanDatabase][beandatabase] upon which this instance of the HK2 service is based.  This is the definition of
the [Configured][configured] annotation:

```java
@Retention(RUNTIME)
@Target( { FIELD, PARAMETER })
@InjectionPointIndicator
public @interface Configured {
    public final static String BEAN_KEY = "$bean";
    
    /**
     * The name of the field in the java bean or
     * bean-like map to use for injecting into
     * this field or parameter
     */
    public String value() default "";
    
    /**
     * Describes how dynamic a configured field or parameter must be.
     * All parameters of a constructor must be STATIC.
     * All parameters of a method must have the same dynamicity value
     * 
     * @return The dynamicicty of this field or parameter
     */
    public Dynamicity dynamicity() default Dynamicity.STATIC;
    
    /**
     * Describes how dynamic a configured field or parameter should be
     */
    public enum Dynamicity {
        /** This value should not automatically change over the life of the service instance */
        STATIC,
        /** This value can change at any time during the life of the service instance */
        FULLY_DYNAMIC
    }

}
```java

When placed on a java Field the name of the parameter can come from the name of the field.  However, when placed
on a parameter of a constructor or a method the [Configured][configured] value field must be filled in with
the name of the parameter from the JavaBean to use.  If the parameter name is &quot;$bean&quot; then the parameter
or field will take the whole bean as its value.

Fields and methods that have parameters marked with [Configured][configured] can opt in to having those values
changed dynamically when the corresponding parameter in the JavaBean is modified.

An HK2 service can also mark methods to run before and after dynamic changes are taking place, and it can also
implement the java.beans.PropertyChangeListener interface.  To mark a method to run prior to dynamic changes
being made use the [PreDynamicChange][predynamicchange] annotation on a method:

```java
@Retention(RUNTIME)
@Target(METHOD)
public @interface PreDynamicChange {
}
```java

To mark a method to run after dynamic changes are complete use the [PostDynamicChange][postdynamicchange] annotation
on a method:

```java
@Retention(RUNTIME)
@Target(METHOD)
public @interface PostDynamicChange {
}
```java

The methods marked with [PreDynamicChange][predynamicchange] or [PostDynamicChange][postdynamicchange] can take
zero parameters or a single parameter that is a List&lt;PropertyChangeListener&gt;.  If the List version is used
the List will contain the full set of dynamic changes that took place in a single [BeanDatabase][beandatabase]
operation.  Furthermore, the [PreDynamicChange][predynamicchange] can optionally return a boolean value.  If
it does return a boolean value and that boolean value is false, then any fields marked as being dynamic will NOT
get updated.

The HK2 service integration part of the HK2 configuration system can be enabled using the enableConfigurationSystem
method of the [ConfigurationUtilities][configurationutilities] class.

### Configuration Persistence

This space is under construction

[hub]: apidocs/org/glassfish/hk2/configuration/hub/api/Hub.html
[servicelocator]: apidocs/org/glassfish/hk2/api/ServiceLocator.html
[managerutilities]: apidocs/org/glassfish/hk2/configuration/hub/api/ManagerUtilities.html
[beandatabase]: apidocs/org/glassfish/hk2/configuration/hub/api/BeanDatabase.html
[writeablebeandatabase]: apidocs/org/glassfish/hk2/configuration/hub/api/WriteableBeanDatabase.html
[beandatabaseupdatelistener]: apidocs/org/glassfish/hk2/configuration/hub/api/BeanDatabaseUpdateListener.html
[configuredby]: apidocs/org/glassfish/hk2/configuration/api/ConfiguredBy.html
[configured]: apidocs/org/glassfish/hk2/configuration/api/Configured.html
[predynamicchange]: apidocs/org/glassfish/hk2/configuration/api/PreDynamicChange.html
[postdynamicchange]: apidocs/org/glassfish/hk2/configuration/api/PostDynamicChange.html