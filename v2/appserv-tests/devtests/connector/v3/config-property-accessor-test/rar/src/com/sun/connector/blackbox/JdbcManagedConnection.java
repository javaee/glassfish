/*
 * Use of this J2EE Connectors Sample Source Code file is governed by
 * the following modified BSD license:
 * 
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES
 * SUFFERED BY LICENSEE AS A RESULT OF  OR RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA,
 * OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package com.sun.connector.blackbox;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.SecurityException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Tony Ng
 */
public class JdbcManagedConnection implements ManagedConnection {

    private XAConnection xacon;
    private Connection con;
    private JdbcConnectionEventListener jdbcListener;
    private PasswordCredential passCred;
    private ManagedConnectionFactory mcf;
    private PrintWriter logWriter;
    private boolean supportsXA;
    private boolean supportsLocalTx;
    private boolean destroyed;
    private Set connectionSet;  // set of JdbcConnection

    JdbcManagedConnection(ManagedConnectionFactory mcf,
                          PasswordCredential passCred,
                          XAConnection xacon,
                          Connection con,
                          boolean supportsXA,
                          boolean supportsLocalTx) {
        this.mcf = mcf;
        this.passCred = passCred;
        this.xacon = xacon;
        this.con = con;
        this.supportsXA = supportsXA;
        this.supportsLocalTx = supportsLocalTx;
        connectionSet = new HashSet();
        jdbcListener = new JdbcConnectionEventListener(this);
        if (xacon != null) {
            xacon.addConnectionEventListener(jdbcListener);
        }

    }

    // XXX should throw better exception
    private void throwResourceException(SQLException ex)
            throws ResourceException {

        ResourceException re =
                new ResourceException("SQLException: " + ex.getMessage());
        re.setLinkedException(ex);
        throw re;
    }

    public Object getConnection(Subject subject,
                                ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {

        PasswordCredential pc =
                Util.getPasswordCredential(mcf, subject, connectionRequestInfo);
        if (!Util.isPasswordCredentialEqual(pc, passCred)) {
            throw new SecurityException("Principal does not match. Reauthentication not supported");
        }
        checkIfDestroyed();
        JdbcConnection jdbcCon =
                new JdbcConnection(this, this.supportsLocalTx);
        addJdbcConnection(jdbcCon);
        return jdbcCon;
    }

    public void destroy() throws ResourceException {
        try {
            if (destroyed) return;
            destroyed = true;
            Iterator it = connectionSet.iterator();
            while (it.hasNext()) {
                JdbcConnection jdbcCon = (JdbcConnection) it.next();
                jdbcCon.invalidate();
            }
            connectionSet.clear();
            con.close();
            if (xacon != null) xacon.close();
        } catch (SQLException ex) {
            throwResourceException(ex);
        }
    }

    public void cleanup() throws ResourceException {
        try {
            checkIfDestroyed();
            Iterator it = connectionSet.iterator();
            while (it.hasNext()) {
                JdbcConnection jdbcCon = (JdbcConnection) it.next();
                // Dont invalidate during cleanup, invalidate during destroy
                //jdbcCon.invalidate();
            }
            connectionSet.clear();
            if (xacon != null) {
                con.close();
                con = xacon.getConnection();
            } else {
                con.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throwResourceException(ex);
        }
    }


    public void associateConnection(Object connection)
            throws ResourceException {

        checkIfDestroyed();
        if (connection instanceof JdbcConnection) {
            JdbcConnection jdbcCon = (JdbcConnection) connection;
            jdbcCon.associateConnection(this);
        } else {
            throw new IllegalStateException("Invalid connection object: " +
                    connection);
        }
    }


    public void addConnectionEventListener(ConnectionEventListener listener) {
        jdbcListener.addConnectorListener(listener);
    }


    public void removeConnectionEventListener
            (ConnectionEventListener listener) {

        jdbcListener.removeConnectorListener(listener);
    }


    public XAResource getXAResource() throws ResourceException {
        if (!supportsXA) {
            throw new NotSupportedException("XA transaction not supported");
        }
        try {
            checkIfDestroyed();
            return xacon.getXAResource();
        } catch (SQLException ex) {
            throwResourceException(ex);
            return null;
        }
    }

    public LocalTransaction getLocalTransaction() throws ResourceException {
        if (!supportsLocalTx) {
            throw new NotSupportedException("Local transaction not supported");
        } else {
            checkIfDestroyed();
            return new LocalTransactionImpl(this);
        }
    }

    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        checkIfDestroyed();
        return new MetaDataImpl(this);
    }

    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    Connection getJdbcConnection() throws ResourceException {
        checkIfDestroyed();
        return con;
    }

    boolean isDestroyed() {
        return destroyed;
    }

    PasswordCredential getPasswordCredential() {
        return passCred;
    }

    void sendEvent(int eventType, Exception ex) {
        jdbcListener.sendEvent(eventType, ex, null);
    }

    void sendEvent(int eventType, Exception ex, Object connectionHandle) {
        jdbcListener.sendEvent(eventType, ex, connectionHandle);
    }

    void removeJdbcConnection(JdbcConnection jdbcCon) {
        connectionSet.remove(jdbcCon);
    }

    void addJdbcConnection(JdbcConnection jdbcCon) {
        connectionSet.add(jdbcCon);
    }

    private void checkIfDestroyed() throws ResourceException {
        if (destroyed) {
            throw new IllegalStateException("Managed connection is closed");
        }
    }

    ManagedConnectionFactory getManagedConnectionFactory() {
        return mcf;
    }
}
