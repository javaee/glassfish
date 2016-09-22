/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2016 Oracle and/or its affiliates. All rights reserved.
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

/**
 * This class is used to add {@link Descriptor}s to a {@link ServiceLocator}
 * instance.  No operation performed on this object will be reflected
 * in the {@link ServiceLocator} until the commit method is called.
 * 
 * @author jwells
 *
 */
public interface DynamicConfiguration {
    /**
     * This method will bind the given descriptor to this Module.
     * If the descriptor given is not an ActiveDescriptor then a
     * non-reified ActiveDescriptor will be returned with the system
     * provided fields set.  If the descriptor given is a reified
     * ActiveDescriptor then the descriptor returned will be a
     * reified ActiveDescriptor that takes all values except for the
     * id from the given descriptor.  A deep copy will be made
     * of the incoming descriptor
     * 
     * @param key May not be null.  Will be used to derive the various
     * key fields associated with the given provider
     * @return The entry as added to the service registry, with fields
     * of the Descriptor filled in by the system as appropriate
     * @throws IllegalArgumentException if there is an error in the key
     */
    public <T> ActiveDescriptor<T> bind(Descriptor key);
    
    /**
     * This method will bind the given descriptor to this Module.
     * If the descriptor given is not an ActiveDescriptor then a
     * non-reified ActiveDescriptor will be returned with the system
     * provided fields set.  If the descriptor given is a reified
     * ActiveDescriptor then the descriptor returned will be a
     * reified ActiveDescriptor that takes all values except for the
     * id from the given descriptor.
     * 
     * @param key May not be null.  Will be used to derive the various
     * key fields associated with the given provider
     * @param requiresDeepCopy If true a deep copy will be made of the
     * key.  If false then the Descriptor will be used as is, and it
     * is the responsibility of the caller to ensure that the fields
     * of the Descriptor never change (with the exception of any
     * writeable fields, such as ranking)
     * @return The entry as added to the service registry, with fields
     * of the Descriptor filled in by the system as appropriate
     * @throws IllegalArgumentException if there is an error in the key
     */
    public <T> ActiveDescriptor<T> bind(Descriptor key, boolean requiresDeepCopy);
    
    /**
     * This method will bind the descriptors found in the
     * {@link FactoryDescriptors}.  This method will first
     * validate the descriptors from the {@link FactoryDescriptors}
     * and then simply bind them into this configuration as
     * two independent descriptors.  A deep copy will be made
     * of both descriptors
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
     * This method will bind the descriptors found in the
     * {@link FactoryDescriptors}.  This method will first
     * validate the descriptors from the {@link FactoryDescriptors}
     * and then simply bind them into this configuration as
     * two independent descriptors.  A deep copy will be made
     * of both descriptors
     * 
     * @param factoryDescriptors A description of a factory service
     * and the type the factory service provides.  May not be null
     * @param requiresDeepCopy If true a deep copy will be made of the
     * key.  If false then the Descriptor will be used as is, and it
     * is the responsibility of the caller to ensure that the fields
     * of the Descriptor never change (with the exception of any
     * writeable fields, such as ranking)
     * @return The descriptors returned from this object may be cast
     * to ActiveDescriptor and will contain all the fields of the descriptors
     * filled in by the system 
     * @throws IllegalArgumentException if there is an error in the input parameter
     */
    public FactoryDescriptors bind(FactoryDescriptors factoryDescriptors, boolean requiresDeepCopy);
    
