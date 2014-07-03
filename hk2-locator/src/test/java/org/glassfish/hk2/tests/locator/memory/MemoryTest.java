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
package org.glassfish.hk2.tests.locator.memory;

import java.util.List;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;

/**
 * These tests are funnny, in that they at one time or another exhibited a memory
 * leak.  In many cases the tests here are normal scenarios just run over
 * and over again, and the test can not detect the actual memory leak that
 * took place.  However, they are useful tests in any case, and should
 * each have some comment in them describing the memory leak that inspired
 * the test case
 * 
 * @author jwells
 *
 */
public class MemoryTest {
    /**
     * This test causes the injecteeToResolverCache in ServiceLocator
     * impl to grow, as every time reifyDescriptor is called
     * a new SystemDescriptor is created which has new Injectee instances.
     * Since the cache was based on the identity of the object, this
     * caused the cache to continually grow
     */
    @Test @org.junit.Ignore
    public void testDirectlyCallingCreate() {
        ServiceLocator locator = LocatorHelper.create();
        
        ServiceLocatorUtilities.addClasses(locator,
                SimpleService.class);
        
        for (int lcv = 0; lcv < 100; lcv++) {
            ActiveDescriptor<?> issDescriptor = BuilderHelper.activeLink(InjectsSimpleServiceService.class).
                    in(Singleton.class).
                    build();
            
            issDescriptor = locator.reifyDescriptor(issDescriptor);
            
            InjectsSimpleServiceService iss = (InjectsSimpleServiceService) issDescriptor.create(null);
        }
        
    }

}
