/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.internal;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.configuration.api.ConfiguredBy;

/**
 * @author jwells
 *
 */
@Singleton
@Visibility(DescriptorVisibility.LOCAL)
public class ConfiguredByContext implements Context<ConfiguredBy> {
    private final static ThreadLocal<ActiveDescriptor<?>> workingOn = new ThreadLocal<ActiveDescriptor<?>>() {
        public ActiveDescriptor<?> initialValue() {
            return null;
        }
        
    };
    
    private final Object lock = new Object();
    private final HashMap<ActiveDescriptor<?>, Object> db = new HashMap<ActiveDescriptor<?>, Object>();

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#getScope()
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return ConfiguredBy.class;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#findOrCreate(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor,
            ServiceHandle<?> root) {
        ActiveDescriptor<U> previousValue = (ActiveDescriptor<U>) workingOn.get();
        workingOn.set(activeDescriptor);
        try {
            return internalFindOrCreate(activeDescriptor, root);
        }
        finally {
            workingOn.set(previousValue);
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#findOrCreate(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
     */
    @SuppressWarnings("unchecked")
    private <U> U internalFindOrCreate(ActiveDescriptor<U> activeDescriptor,
            ServiceHandle<?> root) {
        synchronized (lock) {
            U retVal = (U) db.get(activeDescriptor);
            if (retVal != null) return retVal;
            
            if (activeDescriptor.getName() == null) {
                throw new MultiException(new IllegalStateException("ConfiguredBy services without names are templates and cannot be created directly"));
            }
            
            retVal = activeDescriptor.create(root);
            db.put(activeDescriptor, retVal);
            
            return retVal;
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#containsKey(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        synchronized (lock) {
            return db.containsKey(descriptor);
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#destroyOne(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        synchronized (lock) {
            Object destroyMe = db.remove(descriptor);
            if (destroyMe == null) return;
            
            ((ActiveDescriptor<Object>) descriptor).dispose(destroyMe);
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
    
    @Override
    public boolean isPermanentlyActive() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#shutdown()
     */
    @Override
    public void shutdown() {
        synchronized (lock) {
            for (ActiveDescriptor<?> killMe : db.keySet()) {
                destroyOne(killMe);
            }
        }
        
    }
    
    /* package */ ActiveDescriptor<?> getWorkingOn() {
        return workingOn.get();
    }
    
    /* package */ Object findOnly(ActiveDescriptor<?> descriptor) {
        synchronized (lock) {
            return db.get(descriptor);
        }
    }

}
