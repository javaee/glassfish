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

import java.io.Serializable;
import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.FederatedConnection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

public class JaxrConnectionFactory
  extends ConnectionFactory
  implements Serializable, Referenceable
{
  private ManagedConnectionFactory mcf;
  private ConnectionManager cm;
  private Reference reference;
  private Properties properties;
  @LogMessagesResourceBundle
  private static final Logger log = Logger.getLogger("com.sun.connector.jaxr");
  
  public JaxrConnectionFactory(ManagedConnectionFactory paramManagedConnectionFactory, ConnectionManager paramConnectionManager)
  {
    this.mcf = paramManagedConnectionFactory;
    log.fine("JAXRConnectionFactory constructor - ManagedConnectionFactory and ConnectionManager are parameters");
    this.cm = new JaxrConnectionManager();
  }
  
  public Connection getConnection()
    throws JAXRException
  {
    try
    {
      JaxrConnectionRequestInfo localJaxrConnectionRequestInfo = null;
      if (this.properties != null) {
        localJaxrConnectionRequestInfo = new JaxrConnectionRequestInfo(this.properties);
      }
      log.fine("JAXRConnectionFactory getConnection - ConnectionManager calling allocateConnection");
      return (Connection)this.cm.allocateConnection(this.mcf, localJaxrConnectionRequestInfo);
    }
    catch (ResourceException localResourceException)
    {
      throw new JAXRException(localResourceException.getMessage());
    }
  }
  
  public Connection getConnection(String paramString1, String paramString2)
    throws JAXRException
  {
    throw new JAXRException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Getting_a_connection_with_username,_password_parameters_is_not_supported"));
  }
  
  Connection getConnection(Properties paramProperties)
    throws JAXRException
  {
    try
    {
      JaxrConnectionRequestInfo localJaxrConnectionRequestInfo = new JaxrConnectionRequestInfo(paramProperties);
      return (Connection)this.cm.allocateConnection(this.mcf, localJaxrConnectionRequestInfo);
    }
    catch (ResourceException localResourceException)
    {
      throw new JAXRException(localResourceException.getMessage());
    }
  }
  
  public void setProperties(Properties paramProperties)
    throws JAXRException
  {
    this.properties = paramProperties;
  }
  
  public Properties getProperties()
    throws JAXRException
  {
    return this.properties;
  }
  
  public Connection createConnection()
    throws JAXRException
  {
    log.fine("JAXRConnectionFactory createConnection calling getConnection -");
    return getConnection();
  }
  
  public FederatedConnection createFederatedConnection(Collection paramCollection)
    throws JAXRException
  {
    throw new UnsupportedCapabilityException();
  }
  
  public void setReference(Reference paramReference)
  {
    this.reference = paramReference;
  }
  
  public Reference getReference()
  {
    return this.reference;
  }
}
