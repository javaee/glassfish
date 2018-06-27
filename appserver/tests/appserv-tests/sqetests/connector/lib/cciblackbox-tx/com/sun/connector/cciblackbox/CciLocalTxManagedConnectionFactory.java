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

package com.sun.connector.cciblackbox;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

/**
 * 
 * @author Sheetal Vartak
 */
public class CciLocalTxManagedConnectionFactory implements ManagedConnectionFactory, Serializable {

  private String url;

  public CciLocalTxManagedConnectionFactory() {
  }

  public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {

    return new CciConnectionFactory(this, cxManager);
  }

  public Object createConnectionFactory() throws ResourceException {
    return new CciConnectionFactory(this, null);
  }

  public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo info)
      throws ResourceException {

    try {
      Connection con = null;
      String userName = null;

      PasswordCredential pc = Util.getPasswordCredential(this, subject, info);
      if (pc == null) {
        con = DriverManager.getConnection(url);
      } else {
        userName = pc.getUserName();
        con = DriverManager.getConnection(url, userName, new String(pc.getPassword()));
      }
      return new CciManagedConnection(this, pc, null, con, false, true);
    }
    catch (SQLException ex) {
      ResourceException re = new EISSystemException("SQLException: " + ex.getMessage());
      re.setLinkedException(ex);
      throw re;
    }

  }

  public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
      ConnectionRequestInfo info) throws ResourceException {

    PasswordCredential pc = Util.getPasswordCredential(this, subject, info);
    Iterator it = connectionSet.iterator();
    while (it.hasNext()) {
      Object obj = it.next();
      if (obj instanceof CciManagedConnection) {
        CciManagedConnection mc = (CciManagedConnection) obj;
        ManagedConnectionFactory mcf = mc.getManagedConnectionFactory();
        if (Util.isPasswordCredentialEqual(mc.getPasswordCredential(), pc) && mcf.equals(this)) {
          return mc;
        }
      }
    }
    return null;
  }

  public void setLogWriter(PrintWriter out) throws ResourceException {

    DriverManager.setLogWriter(out);
  }

  public PrintWriter getLogWriter() throws ResourceException {
    return DriverManager.getLogWriter();
  }

  public String getConnectionURL() {
    return url;
  }

  public void setConnectionURL(String url) {
    this.url = url;
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof CciLocalTxManagedConnectionFactory) {
      String v1 = ((CciLocalTxManagedConnectionFactory) obj).url;
      String v2 = this.url;
      return (v1 == null) ? (v2 == null) : (v1.equals(v2));
    } else {
      return false;
    }
  }

  public int hashCode() {
    if (url == null) {
      return (new String("")).hashCode();
    } else {
      return url.hashCode();
    }
  }
}
