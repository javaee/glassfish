## Integration


### GlassFish integration

HK2 can be used in GlassFish applications.
Every deployed GlassFish application has a unique ServiceLocator associated with it that can be looked up with JNDI name **java:app/hk2/ServiceLocator**.
This ServiceLocator will not have a parent, and will be destroyed when the application is undeployed. 
The following is example code that returns the application ServiceLocator:

```java
  public ServiceLocator getServiceLocator() {

        try {
          Context context = new InitialContext();

          return (ServiceLocator) context.lookup("java:app/hk2/ServiceLocator");
        }
        catch (NamingException ne) {
            return null;
        }
    }
```java

There are several options for populating the per-application ServiceLocator. The first is to use the [hk2-inhabitant-generator][inhabitant-generator]. 
For EJBs and Library JAR files the system will read files named application located in **META-INF/hk2-locator/**. For war files the system will read files named application located in WEB-INF/classes/hk2-locator.

The following is an example maven stanza using the [hk2-inhabitant-generator][inhabitant-generator] to place the inhabitant file of an EJB in the proper place:

```xml
    <build>
      <plugins>
          <plugin>
                <groupId>org.glassfish.hk2</groupId>
                <artifactId>hk2-inhabitant-generator</artifactId>
                <executions>
                    <execution>
                        <configuration>
                            <locator>application</locator>
                        </configuration>
                        <goals>
                            <goal>generate-inhabitants</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```xml
Note that the same stanza can be used for a WAR file, and if the packaging type of the pom is "war" then the [hk2-inhabitant-generator][inhabitant-generator] will automatically put the generated inhabitant file into the correct place.

All inhabitant files are read when the application is deployed, and hence should be able to be looked up from the application [ServiceLocator][serviceLoc].
One can also use the [DynamicConfigurationService][dynamicConf] in order to add services as per any normal HK2 installation. 

This also works along with the [Binder][bindeer]: and [BuilderHelper][buildhelper] service builders.

### CDI Integration

HK2 is fully integrated with the GlassFish 4.0 CDI implementation.

In other words, services created with CDI can be injected into services created with HK2, and services created with HK2 can be injected into services created with CDI. 
It should be noted that if left alone, CDI will think that nearly every object is a CDI object, and hence it is best to let CDI create most of your objects, unless you are using specific features of HK2 that cannot be achieved with CDI.

Furthermore, only HK2 services that have been loaded with **META-INF/hk2-locator/application** (for EJB and JAR) and **WEB-INF/classes/hk2-locator/application** (for WARs) can be injected into CDI services.

This is because CDI does early validation of all injection points, and hence all services that are to be injected into CDI must be present prior to the CDI validation phase.
The CDI validation phase occurs prior to any application code being run.
Due to the dynamic nature of HK2 services, CDI services can be injected into HK2 services that were created at any time in the life of the application.

### Guice

HK2 services can be injected into [Guice][guice] services and [Guice][guice] services can be injected into HK2 services.
To do so use the Guice/HK2 bridge, which is described [here](guice-bridge.html).
  
### Spring

HK2 services can be used as [Spring][spring] beans, and [Spring][spring] beans can be injected into
HK2 services.  To do so use the Spring/HK2 bridge, which is described [here](spring-bridge.html).

[inhabitant-generator]: inhabitant-generator.html
[serviceLoc]: apidocs/org/glassfish/hk2/api/ServiceLocator.html
[dynamicConf]: apidocs/org/glassfish/hk2/api/DynamicConfigurationService.html
[bindeer]: apidocs/org/glassfish/hk2/utilities/Binder.html
[buildhelper]: apidocs/org/glassfish/hk2/utilities/BuilderHelper.html
[guice]: http://code.google.com/p/google-guice
[spring]: http://www.springsource.org