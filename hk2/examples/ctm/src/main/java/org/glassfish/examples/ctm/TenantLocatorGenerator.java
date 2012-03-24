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

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * This is the class that creates the service locator for each
 * known tenant
 * <p>
 * TODO:  Each of these services should instead be backed by
 * their configured instances, not what I'm doing here
 * 
 * @author jwells
 */
public class TenantLocatorGenerator {
    private final static ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();
    public final static String ALICE = "Alice";
    public final static int ALICE_MIN = 1;
    public final static int ALICE_MAX = 2;
    
    public final static String BOB = "Bob";
    public final static int BOB_MIN = 10;
    public final static int BOB_MAX = 20;
    
    public ServiceLocator generateLocatorPerTenant(String tenantName) {
        if (tenantName == null) throw new IllegalArgumentException();
        
        Environment env;
        if (ALICE.equals(tenantName)) {
            env = new EnvironmentImpl(ALICE, ALICE_MIN, ALICE_MAX);
        }
        else if (BOB.equals(tenantName)) {
            env = new EnvironmentImpl(BOB, BOB_MIN, BOB_MAX);
        }
        else {
            env = new EnvironmentImpl(tenantName, 0, 100);
        }
        
        
        
        ServiceLocator retVal = factory.create(tenantName);
        DynamicConfigurationService dcs = retVal.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(
                BuilderHelper.createConstantDescriptor(env));
        
        config.commit();
        
        return retVal;
    }
    
    private static class EnvironmentImpl implements Environment {
        private final String name;
        private final int min;
        private final int max;
        
        private EnvironmentImpl(String name, int min, int max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        /* (non-Javadoc)
         * @see org.glassfish.examples.ctm.Environment#getName()
         */
        @Override
        public String getName() {
            return name;
        }

        /* (non-Javadoc)
         * @see org.glassfish.examples.ctm.Environment#getMinSize()
         */
        @Override
        public int getMinSize() {
            return min;
        }

        /* (non-Javadoc)
         * @see org.glassfish.examples.ctm.Environment#getMaxSize()
         */
        @Override
        public int getMaxSize() {
            return max;
        }
        
    }

}
