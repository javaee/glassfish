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
package org.glassfish.hk2.internal;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import org.glassfish.hk2.api.Scope;
import org.glassfish.hk2.api.DescriptorFilter;
import org.glassfish.hk2.utilities.DescriptorBuilder;

/**
 * This is a simple implementation of the {@link DescriptorBuilder}
 * 
 * @author jwells
 */
public class DescriptorBuilderImpl implements DescriptorBuilder {
	private final HashSet<String> names = new HashSet<String>();
	private final HashSet<String> contracts = new HashSet<String>();
	private final HashSet<String> scopes = new HashSet<String>();
	private final HashSet<String> qualifiers = new HashSet<String>();
	private final HashMap<String, List<String>> metadatas = new HashMap<String, List<String>>();
	private final HashSet<String> implementations = new HashSet<String>();
	private Long id;
	
	public DescriptorBuilderImpl() {
	}
	
	public DescriptorBuilderImpl(String implementation) {
		implementations.add(implementation);
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.utilities.DescriptorBuilder#named(java.lang.String)
	 */
	@Override
	public DescriptorBuilder named(String name) throws IllegalArgumentException {
		if (name == null || names.size() >= 1) {
			throw new IllegalArgumentException();
		}
		
		names.add(name);
		
		return this;
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.utilities.DescriptorBuilder#withContract(java.lang.Class)
	 */
	@Override
	public DescriptorBuilder withContract(Class<?> contract)
			throws IllegalArgumentException {
		if (contract == null) throw new IllegalArgumentException();
		
		return withContract(contract.getName());
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.utilities.DescriptorBuilder#withContract(java.lang.String)
	 */
	@Override
	public DescriptorBuilder withContract(String contract)
			throws IllegalArgumentException {
		if (contract == null) throw new IllegalArgumentException();
		
		contracts.add(contract);
		
		return this;
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.utilities.DescriptorBuilder#in(org.glassfish.hk2.Scope)
	 */
	@Override
	public DescriptorBuilder in(Class<? extends Scope> scope) throws IllegalArgumentException {
		if (scope == null || scopes.size() >= 1) {
			throw new IllegalArgumentException();
		}
		
		return in(scope.getName());
	}
	
	/* (non-Javadoc)
   * @see org.glassfish.hk2.utilities.DescriptorBuilder#in(java.lang.String)
   */
  @Override
  public DescriptorBuilder in(String scope) throws IllegalArgumentException {
    if (scope == null || scopes.size() >= 1) {
      throw new IllegalArgumentException();
    }
    
    scopes.add(scope);
    return this;
  }

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.utilities.DescriptorBuilder#annotatedBy(java.lang.Class)
	 */
	@Override
	public DescriptorBuilder annotatedBy(Class<? extends Annotation> annotation)
			throws IllegalArgumentException {
		if (annotation == null) throw new IllegalArgumentException();
		
		return annotatedBy(annotation.getName());
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.utilities.DescriptorBuilder#annotatedBy(java.lang.String)
	 */
	@Override
	public DescriptorBuilder annotatedBy(String annotation)
			throws IllegalArgumentException {
		if (annotation == null) throw new IllegalArgumentException();
		
		qualifiers.add(annotation);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.utilities.DescriptorBuilder#with(java.lang.String, java.lang.String)
	 */
	@Override
	public DescriptorBuilder has(String key, String value)
			throws IllegalArgumentException {
		if (key == null || value == null) {
			throw new IllegalArgumentException();
		}
		
		LinkedList<String> values = new LinkedList<String>();
		values.add(value);
		
		return has(key, values);
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.utilities.DescriptorBuilder#with(java.lang.String, java.util.List)
	 */
	@Override
	public DescriptorBuilder has(String key, List<String> values)
			throws IllegalArgumentException {
		if (key == null || values == null || values.size() <= 0) {
			throw new IllegalArgumentException();
		}
		
		metadatas.put(key, values);
		
		return this;
	}
	
	/* (non-Javadoc)
   * @see org.glassfish.hk2.utilities.DescriptorBuilder#id(java.lang.Long)
   */
	@Override
	public DescriptorBuilder id(Long id) throws IllegalArgumentException {
		if (this.id != null) throw new IllegalArgumentException();
		
		this.id = id;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.utilities.DescriptorBuilder#build()
	 */
	@Override
	public DescriptorFilter build() throws IllegalArgumentException {
		return new DescriptorImpl(
				contracts,
				names,
				scopes,
				implementations,
				metadatas,
				qualifiers,
				id);
	}
}
