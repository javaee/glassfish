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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Named;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * This class can be used as a starting point for those writing their own
 * ActiveDescriptor.  It also has some helper methods to deal with metadata
 * and adding and removing contracts and qualifiers, which can be helpful
 * when customizing the implementation
 *
 * @author jwells
 * @param <T> The type returned from the cache
 */
public abstract class AbstractActiveDescriptor<T> extends DescriptorImpl implements ActiveDescriptor<T> {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 7080312303893604939L;

    private final static Set<Annotation> EMPTY_QUALIFIER_SET = Collections.emptySet();

    private Set<Type> advertisedContracts = new LinkedHashSet<Type>();
    private final Class<? extends Annotation> scope;
    private Set<Annotation> qualifiers;
    private Long factoryServiceId;
    private Long factoryLocatorId;
    private boolean isReified = true;

    private transient boolean cacheSet = false;
    private transient T cachedValue;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock rLock = rwLock.readLock();
    private final Lock wLock = rwLock.writeLock();

    /**
     * For serialization
     */
    public AbstractActiveDescriptor() {
        super();
        scope = null;
    }

    /**
     * Creates a NON reified ActiveDescriptor based on a copy of the given
     * baseDescriptor.  The values from the baseDescriptor will be copied deeply
     *
     * @param baseDescriptor The non-null base descriptor to copy values from
     */
    protected AbstractActiveDescriptor(Descriptor baseDescriptor) {
        super(baseDescriptor);
        isReified = false;
        scope = null;
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
     * @param metadata Metadata to add to this descriptor
     */
    protected AbstractActiveDescriptor(
            Set<Type> advertisedContracts,
            Class<? extends Annotation> scope,
            String name,
            Set<Annotation> qualifiers,
            DescriptorType descriptorType,
            DescriptorVisibility descriptorVisibility,
            int ranking,
            Boolean proxy,
            Boolean proxyForSameScope,
            String analyzerName,
            Map<String, List<String>> metadata) {
        super();

        this.scope = scope;
        this.advertisedContracts.addAll(advertisedContracts);
        if (qualifiers != null && !qualifiers.isEmpty()) {
            this.qualifiers = new LinkedHashSet<Annotation>();
            this.qualifiers.addAll(qualifiers);
        }

        setRanking(ranking);
        setDescriptorType(descriptorType);
        setDescriptorVisibility(descriptorVisibility);
        setName(name);  // This MUST be called after the qualifiers have already been set
        setProxiable(proxy);
        setProxyForSameScope(proxyForSameScope);

        if (scope != null) {
            setScope(scope.getName());
        }

        for (Type t : advertisedContracts) {
            Class<?> raw = ReflectionHelper.getRawClass(t);
            if (raw == null) continue;

            addAdvertisedContract(raw.getName());
        }

        if (qualifiers != null) {
            for (Annotation q : qualifiers) {
                addQualifier(q.annotationType().getName());
            }
        }

        setClassAnalysisName(analyzerName);

        if (metadata == null) return;

        for (Map.Entry<String, List<String>> entry : metadata.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            for (String value : values) {
                addMetadata(key, value);
            }
        }
    }

    private void removeNamedQualifier() {
        try {
            wLock.lock();

            if (qualifiers == null) return;

            for (Annotation qualifier : qualifiers) {
                if (qualifier.annotationType().equals(Named.class)) {
                    removeQualifierAnnotation(qualifier);
                    return;
                }
            }
        } finally {
            wLock.unlock();
        }
    }

    /**
     * Sets the name of this descriptor.  Will remove any existing Named
     * qualifier and add a Named qualifier for this name
     */
    @Override
    public void setName(String name) {
        try {
            wLock.lock();
            super.setName(name);

            removeNamedQualifier();

            if (name == null) return;

            addQualifierAnnotation(new NamedImpl(name));
        } finally {
            wLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#getCache()
     */
    @Override
    public T getCache() {
        try {
            rLock.lock();
            return cachedValue;
        } finally {
            rLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#isCacheSet()
     */
    @Override
    public boolean isCacheSet() {
        try {
            rLock.lock();
            return cacheSet;
        } finally {
            rLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#setCache(java.lang.Object)
     */
    @Override
    public void setCache(T cacheMe) {
        try {
            wLock.lock();
            cachedValue = cacheMe;
            cacheSet = true;
        } finally {
            wLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#releaseCache()
     */
    @Override
    public void releaseCache() {
        try {
            wLock.lock();
            cacheSet = false;
            cachedValue = null;
        } finally {
            wLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#isReified()
     */
    @Override
    public boolean isReified() {
        try {
            rLock.lock();
            return isReified;
        } finally {
            rLock.unlock();
        }
    }

    /**
     * This method is called to change the state of the
     * reification of this descriptor
     *
     * @param reified true if this descriptor should appear reified,
     * false otherwise
     */
    public void setReified(boolean reified) {
        try {
            wLock.lock();
            isReified = reified;
        } finally {
            wLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getContractTypes()
     */
    @Override
    public Set<Type> getContractTypes() {
        try {
            rLock.lock();
            return Collections.unmodifiableSet(advertisedContracts);
        } finally {
            rLock.unlock();
        }
    }

    /**
     * Adds an advertised contract to the set of contracts advertised by this descriptor
     * @param addMe The contract to add.  May not be null
     */
    public void addContractType(Type addMe) {
        try {
            wLock.lock();
            if (addMe == null) {
                return;
            }
            advertisedContracts.add(addMe);
            Class<?> rawClass = ReflectionHelper.getRawClass(addMe);
            if (rawClass == null) {
                return;
            }
            addAdvertisedContract(rawClass.getName());
        } finally {
            wLock.unlock();
        }
    }

    /**
     * Removes an advertised contract from the set of contracts advertised by this descriptor
     * @param removeMe The contract to remove.  May not be null
     * @return true if removeMe was removed from the set
     */
    public boolean removeContractType(Type removeMe) {
        try {
            wLock.lock();
            if (removeMe == null) return false;

            boolean retVal = advertisedContracts.remove(removeMe);

            Class<?> rawClass = ReflectionHelper.getRawClass(removeMe);
            if (rawClass == null) return retVal;

            return removeAdvertisedContract(rawClass.getName());
        } finally {
            wLock.unlock();
        }
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
        try {
            rLock.lock();
            if (qualifiers == null) return EMPTY_QUALIFIER_SET;

            return Collections.unmodifiableSet(qualifiers);
        } finally {
            rLock.unlock();
        }
    }

    /**
     * Adds the given string to the list of qualifiers
     *
     * @param addMe The fully qualified class name of the qualifier to add.  May not be null
     */
    public void addQualifierAnnotation(Annotation addMe) {
        try {
            wLock.lock();
            if (addMe == null) return;
            if (qualifiers == null) qualifiers = new LinkedHashSet<Annotation>();
            qualifiers.add(addMe);
            addQualifier(addMe.annotationType().getName());
        } finally {
            wLock.unlock();
        }
    }

    /**
     * Removes the given qualifier from the list of qualifiers
     *
     * @param removeMe The fully qualifier class name of the qualifier to remove.  May not be null
     * @return true if the given qualifier was removed
     */
    public boolean removeQualifierAnnotation(Annotation removeMe) {
        try {
            wLock.lock();
            if (removeMe == null) return false;
            if (qualifiers == null) return false;

            boolean retVal = qualifiers.remove(removeMe);
            removeQualifier(removeMe.annotationType().getName());

            return retVal;
        } finally {
            wLock.unlock();
        }
    }

    @Override
    public Long getFactoryServiceId() {
        return factoryServiceId;
    }

    @Override
    public Long getFactoryLocatorId() {
        return factoryLocatorId;
    }

    /**
     * Sets the locator and serviceId for the factory.  This
     * descriptor must be of type PROVIDE_METHOD
     *
     * @param locatorId The locatorId of the factory associated with
     * this method
     * @param serviceId The serviceId of the factory associated with
     * this method
     */
    public void setFactoryId(Long locatorId, Long serviceId) {
        if (!getDescriptorType().equals(DescriptorType.PROVIDE_METHOD)) {
            throw new IllegalStateException("The descriptor type must be PROVIDE_METHOD");
        }

        factoryServiceId = serviceId;
        factoryLocatorId = locatorId;
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

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
