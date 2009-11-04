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
package com.sun.enterprise.resource.pool;

import com.sun.appserv.connectors.internal.api.ConnectorConstants.PoolType;
import com.sun.appserv.connectors.internal.api.TransactedPoolManager;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.listener.PoolLifeCycle;
import org.jvnet.hk2.annotations.Contract;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.transaction.Transaction;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PoolManager manages jdbc and connector connection pool
 */
@Contract
public interface PoolManager extends TransactedPoolManager {

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
     * Flush Connection pool by reinitializing the connections 
     * established in the pool.
     * @param poolName
     * @throws com.sun.appserv.connectors.internal.api.PoolingException
     */
    public boolean flushConnectionPool(String poolName) throws PoolingException;

    //Get status of pool
    public PoolStatus getPoolStatus(String poolName);
    

    ResourceHandle getResourceFromPool(ResourceSpec spec,
                                       ResourceAllocator alloc,
                                       ClientSecurityInfo info,
                                       Transaction tran)
            throws PoolingException;

    public void createEmptyConnectionPool(String name, PoolType pt) throws PoolingException;


    public void putbackResourceToPool(ResourceHandle h, boolean errorOccurred);

    public void putbackBadResourceToPool(ResourceHandle h);

    public void putbackDirectToPool(ResourceHandle h, String poolName);


    void resourceClosed(ResourceHandle res);

    void badResourceClosed(ResourceHandle res);

    void resourceErrorOccurred(ResourceHandle res);


    void transactionCompleted(Transaction tran, int status);

    public void emptyResourcePool(ResourceSpec spec);

    public void killPool(String poolName);

    public void reconfigPoolProperties(ConnectorConnectionPool ccp) throws PoolingException;


    //sets/resets the monitoring levels for the pool
    public void disableMonitoring(String poolName);

    public void setMonitoringEnabledHigh(String poolName);

    public void setMonitoringEnabledLow(String poolName);

    //register the MonitoringLevelListeners
    public void initializeMonitoring();


    //get the pooltable
    public ConcurrentHashMap getPoolTable();

    public ConcurrentHashMap getMonitoredPoolTable();


    public boolean switchOnMatching(String poolName);

    /**
     * Obtain a transactional resource such as JDBC connection
     *
     * @param spec  Specification for the resource
     * @param alloc Allocator for the resource
     * @param info  Client security for this request
     * @return An object that represents a connection to the resource
     * @throws PoolingException Thrown if some error occurs while
     *                          obtaining the resource
     */
    Object getResource(ResourceSpec spec, ResourceAllocator alloc, ClientSecurityInfo info)
            throws PoolingException;

    ResourceReferenceDescriptor getResourceReference(String jndiName);


    public void killAllPools();

    public void killFreeConnectionsInPools();


    public ResourcePool getPool(String poolName);

    public void setSelfManaged(String poolName, boolean flag);

    public void lazyEnlist(ManagedConnection mc) throws ResourceException;

    public void registerPoolLifeCycleListener(PoolLifeCycle poolListener);
    
    public void unregisterPoolLifeCycleListener();
}

