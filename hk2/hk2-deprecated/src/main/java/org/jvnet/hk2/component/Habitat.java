/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2012 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.component;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.deprecated.utilities.Utilities;
import org.glassfish.hk2.extension.ServiceLocatorGenerator;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.deprecated.internal.QualifierFilter;
import org.jvnet.hk2.external.generator.ServiceLocatorGeneratorImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * A set of templates that constitute a world of objects.
 *
 * @author Kohsuke Kawaguchi
 * @author Jerome Dochez
 */
@Deprecated
@SuppressWarnings({"rawtypes", "unchecked"})
public class Habitat implements ServiceLocator {
    private final static String DEFAULT_NAME = "default";
    private final static ServiceLocatorGenerator GENERATOR = new ServiceLocatorGeneratorImpl();
    
    private final ServiceLocator delegate;

    public Habitat() {
        this(null);
    }

    public Habitat(ServiceLocator delegate) {
        this(null, delegate, null);
    }

    public Habitat(Habitat parent, String name) {
        this(parent, getServiceLocator(name), null);
    }

    Habitat(Habitat parent, ServiceLocator serviceLocator, Boolean concurrency_controls) {
        if (parent != null || concurrency_controls != null) {
            throw new UnsupportedOperationException("<clinit> (" + parent + "," + concurrency_controls + ") in Habitat");
        }
        
        delegate = serviceLocator != null ? serviceLocator : getServiceLocator(DEFAULT_NAME);

        ActiveDescriptor<?> foundDescriptor = delegate.getBestDescriptor(BuilderHelper.createContractFilter(Habitat.class.getName()));
        if (foundDescriptor != null) return;

        // Add this habitat in, so it can be looked up!
        AbstractActiveDescriptor<Habitat> habitatDescriptor = BuilderHelper.createConstantDescriptor(this);
        habitatDescriptor.removeContractType(ServiceLocator.class);

        DynamicConfigurationService dcs = delegate.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.addActiveDescriptor(habitatDescriptor);

        config.commit();
    }

    private static ServiceLocator getServiceLocator(String name) {
        return ServiceLocatorFactory.getInstance().create(name, null, GENERATOR);
    }

    /**
     * Gets all the inhabitants for a spcial contract.
     *
     * FOR COMPATIBILITY REASONS
     *
     * @param type the contract type
     * @param <T> the parameterized type
     * @return
     */
    public <T> Collection<Inhabitant<? extends T>> getInhabitants(Class<T> type) {
        List<ActiveDescriptor<?>> all = delegate.getDescriptors(BuilderHelper.createContractFilter(type.getName()));
        
        LinkedList<Inhabitant<? extends T>> retVal = new LinkedList<Inhabitant<? extends T>>();
        
        for (ActiveDescriptor<?> a : all) {
            Inhabitant<? extends T> addMe = Utilities.getInhabitantFromActiveDescriptor((ActiveDescriptor<? extends T>) a, delegate);
            
            retVal.add(addMe);
        }
        
        return retVal;
    }

    /**
     * Gets all the inhabitants that has the given implementation type name.
     */
    public Collection<Inhabitant<?>> getInhabitantsByType(
            String fullyQualifiedClassName) {
        List<ActiveDescriptor<?>> all = delegate.getDescriptors(BuilderHelper.createContractFilter(fullyQualifiedClassName));
        
        LinkedList<Inhabitant<?>> retVal = new LinkedList<Inhabitant<?>>();
        
        for (ActiveDescriptor<?> a : all) {
            Inhabitant<?> addMe = Utilities.getInhabitantFromActiveDescriptor((ActiveDescriptor<?>) a, delegate);
            
            retVal.add(addMe);
        }
        
        return retVal;
    }

    public Collection<Inhabitant<?>> getInhabitantsByContract(
            String fullyQualifiedClassName) {
        return getInhabitantsByType(fullyQualifiedClassName);
    }

