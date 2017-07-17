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

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Tony Ng
 */
public class LocalTxManagedConnectionFactory
        implements ValidatingManagedConnectionFactory, ManagedConnectionFactory, Serializable, ResourceAdapterAssociation {

    private String url;
    private ResourceAdapter ra;
    private int count = 0;
    private boolean booleanWithIsBooleanAccessor;
    private boolean booleanWithGetBooleanAccessor;

    public LocalTxManagedConnectionFactory() {
    }

    public Object createConnectionFactory(ConnectionManager cxManager)
            throws ResourceException {

        return new JdbcDataSource_Tx(this, cxManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return new JdbcDataSource_Tx(this, null);
    }

    public ManagedConnection
    createManagedConnection(Subject subject,
                            ConnectionRequestInfo info)
            throws ResourceException {

        try {
            validateConfigProperties();
            Connection con = null;
            String userName = null;
            PasswordCredential pc =
                    Util.getPasswordCredential(this, subject, info);

            System.out.println("############ ###########################");
            System.out.println("############ Attempting to load driver...");
            System.out.println("############ ###########################");
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            Driver dr = DriverManager.getDriver(url);

            if (pc == null) {
                System.out.println("############ ###########################");
                System.out.println("############ Attempting to Connect...");
                System.out.println("############ ###########################");
                con = DriverManager.getConnection(url);
            } else {
                userName = pc.getUserName();
                con = DriverManager.getConnection
                        (url, userName, new String(pc.getPassword()));
            }
            return new com.sun.connector.blackbox.JdbcManagedConnection
                    (this, pc, null, con, false, true);
        } catch (SQLException ex) {
            ResourceException re =
                    new EISSystemException("SQLException: " + ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        } catch (ClassNotFoundException cnfe) {
            ResourceException re1 =
                    new EISSystemException("SQLException: " + cnfe.getMessage());
            re1.setLinkedException(cnfe);
            throw re1;

        }

    }

    /**
     * Check whether the config properties booleanWithIsBooleanAccessor
     * and booleanWithGetBooleanAccessor are set to true
     * @throws ResourceException
     */
    private void validateConfigProperties() throws ResourceException {
        if (!getBooleanWithGetBooleanAccessor()) {
            throw new ResourceException("Blackbox RAR : Get Boolean Accessor is false");
        }
        
        if (!isBooleanWithIsBooleanAccessor()) {
            throw new ResourceException("Blackbox RAR : Is Boolean Accessor is false");
        }
    }

    public ManagedConnection
    matchManagedConnections(Set connectionSet,
                            Subject subject,
                            ConnectionRequestInfo info)
            throws ResourceException {
        PasswordCredential pc =
                Util.getPasswordCredential(this, subject, info);
        Iterator it = connectionSet.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof JdbcManagedConnection) {
                JdbcManagedConnection mc = (JdbcManagedConnection) obj;
                ManagedConnectionFactory mcf =
                        mc.getManagedConnectionFactory();
                if (Util.isPasswordCredentialEqual
                        (mc.getPasswordCredential(), pc) &&
                        mcf.equals(this)) {
                    /*
                         count++;
                         System.out.println("@@@@ count is => " + count );
                     if (count == 3) {
                     System.out.println(" Sending ConnectionErrorOccured");
                 //count = 0;
                         mc.sendEvent(
                     javax.resource.spi.ConnectionEvent.CONNECTION_ERROR_OCCURRED,
                     null);
                 return null;
                     }
                 */
                    return mc;
                }
            }
        }
        return null;
    }

    public Set getInvalidConnections(Set connectionSet) {
        HashSet conn = null;
        count++;
        JdbcManagedConnection mc = (JdbcManagedConnection)
                connectionSet.iterator().next();
        if (count == 3) {
            //Add a delay

            try {
                Thread.sleep(1000 * 5);
            } catch (Exception e) {
            }

            System.out.println(" Sending ConnectionErrorOccured");
            //count = 0;
            mc.sendEvent(
                    javax.resource.spi.ConnectionEvent.CONNECTION_ERROR_OCCURRED,
                    null);
            conn = new HashSet();
            conn.add(mc);

        }

        return conn;

    }

    public void setLogWriter(PrintWriter out)
            throws ResourceException {

        // DriverManager.setLogWriter(out);
    }

    public PrintWriter getLogWriter() throws ResourceException {
        // return DriverManager.getLogWriter();
        return null;
    }

    public String getConnectionURL() {
        return url;
    }

    public void setConnectionURL(String url) {
        this.url = url;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof LocalTxManagedConnectionFactory) {
            String v1 = ((LocalTxManagedConnectionFactory) obj).url;
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

    public ResourceAdapter getResourceAdapter() {
        return this.ra;
    }

    public void setResourceAdapter(javax.resource.spi.ResourceAdapter resourceAdapter) throws ResourceException {
        this.ra = (ResourceAdapter)resourceAdapter;
    }

    public void setBooleanWithIsBooleanAccessor(boolean value){
        booleanWithIsBooleanAccessor = value;
    }

    public boolean isBooleanWithIsBooleanAccessor(){
        return booleanWithIsBooleanAccessor;
    }


    public void setBooleanWithGetBooleanAccessor(boolean value){
        booleanWithGetBooleanAccessor = value;
    }

    public boolean getBooleanWithGetBooleanAccessor(){
        return booleanWithGetBooleanAccessor;
    }

}
