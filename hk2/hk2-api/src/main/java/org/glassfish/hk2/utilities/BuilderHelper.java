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
package org.glassfish.hk2.utilities;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.internal.ConstantActiveDescriptor;
import org.glassfish.hk2.internal.DescriptorBuilderImpl;
import org.glassfish.hk2.internal.IndexedFilterImpl;
import org.glassfish.hk2.internal.ReflectionHelper;
import org.glassfish.hk2.internal.StarFilter;

/**
 * This class is used to generate DescriptorBuilders to be used
 * as a simple mechanism to create a Filter or Descriptor.
 */
public class BuilderHelper {
    /**
     * Returns an indexed filter that will return all descriptors that
     * have contract as an advertised contract
     * 
     * @param contract The advertised contract to look for
     * @return The indexed filter that can be used to calls to ServiceLocator methods
     */
    public static IndexedFilter createContractFilter(String contract) {
        return new IndexedFilterImpl(contract, null);
    }
    
    /**
     * Returns an indexed filter that will return all descriptors that
     * have the given name
     * 
     * @param name The name to look for
     * @return The indexed filter that can be used to calls to ServiceLocator methods
     */
    public static IndexedFilter createNameFilter(String name) {
        return new IndexedFilterImpl(null, name);
    }
    
    /**
     * Returns an indexed filter that will return all descriptors that
     * have the given name and given contract
     * 
     * @param contract The advertised contract to look for
     * @param name The name to look for
     * @return The indexed filter that can be used to calls to ServiceLocator methods
     */
    public static IndexedFilter createNameAndContractFilter(String contract, String name) {
        return new IndexedFilterImpl(contract, name);
    }
    
	/**
	 * This method generates a {@link DescriptorBuilder} without a specific
	 * implementation class, useful in lookup operations
	 * 
	 * @return A {@link DescriptorBuilder} that can be used to further
	 * build up the {@link Descriptor}
	 */
	public static DescriptorBuilder link() {
		return new DescriptorBuilderImpl();
	}
	
	/**
     * This method links an implementation class with a {@link DescriptorBuilder}, to
     * be used to further build the {@link Descriptor}.
     * 
     * @param implementationClass The fully qualified name of the implementation
     * class to be associated with the PredicateBuilder.
     * @param addToContracts if true, this implementation class will be added to the
     * list of contracts
     * 
     * @return A {@link DescriptorBuilder} that can be used to further build up the
     * {@link Descriptor}
     * @throws IllegalArgumentException if implementationClass is null
     */
    public static DescriptorBuilder link(String implementationClass, boolean addToContracts) throws IllegalArgumentException {
        if (implementationClass == null) throw new IllegalArgumentException();
        
        return new DescriptorBuilderImpl(implementationClass, addToContracts);
    }
	
	/**
	 * This method links an implementation class with a {@link DescriptorBuilder}, to
	 * be used to further build the {@link Descriptor}.  This method will automatically
	 * put the implementationClass into the list of advertised contracts.
	 * 
	 * @param implementationClass The fully qualified name of the implementation
	 * class to be associated with the PredicateBuilder.
	 * 
	 * @return A {@link DescriptorBuilder} that can be used to further build up the
	 * {@link Descriptor}
	 * @throws IllegalArgumentException if implementationClass is null
	 */
	public static DescriptorBuilder link(String implementationClass) throws IllegalArgumentException {
	    return link(implementationClass, true);
	}
	
	/**
     * This method links an implementation class with a {@link DescriptorBuilder}, to
     * be used to further build the {@link Descriptor}.
     * If the class is annotated with &#86;Named, it will also automatically fill in
     * the name field of the descriptor.  If the class implements {@link Factory} then
     * the name will automatically be filled in with the fully qualified class name
     * of the actual type that the factory produces.  If the class has both
     * &#86;Named and is a Factory and the value of the &;Named annotation does not match
     * the value in &#86;Named then this method will throw an IllegalArgumentException
     * 
     * @param implementationClass The implementation class to be associated
     * with the {@link DescriptorBuilder}.
     * @param addToContracts true if this impl class should be automatically added to
     * the list of contracts
     * @param getName true if the name should be automatically added (if it is there)
     * @return A {@link DescriptorBuilder} that can be used to further build up the
     * {@link Descriptor}
     * @throws IllegalArgumentException if implementationClass is null
     */
    public static DescriptorBuilder link(Class<?> implementationClass, boolean addToContracts, boolean getName) throws IllegalArgumentException {
        if (implementationClass == null) throw new IllegalArgumentException();
        
        DescriptorBuilder builder = link(implementationClass.getName(), addToContracts);
        
        if (getName) {
            String name = ReflectionHelper.getName(implementationClass);
            if (name != null) {
                builder = builder.named(name);
            }
        }
        
        return builder;
    }
	
