/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package com.mallory.application;

import javax.inject.Inject;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;

import com.alice.application.AliceApp;

/**
 * This naughty service will attempt to do many things it is not allowed to do
 * 
 * @author jwells
 *
 */
@Service
public class MalloryApp {
    @Inject
    private AliceApp alice;
    
    @Inject
    private ServiceLocator locator;
    
    /**
     * Mallory is allowed to call the AliceApp
     */
    public void doAnApprovedOperation() {
        alice.doAuditedService("Mallory");
    }
    
    /**
     * Mallory cannot however lookup the AuditService himself!
     */
    public void tryToGetTheAuditServiceMyself() {
        Object auditService = locator.getBestDescriptor(BuilderHelper.createContractFilter("org.acme.security.AuditService"));
        auditService.toString();  // This should NPE!
    }
    
    /**
     * Mallory cannot advertise the EvilService
     */
    public void tryToAdvertiseAService() {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(EvilService.class);
        
        config.commit();  // This will throw a MultiException
    }
    
    /**
     * Mallory cannot un-advertise ServiceLocator!
     */
    public void tryToUnAdvertiseAService() {
        final Descriptor locatorService = locator.getBestDescriptor(BuilderHelper.createContractFilter(ServiceLocator.class.getName()));
        
        // This filter matches ServiceLocator itself!
        Filter unbindFilter = new Filter() {

            @Override
            public boolean matches(Descriptor d) {
                if (d.getServiceId().equals(locatorService.getServiceId())) {
                    if (d.getLocatorId().equals(locator.getLocatorId())) {
                        return true;
                    }
                }
                
                return false;
            }
            
        };
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addUnbindFilter(unbindFilter);
        
        config.commit();  // This will throw a MultiException
    }
    
    /**
     * Tries to instantiate a service with an illegal injection point
     */
    public void tryToInstantiateAServiceWithABadInjectionPoint() {
        locator.getService(EvilInjectedService.class);  // Will throw MultiException
    }
}
