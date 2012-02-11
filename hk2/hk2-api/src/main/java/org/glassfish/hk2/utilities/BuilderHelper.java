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

import javax.inject.Provider;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.OrFilter;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.internal.DescriptorBuilderImpl;
import org.glassfish.hk2.internal.DescriptorOrFilterImpl;
import org.glassfish.hk2.internal.StarFilter;

/**
 * This class is used to generate DescriptorBuilders to be used
 * as a simple mechanism to create a Filter or Descriptor.
 */
public class BuilderHelper {
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
	 * 
	 * @return A {@link DescriptorBuilder} that can be used to further build up the
	 * {@link Descriptor}
	 * @throws IllegalArgumentException if implementationClass is null
	 */
	public static DescriptorBuilder link(String implementationClass) throws IllegalArgumentException {
		if (implementationClass == null) throw new IllegalArgumentException();
		
		return new DescriptorBuilderImpl(implementationClass);
	}
	
	/**
	 * This method links an implementation class with a {@link DescriptorBuilder}, to
	 * be used to further build the {@link Descriptor}.
	 * 
	 * @param implementationClass The implementation class to be associated
	 * with the {@link DescriptorBuilder}.
	 * @return A {@link DescriptorBuilder} that can be used to further build up the
	 * {@link Descriptor}
	 * @throws IllegalArgumentException if implementationClass is null
	 */
	public static DescriptorBuilder link(Class<?> implementationClass) throws IllegalArgumentException {
		if (implementationClass == null) throw new IllegalArgumentException();
		
		return link(implementationClass.getName());
	}
	
	/**
	 * This method returns a filter that allows for doing a logical OR of descriptors.  If any of the
	 * filters given in the input parameters matches the descriptor passed in then the matches method
	 * will return true.  Otherwise, it will return false
	 * 
	 * @param d1 A descriptor that should be compared with the OR opererator
	 * @return A filter that can be used in complex expressions
	 * @throws IllegalArgumentException
	 */
	public static OrFilter<Descriptor> orFilter(Filter<Descriptor>... d1) throws IllegalArgumentException {
		return new DescriptorOrFilterImpl(d1);
	}
	
	/**
	 * Returns a filter that matches all types of the given class
	 * 
	 * @param ofClass The type of class that this filter should be
	 * @return A filter that matches all instances
	 */
	public static <T> Filter<T> allFilter(Class<T> ofClass) {
	  return new StarFilter<T>();
	}
	
	/**
	 * Returns a filter of type Descriptor that matches
	 * all descriptors
	 * 
	 * @return A filter that matches all descriptors
	 */
	public static Filter<Descriptor> allFilter() {
	  return StarFilter.getDescriptorFilter();
	}
	
	/**
	 * This creates a constant provider that can be used in Singleton scopes, and
	 * which will always return the given instance in the get method
	 * 
	 * @param ofType The type to be used for the returned provider
	 * @param instance An instance of a JSR-330 provider
	 * @return
	 */
	public static <T> Provider<T> createConstantProvider(Class<T> ofType, final T instance) {
	  return new Provider<T>() {

      @Override
      public T get() {
        return instance;
      }
	    
	  };
	  
	  
	}
}