	/**
     * This method links an implementation class with a {@link DescriptorBuilder}, to
     * be used to further build the {@link Descriptor}.
     * If the class is annotated with &#86;Named, it will also automatically fill in
     * the name field of the descriptor.  If the class implements {@link Factory} then
     * the name will automatically be filled in with the fully qualified class name
     * of the actual type that the factory produces.  If the class has both
     * &#86;Named and is a Factory and the value of the &;Named annotation does not match
     * the value in &#86;Named then this method will throw an IllegalArgumentException
     * 
     * @param implementationClass The implementation class to be associated
     * with the {@link DescriptorBuilder}.
     * @param addToContracts true if this impl class should be automatically added to
     * the list of contracts
     * @return A {@link DescriptorBuilder} that can be used to further build up the
     * {@link Descriptor}
     * @throws IllegalArgumentException if implementationClass is null
     */
    public static DescriptorBuilder link(Class<?> implementationClass, boolean addToContracts) throws IllegalArgumentException {
        return link(implementationClass, addToContracts, true);
    }
	
	/**
	 * This method links an implementation class with a {@link DescriptorBuilder}, to
	 * be used to further build the {@link Descriptor}.  If this class is not a factory
	 * this method will put the name of the implementation class into the list of advertised
	 * contracts.  If the class is annotated with &#86;Named, it will also automatically fill
	 * in the name field of the descriptor.  If this class is a factory it will automatically
	 * put {@link Factory} into the list of advertised contracts.
	 * 
	 * @param implementationClass The implementation class to be associated
	 * with the {@link DescriptorBuilder}.
	 * @return A {@link DescriptorBuilder} that can be used to further build up the
	 * {@link Descriptor}
	 * @throws IllegalArgumentException if implementationClass is null
	 */
	public static DescriptorBuilder link(Class<?> implementationClass) throws IllegalArgumentException {
	    if (implementationClass == null) throw new IllegalArgumentException();
	    
	    boolean isFactory = (Factory.class.isAssignableFrom(implementationClass));
	    
	    DescriptorBuilder db = link(implementationClass, !isFactory);
	    
	    if (isFactory) {
	        db = db.to(Factory.class);
	    }
	    
	    return db;
	}
	
	/**
     * This method links an a factory with a {@link DescriptorBuilder}, to
     * be used to further build the {@link Descriptor}.  This method will
     * NOT put this implementation class into the list of advertised contracts.
     * The {@link Descriptor} that is produced from this should describe what
     * the factory creates, not the factory itself.  Remember that the factory
     * itself must also be bound into the {@link Module}.
     * 
     * @param implementationClass The fully qualified name of the implementation
     * class to be associated with the PredicateBuilder.
     * 
     * @return A {@link DescriptorBuilder} that can be used to further build up the
     * {@link Descriptor}
     * @throws IllegalArgumentException if implementationClass is null
     */
    public static DescriptorBuilder linkFactory(Class<?> factoryClass) throws IllegalArgumentException {
        return link(factoryClass, false, false);
    }
    
    /**
     * This creates a descriptor that will always return the given object.  The
     * set of types in the advertised contracts will contain the class of the
     * constant and all interfaces implemented by the constant (including those
     * not marked with &86;Contract).
     * 
     * @param constant The non-null constant that should always be returned from
     * the create method of this ActiveDescriptor.  
     * @return The descriptor can be used in calls to Configuration.addActiveDescriptor
     */
    public static ActiveDescriptor<?> createConstantDescriptor(Object constant) {
        if (constant == null) throw new IllegalArgumentException();
        
        return new ConstantActiveDescriptor<Object>(
                constant,
                ReflectionHelper.getAdvertisedTypesFromObject(constant),
                ReflectionHelper.getScopeFromObject(constant),
                ReflectionHelper.getName(constant.getClass()),
                ReflectionHelper.getQualifiersFromObject(constant));
    }
	
	/**
	 * Returns a filter of type Descriptor that matches
	 * all descriptors
	 * 
	 * @return A filter that matches all descriptors
	 */
	public static Filter allFilter() {
	  return StarFilter.getDescriptorFilter();
	}
}
