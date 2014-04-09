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
package org.jvnet.hk2.guice.bridge.internal;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

/**
 * @author jwells
 *
 */
public class GuiceBridgeImpl extends GuiceBridge {

    /* (non-Javadoc)
     * @see org.jvnet.hk2.guice.bridge.api.GuiceBridge#initializeGuiceBridge(org.glassfish.hk2.api.ServiceLocator)
     */
    @Override
    public void initializeGuiceBridge(ServiceLocator locator) {
        boolean addService = true;
        if (locator.getBestDescriptor(BuilderHelper.createContractFilter(GuiceIntoHK2Bridge.class.getName())) != null) {
            addService = false;
        }
        
        boolean addContext = true;
        if (locator.getBestDescriptor(BuilderHelper.createContractFilter(GuiceScopeContext.class.getName())) != null) {
            addContext = false;
        }
        
        if (!addService && !addContext) return;
        
        
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        if (dcs == null) {
            throw new IllegalStateException("This service locator has no DynamicConfiugurationService: " + locator);
        }
        
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        if (addContext) {
            config.addActiveDescriptor(GuiceScopeContext.class);
        }
        
        if (addService) {
            config.addActiveDescriptor(GuiceIntoHK2BridgeImpl.class);
        }
        
        config.commit();

    }

}
