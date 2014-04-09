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
package org.jvnet.hk2.guice.bridge.test.bidirectional;

import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.jvnet.hk2.guice.bridge.api.HK2IntoGuiceBridge;
import org.jvnet.hk2.guice.bridge.test.utilities.Utilities;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author jwells
 *
 */
public class BiDirectionalBridgeTest {
    private final static ServiceLocator testLocator = Utilities.createLocator("BiDirectionalBridgeTest", new BiDirectionalBridgeModule());
    
    private Injector injector;
    
    private static Injector createBiDirectionalGuiceBridge(ServiceLocator serviceLocator,
            Module... applicationModules) {
        Module allModules[] = new Module[applicationModules.length + 1];
        
        allModules[0] = new HK2IntoGuiceBridge(serviceLocator);
        for (int lcv = 0; lcv < applicationModules.length; lcv++) {
            allModules[lcv + 1] = applicationModules[lcv];
        }
        
        Injector injector = Guice.createInjector(allModules);
        
        GuiceIntoHK2Bridge g2h = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        g2h.bridgeGuiceInjector(injector);
        
        return injector;
    }
    
    @Before
    public void before() {
        // Setup the bidirection bridge
        injector = createBiDirectionalGuiceBridge(testLocator,
                new AbstractModule() {

                    @Override
                    protected void configure() {
                        bind(GuiceService1_1.class);
                        bind(GuiceService1_3.class);
                    }
            
        });
    }
    
    /**
     * In this test we get a guice service
     * that injects an hk2 service that
     * injects a guice service that injects
     * an hk2 service
     */
    @Test
    public void testGuiceHK2GuiceHK2() {
        GuiceService1_3 guice3 = injector.getInstance(GuiceService1_3.class);
        Assert.assertNotNull(guice3);
        
        guice3.check();
    }

}
