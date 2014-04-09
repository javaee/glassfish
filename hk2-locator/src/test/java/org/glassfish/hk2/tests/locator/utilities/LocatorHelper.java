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
package org.glassfish.hk2.tests.locator.utilities;

import junit.framework.Assert;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;

/**
 * @author jwells
 *
 */
public class LocatorHelper {
    /** This should be thrown from negative tests */
    public final static String EXPECTED = "Expected Exception";
    
    private final static ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();
    
    /**
     * Creates an unnamed, untracked service locator
     * @return An unnamed, untracked service locator
     */
    public static ServiceLocator create() {
        return factory.create(null);
    }
    
    /**
     * Creates an unnamed, untracked service locator with the given parent
     * @param parent The non-null parent to be associated with this locator
     * @return An unnamed, untracked service locator with the given parent
     */
    public static ServiceLocator create(ServiceLocator parent) {
        return factory.create(null, parent);
    }
    
    /**
     * Will create a ServiceLocator after doing test-specific bindings from the TestModule
     * 
     * @param name The name of the service locator to create.  Should be unique per test, otherwise
     * this method will fail.
     * @param module The test module, that will do test specific bindings.  May be null
     * @return A service locator with all the test specific bindings bound
     */
    public static ServiceLocator create(String name, TestModule module) {
        return create(name, null, module);
    }
    
    /**
     * Will create a ServiceLocator after doing test-specific bindings from the TestModule
     * 
     * @param name The name of the service locator to create.  Should be unique per test, otherwise
     * this method will fail.
     * @param parent The parent locator this one should have.  May be null
     * @param module The test module, that will do test specific bindings.  May be null
     * @return A service locator with all the test specific bindings bound
     */
    public static ServiceLocator create(String name, ServiceLocator parent, TestModule module) {
        ServiceLocator retVal = factory.find(name);
        Assert.assertNull("There is already a service locator of this name, change names to ensure a clean test: " + name, retVal);
        
        retVal = factory.create(name, parent);
        
        if (module == null) return retVal;
        
        DynamicConfigurationService dcs = retVal.getService(DynamicConfigurationService.class);
        Assert.assertNotNull("Their is no DynamicConfigurationService.  Epic fail", dcs);
        
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        Assert.assertNotNull("DynamicConfiguration creation failure", dc);
        
        module.configure(dc);
        
        dc.commit();
        
        return retVal;
    }
    
    /**
     * Creates a ServiceLocator equipped with a RunLevelService and the set of classes given
     * 
     * @param classes The set of classes to also add to the descriptor (should probably contain some run level services, right?)
     * @return The ServiceLocator to use
     */
    public static ServiceLocator getServiceLocator(Class<?>... classes) {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().create(null);
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        for (Class<?> clazz : classes) {
            config.addActiveDescriptor(clazz);
        }
        
        config.commit();
        
        return locator;
    }

}
