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
