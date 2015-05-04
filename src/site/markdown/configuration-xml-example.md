## XML Configuration Example

This page shows an example of how to use the HK2 XML configuration service in order to inject
values into HK2 services from an XML file.

The example itself is that of a fake application that can boot multiple web servers, 
Each web server has three ports it can open: an admin port, a SSL port and a normal HTTP port.
The SSL port and HTTP port can be dynamically changed while the web server is up and running, but the
admin port cannot.  Individual web servers can be added and removed at runtime to the
application.  Changes made dynamically at runtime can be written out to an XML file in order
to save the current state of the application.

### The XML Configuration File

The application is to be configured with an XML file.  An example XML file is found in the
src/test/resources directory of the example and is named webserverExample1.xml.
It looks like this:

```xml
<application>
  <web-server name="Development Server">
    <adminPort>8001</adminPort>
    <port>8002</port>
    <SSLPort>8003</SSLPort>
  </web-server>
  
  <web-server name="QA Server">
    <adminPort>9001</adminPort>
    <port>9002</port>
    <SSLPort>9003</SSLPort>
  </web-server>
  
  <web-server name="External Server">
    <adminPort>10001</adminPort>
  </web-server>
</application>
```xml

### The Example Application

This application will have three web servers started.  The properties of each of the servers is defined in the XML file.
The HK2 XML Service uses JAXB to parse XML files.  In most JAXB applications the users must supply an
annotated Java concrete class or use the automatically generated implementations JAXB can generate from schema.
However the HK2 XML Service allows the user to use an interface annotated with JAXB annotations (and a few optional
HK2 specific annotations) rather than a concrete Java class.  As an example, lets look at the WebServerBean, which
is an interface that is annotated with JAXB annotations.  Comments have been removed for brevity.

```java
package org.glassfish.examples.configuration.xml.webserver;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.glassfish.hk2.xml.api.annotations.Hk2XmlPreGenerate;
import org.glassfish.hk2.xml.api.annotations.XmlIdentifier;
import org.jvnet.hk2.annotations.Contract;

@Contract
@Hk2XmlPreGenerate
public interface WebServerBean {
    @XmlAttribute(required=true)
    @XmlIdentifier
    public String getName();
    public void setName(String name);
    
    @XmlElement
    public String getAddress();
    public void setAddress(String address);
    
    @XmlElement(defaultValue="1007")
    public int getAdminPort();
    public void setAdminPort(int adminPort);
    
    @XmlElement(name="SSLPort", defaultValue="81")
    public int getSSLPort();
    public void setSSLPort(int sslPort);
    
    @XmlElement(defaultValue="80")
    public int getPort();
    public void setPort(int port);
}
```java

XmlAttribute and XmlElement are standard JAXB annotations that would normally only go onto concrete classes.
[XmlIdentifier][xmlidentifier] and [Hk2XmlPreGenerate][hk2xmlpregenerate] are HK2 extensions that will be 
explained later.  The HK2 XML service will generate a proxy for this interface, copying over all annotations
(and in some cases making slight modifications to them in order to ensure JAXB can parse the XML properly).
[Contract][contract] is the standard HK2 annotation that denotes that this interface should be included when
doing automatic service analysis.

[XmlIdentifier][xmlidentifier] is an HK2 XML Service annotation that tells the HK2 XML Service that the attribute represented
by the getter or setter can be used as the key for this bean when the bean is a child of another bean.
It is very much like the JAXB annotation XmlID except that in the case of the XmlID the key must be unique
within the scope of the entire document, whereas with XmlIdentifier the scope of uniqueness only needs
to be within the xpath of the stanza.

[Hk2XmlPreGenerate][hk2xmlpregenerate] is an HK2 XML Service annotation that tells HK2 that the proxy
for this bean should be generated at build time and placed within the JAR being built.  This uses the
standard JSR-269 annotation processor and so should work with most build systems including ant, maven,
gradle and others.  The only requirement is that the HK2 XML Service jar be in the classpath of the compiler.
Note that if the [Hk2XmlPreGenerate][hk2xmlpregenerate] annotation is NOT put on the interface class then the
proxy will be generated dynamically at runtime, so using the [Hk2XmlPreGenerate][hk2xmlpregenerate] annotation
is mainly a matter of runtime performance, since proxy generation can be a heavy operation.

Lets take a look at the ApplicationBean, which has the WebServer bean as a child:

