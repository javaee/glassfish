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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceHandle;

/**
 * @author jwells
 */
public class SystemDescriptor<T> implements ActiveDescriptor<T> {
    private final static Object sLock = new Object();
    private static long currentId = 1L;
    
    private final Descriptor baseDescriptor;
    private final Long id;
    private final ActiveDescriptor<T> activeDescriptor;
    private boolean reified;
    
    private final Object cacheLock = new Object();
    private boolean cacheSet = false;
    private T cachedValue;
    
    // These are used when we are doing the reifying ourselves
    private Class<?> implClass;
    private Class<? extends Annotation> scope;
    private Set<Type> contracts;
    private Set<Annotation> qualifiers;
    private Creator<T> creator;
    
    /* package */ @SuppressWarnings("unchecked")
    SystemDescriptor(Descriptor baseDescriptor) {
        this.baseDescriptor = baseDescriptor;
        
        synchronized (sLock) {
            id = new Long(currentId++);
        }
        
        if (baseDescriptor instanceof ActiveDescriptor) {
            ActiveDescriptor<T> active = (ActiveDescriptor<T>) baseDescriptor;
            if (active.isReified()) {
                activeDescriptor = active;
                reified = true;
            }
            else {
                activeDescriptor = null;
                reified = false;
            }
        }
        else {
            activeDescriptor = null;
            reified = false;
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getImplementation()
     */
    @Override
    public String getImplementation() {
        return baseDescriptor.getImplementation();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getAdvertisedContracts()
     */
    @Override
    public Set<String> getAdvertisedContracts() {
        return baseDescriptor.getAdvertisedContracts();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getScope()
     */
    @Override
    public String getScope() {
        return baseDescriptor.getScope();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getNames()
     */
    @Override
    public String getName() {
        return baseDescriptor.getName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getQualifiers()
     */
    @Override
    public Set<String> getQualifiers() {
        return baseDescriptor.getQualifiers();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getMetadata()
     */
    @Override
    public Map<String, List<String>> getMetadata() {
        return baseDescriptor.getMetadata();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getRanking()
     */
    @Override
    public int getRanking() {
        return baseDescriptor.getRanking();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getServiceId()
     */
    @Override
    public Long getServiceId() {
        return id;
    }
    
    

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#getCache()
     */
    @Override
    public T getCache() {
        return cachedValue;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#isCacheSet()
     */
    @Override
    public boolean isCacheSet() {
        return cacheSet;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#setCache(java.lang.Object)
     */
    @Override
    public void setCache(T cacheMe) {
        synchronized (cacheLock) {
            cachedValue = cacheMe;
            cacheSet = true;
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.SingleCache#releaseCache()
     */
    @Override
    public void releaseCache() {
        synchronized (cacheLock) {
            cacheSet = false;
            cachedValue = null;
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#isReified()
     */
    @Override
    public boolean isReified() {
        return reified;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
     */
    @Override
    public Class<?> getImplementationClass() {
        checkState();
        
        if (activeDescriptor != null) {
            return activeDescriptor.getImplementationClass();
        }
        
        return implClass;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getContractTypes()
     */
    @Override
    public Set<Type> getContractTypes() {
        checkState();
        
        if (activeDescriptor != null) {
            return activeDescriptor.getContractTypes();
        }
        
        return contracts;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getScopeAnnotation()
     */
    @Override
    public Class<? extends Annotation> getScopeAnnotation() {
        checkState();
        
        if (activeDescriptor != null) {
            return activeDescriptor.getScopeAnnotation();
        }
        
        return scope;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getQualifierAnnotations()
     */
    @Override
    public Set<Annotation> getQualifierAnnotations() {
        checkState();
        
        if (activeDescriptor != null) {
            return activeDescriptor.getQualifierAnnotations();
        }
        
        return qualifiers;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getInjectees()
     */
    @Override
    public List<Injectee> getInjectees() {
        checkState();
        
        if (activeDescriptor != null) {
            return activeDescriptor.getInjectees();
        }
        
        return creator.getInjectees();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public T create(ServiceHandle<?> root) {
        checkState();
        
        if (activeDescriptor != null) {
            return activeDescriptor.create(root);
        }
        
        return creator.create(root);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#dispose(java.lang.Object, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public void dispose(T instance, ServiceHandle<?> root) {
        checkState();
        
        if (activeDescriptor != null) {
            activeDescriptor.dispose(instance, root);
            return;
        }
        
        creator.dispose(instance, root);
    }
    
    private void checkState() {
        if (!reified) throw new IllegalStateException();
    }
    
    /* package */ void reify(Class<?> implClass, ServiceLocatorImpl locator, MultiException collector) {
        this.implClass = implClass;
        
        if (!Factory.class.isAssignableFrom(implClass) || getAdvertisedContracts().contains(Factory.class.getName())) {
            creator = new ClazzCreator<T>(locator, implClass, collector);
            
            scope = Utilities.getScopeAnnotationType(implClass, collector);
            contracts = Collections.unmodifiableSet(Utilities.getTypeClosure(implClass, baseDescriptor.getAdvertisedContracts()));
            qualifiers = Collections.unmodifiableSet(Utilities.getAllQualifiers(implClass, baseDescriptor.getName(), collector));
        }
        else {
            creator = new FactoryCreator<T>(locator, implClass);
            
            Method provideMethod = Utilities.getFactoryProvideMethod(implClass);
            
            scope = Utilities.getScopeAnnotationType(provideMethod, collector);
            
            Type factoryProvidedType = provideMethod.getGenericReturnType();
            contracts = Collections.unmodifiableSet(Utilities.getTypeClosure(factoryProvidedType, baseDescriptor.getAdvertisedContracts()));
            qualifiers = Collections.unmodifiableSet(Utilities.getAllQualifiers(provideMethod, baseDescriptor.getName(), collector));
        }
        
        // Check the scope
        if ((baseDescriptor.getScope() == null) && (scope == null)) {
            scope = PerLookup.class;
        }
        
        if (baseDescriptor.getScope() != null && scope != null) {
            String scopeName = scope.getName();
            
            if (!scopeName.equals(baseDescriptor.getScope())) {
                collector.addThrowable(new IllegalArgumentException("The scope name given in the descriptor (" +
                        baseDescriptor.getScope() + ") did not match the scope annotation on the class (" + scope.getName() + ")"));
                
            }
            
        }
        
        reified = true;
    }
    
    @Override
    public int hashCode() {
        int low32 = id.intValue();
        int high32 = (int) (id.longValue() >> 32);
        
        return baseDescriptor.hashCode() ^ low32 ^ high32;
    }
    
    @SuppressWarnings({ "rawtypes" })
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof SystemDescriptor)) return false;
        
        SystemDescriptor sd = (SystemDescriptor) o;
        
        if (!sd.getServiceId().equals(id)) return false;
        
        return sd.baseDescriptor.equals(baseDescriptor);
    }
    
    public String toString() {
        return "SystemDescriptor(" + getImplementation() + "," + Pretty.collection(getAdvertisedContracts()) + "," +
          Pretty.collection(getQualifiers()) + "," + System.identityHashCode(this) + ")";
    }
}
