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
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.Populator;

/**
 * This is the class that creates the service locator for each
 * known tenant.
 * 
 * @author jwells
 * @author andriy.zhdanov
 */
public class TenantLocatorGenerator {
    private final static ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();
    public final static String ALICE = "Alice";
    public final static int ALICE_MIN = 1;
    public final static int ALICE_MAX = 2;
    
    public final static String BOB = "Bob";
    public final static int BOB_MIN = 10;
    public final static int BOB_MAX = 20;
    
    public final static String CTM_LOCATOR_NAME = "CTMTest";
    
    public ServiceLocator generateLocatorPerTenant(String tenantName) {
        if (tenantName == null) throw new IllegalArgumentException();

        ServiceLocator parent = ServiceLocatorFactory.getInstance().find(CTM_LOCATOR_NAME);
        
        ServiceLocator serviceLocator = factory.create(tenantName, parent);

        // Will add itself to serviceLocator by tenantName
        Habitat h = new Habitat(null, tenantName);
        
        // Populate this serviceLocator with config data.
        //
        // Note, populator comes from parent service locator, though it gets
        // Habitat injected from this service locator.  But it does not work
        // with HK2Populator.populateConfig, probably because habitat there
        // is marked as @Optional 
        // 
        // CAUTION: this must be done by direct lookup of EnvironmentXml in real life,
        // to avoid populating with everything possible.
        for (Populator p : serviceLocator.<Populator>getAllServices(Populator.class)) {
            p.run(new ConfigParser(h));
        }
        
        return serviceLocator;
    }
}
