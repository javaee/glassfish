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
import org.glassfish.hk2.api.Descriptor;
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
import org.glassfish.hk2.internal.IndexedFilterImpl;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.AliasDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
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
public class Habitat implements ServiceLocator, BaseServiceLocator {
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

    /*
     * Why initialize/processInhabitantDecorations didn't work:
     * 
     * when a new CageBuilder comes into the habitat, it needs to build cages
     * for all existing components. when a caged component comes into the
     * habitat, it checks existing cage builders in the habitat.
     * 
     * Now, when a cage builder and a component are both initialized first and
     * then processInhabitantDecorations runs for both of them later, then you
     * end up creating a cage twice, because the builder think it got into
     * habitat after than the component, and the component think it got into
     * habitat after the builder.
     */

    /**
     * Adds a new inhabitant.
     * <p/>
     * <p/>
     * See {@link Inhabitants} for typical ways to create {@link Inhabitant}s.
     */
    public void add(final Inhabitant<?> i) {
        Utilities.add(delegate, i);
    }

    /**
     * Adds a new index to look up the given inhabitant.
     *
     * @param index Primary index name, such as contract FQCN.
     * @param name  Name that identifies the inhabitant among other inhabitants in
     *              the same index. Can be null for unnamed inhabitants.
     */
    public void addIndex(Inhabitant<?> i, String index, String name) {
        addIndex(i, index, name, true);
    }

    protected void addIndex(Inhabitant<?> i, String index, String name, boolean notify) {

        // Need the descriptorImp because the inhabitant likely has equals overridden
        final Descriptor matchDescriptor = new DescriptorImpl(i);
        List<ActiveDescriptor<?>> activeDescriptors = getDescriptors(new Filter(){
            @Override
            public boolean matches(Descriptor d) {
                return matchDescriptor.equals(d);
            }
        });

        ActiveDescriptor<?> activeDescriptor = null;
        for (ActiveDescriptor candidate : activeDescriptors) {
            Descriptor d = candidate.getBaseDescriptor();
            if (d == null) continue;
            
            if (d == i) {
                activeDescriptor = candidate;
                break;
            }
            
        }
        
        if (activeDescriptor == null) {
            activeDescriptor = Utilities.add(delegate, i);
        }

        Utilities.addIndex(delegate, activeDescriptor, index, name);

        if (notify) {
            // TODO : notify not currently supported
        }
    }

    protected static Long getServiceRanking(Inhabitant<?> i, boolean wantNonNull) {
        int rank = i.getRanking();
        Long retVal = new Long(rank);
        
        return retVal;
    }

    /**
     * Removes an inhabitant
     *
     * @param inhabitant inhabitant to be removed
     */
    public boolean remove(Inhabitant<?> inhabitant) {
        final Descriptor matchDescriptor = new DescriptorImpl(inhabitant);
        final Filter filter = new Filter() {
            @Override
            public boolean matches(Descriptor d) {
                return matchDescriptor.equals(d);
            }
        };

        // TODO : notify not currently supported

        return Utilities.remove(delegate, filter);
    }

    /**
     * Removes a NamedInhabitant for a specific contract
     *
     * @param index contract name
     * @param name  instance name
     * @return true if the removal was successful
     */
    public boolean removeIndex(String index, String name) {
        // TODO : notify not currently supported

        return Utilities.remove(delegate, new IndexedFilterImpl(index, name));
    }

    /**
     * Removes a Contracted service
     *
     * @param index               the contract name
     * @param serviceOrInhabitant the service instance, or an Inhabitant instance
     */
    public boolean removeIndex(String index, Object serviceOrInhabitant) {
        final Object o        = serviceOrInhabitant;
        final String contract = index;
        final Filter filter = new Filter() {
            @Override
            public boolean matches(Descriptor d) {
                if (d.getAdvertisedContracts().contains(contract)) {
                    Descriptor baseDescriptor = d.getBaseDescriptor();
                    if (baseDescriptor instanceof AliasDescriptor<?>) {
                        if (((AliasDescriptor<?>)baseDescriptor).getDescriptor().getBaseDescriptor().equals(o)) {
                            return true;
                        }
                    }
                    ActiveDescriptor<?> activeDescriptor = (ActiveDescriptor<?>)d;

                    return (activeDescriptor.isReified() &&
                            delegate.getServiceHandle(activeDescriptor).getService().equals(o));
                }
                return false;
            }
        };

        // TODO : notify not currently supported

        return Utilities.remove(delegate, filter);
    }

    /**
     * FOR INTERNAL USE ONLY
     */
    public synchronized void initialized() {
        return;
    }

