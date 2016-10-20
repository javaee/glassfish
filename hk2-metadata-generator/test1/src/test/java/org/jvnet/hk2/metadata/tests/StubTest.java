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
package org.jvnet.hk2.metadata.tests;

import java.sql.Connection;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.metadata.tests.faux.stub.AbstractService;
import org.jvnet.hk2.metadata.tests.faux.stub.ConnectionStub;
import org.jvnet.hk2.metadata.tests.faux.stub.FailingLargeInterfaceStub;
import org.jvnet.hk2.metadata.tests.faux.stub.InterfaceWithTypes;
import org.jvnet.hk2.metadata.tests.faux.stub.NamedBean;
import org.jvnet.hk2.metadata.tests.stub.LargeInterface;

/**
 * @author jwells
 *
 */
public class StubTest {
    /**
     * Makes sure that the stubbed interface is used not the one from the main jar
     */
    @Test
    public void testGetsStubImplementationRatherThanOneFromMain() {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        
        LargeInterface li = locator.getService(LargeInterface.class);
        
        Assert.assertEquals(0, li.methodInt(27));
    }
    
    /**
     * Makes sure that the stubbed interface is used not the one from the main jar
     */
    @Test
    public void testInnerClassCanBeStubbed() {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        
        AbstractService li = locator.getService(AbstractService.class);
        
        Assert.assertNotNull(li.getRandomBeanStub());
    }
    
    /**
     * Ensures that {@link javax.inject.Named} works in a stub
     */
    @Test
    public void testNamedWorks() {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        
        NamedBean rawNamedService = locator.getService(NamedBean.class, "NamedBeanStub");
        Assert.assertEquals("NamedBeanStub", rawNamedService.getName());
        
        NamedBean aliceService = locator.getService(NamedBean.class, InhabitantsGeneratorTest.ALICE);
        Assert.assertEquals(InhabitantsGeneratorTest.ALICE, aliceService.getName());
        
        Assert.assertNull(rawNamedService.getAddress());
        Assert.assertNull(aliceService.getAddress());
    }
    
    /**
     * Ensures that the exception version of the stub works properly
     */
    @Test
    public void testExceptionTypeStub() {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        
        FailingLargeInterfaceStub stub = locator.getService(FailingLargeInterfaceStub.class);
        
        Assert.assertTrue(stub.notOverridden(false));
        
        try {
            stub.methodBoolean(true);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodVoids();
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodByte((byte) 0);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
            
        try {
            stub.methodChar('a');
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodDouble((double) 0.0);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodFloat((float) 0.0);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
          stub.methodInt(0);
          Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodInt(0);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        
        try {
            stub.methodShort((short) 0);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodDeclared(null, null, null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodBooleanArray(null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodByteArray(null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodCharArray(null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodDoubleArray(null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodFloatArray(null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodIntArray((int[]) null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodIntArray((long[][][][][]) null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodShortArray(null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
        
        try {
            stub.methodDeclaredArray(null, null);
            Assert.fail("Should have thrown exception");
        }
        catch (UnsupportedOperationException uoe) {
            // ok
        }
    }
    
    /**
     * Ensures that the exception version of the stub works properly
     */
    @Test
    public void testStubsWithTypeVariables() {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        
        InterfaceWithTypes<?,?> stub = locator.getService(InterfaceWithTypes.class);
        
        Assert.assertNull(stub.get(null));
        Assert.assertNull(stub.reverseGet(null));
    }
    
    /**
     * Tests that we can use ContractsProvided on a stub for stubbing
     * things we have no control over (like SQL stuff)
     */
    @Test
    // @org.junit.Ignore
    public void testStubWithProvidedContracts() {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        
        Connection conn = locator.getService(Connection.class);
        Assert.assertNotNull(conn);
        
        ConnectionStub stub = locator.getService(ConnectionStub.class);
        Assert.assertNotNull(stub);
        
        Assert.assertEquals(stub, conn);
        
    }

}
