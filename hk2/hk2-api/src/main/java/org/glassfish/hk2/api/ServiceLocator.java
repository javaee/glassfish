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
	 * best is some measure of the most appropriate service where
	 * there may be multiple providers of the contract.  If the
	 * best is not what was intended use getAllServices and choose
	 * amongst the returned types
	 * 
	 * @param contractOrImpl May not be null, and is the contract
	 * or concrete implementation to get the best instance of
	 * @return An instance of the contract or impl.  May return
	 * null if there is no provider that provides the given
	 * implementation or contract
	 */
  public <T> T getService(Class<T> contractOrImpl);
    
  /**
	 * Gets the all the services from this locator that implements
	 * this contract or has this implementation
	 * 
	 * @param contractOrImpl May not be null, and is the contract
	 * or concrete implementation to get the best instance of
	 * @return A list of services implementing this contract
	 * or concrete implementation.  May not return null, but
	 * may return an empty list
	 */
  public <T> List<T> getAllServices(Class<T> contractOrImpl);
    
  /**
	 * Gets the best service from this locator that implements
	 * this contract or has this implementation and has the given
	 * name
	 * <p>
	 * best is some measure of the most appropriate service where
	 * there may be multiple providers of the contract.  If the
	 * best is not what was intended use getAllServices and choose
	 * amongst the returned types
	 * 
	 * @param contractOrImpl May not be null, and is the contract
	 * or concrete implementation to get the best instance of
	 * @param name May not be null, and is the name of the
	 * implementation to be returned
	 * @return An instance of the contract or impl.  May return
	 * null if there is no provider that provides the given
	 * implementation or contract
	 */
  public <T> T getService(Class<T> contractOrImpl, String name);
    
  /**
	 * Gets the best service from this locator that implements
	 * this contract or has this implementation and has the given
	 * name
	 * <p>
	 * best is some measure of the most appropriate service where
	 * there may be multiple providers of the contract.  If the
	 * best is not what was intended use getAllServices and choose
	 * amongst the returned types
	 * <p>
	 * Will use the CCL for loading any classes
	 * 
	 * @param contractOrImpl May not be null, and is the fully
	 * qualified class name of the contract or concrete implementation to
	 * get the best instance of
	 * @return An instance of the contract or impl.  May return
	 * null if there is no provider that provides the given
	 * implementation or contract
	 */
  <T> T getService(String contractOrImpl);
    
  /**
	 * Gets the all the services from this locator that implements
	 * this contract or has this implementation
	 * <p>
	 * Will use the CCL for loading any classes
	 * 
	 * @param contractOrImpl May not be null, and is the fully
	 * qualified class name of the contract or concrete implementation to
	 * get the best instance of
	 * @return A list of services implementing this contract
	 * or concrete implementation.  May not return null, but
	 * may return an empty list
	 */
  <T> List<T> getAllServices(String contractOrImpl);
    
  /**
	 * Gets the best service from this locator that implements
	 * this contract or has this implementation and has the given
	 * name
	 * <p>
	 * best is some measure of the most appropriate service where
	 * there may be multiple providers of the contract.  If the
	 * best is not what was intended use getAllServices and choose
	 * amongst the returned types
	 * <p>
	 * Will use the CCL for loading any classes
	 * 
	 * @param contractOrImpl May not be null, and is the fully
	 * qualified class name of the contract or concrete implementation to
	 * get the best instance of
	 * @param name May not be null, and is the name of the
	 * implementation to be returned
	 * @return An instance of the contract or impl.  May return
	 * null if there is no provider that provides the given
	 * implementation or contract
	 */
  <T> T getService(String contractOrImpl, String name);
    
  /**
	 * Gets the best service from this locator that matches
	 * the given filter
	 * <p>
	 * best is some measure of the most appropriate service where
	 * there may be multiple providers of the contract.  If the
	 * best is not what was intended use getAllServices and choose
	 * amongst the returned types
	 * 
	 * @param searchCriteria The returned service will match the Filter
	 * (in other words, searchCriteria.matches returns true).  May not
	 * be null
	 * @return An instance of a service that matches the filter.  May
	 * return null if there is no provider that matches the filter
	 */
  <T> T getService(Filter<Descriptor> searchCriteria);
    
  /**
	 * Gets the all the services from this locator that implements
	 * this contract or has this implementation
	 * 
	 * @param searchCriteria The returned service will match the Filter
	 * (in other words, searchCriteria.matches returns true).  May not
	 * be null
	 * @return A list of services matching this filter.  May not return null,
	 * but may return an empty list
	 */
  <T> List<T> getAllServices(Filter<Descriptor> searchCriteria);
    
  /**
   * This method returns the best provider that matches the search criteria.  Note
   * that this does not return an instance of the service itself, but rather a provider
   * for the service.  This is useful when the service should be instantiated at a later
   * time
   * <p>
	 * best is some measure of the most appropriate service where
	 * there may be multiple providers of the contract.  If the
	 * best is not what was intended use getAllServices and choose
	 * amongst the returned types
	 * 
   * @param searchCriteria The returned service will match the Filter
	 * (in other words, searchCriteria.matches returns true). May not be null
	 * @return An instance of a service that matches the filter.  May
	 * return null if there is no provider that matches the filter
   */
  <T> ExtendedProvider<T> getServiceProvider(Filter<Descriptor> searchCriteria);
    
  /**
	 * Gets the all the service providers from this locator that
	 * matches this filter
	 *
	 * @param searchCriteria The returned service will match the Filter
	 * (in other words, searchCriteria.matches returns true).  May not
	 * be null
	 * @return A list of service providers matching this filter.  May not return null,
	 * but may return an empty list
	 */
  <T> List<ExtendedProvider<T>> getAllServiceProviders(Filter<Descriptor> searchCriteria);
    
  /**
   * Allow dynamic additions to this service registry.
   *
   * @return a {@link DynamicBinderFactory} instance to add services after
   * this services has been initialized.
   *
  DynamicBinderFactory bindDynamically();
   */
    
  /**
   * Retrieve the collection of all registered bindings in this, and only this,
   * Services instance.
   * 
   * @return a non-null collection of service bindings
   *
   * JRW removed since I think this is just getAllServiceProviders with a true filter
  List<Descriptor> getDeclaredBindings();
   */
    
  /**
   * Retrieve the collection of registered bindings in this, and only this, Services
   * instance that match the {@link Descriptor} argument.
   * 
   * <p/>
   * A {@link Descriptor} matches if it's attributes are equal, or specified as null.
   * A Descriptor with all null attributes will therefore match all services in this
   * services registry.
   * 
   * @param descriptor the descriptor used for matching, or null for all
   * @return a non-null collection of service bindings matching the argument
   *
   * JRW removed since I think this is just getAllServiceProviders
  List<Descriptor> getDeclaredBindings(Filter descriptor);
   */
  
  /**
   * Returns the name of this ServiceLocator
   * @return The name of this ServiceLocator, will not return null
   */
  public String getName();
  
  /**
   * This method will shutdown every service associated with this ServiceLocator.
   * Those services that have a preDestroy shall have their preDestroy called
   */
  public void shutdown();

}


