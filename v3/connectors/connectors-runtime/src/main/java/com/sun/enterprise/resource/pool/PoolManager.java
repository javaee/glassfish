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
package com.sun.enterprise;

import java.util.Vector;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.security.Principal;
import javax.transaction.Transaction;
import javax.sql.XAConnection;
import com.sun.enterprise.resource.*;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import javax.security.auth.Subject;
import javax.transaction.xa.Xid;
import javax.naming.Reference;
import javax.resource.spi.ManagedConnection;
import javax.resource.ResourceException;

import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorConstants.PoolType;

/**
 *
 * PoolManager manages resource connections such as JDBC connections
 *
 */
public interface PoolManager {

    // transaction support levels
    static public final int NO_TRANSACTION = 0;
    static public final int LOCAL_TRANSACTION = 1;
    static public final int XA_TRANSACTION = 2;

    // Authentication mechanism levels
    static public final int BASIC_PASSWORD = 0;
    static public final int KERBV5 = 1;

    // Credential Interest levels
    static public final String PASSWORD_CREDENTIAL = "javax.resource.spi.security.PasswordCredential";
    static public final String GENERIC_CREDENTIAL = "javax.resource.spi.security.GenericCredential";

    /**
     * Obtain a transactional resource such as JDBC connection 
     *
     * @param spec Specification for the resource
     * @param alloc Allocator for the resource
     * @param info Client security for this request
     *
     * @return An object that represents a connection to the resource
     *
     * @exception ResourceReferenceException Thrown if some error occurs while
     * obtaining the resource
     */
    Object getResource(ResourceSpec spec, ResourceAllocator alloc,
                       ClientSecurityInfo info)
        throws PoolingException;

    ResourceReferenceDescriptor 
        getResourceReference(String jndiName);

    void resourceEnlisted(Transaction tran, ResourceHandle res)
         throws IllegalStateException;

    void resourceClosed(ResourceHandle res);
    void badResourceClosed(ResourceHandle res);

    void resourceErrorOccurred(ResourceHandle res);

    void transactionCompleted(Transaction tran, int status);

    public void putbackResourceToPool(ResourceHandle h, 
                                      boolean errorOccurred);

    public void putbackBadResourceToPool(ResourceHandle h);

    public void putbackDirectToPool(ResourceHandle h, String poolName);

    public ResourceHandle getResourceFromPool(ResourceSpec spec, 
                                              ResourceAllocator alloc,
                                              ClientSecurityInfo info,
                                              Transaction tran)
        throws PoolingException;

    public void registerResource(ResourceHandle resource)
        throws PoolingException;

    public void unregisterResource(ResourceHandle resource,
                                   int xaresFlag);

    public void emptyResourcePool(ResourceSpec spec);

    public void killPool( String poolName );

    public void reconfigPoolProperties( ConnectorConnectionPool ccp ) 
            throws PoolingException;

    //sets/resets the monitoring levels for the pool
    public void disableMonitoring( String poolName);	    
    public void setMonitoringEnabledHigh( String poolName);	    
    public void setMonitoringEnabledLow( String poolName);	    
    
    //get the pooltable
    public ConcurrentHashMap getPoolTable();

    //register the MonitoringLevelListeners
    public void initializeMonitoring();

    public boolean switchOnMatching(String poolName);

    public void killAllPools();

    public void killFreeConnectionsInPools(); 

    public void createEmptyConnectionPool(String name, 
        PoolType pt) throws PoolingException;

    public ResourcePool getPool( String poolName );

    public void setSelfManaged( String poolName, boolean flag );

    public void lazyEnlist( ManagedConnection mc ) throws ResourceException;

    public void postInvoke();
}

