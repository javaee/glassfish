[//]: # " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. "
[//]: # "  "
[//]: # " Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " The contents of this file are subject to the terms of either the GNU "
[//]: # " General Public License Version 2 only (''GPL'') or the Common Development "
[//]: # " and Distribution License(''CDDL'') (collectively, the ''License'').  You "
[//]: # " may not use this file except in compliance with the License.  You can "
[//]: # " obtain a copy of the License at "
[//]: # " https://oss.oracle.com/licenses/CDDL+GPL-1.1 "
[//]: # " or LICENSE.txt.  See the License for the specific "
[//]: # " language governing permissions and limitations under the License. "
[//]: # "  "
[//]: # " When distributing the software, include this License Header Notice in each "
[//]: # " file and include the License file at LICENSE.txt. "
[//]: # "  "
[//]: # " GPL Classpath Exception: "
[//]: # " Oracle designates this particular file as subject to the ''Classpath'' "
[//]: # " exception as provided by Oracle in the GPL Version 2 section of the License "
[//]: # " file that accompanied this code. "
[//]: # "  "
[//]: # " Modifications: "
[//]: # " If applicable, add the following below the License Header, with the fields "
[//]: # " enclosed by brackets [] replaced by your own identifying information: "
[//]: # " ''Portions Copyright [year] [name of copyright owner]'' "
[//]: # "  "
[//]: # " Contributor(s): "
[//]: # " If you wish your version of this file to be governed by only the CDDL or "
[//]: # " only the GPL Version 2, indicate your decision by adding ''[Contributor] "
[//]: # " elects to include this software in this distribution under the [CDDL or GPL "
[//]: # " Version 2] license.''  If you don't indicate a single choice of license, a "
[//]: # " recipient has the option to distribute your version of this file under "
[//]: # " either the CDDL, the GPL Version 2 or to extend the choice of license to "
[//]: # " its licensees as provided above.  However, if you add GPL Version 2 code "
[//]: # " and therefore, elected the GPL Version 2 license, then the option applies "
[//]: # " only if the new code is made subject to such option by the copyright "
[//]: # " holder. "

## Configuration Example

This page shows an example of how to use the HK2 configuration service in order to inject
values into HK2 services from a properties file.  It is designed to show how end users
would interact with the system, and as such does not demonstrate how the internals of the
configuration system interact with each other.

The example itself is of a fake web server, which server has three ports it can open: an
admin port, a SSL port and a normal HTTP port.  The SSL port and HTTP port can be dynamically
changed while the web server is up and running, but the admin port cannot.  The web server
can also be associated with one or more SSL certificates and private keys, which are themselves
configured.  In this specific example the web server is associated with two SSL certificates.

### The Web Server

Lets first take a look at the Web Server.  The Web Server service implements this HK2 [Contract][contract]:

```java
@Contract 
public interface WebServer {
    /**
     * Gets the name of this web server
     */
    public String getName();
    
    /**
     * Opens the admin port, and returns the number
     * of the port open
     */
    public int openAdminPort();
    
    /**
     * Opens the SSL port, and returns the number
     * of the port open
     */
    public int openSSLPort();
    
    /**
     * Opens the non-SSL port, and returns the number
     * of the port open
     */
    public int openPort();
    
    /**
     * Gets the current admin port, or -1
     * if the port is not open
     */
    public int getAdminPort();
    
    /**
     * Gets the current SSL port, or -1
     * if the port is not open
     */
    public int getSSLPort();
    
    /**
     * Gets the current HTTP port, or -1
     * if the port is not open
     */
    public int getPort();
    
    /**
     * Gets the list of certificates that are
     * used by this web server
     * 
     * @return A non-null but possibly empty set
     * of Files pointing to the public certificates
     * of the web server
     */
    public List<File> getCertificates(); 
}
```java

Several fields of the WebServer are meant to be configured from an external source.  In this example that source will be
a properties file, which we will look at in detail later.  All the configuration properties of the WebServer are encapsulated
in a WebServerBean, which you can see here:

```java
/**
 * This bean describes a WebServer
 */
public class WebServerBean {
    private String name;
    private String address;
    private int adminPort;
    private int sslPort;
    private int port;
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }
    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }
    /**
     * @return the adminPort
     */
    public int getAdminPort() {
        return adminPort;
    }
    /**
     * @param adminPort the adminPort to set
     */
    public void setAdminPort(int adminPort) {
        this.adminPort = adminPort;
    }
    /**
     * @return the sslPort
     */
    public int getSSLPort() {
        return sslPort;
    }
    /**
     * @param sslPort the sslPort to set
     */
    public void setSSLPort(int sslPort) {
        this.sslPort = sslPort;
    }
    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }
    /**
     * @param sshPort the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }
}
```java

The implementation of the WebServer contract is the WebServerImpl.  The WebServerImpl is in the @ConfiguredBy scope, and injects
several fields from the WebServerBean.  The interesting parts of this code can be seen below.  You can find the full source for
this code [here][webserverimpl].

```java
@Service @ConfiguredBy("WebServerBean")
public class WebServerImpl implements WebServer {
    @Configured
    private String name;
    
    @Configured
    private int adminPort;
    private int openAdminPort = -1;
    
    @Configured(dynamicity=Configured.Dynamicity.FULLY_DYNAMIC)
    private String address;
    
    private int sslPort;
    private int openSSLPort = -1;
    private int port;
    private int openPort = -1;
    
    private boolean opened = false;
    
    /**
     * These are configured services that can be used to get other
     * variable information about the WebServer.  In this case
     * it is getting information about the certificates that
     * this server can use for SSL
     */
    @Inject
    private IterableProvider<SSLCertificateService> certificates;
    
    /**
     * This method is called to set the port and sshPort.  It is guaranteed that
     * the server will not have these ports open at the time this method is called.
     * That is guaranteed since the ports are not open until the postConstruct method
     * is called on boot, and it is only called between the startDynamicConfiguration
     * and finishDynamicConfiguration methods when a dynamic configuration change is
     * made
     * 
     * @param sshPort The sshPort to use
     * @param port The port to use
     */
    @SuppressWarnings("unused")
    private void setUserPorts(
            @Configured(value="SSLPort", dynamicity=Configured.Dynamicity.FULLY_DYNAMIC) int sslPort,
            @Configured(value="port", dynamicity=Configured.Dynamicity.FULLY_DYNAMIC) int port) {
        this.sslPort = sslPort;
        this.port = port;
        
        if (opened) {
            openSSLPort = sslPort;
            openPort = port;
        }
    }
    
    @PostConstruct
    private void postConstruct() {
        opened = true;
    }
    
    @PreDestroy
    private void preDestroy() {
        openPort = -1;
        openSSLPort = -1;
        openAdminPort = -1;
    }
    
    // ... (uninteresting code)
    
    /* (non-Javadoc)
     * @see org.glassfish.examples.configuration.webserver.WebServer#getCertificates()
     */
    @Override
    public List<File> getCertificates() {
        LinkedList<File> retVal = new LinkedList<File>();
        
        for (SSLCertificateService certService : certificates) {
            retVal.add(certService.getCertificate());
        }
        
        return retVal;
    }
}
```java

The above implementation of the WebServer contract is an HK2 [Service][service], and it is in the [ConfiguredBy][configuredby] scope.  The
[ConfiguredBy][configuredby] annotation requires the name of the type upon which instances of the service are based, in this case
&quot;WebServerBean&quot;.  This implies that for every instance of the type &quot;WebServerBean&quot; found in the HK2 management
[Hub][hub] a new instance of the WebServerImpl class will be created.  Lets now look at the interesting fields and methods of the
WebServerImpl.

The first configured parameter is a field called &quot;name&quot;.  In theory there could be several web servers configured, each with a different name.  In
this example we don't do that, but the ability to do so is there.  The admin port is also configured, like this:

```java
    @Configured
    private int adminPort;
```java

By putting [@Configured][configured] on the field, that tells the HK2 configuration system to look for a getter named getAdminPort 
on the bean, and to get the value for the adminPort from that property.  By the time the postConstruct method of the WebServerImpl
has been called it is guaranteed that this field will have been filled in.  Notice that the [@Configured][configured] annotation did
not take a value here, instead taking the name of the property from the name of the field.  In contrast consider this method, which
sets the ssl and http ports of the server:

```java
    private void setUserPorts(
            @Configured(value="SSLPort", dynamicity=Configured.Dynamicity.FULLY_DYNAMIC) int sslPort,
            @Configured(value="port", dynamicity=Configured.Dynamicity.FULLY_DYNAMIC) int port) {
        this.sslPort = sslPort;
        this.port = port;
        
        if (opened) {
            openSSLPort = sslPort;
            openPort = port;
        }
    }
```java

There are several interesting things to notice about this method.  One thing is that we had to put @Configured on all the parameters
that were to come from beans.  If there had been other parameters on this method they would have been filled in with hk2 services
like a normal hk2 initializer method. Another interesting thing is to notice that we set the dynamicity of the configured fields
to FULLY_DYNAMIC.  This means that if either of these two fields change, this method will get called with the new values.  The
dynamicity value of all @Configured annotated parameters on a method must be the same.  One other subtle thing to notice is that
unlike a normal HK2 intialization method, this method can now get called AFTER the postConstruct method has been called.  In fact,
this method works slightly different depending on whether it has been called before or after the postConstruct method.

Any HK2 service can also injected into WebServerImpl.  This includes other configured services.  For example, in the WebServerImpl
there is this injection point:

```java
    @Inject
    private IterableProvider<SSLCertificateService> certificates;
```java

Let us look in detail at the SSLCertificateService to more fully understand the above usage.

### The SSLCertificateService

SSLCertificateService is an HK2 service that deals with SSL certificates.  This is the full implementation of the service: 

```java
@Service @ConfiguredBy("SSLCertificateBean")
public class SSLCertificateService {
    @Configured("$bean")
    private SSLCertificateBean certificateBean;
    
    /**
     * Returns the location of the public certificate
     * 
     * @return The public certificate for this SSL service
     */
    public File getCertificate() {
        return certificateBean.getCertificateLocation();
    }

}
```java

Like the WebServerImpl this service is in the [@ConfiguredBy][configuredby] scope.  Instances of this service will be created for each instance
of type &quot;SSLCertificateBean&quot; found in the [Hub][hub].  Look at the [@Configured][configured] injection point:

```java
    @Configured("$bean")
    private SSLCertificateBean certificateBean;
```java

In this case the [@Configured][configured] annotation has a value of &quot;$bean&quot;.  When a [@Configured][configured] annotation has a
value of &quot;$bean&quot; it means to inject the whole bean rather than a value from the bean.  The code for the SSLCertificateBean will
be found below in the discussion about how the property file is translated into beans.  Here is the injection point in the WebServerImpl:

```java
    @Inject
    private IterableProvider<SSLCertificateService> certificates;
```java

This injection point will be able to get all of the configured SSKCertificateService instances created.  So if there are two SSLCertificateBeans
in the [Hub][hub] then this iterator will return two SSLCertificateServices.

### Configuring the Services

This example has a specific configuration that it will read and then modify to ensure that the system is working properly.  The initial
property file for the example can be found here:

```java
WebServerBean.Acme.name=Acme
WebServerBean.Acme.address=localhost
WebServerBean.Acme.adminPort=7070
WebServerBean.Acme.sslPort=81
WebServerBean.Acme.port=80

SSLCertificateBean.Corporate.certificateLocation=Corporatex509.cert
SSLCertificateBean.Corporate.secretKeyLocation=Corporate.pem

SSLCertificateBean.HR.certificateLocation=HRx509.cert
SSLCertificateBean.HR.secretKeyLocation=HR.pem
```java

The way the HK2 Properties interpreter works is that property keys have three parts, which is the type name followed by the instance name
followed by the parameter name.  So the above properties file means to add an instance of the WebServerBean named &quot;Acme&quot; to the
[Hub][hub] with the properties given.  Also add two instances of the SSLCertificateBean, one named &quot;Corporate&quot; and one
named &quot;HR&quot; also with the given properties.

Properties are generally strings, but the HK2 Properties interpreter is able to translate those strings to the types found in the associated
JavaBeans.  This includes being able to translate the String if the type associated with that parameter name has a public constructor that
takes a string.  For example, consider the SSLCertificateBean:

```java
public class SSLCertificateBean {
    private File certificateLocation;
    private File secretKeyLocation;
    /**
     * @return the certificateLocation
     */
    public File getCertificateLocation() {
        return certificateLocation;
    }
    /**
     * @param certificateLocation the certificateLocation to set
     */
    public void setCertificateLocation(File certificateLocation) {
        this.certificateLocation = certificateLocation;
    }
    /**
     * @return the secretKeyLocation
     */
    public File getSecretKeyLocation() {
        return secretKeyLocation;
    }
    /**
     * @param secretKeyLocation the secretKeyLocation to set
     */
    public void setSecretKeyLocation(File secretKeyLocation) {
        this.secretKeyLocation = secretKeyLocation;
    }
}
```java

The SSLCertificateBean has File as the type for both the &quot;secretKeyLocation&quot; and &quot;CertificateLocation&quot; parameters.  HK2
knows how to translate the strings in the properties file because File has a public constructor that takes a string, which is what
it uses to fill in the fields of this bean.

In order to read the properties file into the [Hub][hub] this example uses the [PropertyFileService][propertyfileservice].
The [PropertyFileService][propertyfileservice] is used to create [PropertyFileHandle][propertyfilehandle] instances.  This
is how the test code reads the above property file into the [Hub][hub]:

```java
        Properties configuration = new Properties();
        
        URL configURL = getClass().getClassLoader().getResource("config.prop");
        InputStream configStream = configURL.openConnection().getInputStream();
        try {
            // Read the property file
            configuration.load(configStream);
        }
        finally {
            configStream.close();
        }
        
        // In order to read the Properties object into HK2 we need to get a PropertyFileHandle
        PropertyFileService propertyFileService = locator.getService(PropertyFileService.class);
        PropertyFileHandle propertyFileHandle = propertyFileService.createPropertyHandleOfAnyType();
        
        // Now read the configuration into hk2
        propertyFileHandle.readProperties(configuration);
```java

The reason for using a [PropertyFileHandle][propertyfilehandle] is because the handle keeps the state
of the property file the last time it was read.  In this way if the property file is modified
and read in again the propertyFileHandle would know which values had changed, which instances
were added and which instances were removed (which is the difficult part of the whole
thing!).

### Dynamic changes

In this example we simply modify the Properties object itself rather than reading a second file for
simplicity.  This is the test code that changes the properties and then tells hk2 about the
changes:

```java
        // Change the ports so that they look like this in the properties file:
        // adminPort = 8082
        // sslPort = 8081
        // port = 8080
        configuration.put("WebServerBean.Acme.adminPort", "8082");
        configuration.put("WebServerBean.Acme.sslPort", "8081");
        configuration.put("WebServerBean.Acme.port", "8080");
        
        // Tell hk2 about the change
        propertyFileHandle.readProperties(configuration);
```java

The propertyFileHandle will notice the changes and inform the [Hub][hub], which will then notify the backing
WebServerImpl about the modified dynamic properties.  Even though the adminPort has changed the WebServerImpl
will NOT be notified about that parameter changing because that field is NOT marked as being dynamic.

### Initialization and setup

In order to use the various parts of the HK2 configuration subsystem they must be initialized.  This is
done via static methods on helper classes.  This is the test initialization code (junit @Before block):

```java
        // Enable HK2 service integration
        ConfigurationUtilities.enableConfigurationSystem(locator);
        
        // Enable Properties service, to get service properties from a Properties object
        PropertyFileUtilities.enablePropertyFileService(locator);
```java

The [PropertyFileService][propertyfileservice] also needs the mapping from type name to Java Bean class.  In order
to do this we must configure the [PropertyFileService][propertyfileservice] with a [PropertyFileBean][propertyfilebean].
This is the code that maps the type named &quot;SSLCertificateBean&quot; to the SSLCertificateBean class and the
type named &quot;WebServerBean&quot; to the WebServerBean class:

```java
        // The propertyFileBean contains the mapping from type names to Java Beans
        PropertyFileBean propertyFileBean = new PropertyFileBean();
        propertyFileBean.addTypeMapping("WebServerBean", WebServerBean.class);
        propertyFileBean.addTypeMapping("SSLCertificateBean", SSLCertificateBean.class);
        
        // Add in the mapping from type name to bean classes
        PropertyFileService propertyFileService = locator.getService(PropertyFileService.class);
        propertyFileService.addPropertyFileBean(propertyFileBean);
```java

The last thing to do in order to setup the environment is to tell HK2 about the backing HK2 services.  This is
done with a simple call to addClasses:

```java
        // Add the test services themselves
        ServiceLocatorUtilities.addClasses(locator,
                SSLCertificateService.class,
                WebServerImpl.class);
```java

The @Before block of the test code has now setup HK2 for the test run.

### Running the test

This is the full test that shows that the WebServerImpl is properly running and gets the proper values from the
properties file:

```java
    /**
     * This test demonstrates adding and the modifying the http and
     * ssl ports of the web server
     */
    @Test // @org.junit.Ignore
    public void testDemonstrateWebServerConfiguration() throws IOException {
        // Before we add a configuration there is no web server
        WebServer webServer = locator.getService(WebServer.class);
        Assert.assertNull(webServer);
        
        Properties configuration = new Properties();
        
        // Gets the URL of the configuration property file.  This
        // file contains one web server and two SSL certificate
        // configuration objects
        URL configURL = getClass().getClassLoader().getResource("config.prop");
        InputStream configStream = configURL.openConnection().getInputStream();
        try {
            // Read the property file
            configuration.load(configStream);
        }
        finally {
            configStream.close();
        }
        
        // In order to read the Properties object into HK2 we need to get a PropertyFileHandle
        PropertyFileService propertyFileService = locator.getService(PropertyFileService.class);
        PropertyFileHandle propertyFileHandle = propertyFileService.createPropertyHandleOfAnyType();
        
        // Now read the configuration into hk2
        propertyFileHandle.readProperties(configuration);
        
        // We should now have a web server!
        webServer = locator.getService(WebServer.class);
        Assert.assertNotNull(webServer);
        
        // Lets open all the ports, and check that they have the expected values
        // In this case the ports are:
        // adminPort = 7070
        // sslPort = 81
        // port = 80
        Assert.assertEquals((int) 7070, webServer.openAdminPort());
        Assert.assertEquals((int) 81, webServer.openSSLPort());
        Assert.assertEquals((int) 80, webServer.openPort());
        
        // Now lets check that we have two SSL certificates
        List<File> certs = webServer.getCertificates();
        
        // The two certificates should be Corporatex509.cert and HRx509.cert
        Assert.assertEquals(2, certs.size());
        
        HashSet<String> foundCerts = new HashSet<String>();
        for (File cert : certs) {
            foundCerts.add(cert.getName());
        }
        
        Assert.assertTrue(foundCerts.contains("Corporatex509.cert"));
        Assert.assertTrue(foundCerts.contains("HRx509.cert"));
        
        // OK, we have verified that all of the parameters of the
        // webserver are as expected.  We are now going to dynamically
        // change all the ports.  In the webserver however only
        // the ssl and http ports are dynamic, so after the change
        // only the ssl and http ports should have their new values,
        // while the admin port should remain with the old value
        
        // Change the ports so that they look like this in the properties file:
        // adminPort = 8082
        // sslPort = 8081
        // port = 8080
        configuration.put("WebServerBean.Acme.adminPort", "8082");
        configuration.put("WebServerBean.Acme.sslPort", "8081");
        configuration.put("WebServerBean.Acme.port", "8080");
        
        // Tell hk2 about the change
        propertyFileHandle.readProperties(configuration);
        
        // Now lets check the web server, make sure the ports have been modified
        
        // The adminPort is NOT dynamic in the back end service, so it did not change
        Assert.assertEquals(7070, webServer.getAdminPort());
        
        // But the SSL and HTTP ports have changed dynamically
        Assert.assertEquals(8081, webServer.getSSLPort());
        Assert.assertEquals(8080, webServer.openPort());
    }
```java

### Final thoughts

This example is a simple demonstration of the HK2 configuration subsystem from an end-user perspective.  The end-users write
HK2 services such as WebServerImpl and SSLCertificateService.  They annotate those services with [ConfiguredBy][configuredby]
and [Configured][configured].  They can either be injected with parameters from beans or with the entire bean.

The mechanism demonstrated in this example used a properties file that was read in using the [PropertyFileService][propertyfileservice].
Any other mechanism can be used to add java beans to the [Hub][hub], making the persistence of the configuration
data a pluggable mechanism.  Dynamic changes to fields are also supported.  More information about the HK2 configuration
service can be found [here][configurationdoc].

[webserverimpl]: https://github.com/hk2-project/hk2/blob/master/examples/configuration/webserver/src/main/java/org/glassfish/examples/configuration/webserver/internal/WebServerImpl.java
[service]: apidocs/org/jvnet/hk2/annotations/ServiceLocator.html
[contract]: apidocs/org/jvnet/hk2/annotations/Contract.html
[configuredby]: apidocs/org/glassfish/hk2/configuration/api/ConfiguredBy.html
[configured]: apidocs/org/glassfish/hk2/configuration/api/Configured.html
[hub]: apidocs/org/glassfish/hk2/configuration/hub/api/Hub.html
[propertyfileservice]: apidocs/org/glassfish/hk2/configuration/persistence/properties/PropertyFileService.html
[propertyfilehandle]: apidocs/org/glassfish/hk2/configuration/persistence/properties/PropertyFileHandle.html
[propertyfilebean]: apidocs/org/glassfish/hk2/configuration/persistence/properties/PropertyFileBean.html
[configurationdoc]: configuration.html
