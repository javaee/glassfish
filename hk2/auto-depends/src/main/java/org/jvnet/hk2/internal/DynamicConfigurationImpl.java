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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.MultiException;

/**
 * @author jwells
 *
 */
public class DynamicConfigurationImpl implements DynamicConfiguration {
    private final ServiceLocatorImpl locator;
    private final LinkedList<SystemDescriptor<?>> allDescriptors = new LinkedList<SystemDescriptor<?>>();
    private final HashMap<String, HK2Loader> allLoaders = new HashMap<String, HK2Loader>();
    private final HashMap<Class<? extends Annotation>, InjectionResolver> allResolvers =
            new HashMap<Class<? extends Annotation>, InjectionResolver>();
    private final HashSet<Context> allContexts = new HashSet<Context>();
    
    private final Object lock = new Object();
    private boolean committed = false;
    private boolean commitable = true;

    /* package */ DynamicConfigurationImpl(ServiceLocatorImpl locator) {
        this.locator = locator;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#bind(org.glassfish.hk2.api.Descriptor)
     */
    @Override
    public ActiveDescriptor<?> bind(Descriptor key) {
        checkState();
        if ((key == null) || (key.getImplementation() == null)) throw new IllegalArgumentException();
        
        SystemDescriptor<?> sd = new SystemDescriptor<Object>(key, new Long(locator.getLocatorId()));
        
        allDescriptors.add(sd);
        
        return sd;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#addLoader(java.lang.String, org.glassfish.hk2.api.HK2Loader)
     */
    @Override
    public void addLoader(HK2Loader loader) {
        checkState();
        if (loader == null || loader.getLoaderName() == null) {
            throw new IllegalArgumentException();
        }
        
        allLoaders.put(loader.getLoaderName(), loader);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#addInjectionResolver(java.lang.Class, org.glassfish.hk2.api.InjectionResolver)
     */
    @Override
    public void addInjectionResolver(Class<? extends Annotation> indicator,
            InjectionResolver resolver) {
        checkState();
        if (indicator == null || resolver == null) {
            throw new IllegalArgumentException();
        }
        
        allResolvers.put(indicator, resolver);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#addContext(org.glassfish.hk2.api.Context)
     */
    @Override
    public void addContext(Context context) {
        checkState();
        if (context == null) {
            throw new IllegalArgumentException();
        }
        
        Utilities.checkScopeAnnotation(context.getScope());
        
        allContexts.add(context);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#addActiveDescriptor(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public ActiveDescriptor<?> addActiveDescriptor(ActiveDescriptor<?> activeDescriptor)
            throws IllegalArgumentException {
        checkState();
        if (activeDescriptor == null || !activeDescriptor.isReified()) {
            throw new IllegalArgumentException();
        }
        
        SystemDescriptor<?> retVal = new SystemDescriptor<Object>(activeDescriptor,
                new Long(locator.getLocatorId()));
        
        allDescriptors.add(retVal);
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.DynamicConfiguration#commit()
     */
    @Override
    public void commit() throws MultiException {
        synchronized (lock) {
            checkState();
            if (!commitable) throw new IllegalStateException();
            
            committed = true;
        }
        
        locator.addConfiguration(this);
    }
    
    private void checkState() {
        synchronized (lock) {
            if (committed) throw new IllegalStateException();
        }
    }

    /**
     * @return the allDescriptors
     */
    LinkedList<SystemDescriptor<?>> getAllDescriptors() {
        return allDescriptors;
    }

    /**
     * @return the allLoaders
     */
    HashMap<String, HK2Loader> getAllLoaders() {
        return allLoaders;
    }

    /**
     * @return the allResolvers
     */
    HashMap<Class<? extends Annotation>, InjectionResolver> getAllResolvers() {
        return allResolvers;
    }

    /**
     * @return the allContexts
     */
    HashSet<Context> getAllContexts() {
        return allContexts;
    }
    
    /* package */ void setCommitable(boolean commitable) {
        this.commitable = commitable;
        
    }
}