```java
package org.glassfish.examples.configuration.xml.webserver;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jvnet.hk2.annotations.Contract;

@Contract
@XmlRootElement(name="application")
public interface ApplicationBean {

    @XmlElement(name="web-server")
    public WebServerBean[] getWebServers();
    public void setWebServers(WebServerBean[] webServers);
    public void addWebServers(WebServerBean addMe);
    public void removeWebServers(String removeMe);
    public WebServerBean lookupWebServers(String findMe);

}
```java

The ApplicationBean has a standard JAXB getter and setter for the WebServerBean which is annotated with the
JAXB standard annotation XmlElement.  Note that like all JAXB annotations the annotation can be placed
on the setter method or the getter method.  Most JAXB annotations can also be placed on fields of concrete
classes, but since this is an interface they are placed on either the getter or setter.  The
methods addWebServers, removeWebServers and lookupWebServers are methods that the HK2 XML Service understands
are part of the web-server family of methods and allows the users of the API to add, remove and
locate WebServers.  The XmlRootElement annotation is a standard JAXB annotation that is placed on classes
that can be at the root of the XML file.

Note that in this example we chose to use an array type as the return from getWebServers and as the
input to setWebServers but we could have also used a qualified java.util.List.

NOTE:  At the current time there is no support for plurals.  This is a bug that should be fixed.

### [XmlService][xmlservice]

Now we need to parse our XML into these Java Beans.  To do this we use the [XmlService][xmlservice].
You add the XmlService to any locator by using the method enableXmlService in
[XmlServiceUtilities][xmlserviceutilities].

The tests cases for this example use the [HK2Runner][hk2runner], which allows the test
case itself (WebServerXmlTest.java) to directly inject the XmlService after the [HK2Runner][hk2runner]
has been initialized.  The following junit @Before block initializes the [HK2Runner][hk2runner]
and then initializes the [XmlService][xmlservice] using the [XmlServiceUtilities][xmlserviceutilities].

```java
    @Before
    public void before() {
        initialize();
        
        XmlServiceUtilities.enableXmlService(testLocator);
    }
```java

The testLocator is from the [HK2Runner][hk2runner] and is available after the initialize call,
which is also responsible for injecting the [XmlService][xmlservice] provider so that it
can be used by the individual tests:

```java
    @Inject
    private Provider<XmlService> xmlServiceProvider;
```java

We can now look at how to unmarshall the XML into the ApplicationBean in the first test
in WebServerXmlTest:

```java
    @Test
    public void testParseWebServerXmlFile() throws Exception {
        XmlService xmlService = xmlServiceProvider.get();
        
        URI webserverFile = getClass().getClassLoader().getResource(EXAMPLE1_FILENAME).toURI();
        
        XmlRootHandle<ApplicationBean> applicationRootHandle =
                xmlService.unmarshall(webserverFile, ApplicationBean.class);
        
        ApplicationBean root = applicationRootHandle.getRoot();
        WebServerBean webservers[] = root.getWebServers();
        
        Assert.assertEquals(3, webservers.length);
        
        {
            WebServerBean developmentServer = webservers[0];
            Assert.assertEquals("Development Server", developmentServer.getName());
            Assert.assertEquals(8001, developmentServer.getAdminPort());
            Assert.assertEquals(8002, developmentServer.getPort());
            Assert.assertEquals(8003, developmentServer.getSSLPort());
        }
        
        {
            WebServerBean qaServer = webservers[1];
            Assert.assertEquals("QA Server", qaServer.getName());
            Assert.assertEquals(9001, qaServer.getAdminPort());
            Assert.assertEquals(9002, qaServer.getPort());
            Assert.assertEquals(9003, qaServer.getSSLPort());
        }
        
        {
            WebServerBean externalServer = webservers[2];
            Assert.assertEquals("External Server", externalServer.getName());
            Assert.assertEquals(10001, externalServer.getAdminPort());
            Assert.assertEquals(80, externalServer.getPort());
            Assert.assertEquals(81, externalServer.getSSLPort());
        }
     }
```java

The XML file has been unmarshalled into the ApplicationBean and into the three children WebServerBeans.
Note that even the default values have been filled into the getPort and setSSLPort methods from
the External Server WebServer, whose values were not specified directly in the XML file.

The [XmlService][xmlservice] generated an [XmlRootHandle][xmlroothandle] which can be used to
get at the root bean of the JavaBean tree.

The example above illustrates one way to get the data from your XML configuration file and
into your application, which is to directly use the root Java Bean that is produced from the
[XmlRootHandle][xmlroothandle].  There are two other ways to get the data from your XML file
into your application.  They are:

