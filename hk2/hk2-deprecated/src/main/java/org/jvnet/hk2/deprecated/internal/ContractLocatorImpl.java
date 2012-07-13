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
package org.jvnet.hk2.deprecated.internal;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.glassfish.hk2.ContractLocator;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.Scope;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.deprecated.utilities.Utilities;

/**
 * @author jwells
 * @param <T> The type of object to be returned from the Provider
 *
 */
@Deprecated
public class ContractLocatorImpl<T> implements ContractLocator<T> {
    private final ServiceLocator locator;
    private final String contractName;
    
    private final String name;
    private final Scope scope;
    private final Set<Class<? extends Annotation>> qualifiers = new HashSet<Class<? extends Annotation>>();
    
    /**
     * Creates a contract locator from the information given
     * 
     * @param locator The locator to use to fetch the implementations or providers
     * @param contractName The name of the contract to search for
     */
    public ContractLocatorImpl(ServiceLocator locator,
            String contractName) {
        this(locator, contractName, null, null, null, null);
    }
    
    private ContractLocatorImpl(ServiceLocator locator,
            String contractName,
            String name,
            Scope scope,
            Set<Class<? extends Annotation>> qualifiers,
            Class<? extends Annotation> qualifier) {
        this.locator = locator;
        this.contractName = contractName;
        this.name = name;
        this.scope = scope;
        
        if (qualifiers != null) {
            this.qualifiers.addAll(qualifiers);
        }
        
        if (qualifier != null) {
            qualifiers.add(qualifier);
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.Providers#all()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<Provider<T>> all() {
        List<ActiveDescriptor<?>> all = locator.getDescriptors(new ContractLocatorFilter(contractName, name, scope, qualifiers));
        
        List<Provider<T>> retVal = new LinkedList<Provider<T>>();
        for (ActiveDescriptor<?> desc : all) {
            Provider<T> provider = Utilities.getInhabitantFromActiveDescriptor((ActiveDescriptor<T>) desc, locator);
            
            retVal.add(provider);
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.Providers#getProvider()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Provider<T> getProvider() {
        ActiveDescriptor<T> best = (ActiveDescriptor<T>)
                locator.getBestDescriptor(new ContractLocatorFilter(contractName, name, scope, qualifiers));
        
        return Utilities.getInhabitantFromActiveDescriptor(best, locator);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.Providers#get()
     */
    @Override
    public T get() {
        throw new UnsupportedOperationException("all in ContractLocatorImpl");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.Providers#getByType(java.lang.Class)
     */
    @Override
    public <U> U getByType(Class<U> type) {
        throw new UnsupportedOperationException("all in ContractLocatorImpl");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.ContractLocator#named(java.lang.String)
     */
    @Override
    public ContractLocator<T> named(String name) {
        return new ContractLocatorImpl<T>(locator, contractName, name, scope, qualifiers, null);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.ContractLocator#in(org.glassfish.hk2.Scope)
     */
    @Override
    public ContractLocator<T> in(Scope scope) {
        return new ContractLocatorImpl<T>(locator, contractName, name, scope, qualifiers, null);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.ContractLocator#annotatedWith(java.lang.Class)
     */
    @Override
    public ContractLocator<T> annotatedWith(Class<? extends Annotation> annotation) {
        return new ContractLocatorImpl<T>(locator, contractName, name, scope, qualifiers, annotation);
    }

}
