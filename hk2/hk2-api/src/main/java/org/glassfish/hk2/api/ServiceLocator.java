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
 * Services can be looked up from this instance
 */
@Contract
public interface ServiceLocator {
    /**
     * Gets the best service from this locator that implements
     * this contract or has this implementation
     * <p>
     * Use this method only if destroying the service is not important
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
     * Use this method only if destroying the service is not important
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
     * Gets the best service from this locator that implements
     * this contract or has this implementation and has the given
     * name.  This method will wait indefinitely for a service to arrive.
     * <p>
     * Use this method only if destroying the service is not important
     * <p>
     * This service is waiting for a matching descriptor.  It does not take
     * into account any state changes in context or in descriptors themselves.
     * Only doing a dynamic change to the set of descriptors in the
     * system will cause this method to re-check the service registry
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param name May be null (to indicate any name is ok), and is the name of the
     * implementation to be returned
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return An instance of the service.  May return null if the context
     * from which this service comes allows null service returns
     * @throws MultiException if there was an error during service creation or
     * if the thread was interrupted while waiting, in which case one of the exceptions
     * in the MultiException will contain the {@link InterruptedException}
     * @throws IllegalArgumentException if contractOrImpl is null
     * @throws IllegalStateException if this is called on a locator that has been shut down or the
     * locator is shut down prior to the timeout
     */
    public <T> T waitForService(Type contractOrImpl, String name, Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets the best service from this locator that implements
     * this contract or has this implementation and has the given
     * name.  This method will wait the given amount of time for a service to arrive.
     * <p>
     * Use this method only if destroying the service is not important
     * <p>
     * This service is waiting for a matching descriptor.  It does not take
     * into account any state changes in context or in descriptors themselves.
     * Only doing a dynamic change to the set of descriptors in the
     * system will cause this method to re-check the service registry
     * 
     * @paramTime The amount of time to wait (in milliseconds) for the service
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param name May be null (to indicate any name is ok), and is the name of the
     * implementation to be returned
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return An instance of the service.  May return null if the context
     * from which this service comes allows null service returns.  Will
     * also return null if the service is not found in the given amount of time
     * @throws MultiException if there was an error during service creation or
     * if the thread was interrupted while waiting, in which case one of the exceptions
     * in the MultiException will contain the {@link InterruptedException}
     * @throws IllegalArgumentException if contractOrImpl is null
     * @throws IllegalStateException if this is called on a locator that has been shut down or the
     * locator is shut down prior to the timeout
     */
    public <T> T waitForService(long waitTime, Type contractOrImpl, String name, Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets the all the services from this locator that implements
     * this contract or has this implementation
     * <p>
     * Use this method only if destroying the service is not important
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
     * Use this method only if destroying the services is not important
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
     * Gets the all the services from this locator that implements
     * this contract or has this implementation
     * <p>
     * Use this method only if destroying the service is not important
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
     * Gets a service handle that can be used to get and destroy the returned
     * service.  If a service, and all per lookup services must be destroyed then
     * this method should be used to destroy the object
     * <p>
     * It is assumed that this method is called by the top level code.  All injection
     * points created because of this invocation must use the
     * getServiceHandle(ActiveDescriptor<T>, ServiceHandle<T>)
     * method to retrieve objects, so that they can be destroyed in the proper sequence
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
     * Gets a service handle that can be used to get and destroy the returned
     * service
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
     * Gets a service handle that can be used to get and destroy the returned
     * service.  This method will wait an indefinite amount of time for
     * a suitable service to be found
     * <p>
     * This service is waiting for a matching descriptor.  It does not take
     * into account any state changes in context or in descriptors themselves.
     * Only doing a dynamic change to the set of descriptors in the
     * system will cause this method to re-check the service registry
     * 
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param name The name to use to further qualify the search (may be null,
     * indicating that any name will match)
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return the service handle for the best service matching the
     * given criteria
     * @throws MultiException if there was an error during service creation or
     * if the thread was interrupted while waiting.  One of the exceptions
     * in the MultiException will contain the {@link InterruptedException}
     * @throws IllegalArgumentException if contractOrImpl is null
     * @throws IllegalStateException if this is called on a locator that has been shut down or the
     * locator is shut down prior to the timeout
     */
    public <T> ServiceHandle<T> waitForServiceHandle(Type contractOrImpl, String name,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets a service handle that can be used to get and destroy the returned
     * service.  This method will wait an the given amount of time for
     * a suitable service to be found
     * <p>
     * This service is waiting for a matching descriptor.  It does not take
     * into account any state changes in context or in descriptors themselves.
     * Only doing a dynamic change to the set of descriptors in the
     * system will cause this method to re-check the service registry
     * 
     * @param waitTime The time in milliseconds to wait for the service
     * @param contractOrImpl May not be null, and is the contract
     * or concrete implementation to get the best instance of
     * @param name The name to use to further qualify the search (may be null,
     * indicating that any name will match)
     * @param qualifiers The set of qualifiers that must match this service
     * definition
     * @return the service handle for the best service matching the
     * given criteria, or null if no matching service could be found
     * in the given time
     * @throws MultiException if there was an error during service creation or
     * if the thread was interrupted while waiting, in which case one of the exceptions
     * in the MultiException will contain the {@link InterruptedException}
     * @throws IllegalArgumentException if contractOrImpl is null
     * @throws IllegalStateException if this is called on a locator that has been shut down or the
     * locator is shutdown prior to the timeout
     */
    public <T> ServiceHandle<T> waitForServiceHandle(long waitTime, Type contractOrImpl, String name,
            Annotation... qualifiers) throws MultiException;
    
    /**
     * Gets service handles that can be used to get and destroy the returned
     * services
     * <p>
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
     * Gets service handles that can be used to get and destroy the returned
     * services
     * <p>
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
     * Gets a service handle that can be used to get and destroy the returned
     * service.  If a service, and all per lookup services must be destroyed then
     * this method should be used to destroy the object
     * <p>
     * It is assumed that this method is called by the top level code.  All injection
     * points created because of this invocation must use the
     * getServiceHandle(ActiveDescriptor<T>, ServiceHandle<T>)
     * method to retrieve objects, so that they can be destroyed in the proper sequence
     * 
     * @param searchCriteria A filter to use when determining which services should apply 
     * @return A list of handles in ranked order that match the given filter
     * @throws MultiException if there was an error during service creation
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
     * This method will wait indefinitely for there to be a descriptor
     * that matches this filter.
     * <p>
     * This service is waiting for a matching descriptor.  It does not take
     * into account any state changes in context or in descriptors themselves.
     * Only doing a dynamic change to the set of descriptors in the
     * system will cause this method to re-check the service registry
     * 
     * @param filter The non-null filter to use to retrieve the best descriptor
     * @return The best descriptor matching the filter
     * @throws MultiException if the thread was interrupted while waiting, in which case one
     * of the exceptions in the MultiException will contain the {@link InterruptedException}
     * @throws IllegalStateException if this is called on a locator that has been shut down or the
     * locator is shutdown prior to the timeout
     */
    public ActiveDescriptor<?> waitForBestDescriptor(Filter filter) throws MultiException;
    
    /**
     * This method will wait the given amount of time for there to be a descriptor
     * that matches this filter.
     * <p>
     * This service is waiting for a matching descriptor.  It does not take
     * into account any state changes in context or in descriptors themselves.
     * Only doing a dynamic change to the set of descriptors in the
     * system will cause this method to re-check the service registry
     * 
     * @param waitTime The time in milliseconds to wait for a descriptor matching this filter
     * @param filter The non-null filter to use to retrieve the best descriptor
     * @return The best descriptor matching the filter or null if the timeout occurred and no
     * match was found
     * @throws MultiException if the thread was interrupted while waiting in which case one of
     * the exceptions in the MultiException will contain the {@link InterruptedException}
     * @throws IllegalStateException if this is called on a locator that has been shut down or the
     * locator is shutdown prior to the timeout
     */
    public ActiveDescriptor<?> waitForBestDescriptor(long waitTime, Filter filter) throws MultiException;
    
    /**
     * Converts a descriptor to an ActiveDescriptor.  Will use the registered
     * HK2Loaders to perform this action
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
     * 
     * @param injectee the injection point for whom to find the ActiveDescriptor
     * @return The active descriptor for this injection point
     * @throws MultiException if there were errors when loading or analyzing the class
     */
    public ActiveDescriptor<?> getInjecteeDescriptor(Injectee injectee) throws MultiException;
    
    /**
     * Gets a service handle that can be used to get and destroy the returned
     * service.  If a service, and all per lookup services must be destroyed then
     * this method should be used to destroy the object
     * <p>
     * It is assumed that this method is called by the top level code.  All injection
     * points created because of this invocation must use the
     * getServiceHandle(ActiveDescriptor<T>, ServiceHandle<T>)
     * method to retrieve objects, so that they can be destroyed in the proper sequence
     * 
     * @param activeDescriptor The service handle that can be used to get and destroy
     * this service
     * @param injectee The injectee on behalf of whom this descriptor is being injected.  May
     * be null if the injectee is unknown
     * @return Will return root as a convenience
     * @throws MultiException if there was an error during service creation
     */
    public <T> ServiceHandle<T> getServiceHandle(ActiveDescriptor<T> activeDescriptor, Injectee injectee) throws MultiException;
    
    /**
     * Gets a service handle that can be used to get and destroy the returned
     * service.  If a service, and all per lookup services must be destroyed then
     * this method should be used to destroy the object
     * <p>
     * It is assumed that this method is called by the top level code.  All injection
     * points created because of this invocation must use the
     * getServiceHandle(ActiveDescriptor<T>, ServiceHandle<T>)
     * method to retrieve objects, so that they can be destroyed in the proper sequence
     * 
     * @param activeDescriptor The service handle that can be used to get and destroy
     * this service
     * @return Will return root as a convenience
     * @throws MultiException if there was an error during service creation
     */
    public <T> ServiceHandle<T> getServiceHandle(ActiveDescriptor<T> activeDescriptor) throws MultiException;
    
    /**
     * This method should be called by code getting injectee's on behalf of some
     * root object.  In this way the objects associated with the root object can
     * be destroyed in the proper sequence
     * 
     * @param activeDescriptor The descriptor whose service to create
     * @param root The ultimate parent of this service creation.  May be null
     * @return The service matching this descriptor
     * @throws MultiException if there was an error during service creation
     */
    public <T> T getService(ActiveDescriptor<T> activeDescriptor, ServiceHandle<?> root) throws MultiException;
  
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
     * This method will shutdown every service associated with this ServiceLocator.
     * Those services that have a preDestroy shall have their preDestroy called
     */
    public void shutdown();
    
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
     * This will analyze the given object and inject into its fields and methods.
     * The object injected in this way will not be managed by HK2
     * 
     * @param injectMe The object to be analyzed and injected into
     */
    public void inject(Object injectMe);
    
    /**
     * This will analyze the given object and call the postConstruct method.
     * The object given will not be managed by HK2
     * 
     * @param postConstructMe The object to postConstruct
     */
    public void postConstruct(Object postConstructMe);
    
    /**
     * This will analyze the given object and call the preDestroy method.
     * The object given will not be managed by HK2
     * 
     * @param preDestroyMe The object to preDestroy
     */
    public void preDestroy(Object preDestroyMe);
    
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

}