1.  The individual Java Beans are put into the HK2 Service registry and so can be used like normal services
2.  You can use the [ConfiguredBy][configuredby] scope to have HK2 services whose lifecycle is managed by the Java Bean tree

The following two sections will explain each option.

### Unmarshalled Java Beans as HK2 services

When you unmarshall XML with the [XmlService][xmlservice] unmarshall method and the interfaces
in the Java Bean tree are marked with [Contract][contract] then those beans will be
added to the HK2 service registry.  If the Java Bean has a field marked with either
[XmlIdentifier][xmlidentifier] or the standard JAXB XmlID annotation then the service
will be put into the HK2 service registry with that field as its name.  So in this
example three services would be put into the Service registry with contract
WebServerBean, each one with a different name (&quot;Development Server&quot;,
&quot;QA Server&quot; and &quot;External Server&quot;).

The following example has a WebServerManager that injects an [IterableProvider][iterableprovider]
of WebServerBean.  The named method of the [IterableProvider][iterableprovider] allows
the WebServerManager to find the correct WebServerBean given the desired name.
Here is the code for the WebServerManager:

```java
@Service
public class WebServerManager {
    @Inject
    private IterableProvider<WebServerBean> allWebServers;
    
    /**
     * Gets the WebServer bean with the given name, or null if
     * none can be found with that name
     * 
     * @param name The non-null name of the web server to find in
     * the HK2 service registry
     * @return the WebServerBean HK2 service with the given name
     */
    public WebServerBean getWebServer(String name) {
        return allWebServers.named(name).get();
    }
}
```java

The test file for this use case is WebServersAsHK2ServicesTest and is mostly the
same as the previous example.  In this case the test gets the WebServerManager
service and uses that to query for WebServersBeans as HK2 services from
the WebServerManager.  It then validates that the expected values for each
WebServerBean are as expected:

```java
    @Test
    public void testWebServerBeansAreHK2Services() throws Exception {
        XmlService xmlService = xmlServiceProvider.get();
        
        URI webserverFile = getClass().getClassLoader().getResource(EXAMPLE1_FILENAME).toURI();
        
        XmlRootHandle<ApplicationBean> applicationRootHandle =
                xmlService.unmarshall(webserverFile, ApplicationBean.class);
        
        WebServerManager manager = testLocator.getService(WebServerManager.class);
        Assert.assertNotNull(manager);
        
        {
            WebServerBean developmentServer = manager.getWebServer("Development Server");
            Assert.assertEquals("Development Server", developmentServer.getName());
            Assert.assertEquals(8001, developmentServer.getAdminPort());
            Assert.assertEquals(8002, developmentServer.getPort());
            Assert.assertEquals(8003, developmentServer.getSSLPort());
        }
        
        {
            WebServerBean qaServer = manager.getWebServer("QA Server");
            Assert.assertEquals("QA Server", qaServer.getName());
            Assert.assertEquals(9001, qaServer.getAdminPort());
            Assert.assertEquals(9002, qaServer.getPort());
            Assert.assertEquals(9003, qaServer.getSSLPort());
        }
        
        {
            WebServerBean externalServer = manager.getWebServer("External Server");
            Assert.assertEquals("External Server", externalServer.getName());
            Assert.assertEquals(10001, externalServer.getAdminPort());
            Assert.assertEquals(80, externalServer.getPort());
            Assert.assertEquals(81, externalServer.getSSLPort());
        }
    }
```java

Of course a service can always just use javax.inject.Named to inject a specific WebServerBean if
wants to hard-code the name in the code.

### An HK2 Service Per web-server XML stanza

In the next example the system will generate a new WebServer for every web-server XML stanza
in the file, and will inject that WebServer with the values found from that stanza.
 
[xmlservice]: apidocs/org/glassfish/hk2/xml/api/XmlService.html
[xmlserviceutilities]: apidocs/org/glassfish/hk2/xml/api/XmlServiceUtilities.html
[hk2runner]: apidocs/org/jvnet/hk2/testing/junit/HK2Runner.html
[xmlroothandle]: apidocs/org/glassfish/hk2/xml/api/XmlRootHandle.html
[configuredby]:apidocs/org/glassfish/hk2/configuration/api/ConfiguredBy.html
[contract]: apidocs/org/jvnet/hk2/annotations/Contract.html
[xmlidentifier]: apidocs/org/glassfish/hk2/xml/api/annotations/XmlIdentifier.html
[hk2xmlpregenerate]: apidocs/org/glassfish/hk2/xml/api/annotations/Hk2XmlPreGenerate.html
