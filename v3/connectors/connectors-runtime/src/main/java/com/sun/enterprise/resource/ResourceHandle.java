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
package com.sun.enterprise.resource;

import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.allocator.LocalTxConnectorAllocator;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.transaction.spi.TransactionalResource;

import javax.resource.spi.ConnectionEventListener;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import javax.transaction.Transaction;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * ResourceHandle encapsulates a resource connection.
 * Equality on the handle is based on the id field
 *
 * @author Tony Ng
 */
public class ResourceHandle implements 
        com.sun.appserv.connectors.internal.api.ResourceHandle, TransactionalResource {

    // unique ID for resource handles
    static private long idSequence;

    private long id;
    private ClientSecurityInfo info;
    private Object resource;  // XAConnection for JDBC 2.0
    private ResourceSpec spec;
    private XAResource xares;
    private Object usercon;   // Connection for JDBC 2.0
    private ResourceAllocator alloc;
    private Object instance;  // the component instance holding this resource
    private int shareCount;   // sharing within a component (XA only)
    private boolean supportsXAResource = false;

    private Subject subject = null;

    private ResourceState state = null;
    private ConnectionEventListener listener = null;

    private static Logger logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);

    public final Object lock = new Object();
    private long lastValidated; //holds the latest time at which the connection was validated.
    private int usageCount; //holds the no. of times the handle(connection) is used so far.
    private int partition;

    static private long getNextId() {
        synchronized (ResourceHandle.class) {
            idSequence++;
            return idSequence;
        }
    }

    public ResourceHandle(Object resource,
                          ResourceSpec spec,
                          ResourceAllocator alloc,
                          ClientSecurityInfo info) {
        this.id = getNextId();
        this.spec = spec;
        this.info = info;
        this.resource = resource;
        this.alloc = alloc;

	if ( alloc instanceof LocalTxConnectorAllocator)
	    supportsXAResource = false;
	else
	    supportsXAResource = true;

    }

	public ResourceHandle(Object resource,
                          ResourceSpec spec,
                          ResourceAllocator alloc,
                          ClientSecurityInfo info,
			  boolean supportsXA) {
        this.id = getNextId();
        this.spec = spec;
        this.info = info;
        this.resource = resource;
        this.alloc = alloc;

		supportsXAResource = supportsXA;

    }    


    /**
     * Does this resource need enlistment to transaction manager?
     */
    public boolean isTransactional() {
        return alloc.isTransactional();
    }

    public boolean isEnlistmentSuspended() {
        //throw new UnsupportedOperationException("Transaction is not supported yet");
        //TODO V3 till lazy enlistment is done
        return false;
    }

    public boolean supportsXA() {
        return supportsXAResource;
    }

    public ResourceAllocator getResourceAllocator() {
        return alloc;
    }

    public Object getResource() {
        return resource;
    }

    public ClientSecurityInfo getClientSecurityInfo() {
        return info;
    }

    public void setResourceSpec(ResourceSpec spec) {
        this.spec = spec;
    }

    public ResourceSpec getResourceSpec() {
        return spec;
    }

    public XAResource getXAResource() {
        return xares;
    }

    public Object getUserConnection() {
        return usercon;
    }

    public void setComponentInstance(Object instance) {
        this.instance = instance;
    }

    public void closeUserConnection() throws PoolingException {
        getResourceAllocator().closeUserConnection(this);
    }

    public Object getComponentInstance() {
        return instance;
    }

    public void fillInResourceObjects(Object userConnection,
                                      XAResource xaRes) {
        if (userConnection != null) usercon = userConnection;

        if (xaRes !=null) {
           if(logger.isLoggable(Level.FINEST)){
             //When Log level is Finest, XAResourceWrapper is used to log
             //all XA interactions - Don't wrap XAResourceWrapper if it is 
             //already wrapped
               if ((xaRes instanceof XAResourceWrapper) ||
                       (xaRes instanceof ConnectorXAResource)) {
                   this.xares = xaRes;
               } else {
                   this.xares = new XAResourceWrapper(xaRes);
               }
           } else {
            this.xares = xaRes;
           }
        }

    }

    // For XA-capable connections, multiple connections within a
    // component are collapsed into one. shareCount keeps track of
    // the number of additional shared connections
    public void incrementCount() {
        shareCount++;
    }

    public void decrementCount() {
        if (shareCount == 0) {
            throw new IllegalStateException("shareCount cannot be negative");
        } else {
            shareCount--;
        }
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Subject getSubject() {
        return subject;
    }

    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof ResourceHandle) {
            return this.id == (((ResourceHandle) other).id);
        }
        return false;
    }

    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

    public String toString() {
        return String.valueOf(id);
    }

    private boolean connectionErrorOccurred = false;

    public void setConnectionErrorOccurred() {
        connectionErrorOccurred = true;
    }

    public boolean hasConnectionErrorOccurred() {
        return connectionErrorOccurred;
    }

    public void setResourceState(ResourceState state) {
        this.state = state;
    }

    public ResourceState getResourceState() {
        return state;
    }

    public void setListener(ConnectionEventListener l) {
        this.listener = l;
    }

    public ConnectionEventListener getListener() {
        return listener;
    }

    public boolean isShareable() {
        return alloc.shareableWithinComponent();
    }

    public void destroyResource() {
        throw new UnsupportedOperationException("Transaction is not supported yet");
    }

    public boolean isEnlisted() {
        return state != null && state.isEnlisted();
    }

    public long getLastValidated() {
        return lastValidated;
    }

    public void setLastValidated(long lastValidated) {
        this.lastValidated = lastValidated;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void incrementUsageCount() {
        usageCount++;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public String getName() {
        return spec.getResourceId();
    }

    public void enlistedInTransaction(Transaction tran) throws IllegalStateException {
        ConnectorRuntime.getRuntime().getPoolManager().resourceEnlisted(tran, this);
    }
}
