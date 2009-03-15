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

import com.sun.logging.LogDomains;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Tony Ng
 */
public class JdbcManagedConnectionFactory
        implements ManagedConnectionFactory, Serializable {

    private String XADataSourceName;
    private ResourceAdapter ra;
    private int count = 0;
    transient private Context ic;
    static Logger _logger;

    static {
        Logger _logger = LogDomains.getLogger( JdbcManagedConnectionFactory.class, LogDomains.RSR_LOGGER);
    }

    public JdbcManagedConnectionFactory() {
    }

    public Object createConnectionFactory(ConnectionManager cxManager)
            throws ResourceException {

        return new JdbcDataSource(this, cxManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return new JdbcDataSource(this, null);
    }

    public ManagedConnection
    createManagedConnection(Subject subject,
                            ConnectionRequestInfo info)
            throws ResourceException {

        try {
            XAConnection xacon = null;
            String userName = null;
            PasswordCredential pc =
                    Util.getPasswordCredential(this, subject, info);
            if (pc == null) {
                xacon = getXADataSource().getXAConnection();
            } else {
                userName = pc.getUserName();
                xacon = getXADataSource().
                        getXAConnection(userName,
                                new String(pc.getPassword()));
            }
            Connection con = xacon.getConnection();
            return new JdbcManagedConnection
                    (this, pc, xacon, con, true, true);
        } catch (SQLException ex) {
            ResourceException re =
                    new EISSystemException("SQLException: " + ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        }

    }

    public ManagedConnection
    matchManagedConnections(Set connectionSet,
                            Subject subject,
                            ConnectionRequestInfo info)
            throws ResourceException {
        _logger.severe(" In matchManagedConnections ");
        PasswordCredential pc =
                Util.getPasswordCredential(this, subject, info);
        Iterator it = connectionSet.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            count++;
            if (count == 3) {
                _logger.severe("@@@@@@ Sending ResourceErrorOccured event");
                count = 0;
                ((JdbcManagedConnection) obj).sendEvent(
                        javax.resource.spi.ConnectionEvent.CONNECTION_ERROR_OCCURRED,
                        null);
            }
            if (obj instanceof JdbcManagedConnection) {
                JdbcManagedConnection mc = (JdbcManagedConnection) obj;
                ManagedConnectionFactory mcf =
                        mc.getManagedConnectionFactory();
                if (Util.isPasswordCredentialEqual
                        (mc.getPasswordCredential(), pc) &&
                        mcf.equals(this)) {
                    return mc;
                }
            }
        }
        return null;
    }

    public void setLogWriter(PrintWriter out)
            throws ResourceException {

        try {
            getXADataSource().setLogWriter(out);
        } catch (SQLException ex) {
            // XXX I18N
            ResourceException rex = new ResourceException("SQLException");
            rex.setLinkedException(ex);
            throw rex;
        }
    }

    public PrintWriter getLogWriter() throws ResourceException {
        try {
            return getXADataSource().getLogWriter();
        } catch (SQLException ex) {
            // XXX I18N
            ResourceException rex = new ResourceException("SQLException");
            rex.setLinkedException(ex);
            throw rex;
        }
    }

    public String getXADataSourceName() {
        return XADataSourceName;
    }

    public void setXADataSourceName(String XADataSourceName) {
        this.XADataSourceName = XADataSourceName;
    }

    private XADataSource getXADataSource() throws ResourceException {
        try {
            if (ic == null) ic = new InitialContext();
            XADataSource ds = (XADataSource) ic.lookup(XADataSourceName);
            return ds;
        } catch (Exception ex) {
            ResourceException rex = new ResourceException("");
            rex.setLinkedException(ex);
            throw rex;
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof JdbcManagedConnectionFactory) {
            String v1 = ((JdbcManagedConnectionFactory) obj).
                    XADataSourceName;
            String v2 = this.XADataSourceName;
            return (v1 == null) ? (v2 == null) : (v1.equals(v2));
        } else {
            return false;
        }
    }

    public int hashCode() {
        if (XADataSourceName == null) {
            return (new String("")).hashCode();
        } else {
            return XADataSourceName.hashCode();
        }
    }

    public ResourceAdapter getResourceAdapter() {
        return this.ra;
    }

    public void setResourceAdapter(ResourceAdapter ra) {
        this.ra = ra;
    }
}
