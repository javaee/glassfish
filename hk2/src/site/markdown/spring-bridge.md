## The Spring/HK2 Bridge

The Spring/HK2 Bridge can be used to inject [Spring][spring] services
into HK2 services or inject HK2 services into [Spring][spring] services.
 
Table of Contents

+ [Definitions](spring-bridge.html#Definitions)
+ [Injecting Spring services into HK2 services](spring-bridge.html#Injecting_Spring_services_into_HK2_services)
+ [Injecting HK2 services into Spring services](spring-bridge.html#Injecting_HK2_services_into_Spring_services)
+ [Bi-Directional Spring/HK2 Bridge](spring-bridge.html#Bi-Directional_Spring/HK2_Bridge)

### Definitions

+ A [Spring][spring] service is a service that is instantiated (created) by [Spring][spring]
+ An HK2 service is a service that is instantiated (created) by HK2
 
### Injecting Spring services into HK2 services

[Spring][spring] services can be injected into any injection point in HK2.
In order to do this you must tell HK2 about the [Spring][spring] [BeanFactory][beanfactory]
which has the [Spring][spring] bean definitions. This is accomplished in two steps.
 
In the first step you should have the [ServiceLocator][servicelocator] that contains services
you wish to be injected with [Spring][spring] services.
You must initialize this [ServiceLocator][servicelocator] with some required Spring/HK2 bridge services.
You can do this using the utility class [SpringBridge][springbridge].
This is a code snippet that initializes a [ServiceLocator][servicelocator]:

```java
  SpringBridge.getSpringBridge().initializeSpringBridge(aServiceLocator);
```java

In the second step you must tell your initialized [ServiceLocator][servicelocator] about the
specific [Spring][spring] [BeanFactory][beanfactory] that you want it to look for services in.
You do this with the [SpringIntoHK2Bridge][springintohk2bridge] service that was added in the previous step.
The following code snippet adds a [Spring][spring] [BeanFactory][beanfactory] to be searched for services when injecting into HK2 services:
 
```java
  public void tieBeanFactoryToLocator(ServiceLocator aServiceLocator, BeanFactory springFactory) {
      SpringIntoHK2Bridge springBridge = aServiceLocator.getService(SpringIntoHK2Bridge.class);
      springBridge.bridgeSpringBeanFactory(springFactory);
  }
```java

 Any [Spring][spring] [BeanFactory][beanfactory] added with the bridgeSpringBeanFactory method
 will be searched for services that HK2 cannot otherwise find.
 
 For example, if you have a service called SpringService that is created by
 [Spring][spring], you can inject it into an HK2 service
 (called HK2Service) like this:
 
```java
  @Service
  public class HK2Service {
      @Inject
      private SpringService springService;
  }
```java

### Injecting HK2 services into Spring services

  HK2 services can be injected into [Spring][spring] services.  A HK2 service
  can be injected into any place a normal Spring Bean can be injected.  For example, if you have an HK2 service
  named HK2Service that is to be injected
  into a [Spring][spring] service (called SpringService) your code
  could look like this:

```java
  public class SpringService {
      private HK2Service hk2Service;
      
      public void setHK2Service(HK2Service hk2Service) {
          this.hk2Service = hk2Service;
      }
  }
```java

  All HK2 services are in a [Spring][spring]
  [Scope][scope]
  that is usually named "hk2".  In order to do this we have provided an implementation of
  [Scope][scope]
  called
  SpringScopeImpl.
  SpringScopeImpl
  is a [Spring][spring] service
  that either takes a [ServiceLocator][servicelocator] instance
  or the name of a [ServiceLocator][servicelocator] as attributes.
  
  This implementation of
  [Scope][scope]
  can be given to any [Spring][spring]
  [ConfigurableBeanFactory][configurablebeanfactory].
  The usual way this is done is in a [Spring][spring] beans.xml.  The following
  stanza adds a
  SpringScopeImpl
  for a
  [ServiceLocator][servicelocator] named HK2ToSpringTest
  into a [Spring][spring] beans.xml:
  
```xml
  <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
      <property name="scopes">
          <map>
              <entry key="hk2">
                  <bean class="org.jvnet.hk2.spring.bridge.api.SpringScopeImpl" >
	                  <property name="ServiceLocatorName" value="HK2ToSpringTest" />
	              </bean>
              </entry>
          </map>
      </property>
  </bean>
```xml

An HK2 service is then defined by adding it to the [Spring][spring] beans.xml by setting its scope to "hk2".
The id of the HK2 service is formatted as per the utility API BuilderHelper.createTokenizedFilter.
In the usual case this means that the id of the bean is the [Contract][contract] to be 
looked up (though other things can be specified such as name or qualifiers).
The following is an example [Spring][spring] beans.xml stanza that defines an HK2 service.
  
```xml
    <bean id="org.jvnet.hk2.spring.bridge.test.hk2tospring.HK2Service"
	      class="org.jvnet.hk2.spring.bridge.test.hk2tospring.HK2Service"
	      scope="hk2" />
```xml

In the stanza above the scope was set to "hk2," implying that the HK2 SpringScopeImpl 
will be used to lookup the bean.
This bean can then be injected into any other [Spring][spring] bean in the normal way.
The following stanza injects HK2Service into SpringService:
  
```xml
    <bean id="SpringService" class="org.jvnet.hk2.spring.bridge.test.hk2tospring.SpringService">
	  <property name="HK2Service" ref="org.jvnet.hk2.spring.bridge.test.hk2tospring.HK2Service" />
	</bean>
```xml

To make it clear, the following is the entire [Spring][spring] beans.xml which injects HK2Service into SpringService:
  
```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-2.5.xsd">
	
	<bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
        <property name="scopes">
            <map>
                <entry key="hk2">
                    <bean class="org.jvnet.hk2.spring.bridge.api.SpringScopeImpl" >
	                  <property name="ServiceLocatorName" value="HK2ToSpringTest" />
	                </bean>
                </entry>
            </map>
        </property>
    </bean>
	
	<bean id="org.jvnet.hk2.spring.bridge.test.hk2tospring.HK2Service"
	      class="org.jvnet.hk2.spring.bridge.test.hk2tospring.HK2Service"
	      scope="hk2" />
 
	<bean id="SpringService" class="org.jvnet.hk2.spring.bridge.test.hk2tospring.SpringService">
	  <property name="HK2Service" ref="org.jvnet.hk2.spring.bridge.test.hk2tospring.HK2Service" />
	</bean>
 
  </beans>
```xml

### Bi-Directional Spring/HK2 Bridge

When using Spring and HK2 bi-directionally it is important to remember that Spring instantiates beans
as soon as they are satisfied (i.e., as soon as all references are available).  This can make bootstrapping
difficult as Spring may try to instantiate beans before they are available in HK2.  In order to avoid this
you can use any of the methods Spring allows for lazy initialization.  The following Spring beans.xml
file uses the lazy-init mechanism for this, but other mechanism (such as the use of proxies) can
also be used.
  
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-2.5.xsd">
	
	<bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
        <property name="scopes">
            <map>
                <entry key="hk2">
                    <bean class="org.jvnet.hk2.spring.bridge.api.SpringScopeImpl" >
	                  <property name="ServiceLocatorName" value="BiDirectionalSpringBridge" />
	                </bean>
                </entry>
            </map>
        </property>
    </bean>
	
	<bean id="org.jvnet.hk2.spring.bridge.test.bidirectional.HK2Service1_0"
	      class="org.jvnet.hk2.spring.bridge.test.bidirectional.HK2Service1_0"
	      scope="hk2" 
	      lazy-init="true"/>
 
	<bean id="SpringService1_1"
	      class="org.jvnet.hk2.spring.bridge.test.bidirectional.SpringService1_1"
	      lazy-init="true">
	  <property name="HK2Service1_0" ref="org.jvnet.hk2.spring.bridge.test.bidirectional.HK2Service1_0" />
	</bean>
	
	<bean id="org.jvnet.hk2.spring.bridge.test.bidirectional.HK2Service1_2"
	      class="org.jvnet.hk2.spring.bridge.test.bidirectional.HK2Service1_2"
	      scope="hk2" 
	      lazy-init="true"/>
	      
	<bean id="SpringService1_3"
	      class="org.jvnet.hk2.spring.bridge.test.bidirectional.SpringService1_3"
	      lazy-init="true">
	  <property name="HK2Service1_2" ref="org.jvnet.hk2.spring.bridge.test.bidirectional.HK2Service1_2" />
	</bean>
 
</beans>
```xml

In the above example SpringService1_3 has HK2Service1_2 injected into it, while HK2Service1_2 has
SpringService1_1 injected into it (though you can't see that in the XML stanza), and SpringService1_1
has HK2Service1_0 injected into it.  Since everything in the beans.xml is lazily initialized you
can start the Spring BeanFactory either before or after you initialize the ServiceLocator.

[spring]: http://www.springsource.com/
[beanfactory]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/beans/factory/BeanFactory.html
[servicelocator]: apidocs/org/glassfish/hk2/api/ServiceLocator.html
[contract]: apidocs/org/jvnet/hk2/annotations/Contract.html
[configurablebeanfactory]: http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/beans/factory/config/ConfigurableBeanFactory.html
[scope]: http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/beans/factory/config/Scope.html
[springintohk2bridge]: apidocs/org/jvnet/hk2/spring/bridge/api/SpringIntoHK2Bridge.html
[springbridge]: apidocs/org/jvnet/hk2/spring/bridge/api/SpringBridge.html