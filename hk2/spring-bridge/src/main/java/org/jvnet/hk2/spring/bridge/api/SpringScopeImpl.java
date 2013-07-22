/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.spring.bridge.api;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * @author jwells
 *
 */
public class SpringScopeImpl implements Scope {
    private ServiceLocator locator;
    
    /**
     * Sets the service locator to use with this scope
     * @param locator The (non-null) locator to use for this scope
     */
    public synchronized void setServiceLocator(ServiceLocator locator) {
        this.locator = locator;
    }
    
    /**
     * This can be used to configure the name of the service locator
     * to use
     * 
     * @param name The name to be used.  If null an anonymous service
     * locator will be used
     */
    public synchronized void setServiceLocatorName(String name) {
        locator = ServiceLocatorFactory.getInstance().create(name);
    }
    
    /**
     * Returns the {@link ServiceLocator} associated with this
     * scope
     * 
     * @return The {@link ServiceLocator} to be used with this scope
     */
    public synchronized ServiceLocator getServiceLocator() {
        return locator;
    }
    
    private synchronized ServiceHandle<?> getServiceFromName(String id) {
        if (locator == null) throw new IllegalStateException(
                "ServiceLocator must be set");
        
        ActiveDescriptor<?> best = locator.getBestDescriptor(BuilderHelper.createTokenizedFilter(id));
        if (best == null) return null;
        
        return locator.getServiceHandle(best);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#get(java.lang.String, org.springframework.beans.factory.ObjectFactory)
     */
    @Override
    public Object get(String contractName, ObjectFactory<?> factory) {
        ServiceHandle<?> serviceHandle = getServiceFromName(contractName);
        if (serviceHandle == null) return factory.getObject();
        
        return serviceHandle.getService();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#getConversationId()
     */
    @Override
    public String getConversationId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String, java.lang.Runnable)
     */
    @Override
    public void registerDestructionCallback(String arg0, Runnable arg1) {
        // TODO Not sure what to do with this

    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
     */
    @Override
    public Object remove(String arg0) {
        // TODO:  One possibility is to truly keep the handles
        
        ServiceHandle<?> handle = getServiceFromName(arg0);
        if (handle == null) return null;
        
        handle.destroy();
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#resolveContextualObject(java.lang.String)
     */
    @Override
    public Object resolveContextualObject(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
