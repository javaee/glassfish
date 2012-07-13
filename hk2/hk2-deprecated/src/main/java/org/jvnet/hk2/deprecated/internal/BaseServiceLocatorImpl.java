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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.component.BaseServiceLocator;
import org.jvnet.hk2.component.ComponentException;

/**
 * This will be added in GlassFish to help keep the porting of
 * new HK2 down to a reasonable size
 * 
 * @author jwells
 *
 */
@Deprecated
public class BaseServiceLocatorImpl implements BaseServiceLocator {
    private final ServiceLocator locator = ServiceLocatorFactory.getInstance().create("default");

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.BaseServiceLocator#getComponent(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getComponent(Class<T> contract, String name)
            throws ComponentException {
        return (T) locator.getService(contract, name);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.BaseServiceLocator#getComponent(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getComponent(String fullQualifiedName, String name) {
        IndexedFilter iff = BuilderHelper.createNameAndContractFilter(fullQualifiedName, name);
        ActiveDescriptor<T> best = (ActiveDescriptor<T>) locator.getBestDescriptor(iff);
        if (best == null) return null;
        
        ServiceHandle<T> handle = locator.getServiceHandle(best);
        return handle.getService();
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.BaseServiceLocator#getComponent(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getComponent(Class<T> clazz) throws ComponentException {
        return (T) locator.getService(clazz);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.BaseServiceLocator#getByType(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getByType(Class<T> implType) {
        return (T) locator.getService(implType);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.BaseServiceLocator#getByType(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getByType(String implType) {
        return (T) getComponent(implType, null);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.BaseServiceLocator#getByContract(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getByContract(Class<T> contractType) {
        return (T) locator.getService(contractType);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.BaseServiceLocator#getByContract(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getByContract(String contractType) {
        Object retVal = getComponent(contractType, null);
        return (T) retVal;
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.BaseServiceLocator#getAllByContract(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Collection<T> getAllByContract(Class<T> contractType) {
        return (Collection<T>) locator.getAllServices(contractType);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.BaseServiceLocator#getAllByContract(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Collection<T> getAllByContract(String contractType) {
        IndexedFilter iff = BuilderHelper.createContractFilter(contractType);
        List<ActiveDescriptor<?>> descriptors = locator.getDescriptors(iff);
        
        List<T> retVal = new LinkedList<T>();
        
        for (ActiveDescriptor<?> descriptor : descriptors) {
            ServiceHandle<?> handle = locator.getServiceHandle(descriptor);
            
            retVal.add((T) handle.getService());
        }
        
        return retVal;
    }

}
