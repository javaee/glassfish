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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.SecurityException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

/**
 * This class represents a physical connection to an underlying EIS.
 * @author Sheetal Vartak
 */
public class CciManagedConnection implements ManagedConnection {

  private XAConnection xacon;

  private java.sql.Connection con;

  private CciConnectionEventListener cciListener;

  private PasswordCredential passCred;

  private ManagedConnectionFactory mcf;

  private PrintWriter logWriter;

  private boolean supportsXA;

  private boolean supportsLocalTx;

  private boolean destroyed;

  private Set connectionSet; // set of CciConnection

  CciManagedConnection(ManagedConnectionFactory mcf, PasswordCredential passCred,
      XAConnection xacon, Connection con, boolean supportsXA, boolean supportsLocalTx) {
    this.mcf = mcf;
    this.passCred = passCred;
    this.xacon = xacon;
    this.con = con;
    this.supportsXA = supportsXA;
    this.supportsLocalTx = supportsLocalTx;
    connectionSet = new HashSet();
    cciListener = new CciConnectionEventListener(this);
    if (xacon != null) {
      xacon.addConnectionEventListener(cciListener);
    }

  }

  public void setXAConnection(XAConnection xa) {
    this.xacon = xa;
  }

  public void setConnection(java.sql.Connection con) {
    this.con = con;
  }

  public void setSupportsXA(boolean xa) {
    this.supportsXA = xa;
  }

  public void setSupportsLocalTx(boolean xa) {
    this.supportsLocalTx = xa;
  }

  public void setManagedConnectionFactory(ManagedConnectionFactory xa) {
    this.mcf = xa;
  }

  public XAConnection getXAConnection() {
    return this.xacon;
  }

  public java.sql.Connection getConnection() {
    return this.con;
  }

  public boolean getSupportsXA() {
    return this.supportsXA;
  }

  public boolean getSupportsLocalTx() {
    return this.supportsLocalTx;
  }

  public ManagedConnectionFactory getManagedConnectionFactory() {
    return this.mcf;
  }

  private void throwResourceException(SQLException ex) throws ResourceException {

    ResourceException re = new ResourceException("SQLException: " + ex.getMessage());
    re.setLinkedException(ex);
    throw re;
  }

  public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo)
      throws ResourceException {

    PasswordCredential pc = Util.getPasswordCredential(mcf, subject, connectionRequestInfo);
    if (!Util.isPasswordCredentialEqual(pc, passCred)) {
      throw new SecurityException("Principal does not match." + "Reauthentication not supported");
    }
    checkIfDestroyed();
    CciConnection cciCon = new CciConnection(this);
    addCciConnection(cciCon);
    return cciCon;
  }

  public void destroy() throws ResourceException {
    try {
      if (destroyed) return;
      destroyed = true;
      Iterator it = connectionSet.iterator();
      while (it.hasNext()) {
        CciConnection cciCon = (CciConnection) it.next();
        cciCon.invalidate();
      }
      connectionSet.clear();
      con.close();
      if (xacon != null) xacon.close();
    }
    catch (SQLException ex) {
      throwResourceException(ex);
    }
  }

  public void cleanup() throws ResourceException {
    try {
      checkIfDestroyed();
      Iterator it = connectionSet.iterator();
      while (it.hasNext()) {
        CciConnection cciCon = (CciConnection) it.next();
        cciCon.invalidate();
      }
      connectionSet.clear();
      if (xacon != null) {
        con.close();
        con = xacon.getConnection();
      } else {
        con.setAutoCommit(true);
      }
    }
    catch (SQLException ex) {
      throwResourceException(ex);
    }
  }

  public void associateConnection(Object connection) throws ResourceException {

    checkIfDestroyed();
    if (connection instanceof CciConnection) {
      CciConnection cciCon = (CciConnection) connection;
      cciCon.associateConnection(this);
    } else {
      throw new IllegalStateException("Invalid connection object: " + connection);
    }
  }

  public void addConnectionEventListener(ConnectionEventListener listener) {
    cciListener.addConnectorListener(listener);
  }

  public void removeConnectionEventListener(ConnectionEventListener listener) {

    cciListener.removeConnectorListener(listener);
  }

  public XAResource getXAResource() throws ResourceException {
    if (!supportsXA) {
      throw new NotSupportedException("XA transaction not supported");
    }
    try {
      checkIfDestroyed();
      return xacon.getXAResource();
    }
    catch (SQLException ex) {
      throwResourceException(ex);
      return null;
    }
  }

  public javax.resource.spi.LocalTransaction getLocalTransaction() throws ResourceException {
    if (!supportsLocalTx) {
      throw new NotSupportedException("Local transaction not supported");
    } else {
      checkIfDestroyed();
      return new SpiLocalTransactionImpl(this);
    }
  }

  public ManagedConnectionMetaData getMetaData() throws ResourceException {
    checkIfDestroyed();
    return new CciManagedConnectionMetaDataImpl(this);
  }

  public void setLogWriter(PrintWriter out) throws ResourceException {
    this.logWriter = out;
  }

  public PrintWriter getLogWriter() throws ResourceException {
    return logWriter;
  }

  public Connection getJdbcConnection() throws ResourceException {
    checkIfDestroyed();
    return con;
  }

  boolean isDestroyed() {
    return destroyed;
  }

  PasswordCredential getPasswordCredential() {
    return passCred;
  }

  public void sendEvent(int eventType, Exception ex) {
    cciListener.sendEvent(eventType, ex, null);
  }

  public void sendEvent(int eventType, Exception ex, Object connectionHandle) {
    cciListener.sendEvent(eventType, ex, connectionHandle);
  }

  public void removeCciConnection(CciConnection cciCon) {
    connectionSet.remove(cciCon);
  }

  public void addCciConnection(CciConnection cciCon) {
    connectionSet.add(cciCon);
  }

  private void checkIfDestroyed() throws ResourceException {
    if (destroyed) {
      throw new IllegalStateException("Managed connection is closed");
    }
  }
}
