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
package org.glassfish.hk2.tests.locator.iterableinject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class IterableInjectTest {
    public static final String ALICE = "Alice";
    public static final String BOB = "Bob";
    
    /**
     * Tests the most basic iterable injection
     */
    @Test @org.junit.Ignore
    public void testBasicListInjection() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                AliceService.class,
                BobService.class,
                BasicIterableInjectingService.class);
        
        BasicIterableInjectingService blis = locator.getService(BasicIterableInjectingService.class);
        Iterable<NamedService> allNamed = blis.getAllNamed();
        
        AliceService alice = null;
        BobService bob = null;
        int count = 0;
        for (NamedService ns : allNamed) {
            count++;
            if (ns instanceof AliceService) {
                alice = (AliceService) ns;
            }
            else if (ns instanceof BobService) {
                bob = (BobService) ns;
            }
            else {
                Assert.fail("unknown ns type: " + ns);
            }
        }
        
        Assert.assertEquals(2, count);
        
        Assert.assertNotNull(alice);
        Assert.assertNotNull(bob);
        
        Assert.assertEquals(ALICE, alice.getName());
        Assert.assertEquals(BOB, bob.getName());
    }
    
    /**
     * Tests the most basic iterable injection
     */
    @Test @org.junit.Ignore
    public void testListInjectionWithQualifier() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                AliceService.class,
                BobService.class,
                AliceIterableInjectionService.class);
        
        AliceIterableInjectionService blis = locator.getService(AliceIterableInjectionService.class);
        Iterable<NamedService> allNamed = blis.getAllAlice();
        
        AliceService alice = null;
        BobService bob = null;
        int count = 0;
        for (NamedService ns : allNamed) {
            count++;
            if (ns instanceof AliceService) {
                alice = (AliceService) ns;
            }
            else if (ns instanceof BobService) {
                bob = (BobService) ns;
            }
            else {
                Assert.fail("unknown ns type: " + ns);
            }
        }
        
        Assert.assertEquals(1, count);
        
        Assert.assertNotNull(alice);
        Assert.assertNull(bob);
        
        Assert.assertEquals(ALICE, alice.getName());
    }
    
    /**
     * Tests that this works with a qualifier that contains values
     */
    @Test @org.junit.Ignore
    public void testQualifierWithValue() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(
                TernaryServices.NeitherOne.class,
                TernaryServices.NeitherTwo.class,
                TernaryServices.NeitherThree.class,
                TernaryServices.TrueOne.class,
                TernaryServices.TrueTwo.class,
                TernaryInjectedService.class);
        
        TernaryInjectedService tis = locator.getService(TernaryInjectedService.class);
        
        Assert.assertEquals(3, tis.getNumNeithers());
        Assert.assertEquals(2, tis.getNumTrues());
        Assert.assertEquals(0, tis.getNumFalses());
        Assert.assertEquals(5, tis.getNumAlls());
    }

}
