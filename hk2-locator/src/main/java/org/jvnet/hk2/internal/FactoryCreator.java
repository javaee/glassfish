/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.internal;

import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstantiationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.general.ThreadSpecificObject;
import org.glassfish.hk2.utilities.reflection.Pretty;

/**
 * @author jwells
 * @param <T> The thing this factory is producing
 */
public class FactoryCreator<T> implements Creator<T> {
    private final static Object MAP_VALUE = new Object();
    private final ConcurrentHashMap<ThreadSpecificObject<ActiveDescriptor<?>>, Object> cycleFinder =
            new ConcurrentHashMap<ThreadSpecificObject<ActiveDescriptor<?>>, Object>();
    
    private final ServiceLocator locator;
    private final ActiveDescriptor<?> factoryDescriptor;
    private final InstantiationServiceImpl instantiationService;
    
    /* package */ FactoryCreator(ServiceLocator locator, ActiveDescriptor<?> factoryDescriptor) {
        this.locator = locator;
        this.factoryDescriptor = factoryDescriptor;
        
        if (!factoryDescriptor.isReified()) {
            factoryDescriptor = locator.reifyDescriptor(factoryDescriptor);
        }
        
        InstantiationServiceImpl found = null;
        for (Injectee factoryInjectee : factoryDescriptor.getInjectees()) {
            if (InstantiationService.class.equals(factoryInjectee.getRequiredType())) {
                found = locator.getService(InstantiationServiceImpl.class);
                break;
            }
        }
        
        // Will ONLY be non-null if the factory has injected the InstantiationService
        instantiationService = found;
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#getInjectees()
     */
    @Override
    public List<Injectee> getInjectees() {
        return Collections.emptyList();
    }
    
    @SuppressWarnings("unchecked")
    private ServiceHandle<Factory<T>> getFactoryHandle() {
        try {
            return (ServiceHandle<Factory<T>>) locator.getServiceHandle(factoryDescriptor);
        }
        catch (Throwable th) {
            throw new MultiException(th);
        }
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public T create(ServiceHandle<?> root, SystemDescriptor<?> eventThrower) throws MultiException {
        ServiceHandle<Factory<T>> handle = getFactoryHandle();
        
        eventThrower.invokeInstanceListeners(new InstanceLifecycleEventImpl(
                InstanceLifecycleEventType.PRE_PRODUCTION, null, eventThrower));
        
        ThreadSpecificObject<ActiveDescriptor<?>> tso = new ThreadSpecificObject<ActiveDescriptor<?>>(handle.getActiveDescriptor());
        
        if (cycleFinder.containsKey(tso)) {
            HashSet<String> impls = new HashSet<String>();
            for (ThreadSpecificObject<ActiveDescriptor<?>> candidate : cycleFinder.keySet()) {
                if (candidate.getThreadIdentifier() != tso.getThreadIdentifier()) continue;
                impls.add(candidate.getIncomingObject().getImplementation());
            }
            
            throw new AssertionError("A cycle was detected involving these Factory implementations: " + Pretty.collection(impls));
        }
        
        cycleFinder.put(tso, MAP_VALUE);
        Factory<T> retValFactory;
        try {
            retValFactory = handle.getService();
        }
        finally {
            cycleFinder.remove(tso);
        }
        
        if (instantiationService != null) {
            Injectee parentInjectee = null;
            if (root != null && (root instanceof ServiceHandleImpl)) {
                parentInjectee = ((ServiceHandleImpl<?>) root).getOriginalRequest();
            }
            
            // Even if it is null
            instantiationService.pushInjecteeParent(parentInjectee);
        }
        
        T retVal;
        try {
            retVal = retValFactory.provide();
        }
        finally {
            if (instantiationService != null) {
                instantiationService.popInjecteeParent();
            }
        }
        
        eventThrower.invokeInstanceListeners(new InstanceLifecycleEventImpl(
                InstanceLifecycleEventType.POST_PRODUCTION, retVal, eventThrower));
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#dispose(java.lang.Object, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public void dispose(T instance) {
        try {
            ServiceHandle<Factory<T>> handle = getFactoryHandle();
            
            Factory<T> factory = handle.getService();
            
            factory.dispose(instance);
        }
        catch (Throwable th) {
            if (th instanceof MultiException) {
                throw (MultiException) th;
            }
            
            throw new MultiException(th);
        }
    }
    
    public String toString() {
        return "FactoryCreator(" + locator + "," + factoryDescriptor + "," + System.identityHashCode(this) + ")";
    }
}
