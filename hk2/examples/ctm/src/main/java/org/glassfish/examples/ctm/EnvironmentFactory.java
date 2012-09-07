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
package org.glassfish.examples.ctm;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
@Service @Singleton
public class EnvironmentFactory implements Factory<Environment> {
    private final HashMap<String, ServiceLocator> backingLocators = new HashMap<String, ServiceLocator>();
    
    @Inject
    private TenantManager manager;

    @Inject
    TenantLocatorGenerator generator;

    /**
     * This method creates environments based on the current tenant.
     * Each tenant will have a backing ServiceLocator.  It is not the
     * job of the factory to keep track of the items it produces, that
     * will be done by the scoped context
     */
    @TenantScoped
    public Environment provide() {
        ServiceLocator locator = getCurrentLocator();
        
        return locator.getService(Environment.class);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Factory#dispose(java.lang.Object)
     */
    @Override
    public void dispose(Environment instance) {
        // No disposal in this case
        
    }
    
    private ServiceLocator getCurrentLocator() {
        if (manager.getCurrentTenant() == null) throw new IllegalStateException("There is no current tenant");
        
        ServiceLocator locator = backingLocators.get(manager.getCurrentTenant());
        if (locator == null) {
            locator = createNewLocator();
            backingLocators.put(manager.getCurrentTenant(), locator);
        }
        
        return locator;
    }
    
    private ServiceLocator createNewLocator() {
        return generator.generateLocatorPerTenant(manager.getCurrentTenant());
    }

}
