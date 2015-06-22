/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.extras.operation;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.extras.operation.internal.OperationHandleImpl;
import org.glassfish.hk2.extras.operation.internal.SingleOperationManager;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.jvnet.hk2.annotations.Contract;

/**
 * The implementation of {@link Context} for an Operation.
 * <p>
 * An operation is defined as a unit of work that can
 * be associated with one or more java threads, but where
 * two operations of the same type may not be associated
 * with the same thread at the same time.  Examples of such
 * an operation might be a RequestScope or a TenantRequesteOperation.
 * An operation is a more general concept than the normal
 * Java EE request scope, since it does not require a Java EE
 * container
 * <p>
 * Users of this API generally create a {@link Scope} annotation
 * and extend this class, implementing the {@link Context#getScope()}
 * and making sure the parameterized type is the Scope annotation.
 * The {@link Scope} annotation for an Operation is usually
 * {@link Proxiable} but does not have to be. As with all implementations
 * of {@link Context} the subclass of this class must be in the {@link Singleton}
 * scope.  The user code then uses the {@link OperationManager} and {@link OperationHandle}
 * to start and stop Operations and to associate and dis-associate
 * threads with Operations
 * <p>
 * Classes extending this class may also choose to override the method
 * {@link Context#supportsNullCreation()} which returns false by default
 * 
 * @author jwells
 */
