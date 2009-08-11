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
package com.sun.enterprise.resource.pool.datastructure;

import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.pool.ResourceHandler;
import com.sun.enterprise.resource.pool.datastructure.strategy.ResourceSelectionStrategy;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * List based datastructure that can be used by connection pool <br>
 *
 * @author Jagadish Ramu
 */
public class ListDataStructure implements DataStructure {
    private final ArrayList<ResourceHandle> free;
    private final ArrayList<ResourceHandle> resources;
    private int maxSize;
    private ResourceHandler handler;
    private ResourceSelectionStrategy strategy;
    private Semaphore semaphore;

    public ListDataStructure(String parameters, int maxSize, ResourceHandler handler, String strategyClass) {
        resources = new ArrayList<ResourceHandle>((maxSize > 1000) ? 1000 : maxSize);
        free = new ArrayList<ResourceHandle>((maxSize > 1000) ? 1000 : maxSize);
        this.maxSize = maxSize;
        this.handler = handler;
        initializeStrategy(strategyClass);
        semaphore = new Semaphore(this.maxSize);
    }

    private void initializeStrategy(String strategyClass) {
        //TODO V3 handle later
    }

    /**
     * creates a new resource and adds to the datastructure.
     *
     * @param allocator ResourceAllocator
     * @param count     Number (units) of resources to create
     */
    public void addResource(ResourceAllocator allocator, int count) throws PoolingException {
        for (int i = 0; i < count && resources.size() < maxSize; i++) {
            boolean lockAcquired = semaphore.tryAcquire();
            if(lockAcquired) {
                try {
                    ResourceHandle handle = handler.createResource(allocator);
                    synchronized (resources) {
                        synchronized (free) {
                            free.add(handle);
                            resources.add(handle);
                        }
                    }
                } catch (Exception e) {
                    semaphore.release();
                    PoolingException pe = new PoolingException(e.getMessage());
                    pe.initCause(e);
                    throw pe;
                }
            }
        }
    }

    /**
     * get a resource from the datastructure
     *
     * @return ResourceHandle
     */
    public ResourceHandle getResource() {
        ResourceHandle resource = null;
        if (strategy != null) {
            resource = strategy.retrieveResource();
        } else {
            synchronized (free) {
                if (free.size() > 0){
                    resource = free.remove(0);
                }
            }
        }
        return resource;
    }

    /**
     * remove the specified resource from the datastructure
     *
     * @param resource ResourceHandle
     */
    public void removeResource(ResourceHandle resource) {
        synchronized (resources) {
            synchronized (free) {
                free.remove(resource);
                resources.remove(resource);
            }
        }
        semaphore.release();
        handler.deleteResource(resource);
    }

    /**
     * returns the resource to the datastructure
     *
     * @param resource ResourceHandle
     */
    public void returnResource(ResourceHandle resource) {
        synchronized (free) {
            free.add(resource);
        }
    }

    /**
     * get the count of free resources in the datastructure
     *
     * @return int count
     */
    public int getFreeListSize() {
        return free.size();
    }

    /**
     * remove & destroy all resources from the datastructure.
     */
    public void removeAll() {
        synchronized (resources) {
            synchronized (free) {
                while (resources.size() > 0) {
                    ResourceHandle handle = resources.remove(0);
                    free.remove(handle);
                    semaphore.release();
                    handler.deleteResource(handle);
                }
            }
        }
        free.clear();
        resources.clear();
    }

    /**
     * get total number of resources in the datastructure
     *
     * @return int count
     */
    public int getResourcesSize() {
        return resources.size();
    }
}
