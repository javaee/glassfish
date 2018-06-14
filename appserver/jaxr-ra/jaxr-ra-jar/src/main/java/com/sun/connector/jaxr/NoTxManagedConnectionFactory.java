/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.connector.jaxr;

import java.io.PrintWriter;
import java.io.Serializable;
import java.net.PasswordAuthentication;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.security.auth.Subject;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import java.util.logging.Level;

public class NoTxManagedConnectionFactory
  implements ManagedConnectionFactory, Serializable
{
  static final String QUERY_URL_PROP = "javax.xml.registry.queryManagerURL";
  static final String LIFE_CYCLE_URL_PROP = "javax.xml.registry.lifeCycleManagerURL";
  static final String HTTP_PROXY_HOST = "com.sun.xml.registry.http.proxyHost";
  static final String HTTP_PROXY_PORT = "com.sun.xml.registry.http.proxyPort";
  static final String HTTPS_PROXY_HOST = "com.sun.xml.registry.https.proxyHost";
  static final String HTTPS_PROXY_PORT = "com.sun.xml.registry.https.proxyPort";
  static final String PROXY_USER_NAME = "com.sun.xml.registry.http.proxyUserName";
  static final String PROXY_PASSWORD = "com.sun.xml.registry.http.proxyPassword";
  static final String USER_DEF_TAXONOMIES = "com.sun.xml.registry.userTaxonomyFilenames";
  static final String USERNAME = "com.sun.xml.registry.userName";
  static final String PASSWORD = "com.sun.xml.registry.userPassword";
  static final String APPSERV_PROPERTY = "com.sun.appserv.uddi";
  transient private ResourceAdapter ra;
  private String queryManagerURL;
  private String lifeCycleManagerURL;
  private String httpProxyHost;
  private String httpProxyPort;
  private String httpsProxyHost;
  private String httpsProxyPort;
  private String proxyUsername;
  private String proxyUserPassword;
  private Properties properties;
  transient private PrintWriter out;
  private String userName;
  private String userPassword;
  private Boolean appserverUDDI = Boolean.valueOf(true);
  @LogMessagesResourceBundle
  private static final Logger log = Logger.getLogger("com.sun.connector.jaxr");
  
  public Object createConnectionFactory(ConnectionManager paramConnectionManager)
    throws ResourceException
  { 
    if (log.isLoggable(Level.FINE)) {
    	log.fine("NoTxManagedConnectionFactory returing JAXRConnectionFactory with ConnectionManager");
    	log.fine("NoTxManagedConnectionFactory - ConnectionManager class is " + paramConnectionManager.getClass().getName());
    }
    return new JaxrConnectionFactory(this, paramConnectionManager);
  }
  
  public Object createConnectionFactory()
    throws ResourceException
  {
    return new JaxrConnectionFactory(this, null);
  }
  
  public ManagedConnection createManagedConnection(Subject paramSubject, ConnectionRequestInfo paramConnectionRequestInfo)
    throws ResourceException
  {
    Properties localProperties = null;
    try
    {
      Connection localConnection = null;
      if (paramConnectionRequestInfo != null) {
        localProperties = ((JaxrConnectionRequestInfo)paramConnectionRequestInfo).getProperties();
      }
      this.log.fine("NoTxManagedConnectionFactory creating JAXRManagedConnection");
       Object localObject = ConnectionFactory.newInstance();
      populateProperties(localProperties);
      if (this.properties != null) {
        ((ConnectionFactory)localObject).setProperties(this.properties);
      }
      this.log.fine("NoTxManagedConnectionFactory creating actual jaxr ConnectionImpl");
      localConnection = ((ConnectionFactory)localObject).createConnection();
      if (this.userName != null)
      {
        PasswordAuthentication localPasswordAuthentication = new PasswordAuthentication(this.userName, this.userPassword.toCharArray());
        HashSet localHashSet = new HashSet();
        localHashSet.add(localPasswordAuthentication);
        localConnection.setCredentials(localHashSet);
      }
      this.log.fine("NoTxManagedConnectionFactory returning new JAXRManagedConnection");
      return new JaxrManagedConnection(this, this.properties, null, localConnection);
    }
    catch (Exception localException)
    {
      Object localObject = new EISSystemException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("JAXRException:") + localException.getMessage());
      ((ResourceException)localObject).setLinkedException(localException);
      throw ((ResourceException)localObject);
    }
  }
  
  public ManagedConnection matchManagedConnections(Set paramSet, Subject paramSubject, ConnectionRequestInfo paramConnectionRequestInfo)
    throws ResourceException
  {
    this.log.fine("NoTxManagedConnectionFactory MatchingManagedConnections");
    this.log.fine("NoTxManagedConnectionFactory mC - let's interate through the connectionSet passed as parameter by Appserer");
    Iterator localIterator = paramSet.iterator();
    int i = 0;
    while (localIterator.hasNext())
    {
      JaxrManagedConnection localJaxrManagedConnection = (JaxrManagedConnection)localIterator.next();
      localJaxrManagedConnection.destroy();
      this.log.fine("NoTxManagedConnectionFactory mC - interate - got JaxrManagedconnection ");
      i++;
      if (log.isLoggable(Level.FINE)) {
          this.log.fine("NoTxManagedConnec - JaxrManagedconnection number " + i);
      }    
    }
    paramSet.clear();
    this.log.fine("NoTxManagedConnec - returning noMantched Connections");
    return null;
  }
  
  public void setLogWriter(PrintWriter paramPrintWriter)
    throws ResourceException
  {
    this.out = paramPrintWriter;
  }
  
  public PrintWriter getLogWriter()
    throws ResourceException
  {
    return this.out;
  }
  
  public boolean equals(Object paramObject)
  {
    if (paramObject == null) {
      return false;
    }
    if ((paramObject instanceof NoTxManagedConnectionFactory))
    {
      Properties localProperties1 = ((NoTxManagedConnectionFactory)paramObject).properties;
      Properties localProperties2 = this.properties;
      return localProperties1 == null ? false : localProperties2 == null ? true : localProperties1.equals(localProperties2);
    }
    return false;
  }
  
  public int hashCode()
  {
    if (this.properties == null) {
      return "".hashCode();
    }
    return this.properties.hashCode();
  }
  
  public ResourceAdapter getResourceAdapter()
  {
    return this.ra;
  }
  
  public void setResourceAdapter(ResourceAdapter paramResourceAdapter)
  {
    this.ra = paramResourceAdapter;
  }
  
  public String getQueryManagerURL()
  {
    return this.queryManagerURL;
  }
  
  public void setQueryManagerURL(String paramString)
  {
    this.queryManagerURL = paramString;
  }
  
  public String getLifeCycleManagerURL()
  {
    return this.lifeCycleManagerURL;
  }
  
  public void setLifeCycleManagerURL(String paramString)
  {
    this.lifeCycleManagerURL = paramString;
  }
  
  public void setUserName(String paramString)
  {
    this.userName = paramString;
  }
  
  public String getUserName()
  {
    return this.userName;
  }
  
  public void setUserPassword(String paramString)
  {
    this.userPassword = paramString;
  }
  
  public String getUserPassword()
  {
    return this.userPassword;
  }
  
  public Boolean getAppserverUDDI()
  {
    return this.appserverUDDI;
  }
  
  public void setAppserverUDDI(Boolean paramBoolean)
  {
    this.appserverUDDI = paramBoolean;
  }
  
  public String getHttpProxyHost()
  {
    return this.httpProxyHost;
  }
  
  public void setHttpProxyHost(String paramString)
  {
    this.httpProxyHost = paramString;
  }
  
  public String getHttpProxyPort()
  {
    return this.httpProxyPort;
  }
  
  public void setHttpProxyPort(String paramString)
  {
    this.httpProxyPort = paramString;
  }
  
  public String getHttpsProxyHost()
  {
    return this.httpsProxyHost;
  }
  
  public void setHttpsProxyHost(String paramString)
  {
    this.httpsProxyHost = paramString;
  }
  
  public String getHttpsProxyPort()
  {
    return this.httpsProxyPort;
  }
  
  public void setHttpsProxyPort(String paramString)
  {
    this.httpsProxyPort = paramString;
  }
  
  public String getProxyUsername()
  {
    return this.proxyUsername;
  }
  
  public void setProxyUsername(String paramString)
  {
    this.proxyUsername = paramString;
  }
  
  public String getProxyUserPassword()
  {
    return this.proxyUserPassword;
  }
  
  public void setProxyUserPassword(String paramString)
  {
    this.proxyUserPassword = paramString;
  }
  
  void populateDefaultProperties(Properties paramProperties)
  {
    if (paramProperties == null) {
      paramProperties = new Properties();
    }
    if (this.queryManagerURL != null) {
      paramProperties.put("javax.xml.registry.queryManagerURL", this.queryManagerURL);
    }
    if (this.lifeCycleManagerURL != null) {
      paramProperties.put("javax.xml.registry.lifeCycleManagerURL", this.lifeCycleManagerURL);
    }
    if (this.httpProxyHost != null) {
      paramProperties.put("com.sun.xml.registry.http.proxyHost", this.httpProxyHost);
    }
    if (this.httpProxyPort != null) {
      paramProperties.put("com.sun.xml.registry.http.proxyPort", this.httpProxyPort);
    }
    if (this.httpsProxyHost != null) {
      paramProperties.put("com.sun.xml.registry.https.proxyHost", this.httpsProxyHost);
    }
    if (this.httpsProxyPort != null) {
      paramProperties.put("com.sun.xml.registry.https.proxyPort", this.httpsProxyPort);
    }
    if (this.proxyUsername != null) {
      paramProperties.put("com.sun.xml.registry.http.proxyUserName", this.proxyUsername);
    }
    if (this.proxyUserPassword != null) {
      paramProperties.put("com.sun.xml.registry.http.proxyPassword", this.proxyUserPassword);
    }
    if (this.userName != null) {
      paramProperties.put("com.sun.xml.registry.userName", this.userName);
    }
    if (this.userPassword != null) {
      paramProperties.put("com.sun.xml.registry.userPassword", this.userPassword);
    }
    if (this.appserverUDDI != null) {
      paramProperties.put("com.sun.appserv.uddi", this.appserverUDDI);
    }
  }
  
  void populateProperties(Properties paramProperties)
  {
    if (this.properties == null) {
      this.properties = new Properties();
    }
    if (paramProperties == null)
    {
      populateDefaultProperties(this.properties);
      return;
    }
    String str1 = paramProperties.getProperty("javax.xml.registry.queryManagerURL");
    if (str1 == null) {
      str1 = this.queryManagerURL;
    }
    if (str1 != null) {
      this.properties.put("javax.xml.registry.queryManagerURL", str1);
    }
    String str2 = paramProperties.getProperty("javax.xml.registry.lifeCycleManagerURL");
    if (str2 == null) {
      str2 = this.lifeCycleManagerURL;
    }
    if (str2 != null) {
      this.properties.put("javax.xml.registry.lifeCycleManagerURL", str2);
    }
    String str3 = paramProperties.getProperty("com.sun.xml.registry.http.proxyHost");
    if (str3 == null) {
      str3 = this.httpProxyHost;
    }
    if (str3 != null) {
      this.properties.put("com.sun.xml.registry.http.proxyHost", str3);
    }
    String str4 = paramProperties.getProperty("com.sun.xml.registry.http.proxyPort");
    if (str4 == null) {
      str4 = this.httpProxyPort;
    }
    if (str4 != null) {
      this.properties.put("com.sun.xml.registry.http.proxyPort", str4);
    }
    String str5 = paramProperties.getProperty("com.sun.xml.registry.https.proxyHost");
    if (str5 == null) {
      str5 = this.httpsProxyHost;
    }
    if (str5 != null) {
      this.properties.put("com.sun.xml.registry.https.proxyHost", str5);
    }
    String str6 = paramProperties.getProperty("com.sun.xml.registry.https.proxyPort");
    if (str6 == null) {
      str6 = this.httpsProxyPort;
    }
    if (str6 != null) {
      this.properties.put("com.sun.xml.registry.https.proxyPort", str6);
    }
    String str7 = paramProperties.getProperty("com.sun.xml.registry.http.proxyUserName");
    if (str7 == null) {
      str7 = this.proxyUsername;
    }
    if (str7 != null) {
      this.properties.put("com.sun.xml.registry.http.proxyUserName", str7);
    }
    String str8 = paramProperties.getProperty("com.sun.xml.registry.http.proxyPassword");
    if (str8 == null) {
      str8 = this.proxyUserPassword;
    }
    if (str8 != null) {
      this.properties.put("com.sun.xml.registry.http.proxyPassword", str8);
    }
    String str9 = paramProperties.getProperty("com.sun.xml.registry.userName");
    if (str9 == null) {
      str9 = this.userName;
    }
    if (str9 != null) {
      this.properties.put("com.sun.xml.registry.userName", str9);
    }
    String str10 = paramProperties.getProperty("com.sun.xml.registry.userPassword");
    if (str10 == null) {
      str10 = this.userPassword;
    }
    if (str10 != null) {
      this.properties.put("com.sun.xml.registry.userPassword", str10);
    }
    Boolean localBoolean = Boolean.valueOf(paramProperties.getProperty("com.sun.appserv.uddi"));
    if (localBoolean == null) {
      localBoolean = this.appserverUDDI;
    }
    this.properties.put("com.sun.appserv.uddi", Boolean.toString(localBoolean.booleanValue()));
  }
}
