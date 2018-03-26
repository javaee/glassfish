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
import java.util.Vector;
import java.util.logging.Logger;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

public class JaxrConnectionEventListener
{
  private Vector listeners = new Vector();
  private ManagedConnection mcon;
  @LogMessagesResourceBundle
  private static final Logger log = Logger.getLogger("com.sun.connector.jaxr");
  
  public JaxrConnectionEventListener(ManagedConnection paramManagedConnection)
  {
    this.log.fine("JAXRConnectionEventListener constructor - ManagedConnection as parameter");
    this.mcon = paramManagedConnection;
  }
  
  public void sendEvent(int paramInt, Exception paramException, Object paramObject)
  {
    Vector localVector = (Vector)this.listeners.clone();
    ConnectionEvent localConnectionEvent = null;
    this.log.fine("JAXRConnectionEventListener sendEvent creating connection Event");
    if (paramException == null) {
      localConnectionEvent = new ConnectionEvent(this.mcon, paramInt);
    } else {
      localConnectionEvent = new ConnectionEvent(this.mcon, paramInt, paramException);
    }
    if (paramObject != null)
    {
      this.log.fine("JAXRConnectionEventListener sendEvent setting connection handle on connection Event");
      localConnectionEvent.setConnectionHandle(paramObject);
    }
    int i = localVector.size();
    for (int j = 0; j < i; j++)
    {
      ConnectionEventListener localConnectionEventListener = (ConnectionEventListener)localVector.elementAt(j);
      this.log.fine("JAXRConnectionEventListener sendEvent processing eventType connection Event");
      switch (paramInt)
      {
      case 1: 
        localConnectionEventListener.connectionClosed(localConnectionEvent);
        this.log.fine("JAXRConnectionEventListener sendEvent processing Closed eventType --calling listener.closed");
        break;
      case 2: 
        localConnectionEventListener.localTransactionStarted(localConnectionEvent);
        break;
      case 3: 
        localConnectionEventListener.localTransactionCommitted(localConnectionEvent);
        break;
      case 4: 
        localConnectionEventListener.localTransactionRolledback(localConnectionEvent);
        break;
      case 5: 
        localConnectionEventListener.connectionErrorOccurred(localConnectionEvent);
        break;
      default: 
        throw new IllegalArgumentException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Illegal_eventType:_") + paramInt);
      }
    }
  }
  
  public void addConnectorListener(ConnectionEventListener paramConnectionEventListener)
  {
    this.listeners.addElement(paramConnectionEventListener);
  }
  
  public void removeConnectorListener(ConnectionEventListener paramConnectionEventListener)
  {
    this.listeners.removeElement(paramConnectionEventListener);
  }
  
  public void connectionClosed(ConnectionEvent paramConnectionEvent)
  {
    this.log.fine("JAXRConnectionEventListener connectionClosed - doing nothing");
  }
  
  public void connectionErrorOccurred(ConnectionEvent paramConnectionEvent)
  {
    sendEvent(5, null, null);
  }
}

