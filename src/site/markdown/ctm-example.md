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

## Tenant Managed Scope Example

This example illustrates how a scope can be made that is tenant aware.  First, lets define a tenant.

A tenant is a an entity who has certain attributes that are backed by an XML configuration file.
The XML configuration file will contain all the information about that tenant.
For example, this XML contains all the data about a Tenant named Alice
 
```xml
<environments>
    <environment name="Alice" minSize="1" maxSize="2" />
</environments>
```xml

 This XML contains all the data about a tenant named Bob
 
```xml
<environments>
    <environment name="Bob" minSize="10" maxSize="20" />
</environments>
```xml

 The CTM team would like to have an interface called Environment that encapsulates all the information about a tenant:

```java
public interface Environment {
    String getName();
    int getMinSize();
    int getMaxSize();
}
```java

  There is code in the system that would like to inject the Environment class.  However, the code that will inject
  this interface is in the Singleton scope, which means it never changes.  And yet, depending on the state of the system
  the underlying tenant may change from Alice to Bob, or to any other tenant.  For example, the class could look like
  this:
  
```java
@Singleton
public class ServiceProvisioningEngine {

   @Inject
   private Environment tenant;

   private void provisionServices() {
       //envs here could be  either env1   or  env2 depending on
       //   whether this code is being executed on behalf of either tenant1 OR tenant2
   }

}
```java

We would like to demonstrate how a custom scope, called the TenantScope, can be used to solve this problem.  Firstly, in order
to solve the problem of the Singleton service getting injected with the Environment only at construction time, we must have HK2
inject a proxy for the Environment service rather than the particular environment itself.  The proxy can figure out, depending
on the state of the system, which tenant is active, and then use the proper Environment object for the real call.  In order to
ensure that this proxy is created, the TenantScope must be Proxiable.  You mark a scope as being Proxiable by adding the @Proxiable
annotation to the definition of the scope annotation itself, like this:

```java
@Scope
@Proxiable
@Retention(RUNTIME)
@Target( { TYPE, METHOD })
public @interface TenantScoped {
}
```java
 
This annotation is now marked as a scope indicator (via the @Scope annotation) and as proxiable (via the @Proxiable annotation).  All
objects that are injected from this scope will be given a proxy that uses the underlying machinery to determine the state of the system
and to use the proper backing objects for the real calls.

Now, however, we need some way to tell the system what tenant is currently active.  For this example, we have created a
class called the TenantManager.  The TenantManager is in the Singleton scope which means it will only be created once (when the first
person demands one).  The job of the TenantManager is to be able to set the currently active tenant.  It does so with this call:
 
```java
@Service
public class TenantManager {
    private String currentTenant;
    
    public void setCurrentTenant(String currentTenant) {
        this.currentTenant = currentTenant;
    }
    
    public String getCurrentTenant() {
        return currentTenant;
    }
}
```java

OK, great, now we know how to change the current tenant that is running on the system.  But how do we make the system understand this?
Well, since we decided that we were going to have a proxiable scope, we also need a corresponding implementation of the Context interface.
You do this by implementing Context, and making sure that the parameterized type of the Context is the class of the scope annotation.
In this case, we would implement the context for the TenantScope scope like this:
 
```java
@Singleton
public class TenantScopedContext implements Context<TenantScoped> {
    @Inject
    private TenantManager manager;
    ...
```java

Note that the implementation of Context is itself a service, in the Singleton scope (there is nothing that says a Context must be
in the Singleton scope, but most probably would be).  The only rule is that a Context implementation cannot be in the same scope as
the scope it is backing.  Also notice that since Context is a regular service that it can be injected with other services, such
as the TenantManager.  Luckily, that is just what we need, since the TenantManager knows what the current scope is!  First, lets
see what the implementation of the isActive method of the TenantScopedContext would look like:
 
```java
    public boolean isActive() {
        return manager.getCurrentTenant() != null;
    }
```java

That was pretty easy.  But now lets think about what the TenantScopeContext has to do.  It must keep the set of objects created
per tenant.  For example, we do not want to create the Environment implementation for the Alice tenant more than once.  And
so the TenantScopeContext keeps a mapping for each tenant.  Here is the map, and the code that gets the proper mapping based
on the current tenant:
 
```java
    private final HashMap<String, HashMap<ActiveDescriptor<?>, Object>> contexts = new HashMap<String, HashMap<ActiveDescriptor<?>, Object>>();
    
    private HashMap<ActiveDescriptor<?>, Object> getCurrentContext() {
        if (manager.getCurrentTenant() == null) throw new IllegalStateException("There is no current tenant");
        
        HashMap<ActiveDescriptor<?>, Object> retVal = contexts.get(manager.getCurrentTenant());
        if (retVal == null) {
            retVal = new HashMap<ActiveDescriptor<?>, Object>();
            
            contexts.put(manager.getCurrentTenant(), retVal);
        }
        
        return retVal;
    }
```java

Based on this code, it is now easy to write the find method of the TenantScopedContext:
 
```java
    public <T> T find(ActiveDescriptor<T> descriptor) {
        HashMap<ActiveDescriptor<?>, Object> mappings = getCurrentContext();
        
        return (T) mappings.get(descriptor);
    }
```java

The method that does findOrCreate is also fairly simple to write now.  If it cannot find the service in the mapping
for this tenant, then it must create one using the create method of the ActiveDescriptor, passing in the root.  The
passing in of the root allows for objects of scope PerLookup to be destroyed properly when this object gets destroyed.
    
