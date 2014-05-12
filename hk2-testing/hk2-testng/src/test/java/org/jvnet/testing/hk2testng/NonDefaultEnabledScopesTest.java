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
package org.jvnet.testing.hk2testng;

import javax.inject.Inject;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.testing.hk2testng.service.GenericInterface;
import org.jvnet.testing.hk2testng.service.PerThreadService;
import org.jvnet.testing.hk2testng.service.impl.ImmediateServiceImpl;
import org.jvnet.testing.hk2testng.service.impl.SimpleService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jwells
 *
 */
@HK2(enablePerThread = false, enableImmediate = false, enableLookupExceptions = false)
public class NonDefaultEnabledScopesTest {
    @Inject
    private ServiceLocator locator;
    
    /**
     * Tests that immediate scope is working by default
     * 
     * @throws InterruptedException
     */
    @Test
    public void assertImmediateScopeWorks() throws InterruptedException {
        ServiceLocatorUtilities.addClasses(locator, ImmediateServiceImpl.class);
        
        try {
            locator.getService(ImmediateServiceImpl.class);
            Assert.fail("No context available for ImmediateServiceImpl");
        }
        catch (MultiException me) {
            // success
        }
    }
    
    /**
     * Tests that per thread scope is working by default
     * 
     * @throws InterruptedException
     */
    @Test
    public void assertPerThreadScopeWorks() throws InterruptedException {
        try {
            locator.getService(PerThreadService.class);
            Assert.fail("No context available for PerThreadService");
        }
        catch (MultiException me) {
            // success
        }
    }
    
    /**
     * Tests that reification errors are not rethrown
     */
    @Test
    public void assertReifyExceptionsAreThrown() {
        Descriptor addMe = BuilderHelper.link(SimpleService.class.getName()).
                to(GenericInterface.class.getName()).
                andLoadWith(new HK2Loader() {

                    @Override
                    public Class<?> loadClass(String className)
                            throws MultiException {
                        throw new MultiException(new ClassNotFoundException("Could not find " + className));
                    }
                    
                }).build();
        
        ServiceLocatorUtilities.addOneDescriptor(locator, addMe);
        
        GenericInterface gi = locator.getService(GenericInterface.class);
        assertThat(gi).isNull();
    }

}
