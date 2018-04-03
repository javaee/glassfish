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

import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.IllegalStateException;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

public class JaxrConnection
  implements Connection
{
  private JaxrManagedConnection mc;

  @LogMessagesResourceBundle
  private static final Logger log = Logger.getLogger("com.sun.connector.jaxr");
  
  public JaxrConnection(JaxrManagedConnection paramJaxrManagedConnection)
  {
    this.mc = paramJaxrManagedConnection;
  }
  
  public RegistryService getRegistryService()
    throws JAXRException
  {
    log.fine("Getting RegistryService");
    return getJaxrConnection().getRegistryService();
  }
  
  public void close()
    throws JAXRException
  {
    log.fine("JAXRConnection close - delegating to managedConnection");
    if (this.mc == null) {
      return;
    }
    log.fine("ManagedConnection removing JAXR Connection");
    this.mc.removeJaxrConnection(this);
    log.fine("ManagedConnection sending connection closed Event");
    log.fine("ManagedConnection - connection closed Event Sent");
    this.mc = null;
  }
  
  public boolean isClosed()
    throws JAXRException
  {
    return this.mc == null ? true : getJaxrConnection().isClosed();
  }
  
  public boolean isSynchronous()
    throws JAXRException
  {
    return getJaxrConnection().isSynchronous();
  }
  
  public void setSynchronous(boolean paramBoolean)
    throws JAXRException
  {
    getJaxrConnection().setSynchronous(paramBoolean);
  }
  
  public void setCredentials(Set paramSet)
    throws JAXRException
  {
    getJaxrConnection().setCredentials(paramSet);
  }
  
  public Set getCredentials()
    throws JAXRException
  {
    return getJaxrConnection().getCredentials();
  }
  
  void associateConnection(JaxrManagedConnection paramJaxrManagedConnection)
    throws ResourceException
  {
    try
    {
      checkIfValid();
    }
    catch (JAXRException localJAXRException)
    {
      throw new IllegalStateException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Unable_to_associate_JAXR_Connection,_Connection_is_invalid"));
    }
    this.mc.removeJaxrConnection(this);
    paramJaxrManagedConnection.addJaxrConnection(this);
    this.mc = paramJaxrManagedConnection;
  }
  
  void checkIfValid()
    throws JAXRException
  {
    if (this.mc == null) {
      throw new JAXRException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Connection_is_invalid"));
    }
  }
  
  Connection getJaxrConnection()
    throws JAXRException
  {
    checkIfValid();
    try
    {
      return this.mc.getJaxrConnection();
    }
    catch (ResourceException localResourceException)
    {
      throw new JAXRException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Unable_to_obtain_JAXR_Connection_") + localResourceException.getMessage());
    }
  }
  
  void invalidate()
  {
    this.mc = null;
  }
}

