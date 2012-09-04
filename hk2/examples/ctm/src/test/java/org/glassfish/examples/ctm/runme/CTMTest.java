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
package org.glassfish.examples.ctm.runme;

import java.beans.PropertyVetoException;
import java.io.IOException;

import junit.framework.Assert;

import org.glassfish.examples.ctm.Environment;
import org.glassfish.examples.ctm.ServiceProviderEngine;
import org.glassfish.examples.ctm.TenantLocatorGenerator;
import org.glassfish.examples.ctm.TenantManager;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.bootstrap.HK2Populator;
import org.glassfish.hk2.bootstrap.impl.ClasspathDescriptorFileFinder;
import org.glassfish.hk2.bootstrap.impl.Hk2LoaderPopulatorPostProcessor;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * This runs a simple test to be sure that the ServiceProviderEngine
 * properly reflects the current tenant on the thread
 * 
 * @author jwells
 */
public class CTMTest {
    private final static String TEST_NAME = TenantLocatorGenerator.CTM_LOCATOR_NAME;
    private final static ServiceLocator locator = ServiceLocatorFactory.getInstance().create(TEST_NAME);
    
    @BeforeClass
    public static void before() throws IOException {
        new Habitat(null, TEST_NAME);  // Adds a habitat to the base CTM locator
        
        HK2Populator.populate(locator,
                new ClasspathDescriptorFileFinder(),
                new Binder() {

					@Override
					public void bind(DynamicConfiguration config) {
						// TODO Auto-generated method stub
						config.bind(BuilderHelper.createConstantDescriptor(new Hk2LoaderPopulatorPostProcessor(null)));
					}}
                );
    }
    
    @Test
    public void testProviderEngineUsesCorrectTenant() throws TransactionFailure {
        TenantManager tenantManager = locator.getService(TenantManager.class);
        ServiceProviderEngine engine = locator.getService(ServiceProviderEngine.class);
        
        // TODO:  Eventually these hard-coded names would go away...
        tenantManager.setCurrentTenant(TenantLocatorGenerator.ALICE);
        
        Assert.assertEquals(TenantLocatorGenerator.ALICE, engine.getTenantName());
        Assert.assertEquals(TenantLocatorGenerator.ALICE_MAX, engine.getTenantMax());
        Assert.assertEquals(TenantLocatorGenerator.ALICE_MIN, engine.getTenantMin());
        
        tenantManager.setCurrentTenant(TenantLocatorGenerator.BOB);
        
        Assert.assertEquals(TenantLocatorGenerator.BOB, engine.getTenantName());
        Assert.assertEquals(TenantLocatorGenerator.BOB_MAX, engine.getTenantMax());
        Assert.assertEquals(TenantLocatorGenerator.BOB_MIN, engine.getTenantMin());

        // just make sure we can modify it also (HK2-78)
        // caution, it implicitly uses SimpleConfigBeanDomDecorator
        // from config-api.test-jar
        Environment env = engine.getEnvironment();
        ConfigSupport.apply(new SingleConfigCode<Environment>() {

            @Override
            public Object run(Environment param) throws PropertyVetoException,
                    TransactionFailure {
                return null;
            }
            
        }, env);

        new Transaction().enroll(env);
    }
}
