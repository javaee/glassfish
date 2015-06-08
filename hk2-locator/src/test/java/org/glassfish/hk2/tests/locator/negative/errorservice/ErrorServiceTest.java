/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.negative.errorservice;

import java.util.List;

import junit.framework.Assert;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Test;

/**
 * Note that the ordering of the test methods in this
 * class is very important.  Therefore we do not rely
 * on the Junit ordering of &#64;Test methods, instead
 * having one test that calls the other tests in the
 * order in which they have to go in order to
 * function properly
 *
 * @author jwells
 *
 */
public class ErrorServiceTest {
    private final static String TEST_NAME = "ErrorServiceTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new ErrorServiceModule());

    /** The string the exception will throw */
    public final static String EXCEPTION_STRING = "Expected Exception";

    /** Another exception string */
    public final static String EXCEPTION_STRING_DUEX = "Expected Exception Duex";

    private void testLookupPriorToFixing(ErrorServiceImpl esi) {
        esi.clear();

        FaultyClass fc = locator.getService(FaultyClass.class);
        Assert.assertNull(fc);

        Descriptor faultyDesc = locator.getBestDescriptor(BuilderHelper.createContractFilter(FaultyClass.class.getName()));
        Assert.assertNotNull(faultyDesc);

        ActiveDescriptor<?> fromError = esi.getDescriptor();
        Assert.assertNotNull(fromError);

        Assert.assertEquals(faultyDesc, fromError);

        Assert.assertNull(esi.getInjectee());

        MultiException me = esi.getMe();
        Assert.assertNotNull(me);

        Assert.assertTrue("Expected " + EXCEPTION_STRING + " but got " + me.getMessage(), me.getMessage().contains(EXCEPTION_STRING));
    }

    private void testLookupInjecteePriorToFixing(ErrorServiceImpl esi) {
        esi.clear();

        try {
            locator.getService(InjectedWithFaultyClass.class);
            Assert.fail("The bad injection point would cause this to fail");
        }
        catch (MultiException me) {
            Assert.assertTrue(me.getMessage(),
                    me.getMessage().contains("There was no object available for injection at "));

        }

        Descriptor faultyDesc = locator.getBestDescriptor(BuilderHelper.createContractFilter(FaultyClass.class.getName()));
        Assert.assertNotNull(faultyDesc);

        ActiveDescriptor<?> fromError = esi.getDescriptor();
        Assert.assertNotNull(fromError);

        Assert.assertEquals(faultyDesc, fromError);

        Assert.assertNotNull(esi.getInjectee());

        MultiException me = esi.getMe();
        Assert.assertNotNull(me);

        Assert.assertTrue("Expected " + EXCEPTION_STRING + " but got " + me.getMessage(), me.getMessage().contains(EXCEPTION_STRING));
    }

    private void testLookupHandlesPriorToFixing(ErrorServiceImpl esi) {
        esi.clear();

        List<FaultyClass> faulties = locator.getAllServices(
                FaultyClass.class);
        Assert.assertTrue("faulties.size=" + faulties.size(), faulties.isEmpty());

        Descriptor faultyDesc = locator.getBestDescriptor(BuilderHelper.createContractFilter(FaultyClass.class.getName()));
        Assert.assertNotNull(faultyDesc);

        ActiveDescriptor<?> fromError = esi.getDescriptor();
        Assert.assertNotNull(fromError);

        Assert.assertEquals(faultyDesc, fromError);

        Assert.assertNull(esi.getInjectee());

        MultiException me = esi.getMe();
        Assert.assertNotNull(me);

        Assert.assertTrue("Expected " + EXCEPTION_STRING + " but got " + me.getMessage(), me.getMessage().contains(EXCEPTION_STRING));
    }

    private void testLookupHandlesWithContractPriorToFixing(ErrorServiceImpl esi) {
        esi.clear();

        List<ServiceHandle<FaultyClass>> handles = locator.getAllServiceHandles(FaultyClass.class);
        Assert.assertTrue("handles.size=" + handles.size(), handles.isEmpty());

        Descriptor faultyDesc = locator.getBestDescriptor(BuilderHelper.createContractFilter(FaultyClass.class.getName()));
        Assert.assertNotNull(faultyDesc);

        ActiveDescriptor<?> fromError = esi.getDescriptor();
        Assert.assertNotNull(fromError);

        Assert.assertEquals(faultyDesc, fromError);

        Assert.assertNull(esi.getInjectee());

        MultiException me = esi.getMe();
        Assert.assertNotNull(me);

        Assert.assertTrue("Expected " + EXCEPTION_STRING + " but got " + me.getMessage(), me.getMessage().contains(EXCEPTION_STRING));
    }

    private void testLookupPriorToFixingButThrowing(ErrorServiceImpl esi) {
        esi.doThrow();
        esi.clear();

        try {
            locator.getService(FaultyClass.class);
            Assert.fail("The error service now throws an assertion error");
        }
        catch (MultiException me) {
            Assert.assertTrue("Expected " + EXCEPTION_STRING_DUEX + " but got " + me.getMessage(),
                    me.getMessage().contains(EXCEPTION_STRING_DUEX));

        }

        ActiveDescriptor<?> fromError = esi.getDescriptor();
        Assert.assertNotNull(fromError);

        Assert.assertNull(esi.getInjectee());

        MultiException me = esi.getMe();
        Assert.assertNotNull(me);

        Assert.assertTrue("Expected " + EXCEPTION_STRING + " but got " + me.getMessage(), me.getMessage().contains(EXCEPTION_STRING));
    }

    private void testLookupPriorToFixingButReThrowing(ErrorServiceImpl esi) {
        esi.reThrow();
        esi.clear();

        try {
            locator.getService(FaultyClass.class);
            Assert.fail("The error service now rethrows the ME error");
        }
        catch (MultiException me) {
            Assert.assertTrue("Expected " + EXCEPTION_STRING + " but got " + me.getMessage(), me.getMessage().contains(EXCEPTION_STRING));
        }

        ActiveDescriptor<?> fromError = esi.getDescriptor();
        Assert.assertNotNull(fromError);

        Assert.assertNull(esi.getInjectee());

        MultiException me = esi.getMe();
        Assert.assertNotNull(me);

        Assert.assertTrue("Expected " + EXCEPTION_STRING + " but got " + me.getMessage(), me.getMessage().contains(EXCEPTION_STRING));
    }

    private void testLookupAfterFixing() {
        FaultyClass fc = locator.getService(FaultyClass.class);
        Assert.assertNotNull(fc);
    }

    private void testLookupInjecteeAfterFixing() {
        InjectedWithFaultyClass iwfc = locator.getService(InjectedWithFaultyClass.class);
        Assert.assertNotNull(iwfc);
    }

    /**
     * This test ensures that the other methods are called in the proper order
     */
    @Test
    public void testOrdered() {
        ErrorServiceImpl esi = locator.getService(ErrorServiceImpl.class);

        testLookupPriorToFixing(esi);
        testLookupInjecteePriorToFixing(esi);
        testLookupHandlesPriorToFixing(esi);
        testLookupHandlesWithContractPriorToFixing(esi);
        testLookupPriorToFixingButThrowing(esi);
        testLookupPriorToFixingButReThrowing(esi);

        TempermentalLoader.fixIt();

        testLookupAfterFixing();
        testLookupInjecteeAfterFixing();
    }

}
