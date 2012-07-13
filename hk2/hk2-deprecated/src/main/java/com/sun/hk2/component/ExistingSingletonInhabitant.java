/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.hk2.component;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

/**
 * {@link org.jvnet.hk2.component.Inhabitant} built around an object that already exists.
 *
 * @author Kohsuke Kawaguchi
 */
@Deprecated
public class ExistingSingletonInhabitant<T> extends AbstractInhabitantImpl<T> implements ActiveDescriptor<T> {
    /**
     * 
     */
    private static final long serialVersionUID = 7658190710958938371L;
    
    private final T object;
    private final Class<T> type;

    public ExistingSingletonInhabitant(T object) {
        this((Class<T>) object.getClass(), object);
    }

    public ExistingSingletonInhabitant(Class<T> type, T object) {
        super(BuilderHelper.createConstantDescriptor(object));
        this.object = object;
        this.type = (Class<T>) object.getClass();
    }

    public ExistingSingletonInhabitant(Class<T> type, T object, Map<String, List<String>> metadata) {
        this(type, object);
        super.getDescriptor().getMetadata().putAll(metadata);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "-" + System.identityHashCode(this) + 
          "(value=" + get(null) + ", " + getDescriptor() + ")\n";
    }

    @Override
    public String typeName() {
        return type.getName();
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public Map<String, List<String>> metadata() {
        return (Map<String, List<String>>) getDescriptor().getMetadata();
    }

    public T get(Inhabitant onBehalfOf) throws ComponentException {
        return object;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void release() {
        // since we are working on the existing object,
        // we can't release its instance.
        // (technically it's possible to invoke PreDestroy,
        // not clear which is better --- you can argue both ways.)
    }
    
    private boolean isCacheSet = false;
    private T cache;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#getCache()
     */
    @Override
    public T getCache() {
        return cache;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#isCacheSet()
     */
    @Override
    public boolean isCacheSet() {
        return isCacheSet;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#setCache(java.lang.Object)
     */
    @Override
    public void setCache(T cacheMe) {
        cache = cacheMe;
        isCacheSet = true;
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#releaseCache()
     */
    @Override
    public void releaseCache() {
        isCacheSet = false;
        cache = null;
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#isReified()
     */
    @Override
    public boolean isReified() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
     */
    @Override
    public Class<?> getImplementationClass() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getContractTypes()
     */
    @Override
    public Set<Type> getContractTypes() {
        Set<Type> retVal = Collections.singleton((Type) type);
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getScopeAnnotation()
     */
    @Override
    public Class<? extends Annotation> getScopeAnnotation() {
        if (getScope() == null || PerLookup.class.getName().equals(getScope())) {
            return PerLookup.class;
        }
        
        return Singleton.class;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getQualifierAnnotations()
     */
    @Override
    public Set<Annotation> getQualifierAnnotations() {
        return Collections.emptySet();
    }
    
    public Long getFactoryServiceId() {
        return null;
    }
    
    public Long getFactoryLocatorId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getInjectees()
     */
    @Override
    public List<Injectee> getInjectees() {
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public T create(ServiceHandle<?> root) {
        return object;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#dispose(java.lang.Object)
     */
    @Override
    public void dispose(T instance) {
        
    }
}