    /**
     * This allows third party systems to add reified active descriptors to the system.
     * The active descriptor given must be fully reified (isReified must return true) and
     * the create and destroy methods must be implemented.  A deep copy will
     * be made of the descriptor
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
     * This allows third party systems to add reified active descriptors to the system.
     * The active descriptor given must be fully reified (isReified must return true) and
     * the create and destroy methods must be implemented.  A deep copy will
     * be made of the descriptor
     * 
     * @param activeDescriptor The reified active descriptor to be added to the system.  The
     * system will not attempt to reify this descriptor itself
     * @param requiresDeepCopy If true a deep copy will be made of the
     * key.  If false then the Descriptor will be used as is, and it
     * is the responsibility of the caller to ensure that the fields
     * of the Descriptor never change (with the exception of any
     * writeable fields, such as ranking)
     * @return The entry as added to the service registry, with fields
     * of the Descriptor filled in by the system as appropriate
     * @throws IllegalArgumentException if the descriptor is not reified
     */
    public <T> ActiveDescriptor<T> addActiveDescriptor(ActiveDescriptor<T> activeDescriptor, boolean requiresDeepCopy)
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
     * This adds two active descriptors to the system based completely on the analysis
     * of the given {@link Factory} class.  The {@link Factory} class itself and all
     * interfaces marked contract will be in the list of advertised services.  The scope
     * and qualifiers will be taken from the annotations on the class.  The annotations
     * on the {@link Factory#provide} method will provide the scope and qualifiers of the
     * service produced by the {@link Factory}
     * 
     * @param rawFactoryClass The class to analyze, must not be null 
     * @return The factory descriptors that have been added to the system, with
     * all fields filled in based on the rawFactoryClass
     * @throws MultiException If this class cannot be a service
     * @throws IllegalArgumentException if rawClass is null
     */
    public <T> FactoryDescriptors addActiveFactoryDescriptor(Class<? extends Factory<T>> rawFactoryClass)
            throws MultiException, IllegalArgumentException;
    
    /**
     * This filter will added to the list of filters in this Configuration that will
     * determine which Descriptors will be removed from the system.  Only services directly
     * from this Configuration objects' associated ServiceLocator will be given to this Filter
     * (it will not be given descriptors from the ServiceLocators parent).  The descriptors passed
     * into this filter may be cast to {@link ActiveDescriptor}.  The descriptors passed into this
     * filter may or may not have been reified.  This filter should not reify passed in descriptors.
     * <p>
     * And descriptor for which this filter returns true will be removed from the
     * {@link ServiceLocator} prior to any additions that are performed with this
     * Configuration object.  Hence a Configuration can remove and add a descriptor of the
     * same type in one commit.
     * <p>
     * In order to unbind a filter the caller of commit must pass the LOOKUP validators and the
     * UNBIND validators.
     * 
     * @param unbindFilter This filter will be added to the list of filters that this
     * configuration object will use to determine which descriptors to unbind from the system.
     * May not be null
     * @throws IllegalArgumentException if unbindFilter is null
     */
    public void addUnbindFilter(Filter unbindFilter) throws IllegalArgumentException;
    
    /**
     * At commit time all idempotent filters in this dynamic configuration will be run
     * against all validation-visible descriptors.  If any of the idempotent filters are
     * a match then the commit will FAIL and none of the descriptors in this DynamicConfiguration
     * will be added or removed.  The idempotent filters will be run under the same lock as the
     * commit, and hence can guarantee true idempotency of the transaction.
     * <p>
     * The normal use case for the use of this filter is to ensure that a service is only added
     * once to the {@link ServiceLocator}, even when multiple threads may be attempting to add the
     * same service
     * <p>
     * The filter passed in should not do any change to the set of descriptors itself, any attempt
     * to do so will leave the system in an inconsistent state.  {@link IndexedFilter} is supported
     * and is the normal use of an idempotent filter, though it is not required
     * 
     * @param idempotentFilter A non-null idempotent filter to use during commit.  If any descriptors
     * match the filter, the commit will fail
     * @throws IllegalArgumentException If any of the filters are null
     */
    public void addIdempotentFilter(Filter... idempotentFilter) throws IllegalArgumentException;
    
    /**
     * Registers two-phase resources in the order in which they are to run.  Subsequent calls
     * to this method will add resources at the end of the existing list
     * 
     * @param resources A list of resources to add to this dynamic configuration
     */
    public void registerTwoPhaseResources(TwoPhaseResource... resources);
    
    /**
     * This causes the configuration to get committed.  This
     * method may only be called once
     * 
     * @throws MultiException If errors were found in the commit process
     * @throws IllegalStateException if this method was called more than once
     */
    public void commit() throws MultiException;

}
