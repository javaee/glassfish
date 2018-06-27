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

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.ManagedConnectionMetaData;

public class MetaDataImpl
  implements ManagedConnectionMetaData
{
  private static final String PRODUCT_NAME = "JAXR Resource Adapter";
  private static final String PRODUCT_VERSION = "1.0";
  private static final int MAX_CONNECTIONS = 150;
  private JaxrManagedConnection mc;
  
  public MetaDataImpl(JaxrManagedConnection paramJaxrManagedConnection)
  {
    this.mc = paramJaxrManagedConnection;
  }
  
  public String getEISProductName()
    throws ResourceException
  {
    try
    {
      return "JAXR Resource Adapter";
    }
    catch (Exception localException)
    {
      EISSystemException localEISSystemException = new EISSystemException(localException.getMessage());
      localEISSystemException.setLinkedException(localException);
      throw localEISSystemException;
    }
  }
  
  public String getEISProductVersion()
    throws ResourceException
  {
    try
    {
      return "1.0";
    }
    catch (Exception localException)
    {
      EISSystemException localEISSystemException = new EISSystemException(localException.getMessage());
      localEISSystemException.setLinkedException(localException);
      throw localEISSystemException;
    }
  }
  
  public int getMaxConnections()
    throws ResourceException
  {
    try
    {
      return 150;
    }
    catch (Exception localException)
    {
      EISSystemException localEISSystemException = new EISSystemException(localException.getMessage());
      localEISSystemException.setLinkedException(localException);
      throw localEISSystemException;
    }
  }
  
  public String getUserName()
    throws ResourceException
  {
    if (this.mc.isDestroyed()) {
      throw new IllegalStateException("ManagedConnection has been destroyed");
    }
    throw new NotSupportedException("Credentials not supported in JAXR Connector");
  }
}
