/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.NamedImpl;

/**
 * @author jwells
 *
 */
public class DelegatingNamedActiveDescriptor implements
        ActiveDescriptor<Object> {
    private final ActiveDescriptor<?> parent;
    private final Named name;
    private final HashSet<String> qualifierNames;
    private final HashSet<Annotation> qualifiers;
    
    /* package */ DelegatingNamedActiveDescriptor(ActiveDescriptor<?> parent, String name) {
        this.parent = parent;
        this.name = new NamedImpl(name);
        
        qualifierNames = new HashSet<String>(parent.getQualifiers());
        qualifierNames.add(Named.class.getName());
        
        qualifiers = new HashSet<Annotation>(parent.getQualifierAnnotations());
        qualifiers.add(this.name);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getImplementation()
     */
    @Override
    public String getImplementation() {
        return parent.getImplementation();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getAdvertisedContracts()
     */
    @Override
    public Set<String> getAdvertisedContracts() {
        return parent.getAdvertisedContracts();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getScope()
     */
    @Override
    public String getScope() {
        return parent.getScope();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getName()
     */
    @Override
    public String getName() {
        return name.value();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getQualifiers()
     */
    @Override
    public Set<String> getQualifiers() {
        return qualifierNames;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getDescriptorType()
     */
    @Override
    public DescriptorType getDescriptorType() {
        return parent.getDescriptorType();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getDescriptorVisibility()
     */
    @Override
    public DescriptorVisibility getDescriptorVisibility() {
        return parent.getDescriptorVisibility();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getMetadata()
     */
    @Override
    public Map<String, List<String>> getMetadata() {
        return parent.getMetadata();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getLoader()
     */
    @Override
    public HK2Loader getLoader() {
        return parent.getLoader();
    }
    
    private int ranking = 0;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getRanking()
     */
    @Override
    public int getRanking() {
        return ranking;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#setRanking(int)
     */
    @Override
    public int setRanking(int ranking) {
        int retVal = ranking;
        this.ranking = ranking;
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#isProxiable()
     */
    @Override
    public Boolean isProxiable() {
        return parent.isProxiable();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#isProxyForSameScope()
     */
    @Override
    public Boolean isProxyForSameScope() {
        return parent.isProxyForSameScope();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getClassAnalysisName()
     */
    @Override
    public String getClassAnalysisName() {
        return parent.getClassAnalysisName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getServiceId()
     */
    @Override
    public Long getServiceId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getLocatorId()
     */
    @Override
    public Long getLocatorId() {
        return null;
    }
    
    private Object lock = new Object();
    private Object cache;
    private boolean isSet = false;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#getCache()
     */
    @Override
    public Object getCache() {
        synchronized (lock) {
            return cache;
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#isCacheSet()
     */
    @Override
    public boolean isCacheSet() {
        synchronized (lock) {
            return isSet;
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#setCache(java.lang.Object)
     */
    @Override
    public void setCache(Object cacheMe) {
        synchronized (lock) {
            isSet = true;
            cache = cacheMe;
        }

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#releaseCache()
     */
    @Override
    public void releaseCache() {
        synchronized (lock) {
            cache = null;
            isSet = false;
        }

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#isReified()
     */
    @Override
    public boolean isReified() {
        // But had BETTER be true
        return parent.isReified();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
     */
    @Override
    public Class<?> getImplementationClass() {
        return parent.getImplementationClass();
    }

    @Override
    public Type getImplementationType() {
        return parent.getImplementationType();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getContractTypes()
     */
    @Override
    public Set<Type> getContractTypes() {
        return parent.getContractTypes();
    }
    
    @Override
    public Annotation getScopeAsAnnotation() {
        return parent.getScopeAsAnnotation();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getScopeAnnotation()
     */
    @Override
    public Class<? extends Annotation> getScopeAnnotation() {
        return parent.getScopeAnnotation();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getQualifierAnnotations()
     */
    @Override
    public Set<Annotation> getQualifierAnnotations() {
        return qualifiers;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getInjectees()
     */
    @Override
    public List<Injectee> getInjectees() {
        return parent.getInjectees();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getFactoryServiceId()
     */
    @Override
    public Long getFactoryServiceId() {
        return parent.getFactoryServiceId();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getFactoryLocatorId()
     */
    @Override
    public Long getFactoryLocatorId() {
        return parent.getFactoryLocatorId();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public Object create(ServiceHandle<?> root) {
        return parent.create(root);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#dispose(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void dispose(Object instance) {
        ((ActiveDescriptor<Object>) parent).dispose(instance);

    }

}
