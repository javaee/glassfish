/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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
