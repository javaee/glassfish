/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.proxiableshared;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.operation.OperationHandle;
import org.glassfish.hk2.extras.operation.OperationManager;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * @author jwells
 *
 */
public class ForeignActiveDescriptor<T> extends AbstractActiveDescriptor<T> {
    private final OperationManager operationManager;
    
    private final Class<?> implClass;
    private final Descriptor baseDescriptor;
    
    private final Map<ServiceLocator, ActiveDescriptor<T>> multiplexor = new HashMap<ServiceLocator, ActiveDescriptor<T>>();
    
    public ForeignActiveDescriptor(OperationManager manager, ServiceLocator childLocator, ActiveDescriptor<T> foreignDescriptor) {
        super(
                foreignDescriptor.getContractTypes(),
                foreignDescriptor.getScopeAnnotation(),
                foreignDescriptor.getName(),
                foreignDescriptor.getQualifierAnnotations(),
                foreignDescriptor.getDescriptorType(),
                DescriptorVisibility.LOCAL,
                foreignDescriptor.getRanking(),
                foreignDescriptor.isProxiable(),
                foreignDescriptor.isProxyForSameScope(),
                foreignDescriptor.getClassAnalysisName(),
                foreignDescriptor.getMetadata());
        
        setScopeAsAnnotation(foreignDescriptor.getScopeAsAnnotation());
        setReified(true);
        
        implClass = foreignDescriptor.getImplementationClass();
        baseDescriptor = new DescriptorImpl(foreignDescriptor);
        
        this.operationManager = manager;
        
        multiplexor.put(childLocator, foreignDescriptor);
    }
    
    public synchronized void addSimilarChild(ServiceLocator locator, ActiveDescriptor<T> foreignDescriptor) {
        if (multiplexor.containsKey(locator)) {
            throw new IllegalStateException("We already have this descriptor for locator " + locator);
        }
        
        Descriptor d = new DescriptorImpl(foreignDescriptor);
        if (!d.equals(baseDescriptor)) {
            throw new IllegalArgumentException("The descriptor " + foreignDescriptor + " does not match the base descriptor " + baseDescriptor);
        }
        
        multiplexor.put(locator, foreignDescriptor);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
     */
    @Override
    public Class<?> getImplementationClass() {
        return implClass;
    }

    @Override
    public Type getImplementationType() {
        return implClass;
    }
    
    @Override
    public String getImplementation() {
        return baseDescriptor.getImplementation();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public synchronized T create(ServiceHandle<?> root) {
        OperationHandle<ReqScoped> handle = operationManager.getCurrentOperation(ReqScopedImpl.REQ_SCOPED);
        if (handle == null) {
            throw new IllegalStateException("no current operation on the thread");
        }
        
        ServiceLocator childLocator = (ServiceLocator) handle.getOperationData();
        ActiveDescriptor<T> foreignDescriptor = multiplexor.get(childLocator);
        
        ServiceHandle<T> serviceHandle = childLocator.getServiceHandle(foreignDescriptor);
        return serviceHandle.getService();
    }
}
