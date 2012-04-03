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
package org.glassfish.hk2.utilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.internal.ReflectionHelper;

/**
 * This class can be used as a starting point for those writing their own
 * ActiveDescriptor.  Most of the methods are implemented, except the create
 * and destroy
 * 
 * @author jwells
 * @param <T> The type returned from the cache and other methods
 */
public abstract class AbstractActiveDescriptor<T> implements ActiveDescriptor<T> {
    private final Set<Type> advertisedContracts;
    private final Set<String> contractsAsStrings = new HashSet<String>();
    private final Class<? extends Annotation> scope;
    private final String name;
    private final Set<Annotation> qualifiers;
    private final Set<String> qualifiersAsStrings = new HashSet<String>();
    private final DescriptorType descriptorType;
    private int ranking;
    
    /**
     * This constructor must be called with the information about
     * this descriptor
     * 
     * @param advertisedContracts The contracts that should be
     * advertised by this descriptor (may not be null, but may be
     * empty)
     * @param scope The scope of this descriptor (may not be null)
     * @param name The name of this descriptor (may be null)
     * @param qualifiers The qualifiers of this descriptor (may not
     * be null, but may be empty)
     * @param ranking The ranking for this descriptor
     * @param locatorId The id of the locator for this descriptor
     */
    protected AbstractActiveDescriptor(
            Set<Type> advertisedContracts,
            Class<? extends Annotation> scope,
            String name,
            Set<Annotation> qualifiers,
            DescriptorType descriptorType,
            int ranking) {
        this.scope = scope;
        this.advertisedContracts = Collections.unmodifiableSet(advertisedContracts);
        this.qualifiers = Collections.unmodifiableSet(qualifiers);
        this.ranking = ranking;
        this.descriptorType = descriptorType;
        this.name = name;
        
        for (Type t : advertisedContracts) {
            Class<?> raw = ReflectionHelper.getRawClass(t);
            if (raw == null) continue;
            
            contractsAsStrings.add(raw.getName());
        }
        
        for (Annotation q : qualifiers) {
            qualifiersAsStrings.add(q.annotationType().getName());
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getAdvertisedContracts()
     */
    @Override
    public Set<String> getAdvertisedContracts() {
        return Collections.unmodifiableSet(contractsAsStrings);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getScope()
     */
    @Override
    public String getScope() {
        return scope.getName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getNames()
     */
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Long getLocatorId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getQualifiers()
     */
    @Override
    public Set<String> getQualifiers() {
        return Collections.unmodifiableSet(qualifiersAsStrings);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getDescriptorType()
     */
    public DescriptorType getDescriptorType() {
        return descriptorType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getMetadata()
     */
    @Override
    public Map<String, List<String>> getMetadata() {
        return Collections.emptyMap();
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getLoader()
     */
    @Override
    public HK2Loader getLoader() {
        return null;
    }

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
        int retVal = this.ranking;
        this.ranking = ranking;
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getServiceId()
     */
    @Override
    public Long getServiceId() {
        // Set by system, no need to have it here
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#getCache()
     */
    @Override
    public T getCache() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#isCacheSet()
     */
    @Override
    public boolean isCacheSet() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#setCache(java.lang.Object)
     */
    @Override
    public void setCache(T cacheMe) {
        // Does nothing
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#releaseCache()
     */
    @Override
    public void releaseCache() {
        // Does nothing
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#isReified()
     */
    @Override
    public boolean isReified() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getContractTypes()
     */
    @Override
    public Set<Type> getContractTypes() {
        return advertisedContracts;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getScopeAnnotation()
     */
    @Override
    public Class<? extends Annotation> getScopeAnnotation() {
        return scope;
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
        return Collections.emptyList();
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#dispose(java.lang.Object, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public void dispose(T instance) {

    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getBaseDescriptor()
     */
    public Descriptor getBaseDescriptor() {
        return null;
    }
    
    
}

