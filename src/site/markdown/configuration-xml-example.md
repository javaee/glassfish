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

The application is to be configured with an XML file.  An example XML file could look like this:

```java
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
```java

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
XmlIdentifier and Hk2XmlPreGenerate are HK2 extensions that will be explained later.  The HK2 XML service
will generate a proxy for this interface, copying over all annotations (and in some cases making slight
modifications to them in order to ensure JAXB can parse the XML properly).

XmlIdentifier is an HK2 XML Service annotation that tells the HK2 XML Service that the attribute represented
by the getter or setter can be used as the key for this bean when the bean is a child of another bean.
It is very much like the JAXB annotation XmlID except that in the case of the XmlID the key must be unique
within the scope of the entire document, whereas with XmlIdentifier the scope of uniqueness only needs
to be within the xpath of the stanza.

Hk2XmlPreGenerate is an HK2 XML Service annotation that tells HK2 that the proxy for this bean should be
generated at build time and placed within the JAR being built.  This uses the standard JSR-269 annotation
processor and so should work with most build systems including ant, maven, gradle and others.  The only
requirement is that the HK2 XML Service's jar be in the classpath of the compiler at compile time.  Note
that if the Hk2XmlPreGenerate annotation is NOT put on the interface class then the proxy will be generated
at runtime, so using the Hk2XmlPreGenerate annotation is mainly a matter of runtime performance, since
proxy generation can be a heavy operation.

Lets take a look at the ApplicationBean, which has the WebServer bean as a child:

```java
package org.glassfish.examples.configuration.xml.webserver;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
on the setter method or the getter method.  Most JAXB annotations can also be placed on fields of concreted
classes, but since this is an interface they are always placed on either the getter or setter.  The
methods addWebServers, removeWebServers and lookupWebServers and methods that the HK2 XML Service understands
is part of the web-server family of methods and allows the users of the API to add, remove and
locate WebServers.

The XmlRootElement annotation is a standard JAXB annotation that is placed on classes that can be at
the root of the XML file.

NOTE:  At the current time there is no support for plurals.  This is a bug that should be fixed.

