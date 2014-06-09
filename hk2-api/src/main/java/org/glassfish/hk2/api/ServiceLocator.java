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
package org.glassfish.hk2.api;

import org.jvnet.hk2.annotations.Contract;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * ServiceLocator is the registry for HK2 services
 * <p>
 * This service is the most fundamental service in an HK2 system.  Every
 * service locator starts with a ServiceLocator as a service, and hence
 * ServiceLocators can be injected into every object managed by HK2.
 * <p>
 * A service locator can have a single parent.  Services are looked up in
 * the current service locator and in all the parents of the service locator.
 * If multiple services exist that match the filter they will all be returned.
 * Two services with the same priority are sorted first by service locator
 * id and second by service id.  This implies that services directly installed
 * in a ServiceLocator have higher natural priority than those in the parents
 * of the ServiceLocator.  Services can also be marked as having visibility LOCAL,
 * in which case they will only be available to the ServiceLocator performing
 * the lookup, and will not leak out to children of that ServiceLocator.
 *
 */
@Contract
public interface ServiceLocator {
    /**
     * Gets the best service from this locator that implements
     * this contract or has this implementation
     * <p>
     * Use this method only if destroying the service is not important,
     * otherwise use {@link ServiceLocator#getServiceHandle(Class, Annotation...)}
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return An instance of the contract or impl.  May return
     * null if there is no provider that provides the given
     * implementation or contract
     * @throws MultiException if there was an error during service creation
     */
    public <T> T getService(Class<T> contractOrImpl, Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets the best service from this locator that implements
     * this contract or has this implementation
     * <p>
     * Use this method only if destroying the service is not important,
     * otherwise use {@link ServiceLocator#getServiceHandle(Type, Annotation...)}
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return An instance of the contract or impl.  May return
     * null if there is no provider that provides the given
     * implementation or contract
     * @throws MultiException if there was an error during service creation
     */
    public <T> T getService(Type contractOrImpl, Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets the best service from this locator that implements
     * this contract or has this implementation and has the given
     * name
     * <p>
     * Use this method only if destroying the service is not important,
     * otherwise use {@link ServiceLocator#getServiceHandle(Class, String, Annotation...)}
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param name May be null (to indicate any name is ok), and is the name of the
     * implementation to be returned
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return An instance of the contract or impl.  May return
     * null if there is no provider that provides the given
     * implementation or contract
     * @throws MultiException if there was an error during service creation
     */
    public <T> T getService(Class<T> contractOrImpl, String name, Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets the best service from this locator that implements
     * this contract or has this implementation and has the given
     * name
     * <p>
     * Use this method only if destroying the service is not important,
     * otherwise use {@link ServiceLocator#getServiceHandle(Type, String, Annotation...)}
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param name May be null (to indicate any name is ok), and is the name of the
     * implementation to be returned
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return An instance of the contract or impl.  May return
     * null if there is no provider that provides the given
     * implementation or contract
     * @throws MultiException if there was an error during service creation
     */
    public <T> T getService(Type contractOrImpl, String name, Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets the all the services from this locator that implements
     * this contract or has this implementation
     * <p>
     * Use this method only if destroying the service is not important,
     * otherwise use {@link ServiceLocator#getAllServiceHandles(Class, Annotation...)}
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return A list of services implementing this contract
     * or concrete implementation.  May not return null, but
     * may return an empty list
     * @throws MultiException if there was an error during service creation
     */
    public <T> List<T> getAllServices(Class<T> contractOrImpl,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets the all the services from this locator that implements
     * this contract or has this implementation
     * <p>
     * Use this method only if destroying the service is not important,
     * otherwise use {@link ServiceLocator#getAllServiceHandles(Type, Annotation...)}
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return A list of services implementing this contract
     * or concrete implementation.  May not return null, but
     * may return an empty list
     * @throws MultiException if there was an error during service creation
     */
    public <T> List<T> getAllServices(Type contractOrImpl,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets the all the services from this locator that has the given
     * qualifier or qualifiers
     * <p>
     * Use this method only if destroying the services is not important,
     * otherwise use {@link ServiceLocator#getAllServiceHandles(Annotation, Annotation...)}
     * 
     * @param qualifier May not be null, and is a qualifier that must
     * match the service definition
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return A list of services implementing this contract
     * or concrete implementation.  May not return null, but
     * may return an empty list
     * @throws MultiException if there was an error during service creation
     */
    public <T> List<T> getAllServices(Annotation qualifier,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets the all the services from this locator that matches the
     * {@link Filter}
     * <p>
     * Use this method only if destroying the service is not important,
     * otherwise use {@link ServiceLocator#getAllServiceHandles(Filter)}
     * <p>
     * This method should also be used with care to avoid classloading
     * a large number of services
     * 
     * @param searchCriteria The returned service will match the Filter
     * (in other words, searchCriteria.matches returns true).  May not
     * be null
     * @return A list of services matching this filter.  May not return null,
     * but may return an empty list
     * @throws MultiException if there was an error during service creation
     */
    public List<?> getAllServices(Filter searchCriteria) throws MultiException;
    
    /**
     * Gets a {@link ServiceHandle} that can be used to get and destroy the
     * service that best matches the given criteria
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return Will return root as a convenience
     * @throws MultiException if there was an error during service creation
     */
    public <T> ServiceHandle<T> getServiceHandle(Class<T> contractOrImpl, Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets a {@link ServiceHandle} that can be used to get and destroy the
     * service that best matches the given criteria
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return Will return root as a convenience
     * @throws MultiException if there was an error during service creation
     */
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl, Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets a {@link ServiceHandle} that can be used to get and destroy the
     * service that best matches the given criteria
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param name The name to use to further qualify the search (may be null,
     * indicating that any name will match)
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return Will the service handle for the best service matching the
     * given criteria, or null if no matching service could be found
     * @throws MultiException if there was an error during service creation
     * @throws IllegalArgumentException if contractOrImpl is null
     */
    public <T> ServiceHandle<T> getServiceHandle(Class<T> contractOrImpl, String name,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets a {@link ServiceHandle} that can be used to get and destroy the
     * service that best matches the given criteria
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param name The name to use to further qualify the search (may be null,
     * indicating that any name will match)
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return Will the service handle for the best service matching the
     * given criteria, or null if no matching service could be found
     * @throws MultiException if there was an error during service creation
     * @throws IllegalArgumentException if contractOrImpl is null
     */
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl, String name,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets a list of {@link ServiceHandle} that can be used to get and destroy services
     * associated with descriptors that match the provided criteria
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return A non-null but possibly empty list of service handles matching
     * the given criteria
     * @throws MultiException if there was an error during service creation
     * @throws IllegalArgumentException if contractOrImpl is null
     */
    public <T> List<ServiceHandle<T>> getAllServiceHandles(Class<T> contractOrImpl,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets a list of {@link ServiceHandle} that can be used to get and destroy services
     * associated with descriptors that match the provided criteria
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return A non-null but possibly empty list of service handles matching
     * the given criteria
     * @throws MultiException if there was an error during service creation
     * @throws IllegalArgumentException if contractOrImpl is null
     */
    public List<ServiceHandle<?>> getAllServiceHandles(Type contractOrImpl,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets a list of {@link ServiceHandle} that can be used to get and destroy services
     * associated with descriptors that match the provided criteria
     * 
     * @param qualifier May not be null, and is a qualifier that must
     * match the service definition
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return A non-null but possibly empty list of service handles matching
     * the given criteria
     * @throws MultiException if there was an error during service creation
     * @throws IllegalArgumentException if contractOrImpl is null
     */
    public List<ServiceHandle<?>> getAllServiceHandles(Annotation qualifier,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets a list of {@link ServiceHandle} whose {@link ActiveDescriptor}s match
     * the supplied filter.  The returned {@link ServiceHandle}s may be used to
     * get or destroy the services associated with the matching descriptors
     * 
     * @param searchCriteria A filter to use when determining which descriptors should apply 
     * @return A list of service handles in ranked order that match the given filter
     * @throws MultiException if there was an error during service handle creation
     */
    public List<ServiceHandle<?>> getAllServiceHandles(Filter searchCriteria) throws MultiException;
    
    /**
     * Gets the list of descriptors that match the given filter
     * 
     * @param filter A filter to use when determining which services should apply
     * @return A list of descriptors in ranked order that match the given filter
     */
    public List<ActiveDescriptor<?>> getDescriptors(Filter filter);
    
    /**
     * Gets the descriptor that best matches this filter, taking ranking
     * and service id into account
     * 
     * @param filter The non-null filter to use to retrieve the best descriptor
     * @return The best descriptor matching the filter, or null if there
     * is no descriptor that matches the filter
     */
    public ActiveDescriptor<?> getBestDescriptor(Filter filter);
    
    /**
     * Converts a descriptor to an ActiveDescriptor.  Will use the registered
     * HK2Loaders to perform this action.  If no HK2Loader is available for
     * the descriptor, will use the injectee to discover a classloader
     * 
     * @param descriptor The descriptor to convert, may not be null
     * @param injectee The injectee on behalf of whom this descriptor is being injected.  May
     * be null if the injectee is unknown
     * @return The active descriptor as loaded with the first valid {@link HK2Loader}
     * @throws MultiException if there were errors when loading or analyzing the class
     */
    public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor, Injectee injectee) throws MultiException;
    
    /**
     * Converts a descriptor to an ActiveDescriptor.  Will use the registered
     * HK2Loaders to perform this action
     * 
     * @param descriptor The descriptor to convert, may not be null
     * @return The active descriptor as loaded with the first valid {@link HK2Loader}
     * @throws MultiException if there were errors when loading or analyzing the class
     */
    public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor) throws MultiException;
    
    /**
     * This method will first find a descriptor for this injectee, and then
     * reify that descriptor.  If multiple descriptors are found, they will
     * be reified in ranking order until an ActiveDescriptor matching the Injectee is
     * found.
     * <p>
     * This method is responsible for using the available {@link JustInTimeInjectionResolver}
     * to add in new descriptors should the descriptor for the given injectee
     * not be found initially
     * 
     * @param injectee the injection point for whom to find the ActiveDescriptor
     * @return The active descriptor for this injection point
     * @throws MultiException if there were errors when loading or analyzing the class
     */
    public ActiveDescriptor<?> getInjecteeDescriptor(Injectee injectee) throws MultiException;
    
    /**
     * Gets a {@link ServiceHandle} that can be used to get and destroy the service
     * described by the {@link ActiveDescriptor}.  The injectee may be used to discover
     * the proper classloader to use when attempting to reify the {@link ActiveDescriptor}
     * 
     * @param activeDescriptor The descriptor for which to create a {@link ServiceHandle}.
     * May not be null
     * @param injectee The injectee on behalf of whom this service is being injected.  May
     * be null if the injectee is unknown
     * @return A {@link ServiceHandle} that may be used to create or destroy the service
     * associated with this {@link ActiveDescriptor}
     * @throws MultiException if there was an error during service handle creation
     */
    public <T> ServiceHandle<T> getServiceHandle(ActiveDescriptor<T> activeDescriptor, Injectee injectee) throws MultiException;
    
    /**
     * Gets a {@link ServiceHandle} that can be used to get and destroy the service
     * described by the {@link ActiveDescriptor}.
     * 
     * @param activeDescriptor The descriptor for which to create a {@link ServiceHandle}.
     * May not be null
     * @return A {@link ServiceHandle} that may be used to create or destroy the service
     * associated with this {@link ActiveDescriptor}
     * @throws MultiException if there was an error during service handle creation
     */
    public <T> ServiceHandle<T> getServiceHandle(ActiveDescriptor<T> activeDescriptor) throws MultiException;
    
    /**
     * This method should be called by code resolving injectee's on behalf of some
     * root service, usually by an implementation of {@link InjectionResolver#resolve(Injectee, ServiceHandle)}.  In
     * this way the objects associated with the root object can be destroyed in the proper sequence
     * 
     * @param activeDescriptor The descriptor whose service to create
     * @param root The ultimate parent of this service creation.  May be null
     * @return The service matching this descriptor
     * @throws MultiException if there was an error during service creation
     * @deprecated use {@link ServiceLocator#getService(ActiveDescriptor, ServiceHandle, Injectee)}
     */
    @Deprecated
    public <T> T getService(ActiveDescriptor<T> activeDescriptor, ServiceHandle<?> root) throws MultiException;
    
    /**
     * This method should be called by code resolving injectee's on behalf of some
     * root service, usually by an implementation of {@link InjectionResolver#resolve(Injectee, ServiceHandle)}.  In
     * this way the objects associated with the root object can be destroyed in the proper sequence
     * 
     * @param activeDescriptor The descriptor whose service to create
     * @param root The ultimate parent of this service creation.  May be null
     * @param injectee The injectee passed into the {@link InjectionResolver#resolve(Injectee, ServiceHandle)} if known,
     * null otherwise
     * @return The service matching this descriptor
     * @throws MultiException if there was an error during service creation
     */
    public <T> T getService(ActiveDescriptor<T> activeDescriptor, ServiceHandle<?> root, Injectee injectee) throws MultiException;
    
    /**
     * Gets the name of the default class analyzer service
     *  
     * @return The name of the default class analyzer.  Will not return null
     */
    public String getDefaultClassAnalyzerName();
    
    /**
     * Sets the name of the default class analyzer that should be used for all
     * {@link Descriptor}s that return null as their class analyzer.  If null is given
     * then the default class analyzer name of {@link ClassAnalyzer#DEFAULT_IMPLEMENTATION_NAME}
     * will be used
     * 
     * @param defaultClassAnalyzer The possibly null name of the default class
     * analyzer (the class analyzer that will be used if a descriptor has not
     * explicitly set the name of its class analyzer)
     */
    public void setDefaultClassAnalyzerName(String defaultClassAnalyzer);
  
    /**
     * Returns the name of this ServiceLocator
     * @return The name of this ServiceLocator, will not return null
     */
    public String getName();
    
    /**
     * This returns the unique locator ID for this locator.  The locator ID will
     * be assigned at the time of creation and must be a monotonacally increasing
     * number (starting at zero)
     * @return The identifier for this service locator
     */
    public long getLocatorId();
    
    /**
     * Gets the parent service locator for this locator
     * 
     * @return The parent service locator for this locator, or null if this
     * service locator does not have a parent
     */
    public ServiceLocator getParent();
  
    /**
     * This method will shutdown every service associated with this ServiceLocator.
     * Those services that have a preDestroy shall have their preDestroy called
     */
    public void shutdown();
    
    /**
     * Returns the current state of this service locator.  This method will work
     * in all service locator states
     * 
     * @return The current state of the service locator
     */
    public ServiceLocatorState getState();
    
    /**
     * This returns the value of neutralContextClassLoader.  If
     * this value is true then HK2 will ensure that the context
     * class loader on the thread is maintained whenever hk2 calls
     * into user code.  If this value is false then the value of
     * the context class loader on the thread may be changed by
     * the code hk2 is calling.
     * <p>
     * When set to false this value is used to increase performance
     * since getting and setting the context class loader can be expensive.
     * However, if the user code being called by hk2 may change the context
     * class loader of the thread, this value should be true to ensure that
     * tricky and hard to find bugs don't arise when this thread is used for
     * other purposes later on
     * <p>
     * All new ServiceLocator implementation have this value initially set
     * to true
     * @return If true hk2 will ensure that the context class loader cannot
     * be changed by user code.  If false hk2 will not modify the context
     * class loader of the thread when user code has finished
     */
    public boolean getNeutralContextClassLoader();
    
    /**
     * This sets the value of neutralContextClassLoader.  If
     * this value is true then HK2 will ensure that the context
     * class loader on the thread is maintained whenever hk2 calls
     * into user code.  If this value is false then the value of
     * the context class loader on the thread may be changed by
     * the code hk2 is calling.
     * <p>
     * When set to false this value is used to increase performance
     * since getting and setting the context class loader can be expensive.
     * However, if the user code being called by hk2 may change the context
     * class loader of the thread, this value should be true to ensure that
     * tricky and hard to find bugs don't arise when this thread is used for
     * other purposes later on
     * <p>
     * All new ServiceLocator implementation have this value initially set
     * to true
     * 
     * @param neutralContextClassLoader true if hk2 should ensure context class
     * loader neutrality, false if hk2 should not change the context class loader
     * on the thread around user code calls
     */
    public void setNeutralContextClassLoader(boolean neutralContextClassLoader);
    
    /**
     * This method will analyze the given class, and create it if can.  The object
     * created in this way will not be managed by HK2.  It is the responsibility of
     * the caller to ensure that any lifecycle this object has is honored
     * 
     * @param createMe The class to create, may not be null
     * @return An instance of the object
     */
    public <T> T create(Class<T> createMe);
    
    /**
     * This method will analyze the given class, and create it if can.  The object
     * created in this way will not be managed by HK2.  It is the responsibility of
     * the caller to ensure that any lifecycle this object has is honored
     * 
     * @param createMe The class to create, may not be null
     * @param strategy The name of the {@link ClassAnalyzer} that should be used. If
     * null the default analyzer will be used
     * @return An instance of the object
     */
    public <T> T create(Class<T> createMe, String strategy);
    
    /**
     * This will analyze the given object and inject into its fields and methods.
     * The object injected in this way will not be managed by HK2
     * 
     * @param injectMe The object to be analyzed and injected into
     */
    public void inject(Object injectMe);
    
    /**
     * This will analyze the given object and inject into its fields and methods.
     * The object injected in this way will not be managed by HK2
     * 
     * @param injectMe The object to be analyzed and injected into
     * @param strategy The name of the {@link ClassAnalyzer} that should be used. If
     * null the default analyzer will be used
     */
    public void inject(Object injectMe, String strategy);
    
    /**
     * This will analyze the given object and call the postConstruct method.
     * The object given will not be managed by HK2
     * 
     * @param postConstructMe The object to postConstruct
     */
    public void postConstruct(Object postConstructMe);
    
    /**
     * This will analyze the given object and call the postConstruct method.
     * The object given will not be managed by HK2
     * 
     * @param postConstructMe The object to postConstruct
     * @param strategy The name of the {@link ClassAnalyzer} that should be used. If
     * null the default analyzer will be used
     */
    public void postConstruct(Object postConstructMe, String strategy);
    
    /**
     * This will analyze the given object and call the preDestroy method.
     * The object given will not be managed by HK2
     * 
     * @param preDestroyMe The object to preDestroy
     */
    public void preDestroy(Object preDestroyMe);
    
    /**
     * This will analyze the given object and call the preDestroy method.
     * The object given will not be managed by HK2
     * 
     * @param preDestroyMe The object to preDestroy
     * @param strategy The name of the {@link ClassAnalyzer} that should be used. If
     * null the default analyzer will be used
     */
    public void preDestroy(Object preDestroyMe, String strategy);
    
    /**
     * This method creates, injects and post-constructs an object with the given
     * class. This is equivalent to calling the {@link ServiceLocator#create(Class)}
     * method followed by the {@link ServiceLocator#inject(Object)} method followed
     * by the {@link ServiceLocator#postConstruct(Object)} method.
     * <p>
     * The object created is not managed by the locator.
     *
     * @param createMe The non-null class to create this object from
     * @return An instance of the object that has been created, injected and post constructed
     * @throws MultiException if there was an error when creating or initializing the object
     */
    public <U> U createAndInitialize(Class<U> createMe);
    
    /**
     * This method creates, injects and post-constructs an object with the given
     * class. This is equivalent to calling the {@link ServiceLocator#create(Class)}
     * method followed by the {@link ServiceLocator#inject(Object)} method followed
     * by the {@link ServiceLocator#postConstruct(Object)} method.
     * <p>
     * The object created is not managed by the locator.
     *
     * @param createMe The non-null class to create this object from
     * @param strategy The name of the {@link ClassAnalyzer} that should be used. If
     * null the default analyzer will be used
     * @return An instance of the object that has been created, injected and post constructed
     * @throws MultiException if there was an error when creating or initializing the object
     */
    public <U> U createAndInitialize(Class<U> createMe, String strategy);

}