@Contract
public abstract class OperationContext<T extends Annotation> implements Context<T> {
    private SingleOperationManager<T> manager;
    private final HashMap<OperationHandleImpl<T>, LinkedHashMap<ActiveDescriptor<?>, Object>> operationMap =
            new HashMap<OperationHandleImpl<T>, LinkedHashMap<ActiveDescriptor<?>, Object>>();
    private final HashSet<ActiveDescriptor<?>> creating = new HashSet<ActiveDescriptor<?>>();
    private final HashMap<Long, LinkedList<OperationHandleImpl<T>>> closingOperations = new HashMap<Long, LinkedList<OperationHandleImpl<T>>>();
    private boolean shuttingDown = false;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#findOrCreate(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor,
            ServiceHandle<?> root) {
        SingleOperationManager<T> localManager;
        LinkedList<OperationHandleImpl<T>> closingOperationStack;
        boolean closingOperation;
        
        synchronized (this) {
            localManager = manager;
            closingOperationStack = closingOperations.get(Thread.currentThread().getId());
            closingOperation = (closingOperationStack != null && !closingOperationStack.isEmpty());
        }
        
        if (localManager == null) {
            throw new IllegalStateException("There is no manager for " +
                getScope().getName() + " on thread " + Thread.currentThread().getId());
        }
        
        OperationHandleImpl<T> operation = localManager.getCurrentOperationOnThisThread();
        if (operation == null) {
            synchronized (this) {
                if (!closingOperation) {
                    throw new IllegalStateException("There is no current operation of type " +
                            getScope().getName() + " on thread " + Thread.currentThread().getId());
                }
                
                operation = closingOperationStack.get(0);
            }
        }
        
        LinkedHashMap<ActiveDescriptor<?>, Object> serviceMap;
        synchronized (this) {
            serviceMap = operationMap.get(operation);
            if (serviceMap == null) {
                if (closingOperation || shuttingDown) {
                    throw new IllegalStateException("The operation " + operation.getIdentifier() +
                            " is closing.  A new instance of " + activeDescriptor +
                            " cannot be created");
                }
                
                serviceMap = new LinkedHashMap<ActiveDescriptor<?>, Object>();
                operationMap.put(operation, serviceMap);
            }
            
            Object retVal = serviceMap.get(activeDescriptor);
            if (retVal != null) return (U) retVal;
            
            if (supportsNullCreation() && serviceMap.containsKey(activeDescriptor)) {
                return null;
            }
            
            if (closingOperation || shuttingDown) {
                throw new IllegalStateException("The operation " + operation.getIdentifier() +
                        " is closing.  A new instance of " + activeDescriptor +
                        " cannot be created after searching existing descriptors");
            }
            
            // retVal is null, and this is not an explicit null, so must actually do the creation
            while (creating.contains(activeDescriptor)) {
                try {
                    this.wait();
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            
            retVal = serviceMap.get(activeDescriptor);
            if (retVal != null) return (U) retVal;
            
            if (supportsNullCreation() && serviceMap.containsKey(activeDescriptor)) {
                return null;
            }
            
            // Not in creating, and not created.  Create it ourselves
            creating.add(activeDescriptor);
        }
        
        Object retVal = null;
        boolean success = false;
        try {
            retVal = activeDescriptor.create(root);
            if (retVal == null && !supportsNullCreation()) {
                throw new IllegalArgumentException("The operation for context " + getScope().getName() +
                        " does not support null creation, but descriptor " + activeDescriptor + " returned null");
            }
            
            success = true;
        }
        finally {
            synchronized (this) {
                if (success) {
                    serviceMap.put(activeDescriptor, retVal);
                }
                
                creating.remove(activeDescriptor);
                this.notifyAll();
            }
        }
        
        return (U) retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#containsKey(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        SingleOperationManager<T> localManager;
        synchronized (this) {
            localManager = manager;
        }
        if (localManager == null) return false;
        
        OperationHandleImpl<T> operation = localManager.getCurrentOperationOnThisThread();
        if (operation == null) return false;
        
        synchronized (this) {
            HashMap<ActiveDescriptor<?>, Object> serviceMap;
            
            serviceMap = operationMap.get(operation);
            if (serviceMap == null) return false;
            
            return serviceMap.containsKey(descriptor);
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#destroyOne(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        synchronized (this) {
            for (HashMap<ActiveDescriptor<?>, Object> serviceMap : operationMap.values()) {
                Object killMe = serviceMap.remove(descriptor);
                if (killMe == null) continue;
                
                ((ActiveDescriptor<Object>) descriptor).dispose(killMe);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public void closeOperation(OperationHandleImpl<T> operation) {
        long tid = Thread.currentThread().getId();
        HashMap<ActiveDescriptor<?>, Object> serviceMap;
        LinkedList<OperationHandleImpl<T>> stack;
        
        synchronized (this) {
            stack = closingOperations.get(tid);
            if (stack == null) {
                stack = new LinkedList<OperationHandleImpl<T>>();
                closingOperations.put(tid, stack);
            }
            
            stack.addFirst(operation);
            
            serviceMap = operationMap.get(operation);
        }
        
        try {
            // Must be done outside of the lock
            
            if (serviceMap == null) return;
        
            // Reverses creation order
            LinkedList<Map.Entry<ActiveDescriptor<?>, Object>> destructionList = new LinkedList<Map.Entry<ActiveDescriptor<?>, Object>>();
            for (Map.Entry<ActiveDescriptor<?>, Object> entry : serviceMap.entrySet()) {
                destructionList.addFirst(entry);
            }
            
            for (Map.Entry<ActiveDescriptor<?>, Object> entry : destructionList) {
                ActiveDescriptor<Object> desc = (ActiveDescriptor<Object>) entry.getKey();
                Object value = entry.getValue();
            
                try {
                    desc.dispose(value);
                }
                catch (Throwable th) {
                    Logger.getLogger().debug(getClass().getName(), "closeOperation", th);
                }
            }
        }
        finally {
            synchronized (this) {
                operationMap.remove(operation);
            
                stack.removeFirst();
                if (stack.isEmpty()) {
                    closingOperations.remove(tid);
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#shutdown()
     */
    @Override
    public void shutdown() {
        Set<OperationHandleImpl<T>> toShutDown;
        synchronized (this) {
            shuttingDown = true;
            toShutDown = operationMap.keySet();
        }
        
        try {
            for (OperationHandleImpl<T> shutDown : toShutDown) {
                shutDown.closeOperation();
            }
        }
        finally {
            synchronized (this) {
                operationMap.clear();
            }
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#supportsNullCreation()
     */
    @Override
    public boolean supportsNullCreation() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#isActive()
     */
    @Override
    public boolean isActive() {
        return true;
    }

    public synchronized void setOperationManager(SingleOperationManager<T> manager) {
        this.manager = manager;
    }
    
    @Override
    public String toString() {
        return "OperationContext(" + getScope().getName() + "," + System.identityHashCode(this) + ")";
    }
}