```java
    public <T> T findOrCreate(ActiveDescriptor<T> activeDescriptor,
            ServiceHandle<?> root) {
        HashMap<ActiveDescriptor<?>, Object> mappings = getCurrentContext();
        
        Object retVal = mappings.get(activeDescriptor);
        if (retVal == null) {
            retVal = activeDescriptor.create(root);
            
            mappings.put(activeDescriptor, retVal);
        }
        
        return (T) retVal;
    }
```java

That is it for writing the Context implementation for the TenantScope!  At this point, objects will be created in the
tenant scope only when no object has already been created for that tenant.  And when a tenant is switched, a new mapping
is generated and we start the process all over again.

That is all well and good, but the question of how these objects are truly created for the TenantScope is still unclear.  For example,
we need for the Alice Environment implementation to be produced when the Alice tenant is active, and we need the Bob Environment
to be produced when the Bob tenant is active.

In order to achieve this goal, we create a factory of Environments.  A factory can be used to create objects when some criteria
that cannot be easily expressed as an injection point needs to be taken into account.  Factories
can produce things into any scope and with any qualifiers.  The Factory interface has a method on it called produce(), which
can be annotated with the scope and qualifiers that are to be associated with the produced objects.

We then implement an EnvironmentFactory, giving the type we are producing as the actual type in the parameterized type, like this:

```java
@Singleton
public class EnvironmentFactory implements Factory<Environment> {
    @Inject
    private TenantManager manager;
    
    ...
}
```java

It is interesting to notice that the factory itself is a service in the Singleton scope, and hence can be injected with the
TenantManager.  However, that does not mean that the EnvironmentFactory is producing objects into the Singleton scope, only that the
factory itself is in the Singleton scope.  We tell the system that this factory is producing items for the TenantScope by annotating
the produce method, like this:

```java
    @TenantScoped
    public Environment provide() {...}
```java

In this example, we are going to use other ServiceLocator registry's in order to create the specific Environment objects that we need.
We will have a new ServiceLocator registry for each tenant, and that new ServiceLocator will be responsible for instantiating
and providing the Environment implementations that we require.

ServiceLocators will be used as delegate for Habitat to create config instance of Environments based on a
particular backing XML file, using the configuration subsystem of hk2.

Thus the job of the EnvironmentFactory is to keep a map from tenants to their backing ServiceLocator registries.  Here is
the mapping and the code that associates a particular ServiceLocator with the current tenant:
 
```java
    private final HashMap<String, ServiceLocator> backingLocators = new HashMap<String, ServiceLocator>();
    private final TenantLocatorGenerator generator = new TenantLocatorGenerator();
    
    private ServiceLocator getCurrentLocator() {
        if (manager.getCurrentTenant() == null) throw new IllegalStateException("There is no current tenant");
        
        ServiceLocator locator = backingLocators.get(manager.getCurrentTenant());
        if (locator == null) {
            locator = createNewLocator();
            backingLocators.put(manager.getCurrentTenant(), locator);
        }
        
        return locator;
    }
    
    private ServiceLocator createNewLocator() {
        return generator.generateLocatorPerTenant(manager.getCurrentTenant());
    }
```java

The job of the TenantLocatorGenerator is to create a new ServiceLocator based on the current tenant and populate it
with values from a backing XML file.
 
```java
        ServiceLocator serviceLocator = factory.create(tenantName, parent);

        // Will add itself to serviceLocator by tenantName
        Habitat h = new Habitat(null, tenantName);
        
        // Populate this serviceLocator with config data.
        for (Populator p : serviceLocator.<Populator>getAllServices(Populator.class)) {
            p.run(new ConfigParser(h));
        }
```java

Then it is necessary to implement Populator service, as follows below. Note, Habitat is backed by ServiceLocator for tenant.
 
```java
		@Service
		public class EnvironmentXml implements Populator {
		    @Inject
		    TenantManager tenantManager;
		
		    @Inject
		    protected Habitat habitat;
		
		    @Override
		    public void run(ConfigParser parser) throws ConfigPopulatorException {
		        String tenantName = tenantManager.getCurrentTenant();
		        URL source = URL<tenantName.xml>
		        parser.parse(source, new DomDocument(habitat));
		    }
		
		}
```java
  
The code of the produce method in the EnvironmentFactory is now straightforward:

```java
    @TenantScoped
    public Environment provide() {
        ServiceLocator locator = getCurrentLocator();
        
        return locator.getService(Environment.class);
    }
```java

Voila!  We have a fairly simple example of how to create a TenantContext that meets the original requirements.  We have
created a TenantScope/TenantContext pair, and made sure it only creates objects when no object already exists for a certain
tenant.  We have also seen how to write a factory which knows how to produce Environment objects based on the currently
active tenant. 
 
Now, lets take a look at the test, and how the test validates the original requirement, which was that the
ServiceProviderEngine should be using the proper tenant based on the current state of the system, without having
to be re-injected.  Here is the pseudo-code for the test:
 
```java
    TenantManager tenantManager = locator.getService(TenantManager.class);
    ServiceProviderEngine engine = locator.getService(ServiceProviderEngine.class);
        
    tenantManager.setCurrentTenant(TenantLocatorGenerator.ALICE);
        
    // Validate that the engine is using the ALICE tenant
        
    tenantManager.setCurrentTenant(TenantLocatorGenerator.BOB);
        
    // Validate that the engine is using the BOB tenant
```java

The point of the test is to ensure that the Environment object passed into the ServiceProviderEngine is in fact getting switched
when we switch the tenant from Alice to Bob.



 
    
  
