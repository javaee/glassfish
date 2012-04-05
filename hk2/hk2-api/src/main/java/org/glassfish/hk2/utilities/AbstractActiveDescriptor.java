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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
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
 * ActiveDescriptor.  It also has some helper methods to deal with metadata
 * and adding and removing contracts and qualifiers, which can be helpful
 * when customizing the implementation
 * 
 * @author jwells
 * @param <T> The type returned from the cache and other methods
 */
public abstract class AbstractActiveDescriptor<T> implements ActiveDescriptor<T>, Serializable {
    /**
     * For serialization 
     */
    private static final long serialVersionUID = 7080312303893604939L;
    
    private Set<Type> advertisedContracts = new HashSet<Type>();
    private Set<String> contractsAsStrings = new HashSet<String>();
    private Class<? extends Annotation> scope;
    private String name;
    private Set<Annotation> qualifiers = new HashSet<Annotation>();
    private Set<String> qualifiersAsStrings = new HashSet<String>();
    private DescriptorType descriptorType;
    private Map<String, List<String>> metadata = new HashMap<String, List<String>>();
    private int ranking;
    
    private transient boolean cacheSet = false;
    private transient T cachedValue;
    
    /**
     * For serialization
     */
    public AbstractActiveDescriptor() {
    }
    
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
        this.advertisedContracts.addAll(advertisedContracts);
        this.qualifiers.addAll(qualifiers);
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
    public synchronized Set<String> getAdvertisedContracts() {
        return Collections.unmodifiableSet(contractsAsStrings);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getScope()
     */
    @Override
    public synchronized String getScope() {
        return scope.getName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getNames()
     */
    @Override
    public synchronized String getName() {
        return name;
    }
    
    @Override
    public synchronized Long getLocatorId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getQualifiers()
     */
    @Override
    public synchronized Set<String> getQualifiers() {
        return Collections.unmodifiableSet(qualifiersAsStrings);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getDescriptorType()
     */
    public synchronized DescriptorType getDescriptorType() {
        return descriptorType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getMetadata()
     */
    @Override
    public synchronized Map<String, List<String>> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
    
    /**
     * Adds a value to the list of values associated with this key
     * 
     * @param key The key to which to add the value.  May not be null
     * @param value The value to add.  May not be null
     */
    public synchronized void addMetadata(String key, String value) {
        ReflectionHelper.addMetadata(metadata, key, value);
    }
    
    /**
     * Removes the given value from the given key
     * 
     * @param key The key of the value to remove.  May not be null
     * @param value The value to remove.  May not be null
     * @return true if the value was removed
     */
    public synchronized boolean removeMetadata(String key, String value) {
        return ReflectionHelper.removeMetadata(metadata, key, value);
    }
    
    /**
     * Removes all the metadata values associated with key
     * 
     * @param key The key of the metadata values to remove
     * @return true if any value was removed
     */
    public synchronized boolean removeAllMetadata(String key) {
        return ReflectionHelper.removeAllMetadata(metadata, key);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getLoader()
     */
    @Override
    public synchronized HK2Loader getLoader() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getRanking()
     */
    @Override
    public synchronized int getRanking() {
        return ranking;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#setRanking(int)
     */
    @Override
    public synchronized int setRanking(int ranking) {
        int retVal = this.ranking;
        this.ranking = ranking;
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getServiceId()
     */
    @Override
    public synchronized Long getServiceId() {
        // Set by system, no need to have it here
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#getCache()
     */
    @Override
    public synchronized T getCache() {
        return cachedValue;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#isCacheSet()
     */
    @Override
    public synchronized boolean isCacheSet() {
        return cacheSet;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#setCache(java.lang.Object)
     */
    @Override
    public synchronized void setCache(T cacheMe) {
        cachedValue = cacheMe;
        cacheSet = true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#releaseCache()
     */
    @Override
    public synchronized void releaseCache() {
        cacheSet = false;
        cachedValue = null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#isReified()
     */
    @Override
    public synchronized boolean isReified() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getContractTypes()
     */
    @Override
    public synchronized Set<Type> getContractTypes() {
        return Collections.unmodifiableSet(advertisedContracts);
    }
    
    /**
     * Adds an advertised contract to the set of contracts advertised by this descriptor
     * @param addMe The contract to add.  May not be null
     */
    public synchronized void addAdvertisedContract(Type addMe) {
        if (addMe == null) return;
        
        advertisedContracts.add(addMe);
        
        Class<?> rawClass = ReflectionHelper.getRawClass(addMe);
        if (rawClass == null) return;
        contractsAsStrings.add(rawClass.getName());
    }
    
    /**
     * Removes an advertised contract from the set of contracts advertised by this descriptor
     * @param removeMe The contract to remove.  May not be null
     * @return true if removeMe was removed from the set
     */
    public synchronized boolean removeAdvertisedContract(Type removeMe) {
        if (removeMe == null) return false;
        
        boolean retVal = advertisedContracts.remove(removeMe);
        
        Class<?> rawClass = ReflectionHelper.getRawClass(removeMe);
        if (rawClass == null) return retVal;
        
        return contractsAsStrings.remove(rawClass.getName());
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getScopeAnnotation()
     */
    @Override
    public synchronized Class<? extends Annotation> getScopeAnnotation() {
        return scope;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getQualifierAnnotations()
     */
    @Override
    public synchronized Set<Annotation> getQualifierAnnotations() {
        return Collections.unmodifiableSet(qualifiers);
    }
    
    /**
     * Adds the given string to the list of qualifiers
     * 
     * @param addMe The fully qualified class name of the qualifier to add.  May not be null
     */
    public synchronized void addQualifier(Annotation addMe) {
        if (addMe == null) return;
        qualifiers.add(addMe);
        qualifiersAsStrings.add(addMe.annotationType().getName());
    }
    
    /**
     * Removes the given qualifier from the list of qualifiers
     * 
     * @param removeMe The fully qualifier class name of the qualifier to remove.  May not be null
     * @return true if the given qualifier was removed
     */
    public synchronized boolean removeQualifier(Annotation removeMe) {
        if (removeMe == null) return false;
        
        boolean retVal = qualifiers.remove(removeMe);
        qualifiersAsStrings.remove(removeMe.annotationType().getName());
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getInjectees()
     */
    @Override
    public synchronized List<Injectee> getInjectees() {
        return Collections.emptyList();
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#dispose(java.lang.Object, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public synchronized void dispose(T instance) {

    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getBaseDescriptor()
     */
    public synchronized Descriptor getBaseDescriptor() {
        return null;
    }
    
    public synchronized String toString() {
        return ReflectionHelper.prettyPrintDescriptor(this);
    }
}

