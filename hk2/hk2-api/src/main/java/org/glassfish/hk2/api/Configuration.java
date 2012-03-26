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

import java.lang.annotation.Annotation;

/**
 * This class is used to add entities to a {@link ServiceLocator}
 * instance.
 * 
 * @author jwells
 */
public interface Configuration {
	
	/**
	 * This method will bind the given descriptor to this Module.
	 * If the descriptor given is not an ActiveDescriptor then a
	 * non-reified ActiveDescriptor will be returned with the system
	 * provided fields set.  If the descriptor given is a reified
	 * ActiveDescriptor then the descriptor returned will be a
	 * reified ActiveDescriptor that takes all values except for the
	 * id from the given descriptor
	 * 
	 * @param key May not be null.  Will be used to derive the various
	 * key fields associated with the given provider
	 * @return The entry as added to the service registry, with fields
	 * of the Descriptor filled in by the system as appropriate
	 * @throws IllegalArgumentException if there is an error in the key
	 */
	public ActiveDescriptor<?> bind(Descriptor key);
	
	/**
     * This method will bind the descriptors found in the
     * {@link FactoryDescriptors}.  This method will first
     * validate the descriptors from the {@link FactoryDescriptors}
     * and then simply bind them into this configuration as
     * two independent descriptors. 
     * 
     * @param factoryDescriptors A description of a factory service
     * and the type the factory service provides.  May not be null
     * @return The descriptors returned from this object may be cast
     * to ActiveDescriptor and will contain all the fields of the descriptors
     * filled in by the system 
     * @throws IllegalArgumentException if there is an error in the input parameter
     */
    public FactoryDescriptors bind(FactoryDescriptors factoryDescriptors);
	
	/**
	 * This adds a custom class loader to the system.  Custom class
	 * loaders will be searched in a random order, so users should not
	 * rely on the order of invocation of loaders.  The first loader to
	 * not return null shall be the one that is used, and no further loaders
	 * will be consulted.  There is a system loader that will be consulted
	 * last which uses the context class loader in order to load the classes.
	 * 
	 * @param loader The custom loader to consult when loading classes
	 */
	public void addLoader(HK2Loader loader);
	
	/**
	 * This will add an injection resolver to the system. The system will
	 * provide a default implementation that handles &#86;Inject.  However, if the
	 * user provides a resolver for &#86;Inject then that one will be used in
	 * preference to the default system implementation
	 * 
	 * @param indicator The annotation that indicates an injection point.  Must
	 * be valid for constructors, methods and fields
	 * @param resolver The resolver to use when finding instances
	 */
	public void addInjectionResolver(Class<? extends Annotation> indicator, InjectionResolver resolver);
	
	/**
	 * This gets the InjectionResolver for the given indicator currently registered
	 * with the system.  This method gets InjectionResolvers already registered with
	 * the underlying ServiceLocator, not InjectionResolvers that will be added by
	 * this Configuration object
	 * 
	 * @param indicator The annotation that indicates an injection point.  If null, null
	 * will be returned
	 * 
	 * @return The currently registered injection resolver for this indicator, or null if none
	 * are found for this indicator
	 */
	public InjectionResolver getInstalledInjectionResolver(Class<? extends Annotation> indicator);
	
	/**
	 * This allows third party systems to add reified active descriptors to the system.
	 * The active descriptor given must be fully reified (isReified must return true) and
	 * the create and destroy methods must be implemented.
	 * 
	 * @param activeDescriptor The reified active descriptor to be added to the system.  The
	 * system will not attempt to reify this descriptor itself
	 * @return The entry as added to the service registry, with fields
     * of the Descriptor filled in by the system as appropriate
	 * @throws IllegalArgumentException if the descriptor is not reified
	 */
	public <T> ActiveDescriptor<T> addActiveDescriptor(ActiveDescriptor<T> activeDescriptor)
	        throws IllegalArgumentException;
	
	/**
     * This adds an active descriptor to the system based completely on the analysis
     * of the given class.  The class itself and all interfaces marked contract will
     * be in the list of advertised services.  The scope and qualifiers will be taken
     * from the annotations on the class.
     * 
	 * @param rawClass The class to analyze, must not be null 
	 * @return The active (reified) descriptor that has been added to the system, with
	 * all fields filled in based on the rawClass
	 * @throws MultiException If this class cannot be a service
	 * @throws IllegalArgumentException if rawClass is null
     */
    public <T> ActiveDescriptor<T> addActiveDescriptor(Class<T> rawClass)
            throws MultiException, IllegalArgumentException;
	
	/**
	 * Adds a validator to the system.  This validator will be called whenver a
	 * validating class is about to be injected (or looked up).  All validators
	 * registered with the system will be invoked.  If any of them fail, the operation
	 * will fail.
	 * 
	 * @param validator The validator to add to the system.  May not be null
	 * @throws IllegalArgumentException If validator is null
	 */
	public void addValidator(InjectionPointValidator validator)
	        throws IllegalArgumentException;
}
