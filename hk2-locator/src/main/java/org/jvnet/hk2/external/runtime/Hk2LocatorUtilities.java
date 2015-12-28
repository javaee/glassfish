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
package org.jvnet.hk2.external.runtime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.jvnet.hk2.internal.DefaultClassAnalyzer;
import org.jvnet.hk2.internal.DynamicConfigurationServiceImpl;
import org.jvnet.hk2.internal.InstantiationServiceImpl;
import org.jvnet.hk2.internal.ServiceLocatorImpl;
import org.jvnet.hk2.internal.ServiceLocatorRuntimeImpl;
import org.jvnet.hk2.internal.ThreeThirtyResolver;

/**
 * This is a utility class specific to this implementation
 * of the hk2 API
 * 
 * @author jwells
 *
 */
public class Hk2LocatorUtilities {
    private final static Filter NO_INITIAL_SERVICES_FILTER = new Filter() {
        private final List<String> INITIAL_SERVICES = Arrays.asList(new String[] {
            ServiceLocatorImpl.class.getName(),
            ThreeThirtyResolver.class.getName(),
            DynamicConfigurationServiceImpl.class.getName(),
            DefaultClassAnalyzer.class.getName(),
            ServiceLocatorRuntimeImpl.class.getName(),
            InstantiationServiceImpl.class.getName()
        });
        
        private final HashSet<String> INITIAL_SERVICE_SET = new HashSet<String>(INITIAL_SERVICES);

        @Override
        public boolean matches(Descriptor d) {
            return !INITIAL_SERVICE_SET.contains(d.getImplementation());
        }
        
    };
    
    /**
     * Returns a filter that only returns services that are not
     * in the initial set of services offered by all ServiceLocators
     * created by this implementation of hk2.  This filter
     * is guaranteed to work properly for all versions of this
     * implementation of hk2
     * 
     * @return A Filter that only returns services that are not
     * in the initial set of services offered by all ServiceLocators
     * created by this implementation of hk2
     */
    public static Filter getNoInitialServicesFilter() {
        return NO_INITIAL_SERVICES_FILTER;
        
    }

}