    public Inhabitant getInhabitantByContract(String fullyQualifiedName,
                                              String name) {
        ActiveDescriptor<?> best = delegate.getBestDescriptor(BuilderHelper.createNameAndContractFilter(fullyQualifiedName, name));
        return Utilities.getInhabitantFromActiveDescriptor(best, delegate);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> T getService(Type contractOrImpl, Annotation... qualifiers)
            throws MultiException {
        return (T) delegate.getService(contractOrImpl, qualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(java.lang.reflect.Type, java.lang.String, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> T getService(Type contractOrImpl, String name,
            Annotation... qualifiers) throws MultiException {
        return (T) delegate.getService(contractOrImpl, name, qualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServices(java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> List<T> getAllServices(Type contractOrImpl,
            Annotation... qualifiers) throws MultiException {
        return delegate.getAllServices(contractOrImpl, qualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServices(org.glassfish.hk2.api.Filter)
     */
    @Override
    public List<?> getAllServices(Filter searchCriteria) throws MultiException {
        return delegate.getAllServices(searchCriteria);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl,
            Annotation... qualifiers) throws MultiException {
        return delegate.getServiceHandle(contractOrImpl, qualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(java.lang.reflect.Type, java.lang.String, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl,
            String name, Annotation... qualifiers) throws MultiException {
        return delegate.getServiceHandle(contractOrImpl, name, qualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServiceHandles(java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @Override
    public List<ServiceHandle<?>> getAllServiceHandles(Type contractOrImpl,
            Annotation... qualifiers) throws MultiException {
        return delegate.getAllServiceHandles(contractOrImpl, qualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServiceHandles(org.glassfish.hk2.api.Filter)
     */
    @Override
    public List<ServiceHandle<?>> getAllServiceHandles(Filter searchCriteria)
            throws MultiException {
        return delegate.getAllServiceHandles(searchCriteria);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getDescriptors(org.glassfish.hk2.api.Filter)
     */
    @Override
    public List<ActiveDescriptor<?>> getDescriptors(Filter filter) {
        return delegate.getDescriptors(filter);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getBestDescriptor(org.glassfish.hk2.api.Filter)
     */
    @Override
    public ActiveDescriptor<?> getBestDescriptor(Filter filter) {
        return delegate.getBestDescriptor(filter);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#reifyDescriptor(org.glassfish.hk2.api.Descriptor, org.glassfish.hk2.api.Injectee)
     */
    @Override
    public ActiveDescriptor<?> reifyDescriptor(
            org.glassfish.hk2.api.Descriptor descriptor, Injectee injectee)
            throws MultiException {
        return delegate.reifyDescriptor(descriptor, injectee);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#reifyDescriptor(org.glassfish.hk2.api.Descriptor)
     */
    @Override
    public ActiveDescriptor<?> reifyDescriptor(
            org.glassfish.hk2.api.Descriptor descriptor) throws MultiException {
        return delegate.reifyDescriptor(descriptor);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getInjecteeDescriptor(org.glassfish.hk2.api.Injectee)
     */
    @Override
    public ActiveDescriptor<?> getInjecteeDescriptor(Injectee injectee)
            throws MultiException {
        return delegate.getInjecteeDescriptor(injectee);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.Injectee)
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(
            ActiveDescriptor<T> activeDescriptor, Injectee injectee)
            throws MultiException {
        return delegate.getServiceHandle(activeDescriptor, injectee);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(
            ActiveDescriptor<T> activeDescriptor) throws MultiException {
        return delegate.getServiceHandle(activeDescriptor);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public <T> T getService(ActiveDescriptor<T> activeDescriptor,
            ServiceHandle<?> root) throws MultiException {
        return (T) delegate.getService(activeDescriptor, root);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getName()
     */
    @Override
    public String getName() {
        return delegate.getName();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getLocatorId()
     */
    @Override
    public long getLocatorId() {
        return delegate.getLocatorId();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#shutdown()
     */
    @Override
    public void shutdown() {
        delegate.shutdown();
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#create(java.lang.Class)
     */
    @Override
    public <T> T create(Class<T> createMe) {
        return (T) delegate.create(createMe);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#inject(java.lang.Object)
     */
    @Override
    public void inject(Object object) {
        delegate.inject(object);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#postConstruct(java.lang.Object)
     */
    @Override
    public void postConstruct(Object postConstructMe) {
        delegate.postConstruct(postConstructMe);
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#preDestroy(java.lang.Object)
     */
    @Override
    public void preDestroy(Object preDestroyMe) {
        delegate.preDestroy(preDestroyMe);
        
    }
    
    @Override
    public <U> U createAndInitialize(Class<U> createMe) {
        return delegate.createAndInitialize(createMe);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServices(java.lang.annotation.Annotation, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> List<T> getAllServices(Annotation qualifier,
            Annotation... qualifiers) throws MultiException {
        return (List<T>) delegate.getAllServices(qualifier, qualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServiceHandles(java.lang.annotation.Annotation, java.lang.annotation.Annotation[])
     */
    @Override
    public List<ServiceHandle<?>> getAllServiceHandles(Annotation qualifier,
            Annotation... qualifiers) throws MultiException {
        return (List<ServiceHandle<?>>) delegate.getAllServiceHandles(qualifier, qualifiers);
    }
    
    public String toString() {
        return "Habitat(" + delegate + "," + System.identityHashCode(this) + ")";
    }

    
}
