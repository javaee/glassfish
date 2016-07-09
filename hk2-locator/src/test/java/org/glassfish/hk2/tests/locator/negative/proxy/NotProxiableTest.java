/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.negative.proxy;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class NotProxiableTest {
    /**
     * Sanity test, just makes sure ProxiableSingleton basically works
     */
    @Test
    public void testSanity() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ProxiableSingletonContext.class,
                SimpleService.class,
                InjectAProxiableService.class);
        
        InjectAProxiableService iaps = locator.getService(InjectAProxiableService.class);
        iaps.checkSS();
    }
    
    /**
     * Makes sure a non-proxiable scalar type (integer) cannot be
     * proxied
     */
    @Test
    public void testInjectingAProxiableInteger() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ProxiableSingletonContext.class,
                IntegerFactory.class,
                InjectAProxiableInteger.class);
        
        try {
          locator.getService(InjectAProxiableInteger.class);
          Assert.fail("Should not have been able to proxy an integer");
        }
        catch (MultiException me) {
            for (Throwable th : me.getErrors()) {
                if (th instanceof RuntimeException) {
                    Assert.assertTrue(th.getMessage().contains("final"));
                    return;
                }
            }
            
            // Fail the test, didn't get expected exception
            throw me;
        }
    }
    
    /**
     * Makes sure a non-proxiable scalar type (integer) cannot be
     * proxied
     */
    @Test
    public void testInjectingANonProxiableClass() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ProxiableSingletonContext.class,
                NonProxiableFinalClass.class,
                InjectANonProxiableClass.class);
        
        try {
          locator.getService(InjectANonProxiableClass.class);
          Assert.fail("Should not have been able to proxy a non proxiable class");
        }
        catch (MultiException me) {
            for (Throwable th : me.getErrors()) {
                if (th instanceof RuntimeException) {
                    Assert.assertTrue(th.getMessage().contains("final"));
                    return;
                }
            }
            
            // Fail the test, didn't get expected exception
            throw me;
        }
    }

}