    public boolean isInitialized() {
        return true;
    }
    
    /**
     * Gets all the inhabitants registered under the given {@link Contract}.
     * This is an example of heterogeneous type-safe container.
     *
     * @return can be empty but never null.
     */
    public <T> Collection<T> getAllByContract(Class<T> contractType) {
        return delegate.getAllServices(contractType);
    }

    public <T> Collection<T> getAllByContract(String contractType) {
        List<?> allServices = delegate.getAllServices(BuilderHelper.createContractFilter(contractType));
        return (Collection<T>) allServices;
    }

    /**
     * Gets the object of the given type.
     *
     * @return can be empty but never null.
     */
    public <T> Collection<T> getAllByType(Class<T> implType) {
        return delegate.getAllServices(implType);
    }

    /**
     * Add an already instantiated component to this manager. The component has
     * been instantiated by external code, however dependency injection,
     * PostConstruct invocation and dependency extraction will be performed on
     * this instance before it is store in the relevant scope's resource
     * manager.
     *
     * @param component component instance
     * @throws ComponentException if the passed object is not an HK2 component or
     *                            injection/extraction failed.
     */
    public <T> void addComponent(T component) throws ComponentException {
        if (component == null) return;
        
        DynamicConfigurationService dcs = delegate.getService(DynamicConfigurationService.class);
        if (dcs == null) {
            throw new ComponentException("No DynamicConfigurationService available");
        }
        
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(BuilderHelper.createConstantDescriptor(component));
        
        try {
            config.commit();
        }
        catch (MultiException me) {
            throw new ComponentException(me);
        }
    }

    /**
     * Obtains a reference to the component inside the manager.
     * <p/>
     * <p/>
     * This is the "new Foo()" equivalent in the IoC world.
     * <p/>
     * <p/>
     * Depending on the {@link Scope} of the component, a new instance might be
     * created, or an existing instance might be returned.
     *
     * @return non-null.
     * @throws ComponentException If failed to obtain a requested instance. In practice,
     *                            failure only happens when we try to create a new instance of
     *                            the component.
     */
    public <T> T getComponent(Class<T> clazz) throws ComponentException {
        return (T) delegate.getService(clazz);
    }

    @Override
    public <T> T getComponent(final Class<T> contract, String name)
            throws ComponentException {
        if (name != null && name.length() <= 0) name = null;
        
        return (T) delegate.getService(contract, name);
    }

    @Override
    public <T> T getComponent(String fullQualifiedName, String name) {
        if (name != null && name.length() <= 0) name = null;
        
        ActiveDescriptor<?> best = delegate.getBestDescriptor(BuilderHelper.createNameAndContractFilter(fullQualifiedName, name));
        if (best == null) return null;
        
        return (T) delegate.getServiceHandle(best).getService();
    }

    /**
     * Gets a lazy reference to the component.
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return null if no such component is found.
     */
    public <T> Inhabitant<T> getInhabitant(Class<T> contract, String name)
            throws ComponentException {
        ActiveDescriptor<T> best = (ActiveDescriptor<T>)
                delegate.getBestDescriptor(BuilderHelper.createNameAndContractFilter(contract.getName(), name));
        return Utilities.getInhabitantFromActiveDescriptor(best, delegate);
    }


    public <T> Inhabitant<T> getInhabitant(java.lang.reflect.Type type, String name) {
        ServiceHandle<T> handle = delegate.getServiceHandle(type, name);
        if (handle == null) return null;
        
        ActiveDescriptor<T> best = (ActiveDescriptor<T>) handle.getActiveDescriptor();
        return Utilities.getInhabitantFromActiveDescriptor(best, delegate);
    }

    /**
     * Gets a lazy reference to the component.
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return null if no such component is found.
     */
    public <T> Inhabitant<T> getInhabitantByType(Class<T> implType) {
        return getInhabitant(implType, null);
    }

    public <T> Inhabitant<T> getInhabitantByType(java.lang.reflect.Type implType) {
        return getInhabitant(implType, null);
    }

    public Inhabitant<?> getInhabitantByType(String fullyQualifiedClassName) {
        ActiveDescriptor<?> best = delegate.getBestDescriptor(BuilderHelper.createContractFilter(fullyQualifiedClassName));
        return Utilities.getInhabitantFromActiveDescriptor(best, delegate);
    }

