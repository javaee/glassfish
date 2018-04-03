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
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

public class JaxrManagedConnection
  implements ManagedConnection
{
  private XAConnection xacon;
  private Connection con;
  private JaxrConnectionEventListener jaxrListener;
  private ManagedConnectionFactory mcf;
  private PrintWriter logWriter;
  private boolean destroyed;
  private Properties properties;
  private Set connectionSet;
  @LogMessagesResourceBundle
  Logger log = Logger.getLogger("com.sun.connector.jaxr");
  
  JaxrManagedConnection(ManagedConnectionFactory paramManagedConnectionFactory, XAConnection paramXAConnection, Connection paramConnection)
  {
    this.mcf = paramManagedConnectionFactory;
    this.xacon = paramXAConnection;
    this.log.fine("JAXRManagedConnection has actual jaxr connection impl");
    this.con = paramConnection;
    this.connectionSet = new HashSet();
    this.log.fine("Instantiating JAXRConnectionEventListener in JAXRManagedConnection constructor");
    this.jaxrListener = new JaxrConnectionEventListener(this);
    this.log.fine("End of JAXRManagedConnection constructor");
  }
  
  JaxrManagedConnection(ManagedConnectionFactory paramManagedConnectionFactory, Properties paramProperties, XAConnection paramXAConnection, Connection paramConnection)
  {
    this.mcf = paramManagedConnectionFactory;
    this.properties = paramProperties;
    this.xacon = paramXAConnection;
    this.log.fine("JAXRManagedConnection has actual jaxr connection impl");
    this.con = paramConnection;
    this.connectionSet = new HashSet();
    this.log.fine("Instantiating JAXRConnectionEventListener in JAXRManagedConnection constructor");
    this.jaxrListener = new JaxrConnectionEventListener(this);
    this.log.fine("End of JAXRManagedConnection constructor");
  }
  
  private void throwResourceException(JAXRException paramJAXRException)
    throws ResourceException
  {
    this.log.fine("throwing REx in in JAXRManagedConnection constructor");
    paramJAXRException.printStackTrace();
    ResourceException localResourceException = new ResourceException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("JAXRException:_") + paramJAXRException.getMessage());
    localResourceException.setLinkedException(paramJAXRException);
    throw localResourceException;
  }
  
  private void throwResourceException(SQLException paramSQLException)
    throws ResourceException
  {
    ResourceException localResourceException = new ResourceException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("SQLException:_") + paramSQLException.getMessage());
    localResourceException.setLinkedException(paramSQLException);
    throw localResourceException;
  }
  
  public Object getConnection(Subject paramSubject, ConnectionRequestInfo paramConnectionRequestInfo)
    throws ResourceException
  {
    checkIfDestroyed();
    this.log.fine("JAXRManagedConnection getting connection");
    JaxrConnection localJaxrConnection = new JaxrConnection(this);
    this.log.fine("JAXRManagedConnectiond created JAXRConnection");
    addJaxrConnection(localJaxrConnection);
    this.log.fine("JAXRMananagedConnection adding JAXRConnection to connection set");
    return localJaxrConnection;
  }
  
  public void destroy()
    throws ResourceException
  {
    try
    {
      if (this.destroyed) {
        return;
      }
      this.log.fine("JAXRManagedConnection destroying all JAXRConnections");
      this.destroyed = true;
      Iterator localIterator = this.connectionSet.iterator();
      while (localIterator.hasNext())
      {
        JaxrConnection localJaxrConnection = (JaxrConnection)localIterator.next();
        this.log.fine("JAXRManagedConnection destroying JAXRConnection - invalidate");
        localJaxrConnection.invalidate();
      }
      this.log.fine("JAXRManagedConnection destorying JAXRConnection - connection set clear");
      this.connectionSet.clear();
      this.log.fine("JAXRManagedConnection destorying JAXRConnection - closing actual jaxr connectionImpl");
      this.con.close();
      if (this.xacon != null) {
        this.xacon.close();
      }
    }
    catch (JAXRException localJAXRException)
    {
      throwResourceException(localJAXRException);
    }
    catch (SQLException localSQLException)
    {
      throwResourceException(localSQLException);
    }
  }
  
  public void cleanup()
    throws ResourceException
  {
    try
    {
      checkIfDestroyed();
      this.log.fine("JAXRManagedConnection cleanup all JAXRConnections");
      Iterator localIterator = this.connectionSet.iterator();
      while (localIterator.hasNext())
      {
        JaxrConnection localJaxrConnection = (JaxrConnection)localIterator.next();
        this.log.fine("JAXRManagedConnection cleaning JAXRConnection - invalidate");
        localJaxrConnection.invalidate();
      }
      this.log.fine("JAXRManagedConnection cleaning JAXRConnection - connection set clear");
      this.connectionSet.clear();
    }
    catch (Exception localException)
    {
      throw new ResourceException(localException);
    }
  }
  
  public void associateConnection(Object paramObject)
    throws ResourceException
  {
    checkIfDestroyed();
    this.log.fine("JAXRManagedConnection associate JAXRConnection - ");
    if ((paramObject instanceof JaxrConnection))
    {
      JaxrConnection localJaxrConnection = (JaxrConnection)paramObject;
      this.log.fine("JAXRManagedConnection calling JAXRConnection associateConnection - ");
      localJaxrConnection.associateConnection(this);
    }
    else
    {
      throw new IllegalStateException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Invalid_connection_object:_") + paramObject);
    }
  }
  
  public void addConnectionEventListener(ConnectionEventListener paramConnectionEventListener)
  {
    this.log.fine("JAXRManagedConnection calling jaxr connectionEventListener - addConnectorListener");
    this.jaxrListener.addConnectorListener(paramConnectionEventListener);
  }
  
  public void removeConnectionEventListener(ConnectionEventListener paramConnectionEventListener)
  {
    this.jaxrListener.removeConnectorListener(paramConnectionEventListener);
  }
  
  public LocalTransaction getLocalTransaction()
    throws ResourceException
  {
    throw new NotSupportedException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Local_transaction_not_supported"));
  }
  
  public ManagedConnectionMetaData getMetaData()
    throws ResourceException
  {
    checkIfDestroyed();
    return new MetaDataImpl(this);
  }
  
  public void setLogWriter(PrintWriter paramPrintWriter)
    throws ResourceException
  {
    this.logWriter = paramPrintWriter;
  }
  
  public PrintWriter getLogWriter()
    throws ResourceException
  {
    return this.logWriter;
  }
  
  Connection getJaxrConnection()
    throws ResourceException
  {
    checkIfDestroyed();
    this.log.fine("JAXRManagedConnection returning actual jaxr connectionImpl");
    return this.con;
  }
  
  boolean isDestroyed()
  {
    return this.destroyed;
  }
  
  Properties getProperties()
  {
    return this.properties;
  }
  
  void sendEvent(int paramInt, Exception paramException)
  {
    this.log.fine("JAXRManagedConnection calling eventlistener sendEvent");
    this.jaxrListener.sendEvent(paramInt, paramException, null);
  }
  
  void sendEvent(int paramInt, Exception paramException, Object paramObject)
  {
    this.log.fine("JAXRManagedConnection calling eventlistener sendEvent w/ connectionHandle");
    this.jaxrListener.sendEvent(paramInt, paramException, paramObject);
  }
  
  void removeJaxrConnection(JaxrConnection paramJaxrConnection)
  {
    this.log.fine("JAXRManagedConnection removing JAXRConnection from connection set");
    this.connectionSet.remove(paramJaxrConnection);
    paramJaxrConnection.invalidate();
    try
    {
      this.con.close();
      this.log.fine("JAXRManagedConnection Closed actual jaxr ConnectionImpl");
    }
    catch (JAXRException localJAXRException)
    {
      this.log.warning("Error closing jaxr connection");
    }
  }
  
  void addJaxrConnection(JaxrConnection paramJaxrConnection)
  {
    this.log.fine("JAXRManagedConnection adding JAXRConnection to connection set");
    this.connectionSet.add(paramJaxrConnection);
  }
  
  private void checkIfDestroyed()
    throws ResourceException
  {
    if (this.destroyed) {
      throw new IllegalStateException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Managed_connection_is_closed"));
    }
  }
  
  ManagedConnectionFactory getManagedConnectionFactory()
  {
    this.log.fine("JAXRManagedConnection returning JAXRManagedConnectionFactory");
    return this.mcf;
  }
  
  public XAResource getXAResource()
    throws ResourceException
  {
    throw new NotSupportedException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("XAResource_not_supported"));
  }
}
