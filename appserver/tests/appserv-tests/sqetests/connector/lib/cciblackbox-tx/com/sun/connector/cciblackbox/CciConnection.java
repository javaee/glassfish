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

import java.sql.SQLException;

import javax.naming.Context;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.ResultSetInfo;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.IllegalStateException;
import javax.rmi.PortableRemoteObject;

import wlstest.functional.connector.common.apps.ejb.test_proxy.ConnectorTest;
import weblogic.jndi.Environment;

/**
 * This implementation class represents an application level connection
 *handle that is used by a component to access an EIS instance.
 *
 * @author Sheetal Vartak
 */
public class CciConnection implements javax.resource.cci.Connection {

  private boolean destroyed;

  private CciManagedConnection mc;

  // if mc is null, means connection is invalid

  CciConnection(CciManagedConnection mc) {
    this.mc = mc;
  }

  CciManagedConnection getManagedConnection() {
    return mc;
  }

  public Interaction createInteraction() throws ResourceException {
    return new CciSQLInteraction(this);
  }

  public javax.resource.cci.LocalTransaction getLocalTransaction() throws ResourceException {
    try {
      java.sql.Connection con = getJdbcConnection();
      if (con.getTransactionIsolation() == con.TRANSACTION_NONE) {
        throw new ResourceException("Local Transaction not supported!!");
      }
    }
    catch (Exception e) {
      throw new ResourceException(e.getMessage());
    }
    return new CciLocalTransactionImpl(mc);
  }

  public void setAutoCommit(boolean autoCommit) throws ResourceException {

    try {
      java.sql.Connection con = getJdbcConnection();
      if (con.getTransactionIsolation() == con.TRANSACTION_NONE) {
        throw new ResourceException("Local Transaction not " + "supported!!");
      }
      con.setAutoCommit(autoCommit);
    }
    catch (Exception e) {
      throw new ResourceException(e.getMessage());
    }
  }

  public boolean getAutoCommit() throws ResourceException {

    boolean val = false;
    try {
      java.sql.Connection con = getJdbcConnection();
      if (con.getTransactionIsolation() == con.TRANSACTION_NONE) {
        throw new ResourceException("Local Transaction not " + "supported!!");
      }
      val = con.getAutoCommit();
    }
    catch (SQLException e) {
      throw new ResourceException(e.getMessage());
    }
    return val;
  }

  public ResultSetInfo getResultSetInfo() throws ResourceException {
    throw new NotSupportedException("ResultSet is not supported.");
  }

  public void close() throws ResourceException {
    if (mc == null) return; // already be closed
    mc.removeCciConnection(this);
    mc.sendEvent(ConnectionEvent.CONNECTION_CLOSED, null, this);
    mc = null;
  }

  public ConnectionMetaData getMetaData() throws ResourceException {
    return new CciConnectionMetaDataImpl(mc);
  }

  void associateConnection(CciManagedConnection newMc) throws ResourceException {

    try {
      checkIfValid();
    }
    catch (ResourceException ex) {
      throw new IllegalStateException("Connection is invalid");
    }
    // dissociate handle with current managed connection
    mc.removeCciConnection(this);
    // associate handle with new managed connection
    newMc.addCciConnection(this);
    mc = newMc;
  }

  void checkIfValid() throws ResourceException {
    if (mc == null) {
      throw new ResourceException("Connection is invalid");
    }
  }

  java.sql.Connection getJdbcConnection() throws SQLException {

    java.sql.Connection con = null;
    try {
      checkIfValid();
      //  mc.getJdbcConnection() returns a SQL connection object
      con = mc.getJdbcConnection();
    }
    catch (ResourceException ex) {
      throw new SQLException("Connection is invalid.");
    }
    return con;
  }

  void invalidate() {
    mc = null;
  }

  private void checkIfDestroyed() throws ResourceException {
    if (destroyed) {
      throw new IllegalStateException("Managed connection is closed");
    }
  }

  class Internal {
    public Object narrow(Object ref, Class c) {
      return PortableRemoteObject.narrow(ref, c);
    }
  }

  public boolean calcMultiply(String serverUrl, String testUser, String testPassword,
      String testJndiName, int num1, int num2) {

    Context ctx = null;
    ConnectorTest connectorTest = null;
    Environment env = null;
    boolean result;
    try {
      System.out.println("###  calcMultiply");
      env = new Environment();
      env.setProviderUrl(serverUrl);
      env.setSecurityPrincipal(testUser);
      env.setSecurityCredentials(testPassword);
      ctx = env.getInitialContext();
      System.out.println("Lookup for " + testJndiName);
      connectorTest = (ConnectorTest) ctx.lookup(testJndiName);
      //Internal intenalRef = new Internal();
      System.out.println("ConnectorTest is " + connectorTest);
      //ConnectorTest connectorTestRemote = (ConnectorTest) intenalRef.narrow(connectorTestHome.create(), ConnectorTest.class);
      if (connectorTest.calcMultiply(num1, num2) == (num1 * num2)) {
        result = true;
      } else {
        result = false;
      }
    }
    catch (Exception e) {

      result = false;
      System.out.println("Exception in calcMultiply ");
      e.printStackTrace();
    }
    return result;
  }
}