    /**
     * Gets the inhabitant that has the given contract annotation and the given
     * name.
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return null if no such component is found.
     */
    public Inhabitant<?> getInhabitantByAnnotation(
            Class<? extends Annotation> contract, String name)
            throws ComponentException {
        ActiveDescriptor<?> best = delegate.getBestDescriptor(new QualifierFilter(contract.getName(), name));
        return Utilities.getInhabitantFromActiveDescriptor(best, delegate);
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
     * Gets all the inhabitants that has the given contract.
     */
    public <T> Collection<Inhabitant<T>> getInhabitantsByContract(
            Type contract) throws ComponentException {
        List<ServiceHandle<?>> all = delegate.getAllServiceHandles(contract);
        
        LinkedList<Inhabitant<T>> retVal = new LinkedList<Inhabitant<T>>();
        
        for (ServiceHandle<?> a : all) {
            Inhabitant<T> addMe = Utilities.getInhabitantFromActiveDescriptor(
                    (ActiveDescriptor<T>) a.getActiveDescriptor(), delegate);
            
            retVal.add(addMe);
        }
        
        return retVal;
    }

    /**
     * Instantiate the passed type and injects all the {@link org.jvnet.hk2.annotations.Inject}
     * annotated fields and methods
     *
     * @param type class of the requested instance
     * @param <T> type of the requested instance
     * @return the instantiated and injected instance
     */
    public <T> T inject(Class<T> type) {
        Object o = delegate.create(type);
        delegate.inject(o);
        delegate.postConstruct(o);
        
        return (T) o;
    }

    /**
     * Gets all the inhabitants that has the given implementation type.
     */
    public <T> Collection<Inhabitant<T>> getInhabitantsByType(Class<T> implType)
            throws ComponentException {
        return getInhabitantsByContract(implType);
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

    /**
     * Get the first inhabitant by contract
     *
     * @param typeName fullyQualifiedClassName
     * @return
     */
    public Inhabitant<?> getInhabitantByContract(String typeName) {
        return getInhabitantByType(typeName);
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

    /**
     * Gets all the inhabitants that has the given contract and the given name
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return Can be empty but never null.
     */
    public <T> Iterable<Inhabitant<? extends T>> getInhabitants(
            Class<T> contract, String name) throws ComponentException {
        List<ActiveDescriptor<?>> all = delegate.getDescriptors(BuilderHelper.createNameAndContractFilter(contract.getName(), name));
        
        LinkedList<Inhabitant<? extends T>> retVal = new LinkedList<Inhabitant<? extends T>>();
        for (ActiveDescriptor<?> a : all) {
            Inhabitant<? extends T> addMe = Utilities.getInhabitantFromActiveDescriptor((ActiveDescriptor<? extends T>) a, delegate);
            
            retVal.add(addMe);
        }
        
        return retVal;
    }

    /**
     * Gets all the inhabitants that has the given contract annotation and the
     * given name.
     * <p/>
     * <p/>
     * This method defers the actual instantiation of the component until
     * {@link Inhabitant#get()} is invoked.
     *
     * @return Can be empty but never null.
     */
    public Iterable<Inhabitant<?>> getInhabitantsByAnnotation(
            Class<? extends Annotation> contract, String name)
            throws ComponentException {
        List<ActiveDescriptor<?>> all = delegate.getDescriptors(new QualifierFilter(contract.getName(), name));
        
        LinkedList<Inhabitant<?>> retVal = new LinkedList<Inhabitant<?>>();
        for (ActiveDescriptor<?> a : all) {
            Inhabitant<?> addMe = Utilities.getInhabitantFromActiveDescriptor(a, delegate);
            
            retVal.add(addMe);
        }
        
        return retVal;
    }

    @Override
    public <T> T getByType(Class<T> implType) {
        return (T) delegate.getService(implType);
    }

    @Override
    public <T> T getByType(String implType) {
        ActiveDescriptor<T> best = (ActiveDescriptor<T>)
                delegate.getBestDescriptor(BuilderHelper.createContractFilter(implType));
        
        return (T) delegate.getServiceHandle(best).getService();
    }

    public <T> Inhabitant<T> getProvider(Type type, String name) {
        ServiceHandle<T> handle = delegate.getServiceHandle(type, name);
        
        ActiveDescriptor<T> best = handle.getActiveDescriptor();
        return Utilities.getInhabitantFromActiveDescriptor(best, delegate);
    }

    /**
     * Gets the object that has the given contract.
     * <p/>
     * <p/>
     * If there are more than one of them, this method arbitrarily return one of
     * them.
     */
    public <T> T getByContract(Class<T> contractType) {
        return (T) delegate.getService(contractType);
    }

    public <T> T getByContract(String contractType) {
        return (T) getByType(contractType);
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
