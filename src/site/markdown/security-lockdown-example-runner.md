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

## Security Lockdown Example

In this example we will show how to check every injection, lookup, bind
and unbind operation against a set of J2SE security permissions.  In order
to do so we have constructed four projects who have a different set of
permissions in the system.  Lets take a brief glimpse of each project
in the example, to get an overview of what they do and how they interact
with the system.  

The first project is the runner project, which is nothing more than a set of
junit tests which run various scenarios, showing how security is either granted
or denied to the other projects.

The next project is the system project, which is considered to be secure and
trusted system code.  This code will receive AllPermission and therefor is able
to manipulate any object in the system and perform any action.  The system code
contains things like the security validation service itself, along with other
secure code such as security services.

Another project is called "Alice."  Alice is an application provider that is
able to access security services.  Alice will not attempt to do anything bad, but
will remain within the confines of the permissions granted her.  However, Alice
does not have AllPermission, and therefore could not perform some operations even
if she tried (however, she never tries to do anything she shouldn't).

Mallory is the last project.  Mallory is another application provider, who also
has some access to system resources.  In fact, Mallory has access to the Alice application.
Mallory usually sticks to the bounds of what he is allowed to do, but sometimes will attempt
to perform operations that he is not allowed to perform.  We shall see that when he attempts
to do so he is rebuffed by the security operations of the system.

Lets take a look in detail at how this total security lockdown of the system occurs.
 
### The System Project

The system project has some resources it would like to give out to some applications,
but not to others.  It would also like to be able to prevent applications from
binding or unbinding services from the service registry (embodied by the [ServiceLocator][servicelocator].

In order to do this, the system project must decide on a protection scheme.  Therefore the
decision is made that only code with java.lang.AllPermission shall be able to bind or unbind
services.  In order to protect lookup and injection services the decision is made to use
the java.lang.RuntimePermission called "accessClassInPackage".  This permission is used to allow
code to access classes in a particular package, and so seems appropriate for also protecting services
that are from that particular package.

It should be noted that the above decisions were made arbitrarily for this example, and real system
code can use whatever permission scheme it desires, including creating and using its own Permission
classes.

Lets first take a look at the resource that the system project would like to protect.  You will see
in the package "org.acme.security" that there is a service called AuditService.  AuditService is an
extremely simple secure auditing service.  The system code would like to allow some applications access
to this service, and other applications to NOT be allowed to directly use this service.  It should be noted
that the code that CAN access the service may be able to use that service on behalf of the callers of that
service.  That sort of thing will all work according to the normal J2SE security model.  Here is the AuditService,
a service that should be protected:
 
```java
@Service @Singleton
public class AuditService {
    ...
}
```java

The most important thing to notice about this service is that there is nothing at all special about it.  It is
not annotated with any sort of special annotation or indication that it is a secure or protected service.  This
is because in some environments this service may be protected, while in other environments it may not be protected.
It is up to the environment to decide which resources need to be protected, and which do not.  In this way the
burden of deciding which resources are to be protected and which are not are taken away from the developers of the
resource and put into the hands of the definition of the particular environment in which those resources run.  This
is a good thing, as it puts the burden of deciding which resources are to be protected into the hands of those who
can better make that decision.
 
But how *does* the environment decide which resources are protected, and which are not?  In HK2 the answer is a
system defined service called the [ValidationService][validationservice].
The [ValidationService][validationservice] has two functions.  The first
function is to decide which resources should be protected.  The second is to provide code that must be run in
order to protect the resources which are protected.
 
In this example, the system has decided that every resource must be protected.  Hence the name "security-lockdown"
example.  Every injection point, lookup and bind will first run through a
[Validator][validator] to decide whether or not that operation should be allowed
to proceed.  But before the [Validator][validator] is run on anything first the
[ValidationService][validationservice] must tell the system that all resources
are to be protected.
 
In order to do this the system project must implement the
[ValidationService][validationservice].  In doing so it must implement
the getLookupFilter API, which returns a [Filter][filter] to
the system.  The system will use this filter to determine which resources are protected.  If the filter returns
true about any resource, then that resource will have the [Validator][validator]
run over it whenever it is looked up or injected.  Note that bind and unbind operations are always run through
all registered validators, as these operations are rare and can have serious effects on the state of the system.

In our example, where we want to protect every resource, we implement the getLookupFilter in this way:
 
```java
@Service @Singleton
public class ValidationServiceImpl implements ValidationService {
    ...
    
    @Override
    public Filter getLookupFilter() {
        return BuilderHelper.allFilter();
    }
    
    ...
}
```java

The[BuilderHelper][buildhelper] allFilter method returns a [Filter][filter] that always returns true.
In this way we have assured that every lookup and injection in the system will be run through the implementation of 
[Validator][validator], which is also returned by out implementation of [ValidationService][validationservice]:
 
```java
@Service @Singleton
public class ValidationServiceImpl implements ValidationService {
    private final ValidatorImpl validator = new ValidatorImpl();
    
    ...

    @Override
    public Validator getValidator() {
        return validator;
    }
}
```java

Note that since the ValidationService is a Singleton service that the ValidatorImpl will only be created once,
and the same one will be returned to the system every time the getValidator method is called.

However, the most important thing about the system code is how it protects the resources it has been charged
to protect.  That is the job of the ValidatorImpl.  The ValidatorImpl is responsible for determining
how to validate the BIND, UNBIND and LOOKUP operations.
Here is the implementation of the [Validator][validator] validate method:
 
```java
public class ValidatorImpl implements Validator {
    ...
    
    @Override
    public boolean validate(ValidationInformation info) {
        switch(info.getOperation()) {
        case BIND:
        case UNBIND:
            return validateBindAndUnbind();
        case LOOKUP:
            return validateLookup(info.getCandidate(), info.getInjectee());
        default:
            return false;
        }
    }
    
    ...
    
}
```java

It is very important to understand the context in which these calls are made.  In that way it can be determined
how to check the permission being granted.  In the BIND, UNBIND, and in any LOOKUP that is *not* an injection
the call frame will be the Java protection domain to check.  In the case of the
BIND and UNBIND operations this will include the code that called commit on the [DynamicConfiguration][dynamicconfiguration].
In the case of the non-inject lookup, this will contain the user that is attempting to look up a service.

Since the BIND and UNBIND cases are fairly strait-forward in our example, lets see how this permission
check is performed.  First, we make a checkPermission call that returns true or false depending on whether
or not the permission is granted or not.  Here it is:
 
```java
    private static boolean checkPerm(Permission p) {
        try {
            AccessController.checkPermission(p);
            return true;
        }
        catch (AccessControlException ace) {
            return false;
        }
    }
```java

This converts any AccessControlException (an indication that the permission was NOT granted) into a simple
false value.  Otherwise, if the checkPermission call is successful this method will return true.  It should
be noted that using the checkPermission in this way will only work properly when the code to be checked is
on the call stack, and that every piece of code on the call-stack must have the permission being asked for.
For the BIND and UNBIND operations then, the check is easy, since the caller is on the stack and this method
is easy to use:

```java
    private boolean validateBindAndUnbind() {
        return checkPerm(new AllPermission());
    }
```java

This is very simple and ensures that anyone who tries to BIND or UNBIND service descriptions into the system
must have AllPermission!

Now lets talk about the LOOKUP case.  The LOOKUP case can be broken down into two cases, the LOOKUP via an API
case and the injection case.  Both are doing a lookup of a service.  However in the first case (pure lookup) the
caller is directly on the stack.  In the second case there may be no other software on the stack other than the
system software.  Therefore in that case more information may be needed in order to perform our security check.
That extra information comes in the form of the [Injectee][injectee].  Using the combination of the candidate
[ActiveDescriptor][activedescriptor] and the
place that descriptor would be injecting into (the
[Injectee][injectee]) a security decision can be made about the
injection point.
 
Lets look at the method that handles the both LOOKUP cases:
 
```java
    private boolean validateLookup(ActiveDescriptor<?> candidate, Injectee injectee) {
        if (injectee == null) {
            return validateLookupAPI(candidate);
        }
        
        return validateInjection(candidate, injectee);
    }
```java

As you can see from the above code, the lookup has been broken into two cases, the
lookup with the API case, and the lookup based on the injection point case.  The
lookup with the API case is similar to the BIND and UNBIND cases, since the permission
check simply goes against the current call stack.

Before we look at how validateLookupAPI is implemented,  lets look at how we create the
set of permissions that are necessary for the caller to be allowed to use the lookup API.
Firstly, we create a RuntimePermission for a particular package by calling this
method:
 
```java
    private final static String ACCESS_IN_PACKAGE = "accessClassInPackage.";
    
    private static Permission getLookupPermission(String packName) {
        RuntimePermission retVal = new RuntimePermission(ACCESS_IN_PACKAGE + packName);
        
        return retVal;
    }
```java

The above code will create the Permission that needs to be checked to see if the
caller should have access to the passed in package.

When dealing with an [ActiveDescriptor][activedescriptor],
we want to be sure that the caller has the right to access all of the contracts which
that service advertises.  Hence we create a method that returns all of the permissions
that must be checked based on all of the contracts that the
[ActiveDescriptor][activedescriptor] advertises.
Here is the implementation of a method that returns a list of all permissions that
must be allowed in order to access that [ActiveDescriptor][activedescriptor]:
 
```java
    private static List<Permission> getLookupPermissions(ActiveDescriptor<?> ad) {
        LinkedList<Permission> retVal = new LinkedList<Permission>();
        
        for (String contract : ad.getAdvertisedContracts()) {
            int index = contract.lastIndexOf('.');
            String packName = contract.substring(0, index);
            retVal.add(getLookupPermission(packName));
        }
        
        return retVal;
    }
```java

Forearmed with the above methods, we can now look at how the method which
checks whether or not the lookup should be authorized.  Here is the implementation
of validateLookupAPI:
 
```java
    private boolean validateLookupAPI(ActiveDescriptor<?> candidate) {
        List<Permission> lookupPermissions = getLookupPermissions(candidate);
     
        for (Permission lookupPermission : lookupPermissions) {
            if (!checkPerm(lookupPermission)) {
                if (VERBOSE) {
                    System.out.println("candidate " + candidate +
                        " LOOKUP FAILED the security check for permission " + lookupPermission);
                }
                return false;
            }
        }
        
        return true;
    }
```java

This is a strait-forward implementation, simply ensuring with the checkPerm call that
the caller is allowed to access all of the contracts offered by the given candidate
[ActiveDescriptor][activedescriptor].

That leaves us with the case of the lookup being performed on behalf of an injection
point.  This sort of lookup might be done because of some system code, or on the behalf
of a chain of instantiations being done due to a dependency chain.  Hence, it may
not be the case that the proper protection domain is on the calling stack.  So
in this case we must use the protection domain of the injection point in order to
see whether or not the code representing the injection point itself has the
proper permissions.

Here is the implementation of the validateInjection method:
  
```java
    private boolean validateInjection(ActiveDescriptor<?> candidate, Injectee injectee) {
        List<Permission> lookupPermissions = getLookupPermissions(candidate);
        
        // If this is an Inject, get the protection domain of the injectee
        final Class<?> injecteeClass = injectee.getInjecteeClass();
        
        ProtectionDomain pd = AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>() {

            @Override
            public ProtectionDomain run() {
                return injecteeClass.getProtectionDomain();
            }
            
        });
        
        for (Permission lookupPermission : lookupPermissions) {
            if (!pd.implies(lookupPermission)) {
                if (VERBOSE) {
                    System.out.println("candidate " + candidate + " injectee " + injectee +
                        " LOOKUP FAILED the security check for " + lookupPermission);
                }
                
                return false;
            }
        }
        
        return true;
    }
```java

This method first gets the set of permissions that the protection domain must have using
the getLookupPermissions method.  It then gets the ProtectionDomain of the injectee
by getting the ProtectionDomain from the Class of the injection point.  It should be noted
that getting the ProtectionDomain from the Class is itself a priviliged operation, and hence
must be done inside a doPrivileged block, in order to ensure that only the rights of the
system software are checked when getting the protection domain.

The code then runs through the list of permissions that the ProtectionDomain of the [Injectee][injectee] must have in order for
the injection to use an object produced by the [ActiveDescriptor][activedescriptor].

This is the entire implementation of the security-lockdown validator, which will ensure that
only those that have the rights to specific packages will be able to inject or lookup
services from those packages, and that only those who have AllPermission will be able
to bind or unbind services from the system.

Note that the System Project has the following grant:
 
```
grant codeBase "file:${build.dir}/../../system/target/-" {
  permission java.security.AllPermission;
};
```
 
Now lets take a closer look at Alice and Mallory.
 
### Alice

Alice is a simple application that has a limited set of privileges.  Here are the exact
priviliges that Alice is granted:
 
```
grant codeBase "file:${build.dir}/../../alice/target/-" {
  permission java.lang.RuntimePermission "accessClassInPackage.javax.inject";
  permission java.lang.RuntimePermission "accessClassInPackage.com.alice.application";
  permission java.lang.RuntimePermission "accessClassInPackage.org.jvnet.hk2.annotations";
  permission java.lang.RuntimePermission "accessClassInPackage.org.acme.security";
};
```

One thing that will distinguish Alice from Mallory is that Alice has access to the
org.acme.security package, whereas Mallory does not.  The means that Alice can
inject the org.acme.security.AuditService into its services, while Mallory cannot.
Here is the very simple AliceApp, which injects the AuditService:
 
```java
@Service @Singleton
public class AliceApp {
    @Inject
    private AuditService auditor;
    
    /**
     * This service makes use of the auditor service directly, which
     * should be allowed as Alice itself has the ability to use the
     * audit service
     * 
     * @param fromMe
     */
    public void doAuditedService(String fromMe) {
        auditor.auditLog("Alice performed an audit for " + fromMe);
    }
}
```java

The injection point for the AuditService will pass the validator code because Alice
has access to org.acme.service package.  Now lets look at Mallory.
 
### Mallory

Mallory is another application, but unlike Alice, Mallory will sometimes attempt to do
things which it is not allowed to do.  Since Mallory is less trusted by the administrator,
Mallory has different grants:

```
grant codeBase "file:${build.dir}/../../mallory/target/-" {
  permission java.lang.RuntimePermission "accessClassInPackage.javax.inject";
  permission java.lang.RuntimePermission "accessClassInPackage.com.mallory.application";
  permission java.lang.RuntimePermission "accessClassInPackage.com.alice.application";
  permission java.lang.RuntimePermission "accessClassInPackage.org.jvnet.hk2.annotations";
  permission java.lang.RuntimePermission "accessClassInPackage.org.glassfish.hk2.api";
};
```

Note that Mallory does NOT have access to the org.acme.security package, and hence should
not be able to inject or lookup anything from that package.

Note that sometimes Mallory acts according to the rules, and other times he acts against the
rules.  Lets look at the times Mallory acts according to the rules:
 
```java
@Service
public class MalloryApp {
    @Inject
    private AliceApp alice;
    
    @Inject
    private ServiceLocator locator;
    
    ...
    
    public void doAnApprovedOperation() {
        alice.doAuditedService("Mallory");
    }
    
    ...
}
```java

Mallory injects both the ServiceLocator itself as well as the AliceApp.  Since Mallory has
been granted those rights, these injections are Validated by the Validator and found to be
perfectly OK.  Hence this service can properly be instantiated.

Now lets see what Mallory might try to do that it shouldn't.  In this case, Mallory attempts
to look up the AuditService, which he should not be allowed to do:
 
```java
    /**
     * Mallory cannot however lookup the AuditService himself!
     */
    public void tryToGetTheAuditServiceMyself() {
        Object auditService = locator.getBestDescriptor(BuilderHelper.createContractFilter(
            "org.acme.security.AuditService"));
        auditService.toString();  // This should NPE!
    }
```java

Note that in this code that tricky fellow Mallory used a pure string representation of the
fully qualified class name of the AuditService, since the system would not have allowed him
access to the AuditService otherwise.  The code above will NPE when the auditService.toString()
method is called.  As far as this lookup is concerned, it acts as though there is no AuditService
to be had, rather than throwing some other form of exception (which might tell Mallory something
about the system).

In this method, Mallory will attempt to advertise a service:
 
```java
    /**
     * Mallory cannot advertise the EvilService
     */
    public void tryToAdvertiseAService() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(EvilService.class);
        
        config.commit();  // This will throw a MultiException
    }
```java

Since Mallory does not have the rights to advertise a service the commit call will throw
an exception.

In this method, Mallory will attempt to unadvertise the ServiceLocator itself!!!  That
can't be allowed, or the very fabric of space-time could be threatened:
 
```java
    /**
     * Mallory cannot un-advertise ServiceLocator!
     */
    public void tryToUnAdvertiseAService() {
        final Descriptor locatorService = locator.getBestDescriptor(
            BuilderHelper.createContractFilter(ServiceLocator.class.getName()));
        
        // This filter matches ServiceLocator itself!
        Filter unbindFilter = new Filter() {

            @Override
            public boolean matches(Descriptor d) {
                if (d.getServiceId().equals(locatorService.getServiceId())) {
                    if (d.getLocatorId().equals(locator.getLocatorId())) {
                        return true;
                    }
                }
                
                return false;
            }
            
        };
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addUnbindFilter(unbindFilter);
        
        config.commit();  // This will throw a MultiException
    }
```java

Again, since Mallory does not have the right to unbind services from the registry, the
commit method will throw an exception and the entire operation will fail.

In the next act of evil that Mallory attempts to perpetrate, it will try to instantiate this
service:
 
```java
@Service @Singleton
public class EvilInjectedService {
    @Inject
    private AuditService auditor;
    
    public EvilInjectedService() {
        throw new AssertionError("This should never get called since AuditService is not available");
    }

}
```java

In this case Mallory is not doing a direct API lookup of the AuditService, but rather the
EvilInjectedService is trying to simply inject the service into itself, hoping that this
mode of access would be allowed.  Unfortunately, Mallory has no such luck in this method
of MalloryApp:
 
```java
    /**
     * Tries to instantiate a service with an illegal injection point
     */
    public void tryToInstantiateAServiceWithABadInjectionPoint() {
        locator.getService(EvilInjectedService.class);  // Will throw MultiException
    }
```java

In this case the system will attempt to instatiate the EvilInjectedService and will fail,
since the EvilInjectedService does not have the rights to inject the AuditService.

That's it for Mallory!  Mallory was allowed to perform the operations he was allowed to
perform, and was not allowed to perform the operations for which it does not have the
proper set of priviliges.

The last project in the example is the runner.
 
### The Runner Project

The Runner project runs a set of junit tests that ensures that the System project, the Alice
project and the Mallory project runs as expected.  The test case ensures that both the
AliceApp and MalloryApp come up, and that when Mallory tries to do things that he should not
do that he is properly rebuffed.  The most interesting thing in the Runner project is in the
pom.xml, which gives the information that is needed to run the security-lockdown example
with the java security manager enabled.  This is from the pom.xml:
 
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <argLine>-Dlocal.repo=${settings.localRepository}
             -Dbuild.dir=${project.build.directory}
             -Djava.security.manager
             -Djava.security.policy=${project.build.directory}/test-classes/policy.txt
    </argLine>
    <!-- -Djava.security.debug=access,failure,domain -->
    </configuration>
</plugin>
```xml

The other interesting file in the Runner project is the policy.txt, which gives the full
set of grants necessary to run this test properly.
 
### Conclusion

This example shows how a secure system can be built in HK2 given the tools provided.  In
a real use case other decisions can be made about how to validate the operations being
performed.  This example is here to illustrate how those decisions can be put into effect, not
what those decisions should be.

[servicelocator]: apidocs/org/glassfish/hk2/api/ServiceLocator.html
[validationservice]: apidocs/org/glassfish/hk2/api/ValidationService.html
[validator]: apidocs/org/glassfish/hk2/api/Validator.html
[filter]: apidocs/org/glassfish/hk2/api/Filter.html
[buildhelper]: apidocs/org/glassfish/hk2/utilities/BuilderHelper.html
[dynamicconfiguration]: apidocs/org/glassfish/hk2/api/DynamicConfiguration.html
[activedescriptor]: apidocs/org/glassfish/hk2/api/ActiveDescriptor.html