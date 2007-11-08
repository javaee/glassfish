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

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

/*
 * SFSBStoreConnectionUtil.java
 *
 * Created on December 3, 2003, 3:40 PM
 */

package com.sun.ejb.ee.sfsb.store;

import java.sql.*;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import com.sun.logging.LogDomains;
import com.sun.ejb.base.sfsb.util.EJBServerConfigLookup;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.enterprise.ee.web.sessmgmt.ConnectionUtil;
import com.sun.enterprise.ee.web.sessmgmt.HAErrorManager;
import com.sun.enterprise.ee.web.sessmgmt.HADBConnectionGroup;
//import org.apache.catalina.*;

/**
 *
 * @author  lwhite
 */
public class SFSBStoreConnectionUtil extends ConnectionUtil {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;
    
    
    /** Creates a new instance of SFSBStoreConnectionUtil */
    public SFSBStoreConnectionUtil(Object cont, SFSBStoreManager mgr) {
        //container = cont;
        super(cont);
        manager = mgr;
    } 
    
    /**
    * get connection data source string for the connection
    */     
    protected String getDataSourceNameFromConfig() {
        if (dataSourceString == null) {        
            EJBServerConfigLookup configLookup = new EJBServerConfigLookup();
            dataSourceString = configLookup.getHaStorePoolJndiNameFromConfig();
        }
        return dataSourceString;
    }
    
    /**
    * return a HADBConnectionGroup from the pool
    */    
    public Connection doSuperGetConnection(boolean autoCommit) throws IOException {
        
        return super.getConnection(autoCommit);
    } 
    
    /**
    * return a HADBConnectionGroup from the pool
    */    
    public Connection doSuperGetConnection2() throws IOException {
        
        return super.getConnection();
    }    
    
    /**
    * return a HADBConnectionGroup from the pool
    */    
    public HADBConnectionGroup doSuperGetConnectionsFromPool(boolean autoCommit) throws IOException {
        
        return super.getConnectionsFromPool(autoCommit);
    }
    
    /**
    * return a HADBConnectionGroup from the pool
    */    
    public HADBConnectionGroup doSuperGetConnectionsFromPool2() throws IOException {
        
        return super.getConnectionsFromPool();
    }    
    
    /**
     * 
     */
    public Connection getConnection(boolean autoCommit) throws IOException {

        Connection conn = null;

        if (System.getSecurityManager() != null) {
            try {
                conn = (Connection) AccessController.doPrivileged(new PrivilegedGetConnection(autoCommit));
            } catch (PrivilegedActionException ex){
                Exception exception = ex.getException();
                //log.error("Exception getting connection group: " + exception);
                if (exception instanceof IOException){
                    throw (IOException)exception;
                }
            }
        } else {
            conn = this.doSuperGetConnection(autoCommit);
        }        
        return conn;
    } 
    
    /**
     * 
     */
    public Connection getConnection() throws IOException {

        Connection conn = null;

        if (System.getSecurityManager() != null) {
            try {
                conn = (Connection) AccessController.doPrivileged(new PrivilegedGetConnection2());
            } catch (PrivilegedActionException ex){
                Exception exception = ex.getException();
                //log.error("Exception getting connection group: " + exception);
                if (exception instanceof IOException){
                    throw (IOException)exception;
                }
            }
        } else {
            conn = this.doSuperGetConnection2();
        }        
        return conn;
    }       
    
    /**
     * 
     */
    public HADBConnectionGroup getConnectionsFromPool(boolean autoCommit) throws IOException {

        HADBConnectionGroup connGroup = null;

        if (System.getSecurityManager() != null) {
            try {
                connGroup = (HADBConnectionGroup) AccessController.doPrivileged(new PrivilegedGetConnections(autoCommit));
            } catch (PrivilegedActionException ex){
                Exception exception = ex.getException();
                //log.error("Exception getting connection group: " + exception);
                if (exception instanceof IOException){
                    throw (IOException)exception;
                }
            }
        } else {
            connGroup = this.doSuperGetConnectionsFromPool(autoCommit);
        }        
        return connGroup;
    } 
    
    /**
     * 
     */
    public HADBConnectionGroup getConnectionsFromPool() throws IOException {

        HADBConnectionGroup connGroup = null;

        if (System.getSecurityManager() != null) {
            try {
                connGroup = (HADBConnectionGroup) AccessController.doPrivileged(new PrivilegedGetConnections2());
            } catch (PrivilegedActionException ex){
                Exception exception = ex.getException();
                //log.error("Exception getting connection group: " + exception);
                if (exception instanceof IOException){
                    throw (IOException)exception;
                }
            }
        } else {
            connGroup = this.doSuperGetConnectionsFromPool2();
        }        
        return connGroup;
    }     
    
    private class PrivilegedGetConnections
        implements PrivilegedExceptionAction {

        private boolean autoCommit;    
            
        PrivilegedGetConnections(boolean autoCommit) {     
            this.autoCommit = autoCommit;
        }

        public Object run() throws Exception{
           return doSuperGetConnectionsFromPool(this.autoCommit);
        }                       
    }
    
    private class PrivilegedGetConnections2
        implements PrivilegedExceptionAction {   
            
        PrivilegedGetConnections2() {     
        }

        public Object run() throws Exception{
           return doSuperGetConnectionsFromPool2();
        }                       
    }    
    
    private class PrivilegedGetConnection
        implements PrivilegedExceptionAction {

        private boolean autoCommit;    
            
        PrivilegedGetConnection(boolean autoCommit) {     
            this.autoCommit = autoCommit;
        }

        public Object run() throws Exception{
           return doSuperGetConnection(this.autoCommit);
        }                       
    }
    
    private class PrivilegedGetConnection2
        implements PrivilegedExceptionAction {    
            
        PrivilegedGetConnection2() {     
        }

        public Object run() throws Exception{
           return doSuperGetConnection2();
        }                       
    }     
    
}


