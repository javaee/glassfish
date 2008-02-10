/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.enterprise.connectors;

import com.sun.enterprise.connectors.util.ResourcesUtil;
import java.util.Hashtable;
import java.util.logging.*;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.*;
import javax.sql.DataSource;

import com.sun.enterprise.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.server.*;

/**
 * This is connector resource admin service. It creates and deletes the
 * connector resources.
 * @author    Srikanth P 
 */


public class ConnectorResourceAdminServiceImpl extends 
                     ConnectorServiceImpl implements ConnectorAdminService {
     
    /**
     * Default constructor
     */

     public ConnectorResourceAdminServiceImpl() {
         super();
     }

    /** 
     * Creates the connector resource on a given connection pool
     * @param jndiName JNDI name of the resource to be created
     * @poolName PoolName to which the connector resource belongs.
     * @resourceType Resource type Unused.
     * @throws ConnectorRuntimeException If the resouce creation fails.
     */

    public void createConnectorResource(String jndiName, String poolName, 
                    String resourceType) throws ConnectorRuntimeException
    {

        String errMsg = "rardeployment.jndi_lookup_failed";
        String name = poolName;
        try {
	    ConnectorConnectionPool connectorConnectionPool = null;
            String jndiNameForPool = ConnectorAdminServiceUtils.
                getReservePrefixedJNDINameForPool(poolName);
            InitialContext ic = new InitialContext();
            try {
                connectorConnectionPool = 
                    (ConnectorConnectionPool) ic.lookup(jndiNameForPool);
            } catch(NamingException ne) {
                checkAndLoadPoolResource(poolName);
            }

            connectorConnectionPool = 
                    (ConnectorConnectionPool) ic.lookup(jndiNameForPool);
            ConnectorDescriptorInfo cdi = connectorConnectionPool.
                getConnectorDescriptorInfo();

            javax.naming.Reference ref=new  javax.naming.Reference( 
                   connectorConnectionPool.getConnectorDescriptorInfo().
                   getConnectionFactoryClass(), 
                   "com.sun.enterprise.naming.factory.ConnectorObjectFactory",
                   null);
            StringRefAddr addr = new StringRefAddr("poolName",poolName);
            ref.add(addr);
            addr = new StringRefAddr("rarName", cdi.getRarName() );
            ref.add(addr);

            errMsg = "Failed to bind connector resource in JNDI";
            name = jndiName;
            Switch.getSwitch().getNamingManager().publishObject(
                          jndiName,ref,true);
            //To notify that a connector resource rebind has happened.
            ConnectorResourceNamingEventNotifier.getInstance().
                    notifyListeners(
                            new ConnectorNamingEvent(
                                    jndiName,ConnectorNamingEvent.EVENT_OBJECT_REBIND));

        } catch(NamingException ne) {
            ConnectorRuntimeException cre = 
                  new ConnectorRuntimeException(errMsg);
            cre.initCause(ne);
            _logger.log(Level.SEVERE,errMsg, name); 
            _logger.log(Level.SEVERE,"", cre); 
            throw cre;
        }
    } 

    /**
     * Deletes the connector resource.
     * @param jndiName JNDI name of the resource to delete.
     * @throws ConnectorRuntimeException if connector resource deletion fails.
     */

    public void deleteConnectorResource(String jndiName) 
                       throws ConnectorRuntimeException 
    {

        try {
            InitialContext ic = new InitialContext();
            ic.unbind(jndiName);
        } catch(NamingException ne) {
            ResourcesUtil resUtil = ResourcesUtil.createInstance();
            if(resUtil.resourceBelongsToSystemRar(jndiName)) {
                return;
            }
            if(ne instanceof  NameNotFoundException){
                _logger.log(Level.FINE,
                    "rardeployment.connectorresource_removal_from_jndi_error",
                    jndiName);
                _logger.log(Level.FINE,"", ne);
                return;
            }
            ConnectorRuntimeException cre =  new ConnectorRuntimeException(
                            "Failed to delete connector resource from jndi");
            cre.initCause(ne);
            _logger.log(Level.SEVERE,
                    "rardeployment.connectorresource_removal_from_jndi_error",
                    jndiName);
            _logger.log(Level.SEVERE,"", cre);
            throw cre;
        }
    } 

    /**
     * If the suffix is one of the valid context return true. 
     * Return false, if that is not the case.
     */
    public boolean isValidJndiSuffix(String suffix) {
        if (suffix != null) {
            for (String validSuffix : ConnectorConstants.JNDI_SUFFIX_VALUES) {
                if (validSuffix.equals(suffix)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Look up the JNDI name with appropriate suffix.
     * Suffix can be either __pm or __nontx.
     */
    public Object lookup(String name) throws NamingException {
        Hashtable ht = null;
        String suffix = getValidSuffix(name); 
        if (suffix != null) {
            ht = new Hashtable();
            ht.put(ConnectorConstants.JNDI_SUFFIX_PROPERTY, suffix);
            name = name.substring(0, name.lastIndexOf(suffix));
        }
        InitialContext ic = new InitialContext(ht);
        return ic.lookup(name);
    }

    /**
     *  Gets Connector Resource Rebind Event notifier.
     * @return  ConnectorNamingEventNotifier
     */
    public ConnectorNamingEventNotifier getResourceRebindEventNotifier(){
        return ConnectorResourceNamingEventNotifier.getInstance();
    }

    private String getValidSuffix(String name) {
        if (name != null) {
            for (String validSuffix : ConnectorConstants.JNDI_SUFFIX_VALUES) {
                if (name.endsWith(validSuffix)) {
                    return validSuffix;
                }
            }
        }
        return null;
    }

    /**
     * Get a wrapper datasource specified by the jdbcjndi name
     * This API is intended to be used in the DAS. The motivation for having this
     * API is to provide the CMP backend/ JPA-Java2DB a means of acquiring a connection during
     * the codegen phase. If a user is trying to deploy an JPA-Java2DB app on a remote server,
     * without this API, a resource reference has to be present both in the DAS
     * and the server instance. This makes the deployment more complex for the
     * user since a resource needs to be forcibly created in the DAS Too.
     * This API will mitigate this need.
     *
     * @param jndiName the jndi name of the resource
     * @return DataSource representing the resource.
     */
    protected Object lookupDataSourceInDAS(String jndiName){
        MyDataSource myDS = new MyDataSource();
        myDS.setJndiName(jndiName);
        return myDS;
    }

    class MyDataSource implements DataSource {
            private String jndiName ;
            private PrintWriter logWriter;
            private int loginTimeout;

            public void setJndiName(String name){
                jndiName = name;
            }

            public Connection getConnection() throws SQLException {
                return ConnectorRuntime.getRuntime().getConnection(jndiName);
            }

            public Connection getConnection(String username, String password) throws SQLException {
                return ConnectorRuntime.getRuntime().getConnection(jndiName,username,password);
            }

            public PrintWriter getLogWriter() throws SQLException {
                return logWriter;
            }

            public void setLogWriter(PrintWriter out) throws SQLException {
               this.logWriter = out;
            }

            public void setLoginTimeout(int seconds) throws SQLException {
               loginTimeout = seconds;
            }

            public int getLoginTimeout() throws SQLException {
                return loginTimeout;
            }
            public boolean isWrapperFor(Class<?> iface) throws SQLException{
               throw new SQLException("Not supported operation"); 
            }
            public <T> T unwrap(Class<T> iface) throws SQLException{
               throw new SQLException("Not supported operation"); 
            }
    }
}
