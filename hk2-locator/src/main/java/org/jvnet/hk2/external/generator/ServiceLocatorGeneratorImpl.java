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
package org.jvnet.hk2.external.generator;

import javax.inject.Singleton;

import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extension.ServiceLocatorGenerator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.internal.DefaultClassAnalyzer;
import org.jvnet.hk2.internal.DynamicConfigurationImpl;
import org.jvnet.hk2.internal.DynamicConfigurationServiceImpl;
import org.jvnet.hk2.internal.InstantiationServiceImpl;
import org.jvnet.hk2.internal.ServiceLocatorImpl;
import org.jvnet.hk2.internal.ServiceLocatorRuntimeImpl;
import org.jvnet.hk2.internal.Utilities;

/**
 * @author jwells
 *
 */
public class ServiceLocatorGeneratorImpl implements ServiceLocatorGenerator {
    private ServiceLocatorImpl initialize(String name, ServiceLocator parent) {
        if (parent != null && !(parent instanceof ServiceLocatorImpl)) {
            throw new AssertionError("parent must be a " + ServiceLocatorImpl.class.getName() +
                    " instead it is a " + parent.getClass().getName());
        }
        
        ServiceLocatorImpl sli = new ServiceLocatorImpl(name, (ServiceLocatorImpl) parent);
        
        DynamicConfigurationImpl dci = new DynamicConfigurationImpl(sli);
        
        // The service locator itself
        dci.bind(Utilities.getLocatorDescriptor(sli));
        
        // The injection resolver for three thirty
        dci.addActiveDescriptor(Utilities.getThreeThirtyDescriptor(sli));
        
        // The dynamic configuration utility
        dci.bind(BuilderHelper.link(DynamicConfigurationServiceImpl.class, false).
                to(DynamicConfigurationService.class).
                in(Singleton.class.getName()).
                localOnly().
                build());
        
        dci.bind(BuilderHelper.createConstantDescriptor(
                new DefaultClassAnalyzer(sli)));
        
        dci.bind(BuilderHelper.createDescriptorFromClass(ServiceLocatorRuntimeImpl.class));
        
        dci.bind(BuilderHelper.createConstantDescriptor(
                new InstantiationServiceImpl()));
        
        dci.commit();
        
        return sli;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extension.ServiceLocatorGenerator#create(java.lang.String, org.glassfish.hk2.api.Module)
     */
    @Override
    public ServiceLocator create(String name, ServiceLocator parent) {
        ServiceLocatorImpl retVal = initialize(name, parent);
        
        return retVal;
    }

}
