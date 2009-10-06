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

import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.AssocWithThreadResourceHandle;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.appserv.connectors.internal.api.PoolingException;

import javax.transaction.Transaction;
import java.util.ArrayList;

/**
 * Associates a resource with the thread. When the same thread is used again,
 * it checks whether the resource associated with the thread can serve the request.
 *
 * @author Aditya Gore, Jagadish Ramu
 */
public class AssocWithThreadResourcePool extends ConnectionPool {

    private static ThreadLocal<AssocWithThreadResourceHandle> localResource =
            new ThreadLocal<AssocWithThreadResourceHandle>();

    public AssocWithThreadResourcePool(String poolName)
            throws PoolingException {
        super(poolName);
    }

    /**
     * Prefetch is called to check whether there there is a free resource is already associated with the thread
     * Only when prefetch is unable to find a resource, normal routine (getUnenlistedResource) will happen.
     * @param spec ResourceSpec
     * @param alloc ResourceAllocator
     * @param tran Transaction
     * @return ResourceHandle resource associated with the thread, if any
     */
    protected ResourceHandle prefetch(ResourceSpec spec,
                                      ResourceAllocator alloc, Transaction tran) {
        AssocWithThreadResourceHandle ar = localResource.get();
        if (ar != null) {
            //synch on ar and do a quick-n-dirty check to see if the local
            //resource is usable at all
            synchronized (ar.lock) {
                if ((ar.getThreadId() != Thread.currentThread().getId()) ||
                        ar.hasConnectionErrorOccurred() ||
                        ar.isDirty() || !ar.isAssociated()) {
                    //we were associated with someone else or resource error 
                    //occurred or resource was disassociated and used by some one else. So evict
                    //NOTE: We do not setAssociated to false here since someone
                    //else has associated this resource to themself. Also, if
                    //the eviction is because of a resourceError, the resource is
                    //not going to be used anyway.

                    localResource.remove();
                    return null;
                }

                if (ar.getResourceState().isFree() &&
                        ar.getResourceState().isUnenlisted()) {
                    if (matchConnections) {
                        if (!alloc.matchConnection(ar)) {
                            //again, since the credentials of the caller don't match
                            //evict from ThreadLocal
                            //also, mark the resource as unassociated and make this resource
                            //potentially usable
                            localResource.remove();
                            ar.setAssociated(false);
                            if(poolLifeCycleListener != null){
                                poolLifeCycleListener.connectionNotMatched();
                            }
                            return null;
                        }
                        if(poolLifeCycleListener != null){
                            poolLifeCycleListener.connectionMatched();
                        }
                    }

                    setResourceStateToBusy(ar);
                    if (maxConnectionUsage_ > 0) {
                        ar.incrementUsageCount();
                    }
                    if(poolLifeCycleListener != null) {
                        poolLifeCycleListener.connectionUsed();
                        //Decrement numConnFree
                        poolLifeCycleListener.decrementNumConnFree();
                        
                    }
                    return ar;
                }
            }
        }

        return null;
    }

    /**
     * to associate a resource with the thread
     * @param h ResourceHandle
     */
    private void setInThreadLocal(AssocWithThreadResourceHandle h) {
        if (h != null) {
            synchronized (h.lock) {
                h.setThreadId(Thread.currentThread().getId());
                h.setAssociated(true);
                localResource.set(h);
            }
        }
    }

    /**
     * check whether the resource is unused
     * @param h ResourceHandle
     * @return boolean representing resource usefullness
     */
    protected boolean isResourceUnused(ResourceHandle h) {
        return h.getResourceState().isFree() && !((AssocWithThreadResourceHandle) h).isAssociated();
    }


    // this is the RI getResource() with some modifications
    /**
     * return resource in free list. If none is found, returns null
     */
    protected ResourceHandle getUnenlistedResource(ResourceSpec spec,
                                                   ResourceAllocator alloc, Transaction tran) throws PoolingException {

        ResourceHandle result;
        result = super.getUnenlistedResource(spec, alloc, tran);

        //If we came here, that's because free doesn't have anything
        //to offer us. This could be because:
        //1. All free resources are associated
        //2. There are no free resources
        //3. We cannot create anymore free resources
        //Handle case 1 here

        //DISASSOCIATE
        if (result == null) {
            synchronized (this) {
                ResourceHandle resource;
                ArrayList<ResourceHandle> activeResources = new ArrayList<ResourceHandle>();

                while ((resource = ds.getResource()) != null) {
                    synchronized (resource.lock) {
                        //though we are checking resources from within the free list,
                        //we could have a situation where the resource was free upto
                        //this point, put just before we entered the synchronized block,
                        //the resource "h" got used by the thread that was associating it
                        //so we need to check for isFree also

                        if (resource.getResourceState().isUnenlisted() &&
                                resource.getResourceState().isFree()) {
                            if (!matchConnection(resource, alloc)) {
                                activeResources.add(resource);
                                continue;
                            }

                            if (resource.hasConnectionErrorOccurred()) {
                                activeResources.add(resource);
                                continue;
                            }
                            result = resource;
                            setResourceStateToBusy(result);
                            ((AssocWithThreadResourceHandle) result).setAssociated(false);

                            break;
                        }
                    }
                }

                for (ResourceHandle activeResource : activeResources) {
                    ds.returnResource(activeResource);
                }
            }
        }

        if (localResource.get() == null) {
            setInThreadLocal((AssocWithThreadResourceHandle) result);
        }

        return result;
    }

    /**
     * return the resource back to pool only if it is not associated with the thread.
     * @param h ResourceHandle
     */
    protected synchronized void freeUnenlistedResource(ResourceHandle h) {
        if (!((AssocWithThreadResourceHandle) h).isAssociated()) {
            ds.returnResource(h);
            //free.add(h);
        }
        //update monitoring data
        if(poolLifeCycleListener != null){
            poolLifeCycleListener.decrementConnectionUsed();
            poolLifeCycleListener.incrementNumConnFree(false, steadyPoolSize);
        }

        if (maxConnectionUsage_ > 0) {
            performMaxConnectionUsageOperation(h);
        }
        notifyWaitingThreads();
    }

    /**
     * destroys the resource
     * @param resourceHandle resource to be destroyed
     */
    public void deleteResource(ResourceHandle resourceHandle) {
        try {
            super.deleteResource(resourceHandle);
        } finally {
            //Note: here we are using the connectionErrorOccurred flag to indicate
            //that this resource is no longer usable. This flag would be checked while
            //getting from ThreadLocal
            //The main intention of marking this is to handle the case where 
            //failAllConnections happens
            //Note that setDirty only happens here - i.e during destroying of a 
            //resource

            synchronized (resourceHandle.lock) {
                ((AssocWithThreadResourceHandle) resourceHandle).setDirty();
            }
        }
    }
}
