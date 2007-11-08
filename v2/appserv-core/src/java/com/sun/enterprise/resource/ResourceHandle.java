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

import javax.transaction.xa.XAResource;
import javax.resource.spi.ConnectionEventListener;

import com.sun.logging.LogDomains;
import java.util.logging.Level;
import java.util.logging.Logger;

//  START OF IASRI 4629815
//
import javax.security.auth.Subject; // Added by Miriam - ECU
//  END OF IASRI 4629815
//


/**
 * ResourceHandle encapsulates a resource connection.
 * Equality on the handle is based on the id field
 * 
 * @author Tony Ng
 */
public class ResourceHandle {

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
    private boolean supportsXAResource=false;

    private Subject subject = null;            //Added by Miriam - ECU .

    private ResourceState state = null;
    private ConnectionEventListener listener = null;
    private boolean associated_ = false;
    private long threadId_;

    private static Logger logger = 
	LogDomains.getLogger(LogDomains.RSR_LOGGER);
    
    private boolean supportsLazyEnlistment_ = false;
    private boolean supportsLazyAssoc_ = false;

    //To suspend and enable lazy enlistment - added by Jagadish Ramu

    private boolean enlistmentSuspended = false;
    public final Object lock = new Object();
    private boolean dirty_;
    private long lastValidated; //holds the latest time at which the connection was validated.
    private int usageCount; //holds the no. of times the handle(connection) is used so far.
    
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

        if ( resource instanceof 
            javax.resource.spi.LazyEnlistableManagedConnection ) {
            supportsLazyEnlistment_ = true;
        }

        if ( resource instanceof 
            javax.resource.spi.DissociatableManagedConnection ) {
            supportsLazyAssoc_ = true;
        }
    }
    //START OF IASRI 4721130
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

        dirty_ = false;
    }    
    //END OF IASRI 4721130
    /**
     * Does this resource need enlistment to transaction manager?
     */
    public boolean isTransactional() {
        return alloc.isTransactional();
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
   //  START OF IASRI 4629815
   //

    //Added by Miriam - ECU    
    public void setSubject(Subject subject){
        this.subject = subject;
    }
    //Added by Miriam - ECU
    public Subject getSubject(){
        return subject;
    }

   //  END OF IASRI 4629815
   //

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

    //GJCINT
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

    public boolean isEnlisted() {
        return state != null && state.isEnlisted();
    }

    public boolean isAssociated() {
        return associated_;
    }

    public void setAssociated( boolean flag ) {
        associated_ = flag;
    }

    public long getThreadId() {
        return threadId_;
    }

    public void setThreadId( long threadId ) {
        threadId_ = threadId;
    }

    public boolean supportsLazyEnlistment() {
        return supportsLazyEnlistment_;
    }

    public boolean supportsLazyAssociation() {
        return supportsLazyAssoc_;
    }

    public boolean isDirty() {
        return dirty_;
    }

    public void setDirty() {
        dirty_ = true;
    }

    /**
     * To check whether lazy enlistment is suspended or not.<br>
     * If true, TM will not do enlist/lazy enlist.
     * @return boolean
     */
    public boolean isEnlistmentSuspended()
    {
        return enlistmentSuspended;
    }

    public void setEnlistmentSuspended(boolean enlistmentSuspended)
    {
        this.enlistmentSuspended = enlistmentSuspended;
    }

    public long getLastValidated() {
     return lastValidated;
    }

    public void setLastValidated(long lastValidated) {
     this.lastValidated = lastValidated;
    }


    public int getUsageCount(){
        return usageCount;
    }

    public void incrementUsageCount(){
        usageCount++;
     }

}
