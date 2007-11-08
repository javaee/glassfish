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

import javax.transaction.Transaction;

/**
 * this resource pool does not allow sharing
 * A resource is only given out if it is not used by
 * any enterprise bean and it does not have any pending transaction
 *
 * @author Aditya Gore
 */
public class AssocWithThreadResourcePool extends AbstractResourcePool {
    
    private static ThreadLocal<ResourceHandle> localResource =
        new ThreadLocal<ResourceHandle>();

    public AssocWithThreadResourcePool( String poolName ) 
        throws PoolingException{
        super( poolName );
    }
    
    protected ResourceHandle prefetch( ResourceSpec spec,
        ResourceAllocator alloc, Transaction tran) 
    {
        ResourceHandle ar = localResource.get();
        if (ar != null) {
            //synch on ar and do a quick-n-dirty check to see if the local
            //resource is usable at all
            synchronized( ar.lock ) {
                if ( (ar.getThreadId() != Thread.currentThread().getId()) ||
                        ar.hasConnectionErrorOccurred() || 
                        ar.isDirty() || !ar.isAssociated() ) {
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
                        if (! alloc.matchConnection( ar ) ) {
                            //again, since the credentials of the caller don't match
                            //evict from ThreadLocal
                            //also, mark the resource as unassociated and make this resource
                            //potentially usable
                            localResource.remove();
                            ar.setAssociated( false );
                            if ( monitoringEnabled ) {
                                poolCounters.incrementNumConnNotSuccessfullyMatched();
                            }
                            return null;
                        }
                        if (monitoringEnabled) {
                            poolCounters.incrementNumConnSuccessfullyMatched();
                        }
                    }
            
                    
                    setResourceStateToBusy(ar);
		            if(maxConnectionUsage_ > 0){
                        ar.incrementUsageCount();
                    }
                    return ar;
                }
            }
        }

        return null;
    }

    private void setInThreadLocal(ResourceHandle h) {
        if (h != null) {
            synchronized (h.lock) {
                h.setThreadId(Thread.currentThread().getId());
                h.setAssociated(true);
                localResource.set(h);
            }
        }
    }

    protected boolean isResourceUnused(ResourceHandle h){
        return h.getResourceState().isFree() && !h.isAssociated() ;
    }


    // this is the RI getResource() with some modifications
    /**
     * return resource in free list. If none is found, returns null
     *
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
                for (ResourceHandle resource : resources) {
                    synchronized (resource.lock) {
                        //though we are checking resources from within the free list,
                        //we could have a situation where the resource was free upto
                        //this point, put just before we entered the synchronized block,
                        //the resource "h" got used by the thread that was associating it
                        //so we need to check for isFree also

                        if (resource.getResourceState().isUnenlisted() &&
                                resource.getResourceState().isFree()) {
                            if (!matchConnection(resource, alloc)) {
                                continue;
                            }

                            if (resource.hasConnectionErrorOccurred()) {
                                continue;
                            }
                            result = resource;
                            setResourceStateToBusy(result);
                            result.setAssociated(false);

                            break;
                        }
                    }
                }
            }
        }

        if (localResource.get() == null) {
            setInThreadLocal(result);
        }

        return result;
    }

    protected synchronized void freeUnenlistedResource(ResourceHandle h) {
        if ( ! h.isAssociated() ) {
            free.add( h );
        }
        //update monitoring data
        if(monitoringEnabled){
            poolCounters.decrementNumConnUsed(false);
        }
        
        if (maxConnectionUsage_ > 0) {
            performMaxConnectionUsageOperation(h);
        }
        notifyWaitingThreads();
    }

    protected void destroyResource(ResourceHandle resourceHandle) {
        try {
            super.destroyResource( resourceHandle );
        } finally { 
            //Note: here we are using the connectionErrorOccurred flag to indicate
            //that this resource is no longer usable. This flag would be checked while
            //getting from ThreadLocal
            //The main intention of marking this is to handle the case where 
            //failAllConnections happens
            //Note that setDirty only happens here - i.e during destroying of a 
            //resource
            
            synchronized( resourceHandle.lock ) {
                resourceHandle.setDirty();
            }
        }
    }

}
