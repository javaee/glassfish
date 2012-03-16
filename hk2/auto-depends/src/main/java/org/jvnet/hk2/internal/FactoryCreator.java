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
package org.jvnet.hk2.internal;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author jwells
 *
 */
public class FactoryCreator<T> implements Creator<T> {
    private final ServiceLocator locator;
    private final Class<?> factoryClass;
    
    /* package */ FactoryCreator(ServiceLocator locator, Class<?> factoryClass) {
        this.locator = locator;
        this.factoryClass = factoryClass;
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#getInjectees()
     */
    @Override
    public List<Injectee> getInjectees() {
        return Collections.emptyList();
    }
    
    private ServiceHandle<Factory<T>> getFactoryHandle() {
        String factoryName;
        try {
            factoryName = Utilities.getFactoryName(factoryClass);
            Type factoryType = Utilities.getFactoryType(factoryClass);
            
            Type actualArgs[] = new Type[1];
            actualArgs[0] = factoryType;
            
            ServiceHandle<Factory<T>> handle = locator.getServiceHandle(new ParameterizedTypeImpl(Factory.class, actualArgs), factoryName);
            if (handle == null) {
                throw new IllegalStateException("Could not find a factory for " + factoryName);
            }
            
            return handle;
        }
        catch (Throwable th) {
            throw new MultiException(th);
        }
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public T create(ServiceHandle<?> root) throws MultiException {
        ServiceHandle<Factory<T>> handle = getFactoryHandle();
        
        Factory<T> retVal = handle.getService();
        
        handle.destroy();
        
        return retVal.provide();
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.internal.Creator#dispose(java.lang.Object, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public void dispose(T instance, ServiceHandle<?> root) {
        try {
            ServiceHandle<Factory<T>> handle = getFactoryHandle();
            
            Factory<T> factory = handle.getService();
            
            factory.dispose(instance);
        }
        catch (Throwable th) {
            // ignore
        }
    }

}
