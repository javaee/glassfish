/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.extras;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.hk2bridge.internal.Hk2BridgeImpl;
import org.glassfish.hk2.extras.interception.internal.DefaultInterceptionService;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * These are utilities for the extra features of hk2.
 * Generally they allow for 
 * @author jwells
 *
 */
public class ExtrasUtilities {
    /**
     * This will be put into the metadata of a descriptor that is bridged from another service locator.
     * The value will be the locator id of the service locator from which the service originates
     */
    public final static String HK2BRIDGE_LOCATOR_ID = "org.jvnet.hk2.hk2bridge.locator.id";
    
    /**
     * This will be put into the metadata of a descriptor that is bridged from another service locator.
     * The value will be the service id of the descriptor from which the service originates
     */
    public final static String HK2BRIDGE_SERVICE_ID = "org.jvnet.hk2.hk2bridge.service.id";
    
    /**
     * This method adds in a default implementation of the {@link org.glassfish.hk2.api.InterceptionService}
     * which uses annotations to denote which services should intercept other services.  For more
     * information see the org.glassfish.hk2.extras.interception package.  This method is
     * idempotent, if the service is already available it will not add it
     * 
     * @param locator The locator to add the default interception service implementation to.  May not be null
     */
    public static void enableDefaultInterceptorServiceImplementation(ServiceLocator locator) {
        if (locator.getBestDescriptor(BuilderHelper.createContractFilter(DefaultInterceptionService.class.getName())) == null) {
            ServiceLocatorUtilities.addClasses(locator, DefaultInterceptionService.class);
        }
    }
    
    private final static String BRIDGE_NAME_PREFIX = "LocatorBridge(";
    private final static String COMMA = ",";
    private final static String BRIDGE_NAME_POSTFIX = ")";
    
    private static String getBridgeName(ServiceLocator into, ServiceLocator from) {
        return BRIDGE_NAME_PREFIX + from.getLocatorId() + COMMA + into.getLocatorId() + BRIDGE_NAME_POSTFIX;
    }
    
    private static void checkParentage(ServiceLocator a, ServiceLocator b) {
        ServiceLocator originalA = a;
        
        while (a != null) {
            if (a.getLocatorId() == b.getLocatorId()) {
                throw new IllegalStateException("Locator " + originalA + " is a child of or is the same as locator " + b);
            }
            
            a = a.getParent();
        }
    }
    
    /**
     * This method will bridge all non-local services from the
     * from ServiceLocator into the into ServiceLocator.  Changes
     * to the set of services in the from ServiceLocator will be
     * reflected in the into ServiceLocator.  The two ServiceLocators
     * involved must not have a parent/child relationship
     * 
     * @param into The non-null ServiceLocator that will have services added
     * to it from the from ServiceLocator
     * @param from The non-null ServiceLocator that will add services to the
     * into ServiceLocator
     */
    public static void bridgeServiceLocator(ServiceLocator into, ServiceLocator from) {
        checkParentage(into, from);
        checkParentage(from, into);
        
        String bridgeName = getBridgeName(into, from);
        if (from.getService(Hk2BridgeImpl.class, bridgeName) != null) {
            throw new IllegalStateException("There is already a bridge from locator " + from.getName() + " to " + into.getName());
        }
        
        DescriptorImpl di = BuilderHelper.createDescriptorFromClass(Hk2BridgeImpl.class);
        di.setName(bridgeName);
        
        ServiceLocatorUtilities.addOneDescriptor(from, di);
        
        Hk2BridgeImpl bridge = from.getService(Hk2BridgeImpl.class, bridgeName);
        
        // Kick it off
        bridge.setRemote(into);
    }
    
    /**
     * This method will remove all non-local services from the
     * from ServiceLocator into the into ServiceLocator.  The service
     * locator will no longer be related by this bridge
     * The two ServiceLocators involved must not have a parent/child relationship
     * 
     * @param into The non-null ServiceLocator that will have services added
     * to it from the from ServiceLocator
     * @param from The non-null ServiceLocator that will add services to the
     * into ServiceLocator
     */
    public static void unbridgeServiceLocator(ServiceLocator into, ServiceLocator from) {
        checkParentage(into, from);
        checkParentage(from, into);
        
        String bridgeName = getBridgeName(into, from);
        
        ServiceHandle<Hk2BridgeImpl> handle = from.getServiceHandle(Hk2BridgeImpl.class, bridgeName);
        if (handle == null) return;
        
        handle.destroy();
        
        ServiceLocatorUtilities.removeFilter(from, BuilderHelper.createNameAndContractFilter(
                Hk2BridgeImpl.class.getName(),bridgeName));
    }

}
